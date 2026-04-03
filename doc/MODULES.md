# Module & Package Reference — DoorControl

All source lives under:
```
app/src/main/java/com/fasla/doorcontrol/
```

---

## Root

### `MainActivity.java`
- **Role:** App entry point / splash / onboarding screen
- **Extends:** `AppCompatActivity`
- **Behaviour:** Shows a "Get Started" button → calls `AppNavigator.goToLogin()`
- **Layout:** `res/layout/activity_main.xml`

---

## `core/` — Shared Infrastructure

Nothing inside `core/` depends on any feature. All features may depend on `core/`.

---

### `core/base/`

#### `BaseActivity.java`
- **Extends:** `AppCompatActivity`
- **Purpose:** Abstract parent for every Activity in the app
- **Current helpers:**
  - `showToast(String message)` — short Toast
- **TODO:** `hideKeyboard()`, `showLoadingDialog()`, `dismissLoadingDialog()`

#### `BaseFragment.java`
- **Extends:** `Fragment`
- **Purpose:** Abstract parent for every Fragment in the app
- **TODO:** Common fragment helpers (toast, navigation, lifecycle logging)

---

### `core/utils/`

#### `Constants.java`
- **Purpose:** App-wide constant values — Intent extra keys, SharedPrefs keys, timeouts, etc.
- **Pattern:** `final` class with `private` constructor — never instantiated
- **TODO:** Populate with keys as features are built (e.g., `EXTRA_DEVICE_ADDRESS`)

#### `PermissionUtils.java`
- **Purpose:** Runtime permission helpers for Bluetooth, Location, Camera
- **Pattern:** `final` utility class — all static methods
- **TODO:** `hasBluetoothPermissions(Activity)`, `requestBluetoothPermissions(Activity, int)`

#### `BluetoothUtils.java`
- **Purpose:** Low-level Bluetooth adapter helpers (is enabled? get adapter, etc.)
- **Pattern:** `final` utility class — all static methods
- **TODO:** `isBluetoothEnabled(Context)`, `getAdapter()`

---

### `core/callbacks/`

#### `DeviceClickListener.java`
- **Type:** Interface
- **Purpose:** Callback from `DeviceListAdapter` to `BluetoothFragment` when user taps a device
- **Method:** `void onDeviceClicked(BluetoothDeviceModel device)`

#### `BluetoothStateListener.java`
- **Type:** Interface
- **Purpose:** Callback from `BluetoothService`/`BluetoothManager` to notify connection state changes
- **Methods:**
  - `void onConnected(String deviceAddress)`
  - `void onDisconnected(String deviceAddress)`
  - `void onError(String errorMessage)`

---

### `core/models/`

#### `BluetoothDeviceModel.java`
- **Type:** POJO
- **Purpose:** Represents a discovered Bluetooth device, shared between the bluetooth feature's data and presentation layers
- **Fields:** `String name`, `String address`
- **TODO:** Add `int rssi`, `int bondState`, `BluetoothDevice rawDevice`

---

### `core/di/`

#### `AppModule.java`
- **Purpose:** Future Dependency Injection wiring
- **TODO:** Wire `AuthRepository`, `BluetoothRepository`, `ApiService` instances when Hilt or manual DI is introduced

---

## `features/` — Feature Modules

---

## `features/auth/` — Authentication

Handles user login, registration, and password reset.

### Presentation Layer

#### `presentation/ui/LoginActivity.java`
- **Extends:** `BaseActivity`
- **Layout:** `res/layout/activity_login.xml`
- **Behaviour:**
  - Validates email + password fields
  - Hardcoded check: `admin` / `admin123` (dev only)
  - Navigates to `RegisterActivity` or `ForgotPasswordActivity` via direct Intent
  - **TODO:** Wire to `AuthViewModel` → `LoginUseCase`

#### `presentation/ui/RegisterActivity.java`
- **Extends:** `BaseActivity`
- **Layout:** `res/layout/activity_register.xml`
- **Behaviour:**
  - Validates name, email, password, confirm-password fields
  - Password match check
  - **TODO:** Wire to `AuthViewModel` → `RegisterUseCase`

#### `presentation/ui/ForgotPasswordActivity.java`
- **Extends:** `BaseActivity`
- **Layout:** `res/layout/activity_forgot_password.xml`
- **Behaviour:**
  - Validates email field
  - **TODO:** Wire to `AuthViewModel` → `AuthRepository.resetPassword()`

#### `presentation/viewmodel/AuthViewModel.java`
- **TODO:** Extend `AndroidViewModel`, inject `LoginUseCase` + `RegisterUseCase`, expose `LiveData<AuthState>`

