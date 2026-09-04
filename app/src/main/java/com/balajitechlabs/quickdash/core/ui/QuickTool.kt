/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/ui
 * File: QuickTool.kt
 * Description: EssentialX-styled component for core/ui supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.vector.ImageVector
import com.balajitechlabs.quickdash.R

enum class QuickTool {
    UPI,
    CHAT,
    WHATSAPP,
    INSTAGRAM,
    CLIPBOARD,
    NOTES,
    SEARCH,
    WIFI,
    CALCULATOR,
    TIMER,
    CONVERTER,
    TRANSLATOR,
    CAPTURE,
    POMODORO,
    PASSWORD,
    VOICEMEMOS,
    REMINDERS,
    QRSCANNER,
    CONTACT_QR
}

data class ToolDefinition(
    val tool: QuickTool,
    val title: String,
    val description: String,
    val iconRes: Int = 0,
    val category: String = "Tools",
    val imageVector: ImageVector? = null
)

fun toolDefinitions(usePaypal: Boolean = false, cs: ColorScheme? = null): List<ToolDefinition> {
    return listOf(
        ToolDefinition(QuickTool.UPI, if (usePaypal) "Quick PayPal" else "Quick Collect", "Instant payment QR codes & collection", R.drawable.ic_upi_pay, "Finance", Icons.Rounded.QrCode),
        ToolDefinition(QuickTool.CHAT, "Quick Chat", "Direct messaging on WhatsApp & Telegram", R.drawable.ic_whatsapp, "Tools", Icons.AutoMirrored.Rounded.Chat),
        ToolDefinition(QuickTool.CLIPBOARD, "Smart Clipboard", "History manager with search & auto-clear", R.drawable.ic_note, "Tools", Icons.Rounded.ContentPaste),
        ToolDefinition(QuickTool.NOTES, "Quick Notes", "Floating scratchpad with instant export", R.drawable.ic_note, "Tools", Icons.Rounded.EditNote),
        ToolDefinition(QuickTool.QRSCANNER, "QR & Barcode Scanner", "Instant camera & image barcode reader", R.drawable.ic_qr_code_2, "Media", Icons.Rounded.QrCodeScanner),
        ToolDefinition(QuickTool.CAPTURE, "Quick Capture", "Screen recorder & floating annotator", R.drawable.ic_quickdash_tile, "Media", Icons.Rounded.Screenshot),
        ToolDefinition(QuickTool.VOICEMEMOS, "Voice Memos", "High-fidelity floating audio recorder", R.drawable.ic_phone, "Media", Icons.Rounded.Mic),
        ToolDefinition(QuickTool.CALCULATOR, "Quick Calculator", "One-handed calculation companion", R.drawable.ic_calculator, "Tools", Icons.Rounded.Calculate),
        ToolDefinition(QuickTool.CONVERTER, "Unit Converter", "Length, weight, currency & temperature", R.drawable.ic_tools, "Tools", Icons.Rounded.SyncAlt),
        ToolDefinition(QuickTool.TIMER, "Quick Timer", "Precise countdown & stopwatch alarms", R.drawable.ic_timer, "Tools", Icons.Rounded.Timer),
        ToolDefinition(QuickTool.POMODORO, "Quick Pomodoro", "Focus interval timer with break cycles", R.drawable.ic_timer, "Tools", Icons.Rounded.HourglassBottom),
        ToolDefinition(QuickTool.PASSWORD, "Password Generator", "Cryptographically secure passphrase vault", R.drawable.ic_settings, "Tools", Icons.Rounded.Key),
        ToolDefinition(QuickTool.TRANSLATOR, "Quick Translate", "Real-time multilingual translation", R.drawable.ic_globe, "Tools", Icons.Rounded.Translate),
        ToolDefinition(QuickTool.WIFI, "Wi-Fi Hub", "Speed diagnostics & QR connection sharing", R.drawable.ic_shortcut_wifi, "Tools", Icons.Rounded.Wifi),
        ToolDefinition(QuickTool.REMINDERS, "Quick Reminders", "Lightweight scheduled notifications", R.drawable.ic_timer, "Tools", Icons.Rounded.Alarm),
        ToolDefinition(QuickTool.CONTACT_QR, "Contact QR Card", "Generate sharable vCard QR for instant save", R.drawable.ic_person, "Tools", Icons.Rounded.ContactPage)
    )
}
