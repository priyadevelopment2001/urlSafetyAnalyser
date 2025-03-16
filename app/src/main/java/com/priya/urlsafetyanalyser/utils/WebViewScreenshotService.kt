package com.priya.urlsafetyanalyser.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout

object WebViewScreenshotService {

    fun loadUrlInBackground(
        context: Context,
        url: String,
        onScreenshotCaptured: (Bitmap?) -> Unit
    ) {
        val webView = WebView(context).apply {
            visibility = View.INVISIBLE
            settings.javaScriptEnabled = true
            settings.loadsImagesAutomatically = true
            settings.domStorageEnabled = true
            settings.blockNetworkImage = false
            settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE

            webViewClient = object : WebViewClient() {
                private var isLoading = false

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    isLoading = true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    if (isLoading) {
                        isLoading = false

                        view?.postDelayed({
                            val screenshot = takeScreenshot(view)
                            onScreenshotCaptured(screenshot)


                            destroyWebView(this@apply)
                        }, 1500)
                    }
                }
            }
        }

        val layout = (context as Activity).findViewById<FrameLayout>(android.R.id.content)
        layout.addView(webView)

        webView.loadUrl(url)
    }

    private fun destroyWebView(webView: WebView) {
        val parent = webView.parent as? ViewGroup
        parent?.removeView(webView)
        webView.stopLoading()
        webView.clearHistory()
        webView.clearCache(true)
        webView.loadUrl("about:blank")
        webView.onPause()
        webView.removeAllViews()
        webView.destroy()
    }

    private fun takeScreenshot(webView: WebView?): Bitmap? {
        return try {
            val bitmap = Bitmap.createBitmap(
                webView!!.width,
                webView.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            webView.draw(canvas)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}