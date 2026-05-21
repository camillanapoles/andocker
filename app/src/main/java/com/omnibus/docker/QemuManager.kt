package com.omnibus.docker

import android.content.Context
import android.content.SharedPreferences

/**
 * Gerencia estado da VM QEMU
 * Persiste configurações de porta, RAM, disco
 */
class QemuManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREFS_NAME = "omnibus_qemu_prefs"
        const val KEY_VM_RAM = "vm_ram_mb"
        const val KEY_VM_DISK = "vm_disk_gb"
        const val KEY_API_PORT = "vm_api_port"
        const val KEY_SSH_PORT = "vm_ssh_port"
        const val KEY_VM_RUNNING = "vm_running"
        const val DEFAULT_RAM = 2048
        const val DEFAULT_DISK = 8
        const val DEFAULT_API_PORT = 7749
        const val DEFAULT_SSH_PORT = 2222
    }

    var vmRamMb: Int
        get() = prefs.getInt(KEY_VM_RAM, DEFAULT_RAM)
        set(value) = prefs.edit().putInt(KEY_VM_RAM, value).apply()

    var vmDiskGb: Int
        get() = prefs.getInt(KEY_VM_DISK, DEFAULT_DISK)
        set(value) = prefs.edit().putInt(KEY_VM_DISK, value).apply()

    var apiPort: Int
        get() = prefs.getInt(KEY_API_PORT, DEFAULT_API_PORT)
        set(value) = prefs.edit().putInt(KEY_API_PORT, value).apply()

    var sshPort: Int
        get() = prefs.getInt(KEY_SSH_PORT, DEFAULT_SSH_PORT)
        set(value) = prefs.edit().putInt(KEY_SSH_PORT, value).apply()

    var isVmRunning: Boolean
        get() = prefs.getBoolean(KEY_VM_RUNNING, false)
        set(value) = prefs.edit().putBoolean(KEY_VM_RUNNING, value).apply()

    /**
     * Gera comando QEMU completo para iniciar a VM
     */
    fun generateQemuCommand(): String {
        return buildString {
            append("qemu-system-aarch64 \
")
            append("  -machine virt \
")
            append("  -cpu cortex-a57 \
")
            append("  -m ${vmRamMb} \
")
            append("  -bios ~/qemu-efi/QEMU_EFI.fd \
")
            append("  -drive file=~/alpine-docker.qcow2,format=qcow2,if=virtio \
")
            append("  -nographic \
")
            append("  -nic user,")
            append("hostfwd=tcp::${sshPort}-:22,")
            append("hostfwd=tcp::${apiPort}-:7749,")
            append("hostfwd=tcp::8080-:80,")
            append("hostfwd=tcp::8443-:443")
        }
    }

    /**
     * Retorna URL da API REST na VM (via localhost forwarding)
     */
    fun getApiUrl(): String = "http://localhost:${apiPort}"

    /**
     * Retorna URL do terminal web (servido pela VM)
     */
    fun getTerminalUrl(): String = "http://localhost:${apiPort}/terminal"
}
