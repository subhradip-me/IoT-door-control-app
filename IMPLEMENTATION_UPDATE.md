# Bluetooth Implementation Update

**Branch:** `feature/bluetooth-implementation`
**Last Updated:** 2026-04-03
**Status:** Core stack + Home screen + BT test screen complete — awaiting team input before continuing

---

## What Has Been Built

A full **Classic Bluetooth SPP (Serial Port Profile)** stack plus a working Home screen and Bluetooth test screen. The app launches directly into the Home screen (auth bypassed for dev), shows a paired device list immediately, and can attempt connections to any Classic Bluetooth device.

---

## Technology Choice

### Why Classic Bluetooth SPP (not BLE)?

The hardware will be an **ESP32 using the `BluetoothSerial` Arduino library**, which creates a Classic Bluetooth device with the standard SPP UUID (`00001101-0000-1000-8000-00805F9B34FB`). This is essentially a wireless serial port.

| Consideration | Classic SPP | BLE |
|---|---|---|
| ESP32 library | `BluetoothSerial.h` (simple) | GATT services (complex) |
| Android API | `BluetoothSocket` / streams | `BluetoothGatt` / characteristics |
| Data model | Text stream (`Serial.println`) | Packets with service/characteristic UUIDs |
| Chosen? | ✅ Yes | ❌ Not needed now |

The `BluetoothRepository` interface already abstracts the transport — if BLE is needed later, a `BleRepositoryImpl` can be swapped in behind the same interface with zero UI changes.

---

## Command Protocol

A **newline-terminated text protocol** over Classic BT SPP.

### Android → ESP32
| Constant | Value | Action |
|---|---|---|
| `CMD_OPEN` | `"OPEN\n"` | Unlock / open door |
| `CMD_CLOSE` | `"CLOSE\n"` | Lock / close door |
| `CMD_STATUS` | `"STATUS\n"` | Request current door state |
| `CMD_PING` | `"PING\n"` | Keep-alive check |

### ESP32 → Android
| Constant | Value | Meaning |
|---|---|---|
| `RESP_OK` | `"OK"` | Command executed |
| `RESP_DOOR_OPEN` | `"DOOR_OPEN"` | Door is currently open |
| `RESP_DOOR_CLOSED` | `"DOOR_CLOSED"` | Door is currently locked |
| `RESP_PONG` | `"PONG"` | Response to PING |
| `RESP_ERROR` | `"ERROR"` | Hardware fault on device |

---

## Testing Without ESP32

The implementation is fully testable with any Classic Bluetooth device (headphones, laptops, other phones):

| Feature | Testable? | How |
|---|---|---|
| Launch → Home screen | ✅ Yes | Auth bypassed — app goes directly to Home |
| Load paired devices | ✅ Yes | `loadBondedDevices()` runs on Bluetooth screen open |
| Scan for devices | ✅ Yes | Works with any nearby BT device |
| Attempt connection | ✅ Partial | Fails gracefully on non-SPP devices with clear message |
| Connection state UI | ✅ Yes | CONNECTING → ERROR flow fully visible |
| Home screen OPEN/CLOSE buttons | ✅ Partial | Disabled until CONNECTED (connection state reflected) |
| Send/Receive commands | ❌ Needs ESP32 | SPP stream not available on headphones |

### Connection fallback strategy
```
connect() tries:
  1. createRfcommSocketToServiceRecord()          ← Secure (standard for ESP32)
  2. createInsecureRfcommSocketToServiceRecord()  ← Insecure fallback (for testing)
```

Non-SPP devices show:
> _"Device refused connection — it may not support SPP (e.g., headphones use A2DP, not SPP)."_

---

## Full Architecture Flow

```
User action (tap "Scan" / tap device / tap "OPEN")
        │
        ▼
BluetoothActivity / HomeActivity              [Presentation - UI]
        │  observes LiveData / calls methods
        ▼
BluetoothViewModel                            [Presentation - ViewModel]
        │  calls UseCases
        ▼
ScanDevicesUseCase / ConnectDeviceUseCase     [Domain]
SendDataUseCase / DisconnectDeviceUseCase
GetBondedDevicesUseCase
        │  calls interface
        ▼
BluetoothRepository (interface)               [Domain - Contract]
        │  implemented by
        ▼
BluetoothRepositoryImpl                       [Data]
        │  delegates to
        ▼
BluetoothManager                              [Data - Hardware Abstraction]
        │
        ▼
Android BluetoothAdapter / BluetoothSocket    [OS]
```

