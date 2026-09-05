/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/qr/utils
 * File: QrPayloadParserTest.kt
 * Description: Unit tests verifying QR and barcode payload parsing across UPI, Wi-Fi, vCard, and web URLs.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.qr.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class QrPayloadParserTest {

    @Test
    fun `parse web url with https`() {
        val raw = "https://balajitechlab.com/projects?ref=quickdash"
        val result = QrPayloadParser.parse(raw)

        assertThat(result).isInstanceOf(QrParsedResult.WebUrl::class.java)
        val urlResult = result as QrParsedResult.WebUrl
        assertThat(urlResult.url).isEqualTo("https://balajitechlab.com/projects?ref=quickdash")
        assertThat(urlResult.host).isEqualTo("balajitechlab.com")
        assertThat(urlResult.title).isEqualTo("Website Link")
    }

    @Test
    fun `parse web url with www prefix`() {
        val raw = "www.github.com/balajitechlabs"
        val result = QrPayloadParser.parse(raw)

        assertThat(result).isInstanceOf(QrParsedResult.WebUrl::class.java)
        val urlResult = result as QrParsedResult.WebUrl
        assertThat(urlResult.url).isEqualTo("https://www.github.com/balajitechlabs")
        assertThat(urlResult.host).isEqualTo("www.github.com")
    }

    @Test
    fun `parse upi payment uri with amount and payee`() {
        val raw = "upi://pay?pa=balaji@okaxis&pn=Balaji+Tech+Labs&am=500.00&tn=QuickDash+Pro"
        val result = QrPayloadParser.parse(raw)

        assertThat(result).isInstanceOf(QrParsedResult.UpiPayment::class.java)
        val upiResult = result as QrParsedResult.UpiPayment
        assertThat(upiResult.upiId).isEqualTo("balaji@okaxis")
        assertThat(upiResult.payeeName).isEqualTo("Balaji Tech Labs")
        assertThat(upiResult.amount).isEqualTo("500.00")
        assertThat(upiResult.note).isEqualTo("QuickDash Pro")
        assertThat(upiResult.summary).contains("₹500.00 to Balaji Tech Labs")
    }

    @Test
    fun `parse wifi configuration`() {
        val raw = "WIFI:S:Office_5G;T:WPA;P:SuperSecretPass123;;"
        val result = QrPayloadParser.parse(raw)

        assertThat(result).isInstanceOf(QrParsedResult.WifiNetwork::class.java)
        val wifiResult = result as QrParsedResult.WifiNetwork
        assertThat(wifiResult.ssid).isEqualTo("Office_5G")
        assertThat(wifiResult.authType).isEqualTo("WPA")
        assertThat(wifiResult.password).isEqualTo("SuperSecretPass123")
    }

    @Test
    fun `parse vcard contact`() {
        val raw = "BEGIN:VCARD\nVERSION:3.0\nFN:Balaji Developer\n" +
            "TEL:+919876543210\nEMAIL:admin@balajitechlab.com\nEND:VCARD"
        val result = QrPayloadParser.parse(raw)

        assertThat(result).isInstanceOf(QrParsedResult.ContactCard::class.java)
        val contact = result as QrParsedResult.ContactCard
        assertThat(contact.name).isEqualTo("Balaji Developer")
        assertThat(contact.phone).isEqualTo("+919876543210")
        assertThat(contact.email).isEqualTo("admin@balajitechlab.com")
    }

    @Test
    fun `parse phone number`() {
        val raw = "tel:+918025251234"
        val result = QrPayloadParser.parse(raw)

        assertThat(result).isInstanceOf(QrParsedResult.PhoneNumber::class.java)
        val phone = result as QrParsedResult.PhoneNumber
        assertThat(phone.phone).isEqualTo("+918025251234")
    }

    @Test
    fun `parse email address with subject`() {
        val raw = "mailto:support@balajitechlab.com?subject=QuickDash+Feedback"
        val result = QrPayloadParser.parse(raw)

        assertThat(result).isInstanceOf(QrParsedResult.EmailAddress::class.java)
        val email = result as QrParsedResult.EmailAddress
        assertThat(email.email).isEqualTo("support@balajitechlab.com")
        assertThat(email.subject).isEqualTo("QuickDash Feedback")
    }

    @Test
    fun `parse ean 13 product barcode`() {
        val raw = "8901030948214"
        val result = QrPayloadParser.parse(raw)

        assertThat(result).isInstanceOf(QrParsedResult.ProductBarcode::class.java)
        val barcode = result as QrParsedResult.ProductBarcode
        assertThat(barcode.code).isEqualTo("8901030948214")
    }

    @Test
    fun `parse plain text fallback`() {
        val raw = "Just some standard notes or arbitrary code snippet"
        val result = QrPayloadParser.parse(raw)

        assertThat(result).isInstanceOf(QrParsedResult.PlainText::class.java)
        assertThat(result.raw).isEqualTo(raw)
    }

    @Test
    fun `parse upi payment uri with encoded special characters and note`() {
        val raw = "upi://pay?pa=merchant%40upi&pn=BTL%20Enterprises&am=1250.50&tn=Order%20%239928%20Paid"
        val result = QrPayloadParser.parse(raw)

        assertThat(result).isInstanceOf(QrParsedResult.UpiPayment::class.java)
        val upiResult = result as QrParsedResult.UpiPayment
        assertThat(upiResult.upiId).isEqualTo("merchant@upi")
        assertThat(upiResult.payeeName).isEqualTo("BTL Enterprises")
        assertThat(upiResult.amount).isEqualTo("1250.50")
        assertThat(upiResult.note).isEqualTo("Order #9928 Paid")
    }

    @Test
    fun `parse ean 8 product barcode`() {
        val raw = "96385074"
        val result = QrPayloadParser.parse(raw)

        assertThat(result).isInstanceOf(QrParsedResult.ProductBarcode::class.java)
        val barcode = result as QrParsedResult.ProductBarcode
        assertThat(barcode.code).isEqualTo("96385074")
    }

    @Test
    fun `numeric string above 14 digits falls back to plain text`() {
        val raw = "12345678901234567890" // 20 digits, e.g. timestamp or arbitrary number
        val result = QrPayloadParser.parse(raw)

        assertThat(result).isInstanceOf(QrParsedResult.PlainText::class.java)
    }
}

