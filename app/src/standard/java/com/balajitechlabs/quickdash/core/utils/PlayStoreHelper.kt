package com.balajitechlabs.quickdash.core.utils

import android.app.Activity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory

object PlayStoreHelper {
    fun checkForAppUpdate(activity: Activity) {
        try {
            val appUpdateManager = AppUpdateManagerFactory.create(activity)
            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                val isUpdateAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                val isFlexible = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                if (isUpdateAvailable && isFlexible) {
                    @Suppress("DEPRECATION")
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        activity,
                        AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE),
                        9901
                    )
                }
            }.addOnFailureListener { e ->
                AppLogger.e("PlayStoreHelper", "In-App Update check failed", e)
            }
        } catch (e: Exception) {
            AppLogger.e("PlayStoreHelper", "In-App Update check skipped", e)
        }
    }

    fun requestInAppReview(activity: Activity) {
        try {
            val manager = ReviewManagerFactory.create(activity)
            manager.requestReviewFlow().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    manager.launchReviewFlow(activity, task.result)
                } else {
                    AppLogger.e("PlayStoreHelper", "In-App Review request failed: ${task.exception?.message}")
                }
            }
        } catch (e: Exception) {
            AppLogger.e("PlayStoreHelper", "In-App Review check skipped", e)
        }
    }
}
