package com.sunragav.suitepad.webview

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

class HostNameVerifier :HostnameVerifier {
    override fun verify(hostname: String?, session: SSLSession?): Boolean {
        return hostname?.equals("localhost")==true
    }
}