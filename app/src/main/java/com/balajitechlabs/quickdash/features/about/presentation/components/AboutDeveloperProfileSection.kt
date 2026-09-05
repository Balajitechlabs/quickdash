/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/about/presentation/components
 * File: AboutDeveloperProfileSection.kt
 * Description: Developer showcase with colorful social pill grid matching the balajitechlab.com portfolio.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.about.presentation.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Shop
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.R

data class SocialPill(
    val label: String,
    val handle: String,
    val url: String,
    val badgeColor: Color,
    val iconTint: Color,
    val imageVector: ImageVector? = null,
    val iconRes: Int? = null,
    val fullWidth: Boolean = false
)

val socialPills = listOf(
    SocialPill(
        label = "GitHub",
        handle = "github.com/balajitechlabs",
        url = "https://github.com/balajitechlabs",
        badgeColor = Color(0xFF24292E),
        iconTint = Color.White,
        iconRes = R.drawable.ic_github
    ),
    SocialPill(
        label = "Mail",
        handle = "admin@balajitechlab.com",
        url = "mailto:admin@balajitechlab.com",
        badgeColor = Color(0xFFEA4335),
        iconTint = Color.White,
        imageVector = Icons.Rounded.Email
    ),
    SocialPill(
        label = "My Apps",
        handle = "Play Store",
        url = "https://play.google.com/store/apps/dev?id=9073716923131512981",
        badgeColor = Color(0xFF34A853),
        iconTint = Color.White,
        imageVector = Icons.Rounded.Shop
    ),
    SocialPill(
        label = "X (Twitter)",
        handle = "@balajitechlabs",
        url = "https://twitter.com/balajitechlabs",
        badgeColor = Color(0xFF1A1A1A),
        iconTint = Color.White,
        imageVector = Icons.AutoMirrored.Rounded.Send
    ),
    SocialPill(
        label = "LinkedIn",
        handle = "linkedin.com/in/balajitechlabs",
        url = "https://linkedin.com/in/balajitechlabs",
        badgeColor = Color(0xFF0077B5),
        iconTint = Color.White,
        imageVector = Icons.Rounded.Work
    ),
    SocialPill(
        label = "GitLab",
        handle = "gitlab.com/balajitechlabs",
        url = "https://gitlab.com/balajitechlabs",
        badgeColor = Color(0xFFFC6D26),
        iconTint = Color.White,
        imageVector = Icons.Rounded.Code
    ),
    SocialPill(
        label = "Telegram",
        handle = "t.me/+FYlt5cBA29Q0ZWJl",
        url = "https://t.me/+FYlt5cBA29Q0ZWJl",
        badgeColor = Color(0xFF26A5E4),
        iconTint = Color.White,
        iconRes = R.drawable.ic_telegram
    ),
    SocialPill(
        label = "Instagram",
        handle = "@balajitechlabs",
        url = "https://instagram.com/balajitechlabs",
        badgeColor = Color(0xFFE1306C),
        iconTint = Color.White,
        iconRes = R.drawable.ic_instagram
    ),
    SocialPill(
        label = "r/balajitechlabs",
        handle = "Reddit Community",
        url = "https://www.reddit.com/r/balajitechlabs/",
        badgeColor = Color(0xFFFF4500),
        iconTint = Color.White,
        imageVector = Icons.Rounded.Forum,
        fullWidth = true
    ),
    SocialPill(
        label = "Portfolio",
        handle = "balajitechlab.com",
        url = "https://balajitechlab.com",
        badgeColor = Color(0xFF6C63FF),
        iconTint = Color.White,
        imageVector = Icons.Rounded.Language,
        fullWidth = true
    )
)

@Composable
fun DeveloperProfileHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_developer_avatar),
            contentDescription = "balajitechlabs avatar",
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "balajitechlabs",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "||BTL||™",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SocialPillsGrid(
    context: Context,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        maxItemsInEachRow = 2
    ) {
        socialPills.forEach { pill ->
            SocialPillChip(
                pill = pill,
                context = context,
                modifier = if (pill.fullWidth) Modifier.fillMaxWidth()
                           else Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SocialPillChip(
    pill: SocialPill,
    context: Context,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = pill.badgeColor,
        modifier = modifier.clickable {
            com.balajitechlabs.quickdash.core.ui.playClickVibration(context, true)
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pill.url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (pill.iconRes != null) {
                Icon(
                    painter = painterResource(pill.iconRes),
                    contentDescription = null,
                    tint = pill.iconTint,
                    modifier = Modifier.size(18.dp)
                )
            } else if (pill.imageVector != null) {
                Icon(
                    imageVector = pill.imageVector,
                    contentDescription = null,
                    tint = pill.iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = pill.label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

// Thin wrapper for any callers still using the list-style card
@Composable
fun SocialProfileCard(
    pill: SocialPill,
    context: Context,
    modifier: Modifier = Modifier
) {
    SocialPillChip(pill = pill, context = context, modifier = modifier.fillMaxWidth())
}
