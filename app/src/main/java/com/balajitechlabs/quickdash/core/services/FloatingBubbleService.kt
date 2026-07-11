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
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var params: WindowManager.LayoutParams
    
    // Mini-widget overlay
    private var miniWidgetView: ComposeView? = null
    private var miniWidgetParams: WindowManager.LayoutParams? = null
    private var isMiniWidgetExpanded = false
    private val savedStateRegistryOwner = MySavedStateRegistryOwner()

    override fun onBind(intent: Intent?): IBinder? = null

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
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("QuickDash Active")
            .setContentText("Tap to open dashboard")
            .setSmallIcon(R.drawable.ic_quickdash_tile)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .build()

        startForeground(101, notification)

        // ── Inflate layout ────────────────────────────────────────────
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_bubble, null)

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

        bubbleImage.setImageResource(R.mipmap.ic_launcher_round)
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
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
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
                    try { windowManager.updateViewLayout(floatingView, params) } catch (_: Exception) {}
                }
                start()
            }
        }

        // ── Menu item clicks ──────────────────────────────────────────
        floatingView.findViewById<View>(R.id.btn_menu_upi).setOnClickListener {
            launchSection("com.balajitechlabs.quickdash.ACTION_QUICK_UPI")
        }
        floatingView.findViewById<View>(R.id.btn_menu_chat).setOnClickListener {
            launchSection("com.balajitechlabs.quickdash.ACTION_QUICK_CHAT")
        }
        floatingView.findViewById<View>(R.id.btn_menu_search).setOnClickListener {
            launchSection("com.balajitechlabs.quickdash.ACTION_QUICK_SEARCH")
        }
        floatingView.findViewById<View>(R.id.btn_menu_notes).setOnClickListener {
            launchSection("com.balajitechlabs.quickdash.ACTION_QUICK_NOTES")
        }
        floatingView.findViewById<View>(R.id.btn_menu_calculator).setOnClickListener {
            launchSection("com.balajitechlabs.quickdash.ACTION_QUICK_CALCULATOR")
        }
        floatingView.findViewById<View>(R.id.btn_menu_timer).setOnClickListener {
            launchSection("com.balajitechlabs.quickdash.ACTION_QUICK_TIMER")
        }
        floatingView.findViewById<View>(R.id.btn_menu_settings).setOnClickListener {
            launchSection("com.balajitechlabs.quickdash.ACTION_QUICK_SETTINGS")
        }
        floatingView.findViewById<View>(R.id.btn_menu_close).setOnClickListener {
            collapseMenu()
        }

        // ── Drag + tap logic ──────────────────────────────────────────
        var initialX = 0; var initialY = 0
        var initialTouchX = 0f; var initialTouchY = 0f
        var lastTapTime = 0L
        val tapHandler = android.os.Handler(android.os.Looper.getMainLooper())
        var singleTapRunnable: Runnable? = null
        var isDragging = false

        bubbleImage.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x; initialY = params.y
                    initialTouchX = event.rawX; initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (!isDragging && (abs(dx) > 8 || abs(dy) > 8)) isDragging = true
                    if (isDragging) {
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager.updateViewLayout(floatingView, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val xDiff = abs(event.rawX - initialTouchX)
                    val yDiff = abs(event.rawY - initialTouchY)

                    if (isDragging) {
                        // Released after drag — snap to nearest edge
                        snapToEdge()
                    } else if (xDiff < 10 && yDiff < 10) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastTapTime < 300) {
                            // Double tap → disable bubble
                            singleTapRunnable?.let { tapHandler.removeCallbacks(it) }
                            lastTapTime = 0L
                            val userStore = com.balajitechlabs.quickdash.core.data.UserStore(this@FloatingBubbleService)
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                userStore.setBubbleEnabled(false)
                            }
                            sendBroadcast(Intent("com.balajitechlabs.quickdash.CLOSE_APP"))
                            stopSelf()
                        } else {
                            // Single tap → toggle menu
                            lastTapTime = currentTime
                            singleTapRunnable = Runnable {
                                if (bubbleMenu.visibility == View.VISIBLE) collapseMenu() else expandMenu()
                            }
                            tapHandler.postDelayed(singleTapRunnable!!, 300)
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            try { windowManager.removeView(floatingView) } catch (_: Exception) {}
        }
        miniWidgetView?.let {
            savedStateRegistryOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            savedStateRegistryOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            savedStateRegistryOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            try { windowManager.removeView(it) } catch (_: Exception) {}
        }
    }
}
