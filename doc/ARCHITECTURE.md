# Architecture — DoorControl

## Overview

DoorControl follows **Clean Architecture** — a layered design pattern that enforces strict separation of concerns and makes each layer independently testable and replaceable.

```
┌─────────────────────────────────────────────┐
│              Presentation Layer             │  ← UI (Activities, Fragments, ViewModels, Adapters)
├─────────────────────────────────────────────┤
│               Domain Layer                  │  ← Business Logic (UseCases, Repository Interfaces)
├─────────────────────────────────────────────┤
│                Data Layer                   │  ← Data Sources (API, SharedPrefs, Bluetooth HW)
└─────────────────────────────────────────────┘
```

### The Golden Rule
> **Dependencies only flow inward.** Data → Domain → Presentation. The Domain layer knows nothing about Android, UI, or databases.

---

## Layer Responsibilities

### 1. Presentation Layer
**Package:** `features/<name>/presentation/`

Responsible for everything the user sees and interacts with.

| Class Type | Responsibility |
|---|---|
| `Activity` / `Fragment` | Inflate layout, observe ViewModel, handle click events |
| `ViewModel` | Hold UI state, call UseCases, expose `LiveData` |
| `Adapter` | Bind data to `RecyclerView` items |
| `Service` | Background operations (e.g., Bluetooth GATT connection) |

**Rules:**
- Activities/Fragments never call Repositories directly
- All Android lifecycle logic stays here
- ViewModels survive configuration changes (rotation)

---

### 2. Domain Layer
**Package:** `features/<name>/domain/`

The heart of the app. Pure Java — zero Android imports.

| Class Type | Responsibility |
|---|---|
| `UseCase` | One single business action (e.g., `LoginUseCase.execute()`) |
| `Repository` (interface) | Contract that the data layer must fulfill |
| `Model` (future) | Domain-specific data objects |

**Rules:**
- No Android SDK imports whatsoever
- One UseCase = one action
- Interfaces here, implementations in the data layer

---

### 3. Data Layer
**Package:** `features/<name>/data/`

Knows how and where data is stored/fetched.

| Class Type | Responsibility |
|---|---|
| `RepositoryImpl` | Implements the domain Repository interface |
| `ApiService` | Retrofit interface for remote HTTP calls |
| `Preferences` | SharedPreferences read/write wrapper |
| `Manager` / `Source` | Hardware abstraction (e.g., `BluetoothManager`) |

**Rules:**
- Implements the domain-defined Repository interface
- Decides whether to use remote, local, or hardware data
- Maps raw API/hardware responses to domain models

---

## Data Flow — Login Example

```
User taps "Log In"
        │
        ▼
LoginActivity.handleLogin()          [Presentation]
        │  calls
        ▼
AuthViewModel.login(email, pass)     [Presentation]
        │  calls
        ▼
LoginUseCase.execute(email, pass)    [Domain]
        │  calls interface
        ▼
AuthRepository.login(email, pass)    [Domain - Interface]
        │  implemented by
        ▼
AuthRepositoryImpl.login(...)        [Data]
        │  calls
        ├──► AuthApiService (Retrofit HTTP POST)
        │
        └──► AuthPreferences (save token on success)
        │
        ▼
Result bubbles back up via LiveData
        │
        ▼
LoginActivity observes → navigate to Home or show error
```

---

## Navigation Architecture

All screen transitions go through **`AppNavigator`** — a single static utility class.

```
MainActivity ──► AppNavigator.goToLogin()
                        │
                        ▼
                  LoginActivity
                  ┌─────┴──────────┐
                  │                │
       goToRegister()   goToForgotPassword()
                  │
                  ▼ (on success)
           AppNavigator.goToHome()
                  │
                  ▼
           HomeActivity
```

**Why this pattern?**
- No Activity/Fragment holds a direct reference to another Activity class
- Navigation logic is centralized and easy to audit
- Easy to swap destinations (e.g., nav graph) without touching business logic

---

## Core Layer

**Package:** `core/`

Shared infrastructure used across all features. Nothing in `core/` depends on any feature.

```
core/
├── base/       → BaseActivity, BaseFragment
├── utils/      → Constants, PermissionUtils, BluetoothUtils
├── callbacks/  → DeviceClickListener, BluetoothStateListener (interfaces)
├── models/     → BluetoothDeviceModel (shared POJO)
└── di/         → AppModule (future DI wiring)
```

---

## Feature Isolation

Each feature (`auth`, `home`, `bluetooth`, `settings`) is **self-contained**:

```
features/auth/
├── presentation/   → UI only
├── domain/         → Business rules only
└── data/           → Data access only
```

Features communicate only through `core/` shared types or `AppNavigator`. They never import each other's internal classes.

---

## AndroidManifest Registration

All Activities must be declared in `AndroidManifest.xml` using their **fully-qualified relative package path**:

```xml
<!-- Entry Point (stays in root package) -->
<activity android:name=".MainActivity" />

<!-- Auth Feature -->
<activity android:name=".features.auth.presentation.ui.LoginActivity" />
<activity android:name=".features.auth.presentation.ui.RegisterActivity" />
<activity android:name=".features.auth.presentation.ui.ForgotPasswordActivity" />

<!-- Home Feature -->
<activity android:name=".features.home.presentation.ui.HomeActivity" />

<!-- Settings Feature -->
<activity android:name=".features.settings.presentation.ui.SettingsActivity" />
```

> The leading `.` is shorthand for the `applicationId` (`com.fasla.doorcontrol`).
