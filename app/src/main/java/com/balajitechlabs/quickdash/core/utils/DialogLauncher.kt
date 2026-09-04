/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/utils
 * File: DialogLauncher.kt
 * Description: EssentialX-styled component for core/utils supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.utils

import android.content.Context
import android.content.Intent
import com.balajitechlabs.quickdash.features.dashboard.presentation.FloatingDialogActivity

import com.balajitechlabs.quickdash.core.utils.safeStartActivity

object DialogLauncher {
    fun open(context: Context, allowMultiple: Boolean = false) {
        val intent = Intent(context, FloatingDialogActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
            if (allowMultiple) {
                addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            }
        }
        context.safeStartActivity(intent)
    }
}
