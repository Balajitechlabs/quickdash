/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/qr/utils
 * File: QrPayloadParser.kt
 * Description: Parses QR and barcode payloads into structured types (UPI, Wi-Fi, vCard, URLs).
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.qr.utils

import java.net.URLDecoder

sealed class QrParsedResult(
    open val raw: String,
    open val title: String,
    open val summary: String,
    open val attributes: List<Pair<String, String>> = emptyList()
) {
    data class WebUrl(
        override val raw: String,
        val url: String,
        val host: String
    ) : QrParsedResult(
        raw = raw,
        title = "Website Link",
        summary = host,
        attributes = listOf("URL" to url)
    )

    data class UpiPayment(
        override val raw: String,
        val upiId: String,
        val payeeName: String?,
        val amount: String?,
        val note: String?
    ) : QrParsedResult(
        raw = raw,
        title = "UPI Payment",
        summary = if (!amount.isNullOrBlank()) "₹$amount to ${payeeName ?: upiId}" else "Pay ${payeeName ?: upiId}",
        attributes = buildList {
            add("UPI ID" to upiId)
            if (!payeeName.isNullOrBlank()) add("Payee" to payeeName)
            if (!amount.isNullOrBlank()) add("Amount" to "₹$amount")
            if (!note.isNullOrBlank()) add("Note" to note)
        }
    )

    data class WifiNetwork(
        override val raw: String,
        val ssid: String,
        val authType: String,
        val password: String?
    ) : QrParsedResult(
        raw = raw,
        title = "Wi-Fi Network",
        summary = ssid,
        attributes = buildList {
            add("Network (SSID)" to ssid)
            add("Security" to authType)
            if (!password.isNullOrBlank()) add("Password" to password)
        }
    )

    data class ContactCard(
        override val raw: String,
        val name: String?,
        val phone: String?,
        val email: String?
    ) : QrParsedResult(
        raw = raw,
        title = "Contact Card",
        summary = name ?: phone ?: "Contact",
        attributes = buildList {
            if (!name.isNullOrBlank()) add("Name" to name)
            if (!phone.isNullOrBlank()) add("Phone" to phone)
            if (!email.isNullOrBlank()) add("Email" to email)
        }
    )

    data class PhoneNumber(
        override val raw: String,
        val phone: String
    ) : QrParsedResult(
        raw = raw,
        title = "Phone Number",
        summary = phone,
        attributes = listOf("Number" to phone)
    )

    data class EmailAddress(
        override val raw: String,
        val email: String,
        val subject: String?
    ) : QrParsedResult(
        raw = raw,
        title = "Email Address",
        summary = email,
        attributes = buildList {
            add("Email" to email)
            if (!subject.isNullOrBlank()) add("Subject" to subject)
        }
    )

    data class ProductBarcode(
        override val raw: String,
        val code: String
    ) : QrParsedResult(
        raw = raw,
        title = "Product Barcode",
        summary = code,
        attributes = listOf("Barcode" to code)
    )

    data class PlainText(
        override val raw: String
    ) : QrParsedResult(
        raw = raw,
        title = "Plain Text",
        summary = raw.take(80).replace("\n", " "),
        attributes = emptyList()
    )
}

object QrPayloadParser {

    fun parse(raw: String): QrParsedResult {
        val trimmed = raw.trim()

        return when {
            trimmed.startsWith("upi://pay", ignoreCase = true) -> parseUpi(trimmed)
            trimmed.startsWith("WIFI:", ignoreCase = true) -> parseWifi(trimmed)
            trimmed.startsWith("BEGIN:VCARD", ignoreCase = true) ||
                trimmed.startsWith("MECARD:", ignoreCase = true) -> parseContact(trimmed)
            trimmed.startsWith("tel:", ignoreCase = true) -> {
                val number = trimmed.removePrefix("tel:").removePrefix("TEL:")
                QrParsedResult.PhoneNumber(raw = trimmed, phone = number)
            }
            trimmed.startsWith("mailto:", ignoreCase = true) -> parseMailto(trimmed)
            isWebUrl(trimmed) -> parseWebUrl(trimmed)
            // Matches standard numeric GTIN barcode lengths: EAN-8 (8), UPC-A (12), EAN-13 (13), and ITF-14 (14 digits)
            trimmed.matches(Regex("^[0-9]{8,14}$")) -> {
                QrParsedResult.ProductBarcode(raw = trimmed, code = trimmed)
            }
            else -> QrParsedResult.PlainText(raw = trimmed)
        }
    }

