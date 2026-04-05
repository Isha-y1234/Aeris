package com.runanywhere.kotlin_starter_example.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import android.view.WindowManager

/**
 * FlashAlertService provides a full-screen visual overlay when a sound is detected.
 * Updated to flicker/flash multiple times over a 3-second period.
 */
class FlashAlertService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var flashRunnable: Runnable? = null

    companion object {
        const val EXTRA_COLOR = "flash_color"
        const val TOTAL_DURATION_MS = 3000L
        const val FLASH_COUNT = 3 // Number of full on/off cycles

        fun start(context: Context, color: Int) {
            val intent = Intent(context, FlashAlertService::class.java)
                .putExtra(EXTRA_COLOR, color)
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val color = intent?.getIntExtra(EXTRA_COLOR, Color.RED) ?: Color.RED
        
        // 1. Cancel any pending animation
        flashRunnable?.let { handler.removeCallbacks(it) }
        
        // 2. Remove existing view
        removeOverlay()

        // 3. Start flickering animation
        startFlicker(color)
        
        return START_NOT_STICKY
    }

    private fun startFlicker(color: Int) {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            PixelFormat.TRANSLUCENT
        )

        val flashView = View(this).apply { 
            setBackgroundColor(color)
            alpha = 0.4f
            visibility = View.INVISIBLE // Start invisible
        }
        overlayView = flashView
        
        try {
            windowManager?.addView(flashView, params)
        } catch (e: Exception) {
            stopSelf()
            return
        }

        // Calculate timing: 3 flashes means 6 visibility toggles over 3000ms
        val interval = TOTAL_DURATION_MS / (FLASH_COUNT * 2)
        var toggleCount = 0

        val runnable = object : Runnable {
            override fun run() {
                if (overlayView == null) return
                
                // Toggle visibility
                overlayView?.visibility = if (toggleCount % 2 == 0) View.VISIBLE else View.INVISIBLE
                toggleCount++

                if (toggleCount < FLASH_COUNT * 2) {
                    handler.postDelayed(this, interval)
                } else {
                    dismiss()
                }
            }
        }
        flashRunnable = runnable
        handler.post(runnable)
    }

    private fun dismiss() {
        removeOverlay()
        flashRunnable = null
        stopSelf()
    }

    private fun removeOverlay() {
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                // Ignore
            }
            overlayView = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        flashRunnable?.let { handler.removeCallbacks(it) }
        removeOverlay()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
