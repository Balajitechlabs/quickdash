/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/services
 * File: FloatingBubbleService.kt
 * Description: Foreground service rendering the draggable floating bubble overlay window and handling touch gestures.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.services

import android.animation.ValueAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.balajitechlabs.quickdash.R
import com.balajitechlabs.quickdash.core.utils.AppLogger
import com.balajitechlabs.quickdash.features.dashboard.presentation.FloatingDialogActivity
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.abs
import android.util.Log

class MySavedStateRegistryOwner : SavedStateRegistryOwner, ViewModelStoreOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val vmStore = ViewModelStore()

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = vmStore

    fun handleLifecycleEvent(event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
    }

    fun performRestore(savedState: Bundle?) {
        savedStateRegistryController.performRestore(savedState)
    }
}

class FloatingBubbleService : Service() {

    private val serviceScope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO
    )

    @Suppress("DEPRECATION")
    private fun triggerVibration(duration: Long) {
        try {
            val v = getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator ?: return
            if (!v.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(android.os.VibrationEffect.createOneShot(duration, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                v.vibrate(duration)
            }
        } catch (_: Exception) {}
    }

    @Suppress("DEPRECATION")
    private fun triggerDoubleVibration() {
        try {
            val v = getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator ?: return
            if (!v.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(android.os.VibrationEffect.createWaveform(longArrayOf(0, 15, 100, 15), intArrayOf(0, 255, 0, 255), -1))
            } else {
                v.vibrate(longArrayOf(0, 15, 100, 15), -1)
            }
        } catch (_: Exception) {}
    }

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var params: WindowManager.LayoutParams
    private val savedStateRegistryOwner = MySavedStateRegistryOwner()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (::floatingView.isInitialized) refreshBubbleIcon()
        return START_STICKY
    }

    private fun refreshBubbleIcon() {
        val img = floatingView.findViewById<ImageView>(R.id.img_bubble) ?: return
        img.setImageResource(R.drawable.app_logo)
        img.setBackgroundResource(R.drawable.bg_bubble_circle)
        val p = (4 * resources.displayMetrics.density).toInt()
        img.setPadding(p, p, p, p)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) img.clipToOutline = true
    }

    override fun onCreate() {
        super.onCreate()

        savedStateRegistryOwner.performRestore(null)
        savedStateRegistryOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        savedStateRegistryOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        savedStateRegistryOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        startForegroundNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
            AppLogger.e("FloatingBubbleService", "Overlay permission not granted")
            stopSelf()
            return
        }

        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_bubble, null)
        floatingView.findViewById<View>(R.id.layout_bubble_menu)?.visibility = View.GONE

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0; y = 200
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(floatingView, params)
        refreshBubbleIcon()

        // ── Track pinned tools from DataStore ──────────────────────────
        var pinnedToolIds: List<String> = emptyList()
        val userStore = com.balajitechlabs.quickdash.core.data.UserStore(this@FloatingBubbleService)
        serviceScope.launch {
            try {
                userStore.favoriteTools.collect { favs ->
                    pinnedToolIds = when {
                        favs.isNullOrBlank() || favs == "EMPTY" -> emptyList()
                        else -> favs.split(",").mapNotNull { raw ->
                            when (raw.trim().uppercase()) {
                                "UPI"        -> "upi"
                                "CHAT",
                                "WHATSAPP"   -> "chat"
                                "CLIPBOARD"  -> "clipboard"
                                "NOTES"      -> "notes"
                                "CAPTURE"    -> "capture"
                                "WIFI"       -> "wifi"
                                "TIMER"      -> "timer"
                                "POMODORO"   -> "pomodoro"
                                "PASSWORD"   -> "password"
                                "QRSCANNER"  -> "qr"
                                "CALCULATOR" -> "calc"
                                "SEARCH"     -> "web"
                                "VOICEMEMOS" -> "voice"
                                "CONVERTER"  -> "converter"
                                "TRANSLATOR" -> "translate"
                                "REMINDERS"  -> "reminders"
                                "CONTACT_QR" -> "contactqr"
                                else         -> null
                            }
                        }.take(6)
                    }
                }
            } catch (e: Exception) { Log.e("QuickDash", "Error occurred: ${e.message}", e) }
        }

        // ── Snap-to-edge ───────────────────────────────────────────────
        fun snapToEdge() {
            val sw = resources.displayMetrics.widthPixels
            val bw = floatingView.width.takeIf { it > 0 } ?: 150
            val margin = (12 * resources.displayMetrics.density).toInt()
            val targetX = if (params.x + bw / 2 < sw / 2) margin else sw - bw - margin
            ValueAnimator.ofInt(params.x, targetX).apply {
                duration = 240
                interpolator = DecelerateInterpolator()
                addUpdateListener { anim ->
                    params.x = anim.animatedValue as Int
                    try { windowManager.updateViewLayout(floatingView, params) } catch (_: Exception) {}
                }
                start()
            }
        }

        fun launchDashboard() {
            startActivity(Intent(this@FloatingBubbleService, FloatingDialogActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            })
        }

        fun launchSection(actionIntent: String) {
            startActivity(Intent(this@FloatingBubbleService, FloatingDialogActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("launch_section", actionIntent)
            })
        }

        // ── Radial menu state ──────────────────────────────────────────
        var isRadialMenuOpen = false
        var activeRadialSector = -1
        var radialView: ComposeView? = null
        val radialState = mutableIntStateOf(-1)

        // Screen center — the menu is always drawn here
        val screenCenterX = resources.displayMetrics.widthPixels / 2f
        val screenCenterY = resources.displayMetrics.heightPixels / 2f

        fun dismissRadialMenu() {
            radialView?.let {
                try { windowManager.removeView(it) } catch (_: Exception) {}
            }
            radialView = null
            isRadialMenuOpen = false
            radialState.intValue = -1
            activeRadialSector = -1
        }

        fun showRadialMenu() {
            dismissRadialMenu()
            isRadialMenuOpen = true
            triggerVibration(35)

            // Full-screen overlay so the composable can center itself
            val radialParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 0; y = 0
            }

            val actions = com.balajitechlabs.quickdash.core.ui.components.RadialToolCatalog
                .buildRadialActions(pinnedToolIds)

            radialView = ComposeView(this@FloatingBubbleService).apply {
                setViewTreeLifecycleOwner(savedStateRegistryOwner)
                setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
                setViewTreeViewModelStoreOwner(savedStateRegistryOwner)
                setContent {
                    com.balajitechlabs.quickdash.core.ui.components.RadialBubbleMenu(
                        actions = actions,
                        activeSectorIndex = radialState.intValue,
                        onActionSelected = { action ->
                            triggerVibration(18)
                            dismissRadialMenu()
                            launchSection(action.actionIntent)
                        },
                        onDismiss = { dismissRadialMenu() }
                    )
                }
            }

            try { windowManager.addView(radialView, radialParams) } catch (e: Exception) {
                AppLogger.e("FloatingBubbleService", "Failed to add radial menu", e)
            }
        }

        // ── Touch listener ─────────────────────────────────────────────
        var initialX = 0; var initialY = 0
        var initialTouchX = 0f; var initialTouchY = 0f
        var lastTapTime = 0L
        val tapHandler = android.os.Handler(android.os.Looper.getMainLooper())
        var singleTapRunnable: Runnable? = null
        var isDragging = false
        val touchSlop = (14 * resources.displayMetrics.density).toInt()

        val longPressRunnable = Runnable {
            if (!isDragging) showRadialMenu()
        }

        val bubbleImage = floatingView.findViewById<ImageView>(R.id.img_bubble)

        bubbleImage.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x; initialY = params.y
                    initialTouchX = event.rawX; initialTouchY = event.rawY
                    isDragging = false
                    tapHandler.postDelayed(longPressRunnable, 380)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()

                    if (isRadialMenuOpen) {
                        // Sector detection is relative to the SCREEN CENTER
                        // (where the radial menu is drawn), not the bubble position.
                        val deltaX = event.rawX - screenCenterX
                        val deltaY = event.rawY - screenCenterY
                        val distance = kotlin.math.hypot(deltaX, deltaY)

                        if (distance > 48 * resources.displayMetrics.density) {
                            // User is pointing at one of the nodes
                            var angle = Math.toDegrees(kotlin.math.atan2(deltaY.toDouble(), deltaX.toDouble()))
                            if (angle < 0) angle += 360.0
                            val count = pinnedToolIds.size.coerceAtLeast(1)
                            val step = 360.0 / count
                            // Match the radial layout start angle (270 = North)
                            val sector = (((angle - 270.0 + 360.0 + step / 2.0) % 360.0) / step).toInt() % count
                            if (sector != activeRadialSector) {
                                activeRadialSector = sector
                                radialState.intValue = sector
                                triggerVibration(8)
                            }
                        } else {
                            // Finger near center — no selection
                            if (activeRadialSector != -1) {
                                activeRadialSector = -1
                                radialState.intValue = -1
                            }
                        }
                    } else {
                        if (!isDragging && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                            isDragging = true
                            tapHandler.removeCallbacks(longPressRunnable)
                        }
                        if (isDragging) {
                            params.x = initialX + dx
                            params.y = initialY + dy
                            try { windowManager.updateViewLayout(floatingView, params) } catch (_: Exception) {}
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    tapHandler.removeCallbacks(longPressRunnable)

                    when {
                        isRadialMenuOpen -> {
                            val selected = activeRadialSector
                            if (selected != -1) {
                                // Drag-release to fire selected tool
                                dismissRadialMenu()
                                val actions = com.balajitechlabs.quickdash.core.ui.components.RadialToolCatalog
                                    .buildRadialActions(pinnedToolIds)
                                val chosen = actions.getOrNull(selected)
                                if (chosen != null) {
                                    triggerVibration(22)
                                    launchSection(chosen.actionIntent)
                                }
                            }
                            // No sector selected → keep menu open for tap
                        }

                        isDragging -> snapToEdge()

                        else -> {
                            val xDiff = abs(event.rawX - initialTouchX)
                            val yDiff = abs(event.rawY - initialTouchY)
                            if (xDiff < 12 && yDiff < 12) {
                                val now = System.currentTimeMillis()
                                if (now - lastTapTime < 320) {
                                    // Double tap → close completely
                                    singleTapRunnable?.let { tapHandler.removeCallbacks(it) }
                                    lastTapTime = 0L
                                    triggerDoubleVibration()
                                    serviceScope.launch { userStore.setBubbleEnabled(false) }
                                    sendBroadcast(Intent("com.balajitechlabs.quickdash.CLOSE_APP"))
                                    stopSelf()
                                } else {
                                    // Single tap → open dashboard
                                    lastTapTime = now
                                    singleTapRunnable?.let { tapHandler.removeCallbacks(it) }
                                    val r = Runnable {
                                        triggerVibration(15)
                                        launchDashboard()
                                    }
                                    singleTapRunnable = r
                                    tapHandler.postDelayed(r, 320)
                                }
                            }
                        }
                    }
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    tapHandler.removeCallbacks(longPressRunnable)
                    isDragging = false
                    false
                }

                else -> false
            }
        }
    }

    private fun startForegroundNotification() {
        val channelId = "quickdash_bubble_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Quick Bubble", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Keeps the QuickDash floating bubble alive"
                setShowBadge(false)
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.createNotificationChannel(channel)
        }
        val notification = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setContentTitle("QuickDash Active")
            .setContentText("Tap to open · Long press for quick tools")
            .setSmallIcon(R.drawable.ic_quickdash_tile)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .build()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                startForeground(101, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            else
                startForeground(101, notification)
        } catch (e: Exception) {
            AppLogger.e("FloatingBubbleService", "Foreground start failed: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        if (::floatingView.isInitialized) {
            try { windowManager.removeView(floatingView) } catch (_: Exception) {}
        }
    }
}
