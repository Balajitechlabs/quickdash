/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/ui
 * File: RadialToolCatalog.kt
 * Description: EssentialX-styled component for core/ui supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.ui.components

import com.balajitechlabs.quickdash.R

data class RadialToolInfo(
    val id: String,
    val title: String,
    val iconRes: Int,
    val actionIntent: String
)

object RadialToolCatalog {
    // Icons must match what toolDefinitions() uses on the root page
    val ALL_TOOLS = listOf(
        RadialToolInfo("upi",       "Quick Collect",   R.drawable.ic_upi_pay,        "com.balajitechlabs.quickdash.ACTION_QUICK_UPI"),
        RadialToolInfo("chat",      "Quick Chat",      R.drawable.ic_whatsapp,       "com.balajitechlabs.quickdash.ACTION_QUICK_CHAT"),
        RadialToolInfo("clipboard", "Clipboard",       R.drawable.ic_note,           "com.balajitechlabs.quickdash.ACTION_QUICK_CLIPBOARD"),
        RadialToolInfo("notes",     "Quick Notes",     R.drawable.ic_note,           "com.balajitechlabs.quickdash.ACTION_QUICK_NOTES"),
        RadialToolInfo("capture",   "Capture",         R.drawable.ic_quickdash_tile, "com.balajitechlabs.quickdash.ACTION_QUICK_CAPTURE"),
        RadialToolInfo("wifi",      "Wi-Fi Hub",       R.drawable.ic_shortcut_wifi,  "com.balajitechlabs.quickdash.ACTION_QUICK_WIFI"),
        RadialToolInfo("timer",     "Timer",           R.drawable.ic_timer,          "com.balajitechlabs.quickdash.ACTION_QUICK_TIMER"),
        RadialToolInfo("password",  "Password",        R.drawable.ic_settings,       "com.balajitechlabs.quickdash.ACTION_QUICK_PASSWORD"),
        RadialToolInfo("qr",        "QR Scanner",      R.drawable.ic_qr_code_2,      "com.balajitechlabs.quickdash.ACTION_QUICK_QRSCANNER"),
        RadialToolInfo("calc",      "Calculator",      R.drawable.ic_calculator,     "com.balajitechlabs.quickdash.ACTION_QUICK_CALCULATOR"),
        RadialToolInfo("web",       "Search",          R.drawable.ic_search,         "com.balajitechlabs.quickdash.ACTION_QUICK_WEB"),
        RadialToolInfo("voice",     "Voice Memos",     R.drawable.ic_phone,          "com.balajitechlabs.quickdash.ACTION_QUICK_VOICEMEMOS"),
        RadialToolInfo("converter", "Converter",       R.drawable.ic_tools,          "com.balajitechlabs.quickdash.ACTION_QUICK_CONVERTER"),
        RadialToolInfo("translate", "Translate",       R.drawable.ic_globe,          "com.balajitechlabs.quickdash.ACTION_QUICK_TRANSLATOR"),
        RadialToolInfo("pomodoro",  "Pomodoro",        R.drawable.ic_timer,          "com.balajitechlabs.quickdash.ACTION_QUICK_POMODORO"),
        RadialToolInfo("reminders", "Reminders",       R.drawable.ic_timer,          "com.balajitechlabs.quickdash.ACTION_QUICK_REMINDERS"),
        RadialToolInfo("contactqr", "Contact QR",      R.drawable.ic_person,         "com.balajitechlabs.quickdash.ACTION_QUICK_CONTACTQR"),
    )

    fun getToolById(id: String): RadialToolInfo {
        val key = id.trim().lowercase()
        return ALL_TOOLS.find { it.id == key } ?: ALL_TOOLS[0]
    }

    fun buildRadialActions(toolIds: List<String>): List<RadialAction> {
        if (toolIds.isEmpty()) return emptyList()
        val validIds = toolIds.take(6)
        val count = validIds.size
        val step = 360.0 / count
        val startAngle = 270.0 // North

        return validIds.mapIndexed { index, id ->
            val tool = getToolById(id)
            RadialAction(
                id = tool.id,
                title = tool.title,
                iconRes = tool.iconRes,
                actionIntent = tool.actionIntent,
                angleDegrees = (startAngle + index * step) % 360.0
            )
        }
    }
}
