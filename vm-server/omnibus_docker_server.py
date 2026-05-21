#!/usr/bin/env python3
"""
OMNIBUS Docker Server — API REST + UI Web
Roda dentro da VM Alpine Linux (QEMU) no Android
Expõe: containers, imagens, volumes, networks, compose, proxy reverso
"""

import os
import json
import subprocess
import re
from datetime import datetime
from flask import Flask, jsonify, request, render_template_string

app = Flask(__name__)

# ────────────────────────────────────────────────────────────
# HELPERS
# ────────────────────────────────────────────────────────────

def run_cmd(cmd: list, timeout=30) -> tuple:
    """Executa comando shell e retorna (stdout, stderr, rc)"""
    try:
        result = subprocess.run(
            cmd, capture_output=True, text=True, timeout=timeout,
            env={**os.environ, "DOCKER_CLI_HINTS": "false"}
        )
        return result.stdout, result.stderr, result.returncode
    except Exception as e:
        return "", str(e), 1

def docker_ps():
    stdout, _, rc = run_cmd(["docker", "ps", "--format", "json"])
    if rc != 0:
        return []
    lines = [l for l in stdout.strip().split("\n") if l]
    return [json.loads(l) for l in lines]

def docker_images():
    stdout, _, rc = run_cmd(["docker", "images", "--format", "json"])
    if rc != 0:
        return []
    lines = [l for l in stdout.strip().split("\n") if l]
    return [json.loads(l) for l in lines]

def docker_volumes():
    stdout, _, rc = run_cmd(["docker", "volume", "ls", "--format", "json"])
    if rc != 0:
        return []
    lines = [l for l in stdout.strip().split("\n") if l]
    return [json.loads(l) for l in lines]

def docker_networks():
    stdout, _, rc = run_cmd(["docker", "network", "ls", "--format", "json"])
    if rc != 0:
        return []
    lines = [l for l in stdout.strip().split("\n") if l]
    return [json.loads(l) for l in lines]

def docker_system_df():
    stdout, _, rc = run_cmd(["docker", "system", "df", "--format", "json"])
    if rc != 0:
        return {}
    try:
        return json.loads(stdout)
    except:
        return {}

def docker_compose_ls():
    stdout, _, rc = run_cmd(["docker", "compose", "ls", "--format", "json"])
    if rc != 0:
        return []
    try:
        return json.loads(stdout) if stdout.strip() else []
    except:
        return []

def docker_info():
    stdout, _, rc = run_cmd(["docker", "info", "--format", "json"])
    if rc != 0:
        return {}
    try:
        return json.loads(stdout)
    except:
        return {}

# ────────────────────────────────────────────────────────────
# HTML TEMPLATES (UI Portainer-like embutida)
# ────────────────────────────────────────────────────────────

