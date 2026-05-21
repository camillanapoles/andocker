#!/bin/sh
# ============================================================
# OMNIBUS Docker Android — Instala Docker na VM Alpine
# EXECUTAR DENTRO DA VM (ssh -p 2222 root@localhost)
# ============================================================
set -e

echo "=== [OMNIBUS] Instalando Docker na VM Alpine ==="

# Habilita repositório community
sed -i 's/^#\(.*\/community\)/\1/' /etc/apk/repositories
apk update

# Instala Docker + Compose plugin + Python + Nginx
apk add   docker   docker-cli-compose   openrc   python3   py3-pip   nginx   curl   git

# Inicia Docker no boot
rc-update add docker default
service docker start

# Inicia Nginx
rc-update add nginx default
service nginx start

# Cria diretório para OMNIBUS server
mkdir -p /opt/omnibus-docker


# Instala lazydocker (TUI para Docker)
echo "Instalando lazydocker..."
LAZYDOCKER_VERSION=$(curl -s https://api.github.com/repos/jesseduffield/lazydocker/releases/latest | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/')
if [ -n "$LAZYDOCKER_VERSION" ]; then
  wget -q "https://github.com/jesseduffield/lazydocker/releases/download/${LAZYDOCKER_VERSION}/lazydocker_${LAZYDOCKER_VERSION#v}_Linux_arm64.tar.gz" -O /tmp/lazydocker.tar.gz
  tar xzf /tmp/lazydocker.tar.gz -C /tmp
  mv /tmp/lazydocker /usr/local/bin/
  chmod +x /usr/local/bin/lazydocker
  echo "lazydocker instalado. Execute: lazydocker"
else
  echo "Não foi possível detectar versão do lazydocker. Pulando."
fi

echo "=== [OMNIBUS] Docker instalado ==="
echo "Verifique: docker run --rm hello-world"
echo ""
echo "Próximo passo: execute 06-start-flask-server.sh"
