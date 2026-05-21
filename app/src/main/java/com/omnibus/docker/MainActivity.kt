package com.omnibus.docker

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackInvokedDispatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * OMNIBUS Docker Android — MainActivity
 * Orquestra WebView + Bridge Termux + Gerenciamento VM
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var termuxBridge: TermuxBridge
    private lateinit var qemuManager: QemuManager



    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Edge-to-edge insets (Android 16 requirement)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Predictive back callback (Android 16)
        if (Build.VERSION.SDK_INT >= 36) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        }

        termuxBridge = TermuxBridge(this)
        qemuManager = QemuManager(this)

        setupWebView()
        checkPrerequisites()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView = findViewById(R.id.webview)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
            userAgentString = "OMNIBUS-Docker-Android/1.0"
        }

        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }

            override fun onReceivedError(
                view: WebView?, 
                request: WebResourceRequest?, 
                error: android.webkit.WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.url?.toString()?.contains("localhost") == true) {
                    // VM não responde — mostra fallback
                    view?.loadUrl("file:///android_asset/web/index.html?vm_offline=1")
                }
            }
        }

        // Bridge JS ↔ Kotlin
        webView.addJavascriptInterface(WebAppInterface(this, termuxBridge, qemuManager), "OMNIBUS")

        // Carrega UI embutida
        webView.loadUrl("file:///android_asset/web/index.html")
    }

    private fun checkPrerequisites() {
        // Android 13+ (API 33): runtime permission para notificações
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                REQ_POST_NOTIFICATIONS
            )
        }

        lifecycleScope.launch {
            val termuxInstalled = withContext(Dispatchers.IO) {
                termuxBridge.isTermuxInstalled()
            }
            if (!termuxInstalled) {
                showTermuxInstallDialog()
            } else {
                extractScriptsToStorage()
            }
        }
    }

    companion object {
        const val VM_API_PORT = 7749
        const val VM_SSH_PORT = 2222
        const val REQ_MANAGE_STORAGE = 1001
        const val REQ_POST_NOTIFICATIONS = 1002
    }

    private fun showTermuxInstallDialog() {
        AlertDialog.Builder(this)
            .setTitle("Termux Não Detectado")
            .setMessage("O Termux é obrigatório para rodar Docker no Android.\n\nInstale via F-Droid (recomendado) ou GitHub.")
            .setPositiveButton("Instalar via F-Droid") { _, _ ->
                openUrl("https://f-droid.org/packages/com.termux/")
            }
            .setNegativeButton("Instalar via GitHub") { _, _ ->
                openUrl("https://github.com/termux/termux-app/releases")
            }
            .setNeutralButton("Já Instalei") { _, _ ->
                Toast.makeText(this, "Reinicie o app após instalar Termux", Toast.LENGTH_LONG).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    /**
     * Extrai scripts do assets/ para storage compartilhada
     * O usuário pode então copiar do /sdcard para o Termux
     */
    private fun extractScriptsToStorage() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val scripts = assets.list("termux-scripts") ?: emptyArray()
                val outDir = File("/sdcard/Download/omnibus-docker-scripts")
                outDir.mkdirs()

                scripts.forEach { script ->
                    assets.open("termux-scripts/$script").use { input ->
                        FileOutputStream(File(outDir, script)).use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Scripts extraídos para: ${outDir.absolutePath}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Erro ao extrair: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
    }
}
