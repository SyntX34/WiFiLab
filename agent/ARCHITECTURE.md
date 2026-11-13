# WiFiLab Architecture

## Layer map

```text
app/src/main/java/com/syntx34/wifilab/
├── WiFiLabApplication.kt
├── MainActivity.kt
├── core/
│   ├── di/DatabaseModule.kt
│   ├── di/RepositoryModule.kt
│   └── util/TimeFormatter.kt
├── data/
│   ├── local/
│   │   ├── WiFiLabDatabase.kt
│   │   ├── dao/
│   │   └── entity/
│   ├── network/AndroidNetworkAuditor.kt
│   ├── packet/
│   │   ├── ByteReader.kt
│   │   ├── DnsParser.kt
│   │   ├── EapolParser.kt
│   │   ├── FrameParser.kt
│   │   ├── PcapParser.kt
│   │   └── DemoCaptureFactory.kt
│   ├── preferences/SettingsRepositoryImpl.kt
│   ├── repository/
│   └── wifi/
├── domain/
│   ├── content/
│   ├── model/
│   ├── repository/
│   ├── security/
│   └── usecase/
└── ui/
    ├── attack/
    ├── auditor/
    ├── components/
    ├── dashboard/
    ├── lab/
    ├── learning/
    ├── navigation/
    ├── packet/
    ├── password/
    ├── permissions/
    ├── scanner/
    ├── settings/
    └── theme/
```

## Layer responsibilities

### Domain

Contains immutable models, repository contracts, pure security logic, educational content and use cases. Pure logic is testable without Android:

- `WifiCapabilityParser`
- `SubnetCalculator`
- `BaselineComparator`
- `AuditRecommendations`
- `PasswordEntropy`

### Data

Implements the domain contracts:

- `AndroidWifiRepository` uses `WifiManager` and `ConnectivityManager`.
- `AndroidNetworkAuditor` performs authorized reachability and TCP service checks.
- `PcapParser` and helper parsers read capture bytes.
- `LocalResearchRepository` exposes bundled educational content.
- Room implementations store audits, baseline, capture metadata and activity.
- `SettingsRepositoryImpl` uses DataStore.

All Hilt bindings are in `core/di`.

### UI

Compose screens, reusable components and Hilt view models. Each screen owns a view model where state or data access is required. Navigation is centralized in `ui/navigation`.

## View model state flow

Screens collect `StateFlow` using `collectAsStateWithLifecycle`. View models perform repository or use case work in `viewModelScope`. Long operations are cancellable. User input that is only relevant in memory, such as password text, is never written to Room or DataStore.

## Persistence schema

Room database `wifilab.db`, version 1:

- `audit_sessions`: audit timestamp, CIDR, device count, unexpected count and baseline used flag
- `audit_devices`: devices associated with a session, with foreign key cascade
- `baseline_devices`: trusted device identities
- `research_activity`: local activity trail
- `packet_captures`: imported file metadata

Schemas are exported to `app/schemas`.

DataStore file `wifi_lab_preferences` stores:

- scan cooldown seconds
- completed lesson or exercise ids

Capture files are stored under `filesDir/captures` with generated names.

## Wi Fi scanning flow

1. Scanner checks API specific permission.
2. Repository enforces a minimum request interval and checks permissions defensively.
3. Repository registers a receiver for `SCAN_RESULTS_AVAILABLE_ACTION`.
4. Repository calls `WifiManager.startScan`.
5. A result broadcast or timeout completes a suspend call.
6. `ScanResultMapper` maps Android ScanResult objects into domain models.
7. The view model filters, sorts and starts a configurable cooldown.

The app never starts a background scan and never changes Wi Fi configuration.

## Network audit flow

1. User enters or accepts a private CIDR and confirms authorization.
2. `SubnetCalculator` validates prefix range and RFC 1918 scope.
3. `NetworkAuditUseCase` loads the local baseline and calls the audit service.
4. Service enumerates host addresses with a concurrency semaphore.
5. Each host gets a reachability attempt and conservative TCP connect probes on ports 22, 53, 80, 443, 445, 554, 8080 and 8443.
6. `BaselineComparator` separates unexpected addresses.
7. `AuditRecommendations` builds defensive guidance.
8. Report is persisted and activity is recorded.

No banner grabbing, login attempts, UDP scans or exploitation occurs.

## Capture parsing flow

`PcapParser` detects classic PCAP or PCAPNG by magic bytes.

Classic PCAP parsing handles:

- little and big endian
- microsecond and nanosecond timestamps
- link type from the global header
- bounded packet lengths and packet count

PCAPNG parsing handles:

- section byte order changes
- interface description blocks and timestamp resolution options
- enhanced packet blocks
- simple packet blocks
- block trailer validation

`FrameParser` handles Ethernet, VLAN, raw IP, Linux cooked, Linux cooked v2, IPv4, IPv6, ARP, TCP, UDP, ICMP and DNS. `EapolParser` identifies four way handshake messages and metadata. `DnsParser` safely follows compression pointers.

All parsing happens locally. Encrypted EAPOL key data is not decrypted.

## Educational content

Content lives under `domain/content` and is exposed by `LocalResearchRepository`. This keeps lessons searchable and available offline. Security Lab protocol content is also mapped into Learning Center entries.

## Navigation routes

Routes are string constants in `ui/navigation/Destinations.kt`.

Top level:

- dashboard
- scanner
- auditor
- security_lab
- learning_center

Nested:

- packet_analyzer
- password_lab
- attack_anatomy
- settings

Bottom navigation hides on nested routes and the top bar shows a back action.

## Dependency injection

`DatabaseModule` provides the Room database and DAOs.

`RepositoryModule` binds:

- Wi Fi repository
- network audit service
- audit repository
- activity repository
- packet capture repository
- settings repository
- research repository
- password entropy engine

View models use `@HiltViewModel` and constructor injection.

## Error handling rules

- Platform permission failures are surfaced as explicit states.
- Unsupported device capability is not treated as an exception in the UI.
- Capture parsing errors are shown to the user with a safe message.
- Cancellation must propagate through use cases and must not be converted into a failure result.
- The UI must never claim success before persistence completes.
