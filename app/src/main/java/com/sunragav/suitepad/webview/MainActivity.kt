package com.sunragav.suitepad.webview

import android.annotation.SuppressLint
import android.content.*
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sunragav.suitepad.webview.BuildConfig.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {


    private lateinit var receiver: BroadcastReceiver

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
        setContentView(R.layout.activity_main)
        webView.settings.apply {
            javaScriptEnabled = true
        }
        receiver = MyReceiver()
        val filter = IntentFilter()
        filter.addAction(ACTION_BROADCAST_PROXY_STARTED)
        registerReceiver(receiver, filter)
        startProxyService(moveProxyServiceToForeGround)
    }


    private fun startProxyService(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }


    private fun loadWebView(port: Int) {
        webView.loadUrl("http://localhost:$port/sample.html")
    }

    override fun finish() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.finishAndRemoveTask()
        }
        else {
            super.finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        stopService(moveProxyServiceToForeGround)
    }


    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            println("Proxy Server started broadcast has been received")
            when (intent.action) {
                ACTION_BROADCAST_PROXY_STARTED -> {
                    loadWebView(intent.getIntExtra("port",0))
                }
            }

        }
    }
}
