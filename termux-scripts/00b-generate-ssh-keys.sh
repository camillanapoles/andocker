#!/data/data/com.termux/files/usr/bin/bash
# ============================================================
# OMNIBUS Docker Android — Geração de chaves SSH
# Cria par de chaves Ed25519 e copia pública para VM
# ============================================================
set -e

KEY_FILE="$HOME/.ssh/id_ed25519"
VM_SSH_PORT="2222"

echo "=== [OMNIBUS] Gerando chaves SSH ==="

if [ -f "$KEY_FILE" ]; then
  echo "Chave já existe em $KEY_FILE"
else
  mkdir -p ~/.ssh
  ssh-keygen -t ed25519 -C "omnibus@docker-android" -f "$KEY_FILE" -N ""
  echo "Chave gerada: $KEY_FILE"
fi

echo ""
echo "Copiando chave pública para VM (senha root necessária)..."
ssh-copy-id -p "$VM_SSH_PORT" -i "$KEY_FILE.pub" root@localhost || {
  echo "Falha ao copiar chave. Verifique se a VM está rodando (04-start-vm.sh)"
  echo "e se SSH está acessível na porta $VM_SSH_PORT"
  exit 1
}

echo "=== [OMNIBUS] Chave SSH configurada ==="
echo "Agora você pode acessar a VM sem senha:"
echo "  ssh -p 2222 root@localhost"
