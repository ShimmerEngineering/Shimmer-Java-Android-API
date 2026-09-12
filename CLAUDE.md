# Shimmer-Java-Android-API

Java driver and API for Shimmer devices, shared by PC and Android consumers.

## Build
**JDK 11**, **Gradle 8.10.2**. Each subproject carries its own wrapper — there is none at the repo root:
```
cd ShimmerDriverPC && ./gradlew build -i
```
CI (`gradle.yml`) builds **only `ShimmerDriverPC`**, and only on `master` / PRs into it. That means a
change to another subproject can merge without ever being compiled by CI — build the affected
subproject locally before you claim it works.

Test results land in `**/build/test-results/test/*.xml`.

## Subprojects
| Project | Role |
|---|---|
| `ShimmerDriver` | Core, platform-neutral driver |
| `ShimmerDriverPC` | PC-side driver — the only one CI builds |
| `ShimmerBluetoothManager` | Connection management |
| `ShimmerLSL` | Lab Streaming Layer integration |
| `JavaShimmerConnect`, `ShimmerTCP`, `ShimmerTCPExample` | Connectivity apps/examples |
| `ShimmerPCBasicExamples` | Start here for usage — `SensorMapsExample`, `ShimmerPCExample` |

## Eclipse-bound
Every subproject has a `.classpath`/`.project`. The Eclipse workspace binds them by absolute path,
so the repo cannot be relocated. Two checkouts of this repo exist on this machine — one standalone,
one as a submodule of `ASM_PC` — so confirm which one is in play before editing.

## Consumed as a submodule
`ASM_PC` includes this repo at its root. A driver change can break ASM_PC's build; check there too.

## API conventions
The README documents a long-running deprecation: state and data are delivered via
`ShimmerBluetooth.MSG_IDENTIFIER_*` handler messages, **not** the old `Shimmer.MESSAGE_*` /
`Shimmer.STATE_*` constants. Follow the current form in new code.
