#!/usr/bin/env bash
set -Eeuo pipefail

site=/etc/nginx/sites-available/math-platform
snippet=/etc/nginx/snippets/contact-api-demo.conf
backup="${site}.before-contact-demo-$(date -u +%Y%m%dT%H%M%SZ)"
temporary=$(mktemp)

cleanup() {
  rm -f "$temporary" /tmp/contact-api-demo-nginx.conf
}
trap cleanup EXIT

sudo cp -a "$site" "$backup"
sudo install -m 644 -o root -g root /tmp/contact-api-demo-nginx.conf "$snippet"

if ! sudo grep -Fq 'include /etc/nginx/snippets/contact-api-demo.conf;' "$site"; then
  sudo awk '
    BEGIN { inserted = 0 }
    !inserted && $0 == "    location /api/ {" {
      print "    include /etc/nginx/snippets/contact-api-demo.conf;"
      print ""
      inserted = 1
    }
    { print }
    END { if (!inserted) exit 42 }
  ' "$site" >"$temporary"
  sudo install -m 644 -o root -g root "$temporary" "$site"
fi

if ! sudo nginx -t; then
  sudo cp -a "$backup" "$site"
  sudo nginx -t
  exit 1
fi

if ! sudo systemctl reload nginx; then
  sudo cp -a "$backup" "$site"
  sudo nginx -t
  sudo systemctl reload nginx
  exit 1
fi

printf 'NGINX=active\nBACKUP=%s\n' "$backup"
