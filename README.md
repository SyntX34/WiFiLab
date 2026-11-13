# WiFiLab

WiFiLab is an open source Android application for wireless network discovery, authorized home network auditing, offline packet analysis and practical cybersecurity education. It is built as a native Kotlin and Jetpack Compose application with a local first architecture.

The project is intentionally defensive. It does not implement attacks, rogue access points, packet injection or monitor mode. Every offensive concept in the application is presented as a simulation using fixed educational data.

**Author:** SyntX34  
**License:** GNU General Public License v3.0  
**Package:** `com.syntx34.wifilab`  
**Minimum Android:** API 26, Android 8.0  
**Compile and target SDK:** API 34

## Why WiFiLab exists

Wireless security is easier to learn when discovery data, packet structure and security policy are visible in one place. Most mobile tutorials either hide the details or require root. WiFiLab demonstrates what a normal, non root Android application can do responsibly:

- read the networks Android exposes
- inspect the current connection
- audit a small network you explicitly own
- parse packet captures you provide
- learn from simulated attack flows without touching third party systems

## Features

### Dashboard

- Live Wi Fi connection state, SSID and BSSID when Android exposes them.
- Signal strength, local IPv4 address, gateway, link speed, band and channel.
- Advertised security type when it can be matched from scan data.
- Latest audit summary and unexpected device count.
- Recent local research activity.
- Quick access to the scanner, packet analyzer and security laboratory.

### Wi Fi Scanner

- Foreground scans using the Android Wi Fi framework.
- SSID, BSSID, frequency, channel, band, channel width, Wi Fi generation and RSSI.
- Security capability parsing for WPA, WPA2, WPA3 transition mode, WPA3, WPA Enterprise, OWE, WEP and open networks.
- Interactive channel distribution chart with filtering.
- Search by SSID, BSSID or security, plus band and protection filters.
- Explicit permission flow for location and nearby Wi Fi devices.
- Cooldown and throttling handling with user visible status.
- No automatic joining and no credential capture.

### Network Auditor

- Explicit authorization confirmation before any local network discovery.
- Private IPv4 scopes only, restricted to /24 through /30.
- Current local network detection with editable CIDR.
- Conservative host reachability checks and a small TCP service probe set.
- Progress reporting and cancellation.
- Trusted baseline storage and unexpected device detection.
- Practical security recommendations based on observed local services.
- No exploitation, authentication attempts, traffic modification or automatic blocking.

### Security Laboratory

- Local protocol library covering WPA2, WPA3, WPS, WPA Enterprise and OWE.
- Authentication flows, strengths, weaknesses and defensive notes.
- Direct access to the Packet Analyzer.
- Attack Anatomy simulations for ARP spoofing, DNS spoofing, evil twin, WPA authentication, deauthentication concepts and packet interception.
- Every simulation states clearly that no packets are generated or intercepted.

### Packet Analyzer

- User selected classic PCAP and PCAPNG files.
- Captures copied to private app storage, never uploaded.
- Classic PCAP in big and little endian, microsecond and nanosecond variants.
- PCAPNG section headers, interface descriptions, enhanced packet blocks and simple packet blocks.
- Ethernet, VLAN, raw IP, Linux cooked and Linux cooked v2 link layers.
- IPv4, IPv6, TCP, UDP, ICMP, ARP and DNS metadata.
- EAPOL key message identification for the WPA four way handshake.
- Key control flags, replay counters, nonce presence, encrypted key data status and PMKID presence.
- Searchable packet table and protocol filtering.
- Built in synthetic EAPOL demonstration with no real credentials.
- No decryption, no key derivation and no credential recovery.

### Learning Center

- Searchable terminology and protocol explanations.
- Packet inspection lessons.
- Guided practical exercises with reflection questions.
- Practical home network security recommendations.
- Completion progress stored locally with DataStore.

### Settings

- Configurable scan cooldown from 15 to 120 seconds.
- Local storage inventory for audits, baseline, captures, activity and learning progress.
- Scoped deletion controls and a full local data reset.
- Privacy and open source information.

## Technology

- Kotlin 2.0.21
- Jetpack Compose with Material 3
- Navigation Compose
- MVVM presentation layer
- Hilt dependency injection
- Kotlin Coroutines and Flow
- Room database with KSP
- DataStore Preferences
- Gradle Kotlin DSL and version catalog
- Target SDK 34, minimum SDK 26

## Architecture

WiFiLab follows a layered Clean Architecture style:

```text
ui/                     Compose screens, navigation and Hilt view models
domain/
  model/                Immutable domain types
  repository/           Repository and service contracts
  security/             Pure parsers and calculations
  content/              Offline educational catalog
  usecase/              Application specific orchestration
data/
  local/                Room database, entities and data access objects
  network/              Conservative local network discovery
  packet/               PCAP, PCAPNG, EAPOL and DNS parsers
  preferences/          DataStore backed settings
  repository/           Domain contract implementations
  wifi/                 Android Wi Fi repository and result mapper
core/
  di/                   Hilt modules
  util/                 Formatting helpers
```

