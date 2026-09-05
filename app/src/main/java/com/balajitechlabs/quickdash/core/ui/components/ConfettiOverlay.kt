/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/ui/components
 * File: ConfettiOverlay.kt
 * Description: Full-screen celebratory confetti particle animation overlay for successful completions.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.PartySystem
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size
import java.util.concurrent.TimeUnit

@Composable
fun ConfettiOverlay(
    confettiEnabled: Boolean,
    triggerEmojiConfetti: Boolean,
    onEmojiConfettiEnded: () -> Unit,
    emojiHeaderVal: String,
    settingsConfettiType: String?,
    settingsConfettiKey: Int,
    onSettingsConfettiEnded: () -> Unit
) {
    if (!confettiEnabled) return

    val context = LocalContext.current

    if (triggerEmojiConfetti) {
        val emojiDrawable = remember(emojiHeaderVal) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 48f
                textAlign = Paint.Align.CENTER
            }
            val bounds = Rect()
            paint.getTextBounds(emojiHeaderVal, 0, emojiHeaderVal.length, bounds)
            val width = (bounds.width() + 10).coerceAtLeast(64)
            val height = (bounds.height() + 10).coerceAtLeast(64)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val x = width / 2f
            val y = height / 2f - (paint.descent() + paint.ascent()) / 2f
            canvas.drawText(emojiHeaderVal, x, y, paint)
            BitmapDrawable(context.resources, bitmap)
        }

        val party = Party(
            speed = 10f,
            maxSpeed = 30f,
            damping = 0.9f,
            angle = 0,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x00bcd4, 0x4caf50),
            shapes = listOf(Shape.Circle, Shape.Square),
            emitter = Emitter(duration = 500, TimeUnit.MILLISECONDS).max(50),
            position = Position.Relative(0.5, 0.3)
        )

        KonfettiView(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(200f),
            parties = listOf(party),
            updateListener = object : OnParticleSystemUpdateListener {
                override fun onParticleSystemEnded(system: PartySystem, activeSystems: Int) {
                    if (activeSystems == 0) {
                        onEmojiConfettiEnded()
                    }
                }
            }
        )
    }

    if (settingsConfettiType != null) {
        key(settingsConfettiKey) {
            val partyList = when (settingsConfettiType) {
                "Right" -> listOf(
                    Party(
                        speed = 25f,
                        maxSpeed = 45f,
                        damping = 0.9f,
                        angle = 180,
                        spread = 60,
                        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x00bcd4, 0x4caf50),
                        size = listOf(Size(32, 6f), Size(42, 8f)),
                        emitter = Emitter(duration = 300, TimeUnit.MILLISECONDS).max(100),
                        position = Position.Relative(1.0, 0.5)
                    )
                )
                "Corner" -> listOf(
                    Party(
                        speed = 25f,
                        maxSpeed = 40f,
                        damping = 0.9f,
                        angle = -45,
                        spread = 40,
                        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x00bcd4, 0x4caf50),
                        size = listOf(Size(32, 6f), Size(42, 8f)),
                        emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(80),
                        position = Position.Relative(0.0, 0.8)
                    ),
                    Party(
                        speed = 25f,
                        maxSpeed = 40f,
                        damping = 0.9f,
                        angle = -135,
                        spread = 40,
                        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x00bcd4, 0x4caf50),
                        size = listOf(Size(32, 6f), Size(42, 8f)),
                        emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(80),
                        position = Position.Relative(1.0, 0.8)
                    )
                )
                "Export" -> listOf(
                    Party(
                        speed = 5f,
                        maxSpeed = 25f,
                        damping = 0.9f,
                        angle = 90,
                        spread = 80,
                        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x00bcd4, 0x4caf50),
                        size = listOf(Size(32, 6f), Size(42, 8f)),
                        emitter = Emitter(duration = 1000, TimeUnit.MILLISECONDS).max(100),
                        position = Position.Relative(0.0, 0.0).between(Position.Relative(1.0, 0.0))
                    )
                )
                else -> listOf(
                    Party(
                        speed = 0f,
                        maxSpeed = 30f,
                        damping = 0.9f,
                        angle = 0,
                        spread = 360,
                        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x00bcd4, 0x4caf50),
                        size = listOf(Size(32, 6f), Size(42, 8f)),
                        emitter = Emitter(duration = 200, TimeUnit.MILLISECONDS).max(100),
                        position = Position.Relative(0.5, 0.5)
                    )
                )
            }

            KonfettiView(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(300f),
                parties = partyList,
                updateListener = object : OnParticleSystemUpdateListener {
                    override fun onParticleSystemEnded(system: PartySystem, activeSystems: Int) {
                        if (activeSystems == 0) {
                            onSettingsConfettiEnded()
                        }
                    }
                }
            )
        }
    }
}