---

### Domain Layer

#### `domain/repository/AuthRepository.java`
- **Type:** Interface
- **TODO Methods:**
  - `boolean login(String email, String password)`
  - `boolean register(String name, String email, String password)`
  - `boolean resetPassword(String email)`

#### `domain/usecase/LoginUseCase.java`
- **Purpose:** Encapsulates all login business logic
- **TODO:** Validate inputs, call `AuthRepository.login()`, return typed result

#### `domain/usecase/RegisterUseCase.java`
- **Purpose:** Encapsulates all registration business logic
- **TODO:** Validate inputs, call `AuthRepository.register()`, return typed result

---

### Data Layer

#### `data/remote/AuthApiService.java`
- **Type:** Interface (Retrofit)
- **TODO Endpoints:**
  - `@POST("auth/login")` → `Call<LoginResponse>`
  - `@POST("auth/register")` → `Call<RegisterResponse>`
  - `@POST("auth/reset-password")` → `Call<Void>`

#### `data/local/AuthPreferences.java`
- **Purpose:** Read/write auth token to `SharedPreferences`
- **Prefs File:** `auth_prefs`
- **TODO Methods:** `saveToken(String)`, `getToken()`, `clearToken()`

#### `data/repository/AuthRepositoryImpl.java`
- **Implements:** `AuthRepository`
- **TODO:** Inject `AuthApiService` + `AuthPreferences`, implement all interface methods

---

## `features/home/` — Home / Dashboard

Post-login screen. Shows door status, quick actions, device summary.

| File | Status | Notes |
|---|---|---|
| `presentation/ui/HomeActivity.java` | Stub | Layout not yet created |
| `presentation/viewmodel/HomeViewModel.java` | Stub | — |
| `data/repository/HomeRepositoryImpl.java` | Stub | — |
| `domain/usecase/GetUserDataUseCase.java` | Stub | — |

---

## `features/bluetooth/` — Bluetooth Control

Scans for BLE/Classic devices, connects to the door controller, sends open/close commands.

### Presentation Layer

| File | Purpose |
|---|---|
| `ui/BluetoothFragment.java` | Scan trigger + device list UI |
| `ui/DeviceDetailsFragment.java` | Connected device info + door commands |
| `adapter/DeviceListAdapter.java` | RecyclerView adapter for device list; uses `DeviceClickListener` |
| `service/BluetoothService.java` | Background `Service` managing the GATT connection |
| `viewmodel/BluetoothViewModel.java` | Exposes scan results + connection state via LiveData |

### Domain Layer

| File | Purpose |
|---|---|
| `domain/repository/BluetoothRepository.java` | Interface: `startScan`, `stopScan`, `connect`, `disconnect`, `sendData` |
| `domain/usecase/ScanDevicesUseCase.java` | Starts BLE scan, returns discovered devices |
| `domain/usecase/ConnectDeviceUseCase.java` | Connects to a device by MAC address |
| `domain/usecase/DisconnectDeviceUseCase.java` | Cleanly closes active connection |
| `domain/usecase/SendDataUseCase.java` | Sends byte command (e.g., `0x01` = OPEN, `0x00` = CLOSE) |

### Data Layer

| File | Purpose |
|---|---|
| `data/source/BluetoothManager.java` | Wraps `BluetoothAdapter`, handles scan callbacks and GATT ops |
| `data/repository/BluetoothRepositoryImpl.java` | Implements `BluetoothRepository` via `BluetoothManager` |

---

## `features/settings/` — Settings (Future)

App settings: theme, notifications, account management, device pairing management.

| File | Status |
|---|---|
| `presentation/ui/SettingsActivity.java` | Stub (no layout yet) |
| `data/SettingsDataPackage.java` | Package placeholder — delete when implementing |
| `domain/SettingsDomainPackage.java` | Package placeholder — delete when implementing |

---

## `navigation/`

#### `AppNavigator.java`
- **Pattern:** `final` class with `private` constructor — all static methods
- **Purpose:** Single source of truth for all screen transitions

| Method | Status | Behaviour |
|---|---|---|
| `goToLogin(Context)` | ✅ Active | Clears back stack, starts `LoginActivity` |
| `goToRegister(Context)` | ✅ Active | Starts `RegisterActivity` |
| `goToForgotPassword(Context)` | ✅ Active | Starts `ForgotPasswordActivity` |
| `goToHome(Context)` | 🔧 Stub | Commented — enable when `HomeActivity` layout exists |
