#!/data/data/com.termux/files/usr/bin/bash
# ============================================================
# OMNIBUS Docker Android — Cria disco QCOW2 para VM
# ============================================================
set -e

DISK_GB="${1:-8}"
DISK_PATH="~/omnibus-docker/vm-disks/alpine-docker.qcow2"

echo "=== [OMNIBUS] Criando disco virtual (${DISK_GB}GB) ==="

mkdir -p ~/omnibus-docker/vm-disks

if [ -f "$DISK_PATH" ]; then
  echo "AVISO: Disco já existe em $DISK_PATH"
  echo "Remova manualmente se quiser recriar: rm $DISK_PATH"
  exit 0
fi

qemu-img create -f qcow2 "$DISK_PATH" "${DISK_GB}G"

echo "=== [OMNIBUS] Disco criado: $DISK_PATH ==="
echo "Próximo passo: execute 03-install-alpine.sh"
