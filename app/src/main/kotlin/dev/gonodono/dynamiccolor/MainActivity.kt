package dev.gonodono.dynamiccolor

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dev.gonodono.dynamiccolor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var ui: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Settings.canDrawOverlays(this)) setUpUi() else openPermissionPage()
    }

    override fun onResume() {
        super.onResume()
        // The main UI is left empty if the permission isn't granted.
        if (!::ui.isInitialized && Settings.canDrawOverlays(this)) setUpUi()
    }

    private fun setUpUi() {
        val ui = ActivityMainBinding.inflate(layoutInflater).also { ui = it }
        ViewCompat.setOnApplyWindowInsetsListener(ui.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        ui.buttonShowView.setOnClickListener {
            val service = Intent(this, FloatingViewService::class.java)
                .putExtra(EXTRA_ENABLE_DYNAMIC, ui.checkDynamicColor.isChecked)
            ContextCompat.startForegroundService(this, service)
        }
        setContentView(ui.root)
    }

    private fun openPermissionPage() {
        val uri = "package:$packageName".toUri()
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)
        startActivity(intent)
    }
}