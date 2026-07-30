package com.balajitechlabs.quickdash.core.network

import android.content.Context
import android.os.Build
import android.util.Log
import java.util.UUID

object CrashReporter {

    suspend fun reportCrash(
        context: Context,
        throwable: Throwable,
        lastAction: String = ""
    ): Boolean {
        val request = CrashReportRequest(
            id = UUID.randomUUID().toString(),
            app_version = com.balajitechlabs.quickdash.BuildConfig.VERSION_NAME,
            version_code = com.balajitechlabs.quickdash.BuildConfig.VERSION_CODE,
            device = "${Build.MANUFACTURER} ${Build.MODEL}",
            android_version = "${Build.VERSION.SDK_INT}",
            stacktrace = Log.getStackTraceString(throwable),
            last_action = lastAction
        )
        return QuickDashApiClient.submitCrashReport(request)
    }
}
