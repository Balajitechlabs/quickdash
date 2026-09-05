/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/ui/components
 * File: PaymentModeSwitcher.kt
 * Description: Segmented toggle switch between UPI payment collection, payment links, and PayPal QR modes.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.balajitechlabs.quickdash.R
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.core.ui.playClickVibration

@Composable
fun PaymentModeSwitcherButton(
    usePaypal: Boolean,
    onTogglePaypal: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val userStore = remember { UserStore(context) }
    val hapticEnabled by userStore.hapticEnabled.collectAsStateWithLifecycle(initialValue = true)

    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .size(width = 44.dp, height = 32.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                playClickVibration(context, hapticEnabled)
                onTogglePaypal(!usePaypal)
            }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            AnimatedContent(
                targetState = usePaypal,
                transitionSpec = {
                    fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) togetherWith
                            fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                },
                label = "paymentModeTransition"
            ) { activePaypal ->
                Icon(
                    painter = painterResource(if (activePaypal) R.drawable.ic_paypal else R.drawable.ic_upi_pay),
                    contentDescription = "Switch Payment Mode",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
