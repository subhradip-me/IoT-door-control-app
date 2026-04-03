# DoorControl — Project Documentation

## Table of Contents

| Document | Description |
|---|---|
| [ARCHITECTURE.md](./ARCHITECTURE.md) | Clean Architecture layers, data flow, design decisions |
| [MODULES.md](./MODULES.md) | Every package and file explained |
| [FEATURE_GUIDE.md](./FEATURE_GUIDE.md) | Step-by-step guide to adding a new feature |
| [ROADMAP.md](./ROADMAP.md) | Current status and upcoming work |

---

## Project Overview

**DoorControl** is an Android IoT application that allows users to control a physical door lock via Bluetooth. Users authenticate via the app, then scan for and connect to their paired Bluetooth device to send open/close commands.

### Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 11 |
| Min SDK | API 26 (Android 8.0 Oreo) |
| Target SDK | API 36 |
| UI Framework | AndroidX + Material Design 3 |
| Architecture | Clean Architecture (Presentation → Domain → Data) |
| Navigation | Custom `AppNavigator` (centralized) |
| DI | Manual (Hilt/Dagger planned) |
| Networking | Retrofit (planned) |
| Bluetooth | Android BLE API (planned) |

### Application ID
```
com.fasla.doorcontrol
```

---

## Quick Start

### Prerequisites
- Android Studio Hedgehog or later
- JDK 11+
- Android device or emulator with API 26+

### Build & Run
```bash
# Debug build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run unit tests
./gradlew test
```

### Current Login (Hardcoded — Dev Only)
| Field | Value |
|---|---|
| Email | `admin` |
| Password | `admin123` |

> Replace with real auth once `AuthRepository` is wired to a backend.