DASHBOARD_HTML = """
<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>OMNIBUS Docker Dashboard</title>
<style>
:root { --bg:#0d1117; --card:#161b22; --border:#30363d; --text:#c9d1d9; --muted:#8b949e; --accent:#58a6ff; --ok:#3fb950; --warn:#d29922; --err:#f85149; }
* { box-sizing:border-box; margin:0; padding:0; }
body { font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif; background:var(--bg); color:var(--text); padding:16px; }
header { display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; padding-bottom:12px; border-bottom:1px solid var(--border); }
.logo { font-weight:800; font-size:20px; } .logo span { color:var(--accent); font-weight:400; }
.stats { display:grid; grid-template-columns:repeat(auto-fit,minmax(140px,1fr)); gap:12px; margin-bottom:20px; }
.stat-card { background:var(--card); border:1px solid var(--border); border-radius:10px; padding:14px; text-align:center; }
.stat-card .num { font-size:28px; font-weight:700; color:var(--accent); }
.stat-card .label { font-size:12px; color:var(--muted); margin-top:4px; }
table { width:100%; border-collapse:collapse; font-size:13px; }
th, td { padding:10px 8px; text-align:left; border-bottom:1px solid var(--border); }
th { color:var(--muted); font-weight:600; font-size:11px; text-transform:uppercase; letter-spacing:0.5px; }
tr:hover td { background:rgba(88,166,255,0.04); }
.status-dot { display:inline-block; width:8px; height:8px; border-radius:50%; margin-right:6px; }
.status-running { background:var(--ok); }
.status-exited { background:var(--err); }
.status-paused { background:var(--warn); }
.section { background:var(--card); border:1px solid var(--border); border-radius:12px; padding:16px; margin-bottom:16px; }
.section h2 { font-size:16px; margin-bottom:12px; }
.btn { display:inline-block; padding:8px 14px; border-radius:6px; border:none; background:var(--accent); color:#fff; font-size:13px; cursor:pointer; margin:4px 4px 4px 0; }
.btn-warn { background:var(--warn); color:#000; }
.btn-err { background:var(--err); }
pre { background:#010409; border:1px solid var(--border); border-radius:6px; padding:10px; overflow-x:auto; font-size:12px; color:var(--text); }
.tabs { display:flex; gap:8px; margin-bottom:12px; }
.tab { padding:8px 14px; border-radius:6px; background:var(--bg); border:1px solid var(--border); color:var(--muted); cursor:pointer; font-size:13px; }
.tab.active { background:var(--accent); color:#fff; border-color:var(--accent); }
.hidden { display:none; }
footer { text-align:center; color:var(--muted); font-size:12px; margin-top:30px; padding-top:16px; border-top:1px solid var(--border); }
</style>
</head>
<body>
<header>
  <div class="logo">🐳 OMNIBUS <span>DOCKER</span></div>
  <div style="font-size:12px;color:var(--muted)">VM: {{ info.get('Name','alpine') }} | {{ info.get('ServerVersion','?') }}</div>
</header>

<div class="stats">
  <div class="stat-card"><div class="num">{{ stats.containers_running }}</div><div class="label">Containers Running</div></div>
  <div class="stat-card"><div class="num">{{ stats.containers_total }}</div><div class="label">Containers Total</div></div>
  <div class="stat-card"><div class="num">{{ stats.images }}</div><div class="label">Images</div></div>
  <div class="stat-card"><div class="num">{{ stats.volumes }}</div><div class="label">Volumes</div></div>
  <div class="stat-card"><div class="num">{{ stats.networks }}</div><div class="label">Networks</div></div>
  <div class="stat-card"><div class="num">{{ stats.compose }}</div><div class="label">Compose Stacks</div></div>
</div>

<div class="tabs">
  <div class="tab active" onclick="showTab('containers')">Containers</div>
  <div class="tab" onclick="showTab('images')">Images</div>
  <div class="tab" onclick="showTab('volumes')">Volumes</div>
  <div class="tab" onclick="showTab('networks')">Networks</div>
  <div class="tab" onclick="showTab('compose')">Compose</div>
  <div class="tab" onclick="showTab('system')">System</div>
</div>

<!-- CONTAINERS -->
<div id="tab-containers" class="section">
  <h2>📦 Containers</h2>
  <table>
    <tr><th>Status</th><th>Name</th><th>Image</th><th>Ports</th><th>Created</th></tr>
    {% for c in containers %}
    <tr>
      <td><span class="status-dot status-{{ c.get('State','unknown') }}"></span>{{ c.get('State','?') }}</td>
      <td>{{ c.get('Names','?').lstrip('/') }}</td>
      <td>{{ c.get('Image','?') }}</td>
      <td>{{ c.get('Ports','-') }}</td>
      <td>{{ c.get('RunningFor','?') }}</td>
    </tr>
    {% endfor %}
  </table>
</div>

<!-- IMAGES -->
<div id="tab-images" class="section hidden">
  <h2>🖼️ Images</h2>
  <table>
    <tr><th>Repository</th><th>Tag</th><th>ID</th><th>Size</th><th>Created</th></tr>
    {% for img in images %}
    <tr>
      <td>{{ img.get('Repository','<none>') }}</td>
      <td>{{ img.get('Tag','<none>') }}</td>
      <td>{{ img.get('ID','?')[:12] }}</td>
      <td>{{ img.get('Size','?') }}</td>
      <td>{{ img.get('CreatedAt','?') }}</td>
    </tr>
    {% endfor %}
  </table>
</div>

<!-- VOLUMES -->
<div id="tab-volumes" class="section hidden">
  <h2>💾 Volumes</h2>
  <table>
    <tr><th>Name</th><th>Driver</th><th>Scope</th></tr>
    {% for v in volumes %}
    <tr><td>{{ v.get('Name','?') }}</td><td>{{ v.get('Driver','?') }}</td><td>{{ v.get('Scope','?') }}</td></tr>
    {% endfor %}
  </table>
</div>

<!-- NETWORKS -->
<div id="tab-networks" class="section hidden">
  <h2>🌐 Networks</h2>
  <table>
    <tr><th>Name</th><th>Driver</th><th>Scope</th></tr>
    {% for n in networks %}
    <tr><td>{{ n.get('Name','?') }}</td><td>{{ n.get('Driver','?') }}</td><td>{{ n.get('Scope','?') }}</td></tr>
    {% endfor %}
  </table>
</div>

<!-- COMPOSE -->
<div id="tab-compose" class="section hidden">
  <h2>📝 Docker Compose Stacks</h2>
  <table>
    <tr><th>Name</th><th>Status</th><th>Config Files</th></tr>
    {% for stack in compose %}
    <tr>
      <td>{{ stack.get('Name','?') }}</td>
      <td>{{ stack.get('Status','?') }}</td>
      <td>{{ stack.get('ConfigFiles','-') }}</td>
    </tr>
    {% endfor %}
  </table>
</div>

<!-- SYSTEM -->
<div id="tab-system" class="section hidden">
  <h2>⚙️ System Info</h2>
  <pre>{{ info_json }}</pre>
  <h3 style="margin-top:16px">💽 Disk Usage</h3>
  <pre>{{ df_json }}</pre>
</div>

<footer>
  OMNIBUS Docker Server v1.0 | Alpine Linux VM | Android via QEMU<br>
  <small>Proxy reverso: nginx @ localhost:80 → containers</small>
</footer>

<script>
function showTab(id) {
  document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
  event.target.classList.add('active');
  document.querySelectorAll('.section').forEach(s => s.classList.add('hidden'));
  document.getElementById('tab-' + id).classList.remove('hidden');
}
</script>
</body>
</html>
"""

