#!/usr/bin/env bash
set -Eeuo pipefail
umask 077

environment_file=/etc/contact-api-demo/contact-api-demo.env
incoming_file=/tmp/contact-api-demo-smtp.env
backup_file="${environment_file}.before-smtp-$(date -u +%Y%m%dT%H%M%SZ)"
merged_file=$(mktemp)
response_file=$(mktemp)
committed=false

cleanup() {
  rm -f "$incoming_file" "$merged_file" "$response_file"
}

rollback_on_error() {
  status=$?
  trap - ERR
  if [[ "$committed" != true && -f "$backup_file" ]]; then
    sudo cp -a "$backup_file" "$environment_file"
    sudo systemctl restart contact-api-demo || true
  fi
  cleanup
  exit "$status"
}

trap cleanup EXIT
trap rollback_on_error ERR

required_keys=(
  APP_MAIL_ENABLED
  SMTP_HOST
  SMTP_PORT
  SMTP_USERNAME
  SMTP_PASSWORD
  SMTP_AUTH
  SMTP_SSL
  SMTP_STARTTLS
  CONTACT_MAIL_FROM
  CONTACT_MAIL_TO
)

for key in "${required_keys[@]}"; do
  if ! grep -q "^${key}=." "$incoming_file"; then
    printf 'Missing required SMTP setting: %s\n' "$key" >&2
    exit 1
  fi
done

if grep -Eq 'YOUR_|replace-with|authorization-code' "$incoming_file"; then
  echo 'SMTP file still contains placeholder values.' >&2
  exit 1
fi

sudo cp -a "$environment_file" "$backup_file"

sudo grep -Ev \
  '^(APP_MAIL_ENABLED|SMTP_HOST|SMTP_PORT|SMTP_USERNAME|SMTP_PASSWORD|SMTP_AUTH|SMTP_SSL|SMTP_STARTTLS|SMTP_CONNECTION_TIMEOUT_MS|SMTP_READ_TIMEOUT_MS|SMTP_WRITE_TIMEOUT_MS|CONTACT_MAIL_FROM|CONTACT_MAIL_TO|CONTACT_MAIL_SUBJECT_PREFIX)=' \
  "$environment_file" >"$merged_file"
cat "$incoming_file" >>"$merged_file"
sudo install -m 600 -o root -g root "$merged_file" "$environment_file"

sudo systemctl restart contact-api-demo

healthy=false
for _ in {1..30}; do
  if health_body=$(curl -fsS --max-time 15 \
      http://127.0.0.1:18081/contact-demo/actuator/health 2>/dev/null) \
      && grep -q '"status":"UP"' <<<"$health_body"; then
    healthy=true
    break
  fi
  sleep 1
done

if [[ "$healthy" != true ]]; then
  echo 'Service or SMTP health check did not become healthy.' >&2
  sudo journalctl -u contact-api-demo -n 80 --no-pager >&2
  exit 1
fi

status_code=$(curl -sS --max-time 30 \
  -o "$response_file" \
  -w '%{http_code}' \
  -X POST http://127.0.0.1:18081/contact-demo/api/v1/contacts \
  -H 'Content-Type: application/json' \
  --data '{"name":"SMTP Verification","email":"portfolio-demo@example.com","phone":"+1 415 555 0136","message":"Synthetic test: real contact notification email is enabled."}')

if [[ "$status_code" != 201 ]]; then
  printf 'Synthetic submission failed with HTTP %s.\n' "$status_code" >&2
  cat "$response_file" >&2
  exit 1
fi

committed=true
trap - ERR
printf 'SMTP_ENABLED=true\nHEALTH=%s\nSUBMISSION_STATUS=%s\n' \
  "$health_body" "$status_code"
cat "$response_file"
printf '\n'
