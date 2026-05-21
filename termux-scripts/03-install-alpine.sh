#!/data/data/com.termux/files/usr/bin/bash
# ============================================================
# OMNIBUS Docker Android — Instala Alpine no disco QCOW2
# Boot via ISO, executa setup-alpine com answerfile
# ============================================================
set -e

ALPINE_VERSION="3.21.3"
ISO="~/omnibus-docker/alpine-virt-${ALPINE_VERSION}-aarch64.iso"
DISK="~/omnibus-docker/vm-disks/alpine-docker.qcow2"
EFI="~/omnibus-docker/qemu-efi/QEMU_EFI.fd"

echo "=== [OMNIBUS] Instalando Alpine Linux na VM ==="
echo "AVISO: Isso abrirá QEMU em modo nográfico."
echo "Siga as instruções na tela ou use o answerfile."
echo ""
echo "Comandos dentro da VM (após boot):"
echo "  login: root (sem senha inicial)"
echo "  # setup-alpine -f /media/answerfile"
echo "  # poweroff"
echo ""
echo "Iniciando VM de instalação em 3 segundos..."
sleep 3

qemu-system-aarch64   -machine virt   -cpu cortex-a57   -m 1024   -bios "$EFI"   -drive file="$DISK",format=qcow2,if=virtio   -cdrom "$ISO"   -boot d   -nographic   -nic user,hostfwd=tcp::2222-:22

echo ""
echo "=== [OMNIBUS] Instalação concluída ==="
echo "Próximo passo: execute 04-start-vm.sh"
