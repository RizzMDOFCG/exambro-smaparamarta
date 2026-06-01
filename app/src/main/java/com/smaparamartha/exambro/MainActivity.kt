package com.smaparamartha.exambro

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.BatteryManager
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var tvBattery: TextView
    private lateinit var ivSignal: ImageView
    private lateinit var btnExit: ImageButton

    private val targetUrl = "https://elearningsmaparamartha.vercel.app/"
    private val tokenAccess = "123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Hide system UI for immersive mode
        hideSystemUI()

        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        tvBattery = findViewById(R.id.tvBattery)
        ivSignal = findViewById(R.id.ivSignal)
        btnExit = findViewById(R.id.btnExit)

        setupWebView()
        
        btnExit.setOnClickListener {
            showTokenDialog(isExit = true)
        }

        // Show enter token dialog on startup
        showTokenDialog(isExit = false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.loadsImagesAutomatically = true
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }
        webView.webChromeClient = WebChromeClient()
    }

    private fun showTokenDialog(isExit: Boolean) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_token)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val tvTitle = dialog.findViewById<TextView>(R.id.tvDialogTitle)
        val etToken = dialog.findViewById<EditText>(R.id.etToken)
        val btnSubmit = dialog.findViewById<Button>(R.id.btnSubmitToken)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancelToken)

        if (isExit) {
            tvTitle.setText(R.string.token_keluar)
            btnSubmit.setText(R.string.keluar)
            btnCancel.visibility = View.VISIBLE
        } else {
            tvTitle.setText(R.string.token_masuk)
            btnSubmit.setText(R.string.lanjut)
            btnCancel.visibility = View.GONE
        }

        btnSubmit.setOnClickListener {
            val token = etToken.text.toString().trim()
            if (token == tokenAccess) {
                dialog.dismiss()
                if (isExit) {
                    exitApp()
                } else {
                    startExamMode()
                }
            } else {
                Toast.makeText(this, R.string.token_salah, Toast.showLength_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun startExamMode() {
        // Start Kiosk Mode
        try {
            startLockTask()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        webView.loadUrl(targetUrl)
        registerBatteryReceiver()
        registerSignalListener()
    }

    private fun exitApp() {
        try {
            stopLockTask()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        finishAffinity()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Prevent going back to exit app, but allow webview to go back if possible
            if (webView.canGoBack()) {
                webView.goBack()
            }
            return true // Consume event
        }
        return super.onKeyDown(keyCode, event)
    }

    // --- Battery & Signal Monitoring ---

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level != -1 && scale != -1) {
                val batteryPct = level * 100 / scale
                tvBattery.text = "$batteryPct%"
            }
        }
    }

    private fun registerBatteryReceiver() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)
    }

    private fun registerSignalListener() {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(object : PhoneStateListener() {
            override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
                super.onSignalStrengthsChanged(signalStrength)
                // Simplified signal mapping since API 23+, we can use level 0-4
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    val level = signalStrength?.level ?: 0
                    // In a real app we might switch drawables, but here we just update it if needed.
                    // For simplicity, we just keep the vector icon we have as requested (SVG).
                }
            }
        }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            // Ignored
        }
    }
}
