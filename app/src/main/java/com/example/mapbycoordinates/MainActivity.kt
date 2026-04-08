package com.example.mapbycoordinates

import androidx.webkit.WebViewAssetLoader
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MapByCoordinates()
        }
    }
}

@Composable
fun MapByCoordinates() {
    var latInput by remember { mutableStateOf("52.2297") }
    var lngInput by remember { mutableStateOf("21.0122") }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    val focusManager = LocalFocusManager.current

    val latValue = latInput.toDoubleOrNull()
    val lngValue = lngInput.toDoubleOrNull()
    
    val isLatValid = latInput.isEmpty() || latInput == "-" || (latValue != null && latValue in -90.0..90.0)
    val isLngValid = lngInput.isEmpty() || lngInput == "-" || (lngValue != null && lngValue in -180.0..180.0)

    Box(modifier = Modifier.fillMaxSize()
        .systemBarsPadding()) {
        // --- Map Area (Full Screen Background) ---
        AndroidView(
            factory = { context ->
                val assetLoader = WebViewAssetLoader.Builder()
                    .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
                    .build()

                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true

                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView,
                            request: WebResourceRequest
                        ): WebResourceResponse? {
                            return assetLoader.shouldInterceptRequest(request.url)
                        }
                    }
                    setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    loadUrl("https://appassets.androidplatform.net/assets/map.html")
                    webViewInstance = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // --- UI Layers Overlay ---
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                color = Color(0x99061128)
            ) {
                Text(
                    text = "Map by Coordinates",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x99061128)) // Semi-transparent
                        .padding(16.dp)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                SmallFloatingActionButton(
                    onClick = {
                        if (latValue != null && lngValue != null) {
                            webViewInstance?.evaluateJavascript("updateMap($latValue, $lngValue)", null)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 32.dp, end = 16.dp),
                    containerColor = Color.White,
                    contentColor = Color(0xFF061128)
                ) {
                    Text("◎", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            // --- Bottom Control Panel ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Current Location", color = Color.Gray, fontSize = 14.sp)
                    Text(
                        "${latInput}°N, ${lngInput}°E",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = latInput,
                            onValueChange = { input ->
                                if (input.isEmpty() || input == "-" || input.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                                    latInput = input
                                }
                            },
                            label = { Text("Latitude") },
                            isError = !isLatValid,
                            supportingText = { if (!isLatValid) Text("Range: -90 to 90", color = MaterialTheme.colorScheme.error) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = lngInput,
                            onValueChange = { input ->
                                if (input.isEmpty() || input == "-" || input.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                                    lngInput = input
                                }
                            },
                            label = { Text("Longitude") },
                            isError = !isLngValid,
                            supportingText = { if (!isLngValid) Text("Range: -180 to 180", color = MaterialTheme.colorScheme.error) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (isLatValid && isLngValid && latValue != null && lngValue != null) {
                                webViewInstance?.evaluateJavascript("updateMap($latValue, $lngValue)", null)
                                focusManager.clearFocus() // Closes keyboard
                            }
                        },
                        enabled = isLatValid && isLngValid && latInput.isNotEmpty() && lngInput.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF061128))
                    ) {
                        Text("Update Location", color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
