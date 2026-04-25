#!/usr/bin/env bash
# Creates a CTFd-importable zip from challenges.yaml.
# Usage: ./ctfd/package.sh
# Then: CTFd Admin Panel → Import → upload wespresso-challenges.zip

set -euo pipefail
cd "$(dirname "$0")"

zip -j ../wespresso-challenges.zip challenges.yaml
echo "Created wespresso-challenges.zip — import via CTFd Admin Panel → Import"
