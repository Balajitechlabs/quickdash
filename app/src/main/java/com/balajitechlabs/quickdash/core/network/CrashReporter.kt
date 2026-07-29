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
            app_version = "5.1.1",
            version_code = 511,
            device = "${Build.MANUFACTURER} ${Build.MODEL}",
            android_version = "${Build.VERSION.SDK_INT}",
            stacktrace = Log.getStackTraceString(throwable),
            last_action = lastAction
        )
        return QuickDashApiClient.submitCrashReport(request)
    }
}
