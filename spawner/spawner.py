#!/usr/bin/env python3
import atexit
import os
import re
import signal
import subprocess

from flask import Flask, redirect, request, url_for

# Load .env file before reading config so all vars are available
def _load_env(path):
    try:
        with open(path) as f:
            for line in f:
                line = line.strip()
                if not line or line.startswith("#") or "=" not in line:
                    continue
                key, _, val = line.partition("=")
                os.environ.setdefault(key.strip(), val.strip())
    except FileNotFoundError:
        pass

_load_env(os.path.join(os.path.dirname(__file__), "../.env"))

app = Flask(__name__)

IMAGE      = os.environ.get("WESPRESSO_IMAGE", "ghcr.io/0ber1n/wespresso-world:latest")
ENV_FILE   = os.environ.get("ENV_FILE", os.path.join(os.path.dirname(__file__), "../.env"))
PORT_START = int(os.environ.get("PORT_START", 9000))
PORT_END   = int(os.environ.get("PORT_END",   9100))
HOST       = os.environ.get("HOST", "localhost")

HTML = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>BREWBOARD — Wespresso CTF</title>
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet" />
  <style>
    *, *::before, *::after {{ box-sizing: border-box; margin: 0; padding: 0; }}

    :root {{
      --bg:     #242322;
      --surf:   #2e2c2b;
      --surf2:  #383534;
      --border: #4a4744;
      --accent: #e8941a;
      --gold:   #d4a85a;
      --text:   #f2efeb;
      --muted:  #9a9490;
      --green:  #4cd97a;
      --red:    #e85555;
    }}

    body {{
      font-family: 'Space Grotesk', sans-serif;
      background: var(--bg);
      color: var(--text);
      min-height: 100vh;
    }}

    /* ── TOPBAR ── */
    .topbar {{
      background: var(--surf);
      border-bottom: 3px solid var(--accent);
      padding: 0 32px;
      height: 72px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      position: sticky;
      top: 0;
      z-index: 10;
    }}
    .brand {{
      display: flex;
      align-items: baseline;
      gap: 14px;
    }}
    .brand-name {{
      font-size: 28px;
      font-weight: 800;
      letter-spacing: -1.5px;
      color: var(--accent);
      text-transform: uppercase;
    }}
    .brand-name em {{ color: var(--text); font-style: normal; }}
    .brand-tag {{
      font-size: 12px;
      font-weight: 600;
      letter-spacing: 2px;
      text-transform: uppercase;
      color: var(--muted);
    }}
    .topbar-stat {{
      font-size: 15px;
      font-weight: 600;
      color: var(--muted);
      letter-spacing: 0.5px;
    }}
    .topbar-stat strong {{
      color: var(--accent);
      font-size: 24px;
      font-weight: 800;
      margin-right: 4px;
    }}

    /* ── MAIN ── */
    .main {{
      max-width: 1000px;
      margin: 0 auto;
      padding: 44px 28px 100px;
    }}

    /* ── BANNER ── */
    .banner {{
      border-radius: 10px;
      padding: 22px 26px;
      margin-bottom: 36px;
      display: flex;
      gap: 18px;
      align-items: flex-start;
      font-size: 16px;
      line-height: 1.7;
    }}
    .banner-icon {{ font-size: 26px; flex-shrink: 0; }}
    .banner strong {{ display: block; font-size: 20px; font-weight: 700; margin-bottom: 6px; }}
    .banner small {{ font-size: 13px; opacity: 0.75; display: block; margin-top: 6px; }}
    .banner a {{ color: inherit; font-family: 'JetBrains Mono', monospace; font-size: 15px; font-weight: 500; }}
    .banner.warn {{ background: #1e1200; border: 2px solid var(--accent); color: var(--accent); }}
    .banner.ok   {{ background: #081a10; border: 1px solid #1e5c35; color: var(--green); }}
    .banner.err  {{ background: #1a0808; border: 1px solid #5c1a1a; color: var(--red); }}

    /* ── SPAWN ── */
    .spawn-wrap {{ margin-bottom: 52px; }}
    .section-eyebrow {{
      font-size: 12px;
      font-weight: 700;
      letter-spacing: 2.5px;
      text-transform: uppercase;
      color: var(--muted);
      margin-bottom: 14px;
    }}
    .spawn-row {{ display: flex; gap: 10px; }}
    input[type=text] {{
      flex: 1;
      background: var(--surf);
      border: 2px solid var(--border);
      border-radius: 8px;
      color: var(--text);
      padding: 16px 20px;
      font-size: 18px;
      font-family: 'Space Grotesk', sans-serif;
      font-weight: 500;
      outline: none;
      transition: border-color 0.15s;
    }}
    input[type=text]:focus {{ border-color: var(--accent); }}
    input[type=text]::placeholder {{ color: var(--muted); font-weight: 400; }}

    .brew-btn {{
      background: var(--accent);
      border: none;
      border-radius: 8px;
      color: #1a1210;
      padding: 16px 32px;
      font-size: 16px;
      font-weight: 800;
      font-family: 'Space Grotesk', sans-serif;
      letter-spacing: 0.5px;
      text-transform: uppercase;
      cursor: pointer;
      transition: background 0.15s, transform 0.1s;
      display: flex;
      align-items: center;
      gap: 8px;
      white-space: nowrap;
    }}
    .brew-btn:hover {{ background: #f0a030; transform: translateY(-1px); }}
    .brew-btn:active {{ transform: none; }}
    .brew-btn:disabled {{ opacity: 0.4; cursor: not-allowed; transform: none; }}
    .brew-btn .spin {{
      display: none;
      width: 16px; height: 16px;
      border: 2px solid rgba(0,0,0,0.25);
      border-top-color: #0e0a07;
      border-radius: 50%;
      animation: rot 0.6s linear infinite;
    }}
    .brew-btn.loading .spin {{ display: block; }}
    .brew-btn.loading .btxt {{ opacity: 0.7; }}
    @keyframes rot {{ to {{ transform: rotate(360deg); }} }}

    /* ── QUEUE BAR ── */
    .queue-bar {{
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 18px;
    }}
    .last-call {{
      background: transparent;
      border: 2px solid var(--red);
      border-radius: 6px;
      color: var(--red);
      font-size: 13px;
      font-weight: 800;
      font-family: 'Space Grotesk', sans-serif;
      letter-spacing: 1.5px;
      text-transform: uppercase;
      padding: 9px 18px;
      cursor: pointer;
      transition: background 0.15s;
    }}
    .last-call:hover {{ background: #3d1a1a; }}

    /* ── TICKET GRID ── */
    .grid {{
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
      gap: 14px;
    }}

    .ticket {{
      background: var(--surf);
      border: 1px solid var(--border);
      border-radius: 10px;
      overflow: hidden;
      display: flex;
      flex-direction: column;
      transition: border-color 0.15s, transform 0.15s;
    }}
    .ticket:hover {{ border-color: var(--gold); transform: translateY(-3px); }}

    .ticket-header {{
      background: var(--accent);
      padding: 8px 14px;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }}
    .ticket-label {{
      font-size: 9px;
      font-weight: 800;
      letter-spacing: 2px;
      text-transform: uppercase;
      color: rgba(14,10,7,0.55);
    }}
    .live-pill {{
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 9px;
      font-weight: 800;
      letter-spacing: 1.5px;
      text-transform: uppercase;
      color: rgba(14,10,7,0.7);
    }}
    .live-dot {{
      width: 6px; height: 6px;
      background: #0e0a07;
      border-radius: 50%;
      opacity: 0.7;
      animation: blink 2s ease infinite;
    }}
    @keyframes blink {{ 0%,100% {{ opacity:0.7; }} 50% {{ opacity:0.15; }} }}

    .ticket-body {{
      padding: 16px 14px 12px;
      flex: 1;
    }}
    .ticket-name {{
      font-size: 26px;
      font-weight: 800;
      letter-spacing: -0.5px;
      color: var(--text);
      word-break: break-all;
      margin-bottom: 6px;
    }}
    .ticket-port {{
      font-size: 13px;
      font-weight: 600;
      color: var(--muted);
      letter-spacing: 0.5px;
      margin-bottom: 8px;
    }}
    .ticket-url {{
      font-family: 'JetBrains Mono', monospace;
      font-size: 12px;
      color: var(--gold);
      text-decoration: none;
      word-break: break-all;
      line-height: 1.5;
    }}
    .ticket-url:hover {{ text-decoration: underline; }}

    .ticket-footer {{
      padding: 12px 14px;
      border-top: 1px solid var(--border);
      display: flex;
      gap: 8px;
    }}
    .tkt-btn {{
      flex: 1;
      background: var(--surf2);
      border: 1px solid var(--border);
      border-radius: 6px;
      color: var(--muted);
      font-size: 12px;
      font-weight: 700;
      font-family: 'Space Grotesk', sans-serif;
      letter-spacing: 1px;
      text-transform: uppercase;
      padding: 9px 0;
      cursor: pointer;
      transition: all 0.15s;
    }}
    .tkt-btn:hover {{ color: var(--text); border-color: #6a6460; }}
    .tkt-btn.copied {{ color: var(--green); border-color: var(--green); background: #0d2018; }}
    .tkt-btn.red {{ color: var(--red); border-color: #6b2a2a; }}
    .tkt-btn.red:hover {{ background: #3d1a1a; border-color: var(--red); }}

    /* ── EMPTY ── */
    .empty {{
      grid-column: 1 / -1;
      text-align: center;
      padding: 80px 0;
      border: 2px dashed var(--border);
      border-radius: 12px;
      color: var(--muted);
    }}
    .empty-cup {{ font-size: 52px; margin-bottom: 16px; display: block; }}
    .empty-msg {{ font-size: 18px; font-weight: 600; }}
    .empty-sub {{ font-size: 14px; margin-top: 8px; opacity: 0.6; }}
  </style>
</head>
<body>

<div class="topbar">
  <div class="brand">
    <span class="brand-name">&#9749; Brew<em>Board</em></span>
    <span class="brand-tag">Wespresso CTF Admin</span>
  </div>
  <div class="topbar-stat"><strong>{count}</strong> brewing</div>
</div>

<div class="main">

  {message}

  <div class="spawn-wrap">
    <div class="section-eyebrow">New Order</div>
    <form method="POST" action="/spawn" onsubmit="handleSubmit(this)">
      <div class="spawn-row">
        <input type="text" name="player" placeholder="player / team name" required maxlength="20" autocomplete="off" />
        <button type="submit" class="brew-btn" id="brewBtn">
          <div class="spin"></div>
          <span class="btxt">Brew Up</span>
        </button>
      </div>
    </form>
  </div>

  <div class="queue-bar">
    <div class="section-eyebrow" style="margin:0">Order Queue</div>
    <form method="POST" action="/kill-all" onsubmit="return confirm('Last call — shut down all instances?')">
      <button type="submit" class="last-call">&#9211; Last Call</button>
    </form>
  </div>

  <div class="grid">
    {instances_html}
  </div>

</div>

<script>
  function handleSubmit() {{
    const btn = document.getElementById('brewBtn');
    btn.classList.add('loading');
    btn.disabled = true;
    btn.querySelector('.btxt').textContent = 'Brewing…';
  }}

  function copyUrl(btn, url) {{
    navigator.clipboard.writeText(url).then(() => {{
      btn.textContent = 'Copied!';
      btn.classList.add('copied');
      setTimeout(() => {{ btn.textContent = 'Copy'; btn.classList.remove('copied'); }}, 2000);
    }});
  }}

  function confirmKill(name) {{
    return confirm('86 the instance for "' + name + '"?');
  }}
</script>
</body>
</html>"""


def get_instances():
    """Returns {container_name: host_port} for all wespresso-player-* containers."""
    result = subprocess.run(
        ["docker", "ps",
         "--filter", "name=wespresso-player-",
         "--format", "{{.Names}}\t{{.Ports}}"],
        capture_output=True, text=True
    )
    out = {}
    for line in result.stdout.strip().splitlines():
        if "\t" not in line:
            continue
        name, ports = line.split("\t", 1)
        m = re.search(r":(\d+)->80", ports)
        if m:
            out[name] = int(m.group(1))
    return out



def kill_container(name):
    subprocess.run(["docker", "rm", "-f", name], capture_output=True)



def kill_all_containers():
    for name in get_instances():
        kill_container(name)


def next_free_port(used_ports):
    for port in range(PORT_START, PORT_END + 1):
        if port not in used_ports:
            return port
    return None


def sanitize(name):
    return re.sub(r"[^a-zA-Z0-9_-]", "", name)[:20]


def render_instances(instances):
    if not instances:
        return ('<div class="empty">'
                '<span class="empty-cup">&#9749;</span>'
                '<div class="empty-msg">No orders yet</div>'
                '<div class="empty-sub">Brew up an instance to get started</div>'
                '</div>')
    rows = []
    for n, p in sorted(instances.items(), key=lambda x: x[1]):
        player = n.removeprefix("wespresso-player-")
        url = f"http://{HOST}:{p}"
        rows.append(
            f'<div class="ticket">'
            f'  <div class="ticket-header">'
            f'    <span class="ticket-label">Order</span>'
            f'    <span class="live-pill"><span class="live-dot"></span>Live</span>'
            f'  </div>'
            f'  <div class="ticket-body">'
            f'    <div class="ticket-name">{player}</div>'
            f'    <div class="ticket-port">:{p}</div>'
            f'    <a class="ticket-url" href="{url}" target="_blank">{url}</a>'
            f'  </div>'
            f'  <div class="ticket-footer">'
            f'    <button class="tkt-btn" onclick="copyUrl(this, \'{url}\')">Copy</button>'
            f'    <form method="POST" action="/kill/{n}" onsubmit="return confirmKill(\'{player}\')" style="display:contents">'
            f'      <button type="submit" class="tkt-btn red">86</button>'
            f'    </form>'
            f'  </div>'
            f'</div>'
        )
    return "".join(rows)


def render(message="", instances=None):
    if instances is None:
        instances = get_instances()
    return HTML.format(
        message=message,
        instances_html=render_instances(instances),
        count=len(instances),
    )


@app.get("/")
def index():
    return render()


@app.post("/spawn")
def spawn():
    raw   = request.form.get("player", "").strip()
    name  = sanitize(raw)
    if not name:
        return render('<div class="banner err"><span class="banner-icon">&#9888;</span><div>Name cannot be empty or contain only special characters.</div></div>'), 400

    container = f"wespresso-player-{name}"
    instances = get_instances()

    if container in instances:
        port = instances[container]
        url  = f"http://{HOST}:{port}"
        msg  = f'<div class="banner ok"><span class="banner-icon">&#10003;</span><div><strong>Already brewing</strong><br>Their instance is up at <a href="{url}" target="_blank">{url}</a></div></div>'
        return render(msg, instances)

    port = next_free_port(set(instances.values()))
    if port is None:
        return render('<div class="banner err"><span class="banner-icon">&#9888;</span><div><strong>Sold out</strong>No free ports in range — expand PORT_END or kill some instances.</div></div>'), 503

    env_path = os.path.abspath(ENV_FILE)
    proc = subprocess.run(
        ["docker", "run", "-d",
         "--name", container,
         "--env-file", env_path,
         "-p", f"{port}:80",
         IMAGE],
        capture_output=True, text=True
    )

    if proc.returncode != 0:
        err = proc.stderr.strip()
        return render(f'<div class="banner err"><span class="banner-icon">&#9888;</span><div><strong>Docker error</strong><br><small>{err}</small></div></div>'), 500

    url = f"http://{HOST}:{port}"
    msg = (f'<div class="banner warn"><span class="banner-icon">&#9749;</span>'
           f'<div><strong>Order up — give it ~30 seconds before clicking.</strong><br>'
           f'Spring Boot is still waking up. Click too early and you\'ll get a 502.<br>'
           f'<a href="{url}" target="_blank">{url}</a>'
           f'<small>Send this URL to the player. Same name here retrieves it later.</small></div></div>')
    instances[container] = port
    return render(msg, instances)


@app.post("/kill/<container_name>")
def kill(container_name):
    if not container_name.startswith("wespresso-player-"):
        return "Forbidden", 403
    kill_container(container_name)
    return redirect(url_for("index"))


@app.post("/kill-all")
def kill_all():
    kill_all_containers()
    return redirect(url_for("index"))


# Clean up all player containers when the spawner exits
def _on_sigterm(signum, frame):
    raise SystemExit(0)

atexit.register(kill_all_containers)
signal.signal(signal.SIGTERM, _on_sigterm)


if __name__ == "__main__":
    spawner_port = int(os.environ.get("SPAWNER_PORT", 8080))
    app.run(host="0.0.0.0", port=spawner_port, debug=False)
