# WiFiLab Project Context

## Identity

- Application name: WiFiLab
- Author: SyntX34
- Package and namespace: `com.syntx34.wifilab`
- License: GNU General Public License v3.0
- Repository language: Kotlin, XML resources, Gradle Kotlin DSL, Markdown
- Minimum SDK: 26
- Compile and target SDK: 34
- Current app version: 1.0.0

## Objective

Build a polished Android application for:

- wireless network discovery
- authorized home network security auditing
- offline packet capture analysis
- WPA and password security education
- isolated attack anatomy simulations
- a searchable local research library

The application should look and behave like a professional defensive security tool, not a demonstration shell.

## Non goals

WiFiLab must not implement:

- recovery of real passwords or keys
- attacks against access points or clients
- packet injection
- rogue access points
- monitor mode assumptions
- root access assumptions
- capture or scan upload to remote services
- analytics, advertising or accounts
- exploitation of discovered hosts

## Safety and ethics

- Network audits are restricted to private RFC 1918 IPv4 ranges from /24 through /30.
- The user must explicitly confirm authorization before each audit.
- Audit discovery uses reachability checks and a small set of TCP connect probes only.
- Imported PCAP files remain in app private storage.
- EAPOL analysis exposes metadata only.
- Attack Anatomy is a fixed simulation narrative with no network side effects.
- Password Lab uses synthetic examples and never persists input.

## Current modules

1. Dashboard
2. Wi Fi Scanner
3. Network Auditor
4. Security Lab
5. Packet Analyzer
6. Password Lab
7. Attack Anatomy
8. Learning Center
9. Settings

Both Packet Analyzer and Password Lab are reachable from Security Lab and the Dashboard. Bottom navigation exposes Dashboard, Scanner, Auditor, Lab and Learn. Settings is available from the top bar.

## Important user facing behavior

- Scan cooldown defaults to 30 seconds and is configurable.
- Android scan throttling and permission denial are handled with explicit states.
- Network Auditor can be cancelled while running.
- The audit baseline can be saved only after a completed audit.
- Captures support classic PCAP and PCAPNG and are bounded to 64 MB.
- The parser supports Ethernet and common link and IP layers.
- The built in demo generates a synthetic four way handshake with a PMKID KDE.
- Learning progress, cooldown and completion are stored in DataStore.
- Deletion controls exist for every local data category.

## Dependencies of note

- Hilt for dependency injection
- Room with KSP for persistent data
- DataStore Preferences for settings and learning progress
- Navigation Compose for routing
- Compose Material 3 for UI
- No external networking library is used for research features

## Validation state

The repository is intended to pass:

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Always verify the actual output instead of assuming a successful build.
