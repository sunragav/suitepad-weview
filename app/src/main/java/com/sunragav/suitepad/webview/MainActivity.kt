package com.sunragav.suitepad.webview

import android.annotation.SuppressLint
import android.content.*
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sunragav.suitepad.webview.BuildConfig.*
import com.sunragav.suitepad.webview.utils.OkHttpWebClient
import com.sunragav.suitepad.webview.utils.areRequiredPackagesPresent
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.package_not_available.*


class MainActivity : AppCompatActivity() {


    private lateinit var receiver: BroadcastReceiver

    private var startedService = false

    private val moveProxyServiceToForeGround = Intent().apply {
        action = ACTION_MOVE_TO_FOREGROUND
        component = ComponentName(
            PROXY_SERVICE_APPLICATION_ID,
            PROXY_SERVICE_CLASSNAME
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (areRequiredPackagesPresent()) {
            setContentView(R.layout.activity_main)
            webView.settings.apply {
                javaScriptEnabled = true
            }
            receiver = ServerStartedActionReceiver()
            val filter = IntentFilter()
            filter.addAction(ACTION_BROADCAST_PROXY_STARTED)
            registerReceiver(receiver, filter)
            startProxyService(moveProxyServiceToForeGround)
            startedService = true
        } else {
            setContentView(R.layout.package_not_available)
        }
    }


    private fun startProxyService(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun finish() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.finishAndRemoveTask()
        } else {
            super.finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (startedService) {
            unregisterReceiver(receiver)
            stopService(moveProxyServiceToForeGround)
        }
    }


    inner class ServerStartedActionReceiver : BroadcastReceiver() {
        private val SAMPLE_HTML = "sample.html"

        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            println("Proxy Server started broadcast has been received")
            when (intent.action) {
                ACTION_BROADCAST_PROXY_STARTED -> {
                    webView.webViewClient = OkHttpWebClient(intent.getIntExtra("port", 0))
                    webView.loadUrl("$BASE_URL$SAMPLE_HTML")
                }
            }

        }
    }
}