---

## All Files Changed / Added

### Session 1 — Core BT Stack

#### New Files
| File | Purpose |
|---|---|
| `core/bluetooth/CommandProtocol.java` | All `CMD_*` and `RESP_*` string constants |
| `core/bluetooth/ConnectionState.java` | Enum: `DISCONNECTED`, `SCANNING`, `CONNECTING`, `CONNECTED`, `ERROR` |
| `core/callbacks/ScanResultListener.java` | `onDeviceFound()`, `onScanFinished()`, `onScanError()` |
| `features/bluetooth/domain/usecase/GetBondedDevicesUseCase.java` | Returns already-paired devices — primary dev-time test mechanism |

#### Modified Files
| File | What Changed |
|---|---|
| `AndroidManifest.xml` | Added all BT permissions (API 30- and 31+), `WAKE_LOCK`, `FOREGROUND_SERVICE`, registered `BluetoothService` and `BluetoothActivity` |
| `core/callbacks/BluetoothStateListener.java` | Added `onDataReceived(String data)` for incoming ESP32 messages |
| `core/models/BluetoothDeviceModel.java` | Added `bonded` boolean field + 3-arg constructor |
| `core/utils/BluetoothUtils.java` | Implemented: `isBluetoothEnabled()`, `isBluetoothSupported()`, `hasClassicBluetooth()` |
| `core/utils/PermissionUtils.java` | API-level-aware permission arrays, `hasBluetoothPermissions()`, `requestBluetoothPermissions()`, `allGranted()` |
| `features/bluetooth/data/source/BluetoothManager.java` | **Full Classic SPP implementation** — see logic section below |
| `features/bluetooth/domain/repository/BluetoothRepository.java` | Added `getBondedDevices()` method |
| `features/bluetooth/data/repository/BluetoothRepositoryImpl.java` | Implements all interface methods via `BluetoothManager` |
| `features/bluetooth/domain/usecase/ScanDevicesUseCase.java` | Implemented `execute(ScanResultListener)` and `stopScan()` |
| `features/bluetooth/domain/usecase/ConnectDeviceUseCase.java` | Implemented `execute(address, listener)` |
| `features/bluetooth/domain/usecase/DisconnectDeviceUseCase.java` | Implemented `execute()` |
| `features/bluetooth/domain/usecase/SendDataUseCase.java` | Implemented `execute(String command)` |
| `features/bluetooth/presentation/viewmodel/BluetoothViewModel.java` | **Full implementation** — 7 LiveData streams, all use cases wired, response parsing |

---

### Session 2 — Home Screen + Bluetooth Test UI

#### New Files
| File | Purpose |
|---|---|
| `features/home/presentation/ui/HomeActivity.java` | Home dashboard: greeting, door status card, OPEN/CLOSE buttons, BT card |
| `features/bluetooth/presentation/ui/BluetoothActivity.java` | Scan + connect test screen: device list, state banner, disconnect |
| `features/bluetooth/presentation/adapter/DeviceListAdapter.java` | RecyclerView adapter: name, MAC, paired badge, click delegation |
| `res/layout/activity_home.xml` | Home screen layout |
| `res/layout/activity_bluetooth.xml` | Bluetooth scan/connect screen layout |
| `res/layout/item_device.xml` | Device list row: BT icon bubble, name, MAC, paired badge, chevron |
| `res/drawable/ic_bluetooth.xml` | Bluetooth vector icon |
| `res/drawable/ic_chevron_right.xml` | Navigation arrow |
| `res/drawable/ic_settings.xml` | Settings gear icon |
| `res/drawable/bg_icon_btn.xml` | Circular background for icon buttons |
| `res/drawable/bg_status_dot.xml` | Coloured status indicator dot |

