# Responsible Use and Ethics

WiFiLab is an educational and defensive wireless research tool. It is not a penetration testing platform and it does not provide attack capabilities.

## Authorization

Only scan or audit a network when you own it or have explicit permission from the person responsible for it. Authorization should define:

- which network or subnet is in scope
- who is performing the work
- when the work may occur
- what methods are allowed
- how findings will be stored and shared

The Network Auditor requires an explicit confirmation for each session. This confirmation is an ethical safeguard, not a legal determination.

## Deliberate boundaries

WiFiLab intentionally does not implement:

- deauthentication or disassociation frame injection
- beacon flooding or channel jamming
- rogue access points or evil twin hotspots
- ARP or DNS poisoning against live networks
- packet capture from other people's traffic
- monitor mode or root escalation
- exploitation of discovered services
- automatic blocking or modification of network devices

Attack Anatomy is a set of fixed educational narratives. The screens state that no traffic is generated.

## Packet capture handling

Captures can contain:

- device addresses and names
- DNS queries and application metadata
- authentication metadata
- personal or business communications
- data belonging to people who did not consent to analysis

Use synthetic captures for classes and demonstrations. If you must inspect a real capture, collect the minimum needed, keep it local, restrict access and delete it when the work is complete.

Do not upload captures to third party services without informed authorization from everyone whose data may be present.

## Findings and disclosure

If you discover a vulnerable device or network, report it privately to the owner. Do not publish identifiable network information, credentials, personal data or exploitation details.

For vulnerabilities in WiFiLab itself, follow [SECURITY.md](../SECURITY.md).

## Education and honesty

Security education should build understanding rather than fear. Avoid overstating results:

- a responsive service is not proof of a vulnerable device
- signal strength is not a trust signal
- an SSID is not an identity

WiFiLab labels limitations in the interface and documentation so learners can reason about uncertainty.

## Privacy by default

Research data stays on the device by default. There is no telemetry, advertising, remote analysis or capture upload service. Users control local deletion from Settings.

## Reporting misuse

Do not use this project to harass, monitor or gain unauthorized access to others. Maintainers may decline contributions that weaken the ethical and safety boundaries described here.
