package com.balajitechlabs.quickdash.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun QuickDashNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = QuickDashRoute.Dashboard) {
        composable<QuickDashRoute.Dashboard> {
            /* DashboardScreen() */
        }
        composable<QuickDashRoute.Settings> {
            /* SettingsScreen() */
        }
        composable<QuickDashRoute.Customizer> {
            /* CustomizerScreen() */
        }
        composable<QuickDashRoute.Clipboard> {
            /* ClipboardScreen() */
        }
        composable<QuickDashRoute.Chat> {
            /* QuickChatScreen() */
        }
        composable<QuickDashRoute.Notes> {
            /* QuickNotesScreen() */
        }
        composable<QuickDashRoute.Search> {
            /* QuickSearchScreen() */
        }
        composable<QuickDashRoute.Timer> {
            /* QuickTimerScreen() */
        }
        composable<QuickDashRoute.Wifi> {
            /* QuickWifiScreen() */
        }
        composable<QuickDashRoute.VoiceMemos> {
            /* QuickVoiceMemosScreen() */
        }
        composable<QuickDashRoute.Social> {
            /* QuickSocialScreen() */
        }
        composable<QuickDashRoute.Onboarding> {
            /* OnboardingScreen() */
        }
    }
}
