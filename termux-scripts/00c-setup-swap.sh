#!/data/data/com.termux/files/usr/bin/bash
# ============================================================
# OMNIBUS Docker Android — Swapfile para devices com pouca RAM
# ============================================================
set -e

SWAP_SIZE_MB="${1:-2048}"
SWAP_FILE="$HOME/swapfile"

echo "=== [OMNIBUS] Configurando swapfile (${SWAP_SIZE_MB}MB) ==="

if [ -f "$SWAP_FILE" ]; then
  echo "Swapfile já existe. Desativando antigo..."
  swapoff "$SWAP_FILE" 2>/dev/null || true
  rm "$SWAP_FILE"
fi

echo "Criando swapfile..."
dd if=/dev/zero of="$SWAP_FILE" bs=1M count="$SWAP_SIZE_MB" status=progress
chmod 600 "$SWAP_FILE"
mkswap "$SWAP_FILE"

echo "Ativando swap..."
if swapon "$SWAP_FILE" 2>/dev/null; then
  echo "✅ Swap ativado: $(free -m | grep Swap)"
else
  echo "⚠️  Kernel não suporta swapon para arquivo regular."
  echo "    Isso é normal em alguns devices Android."
  echo "    Considere reduzir a RAM da VM (ex: 1024MB)."
  rm "$SWAP_FILE"
  exit 0
fi

echo "=== [OMNIBUS] Swap configurado ==="
