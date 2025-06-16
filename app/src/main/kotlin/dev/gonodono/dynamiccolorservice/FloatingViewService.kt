package dev.gonodono.dynamiccolorservice

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.os.IBinder
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
import com.google.android.material.color.DynamicColors
import dev.gonodono.dynamiccolorservice.databinding.FloatingViewBinding
import kotlin.math.roundToInt

class FloatingViewService : Service() {

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification(this))
    }

    private var floatingView: View? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        val enableDynamic =
            intent?.getBooleanExtra(EXTRA_ENABLE_DYNAMIC, false) == true

        // Proper method to obtain a UI Context from a non-UI one.
        val defaultDisplay = getSystemService(DisplayManager::class.java)
            .getDisplay(Display.DEFAULT_DISPLAY)
        val windowContext = createDisplayContext(defaultDisplay)
            .createWindowContext(TYPE_APPLICATION_OVERLAY, null)

        // Material Components Views require a compatible theme.
        val themedContext = ContextThemeWrapper(
            windowContext,
            R.style.Theme_DynamicColorService
        )

        val inflaterContext = if (enableDynamic) {
            DynamicColors.wrapContextIfAvailable(themedContext)
        } else {
            themedContext
        }
        val inflater = LayoutInflater.from(inflaterContext)

        val binding = FloatingViewBinding.inflate(inflater)
        @SuppressLint("SetTextI18n")
        binding.textDynamic.text = if (enableDynamic) "Enabled" else "Disabled"
        binding.buttonClose.setOnClickListener { stopSelf() }

        val density = resources.displayMetrics.density
        val params = WindowManager.LayoutParams(
            (250 * density).roundToInt(),
            (300 * density).roundToInt(),
            TYPE_APPLICATION_OVERLAY,
            FLAG_DIM_BEHIND,
            PixelFormat.TRANSLUCENT
        )
        params.dimAmount = 0.7F

        disposeFloatingView()

        floatingView = try {
            val view = binding.root
            windowManager.addView(view, params)
            view
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Error adding View", e)
            null
        }

        return START_STICKY
    }

    private fun disposeFloatingView() {
        floatingView?.let { view ->
            windowManager.removeView(view)
            floatingView = null
        }
    }

    override fun onDestroy() {
        disposeFloatingView()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

internal const val EXTRA_ENABLE_DYNAMIC =
    "${BuildConfig.APPLICATION_ID}.extra.ENABLE_DYNAMIC"

private const val TAG = "FloatingViewService"

private const val CHANNEL_ID = "floating_view_service"
private const val CHANNEL_NAME = "Floating View Service"

private fun createNotification(context: Context): Notification {
    val manager = context.getSystemService(NotificationManager::class.java)
    if (manager.getNotificationChannel(CHANNEL_ID) == null) {
        val channel =
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, IMPORTANCE_DEFAULT)
        manager.createNotificationChannel(channel)
    }
    val notification = Notification.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(CHANNEL_NAME)
        .setContentText(CHANNEL_NAME)
        .build()
    return notification
}

private val Context.windowManager: WindowManager
    get() = getSystemService(WindowManager::class.java)