# Privacy

WiFiLab is designed as a local research tool. It has no account system, no analytics, no advertising and no remote backend.

## Data the app can create

| Data | Storage | Purpose |
| --- | --- | --- |
| Wi Fi scan results | In memory during the current process | Display nearby networks and channel distribution |
| Current connection details | In memory | Dashboard status |
| Audit summaries and device rows | Room database | Local audit history |
| Trusted baseline | Room database | Compare future audits |
| Imported capture metadata | Room database | Reopen local captures |
| Imported capture files | App private files directory | Offline packet inspection |
| Research activity | Room database | Dashboard recent activity |
| Learning completion | DataStore Preferences | Local progress |
| Scan cooldown | DataStore Preferences | Respect Android throttling |

## Data the app does not collect

- No location history or coordinates
- No advertising identifiers
- No contact, calendar, photo or microphone data
- No analytics events
- No crash reports sent by the app
- No captured packet content uploaded to a server
- No MAC address harvesting beyond fields Android exposes

## Network access

The manifest declares `INTERNET` because the Network Auditor uses local TCP sockets to determine whether a service is reachable on an authorized private network. WiFiLab does not send HTTP requests to remote servers for scans, audits, captures or learning.

Capture import and analysis are entirely offline. The Packet Analyzer never contacts addresses found in a capture.

## Android backup

`android:allowBackup="false"` is set. Research data is not included in Android cloud backup or device transfer by the app.

## Permissions

- `ACCESS_FINE_LOCATION` up to API 32 is used only because older Android versions require it to return Wi Fi scan results.
- `NEARBY_WIFI_DEVICES` from API 33 is used to read nearby Wi Fi scan results and connection identity, declared with `neverForLocation`.
- `ACCESS_NETWORK_STATE` is used for active connection and link properties.
- `ACCESS_WIFI_STATE` is used for Wi Fi state.
- `INTERNET` supports local TCP reachability checks during an authorized audit.

WiFiLab does not run in the background, does not scan automatically and does not use location in the background.

## Data deletion

Settings provides controls to delete audit history, the baseline, imported captures, activity, learning progress and all local research data. Imported capture files are deleted from app private storage as part of capture deletion.

Uninstalling the application removes the app sandbox, including the Room database, DataStore preferences and imported capture files.

## Export and sharing

WiFiLab does not automatically export or share research data. Capture files and audit results can contain sensitive network details. If you share evidence manually, redact addresses, names and payloads first.

## Contact

For privacy concerns, open a private security advisory using the repository Security tab. Do not attach real captures or personal network data.
