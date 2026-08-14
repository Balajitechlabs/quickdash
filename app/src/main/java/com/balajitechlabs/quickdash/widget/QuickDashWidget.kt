package com.balajitechlabs.quickdash.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
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

class QuickDashWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(60.dp, 60.dp),   // 1x1 Compact
            DpSize(160.dp, 60.dp),  // 2x1 / 4x1 Bar
            DpSize(160.dp, 160.dp)  // 2x2 / 4x2 Hub
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val size = LocalSize.current
            when {
                size.width < 130.dp -> CompactWidget(context)
                size.height < 110.dp -> QuickActionsBarWidget(context)
                else -> QuickHubWidget(context)
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun CompactWidget(context: Context) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(4.dp)
                .clickable(actionStartActivity<MainActivity>()),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = GlanceModifier
                    .size(56.dp)
                    .background(ColorProvider(Color(0xFF2563EB))) // Material 3 Primary
                    .cornerRadius(22.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_quickdash_tile),
                    contentDescription = "QuickDash Logo",
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
                .background(ColorProvider(Color(0xFF1E293B))) // Material 3 Dark SurfaceContainer
                .cornerRadius(24.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WidgetActionButton(
                iconRes = R.drawable.ic_shortcut_upi,
                contentDescription = "UPI QR",
                action = createSectionAction(context, "com.balajitechlabs.quickdash.ACTION_QUICK_UPI")
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            WidgetActionButton(
                iconRes = R.drawable.ic_note,
                contentDescription = "Notes",
                action = createSectionAction(context, "com.balajitechlabs.quickdash.ACTION_QUICK_NOTES")
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            WidgetActionButton(
                iconRes = R.drawable.ic_calculator,
                contentDescription = "Calculator",
                action = createSectionAction(context, "com.balajitechlabs.quickdash.ACTION_QUICK_CALCULATOR")
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            WidgetActionButton(
                iconRes = R.drawable.ic_timer,
                contentDescription = "Timer",
                action = createSectionAction(context, "com.balajitechlabs.quickdash.ACTION_QUICK_TIMER")
            )
        }
    }

    @androidx.compose.runtime.Composable
    private fun QuickHubWidget(context: Context) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFF0F172A))) // Material 3 SurfaceContainerLowest
                .cornerRadius(28.dp)
                .padding(12.dp)
        ) {
            // Header Row
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(actionStartActivity<MainActivity>()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_quickdash_tile),
                    contentDescription = "App Logo",
                    modifier = GlanceModifier.size(22.dp)
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = "QuickDash ⚡",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                    bgColor = Color(0xFF1E3A8A), // Navy Tint
                    action = createSectionAction(context, "com.balajitechlabs.quickdash.ACTION_QUICK_UPI"),
                    modifier = GlanceModifier.defaultWeight()
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                WidgetGridCard(
                    title = "Notes",
                    iconRes = R.drawable.ic_note,
                    bgColor = Color(0xFF1E293B),
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
                    bgColor = Color(0xFF1E293B),
                    action = createSectionAction(context, "com.balajitechlabs.quickdash.ACTION_QUICK_CALCULATOR"),
                    modifier = GlanceModifier.defaultWeight()
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                WidgetGridCard(
                    title = "Timer",
                    iconRes = R.drawable.ic_timer,
                    bgColor = Color(0xFF1E293B),
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
        action: Action
    ) {
        Box(
            modifier = GlanceModifier
                .size(44.dp)
                .background(ColorProvider(Color(0xFF334155)))
                .cornerRadius(18.dp)
                .clickable(action),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(iconRes),
                contentDescription = contentDescription,
                modifier = GlanceModifier.size(24.dp)
            )
        }
    }

    @androidx.compose.runtime.Composable
    private fun WidgetGridCard(
        title: String,
        iconRes: Int,
        bgColor: Color,
        action: Action,
        modifier: GlanceModifier = GlanceModifier
    ) {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .background(ColorProvider(bgColor))
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
                        color = ColorProvider(Color.White),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
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
