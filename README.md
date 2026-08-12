* I panicked released this repo before cleaning up the repo. Instructions below are accurate, just know there's random things in the repo that aren't needed right meow*

# Wespresso World

A vulnerable web application designed as a hands-on security training ground. Wespresso World simulates a real-world coffee storefront API with intentional vulnerabilities that can be toggled on or off — making it ideal for CTF challenges, security training, and practicing offensive and defensive techniques.

---

## Requirements
- Docker
- Docker Compose

## Quick Start

1. Clone or download `docker-compose.yml` and `.env.example` from this repo

2. Copy the example env file and configure it:
```
cp .env.example .env
```

3. Pull the latest image:
```
docker pull ghcr.io/0ber1n/wespresso-world:latest
```

4. Start the container:
```
docker compose up
```

4. Access the app at **http://localhost:31337**

---

## Vulnerability Settings

Vulnerabilities can be toggled on or off by editing the boolean flags in the `.env` file. This allows you to control the attack surface for training sessions or CTF events.

> If you encounter any bugs that are not explicitly listed as active in the `.env`, please report them.

---

## Offline Version

A standalone offline package (single tar file) is available for air-gapped environments. See the [offline-wespresso](https://github.com/0ber1n/offline-wespresso) repository for setup instructions.

---

## Tech Stack
- **Backend:** Java 21, Spring Boot, SQLite
- **Frontend:** React, Vite, Nginx
- **Auth:** JWT

---

## Good Luck
Happy Hacking!
