# Security Policy

WiFiLab handles local network metadata and imported packet captures. We take security and privacy reports seriously.

## Supported versions

| Version | Supported |
| --- | --- |
| 1.x | Yes |
| Development branches | Best effort |

## Reporting a vulnerability

Do not open a public issue for a security vulnerability, leaked personal data, or a problem that could enable unauthorized network activity.

Use the repository's **Security** tab and choose **Report a vulnerability** to open a private GitHub Security Advisory. Include:

- affected version or commit
- Android version and device model if relevant
- clear reproduction steps using synthetic data
- impact and likely attack conditions
- whether any real network, capture or credential was involved
- a suggested fix if you have one

Do not attach real packet captures, SSIDs, BSSIDs, public IP addresses, personal data or credentials. Redact or synthesize evidence.

## Response expectations

- We aim to acknowledge a complete report within 7 days.
- Triage and remediation are best effort and depend on severity and maintainer availability.
- We will coordinate disclosure and credit with the reporter where appropriate.
- Please allow reasonable time for a fix before public disclosure.

## In scope

- bypasses of the audit authorization or private subnet restriction
- packet parsing memory safety, denial of service or unintended network access
- accidental capture upload, telemetry, backup or path traversal
- local file handling issues involving imported captures
- permission handling errors that expose data without user action
- injection or unsafe rendering of capture and network derived text
- dependency vulnerabilities reachable from the application

## Out of scope

- attacks against networks or devices you do not own
- social engineering of users
- missing security features that are explicitly documented as educational limitations
- reports requiring root, monitor mode or unsupported Android behavior
- denial of service from deliberately enormous files beyond documented limits
- false positive Wi Fi scan results from Android itself
- vulnerabilities in the Android operating system or device firmware

## Security design notes

- WiFiLab never requests root.
- The audit scope is restricted to private RFC 1918 ranges from /24 through /30 and requires explicit authorization.
- Service discovery uses short TCP connections only. It does not authenticate, exploit or modify devices.
- Imported captures are copied into app private storage and parsed locally. They are never uploaded.
- Capture parsing is bounded by file size, packet count and record size limits.
- Captured EAPOL metadata is displayed without key derivation or passphrase recovery.
- Android cloud backup is disabled in the manifest.
- There is no telemetry or remote API.

## Dependency and release hygiene

- Dependencies are version pinned in `gradle/libs.versions.toml`.
- CI runs unit tests, lint and a debug assembly for every push and pull request to `main`.
- Release builds should be signed with a maintainer controlled key that is never committed.
- Security relevant changes must update this document or `docs/PRIVACY.md` when the data handling model changes.
