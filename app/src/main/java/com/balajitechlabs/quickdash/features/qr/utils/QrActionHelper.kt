/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/qr/utils
 * File: QrActionHelper.kt
 * Description: Intents and dispatchers executing primary contextual actions for scanned QR codes.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.qr.utils

import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

object QrActionHelper {

    fun getPayloadIcon(payload: QrParsedResult): ImageVector = when (payload) {
        is QrParsedResult.WebUrl -> Icons.Rounded.Language
        is QrParsedResult.UpiPayment -> Icons.Rounded.AccountBalanceWallet
        is QrParsedResult.WifiNetwork -> Icons.Rounded.Wifi
        is QrParsedResult.ContactCard -> Icons.Rounded.Person
        is QrParsedResult.PhoneNumber -> Icons.Rounded.Call
        is QrParsedResult.EmailAddress -> Icons.Rounded.Email
        is QrParsedResult.ProductBarcode -> Icons.Rounded.QrCode
        is QrParsedResult.PlainText -> Icons.Rounded.TextFields
    }

    fun getActionIcon(payload: QrParsedResult): ImageVector = when (payload) {
        is QrParsedResult.WebUrl -> Icons.Rounded.OpenInBrowser
        is QrParsedResult.UpiPayment -> Icons.Rounded.AccountBalanceWallet
        is QrParsedResult.WifiNetwork -> Icons.Rounded.ContentCopy
        is QrParsedResult.ContactCard -> Icons.Rounded.Person
        is QrParsedResult.PhoneNumber -> Icons.Rounded.Call
        is QrParsedResult.EmailAddress -> Icons.Rounded.Email
        is QrParsedResult.ProductBarcode -> Icons.Rounded.Search
        is QrParsedResult.PlainText -> Icons.Rounded.Search
    }

    fun getActionLabel(payload: QrParsedResult): String = when (payload) {
        is QrParsedResult.WebUrl -> "Open Link"
        is QrParsedResult.UpiPayment -> "Pay via UPI"
        is QrParsedResult.WifiNetwork -> "Copy Password"
        is QrParsedResult.ContactCard -> "Add Contact"
        is QrParsedResult.PhoneNumber -> "Call Number"
        is QrParsedResult.EmailAddress -> "Send Email"
        is QrParsedResult.ProductBarcode -> "Search Product"
        is QrParsedResult.PlainText -> "Search Web"
    }

    fun executePrimaryAction(context: Context, payload: QrParsedResult) {
        try {
            when (payload) {
                is QrParsedResult.WebUrl -> openUrlIntent(context, payload.url)
                is QrParsedResult.UpiPayment -> launchUpiPayment(context, payload.raw)
                is QrParsedResult.WifiNetwork -> copyWifiPassword(context, payload)
                is QrParsedResult.ContactCard -> insertContactCard(context, payload)
                is QrParsedResult.PhoneNumber -> dialPhoneNumber(context, payload.phone)
                is QrParsedResult.EmailAddress -> composeEmail(context, payload)
                is QrParsedResult.ProductBarcode -> searchBarcodeOnline(context, payload.code)
                is QrParsedResult.PlainText -> searchWebQuery(context, payload.raw)
            }
        } catch (_: Exception) {
            copyToClipboard(context, payload.raw)
        }
    }

    fun openUrlIntent(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun launchUpiPayment(context: Context, raw: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(raw)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Pay using UPI").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun copyWifiPassword(context: Context, wifi: QrParsedResult.WifiNetwork) {
        val pass = wifi.password ?: wifi.ssid
        copyToClipboard(context, pass)
        Toast.makeText(context, "Password copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun insertContactCard(context: Context, card: QrParsedResult.ContactCard) {
        val intent = Intent(Intent.ACTION_INSERT_OR_EDIT).apply {
            type = ContactsContract.Contacts.CONTENT_ITEM_TYPE
            if (!card.name.isNullOrBlank()) putExtra(ContactsContract.Intents.Insert.NAME, card.name)
            if (!card.phone.isNullOrBlank()) putExtra(ContactsContract.Intents.Insert.PHONE, card.phone)
            if (!card.email.isNullOrBlank()) putExtra(ContactsContract.Intents.Insert.EMAIL, card.email)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun dialPhoneNumber(context: Context, phone: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun composeEmail(context: Context, email: QrParsedResult.EmailAddress) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${email.email}")).apply {
            if (!email.subject.isNullOrBlank()) putExtra(Intent.EXTRA_SUBJECT, email.subject)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun searchBarcodeOnline(context: Context, code: String) {
        val searchUrl = "https://www.google.com/search?q=$code"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun searchWebQuery(context: Context, query: String) {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, query)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun copyToClipboard(context: Context, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("Scanned Code", text))
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun shareText(context: Context, text: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, "Share scanned code").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
