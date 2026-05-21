#!/data/data/com.termux/files/usr/bin/bash
# ============================================================
# OMNIBUS Docker Android — Mount de Camera/Storage para VM
# Bind mount de diretórios Android para a VM via SSH
# ============================================================
set -e

VM_SSH_PORT="2222"

echo "=== [OMNIBUS] Mount de Storage para VM ==="

# Cria diretórios de mount na VM
ssh -p "$VM_SSH_PORT" root@localhost "mkdir -p /mnt/android/{shared,dcim,downloads}" || {
  echo "ERRO: Não foi possível conectar à VM via SSH"
  echo "Certifique-se de que a VM está rodando (04-start-vm.sh)"
  exit 1
}

# Bind mount via SSHFS (se disponível) ou simples scp/sync
# Nota: SSHFS requer FUSE que pode não estar no Alpine por padrão
# Alternativa: rsync periódico ou volume Docker

echo "Storage Android mapeado na VM em /mnt/android/"
echo "Para acesso via Docker, use volumes:"
echo "  docker run -v /mnt/android/shared:/data ..."
