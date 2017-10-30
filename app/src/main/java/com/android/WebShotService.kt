package com.android

import android.annotation.SuppressLint
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.RemoteViews
import com.android.simplewidget.R

class WebShotService : Service() {
    private var webView: WebView? = null
    private var winManager: WindowManager? = null
    internal var mHandler = Handler()

    private val client = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            val p = Point()
            winManager!!.defaultDisplay.getSize(p)
            webView!!.measure(View.MeasureSpec.makeMeasureSpec(if (p.x < p.y) p.y else p.x,
                    View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(if (p.x < p.y) p.x else p.y, View.MeasureSpec.EXACTLY))

            webView!!.layout(0, 0, webView!!.measuredWidth, webView!!.measuredHeight)
            webView!!.postDelayed(capture, 1000)
        }
    }

    private val capture = object : Runnable {
        override fun run() {
            try {
                val bmp = Bitmap.createBitmap(webView!!.width,
                        webView!!.height, Bitmap.Config.ARGB_8888)
                val c = Canvas(bmp)
                webView!!.draw(c)

                updateWidgets(bmp)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
            Log.d("Widget", "run()")
            mHandler.removeCallbacksAndMessages(null);
            mHandler.postDelayed(this, INTERVAL.toLong())
            //stopSelf()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        winManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        webView = WebView(this)
        val webSettings:WebSettings = webView!!.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = false
        webSettings.builtInZoomControls = false
        webSettings.displayZoomControls = false
        webSettings.setSupportZoom(false)

        webView!!.isVerticalScrollBarEnabled = false
        webView!!.webViewClient = client

        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT)
        params.x = 0
        params.y = 0
        params.width = 0
        params.height = 0

        val frame = FrameLayout(this)
        frame.addView(webView)
        winManager!!.addView(frame, params)

        val url = "https://trends.google.com/trends/hottrends/visualize?nrow=2&ncol=3&pn=p9"
        webView!!.loadUrl(url)
        return Service.START_STICKY
    }

    private fun updateWidgets(bmp: Bitmap) {
        val widgetManager = AppWidgetManager.getInstance(this)
        val ids = widgetManager.getAppWidgetIds(ComponentName(this, HotSearchesWidgetProvider::class.java))

        if (ids.isEmpty()) {
            return
        }

        val views = RemoteViews(packageName, R.layout.widget_layout)
        views.setImageViewBitmap(R.id.widget_image, bmp)
        widgetManager.updateAppWidget(ids, views)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        private val INTERVAL = 50
    }
}