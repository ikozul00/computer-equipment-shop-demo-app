package com.example.computerequipment

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private var appUrl = "https://computer-equipment-shop-demo.vercel.app"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webView)
        setupWebView()
        webView.loadUrl(appUrl)
    }

    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?,
            ): Boolean {

                if (request?.url != null && request.url.toString().startsWith("computershop://callback"))
                {
                    handleDeepLink(request.url)
                } else {
                    view?.loadUrl(request?.url.toString())
                }
                return true
            }
        }

        webView.addJavascriptInterface(WebAppBridge(), "Android")
    }

    inner class WebAppBridge {
        @JavascriptInterface
        fun openGoogleLogin() {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("$appUrl/api/auth/signin/google?callbackUrl=/app-callback");
            startActivity(intent)
        }

        @JavascriptInterface
        fun isOnline(): Boolean {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false;
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
       handleDeepLink(intent?.data)
    }

    private fun handleDeepLink(uri: Uri?) {
        if (uri != null && uri.scheme == "computershop" && uri.host == "callback") {
            val token = uri.getQueryParameter("token");
            if (token != null) {
                webView.loadUrl("$appUrl/api/auth/token-login?token=$token")
            }
        }
    }
}