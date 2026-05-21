// OMNIBUS Docker Android — Frontend Controller

const $ = id => document.getElementById(id);

function updateStatus() {
  try {
    const status = JSON.parse(OMNIBUS.getStatus());
    const termuxBadge = $('termux-badge');
    const vmBadge = $('vm-badge');

    if (status.termuxInstalled) {
      termuxBadge.textContent = '✅ Termux';
      termuxBadge.className = 'badge ok';
    } else {
      termuxBadge.textContent = '❌ Termux';
      termuxBadge.className = 'badge err';
    }

    if (status.vmRunning) {
      vmBadge.textContent = '✅ VM QEMU';
      vmBadge.className = 'badge ok';
    } else {
      vmBadge.textContent = '⏳ VM QEMU';
      vmBadge.className = 'badge';
    }
  } catch (e) {
    console.error('Status error:', e);
  }
}

function loadSetupCommands() {
  try {
    const data = JSON.parse(OMNIBUS.getSetupCommands());
    $('setup-commands').textContent = data.commands.join('\n');
  } catch (e) {
    $('setup-commands').textContent = 'Erro ao carregar comandos. Reinicie o app.';
  }
}

function copySetup() {
  const text = $('setup-commands').textContent;
  OMNIBUS.copyToClipboard(text);
}

function openTermux() {
  OMNIBUS.openTermux();
}

function saveConfig() {
  const ram = parseInt($('ram-input').value, 10);
  const disk = parseInt($('disk-input').value, 10);
  OMNIBUS.setVmConfig(ram, disk);
}

function showQemuCmd() {
  try {
    const cmd = OMNIBUS.getQemuCommand();
    $('qemu-command').textContent = cmd;
    $('qemu-cmd-card').classList.remove('hidden');
  } catch (e) {
    OMNIBUS.showToast('Erro ao gerar comando QEMU');
  }
}

function copyQemuCmd() {
  const text = $('qemu-command').textContent;
  OMNIBUS.copyToClipboard(text);
}

function loadDashboard() {
  try {
    const status = JSON.parse(OMNIBUS.getStatus());
    if (status.apiUrl) {
      window.location.href = status.apiUrl;
    } else {
      OMNIBUS.showToast('API URL não disponível');
    }
  } catch (e) {
    OMNIBUS.showToast('Erro ao carregar dashboard');
  }
}

function loadTerminal() {
  window.location.href = 'terminal.html';
}


// VM Offline Detection — polling a cada 3s
function checkVmHealth() {
  fetch('http://localhost:7749/api/health', { method: 'GET', mode: 'no-cors' })
    .then(() => {
      const badge = document.getElementById('vm-badge');
      if (badge) { badge.textContent = '✅ VM QEMU'; badge.className = 'badge ok'; }
    })
    .catch(() => {
      const badge = document.getElementById('vm-badge');
      if (badge) { badge.textContent = '❌ VM QEMU'; badge.className = 'badge err'; }
    });
}
setInterval(checkVmHealth, 3000);

// Init
document.addEventListener('DOMContentLoaded', () => {
  updateStatus();
  loadSetupCommands();
  setInterval(updateStatus, 5000);
});
