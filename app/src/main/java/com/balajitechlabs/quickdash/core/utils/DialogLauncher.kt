package com.balajitechlabs.quickdash.core.utils

import android.content.Context
import android.content.Intent
import com.balajitechlabs.quickdash.features.dashboard.presentation.FloatingDialogActivity

object DialogLauncher {
    fun open(context: Context, allowMultiple: Boolean = false) {
        val intent = Intent(context, FloatingDialogActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
            if (allowMultiple) {
                addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            }
        }
        context.startActivity(intent)
    }
}