The domain layer has no Android framework dependencies except simple types where required. Parsers, entropy calculations, subnet validation, baseline comparison and capability mapping are unit tested directly on the JVM.

### Data flow

1. Compose screens call Hilt view models.
2. View models use domain repositories and use cases.
3. Data implementations talk to Android APIs, Room or DataStore.
4. Results are exposed as immutable state through StateFlow.
5. All persistent research data stays in the app sandbox.

Room stores audit session summaries, discovered devices, the trusted baseline, imported capture metadata, activity events and local research records. Imported capture files are stored in `filesDir/captures`.

## Project structure

```text
.
├── app/
│   ├── schemas/                  Room schema exports
│   └── src/
│       ├── main/
│       │   ├── java/com/syntx34/wifilab/
│       │   └── res/
│       └── test/                 JVM unit tests
├── agent/                        AI and maintainer handoff notes
├── docs/                         Privacy and responsible use notes
├── gradle/                       Version catalog and wrapper
├── .github/workflows/            Android CI
├── .vscode/                      Editor baseline
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Build locally

### Requirements

- JDK 17 or newer, JDK 21 recommended
- Android SDK Platform 34
- Android Build Tools 34.0.0
- Android SDK Platform Tools
- Internet access for the first Gradle dependency resolution

### Android Studio

Open the repository root in Android Studio. Let Gradle sync, select a device or emulator running Android 8.0 or newer, and run the `app` configuration.

If `local.properties` is absent, create it with your SDK path:

```properties
sdk.dir=C\:\\Android\\Sdk
```

`local.properties` is ignored by Git and must remain machine specific.

### Command line

```bash
./gradlew assembleDebug
```

The debug APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Run unit tests:

```bash
./gradlew testDebugUnitTest
```

Run lint and assemble everything checked by CI:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Install to a connected device with developer options and USB debugging enabled:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Permissions

| Permission | Why it is requested |
| --- | --- |
| `ACCESS_WIFI_STATE` | Read Wi Fi state and connection information. |
| `CHANGE_WIFI_STATE` is intentionally absent | WiFiLab never changes Wi Fi configuration. |
| `ACCESS_NETWORK_STATE` | Read link properties and the active network. |
| `INTERNET` | Required for local TCP reachability checks during an authorized audit. No research data is uploaded. |
| `ACCESS_FINE_LOCATION` up to API 32 | Android requires location permission for Wi Fi scan results on older releases. |
| `NEARBY_WIFI_DEVICES` from API 33 | Required for Wi Fi scanning with the `neverForLocation` declaration. |
| `ACCESS_COARSE_LOCATION` up to API 28 | Compatibility support for older scan permission behavior. |

WiFiLab does not request location in the background, does not store coordinates and does not map scan results.

## Privacy and responsible use

- All scans, audits, captures and educational progress are local by default.
- Android cloud backup is disabled.
- There is no account system, analytics SDK, advertising SDK or remote API.
- Imported captures are copied into app private storage and parsed locally.
- Network Auditor accepts only private IPv4 ranges and requires explicit authorization.
- Attack simulations are fixed narratives and do not transmit traffic.

Read [docs/PRIVACY.md](docs/PRIVACY.md) and [docs/ETHICS.md](docs/ETHICS.md) for more detail.

## Known limitations

- Android hides some Wi Fi fields and throttles scans. Results vary by device and OS.
- The current security of a connected network is not a public Android API on every release, so it may show as unavailable.
- A non root app cannot reliably read the ARP table, use monitor mode or access raw 802.11 frames.
- Local discovery depends on ICMP behavior and a small TCP probe set. Silent hosts can be missed.
- The PCAP parser focuses on Ethernet and common IP protocols. Radiotap and encrypted 802.11 payload analysis are out of scope.
- EAPOL metadata is descriptive only. WiFiLab does not derive PMK, PTK, GTK or plaintext keys.

## Testing

The JVM test suite covers:

- Wi Fi security capability and channel parsing
- signal quality mapping
- subnet validation and host enumeration
- baseline comparison behavior
- PCAP and PCAPNG parsing
- EAPOL four way handshake metadata and PMKID detection
- ARP and DNS metadata extraction

CI runs unit tests, Android lint and a debug assembly on every push and pull request to `main`.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md). Security issues should follow [SECURITY.md](SECURITY.md).

## License

WiFiLab is free software licensed under the GNU General Public License v3.0. See [LICENSE](LICENSE).

```text
Copyright (C) 2026 SyntX34
```

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
