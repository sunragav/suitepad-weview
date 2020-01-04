package com.sunragav.suitepad.webview.app

import android.app.Application
import com.sunragav.suitepad.webview.BuildConfig
import timber.log.Timber
import timber.log.Timber.DebugTree


class SuitePadApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }
}