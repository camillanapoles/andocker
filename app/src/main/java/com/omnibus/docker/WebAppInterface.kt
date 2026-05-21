package com.omnibus.docker

import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import org.json.JSONObject

/**
 * Bridge JavaScript ↔ Kotlin
 * Exposto como objeto `OMNIBUS` no contexto JS do WebView
 */
class WebAppInterface(
    private val activity: MainActivity,
    private val termuxBridge: TermuxBridge,
    private val qemuManager: QemuManager
) {

    @JavascriptInterface
    fun getStatus(): String {
        val json = JSONObject().apply {
            put("termuxInstalled", termuxBridge.isTermuxInstalled())
            put("termuxApiInstalled", termuxBridge.isTermuxApiInstalled())
            put("vmRunning", qemuManager.isVmRunning)
            put("apiPort", qemuManager.apiPort)
            put("sshPort", qemuManager.sshPort)
            put("apiUrl", qemuManager.getApiUrl())
        }
        return json.toString()
    }

    @JavascriptInterface
    fun getSetupCommands(): String {
        val commands = termuxBridge.getSetupCommands()
        return JSONObject().apply {
            put("commands", org.json.JSONArray(commands))
            put("note", "Execute esses comandos no Termux, um por um.")
        }.toString()
    }

    @JavascriptInterface
    fun getQemuCommand(): String {
        return qemuManager.generateQemuCommand()
    }

    @JavascriptInterface
    fun openTermux() {
        activity.runOnUiThread {
            termuxBridge.openTermux()
        }
    }

    @JavascriptInterface
    fun showToast(message: String) {
        activity.runOnUiThread {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun setVmConfig(ramMb: Int, diskGb: Int) {
        qemuManager.vmRamMb = ramMb
        qemuManager.vmDiskGb = diskGb
        activity.runOnUiThread {
            Toast.makeText(activity, "Config: RAM=${ramMb}MB, Disk=${diskGb}GB", Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun getVmConfig(): String {
        return JSONObject().apply {
            put("ramMb", qemuManager.vmRamMb)
            put("diskGb", qemuManager.vmDiskGb)
            put("apiPort", qemuManager.apiPort)
            put("sshPort", qemuManager.sshPort)
        }.toString()
    }

    @JavascriptInterface
    fun copyToClipboard(text: String) {
        val clipboard = activity.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("OMNIBUS", text)
        clipboard.setPrimaryClip(clip)
        activity.runOnUiThread {
            Toast.makeText(activity, "Copiado para clipboard", Toast.LENGTH_SHORT).show()
        }
    }
}
