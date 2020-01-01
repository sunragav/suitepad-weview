package com.sunragav.suitepad.webview.utils

import android.app.Activity
import android.content.pm.PackageManager.NameNotFoundException
import com.sunragav.suitepad.webview.BuildConfig.FILEPROVIDER_APPLICATION_ID
import com.sunragav.suitepad.webview.BuildConfig.PROXY_SERVICE_APPLICATION_ID


fun Activity.isPackageInstalled(packageName: String): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: NameNotFoundException) {
        false
    }
}


fun Activity.areRequiredPackagesPresent(): Boolean {
    return isFileProviderThere() && isProxyWebserverThere()
}


fun Activity.isFileProviderThere(): Boolean {
    return isPackageInstalled(FILEPROVIDER_APPLICATION_ID)
}

fun Activity.isProxyWebserverThere(): Boolean {
    return isPackageInstalled(PROXY_SERVICE_APPLICATION_ID)
}



