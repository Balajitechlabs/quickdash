/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/about/presentation/components
 * File: AboutFloatingWebsiteBubble.kt
 * Description: Floating bubble launcher linking to balajitechlab.com developer website.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.about.presentation.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BoxScope.AboutFloatingWebsiteBubble(
    context: Context,
    modifier: Modifier = Modifier
) {
    val fabScope = rememberCoroutineScope()
    var fabExpanded by remember { mutableStateOf(false) }

    Surface(
        onClick = {
            if (fabExpanded) {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://quickdash.balajitechlab.com"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                fabExpanded = false
            } else {
                fabExpanded = true
                fabScope.launch {
                    delay(2200)
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://quickdash.balajitechlab.com"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                    fabExpanded = false
                }
            }
        },
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 8.dp,
        modifier = modifier
            .align(Alignment.BottomEnd)
            .padding(bottom = 100.dp, end = 20.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (fabExpanded) 16.dp else 14.dp,
                vertical = 14.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Crossfade(targetState = fabExpanded, label = "fab_icon") { expanded ->
                Icon(
                    imageVector = if (expanded) Icons.Rounded.Language else Icons.Rounded.Public,
                    contentDescription = "QuickDash Website",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = fabExpanded,
                enter = expandHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "quickdash.balajitechlab.com",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
