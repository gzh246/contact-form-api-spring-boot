#!/usr/bin/env bash
set -Eeuo pipefail

release="release-$(date -u +%Y%m%dT%H%M%SZ)"

cleanup() {
  rm -f \
    /tmp/contact-api-demo.jar \
    /tmp/contact-api-demo.service \
    /tmp/contact-api-demo.env
}
trap cleanup EXIT

if ! id contactdemo >/dev/null 2>&1; then
  sudo useradd --system --home /nonexistent --shell /usr/sbin/nologin contactdemo
fi

sudo install -d -m 755 -o root -g root \
  /opt/contact-api-demo \
  /opt/contact-api-demo/releases \
  /etc/contact-api-demo
sudo install -d -m 750 -o contactdemo -g contactdemo /var/lib/contact-api-demo
sudo install -d -m 755 -o root -g root "/opt/contact-api-demo/releases/$release"
sudo install -m 644 -o root -g root \
  /tmp/contact-api-demo.jar \
  "/opt/contact-api-demo/releases/$release/contact-api-demo.jar"

if [[ ! -f /etc/contact-api-demo/contact-api-demo.env ]]; then
  sudo install -m 600 -o root -g root \
    /tmp/contact-api-demo.env \
    /etc/contact-api-demo/contact-api-demo.env
fi

sudo install -m 644 -o root -g root \
  /tmp/contact-api-demo.service \
  /etc/systemd/system/contact-api-demo.service
sudo ln -sfn "/opt/contact-api-demo/releases/$release" /opt/contact-api-demo/current
sudo systemctl daemon-reload
sudo systemctl enable contact-api-demo
sudo systemctl restart contact-api-demo

healthy=false
for _ in {1..40}; do
  if curl -fsS \
    http://127.0.0.1:18081/contact-demo/actuator/health \
    >/tmp/contact-api-demo-health.json; then
    healthy=true
    break
  fi
  sleep 1
done

if [[ "$healthy" != true ]]; then
  sudo journalctl -u contact-api-demo -n 100 --no-pager
  exit 1
fi

cat /tmp/contact-api-demo-health.json
printf '\nSERVICE=%s\n' "$(sudo systemctl is-active contact-api-demo)"
printf 'RELEASE=%s\n' "$release"
