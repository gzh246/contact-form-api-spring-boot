# Deployment examples

These files document the deployment shape used by the public demonstration:

- an unprivileged systemd service
- a loopback-only application listener
- an HTTPS Nginx reverse proxy
- a fixed-recipient SMTP adapter configured at runtime

They are examples, not a universal production installer. Review paths, users,
hostnames, TLS settings, backup policy, and firewall rules before adapting them.

## Nginx rate-limit zone

`nginx-location.conf` expects this zone in the Nginx `http` block:

```nginx
limit_req_zone $binary_remote_addr zone=contact_api:10m rate=5r/m;
```

Include `nginx-location.conf` from the HTTPS `server` block only after the zone
exists. Then validate and reload the configuration:

```bash
sudo nginx -t
sudo systemctl reload nginx
```

The application listener in the systemd example remains bound to
`127.0.0.1:18081`; it should not be exposed directly to the internet.

## Runtime secrets

Create the server environment file outside the repository and install it as
root-readable only. Never upload a populated environment file to GitHub.

The `remote-enable-smtp.sh` helper expects a short-lived
`/tmp/contact-api-demo-smtp.env` input, validates required keys, installs a
root-only merged configuration, tests the health endpoint and one synthetic
submission, and removes its temporary inputs. Use a secure transfer method and
remove the source copy immediately if adapting this workflow.
