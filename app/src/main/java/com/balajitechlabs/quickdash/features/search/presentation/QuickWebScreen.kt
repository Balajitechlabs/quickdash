package com.balajitechlabs.quickdash.features.search.presentation

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun QuickWebScreen(
    initialUrl: String = "https://www.google.com",
    onClose: () -> Unit
) {
    var webView: WebView? by remember { mutableStateOf(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var currentUrl by remember { mutableStateOf(initialUrl) }
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0f) }

    BackHandler(enabled = canGoBack) {
        webView?.goBack()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { webView?.goBack() },
                enabled = canGoBack
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            
            IconButton(
                onClick = { webView?.goForward() },
                enabled = canGoForward
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Forward")
            }

            IconButton(
                onClick = { webView?.reload() }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }

            Spacer(modifier = Modifier.weight(1f))
            
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close Web")
            }
        }

        if (isLoading) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // WebView
        AndroidView(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                            url?.let { currentUrl = it }
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                            view?.let {
                                canGoBack = it.canGoBack()
                                canGoForward = it.canGoForward()
                            }
                        }
                    }
                    
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            progress = newProgress / 100f
                        }
                    }
                    
                    loadUrl(initialUrl)
                }
            },
            update = { view ->
                webView = view
            }
        )
    }
}
