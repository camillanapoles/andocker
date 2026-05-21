#!/data/data/com.termux/files/usr/bin/bash
# ============================================================
# OMNIBUS Docker Android — Download Alpine Linux aarch64
# ============================================================
set -e

ALPINE_VERSION="3.21.3"
ALPINE_URL="https://dl-cdn.alpinelinux.org/alpine/v3.21/releases/aarch64/alpine-virt-${ALPINE_VERSION}-aarch64.iso"
EFI_URL="https://releases.linaro.org/components/kernel/uefi-linaro/latest/release/qemu64/QEMU_EFI.fd"

cd ~/omnibus-docker

echo "=== [OMNIBUS] Download Alpine Linux ${ALPINE_VERSION} ==="

if [ ! -f "alpine-virt-${ALPINE_VERSION}-aarch64.iso" ]; then
  echo "Baixando ISO Alpine..."
  wget --show-progress "$ALPINE_URL" -O "alpine-virt-${ALPINE_VERSION}-aarch64.iso"
else
  echo "ISO Alpine já existe. Pulando download."
fi

if [ ! -f "qemu-efi/QEMU_EFI.fd" ]; then
  echo "Baixando firmware UEFI..."
  wget --show-progress "$EFI_URL" -O "qemu-efi/QEMU_EFI.fd"
else
  echo "Firmware UEFI já existe. Pulando download."
fi

echo "=== [OMNIBUS] Download concluído ==="
echo "Próximo passo: execute 02-create-vm-disk.sh"
