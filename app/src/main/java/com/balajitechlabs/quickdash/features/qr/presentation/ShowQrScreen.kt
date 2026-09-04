/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/qr
 * File: ShowQrScreen.kt
 * Description: EssentialX-styled component for features/qr supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.qr.presentation

import android.graphics.Bitmap
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.balajitechlabs.quickdash.R
import com.balajitechlabs.quickdash.core.utils.ShareUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import android.content.Context
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build

@Composable
fun ShowQrScreen(
    amount: String,
    qrBitmap: Bitmap,
    upiId: String,
    payeeName: String,
    showUpiId: Boolean,
    payUrl: String,
    usePaypal: Boolean = false,
    showShareButton: Boolean = true,
    confettiType: String = "Default",
    hapticLevel: String = "Crisp",
    isFloating: Boolean = false,
    onQrShown: () -> Unit,
    onRestoreBrightness: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    @Suppress("DEPRECATION")
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }

    @Suppress("DEPRECATION")
    fun triggerFeedback() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
        audioManager?.playSoundEffect(android.media.AudioManager.FX_KEY_CLICK, 0.3f)
        if (hapticLevel == "Off") return
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                when (hapticLevel) {
                    "Light" -> vibrator.vibrate(VibrationEffect.createOneShot(10, 100))
                    "Medium" -> vibrator.vibrate(VibrationEffect.createOneShot(20, 180))
                    else -> vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                }
            } else {
                vibrator.vibrate(if (hapticLevel == "Light") 10L else if (hapticLevel == "Medium") 20L else 30L)
            }
        }
    }

    // Brightness management
    DisposableEffect(Unit) {
        onQrShown()
        onDispose { onRestoreBrightness() }
    }

    var showConfetti by remember { mutableStateOf(false) }
    var confettiTriggerKey by remember { mutableStateOf(0) }

    // Scale-in animation for the QR
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        triggerFeedback()
        visible = true
        showConfetti = true
        confettiTriggerKey++
    }
    var zoomScale by remember { mutableStateOf(1f) }
    @Suppress("DEPRECATION")
    val transformState = rememberTransformableState { zoomChange, _, _ ->
        zoomScale = (zoomScale * zoomChange).coerceIn(0.5f, 3.5f)
    }
    val qrScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "qrScale"
    )

    val currencyPrefix = "₹"
    val idTypeLabel = "UPI ID"

    Box(modifier = Modifier.then(if (isFloating) Modifier.fillMaxWidth().wrapContentHeight() else Modifier.fillMaxSize())) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        // Header text
        Text(
            "Show this code to receive payment",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFC5C6D0),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // QR Code in a container
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = qrScale * zoomScale
                    scaleY = qrScale * zoomScale
                }
                .transformable(state = transformState),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp)),
                color = Color(0xFF38393F),
                border = BorderStroke(1.dp, Color(0xFF44474F).copy(alpha = 0.6f))
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(8.dp)
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Payment QR Code",
                        modifier = Modifier
                            .size(if (isFloating) 180.dp else 220.dp),
                        filterQuality = FilterQuality.None
                    )
                }
            }
        }

        // Amount or Scan to Pay
        if (amount.isNotBlank()) {
            Text(
                text = "$currencyPrefix$amount",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "Scan to Pay",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ID display
        if (showUpiId) {
            Text(
                text = "$idTypeLabel: $upiId",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFC5C6D0),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Share button
        if (showShareButton) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        triggerFeedback()
                        ShareUtils.shareQrCode(context, qrBitmap, payeeName, upiId, amount, showUpiId, usePaypal)
                        showConfetti = true
                        confettiTriggerKey++
                    },
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF38393F),
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color(0xFF44474F)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_share),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        "Share QR Code Image",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                androidx.compose.material3.OutlinedButton(
                    onClick = {
                        triggerFeedback()
                        ShareUtils.sharePaymentLink(context, payUrl, amount, payeeName)
                        showConfetti = true
                        confettiTriggerKey++
                    },
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color(0xFF44474F)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_share),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        "Share Payment Link",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }

        // Bottom clearance for floating Back pill
        Spacer(modifier = Modifier.height(120.dp))
    }

    if (showConfetti) {
        key(confettiTriggerKey) {
            val partyList = when (confettiType) {
                "Right" -> listOf(
                    nl.dionsegijn.konfetti.core.Party(
                        speed = 25f,
                        maxSpeed = 45f,
                        damping = 0.9f,
                        angle = 180,
                        spread = 60,
                        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x00bcd4, 0x4caf50),
                        size = listOf(nl.dionsegijn.konfetti.core.models.Size(56, 12f), nl.dionsegijn.konfetti.core.models.Size(72, 16f)),
                        emitter = nl.dionsegijn.konfetti.core.emitter.Emitter(duration = 300, java.util.concurrent.TimeUnit.MILLISECONDS).max(100),
                        position = nl.dionsegijn.konfetti.core.Position.Relative(1.0, 0.5)
                    )
                )
                "Corner" -> listOf(
                    nl.dionsegijn.konfetti.core.Party(
                        speed = 25f,
                        maxSpeed = 40f,
                        damping = 0.9f,
                        angle = -45,
                        spread = 40,
                        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x00bcd4, 0x4caf50),
                        size = listOf(nl.dionsegijn.konfetti.core.models.Size(56, 12f), nl.dionsegijn.konfetti.core.models.Size(72, 16f)),
                        emitter = nl.dionsegijn.konfetti.core.emitter.Emitter(duration = 100, java.util.concurrent.TimeUnit.MILLISECONDS).max(80),
                        position = nl.dionsegijn.konfetti.core.Position.Relative(0.0, 0.8)
                    ),
                    nl.dionsegijn.konfetti.core.Party(
                        speed = 25f,
                        maxSpeed = 40f,
                        damping = 0.9f,
                        angle = -135,
                        spread = 40,
                        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x00bcd4, 0x4caf50),
                        size = listOf(nl.dionsegijn.konfetti.core.models.Size(56, 12f), nl.dionsegijn.konfetti.core.models.Size(72, 16f)),
                        emitter = nl.dionsegijn.konfetti.core.emitter.Emitter(duration = 100, java.util.concurrent.TimeUnit.MILLISECONDS).max(80),
                        position = nl.dionsegijn.konfetti.core.Position.Relative(1.0, 0.8)
                    )
                )
                "Export" -> listOf(
                    nl.dionsegijn.konfetti.core.Party(
                        speed = 5f,
                        maxSpeed = 25f,
                        damping = 0.9f,
                        angle = 90,
                        spread = 80,
                        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x00bcd4, 0x4caf50),
                        size = listOf(nl.dionsegijn.konfetti.core.models.Size(56, 12f), nl.dionsegijn.konfetti.core.models.Size(72, 16f)),
                        emitter = nl.dionsegijn.konfetti.core.emitter.Emitter(duration = 1000, java.util.concurrent.TimeUnit.MILLISECONDS).max(100),
                        position = nl.dionsegijn.konfetti.core.Position.Relative(0.0, 0.0).between(nl.dionsegijn.konfetti.core.Position.Relative(1.0, 0.0))
                    )
                )
                else -> listOf(
                    nl.dionsegijn.konfetti.core.Party(
                        speed = 0f,
                        maxSpeed = 30f,
                        damping = 0.9f,
                        angle = 0,
                        spread = 360,
                        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x00bcd4, 0x4caf50),
                        size = listOf(nl.dionsegijn.konfetti.core.models.Size(56, 12f), nl.dionsegijn.konfetti.core.models.Size(72, 16f)),
                        emitter = nl.dionsegijn.konfetti.core.emitter.Emitter(duration = 200, java.util.concurrent.TimeUnit.MILLISECONDS).max(100),
                        position = nl.dionsegijn.konfetti.core.Position.Relative(0.5, 0.5)
                    )
                )
            }

            nl.dionsegijn.konfetti.compose.KonfettiView(
                modifier = Modifier.matchParentSize().zIndex(100f),
                parties = partyList,
                updateListener = object : nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener {
                    override fun onParticleSystemEnded(system: nl.dionsegijn.konfetti.core.PartySystem, activeSystems: Int) {
                        if (activeSystems == 0) {
                            showConfetti = false
                        }
                    }
                }
            )
        }
    }
}
}
