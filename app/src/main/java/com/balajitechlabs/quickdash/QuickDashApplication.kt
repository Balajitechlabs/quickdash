package com.balajitechlabs.quickdash

import android.app.Application
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import android.hardware.SensorManager
import android.hardware.Sensor
import android.content.Context
import android.content.Intent

import com.balajitechlabs.quickdash.core.di.AppContainer
import com.balajitechlabs.quickdash.core.di.AppContainerImpl
import com.balajitechlabs.quickdash.core.utils.LogManager
import com.balajitechlabs.quickdash.core.utils.ShakeDetector
import com.balajitechlabs.quickdash.core.data.EncryptedPrefsHelper
import com.balajitechlabs.quickdash.core.data.RemoteConfigManager
import com.balajitechlabs.quickdash.core.data.UserStore
import com.balajitechlabs.quickdash.features.broadcast.data.TelegramPollerWorker
import com.balajitechlabs.quickdash.features.broadcast.domain.TelegramTracker
import com.balajitechlabs.quickdash.features.dashboard.presentation.FloatingDialogActivity

class QuickDashApplication : Application() {
    private var shakeDetector: ShakeDetector? = null
    private var sensorManager: SensorManager? = null
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        
        container = AppContainerImpl(this)
        
        try { LogManager.init(this) } catch (_: Exception) {}
        LogManager.d("QuickDashApp", "Application starting up...")
        
        try { EncryptedPrefsHelper.init(this) } catch (e: Exception) {
            e.printStackTrace()
        }
        try { com.google.firebase.FirebaseApp.initializeApp(this) } catch (e: Exception) {
            e.printStackTrace()
        }
        try { RemoteConfigManager.fetchAndActivate() } catch (e: Exception) {
            e.printStackTrace()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userStore = container.userStore
                val analyticsEnabled = userStore.isAnalyticsEnabled()
                
                try {
                    com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(analyticsEnabled)
                } catch (_: Exception) {}

                if (userStore.shakeToOpen.first()) {
                    startShakeDetector()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Setup Global Exception Handler for Telegram Crash Reporting
        val defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            reportCrashToTelegram(thread, exception)
            defaultExceptionHandler?.uncaughtException(thread, exception)
        }

        try {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            // Schedule Telegram Admin Poller
            val periodicRequest = androidx.work.PeriodicWorkRequestBuilder<TelegramPollerWorker>(
                15, java.util.concurrent.TimeUnit.MINUTES
            ).setConstraints(constraints).build()
            
            androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "TelegramPoller",
                androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
                periodicRequest
            )
            
            // Also check immediately when the app starts
            val oneTimeRequest = androidx.work.OneTimeWorkRequestBuilder<TelegramPollerWorker>()
                .setConstraints(constraints).build()
                
            androidx.work.WorkManager.getInstance(this).enqueueUniqueWork(
                "TelegramPollerImmediate",
                androidx.work.ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun reportCrashToTelegram(thread: Thread, exception: Throwable) {
        val stackTrace = Log.getStackTraceString(exception)
        val deviceModel = Build.MODEL
        val androidVersion = Build.VERSION.RELEASE
        
        val message = """
            🚨 <b>QuickDash Crash Report</b> 🚨
            <b>Device:</b> $deviceModel (Android $androidVersion)
            <b>Thread:</b> ${thread.name}
            
            <b>Error:</b>
            <pre>${stackTrace.take(3000)}</pre>
        """.trimIndent()
        
        // Use global scope since the app is crashing and lifecycle scopes are dying
        CoroutineScope(Dispatchers.IO).launch {
            LogManager.e("CRASH", "Uncaught Exception in ${thread.name}", exception)
            TelegramTracker.sendMessage(message)
        }
        
        // Give it a tiny bit of time to send before the process dies completely
        Thread.sleep(200)
    }

    fun startShakeDetector() {
        if (shakeDetector != null) return
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        shakeDetector = ShakeDetector {
            // Launch the floating dialog activity on shake
            val intent = Intent(this, FloatingDialogActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
        }
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager?.registerListener(shakeDetector, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopShakeDetector() {
        shakeDetector?.let {
            sensorManager?.unregisterListener(it)
        }
        shakeDetector = null
    }
}