    private fun isWebUrl(text: String): Boolean =
        text.startsWith("http://", ignoreCase = true) ||
            text.startsWith("https://", ignoreCase = true) ||
            text.startsWith("www.", ignoreCase = true)

    private fun parseWebUrl(text: String): QrParsedResult.WebUrl {
        val fullUrl = if (text.startsWith("www.", ignoreCase = true)) "https://$text" else text
        val host = fullUrl
            .removePrefix("http://")
            .removePrefix("https://")
            .substringBefore("/")
            .substringBefore("?")
        return QrParsedResult.WebUrl(raw = text, url = fullUrl, host = host)
    }

    private fun parseUpi(uriString: String): QrParsedResult.UpiPayment {
        val query = uriString.substringAfter("?", "")
        val params = query.split("&").mapNotNull { param ->
            val idx = param.indexOf('=')
            if (idx != -1) {
                val key = param.substring(0, idx).trim()
                val value = param.substring(idx + 1)
                key to value
            } else {
                null
            }
        }.toMap()

        val pa = params["pa"]?.let { decodeSafely(it) } ?: ""
        val pn = params["pn"]?.let { decodeSafely(it) }
        val am = params["am"]?.let { decodeSafely(it) }
        val tn = params["tn"]?.let { decodeSafely(it) }

        return QrParsedResult.UpiPayment(
            raw = uriString,
            upiId = pa,
            payeeName = pn,
            amount = am,
            note = tn
        )
    }

    private fun parseWifi(text: String): QrParsedResult.WifiNetwork {
        var ssid = ""
        var authType = "WPA/WPA2"
        var password: String? = null

        val parts = text.removePrefix("WIFI:").removePrefix("wifi:").split(";")
        for (part in parts) {
            when {
                part.startsWith("S:", ignoreCase = true) -> ssid = part.substring(2)
                part.startsWith("T:", ignoreCase = true) -> authType = part.substring(2)
                part.startsWith("P:", ignoreCase = true) -> password = part.substring(2)
            }
        }
        return QrParsedResult.WifiNetwork(
            raw = text,
            ssid = ssid.ifBlank { "Unknown Network" },
            authType = if (authType.isBlank() || authType.equals("nopass", ignoreCase = true)) "Open" else authType,
            password = password
        )
    }

    private fun parseContact(text: String): QrParsedResult.ContactCard {
        var name: String? = null
        var phone: String? = null
        var email: String? = null

        val lines = text.split("\n", ";")
        for (line in lines) {
            val trimmedLine = line.trim()
            when {
                trimmedLine.startsWith("FN:", ignoreCase = true) -> name = trimmedLine.substring(3)
                trimmedLine.startsWith("N:", ignoreCase = true) && name == null -> {
                    name = trimmedLine.substring(2).replace(";", " ").trim()
                }
                trimmedLine.startsWith("TEL:", ignoreCase = true) -> phone = trimmedLine.substring(4)
                trimmedLine.startsWith("EMAIL:", ignoreCase = true) -> email = trimmedLine.substring(6)
            }
        }
        return QrParsedResult.ContactCard(
            raw = text,
            name = name,
            phone = phone,
            email = email
        )
    }

    private fun parseMailto(text: String): QrParsedResult.EmailAddress {
        val emailPart = text.removePrefix("mailto:").removePrefix("MAILTO:")
        val email = emailPart.substringBefore("?")
        val subject = if (emailPart.contains("?")) {
            emailPart.substringAfter("?")
                .split("&")
                .firstOrNull { it.startsWith("subject=", ignoreCase = true) }
                ?.substringAfter("=")
                ?.let { decodeSafely(it) }
        } else {
            null
        }
        return QrParsedResult.EmailAddress(
            raw = text,
            email = email,
            subject = subject
        )
    }

    private fun decodeSafely(value: String): String {
        return try {
            URLDecoder.decode(value, "UTF-8")
        } catch (_: Exception) {
            value
        }
    }
}
