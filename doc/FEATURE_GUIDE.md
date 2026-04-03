# Feature Implementation Guide — DoorControl

This guide walks you through the exact steps to implement any new feature from scratch, following the project's Clean Architecture pattern.

We'll use an imaginary **"Notifications"** feature as a running example.

---

## Step 0 — Understand the Layers

Every feature has three layers. Always build **bottom-up**:

```
Domain  →  Data  →  Presentation
```

Build the interface/contract first (Domain), then the implementation (Data), then the UI (Presentation).

---

## Step 1 — Create the Domain Repository Interface

**Location:** `features/<name>/domain/repository/`

Define the *contract* — what operations this feature needs, without caring how they're done.

```java
// features/notifications/domain/repository/NotificationRepository.java
package com.fasla.doorcontrol.features.notifications.domain.repository;

import java.util.List;

public interface NotificationRepository {
    List<String> getNotifications();
    void markAllRead();
}
```

> ✅ **No Android imports. No implementation details. Pure Java.**

---

## Step 2 — Create Use Cases

**Location:** `features/<name>/domain/usecase/`

One class per action. Keep each UseCase focused on a single responsibility.

```java
// features/notifications/domain/usecase/GetNotificationsUseCase.java
package com.fasla.doorcontrol.features.notifications.domain.usecase;

import com.fasla.doorcontrol.features.notifications.domain.repository.NotificationRepository;
import java.util.List;

public class GetNotificationsUseCase {

    private final NotificationRepository repository;

    public GetNotificationsUseCase(NotificationRepository repository) {
        this.repository = repository;
    }

    public List<String> execute() {
        return repository.getNotifications();
    }
}
```

---

## Step 3 — Implement the Data Layer

### 3a. Create a Remote/Local Data Source (if needed)

```java
// features/notifications/data/remote/NotificationsApiService.java
package com.fasla.doorcontrol.features.notifications.data.remote;

// TODO: Define Retrofit endpoints
public interface NotificationsApiService {
    // @GET("notifications") Call<List<NotificationDto>> getAll();
}
```

### 3b. Create the Repository Implementation

**Location:** `features/<name>/data/repository/`

```java
// features/notifications/data/repository/NotificationRepositoryImpl.java
package com.fasla.doorcontrol.features.notifications.data.repository;

import com.fasla.doorcontrol.features.notifications.domain.repository.NotificationRepository;
import java.util.List;
import java.util.Arrays;

public class NotificationRepositoryImpl implements NotificationRepository {

    // Inject ApiService / local DB here
    public NotificationRepositoryImpl() { }

    @Override
    public List<String> getNotifications() {
        // TODO: Call API or local DB
        return Arrays.asList("Door opened at 10:05 AM", "Low battery warning");
    }

    @Override
    public void markAllRead() {
        // TODO: Implement
    }
}
```

---

## Step 4 — Create the ViewModel

**Location:** `features/<name>/presentation/viewmodel/`

```java
// features/notifications/presentation/viewmodel/NotificationsViewModel.java
package com.fasla.doorcontrol.features.notifications.presentation.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fasla.doorcontrol.features.notifications.data.repository.NotificationRepositoryImpl;
import com.fasla.doorcontrol.features.notifications.domain.usecase.GetNotificationsUseCase;

import java.util.List;

public class NotificationsViewModel extends AndroidViewModel {

    private final MutableLiveData<List<String>> notifications = new MutableLiveData<>();
    private final GetNotificationsUseCase getNotificationsUseCase;

    public NotificationsViewModel(@NonNull Application application) {
        super(application);
        // Manual DI — swap for injected instance when Hilt is added
        getNotificationsUseCase = new GetNotificationsUseCase(new NotificationRepositoryImpl());
    }

    public LiveData<List<String>> getNotifications() {
        return notifications;
    }

    public void loadNotifications() {
        notifications.setValue(getNotificationsUseCase.execute());
    }
}
```

---

## Step 5 — Create the Activity / Fragment

**Location:** `features/<name>/presentation/ui/`

```java
// features/notifications/presentation/ui/NotificationsActivity.java
package com.fasla.doorcontrol.features.notifications.presentation.ui;

import android.os.Bundle;
import androidx.lifecycle.ViewModelProvider;

import com.fasla.doorcontrol.R;
import com.fasla.doorcontrol.core.base.BaseActivity;
import com.fasla.doorcontrol.features.notifications.presentation.viewmodel.NotificationsViewModel;

public class NotificationsActivity extends BaseActivity {

    private NotificationsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications); // create this layout

        viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);

        viewModel.getNotifications().observe(this, items -> {
            // TODO: Bind items to RecyclerView adapter
            showToast("Loaded " + items.size() + " notifications");
        });

        viewModel.loadNotifications();
    }
}
```

---

## Step 6 — Register in AndroidManifest

Open `app/src/main/AndroidManifest.xml` and add:

```xml
<!-- Notifications Feature -->
<activity android:name=".features.notifications.presentation.ui.NotificationsActivity" />
```

---

## Step 7 — Add Navigation in AppNavigator

Open `navigation/AppNavigator.java` and add:

```java
import com.fasla.doorcontrol.features.notifications.presentation.ui.NotificationsActivity;

/** Navigate to NotificationsActivity. */
public static void goToNotifications(Context context) {
    context.startActivity(new Intent(context, NotificationsActivity.class));
}
```

---

## Step 8 — Create the Layout

Create `res/layout/activity_notifications.xml` with your UI. Follow existing layouts as reference:
- `activity_login.xml` — form-style layout
- `activity_main.xml` — splash/onboarding style

---

## Checklist Summary

```
[ ] Step 1  — domain/repository/NotificationRepository.java (interface)
[ ] Step 2  — domain/usecase/GetNotificationsUseCase.java
[ ] Step 3a — data/remote/NotificationsApiService.java
[ ] Step 3b — data/repository/NotificationRepositoryImpl.java
[ ] Step 4  — presentation/viewmodel/NotificationsViewModel.java
[ ] Step 5  — presentation/ui/NotificationsActivity.java
[ ] Step 6  — AndroidManifest.xml entry
[ ] Step 7  — AppNavigator.goToNotifications()
[ ] Step 8  — res/layout/activity_notifications.xml
```

---

## Key Rules to Remember

| Rule | Why |
|---|---|
| Domain has zero Android imports | Keeps business logic unit-testable with plain JUnit |
| One UseCase = one action | Easy to test, easy to replace |
| All navigation goes through `AppNavigator` | Centralised, auditable |
| Activities extend `BaseActivity` | Gets `showToast()` and future helpers for free |
| Fragments extend `BaseFragment` | Same reason |
| Register every Activity in `AndroidManifest.xml` | App will crash at runtime without it |
