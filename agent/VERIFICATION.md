# Verification and Quality Bar

## Required commands

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

The task is not complete unless all three pass. A build that only compiles is not enough.

## Artifact checks

- Confirm the APK exists at `app/build/outputs/apk/debug/app-debug.apk`.
- Inspect the manifest to verify permissions and the application entry point.
- Confirm no analytics or remote research service dependency is present.
- Confirm imported capture paths resolve inside app private storage.
- Confirm all eight screens are reachable through navigation.
- Confirm deletion controls affect both database rows and capture files.

## Automated coverage

Existing JVM tests cover:

- Wi Fi security capability parsing, channel mapping and signal quality
- subnet validation, host enumeration and RFC 1918 restrictions
- password entropy, dictionary penalty, repeated patterns and synthetic recovery
- trusted baseline comparison
- synthetic classic PCAP parsing and four way handshake metadata
- PCAPNG enhanced packet block parsing
- ARP and DNS metadata
- malformed capture rejection

## Manual checks

Perform these on a physical device when possible:

1. Fresh install and first launch.
2. Deny nearby Wi Fi permission and confirm a useful explanation.
3. Grant permission and run a scan.
4. Trigger scan cooldown and confirm the countdown.
5. Disable location services on an older Android version and confirm the warning.
6. Audit a private test network with authorization confirmation.
7. Cancel an audit and confirm no report is saved.
8. Save a baseline and rerun the audit.
9. Import a small synthetic PCAP, search packets, open EAPOL detail and delete the capture.
10. Load the synthetic demo and verify no file is persisted.
11. Change scan cooldown in Settings and confirm the scanner honors it.
12. Mark a lesson complete and verify dashboard activity and settings count.
13. Clear local data and verify the counts and files are removed.
14. Rotate the device and confirm state survives on primary screens.
15. Use a large font and verify key layouts do not clip critical actions.

## Known gaps

- No instrumented UI test suite is included yet.
- Wi Fi behavior depends on device and Android version.
- The network auditor cannot detect silent hosts.
- Filtering and sorting run in memory, which is acceptable for a single home subnet.
- Capture parser coverage is intentionally limited to documented layers.

## Definition of done

- All required commands pass.
- New pure logic has unit tests.
- The feature does not violate the safety boundaries.
- Documentation is updated.
- No fake data or placeholder user experience is introduced.
- The app builds and the APK can be inspected or installed.
