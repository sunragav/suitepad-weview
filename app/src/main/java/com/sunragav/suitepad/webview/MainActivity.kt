package com.sunragav.suitepad.webview

import android.annotation.SuppressLint
import android.content.*
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    companion object {
        private const val APPLICATION_ID = "com.sunragav.suitepad.proxyserver"
        private const val CLASSNAME = "com.sunragav.suitepad.proxyserver.ProxyWebServer"
        private const val BROADCAST_ACTION_FOREGROUND =
            "com.sunragav.suitepad.proxy.ProxyServerForegroundBroadCast"
        private const val BROADCAST_ACTION_PROXY_STARTED =
            "com.sunragav.suitepad.proxy.ProxyServerStarted"
        private const val MOVE_TO_FOREGROUND_ACTION = "com.sunragav.suitepad.proxy.MoveToForeground"
    }

    lateinit var receiver: BroadcastReceiver

    private val moveToForeGround = Intent().apply {
        action = MOVE_TO_FOREGROUND_ACTION
        component = ComponentName(
            APPLICATION_ID,
            CLASSNAME
        )
    }


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView.settings.apply {
            allowFileAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            allowContentAccess = true
            javaScriptEnabled = true
        }
        receiver = MyReceiver()
        val filter = IntentFilter()
        filter.addAction(BROADCAST_ACTION_PROXY_STARTED)
        registerReceiver(receiver, filter)
        startProxyService(moveToForeGround)
    }


    private fun startProxyService(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }


    private fun loadWebView() {
        webView.loadUrl("http://localhost:8091/sample.html")
    }



    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        stopService(moveToForeGround)
    }


    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            println("Proxy Server started broadcast has been received")
            when (intent.action) {
                BROADCAST_ACTION_PROXY_STARTED -> loadWebView()
            }

        }
    }
}
