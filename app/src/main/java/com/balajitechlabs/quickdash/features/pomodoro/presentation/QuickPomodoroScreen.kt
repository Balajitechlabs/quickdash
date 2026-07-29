package com.balajitechlabs.quickdash.features.pomodoro.presentation

import android.os.CountDownTimer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.balajitechlabs.quickdash.core.ui.components.RoundedCardContainer

/**
 * Tool #14: Quick Pomodoro Focus Timer.
 * 25-min Focus / 5-min Short Break study timer over any app.
 */
@Composable
fun QuickPomodoroScreen(isFloating: Boolean = false) {
    var totalSeconds by remember { mutableStateOf(25 * 60) }
    var secondsRemaining by remember { mutableStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var timerMode by remember { mutableStateOf("FOCUS") } // FOCUS, BREAK

    var timer: CountDownTimer? by remember { mutableStateOf(null) }

    fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(secondsRemaining * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                isRunning = false
                secondsRemaining = 0
            }
        }.start()
        isRunning = true
    }

    fun pauseTimer() {
        timer?.cancel()
        isRunning = false
    }

    fun resetTimer(minutes: Int, modeName: String) {
        timer?.cancel()
        isRunning = false
        timerMode = modeName
        totalSeconds = minutes * 60
        secondsRemaining = minutes * 60
    }

    DisposableEffect(Unit) {
        onDispose {
            timer?.cancel()
        }
    }

    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .then(if (isFloating) Modifier.fillMaxWidth().wrapContentHeight() else Modifier.fillMaxSize())
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Quick Pomodoro Focus",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timer Dial Display Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (timerMode == "FOCUS") "🧠 FOCUS SESSION" else "☕ SHORT BREAK",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timer Preset Controls Pod
        RoundedCardContainer(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = { resetTimer(25, "FOCUS") },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("25m Focus")
                }
                OutlinedButton(
                    onClick = { resetTimer(5, "BREAK") },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("5m Break")
                }
                OutlinedButton(
                    onClick = { resetTimer(15, "BREAK") },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("15m Long")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { if (isRunning) pauseTimer() else startTimer() },
                modifier = Modifier
                    .height(52.dp)
                    .width(160.dp),
                shape = RoundedCornerShape(26.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isRunning) "PAUSE" else "START")
            }

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = { resetTimer(25, "FOCUS") },
                modifier = Modifier.size(52.dp)
            ) {
                Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Reset")
            }
        }
    }
}
