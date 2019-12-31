package com.sunragav.suitepad.webview.utils

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.sunragav.suitepad.webview.BuildConfig.BASE_URL
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit


class OkHttpWebClient(private val port: Int) : WebViewClient() {
    private val okHttpClient = OkHttpClient
        .Builder()
        .cache(null)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS).build()


    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url.toString()
        if (shouldIntercept(url)) {
            return handleRequestViaOkHttp(url)
        }
        return super.shouldInterceptRequest(view, request)
    }

    private fun shouldIntercept(url: String?): Boolean {
        var result = false
        url?.let {
            result = it.startsWith(BASE_URL)
        }
        return result
    }

    private fun handleRequestViaOkHttp(url: String): WebResourceResponse {
        val call = okHttpClient.newCall(
            Request.Builder()
                .url(url.replace(BASE_URL, "http://localhost:$port/"))
                .build()
        )
        val disposable = CompositeDisposable()
        val res = Single.fromCallable {
            call.execute()
        }.subscribeOn(Schedulers.io())
            .doOnSubscribe { disposable.add(it) }
            .map { return@map it }
            .blockingGet()
        disposable.dispose()

        return WebResourceResponse(
            res.header(
                "content-type",
                "plain/text"
            ),
            res.header(
                "content-encoding",
                "utf-8"
            ),
            res.body?.byteStream()
        )

    }
}