# ────────────────────────────────────────────────────────────
# API ROUTES
# ────────────────────────────────────────────────────────────

@app.route("/")
def dashboard():
    """UI web tipo Portainer embutida"""
    containers = docker_ps()
    images = docker_images()
    volumes = docker_volumes()
    networks = docker_networks()
    compose = docker_compose_ls()
    info = docker_info()
    df = docker_system_df()

    stats = {
        "containers_running": sum(1 for c in containers if c.get("State") == "running"),
        "containers_total": len(containers),
        "images": len(images),
        "volumes": len(volumes),
        "networks": len(networks),
        "compose": len(compose),
    }

    return render_template_string(
        DASHBOARD_HTML,
        containers=containers,
        images=images,
        volumes=volumes,
        networks=networks,
        compose=compose,
        info=info,
        info_json=json.dumps(info, indent=2, default=str),
        df_json=json.dumps(df, indent=2, default=str),
        stats=stats,
    )

@app.route("/api/health")
def health():
    return jsonify({"status": "ok", "time": datetime.utcnow().isoformat()})

@app.route("/api/containers")
def api_containers():
    return jsonify(docker_ps())

@app.route("/api/images")
def api_images():
    return jsonify(docker_images())

@app.route("/api/volumes")
def api_volumes():
    return jsonify(docker_volumes())

@app.route("/api/networks")
def api_networks():
    return jsonify(docker_networks())

@app.route("/api/compose")
def api_compose():
    return jsonify(docker_compose_ls())

@app.route("/api/system")
def api_system():
    return jsonify({
        "info": docker_info(),
        "df": docker_system_df(),
    })

@app.route("/api/container/<cid>/logs")
def container_logs(cid):
    stdout, stderr, rc = run_cmd(["docker", "logs", "--tail", "100", cid])
    if rc != 0:
        return jsonify({"error": stderr}), 500
    return jsonify({"logs": stdout})

@app.route("/api/container/<cid>/start", methods=["POST"])
def container_start(cid):
    _, stderr, rc = run_cmd(["docker", "start", cid])
    return jsonify({"success": rc == 0, "error": stderr if rc != 0 else None})

@app.route("/api/container/<cid>/stop", methods=["POST"])
def container_stop(cid):
    _, stderr, rc = run_cmd(["docker", "stop", cid])
    return jsonify({"success": rc == 0, "error": stderr if rc != 0 else None})

@app.route("/api/container/<cid>/remove", methods=["POST"])
def container_remove(cid):
    _, stderr, rc = run_cmd(["docker", "rm", cid])
    return jsonify({"success": rc == 0, "error": stderr if rc != 0 else None})

@app.route("/api/run", methods=["POST"])
def api_run():
    """Executa docker run via API (cuidado!)"""
    data = request.get_json() or {}
    image = data.get("image")
    if not image:
        return jsonify({"error": "image required"}), 400
    cmd = ["docker", "run", "-d", "--name", data.get("name", ""), image]
    if data.get("ports"):
        for p in data["ports"]:
            cmd.extend(["-p", p])
    if data.get("volumes"):
        for v in data["volumes"]:
            cmd.extend(["-v", v])
    stdout, stderr, rc = run_cmd(cmd)
    return jsonify({"success": rc == 0, "output": stdout, "error": stderr})

@app.route("/api/exec", methods=["POST"])
def api_exec():
    """Executa comando arbitrário na VM (restrito)"""
    data = request.get_json() or {}
    cmd = data.get("cmd", [])
    if not cmd:
        return jsonify({"error": "cmd required"}), 400
    # Whitelist básica de segurança
    allowed = {"docker", "ls", "cat", "ps", "free", "df", "uptime", "ip", "ping"}
    if cmd[0] not in allowed:
        return jsonify({"error": f"Comando '{cmd[0]}' não permitido"}), 403
    stdout, stderr, rc = run_cmd(cmd)
    return jsonify({"success": rc == 0, "stdout": stdout, "stderr": stderr})

# ────────────────────────────────────────────────────────────
if __name__ == "__main__":
    print("🐳 OMNIBUS Docker Server iniciando...")
    print("Acesse: http://localhost:7749")
    app.run(host="0.0.0.0", port=7749, debug=False)