#### Modified Files
| File | What Changed |
|---|---|
| `MainActivity.java` | **Auth bypassed** — calls `AppNavigator.goToHome()` + `finish()` immediately |
| `navigation/AppNavigator.java` | `goToHome()` activated, `goToBluetooth()` added |
| `res/values/colors.xml` | Added status colors: `status_open`, `status_open_bg`, `bt_connected`, `bt_scanning`, `bt_connecting`, `bt_error`, `bt_disconnected`, `surface_card` |
| `app/build.gradle` | Added `androidx.recyclerview:recyclerview:1.3.2` dependency |

---

## Key Logic Details

### BluetoothManager — Core Engine
```
startScan()
  → Register BroadcastReceiver (ACTION_FOUND + ACTION_DISCOVERY_FINISHED)
  → adapter.startDiscovery()
  → Each device: build BluetoothDeviceModel(name, address, bonded)
  → Post to main thread

connect(address)
  → Cancel discovery (improves speed)
  → Background thread:
      tryConnect():
        Step 1 — createRfcommSocketToServiceRecord (secure)
        Step 2 — createInsecureRfcommSocketToServiceRecord (fallback)
      → Get InputStream + OutputStream
      → Post onConnected() to main thread
      → Start receive loop

startReceiveLoop()
  → Dedicated background thread ("bt-receive-thread")
  → while connected: inputStream.read(buffer)
  → StringBuilder accumulates chunks
  → Emits complete '\n'-terminated messages to main thread

sendCommand(command)
  → Short-lived background thread ("bt-send-thread")
  → outputStream.write(command.getBytes()) + flush()

getBondedDevices()
  → adapter.getBondedDevices() → List<BluetoothDeviceModel>
```

### BluetoothViewModel — LiveData Streams
| LiveData | Updated when |
|---|---|
| `connectionState` | Every state transition |
| `scannedDevices` | `loadBondedDevices()`, scan results, deduplication by MAC |
| `connectedAddress` | `onConnected()` / `onDisconnected()` |
| `connectedName` | `connect(address, name)` / `onDisconnected()` |
| `lastResponse` | Every `onDataReceived()` |
| `doorState` | `parseResponse()` for `DOOR_OPEN` / `DOOR_CLOSED` |
| `errorMessage` | `onError()` / `onScanError()` |

### HomeActivity — UI Logic
- **Greeting**: time-based (morning < 12, afternoon < 17, evening ≥ 17)
- **Door state card**: observes `doorState` LiveData → text + colour changes
- **Status dot**: observes `connectionState` → colour changes (green/blue/amber/red/grey)
- **OPEN/CLOSE buttons**: `enabled = (connectionState == CONNECTED)`, alpha 100%/38%
- **BT card**: navigates to `BluetoothActivity`

### BluetoothActivity — Behaviour
- Opens → requests permissions → calls `loadBondedDevices()`
- "Scan" button toggles scan / stop
- Device tap → `viewModel.connect(address, name)`
- State banner reflects all 5 `ConnectionState` values
- "Disconnect" button only visible when `CONNECTED`
- Error messages shown via `showToast()` from `BaseActivity`

---

## Auth Bypass

```java
// MainActivity.java — currently:
AppNavigator.goToHome(this);
finish();

// To re-enable auth (production):
// AppNavigator.goToLogin(this);
// finish();
```

---

## Pending — Waiting on Team Input

| Item | Blocked on |
|---|---|
| Real ESP32 connection testing | Hardware availability |
| Command byte values (if binary protocol needed) | Team / hardware decision |
| Auth backend integration | Backend URL / Firebase decision |
| `BluetoothService` foreground service | Confirm if background persistence is needed |
| `DeviceDetailsFragment` (dedicated connected device screen) | Team UX decision |
| `HomeActivity` ↔ `BluetoothActivity` shared state | Architecture decision: service vs application scope |

---

## Next Steps (when team confirms)

```
[ ] Test with real ESP32 using BluetoothSerial
[ ] Implement BluetoothService for background persistence
[ ] Wire HomeActivity door state to a persistent BT connection
[ ] Implement auth feature (LoginUseCase → AuthRepositoryImpl → API)
[ ] Build HomeActivity → DeviceDetailsFragment for door control
```
