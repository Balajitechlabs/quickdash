/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: widget
 * File: QuickDashWidget.kt
 * Description: Glance AppWidget providing home-screen shortcuts to favorite QuickDash tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.balajitechlabs.quickdash.MainActivity
import com.balajitechlabs.quickdash.R
import com.balajitechlabs.quickdash.features.dashboard.presentation.FloatingDialogActivity

/**
 * QuickDash Glance Home Screen Widgets (`QuickDashWidget.kt`).
 * Supports:
 * - 1-Tap Floating Bubble On/Off Toggle from home screen
 * - Material You dynamic theming & dark/light mode
 * - 1x1 Compact, 2x1 / 4x1 Quick Actions Bar, and 2x2 / 4x2 Tool Hub
 */
class QuickDashWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(60.dp, 60.dp),   // 1x1 Compact (1-Tap Bubble Toggle)
            DpSize(160.dp, 60.dp),  // 2x1 / 4x1 Bar
            DpSize(160.dp, 160.dp)  // 2x2 / 4x2 Hub
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                val size = LocalSize.current
                when {
                    size.width < 130.dp -> CompactWidget(context)
                    size.height < 110.dp -> QuickActionsBarWidget(context)
                    else -> QuickHubWidget(context)
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun CompactWidget(context: Context) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(4.dp)
                .clickable(createToggleBubbleAction(context)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = GlanceModifier
                    .size(56.dp)
                    .background(GlanceTheme.colors.primary)
                    .cornerRadius(22.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_quickdash_tile),
                    contentDescription = "Toggle QuickDash Floating Bubble",
                    modifier = GlanceModifier.size(32.dp)
                )
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun QuickActionsBarWidget(context: Context) {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .cornerRadius(24.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WidgetActionButton(
                iconRes = R.drawable.ic_quickdash_tile,
                contentDescription = "Toggle Bubble",
                action = createToggleBubbleAction(context),
                bgColor = GlanceTheme.colors.primary
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            WidgetActionButton(
                iconRes = R.drawable.ic_shortcut_upi,
                contentDescription = "UPI QR",
                action = createSectionAction(context, "com.balajitechlabs.quickdash.ACTION_QUICK_UPI"),
                bgColor = GlanceTheme.colors.primaryContainer
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            WidgetActionButton(
                iconRes = R.drawable.ic_note,
                contentDescription = "Notes",
                action = createSectionAction(context, "com.balajitechlabs.quickdash.ACTION_QUICK_NOTES"),
                bgColor = GlanceTheme.colors.surfaceVariant
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            WidgetActionButton(
                iconRes = R.drawable.ic_calculator,
                contentDescription = "Calculator",
                action = createSectionAction(context, "com.balajitechlabs.quickdash.ACTION_QUICK_CALCULATOR"),
                bgColor = GlanceTheme.colors.surfaceVariant
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            WidgetActionButton(
                iconRes = R.drawable.ic_timer,
                contentDescription = "Timer",
                action = createSectionAction(context, "com.balajitechlabs.quickdash.ACTION_QUICK_TIMER"),
                bgColor = GlanceTheme.colors.surfaceVariant
            )
        }
    }

    @androidx.compose.runtime.Composable
    private fun QuickHubWidget(context: Context) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .cornerRadius(28.dp)
                .padding(12.dp)
        ) {
            // Header Row: Logo toggles bubble, title opens main app
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(32.dp)
                        .background(GlanceTheme.colors.primary)
                        .cornerRadius(12.dp)
                        .clickable(createToggleBubbleAction(context))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_quickdash_tile),
                        contentDescription = "Toggle Bubble",
                        modifier = GlanceModifier.size(20.dp)
                    )
                }
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = "QuickDash",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.clickable(actionStartActivity(Intent(context, MainActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }))
                )
            }

            Spacer(modifier = GlanceModifier.height(10.dp))

            // 2x2 Grid of Quick Actions
            Row(
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WidgetGridCard(
                    title = "UPI Pay",
                    iconRes = R.drawable.ic_shortcut_upi,
                    bgColor = GlanceTheme.colors.primaryContainer,
                    textColor = GlanceTheme.colors.onPrimaryContainer,
                    action = createSectionAction(context, "com.balajitechlabs.quickdash.ACTION_QUICK_UPI"),
                    modifier = GlanceModifier.defaultWeight()
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                WidgetGridCard(
                    title = "Notes",
                    iconRes = R.drawable.ic_note,
                    bgColor = GlanceTheme.colors.surfaceVariant,
                    textColor = GlanceTheme.colors.onSurfaceVariant,
                    action = createSectionAction(context, "com.balajitechlabs.quickdash.ACTION_QUICK_NOTES"),
                    modifier = GlanceModifier.defaultWeight()
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WidgetGridCard(
                    title = "Calculator",
                    iconRes = R.drawable.ic_calculator,
                    bgColor = GlanceTheme.colors.surfaceVariant,
                    textColor = GlanceTheme.colors.onSurfaceVariant,
                    action = createSectionAction(context, "com.balajitechlabs.quickdash.ACTION_QUICK_CALCULATOR"),
                    modifier = GlanceModifier.defaultWeight()
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                WidgetGridCard(
                    title = "Timer",
                    iconRes = R.drawable.ic_timer,
                    bgColor = GlanceTheme.colors.surfaceVariant,
                    textColor = GlanceTheme.colors.onSurfaceVariant,
                    action = createSectionAction(context, "com.balajitechlabs.quickdash.ACTION_QUICK_TIMER"),
                    modifier = GlanceModifier.defaultWeight()
                )
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun WidgetActionButton(
        iconRes: Int,
        contentDescription: String,
        action: Action,
        bgColor: ColorProvider
    ) {
        Box(
            modifier = GlanceModifier
                .size(40.dp)
                .background(bgColor)
                .cornerRadius(16.dp)
                .clickable(action),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(iconRes),
                contentDescription = contentDescription,
                modifier = GlanceModifier.size(22.dp)
            )
        }
    }

    @androidx.compose.runtime.Composable
    private fun WidgetGridCard(
        title: String,
        iconRes: Int,
        bgColor: ColorProvider,
        textColor: ColorProvider,
        action: Action,
        modifier: GlanceModifier = GlanceModifier
    ) {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .background(bgColor)
                .cornerRadius(18.dp)
                .clickable(action)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(iconRes),
                    contentDescription = title,
                    modifier = GlanceModifier.size(22.dp)
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = title,
                    style = TextStyle(
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }

    private fun createToggleBubbleAction(context: Context): Action {
        val intent = Intent(context, ToggleBubbleReceiver::class.java).apply {
            action = "com.balajitechlabs.quickdash.ACTION_TOGGLE_BUBBLE_WIDGET"
            setPackage(context.packageName)
        }
        return actionSendBroadcast(intent)
    }

    private fun createSectionAction(context: Context, sectionAction: String): Action {
        val intent = Intent(context, FloatingDialogActivity::class.java).apply {
            putExtra("launch_section", sectionAction)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        return actionStartActivity(intent)
    }
}

class QuickDashWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = QuickDashWidget()
}
