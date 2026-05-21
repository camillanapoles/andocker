#!/bin/sh
# ============================================================
# OMNIBUS Docker Android — Inicia servidor API na VM
# EXECUTAR DENTRO DA VM
# ============================================================
set -e

cd /opt/omnibus-docker

echo "=== [OMNIBUS] Iniciando servidor Flask ==="

# Instala dependências Python se necessário
pip3 install --break-system-packages flask docker 2>/dev/null || pip3 install flask docker

# Copia servidor se não existir
if [ ! -f "omnibus_docker_server.py" ]; then
  echo "ERRO: omnibus_docker_server.py não encontrado em /opt/omnibus-docker"
  echo "Copie do host via scp:"
  echo "  scp -P 2222 vm-server/omnibus_docker_server.py root@localhost:/opt/omnibus-docker/"
  exit 1
fi

# Inicia servidor em background nohup
nohup python3 omnibus_docker_server.py > /var/log/omnibus-server.log 2>&1 &

echo "Servidor iniciado em http://localhost:7749"
echo "Logs: tail -f /var/log/omnibus-server.log"
