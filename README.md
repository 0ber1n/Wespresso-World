# Wespresso World

A vulnerable web application designed as a hands-on security training ground. Wespresso World simulates a real-world coffee storefront with intentional vulnerabilities that can be toggled on or off — ideal for CTF events, security training, and practicing offensive and defensive techniques.

---

## What You Need Installed First

Before doing anything else, make sure you have these installed on your machine:

- **Docker** — https://docs.docker.com/get-docker/
- **Docker Compose** — included with Docker Desktop; on Linux run `sudo apt install docker-compose-plugin`

Verify both work before continuing:
```bash
docker --version
docker compose version
```

---

## Single Player / Local Setup

This is for running one instance on your own machine.

**Step 1 — Clone the repo**
```bash
git clone https://github.com/0ber1n/wespresso-world.git
cd wespresso-world
```

**Step 2 — Create your config file**
```bash
cp .env.example .env
```
Open `.env` in a text editor. The defaults work fine for local use — you don't need to change anything to get started.

**Step 3 — Pull the Docker image**
```bash
docker pull ghcr.io/0ber1n/wespresso-world:latest
```
This downloads the app. It's a large image (~500MB), give it a minute.

**Step 4 — Start it**
```bash
docker compose up
```

**Step 5 — Open the app**

Go to **http://localhost:31337** in your browser.

To stop it, press `Ctrl+C` in the terminal.

---

## Vulnerability Flags

All vulnerabilities are off by default. Turn them on by editing `.env` and setting the relevant flag to `true`.

```
VULN_SQLI_LOGIN_ENABLED=true       # SQL injection on the login form
VULN_CART_IDOR_ENABLED=true        # Insecure direct object reference on carts
VULN_XXE_HARD_ENABLED=true         # XXE via content-type swap on order export
VULN_SSTI_THYMELEAF_ENABLED=true   # Server-side template injection on receipts
VULN_JWT_NONE_ENABLED=true         # JWT algorithm confusion (none)
VULN_SESSION_FIXATION_ENABLED=true # Session fixation on login
VULN_MASS_ASSIGNMENT_ENABLED=true  # Mass assignment on gift card redemption
# File upload — only enable ONE at a time:
VULN_FILE_UPLOAD_EXT_ONLY_ENABLED=true
VULN_FILE_UPLOAD_EXT_ENDS_WITH_ENABLED=true
VULN_FILE_UPLOAD_MAGIC_BYTE_ONLY_ENABLED=true
VULN_FILE_UPLOAD_CDR_BYPASS_ENABLED=true
```

After editing `.env`, restart the container:
```bash
docker compose down && docker compose up
```

> If you find a bug that isn't covered by any flag above, please open an issue.

---

## CTF / Multi-Player Setup (Running on a Server)

Use this if you're hosting a CTF and want each player to get their own isolated instance. Players visit a web page, enter their name, and get a personal URL — no Docker knowledge needed on their end.

### What You Need on the Server

- Docker installed and running
- Python 3.8 or newer
- Ports open on your firewall: `SPAWNER_PORT` and the full `PORT_START`–`PORT_END` range

### Step 1 — Clone the repo on your server

```bash
git clone https://github.com/0ber1n/wespresso-world.git
cd wespresso-world
```

### Step 2 — Create and configure `.env`

```bash
cp .env.example .env
```

Open `.env` and set these values at the top:

```
SPAWNER_PORT=8080        # port players visit to get their instance
HOST=123.456.789.0       # your server's PUBLIC IP address — change this
PORT_START=9000          # first port in the player instance range
PORT_END=9100            # last port (9000–9100 = up to 101 players)
```

Also set your vulnerability flags and flag values in the same file.

### Step 3 — Pull the Docker image

```bash
docker pull ghcr.io/0ber1n/wespresso-world:latest
```

### Step 4 — Install spawner dependencies

```bash
cd spawner
pip3 install -r requirements.txt
```

### Step 5 — Start the spawner

```bash
python3 spawner.py
```

Leave this running in your terminal (or use `screen`/`tmux` to keep it alive after you disconnect).

### Step 6 — Send players their link

Give players this URL:
```
http://YOUR_SERVER_IP:8080
```

They enter their name, hit **Brew Up**, and get their own instance URL within a few seconds. Tell them to bookmark it — they can re-enter their name to retrieve it if they lose it.

### Managing Instances (Admin)

The spawner page at `http://YOUR_SERVER_IP:8080` shows all running instances. From there you can:
- **86** — kill a single player's instance
- **Last Call** — kill all instances at once

When you stop the spawner (`Ctrl+C`), all player containers are automatically shut down.

---

## Offline Version

A standalone offline package (single tar file) is available for air-gapped environments. See the [offline-wespresso](https://github.com/0ber1n/offline-wespresso) repository for setup instructions.

---

## Tech Stack
- **Backend:** Java 21, Spring Boot, SQLite
- **Frontend:** React, Vite, Nginx
- **Auth:** JWT

---

Happy Hacking!
