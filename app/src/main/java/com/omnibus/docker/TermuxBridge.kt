package com.omnibus.docker

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

/**
 * Bridge para comunicação com Termux
 * Android 10+ limita execução direta de binários de outros apps,
 * então usamos intents e shared storage.
 */
class TermuxBridge(private val context: Context) {

    companion object {
        const val TERMUX_PACKAGE = "com.termux"
        const val TERMUX_API_PACKAGE = "com.termux.api"
    }

    fun isTermuxInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo(TERMUX_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isTermuxApiInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo(TERMUX_API_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Envia intent para Termux executar um comando.
     * Requer que o script esteja em local acessível ao Termux.
     */
    fun runInTermux(command: String) {
        val intent = Intent("com.termux.RUN_COMMAND").apply {
            setPackage(TERMUX_PACKAGE)
            putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/bash")
            putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("-c", command))
            putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home")
            putExtra("com.termux.RUN_COMMAND_BACKGROUND", true)
        }
        try {
            context.startService(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao enviar comando ao Termux: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Abre o app Termux diretamente (para usuário copiar/colar comandos)
     */
    fun openTermux() {
        val intent = context.packageManager.getLaunchIntentForPackage(TERMUX_PACKAGE)
        if (intent != null) {
            context.startActivity(intent)
        }
    }

    /**
     * Retorna comandos prontos para o usuário executar no Termux
     */
    fun getSetupCommands(): List<String> {
        return listOf(
            "cd ~/storage/downloads/omnibus-docker-scripts",
            "bash 00-setup-termux.sh",
            "bash 01-download-alpine.sh",
            "bash 02-create-vm-disk.sh",
            "bash 03-install-alpine.sh",
            "bash 04-start-vm.sh"
        )
    }

    /**
     * Retorna comandos para acessar a VM via SSH
     */
    fun getVmAccessCommands(): List<String> {
        return listOf(
            "ssh -p 2222 root@localhost",
            "# Senha padrão: root (altere após primeiro login)"
        )
    }
}
