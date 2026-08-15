package com.balajitechlabs.quickdash.core.services

import android.animation.ValueAnimator
import android.app.Service
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
import com.balajitechlabs.quickdash.core.utils.AppLogger
import com.balajitechlabs.quickdash.features.dashboard.presentation.FloatingDialogActivity
import com.balajitechlabs.quickdash.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.compose.ui.platform.ComposeView
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import kotlin.math.abs
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MySavedStateRegistryOwner : SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    fun handleLifecycleEvent(event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
    }

    fun performRestore(savedState: Bundle?) {
        savedStateRegistryController.performRestore(savedState)
    }
}


class FloatingBubbleService : Service() {
    private val serviceScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO)

    @Suppress("DEPRECATION")
    private fun triggerVibration(duration: Long, amplitude: Int = android.os.VibrationEffect.DEFAULT_AMPLITUDE) {
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(duration, amplitude))
                } else {
                    vibrator.vibrate(duration)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Suppress("DEPRECATION")
    private fun triggerDoubleVibration() {
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(android.os.VibrationEffect.createWaveform(longArrayOf(0, 15, 100, 15), intArrayOf(0, 255, 0, 255), -1))
                } else {
                    vibrator.vibrate(longArrayOf(0, 15, 100, 15), -1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var params: WindowManager.LayoutParams
    
    // Mini-widget overlay
    private var miniWidgetView: ComposeView? = null
    private var miniWidgetParams: WindowManager.LayoutParams? = null
    private var isMiniWidgetExpanded = false
    private val savedStateRegistryOwner = MySavedStateRegistryOwner()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (::floatingView.isInitialized) {
            val bubbleImage = floatingView.findViewById<ImageView>(R.id.img_bubble)
            bubbleImage?.setImageResource(R.drawable.ic_quickdash_tile)
            bubbleImage?.setBackgroundResource(R.drawable.bg_bubble_circle)
            val p = (10 * resources.displayMetrics.density).toInt()
            bubbleImage?.setPadding(p, p, p, p)
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        // ── Foreground notification ───────────────────────────────────
        val channelId = "quickdash_bubble_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Quick Bubble",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the QuickDash floating bubble alive"
                setShowBadge(false)
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)
                ?.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("QuickDash Active")
            .setContentText("Tap to open dashboard")
            .setSmallIcon(R.drawable.ic_quickdash_tile)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(101, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(101, notification)
            }
        } catch (e: Exception) {
            com.balajitechlabs.quickdash.core.utils.AppLogger.e("FloatingBubbleService", "Failed to promote FloatingBubbleService to foreground: ${e.message}")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
            com.balajitechlabs.quickdash.core.utils.AppLogger.e("FloatingBubbleService", "Overlay permission not granted. Stopping service.")
            stopSelf()
            return
        }

        // ── Inflate layout ────────────────────────────────────────────
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_bubble, null)

        val containerViewGroup = floatingView.findViewById<android.view.ViewGroup>(R.id.layout_bubble_container)
        containerViewGroup.layoutTransition = android.animation.LayoutTransition().apply {
            enableTransitionType(android.animation.LayoutTransition.CHANGING)
            setDuration(200)
        }

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
            x = 0
            y = 200
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(floatingView, params)

        // ── View refs ─────────────────────────────────────────────────
        val container   = floatingView.findViewById<View>(R.id.layout_bubble_container)
        val bubbleImage = floatingView.findViewById<ImageView>(R.id.img_bubble)
        val bubbleMenu  = floatingView.findViewById<View>(R.id.layout_bubble_menu)

        bubbleImage.setImageResource(R.drawable.ic_quickdash_tile)
        val p = (10 * resources.displayMetrics.density).toInt()
        bubbleImage.setPadding(p, p, p, p)
        container.setBackgroundResource(0)

        // ── Helpers ───────────────────────────────────────────────────
        fun collapseMenu() {
            bubbleMenu.visibility = View.GONE
            container.setBackgroundResource(0)
            windowManager.updateViewLayout(floatingView, params)
        }

        fun expandMenu() {
            bubbleMenu.visibility = View.VISIBLE
            container.setBackgroundResource(R.drawable.bg_floating_menu)
            windowManager.updateViewLayout(floatingView, params)
        }

        fun launchSection(section: String) {
            collapseMenu()
            val intent = Intent(this@FloatingBubbleService, FloatingDialogActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("launch_section", section)
            }
            startActivity(intent)
        }
        
        fun toggleMiniWidget() {
            if (miniWidgetView == null) {
                // Initialize Mini Widget
                savedStateRegistryOwner.performRestore(null)
                savedStateRegistryOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                
                miniWidgetView = ComposeView(this).apply {
                    setViewTreeLifecycleOwner(savedStateRegistryOwner)
                    setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
                    setContent {
                        Box(modifier = Modifier.size(width = 64.dp, height = if (isMiniWidgetExpanded) 220.dp else 56.dp).background(Color.DarkGray)) {
                            Text("Widget", color = Color.White)
                        }
                    }
                }
                
                val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
                
                miniWidgetParams = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    layoutFlag,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.CENTER_VERTICAL or Gravity.END
                    x = 0
                    y = 0
                }
                
                windowManager.addView(miniWidgetView, miniWidgetParams)
                savedStateRegistryOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
                savedStateRegistryOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            } else {
                isMiniWidgetExpanded = !isMiniWidgetExpanded
                // Force recomposition
                miniWidgetView?.setContent {
                    Box(modifier = Modifier.size(width = 64.dp, height = if (isMiniWidgetExpanded) 220.dp else 56.dp).background(Color.DarkGray)) {
                        Text("Widget", color = Color.White)
                    }
                }
                windowManager.updateViewLayout(miniWidgetView, miniWidgetParams)
            }
        }

        // ── Snap-to-edge helper ───────────────────────────────────────
        fun snapToEdge() {
            val screenWidth = resources.displayMetrics.widthPixels
            val bubbleWidth = floatingView.width.takeIf { it > 0 } ?: 150

            // Determine target X: snap left or right edge with 12dp margin
            val margin = (12 * resources.displayMetrics.density).toInt()
            val targetX = if (params.x + bubbleWidth / 2 < screenWidth / 2) margin
                          else screenWidth - bubbleWidth - margin

            ValueAnimator.ofInt(params.x, targetX).apply {
                duration = 250
                interpolator = DecelerateInterpolator()
                addUpdateListener { anim ->
                    params.x = anim.animatedValue as Int
                    try { windowManager.updateViewLayout(floatingView, params) } catch (e: Exception) {
                        AppLogger.e("FloatingBubbleService", "Failed to update floating view layout during snap", e)
                    }
                }
                start()
            }
        }

        // ── Menu item clicks ──────────────────────────────────────────
        floatingView.findViewById<View>(R.id.btn_menu_upi).setOnClickListener {
            triggerVibration(10)
            launchSection("com.balajitechlabs.quickdash.ACTION_QUICK_UPI")
        }
        floatingView.findViewById<View>(R.id.btn_menu_chat).setOnClickListener {
            triggerVibration(10)
            launchSection("com.balajitechlabs.quickdash.ACTION_QUICK_CHAT")
        }
        floatingView.findViewById<View>(R.id.btn_menu_search).setOnClickListener {
            triggerVibration(10)
            launchSection("com.balajitechlabs.quickdash.ACTION_QUICK_SEARCH")
        }
        floatingView.findViewById<View>(R.id.btn_menu_notes).setOnClickListener {
            triggerVibration(10)
            launchSection("com.balajitechlabs.quickdash.ACTION_QUICK_NOTES")
        }
        floatingView.findViewById<View>(R.id.btn_menu_calculator).setOnClickListener {
            triggerVibration(10)
            launchSection("com.balajitechlabs.quickdash.ACTION_QUICK_CALCULATOR")
        }
        floatingView.findViewById<View>(R.id.btn_menu_timer).setOnClickListener {
            triggerVibration(10)
            launchSection("com.balajitechlabs.quickdash.ACTION_QUICK_TIMER")
        }
        floatingView.findViewById<View>(R.id.btn_menu_settings).setOnClickListener {
            triggerVibration(10)
            launchSection("com.balajitechlabs.quickdash.ACTION_QUICK_SETTINGS")
        }
        floatingView.findViewById<View>(R.id.btn_menu_web).setOnClickListener {
            triggerVibration(10)
            launchSection("com.balajitechlabs.quickdash.ACTION_QUICK_WEB")
        }
        floatingView.findViewById<View>(R.id.btn_menu_close).setOnClickListener {
            triggerVibration(10)
            collapseMenu()
        }

        // ── Drag + tap + Radial Gesture logic ──────────────────────────
        var initialX = 0; var initialY = 0
        var initialTouchX = 0f; var initialTouchY = 0f
        var lastTapTime = 0L
        val tapHandler = android.os.Handler(android.os.Looper.getMainLooper())
        var singleTapRunnable: Runnable? = null
        var isDragging = false
        var isRadialMenuOpen = false
        var activeRadialSector = -1
        var radialView: ComposeView? = null
        var radialParams: WindowManager.LayoutParams? = null

        val radialState = androidx.compose.runtime.mutableIntStateOf(-1)

        fun dismissRadialMenu() {
            if (radialView != null) {
                try {
                    windowManager.removeView(radialView)
                } catch (e: Exception) {
                    AppLogger.e("FloatingBubbleService", "Error removing radial view", e)
                }
                radialView = null
                isRadialMenuOpen = false
                radialState.intValue = -1
                activeRadialSector = -1
            }
        }

        var currentCustomTools = listOf("upi", "notes", "calc", "timer")
        val userStore = com.balajitechlabs.quickdash.core.data.UserStore(this@FloatingBubbleService)
        serviceScope.launch {
            try {
                userStore.radialCustomTools.collect { tools ->
                    currentCustomTools = tools
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun showRadialMenu(centerX: Int, centerY: Int) {
            dismissRadialMenu()
            isRadialMenuOpen = true
            triggerVibration(30)

            val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

            val sizePx = (240 * resources.displayMetrics.density).toInt()
            radialParams = WindowManager.LayoutParams(
                sizePx,
                sizePx,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = centerX - sizePx / 2
                y = centerY - sizePx / 2
            }

            val dynamicActions = com.balajitechlabs.quickdash.core.ui.components.RadialToolCatalog.buildRadialActions(currentCustomTools)

            radialView = ComposeView(this@FloatingBubbleService).apply {
                setViewTreeLifecycleOwner(savedStateRegistryOwner)
                setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
                setContent {
                    com.balajitechlabs.quickdash.core.ui.components.RadialBubbleMenu(
                        actions = dynamicActions,
                        activeSectorIndex = radialState.intValue,
                        onActionSelected = { action ->
                            triggerVibration(15)
                            dismissRadialMenu()
                            launchSection(action.actionIntent)
                        },
                        onDismiss = {
                            dismissRadialMenu()
                        }
                    )
                }
            }

            try {
                windowManager.addView(radialView, radialParams)
            } catch (e: Exception) {
                AppLogger.e("FloatingBubbleService", "Failed to add radial menu view", e)
            }
        }

        val longPressRunnable = Runnable {
            if (!isDragging) {
                val bubbleWidth = floatingView.width.takeIf { it > 0 } ?: (48 * resources.displayMetrics.density).toInt()
                val bubbleHeight = floatingView.height.takeIf { it > 0 } ?: (48 * resources.displayMetrics.density).toInt()
                showRadialMenu(params.x + bubbleWidth / 2, params.y + bubbleHeight / 2)
            }
        }

        bubbleImage.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x; initialY = params.y
                    initialTouchX = event.rawX; initialTouchY = event.rawY
                    isDragging = false
                    isRadialMenuOpen = false
                    tapHandler.postDelayed(longPressRunnable, 350)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()

                    if (isRadialMenuOpen) {
                        // Calculate sector angle based on delta from touch down
                        val distance = kotlin.math.hypot(event.rawX - initialTouchX, event.rawY - initialTouchY)
                        if (distance > 35 * resources.displayMetrics.density) {
                            var angle = Math.toDegrees(kotlin.math.atan2((event.rawY - initialTouchY).toDouble(), (event.rawX - initialTouchX).toDouble()))
                            if (angle < 0) angle += 360.0

                            // 4 Cardinal sectors:
                            // Right (East): 315° to 45° -> Index 1 (Slot 1)
                            // Bottom (South): 45° to 135° -> Index 2 (Slot 2)
                            // Left (West): 135° to 225° -> Index 3 (Slot 3)
                            // Top (North): 225° to 315° -> Index 0 (Slot 0)
                            val sector = when {
                                angle >= 315.0 || angle < 45.0 -> 1
                                angle in 45.0..135.0 -> 2
                                angle in 135.0..225.0 -> 3
                                else -> 0
                            }
                            if (sector != activeRadialSector) {
                                activeRadialSector = sector
                                radialState.intValue = sector
                                triggerVibration(10)
                            }
                        } else {
                            if (activeRadialSector != -1) {
                                activeRadialSector = -1
                                radialState.intValue = -1
                            }
                        }
                    } else {
                        if (!isDragging && (abs(dx) > 12 || abs(dy) > 12)) {
                            isDragging = true
                            tapHandler.removeCallbacks(longPressRunnable)
                        }
                        if (isDragging) {
                            params.x = initialX + dx
                            params.y = initialY + dy
                            try { windowManager.updateViewLayout(floatingView, params) } catch (e: Exception) {
                                AppLogger.e("FloatingBubbleService", "Error updating bubble layout", e)
                            }
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    tapHandler.removeCallbacks(longPressRunnable)

                    if (isRadialMenuOpen) {
                        val selectedIndex = activeRadialSector
                        dismissRadialMenu()
                        if (selectedIndex in 0..3) {
                            val dynamicActions = com.balajitechlabs.quickdash.core.ui.components.RadialToolCatalog.buildRadialActions(currentCustomTools)
                            val chosenAction = dynamicActions.getOrNull(selectedIndex)
                            if (chosenAction != null) {
                                triggerVibration(20)
                                launchSection(chosenAction.actionIntent)
                            }
                        }
                    } else if (isDragging) {
                        // Released after drag — snap to nearest edge
                        snapToEdge()
                    } else {
                        val xDiff = abs(event.rawX - initialTouchX)
                        val yDiff = abs(event.rawY - initialTouchY)
                        if (xDiff < 15 && yDiff < 15) {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastTapTime < 300) {
                                singleTapRunnable?.let { tapHandler.removeCallbacks(it) }
                                lastTapTime = 0L
                                triggerDoubleVibration()
                                val userStore = com.balajitechlabs.quickdash.core.data.UserStore(this@FloatingBubbleService)
                                serviceScope.launch {
                                    userStore.setBubbleEnabled(false)
                                }
                                sendBroadcast(Intent("com.balajitechlabs.quickdash.CLOSE_APP"))
                                stopSelf()
                            } else {
                                lastTapTime = currentTime
                                singleTapRunnable = Runnable {
                                    triggerVibration(20)
                                    if (bubbleMenu.visibility == View.VISIBLE) collapseMenu() else expandMenu()
                                }
                                tapHandler.postDelayed(singleTapRunnable!!, 300)
                            }
                        }
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    tapHandler.removeCallbacks(longPressRunnable)
                    dismissRadialMenu()
                    false
                }
                else -> false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        if (::floatingView.isInitialized) {
            try { windowManager.removeView(floatingView) } catch (e: Exception) {
                AppLogger.e("FloatingBubbleService", "Failed to remove floating view on destroy", e)
            }
        }
        miniWidgetView?.let {
            savedStateRegistryOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            savedStateRegistryOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            savedStateRegistryOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            try { windowManager.removeView(it) } catch (e: Exception) {
                AppLogger.e("FloatingBubbleService", "Failed to remove mini widget view on destroy", e)
            }
        }
    }
}
