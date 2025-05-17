package dev.gonodono.dynamiccolor

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.google.android.material.color.DynamicColors
import dev.gonodono.dynamiccolor.databinding.FloatingViewBinding
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
        val themedContext =
            ContextThemeWrapper(this, R.style.Theme_DynamicColor)
        val enableDynamicColor =
            intent?.getBooleanExtra(EXTRA_ENABLE_COLOR, false) == true
        val inflater = if (enableDynamicColor) {
            val dynamicColorContext =
                DynamicColors.wrapContextIfAvailable(themedContext)
            LayoutInflater.from(dynamicColorContext)
        } else {
            LayoutInflater.from(themedContext)
        }

        val binding = FloatingViewBinding.inflate(inflater)
        val density = resources.displayMetrics.density
        val params = WindowManager.LayoutParams(
            (250 * density).roundToInt(),
            (300 * density).roundToInt(),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_DIM_BEHIND,
            PixelFormat.TRANSLUCENT
        )
        params.dimAmount = 0.7F

        binding.buttonClose.setOnClickListener {
            @SuppressLint("ImplicitSamInstance")
            stopService(Intent(this, FloatingViewService::class.java))
        }

        disposeFloatingView()

        floatingView = try {
            val view = binding.root
            getSystemService(WindowManager::class.java).addView(view, params)
            view
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Error adding View", e)
            null
        }

        return START_STICKY
    }

    private fun disposeFloatingView() {
        floatingView?.let { view ->
            getSystemService(WindowManager::class.java).removeView(view)
            floatingView = null
        }
    }

    override fun onDestroy() {
        disposeFloatingView()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

private const val TAG = "FloatingViewService"
internal const val EXTRA_ENABLE_COLOR = "enable_color"

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
        .setContentTitle(CHANNEL_NAME)
        .setContentText(CHANNEL_NAME)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .build()
    return notification
}