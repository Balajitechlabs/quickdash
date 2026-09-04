/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/navigation
 * File: QuickDashRoutes.kt
 * Description: EssentialX-styled component for core/navigation supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.navigation

import kotlinx.serialization.Serializable

sealed interface QuickDashRoute {
    @Serializable data object Dashboard : QuickDashRoute
    @Serializable data object Settings : QuickDashRoute
    @Serializable data object Customizer : QuickDashRoute
    @Serializable data object Clipboard : QuickDashRoute
    @Serializable data object Chat : QuickDashRoute
    @Serializable data object Notes : QuickDashRoute
    @Serializable data object Search : QuickDashRoute
    @Serializable data object Timer : QuickDashRoute
    @Serializable data object Wifi : QuickDashRoute
    @Serializable data object VoiceMemos : QuickDashRoute
    @Serializable data object Social : QuickDashRoute
    @Serializable data object Onboarding : QuickDashRoute
}
