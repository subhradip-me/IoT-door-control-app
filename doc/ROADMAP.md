# Roadmap — DoorControl

## Legend
| Symbol | Meaning |
|---|---|
| ✅ | Done |
| 🔧 | Stub created — implementation pending |
| 📋 | Planned — not yet started |
| ❌ | Blocked |

---

## Phase 1 — Project Foundation ✅

- [x] Project created (minSdk 26, targetSdk 36, Java 11)
- [x] Material Design 3 dependency added
- [x] Base screen layouts created (Main, Login, Register, Forgot Password)
- [x] Hardcoded login flow working (`admin` / `admin123`)

---

## Phase 2 — Modular Architecture ✅

- [x] `core/base/` — `BaseActivity`, `BaseFragment`
- [x] `core/utils/` — `Constants`, `PermissionUtils`, `BluetoothUtils`
- [x] `core/callbacks/` — `DeviceClickListener`, `BluetoothStateListener`
- [x] `core/models/` — `BluetoothDeviceModel`
- [x] `core/di/` — `AppModule` stub
- [x] `navigation/AppNavigator` — centralized navigation
- [x] All Activities moved to feature packages
- [x] `AndroidManifest.xml` updated to modular paths
- [x] Auth, Home, Bluetooth, Settings packages scaffolded

---

## Phase 3 — Auth Feature 🔧

### Backend / API
- [ ] Set up backend (or Firebase Auth)
- [ ] Configure Retrofit + OkHttp in `AppModule`
- [ ] Implement `AuthApiService` endpoints (login, register, reset)

### Data Layer
- [ ] Implement `AuthRepositoryImpl.login()`
- [ ] Implement `AuthRepositoryImpl.register()`
- [ ] Implement `AuthRepositoryImpl.resetPassword()`
- [ ] Implement `AuthPreferences.saveToken()` / `getToken()` / `clearToken()`

### Domain Layer
- [ ] Implement `LoginUseCase.execute()`
- [ ] Implement `RegisterUseCase.execute()`

### Presentation Layer
- [ ] Implement `AuthViewModel` with `LiveData<AuthState>`
- [ ] Wire `LoginActivity` → `AuthViewModel`
- [ ] Wire `RegisterActivity` → `AuthViewModel`
- [ ] Wire `ForgotPasswordActivity` → `AuthViewModel`
- [ ] Add loading indicator during API calls
- [ ] Handle error states (wrong credentials, network error, validation)

### Navigation
- [ ] On login success → `AppNavigator.goToHome()`
- [ ] On logout → `AppNavigator.goToLogin()` (clear stack)

---

## Phase 4 — Home / Dashboard Feature 📋

- [ ] Design `activity_home.xml` layout
- [ ] Implement `HomeActivity` (door status card, quick action buttons)
- [ ] Implement `HomeViewModel` with `LiveData`
- [ ] Implement `GetUserDataUseCase`
- [ ] Implement `HomeRepositoryImpl`
- [ ] Enable `AppNavigator.goToHome()`

---

## Phase 5 — Bluetooth Feature 📋

### Permissions
- [ ] Add to `AndroidManifest.xml`:
  - `BLUETOOTH_SCAN` (API 31+)
  - `BLUETOOTH_CONNECT` (API 31+)
  - `ACCESS_FINE_LOCATION` (required for BLE scan)
- [ ] Implement `PermissionUtils.hasBluetoothPermissions()`
- [ ] Request permissions at runtime before scan

### Scan
- [ ] Implement `BluetoothManager.startScan()` using `BluetoothLeScanner`
- [ ] Populate `BluetoothDeviceModel` list from scan results
- [ ] Implement `ScanDevicesUseCase`
- [ ] Show device list in `BluetoothFragment` via `DeviceListAdapter`

### Connect / Disconnect
- [ ] Implement `BluetoothManager.connect(address)`
- [ ] Implement `BluetoothManager.disconnect()`
- [ ] Implement `ConnectDeviceUseCase` + `DisconnectDeviceUseCase`
- [ ] Show connection state in `BluetoothViewModel` LiveData
- [ ] Navigate to `DeviceDetailsFragment` on connect

### Commands
- [ ] Define command byte protocol (e.g., `0x01` = OPEN, `0x00` = CLOSE)
- [ ] Implement `BluetoothManager.sendData(byte[])`
- [ ] Implement `SendDataUseCase`
- [ ] Wire OPEN / CLOSE buttons in `DeviceDetailsFragment`

### Background Service
- [ ] Implement `BluetoothService` as a bound/foreground service
- [ ] Show persistent notification while connected
- [ ] Auto-reconnect on unexpected disconnect

---

## Phase 6 — Settings Feature 📋

- [ ] Design `activity_settings.xml`
- [ ] Implement `SettingsActivity`
- [ ] Theme toggle (Light / Dark)
- [ ] Push notification preferences
- [ ] Account management (logout, delete account)
- [ ] Paired device management

---

## Phase 7 — Polish & Production 📋

- [ ] Replace hardcoded `admin`/`admin123` login
- [ ] Add proper error handling everywhere (network, BT, validation)
- [ ] Add loading states (shimmer / progress indicators)
- [ ] Unit tests for all UseCases
- [ ] Instrumentation tests for auth flows
- [ ] ProGuard / R8 rules configured
- [ ] Introduce Hilt for Dependency Injection
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Play Store release build

---

## Immediate Next Steps

1. **Implement Auth feature** — wire `AuthViewModel` → `LoginUseCase` → `AuthRepositoryImpl` (Phase 3)
2. **Build HomeActivity layout** — enable `AppNavigator.goToHome()` (Phase 4)
3. **Add Bluetooth permissions** to manifest (start of Phase 5)
