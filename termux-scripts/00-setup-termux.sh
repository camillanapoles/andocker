#!/data/data/com.termux/files/usr/bin/bash
# ============================================================
# OMNIBUS Docker Android — Setup Termux
# Instala dependências necessárias no Termux
# ============================================================
set -e

echo "=== [OMNIBUS] Setup Termux ==="
echo "Atualizando pacotes..."
pkg update -y && pkg upgrade -y

echo "Instalando dependências..."
pkg install -y   qemu-system-aarch64   qemu-utils   qemu-common   openssh   wget   curl   tar   unzip   tsu   termux-api

echo "Configurando storage compartilhado..."
termux-setup-storage || true

echo "Criando diretórios de trabalho..."
mkdir -p ~/omnibus-docker/{qemu-efi,vm-disks,scripts}

echo "=== [OMNIBUS] Setup Termux concluído ==="
echo "Próximo passo: execute 01-download-alpine.sh"
