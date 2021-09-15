package com.kunalapps.vaccinePortal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.VISIBLE
import android.webkit.*
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_web_view.*

class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        setSupportActionBar(toolbar)
        supportActionBar?.title = URL_TO_BE_LOADED
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        loaderAnim.visibility=VISIBLE
        setUpWebView()

    }

    //endregion
    //region Private Helper methods
    private fun setUpWebView() {
        webView.settings.javaScriptEnabled = true
        webView.settings.setAppCacheEnabled(false)
        webView.clearCache(true)
        loadUrl()
        setupWebViewClient()
    }

    private fun setupWebViewClient() {
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress == 100) {
                    supportActionBar?.title = view.title
                    loaderAnim.visibility = View.GONE
                    webView.visibility = VISIBLE
                } else {
                    supportActionBar?.title = view.url
                    loaderAnim.visibility = VISIBLE
                    webView.visibility = View.GONE
                }
                super.onProgressChanged(view, progress)
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: String): Boolean {
                view.loadUrl(request)
                return true
            }

            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                Snackbar.make(view, "Connection Error", Snackbar.LENGTH_INDEFINITE)
                    .setBackgroundTint(ContextCompat.getColor(this@WebViewActivity,R.color.purple_200))
                    .setTextColor(ContextCompat.getColor(this@WebViewActivity,R.color.purple_500))
                    .setActionTextColor(ContextCompat.getColor(this@WebViewActivity,R.color.purple_500))
                    .setAction("Retry") { loadUrl() }.show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }

        }
    }

    private fun loadUrl() {
        webView.loadUrl(URL_TO_BE_LOADED)
        }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
            overridePendingTransition(R.anim.slide_from_left, R.anim.slideout_from_right);
        }
    }

    companion object {
        const val URL_TO_BE_LOADED="https://selfregistration.cowin.gov.in/"
    }
}