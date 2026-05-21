#!/data/data/com.termux/files/usr/bin/bash
# ============================================================
# OMNIBUS Docker Android — Inicia VM Alpine com Docker
# Port forwards: 2222→22, 7749→7749, 8080→80, 8443→443
# ============================================================

DISK="~/omnibus-docker/vm-disks/alpine-docker.qcow2"
EFI="~/omnibus-docker/qemu-efi/QEMU_EFI.fd"
RAM_MB="${1:-2048}"

echo "=== [OMNIBUS] Iniciando VM QEMU ==="
echo "RAM: ${RAM_MB}MB | Disco: $DISK"
echo ""
echo "Portas forwardadas:"
echo "  2222  → 22   (SSH)"
echo "  7749  → 7749 (OMNIBUS API)"
echo "  8080  → 80   (HTTP Proxy)"
echo "  8443  → 443  (HTTPS Proxy)"
echo ""
echo "Para desligar a VM: Ctrl+A depois X"
echo ""

qemu-system-aarch64   -machine virt   -cpu cortex-a57   -m "${RAM_MB}"   -bios "$EFI"   -drive file="$DISK",format=qcow2,if=virtio   -nographic   -nic user,hostfwd=tcp::2222-:22,hostfwd=tcp::7749-:7749,hostfwd=tcp::8080-:80,hostfwd=tcp::8443-:443

echo "=== [OMNIBUS] VM encerrada ==="
