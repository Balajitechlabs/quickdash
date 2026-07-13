package com.balajitechlabs.quickdash.core.utils

import android.content.Context
import android.content.Intent
import android.content.ActivityNotFoundException
import android.widget.Toast

fun Context.safeStartActivity(intent: Intent, fallbackErrorMessage: String = "No app found to handle this action") {
    try {
        if (this !is android.app.Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        this.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, fallbackErrorMessage, Toast.LENGTH_SHORT).show()
        AppLogger.e("IntentUtils", "Failed to launch intent: ${intent.action}", e)
    } catch (e: Exception) {
        AppLogger.e("IntentUtils", "Error starting activity", e)
    }
}
