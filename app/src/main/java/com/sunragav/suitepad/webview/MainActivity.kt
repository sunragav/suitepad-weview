package com.sunragav.suitepad.webview

import android.annotation.SuppressLint
import android.content.*
import android.os.*
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.sunragav.suitepad.webview.BuildConfig.*
import com.sunragav.suitepad.webview.utils.OkHttpWebClient
import com.sunragav.suitepad.webview.utils.areRequiredPackagesPresent
import com.sunragav.suitepad.webview.utils.isFileProviderThere
import com.sunragav.suitepad.webview.utils.isProxyWebserverThere
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.package_not_available.*
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity() {

    private lateinit var receiver: BroadcastReceiver

    private var startedService = false

    private val useBoundService = USE_BOUND_SERVICE == "1"

    private val intentToMoveProxyServiceToForeGround = Intent().apply {
        action = ACTION_MOVE_TO_FOREGROUND
        component = ComponentName(
            PROXY_SERVICE_APPLICATION_ID,
            PROXY_SERVICE_CLASSNAME
        )
    }
    private val intentToBindToProxyServiceToForeGround = Intent().apply {
        component = ComponentName(
            PROXY_SERVICE_APPLICATION_ID,
            PROXY_SERVICE_CLASSNAME
        )
    }

    private var isBound = false
    private val messengerToReceiveMsgFromRemoteService = Messenger(IncomingHandler(WeakReference(this)))
    lateinit var messengerToSendMsgToRemoteService: Messenger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (areRequiredPackagesPresent()) {
            setContentView(R.layout.activity_main)
            if(useBoundService) {
                bindService(intentToBindToProxyServiceToForeGround, serviceConnection, BIND_AUTO_CREATE)
            }else {
                receiver = ServerStartedActionReceiver()
                val filter = IntentFilter()
                filter.addAction(ACTION_BROADCAST_PROXY_STARTED)
                registerReceiver(receiver, filter)
                startProxyService(intentToMoveProxyServiceToForeGround)
                startedService = true
            }
        } else {
            setContentView(R.layout.package_not_available)
            if (isFileProviderThere()) cv_fileProvider.visibility = View.GONE
            else if (isProxyWebserverThere()) cv_proxyService.visibility = View.GONE
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
            stopService(intentToMoveProxyServiceToForeGround)
        }
        else if(useBoundService && isBound){
                unbindService(serviceConnection)
                isBound = false
            stopService(intentToBindToProxyServiceToForeGround)
        }
    }

    inner class ServerStartedActionReceiver : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            println("Proxy Server started broadcast has been received")
            when (intent.action) {
                ACTION_BROADCAST_PROXY_STARTED -> {
                    loadWebView(intent.getIntExtra("port", 0))
                }
            }

        }
    }

    class IncomingHandler(private val activityRef:WeakReference<MainActivity>) : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_HTTP_SERVER_STARTED -> {
                    activityRef.get()?.loadWebView(msg.data.getInt("port",0))
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebView(port: Int) {
        webView.settings.apply {
            javaScriptEnabled = true
        }
        webView.webViewClient = OkHttpWebClient(port)
        webView.loadUrl("$BASE_URL$SAMPLE_HTML")
    }


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isBound = true
            messengerToSendMsgToRemoteService = Messenger(service).also {
                it.send(
                    Message.obtain(null, NOTIFY_ME_WHEN_HTTP_SERVER_STARTS).apply {
                        replyTo = messengerToReceiveMsgFromRemoteService
                    }
                )
            }
        }

    }


    companion object {
        private const val SAMPLE_HTML = "sample.html"
        private const val MSG_HTTP_SERVER_STARTED = 2
        private const val NOTIFY_ME_WHEN_HTTP_SERVER_STARTS = 1
    }
}
