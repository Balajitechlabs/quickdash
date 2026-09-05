/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/ui
 * File: QuickDashUiState.kt
 * Description: Sealed interface defining active navigation states, dialog visibility, and current tool screens.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.ui

import android.graphics.Bitmap

sealed interface QuickDashUiState {
    data object Onboarding : QuickDashUiState
    data object Dashboard : QuickDashUiState
    data class Setup(val isManaging: Boolean) : QuickDashUiState
    data class EnterAmount(val upiIds: List<String>, val defaultUpiId: String) : QuickDashUiState
    data class ShowQr(
        val amount: String,
        val qrBitmap: Bitmap,
        val upiId: String,
        val payeeName: String,
        val payUrl: String
    ) : QuickDashUiState

    data object WhatsApp : QuickDashUiState
    data object Instagram : QuickDashUiState
    data object Settings : QuickDashUiState
    data object SystemLogs : QuickDashUiState
    data object Notes : QuickDashUiState
    data object Search : QuickDashUiState
    data object Web : QuickDashUiState
    data object Wifi : QuickDashUiState
    data object Hotspot : QuickDashUiState
    data object ApiPanel : QuickDashUiState
    data object Clipboard : QuickDashUiState
    data object Calculator : QuickDashUiState
    data object Timer : QuickDashUiState
    data object Converter : QuickDashUiState
    data object Translator : QuickDashUiState
    data object Capture : QuickDashUiState
    data object FirebaseSetup : QuickDashUiState
    data object BlogPosts : QuickDashUiState
    data object Pomodoro : QuickDashUiState
    data object Password : QuickDashUiState
    data object VoiceMemos : QuickDashUiState
    data object Reminders : QuickDashUiState
    data object QrScanner : QuickDashUiState
    data object About : QuickDashUiState
    data object ContactQr : QuickDashUiState
    data object BubbleCustomizer : QuickDashUiState
}
