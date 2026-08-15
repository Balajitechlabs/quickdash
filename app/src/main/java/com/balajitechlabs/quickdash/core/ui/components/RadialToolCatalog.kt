package com.balajitechlabs.quickdash.core.ui.components

import com.balajitechlabs.quickdash.R

data class RadialToolInfo(
    val id: String,
    val title: String,
    val iconRes: Int,
    val actionIntent: String,
    val description: String
)

object RadialToolCatalog {
    val ALL_TOOLS = listOf(
        RadialToolInfo("upi", "UPI Pay", R.drawable.ic_shortcut_upi, "com.balajitechlabs.quickdash.ACTION_QUICK_UPI", "Scan QR & pay instantly"),
        RadialToolInfo("notes", "Notes", R.drawable.ic_note, "com.balajitechlabs.quickdash.ACTION_QUICK_NOTES", "Scratchpad & voice memos"),
        RadialToolInfo("calc", "Calc", R.drawable.ic_calculator, "com.balajitechlabs.quickdash.ACTION_QUICK_CALCULATOR", "Floating math calculator"),
        RadialToolInfo("timer", "Timer", R.drawable.ic_timer, "com.balajitechlabs.quickdash.ACTION_QUICK_TIMER", "Countdown & stopwatch"),
        RadialToolInfo("web", "Browser", R.drawable.ic_globe, "com.balajitechlabs.quickdash.ACTION_QUICK_WEB", "Mini popup web browser"),
        RadialToolInfo("chat", "Direct Chat", R.drawable.ic_shortcut_chat, "com.balajitechlabs.quickdash.ACTION_QUICK_CHAT", "Quick message without saving"),
        RadialToolInfo("wifi", "Wi-Fi QR", R.drawable.ic_shortcut_wifi, "com.balajitechlabs.quickdash.ACTION_QUICK_WIFI", "Share & connect Wi-Fi"),
        RadialToolInfo("social", "Social Hub", R.drawable.ic_shortcut_insta, "com.balajitechlabs.quickdash.ACTION_QUICK_INSTA", "Social utility tools"),
        RadialToolInfo("ai", "AI Assistant", R.drawable.ic_quickdash_tile, "com.balajitechlabs.quickdash.ACTION_QUICK_AI", "On-device AI chat")
    )

    fun getToolById(id: String): RadialToolInfo {
        return ALL_TOOLS.find { it.id == id } ?: ALL_TOOLS[0]
    }

    fun buildRadialActions(toolIds: List<String>): List<RadialAction> {
        val angles = listOf(270.0, 0.0, 90.0, 180.0) // Top (North), Right (East), Bottom (South), Left (West)
        val validIds = if (toolIds.size >= 4) toolIds.take(4) else listOf("upi", "notes", "calc", "timer")
        return validIds.mapIndexed { index, id ->
            val tool = getToolById(id)
            RadialAction(
                id = tool.id,
                title = tool.title,
                iconRes = tool.iconRes,
                actionIntent = tool.actionIntent,
                angleDegrees = angles.getOrElse(index) { 0.0 }
            )
        }
    }
}
