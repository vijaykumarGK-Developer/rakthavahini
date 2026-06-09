# 📖 Rakta-Vahini Documentation

> **Version:** 1.0.0 | **Last Updated:** June 2026

---

## Table of Contents

1. [Project Overview](#-project-overview)
2. [Architecture](#-architecture)
3. [Data Models & Firestore Schema](#-data-models--firestore-schema)
4. [State Management](#-state-management)
5. [Component Reference](#-component-reference)
6. [Firebase Setup Guide](#-firebase-setup-guide)
7. [Build & Configuration Reference](#-build--configuration-reference)
8. [Testing](#-testing)
9. [Contributing Guidelines](#-contributing-guidelines)
10. [Code of Conduct](#-code-of-conduct)
11. [Security Policy](#-security-policy)
12. [Changelog](#-changelog)
13. [FAQ](#-faq)
14. [Troubleshooting](#-troubleshooting)

---

## 📋 Project Overview

**Rakta-Vahini** (रक्तवाहिनी — "Blood Vessel" in Sanskrit) is a real-time Android application built with Kotlin and Jetpack Compose that connects blood donors with hospitals and individuals in urgent need. It uses Firebase Cloud Firestore for real-time synchronization and Firebase Authentication for user management.

### Purpose

Eliminate friction in blood emergencies by providing:
- Instant donor discovery using geolocation-based proximity matching
- Real-time emergency (SOS) broadcasting
- Verified hospital and blood bank directory
- Automated eligibility enforcement (90-day cooling period)
- Digital recognition through "Certificate of Heroism"

### Target Audience

- Individual blood donors
- Hospitals and blood banks
- Emergency medical coordinators

---

## 🏗 Architecture

### Pattern: MVI (Model-View-Intent)

Rakta-Vahini uses a lightweight **MVI-inspired architecture** centered around a single `AppState` container class. This approach provides unidirectional data flow, predictable state management, and easy integration with Jetpack Compose's reactive model.

```
┌─────────────────────────────────────────────────────────────────┐
│                        USER INTERFACE                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────────┐│
│  │SearchTab │  │ProfileTab│  │UsersTab  │  │HospitalsTab      ││
│  └─────┬────┘  └─────┬────┘  └─────┬────┘  └────────┬─────────┘│
│        │              │              │                │          │
│        └──────────────┴──────┬──────┴────────────────┘          │
│                              │                                   │
│                    ┌─────────▼──────────┐                       │
│                    │    AppState         │                       │
│                    │  (Single Source     │                       │
│                    │   of Truth)         │                       │
│                    └─────────┬──────────┘                       │
│                              │                                   │
│                    ┌─────────▼──────────┐                       │
│                    │  Firebase Layer    │                       │
│                    │  (Firestore + Auth)│                       │
│                    └────────────────────┘                       │
└─────────────────────────────────────────────────────────────────┘
```

### Layer Breakdown

| Layer | Component | File | Responsibility |
|---|---|---|---|
| **View** | Composables (Tabs, Screens, Overlays) | `MainActivity.kt` | Renders UI from `AppState`, dispatches user intents via callbacks. |
| **Model** | `AppState` class | `MainActivity.kt:125` | Holds all application state, manages Firebase listeners, executes business logic. |
| **Data** | Firebase Firestore + Auth | Firebase Console | Persistent storage, real-time sync, authentication. |
| **Theme** | `RaktaVahiniTheme` | `MainActivity.kt:274` | Material3 color schemes for light and dark modes. |

### Key Design Decisions

1. **Single `AppState` container** — Instead of multiple ViewModels, all state lives in one observable class, making data flow easy to trace and debug.
2. **Firebase snapshot listeners** — Real-time `addSnapshotListener` on Firestore collections keeps all connected devices in sync without polling.
3. **Preview mode** — `AppState(isPreview = true)` provides mock data for Android Studio previews, enabling rapid UI development.
4. **Haversine distance calculation** — Uses the Haversine formula (`MainActivity.kt:1102`) for accurate geographic distance measurement.
5. **Smart scoring** — Donors are ranked by a composite score: `distance + (responseSpeed × 0.1) + (activeDaysAgo × 0.5) - (freq × 0.2)`.

---

## 💾 Data Models & Firestore Schema

### Firestore Collections

```
firestore-database/
├── users/                          # Donor profiles
│   └── {uid}/
│       ├── uid: String             # Firebase Auth UID
│       ├── name: String            # Full name
│       ├── email: String           # Email address
│       ├── age: String             # Age
│       ├── gender: String          # Male / Female / Other
│       ├── group: String           # Blood group (O+, A-, etc.)
│       ├── phone: String           # Primary phone
│       ├── altPhone: String        # Alternate phone
│       ├── address: String         # Home address
│       ├── lat: Double             # Latitude (geolocation)
│       ├── lng: Double             # Longitude (geolocation)
│       ├── lastDonationMs: Long    # Last donation timestamp (ms)
│       ├── responseSpeed: Int      # Response speed rating
│       ├── freq: Int               # Lifetime donation count
│       └── activeDaysAgo: Int      # Days since last activity
│
├── hospitals/                      # Hospital & blood bank directory
│   └── {id}/
│       ├── id: String              # Auto-generated document ID
│       ├── name: String            # Hospital name
│       ├── address: String         # Physical address
│       ├── lat: Double             # Latitude
│       ├── lng: Double             # Longitude
│       ├── email: String           # Contact email
│       ├── phone: String           # Mobile number
│       └── landline: String        # Landline number
│
├── emergencies/                    # Active SOS requests
│   └── {id}/
│       ├── id: String              # Auto-generated document ID
│       ├── requesterId: String     # User ID who broadcasted
│       ├── bloodGroup: String      # Required blood group
│       ├── units: String           # Units needed
│       ├── lat: Double             # Requester latitude
│       ├── lng: Double             # Requester longitude
│       ├── status: String          # "ACTIVE" (default)
│       └── timestamp: Long         # Broadcast timestamp (ms)
│
└── donation_logs/                  # Donation activity feed
    └── {id}/
        ├── id: String              # Auto-generated document ID
        ├── userId: String          # Donor user ID
        ├── userName: String        # Donor display name
        ├── timestamp: Long         # Donation timestamp (ms)
        ├── bloodGroup: String      # Donor blood group
        └── hospitalName: String    # Hospital where donated
```

### Data Classes (Kotlin)

```kotlin
data class Donor(
    val uid: String = "",
    var name: String = "",
    var email: String = "",
    var age: String = "",
    var gender: String = "",
    var group: String = "",
    var phone: String = "",
    var altPhone: String = "",
    var address: String = "",
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    var lastDonationMs: Long = 0,
    var responseSpeed: Int = 0,
    var freq: Int = 0,
    var activeDaysAgo: Int = 0,
    // Transient fields (excluded from Firestore):
    @get:Exclude var distance: Double = 0.0,
    @get:Exclude var calcDist: Double = 0.0,
    @get:Exclude var smartScore: Double = 0.0,
    @get:Exclude var isEligible: Boolean = false
)

data class Hospital(
    val id: String = "",
    var name: String = "",
    var address: String = "",
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    var email: String = "",
    var phone: String = "",
    var landline: String = ""
)

data class DonationLog(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val timestamp: Long = 0,
    val bloodGroup: String = "",
    val hospitalName: String = ""
)

data class EmergencyRequest(
    val id: String = "",
    val requesterId: String = "",
    val bloodGroup: String = "",
    val units: String = "1",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val status: String = "ACTIVE",
    val timestamp: Long = 0
)
```

### Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read/write their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Hospitals are publicly readable, write requires auth
    match /hospitals/{hospitalId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
    
    // Donation logs are publicly readable
    match /donation_logs/{logId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
    
    // Emergencies are publicly readable
    match /emergencies/{emergencyId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

---

## 🎮 State Management

### AppState Class

The `AppState` class (`MainActivity.kt:125`) is the central state container for the entire application.

```kotlin
class AppState(val isPreview: Boolean = false) {
    // Theme & Navigation
    var isDarkTheme by mutableStateOf(false)
    var hasOnboarded by mutableStateOf(false)
    var isAuthenticated by mutableStateOf(true)
    var currentTab by mutableStateOf("search")  // "search" | "profile" | "hospitals" | "users" | "history"
    
    // Overlay visibility
    var showUserDetail by mutableStateOf<Donor?>(null)
    var showHospitalDetail by mutableStateOf<Hospital?>(null)
    var showSettings by mutableStateOf(false)
    var showProfileEdit by mutableStateOf(false)
    var showStaticPage by mutableStateOf<String?>(null)
    var showCertificate by mutableStateOf(false)
    var showEmergencySheet by mutableStateOf(false)
    
    // Data
    var currentUser by mutableStateOf(Donor(...))
    val enrolledUsers = mutableStateListOf<Donor>()
    val enrolledHospitals = mutableStateListOf<Hospital>()
    val activeEmergencies = mutableStateListOf<EmergencyRequest>()
    val globalLogs = mutableStateListOf<DonationLog>()
    var myLiveLocation by mutableStateOf<Pair<Double, Double>?>(null)
}
```

### Key Methods

| Method | Description |
|---|---|
| `loadInitialData()` | Initializes Firestore real-time listeners for all 4 collections. |
| `signOut()` | Signs out the current Firebase user and resets authentication state. |
| `updateUserProfile(donor)` | Updates the current user's Firestore document. |
| `registerDonor(donor)` | Saves a new donor document to Firestore. |
| `registerHospital(hospital)` | Saves a new hospital document to Firestore. |
| `logDonation(hospitalName)` | Creates a donation log entry and updates the donor's last donation timestamp. |
| `broadcastEmergency(group, units)` | Creates a new active SOS emergency request in Firestore. |

### State Flow Diagram

```
User Action (tap, swipe, input)
        │
        ▼
Composable dispatches intent
        │
        ▼
AppState method called
        │
        ▼
State updated (mutableStateOf / mutableStateListOf)
        │
        ▼
Compose recomposition triggered
        │
        ▼
UI re-renders with new state
```

---

## 🧩 Component Reference

### Screens & Tabs

| Composable | Route | Description | Key Features |
|---|---|---|---|
| `SplashScreen` | Initial | Animated splash with branding | Red background, app name, subtitle |
| `OnboardingScreen` | First-run | Welcome screen for new users | App description, "Get Started" button |
| `SearchTab` | `search` | Intelligent donor discovery | Blood group filter, radius slider, smart scoring, skeleton loading, live location |
| `ProfileTab` | `profile` | Dual registration form | Individual donor & hospital registration, donation logging |
| `UsersTab` | `users` | All registered donors | Eligibility badges (✅/🚫), click for details |
| `HospitalsTab` | `hospitals` | Hospital/blood bank directory | Card list with address, click for details |
| `HistoryTab` | `history` | Donation activity feed | User/hospital log toggle, blood badges, formatted dates |

### Overlay Screens

| Composable | Trigger | Description |
|---|---|---|
| `UserDetailScreen` | Click donor card | Full donor profile: blood badge, eligibility, address, contact, secure call |
| `HospitalDetailScreen` | Click hospital card | Hospital info: address, coordinates, email, phone, landline |
| `SettingsScreen` | Settings icon | Dark mode toggle, profile edit, About/Privacy/Support/Report pages |
| `ProfileEditScreen` | From settings | Edit personal details (name, age, gender, blood group, phone, address) |
| `CertificateScreen` | After donation log | "Certificate of Heroism" with recipient name, date, and certificate ID |
| `StaticPageScreen` | From settings | Renders About, FAQ, Privacy, or Support/Report content |
| `EmergencySOSSheet` | FAB (🚨) | Modal bottom sheet for SOS broadcast (blood group + units) |

### Shared UI Components

| Composable | File Location | Purpose |
|---|---|---|
| `RaktButton` | L994 | Branded primary button (red, full-width, rounded) |
| `RaktOutlinedButton` | L1001 | Branded outlined button (red border) |
| `CustomCard` | L979 | Reusable card container with optional click handler |
| `BloodBadge` | L987 | Colored badge displaying blood group (e.g., "O+") |
| `SlideOverlay` | L962 | Animated slide-in/out panel for detail screens |
| `SegmentedControl` | L1008 | Two-option toggle (e.g., Individual/Hospital) |
| `SkeletonCard` | L1042 | Shimmer loading placeholder for search results |
| `DonorListCard` | L1020 | Donor result card with "🌟 Best Match" badge |
| `calculateDistance` | L1102 | Haversine formula utility for geo-distance |

---

## 🔥 Firebase Setup Guide

### Prerequisites

- A Google account with access to [Firebase Console](https://console.firebase.google.com/)
- Android Studio Ladybug (2024.2) or newer

### Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/).
2. Click **Add project** and follow the wizard.
3. Disable Google Analytics if not needed.

### Step 2: Register Android App

1. In Project Settings → General, click **Add app** → **Android**.
2. Package name: `com.example.greetingcard`
3. App nickname: `Rakta-Vahini`
4. Download the `google-services.json` file.

### Step 3: Add google-services.json

1. Place the downloaded file at: `app/google-services.json`
2. The file structure should be:
```
rakthavahini/
└── app/
    └── google-services.json    ← Place here
```

### Step 4: Enable Firestore

1. In Firebase Console, go to **Firestore Database** → **Create database**.
2. Choose **Start in test mode** (for development).
3. Select a Cloud Firestore location closest to your users.

### Step 5: Enable Authentication

1. In Firebase Console, go to **Authentication** → **Sign-in method**.
2. Enable **Anonymous** sign-in (or Email/Password, Google, etc.).
3. For the current version, authentication is simplified and always authenticated.

### Step 6: Create Firestore Collections

Create the following collections (they will auto-populate as the app runs):

```bash
# Collections (no specific schema required — enforced by app code)
users/
hospitals/
emergencies/
donation_logs/
```

### Step 7: Deploy Security Rules

Copy the security rules from [Section 3](#firestore-security-rules) into the Firestore Rules tab and publish.

---

## 🔧 Build & Configuration Reference

### Build Scripts

| Script | Command | Description |
|---|---|---|
| Build Debug | `./gradlew assembleDebug` | Builds debug APK |
| Build Release | `./gradlew assembleRelease` | Builds release APK |
| Run Tests | `./gradlew test` | Runs unit tests |
| Clean | `./gradlew clean` | Cleans build artifacts |

### Gradle Configuration (`app/build.gradle.kts`)

```kotlin
android {
    namespace = "com.example.greetingcard"
    compileSdk = 36
    
    defaultConfig {
        applicationId = "com.example.greetingcard"
        minSdk = 24      // Android 7.0 Nougat
        targetSdk = 36   // Android 16
        versionCode = 1
        versionName = "1.0"
    }
    
    buildFeatures {
        compose = true
    }
}
```

### Dependency Versions (`gradle/libs.versions.toml`)

```toml
[versions]
agp = "9.0.1"
kotlin = "2.0.21"
composeBom = "2024.09.00"
firebaseBom = "33.9.0"
coreKtx = "1.10.1"
activityCompose = "1.8.0"
lifecycleRuntimeKtx = "2.6.1"
credentials = "1.2.2"
kotlinxCoroutinesPlayServices = "1.7.3"
```

### Dependencies Breakdown

| Dependency | Version | Purpose |
|---|---|---|
| `androidx.core:core-ktx` | 1.10.1 | Android core extensions |
| `androidx.lifecycle:lifecycle-runtime-ktx` | 2.6.1 | Lifecycle-aware coroutines |
| `androidx.activity:activity-compose` | 1.8.0 | Compose integration with Activity |
| `androidx.compose:compose-bom` | 2024.09.00 | Compose Bill of Materials |
| `androidx.compose.material3` | via BOM | Material3 design components |
| `androidx.compose.material:material-icons-extended` | via BOM | Extended icon set |
| `com.google.firebase:firebase-bom` | 33.9.0 | Firebase Bill of Materials |
| `com.google.firebase:firebase-auth` | via BOM | Authentication |
| `com.google.firebase:firebase-firestore` | via BOM | Cloud Firestore |
| `androidx.credentials:credentials` | 1.2.2 | Credential manager |
| `com.google.android.libraries.identity.googleid:googleid` | 1.1.1 | Google ID integration |
| `org.jetbrains.kotlinx:kotlinx-coroutines-play-services` | 1.7.3 | Firebase coroutine wrappers |

### Gradle Properties (`gradle.properties`)

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

### ProGuard (`app/proguard-rules.pro`)

The `proguard-rules.pro` file contains custom ProGuard rules for release builds. Currently, minification is disabled (`isMinifyEnabled = false`).

---

## 🧪 Testing

### Unit Tests

Located at: `app/src/test/java/com/example/greetingcard/ExampleUnitTest.kt`

```bash
./gradlew test
```

### Instrumented Tests

Located at: `app/src/androidTest/java/com/example/greetingcard/`

| Test File | Description |
|---|---|
| `ExampleInstrumentedTest.kt` | General instrumentation tests |
| `ArtSpaceScreenTest.kt` | Compose UI component tests |

```bash
./gradlew connectedAndroidTest
```

### Testing with Preview Mode

The `AppState(isPreview = true)` constructor provides mock data for UI development without a Firebase backend. Preview composables are defined at the bottom of `MainActivity.kt` (lines 1114-1177).

```kotlin
@Preview(showBackground = true)
@Composable
fun PreviewSearchTab() {
    RaktaVahiniTheme(false) {
        val s = AppState(true)
        s.loadInitialData()
        SearchTab(s, {}) { _ -> }
    }
}
```

### Testing Best Practices

1. Write tests for `AppState` business logic (eligibility calculation, smart scoring, distance calculation).
2. Use `AppState(isPreview = true)` for screenshot and UI component tests.
3. Test Firebase integration with the Firebase Emulator Suite for local development.

---

## 🤝 Contributing Guidelines

### How to Contribute

We welcome contributions! Follow these steps:

#### 1. Fork the Repository

Click the **Fork** button on the top-right of the [repository page](https://github.com/vijaykumarGK-Developer/rakthavahini).

#### 2. Clone Your Fork

```bash
git clone https://github.com/<your-username>/rakthavahini.git
cd rakthavahini
git remote add upstream https://github.com/vijaykumarGK-Developer/rakthavahini.git
```

#### 3. Create a Feature Branch

```bash
git checkout -b feature/your-feature-name
```

#### 4. Make Your Changes

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
- Use meaningful variable and function names.
- Add comments only where necessary for clarity.
- Keep the code consistent with existing patterns (MVI architecture, composable naming, etc.).

#### 5. Commit Your Changes

```bash
git add .
git commit -m "feat: add your feature description"
```

Use [Conventional Commits](https://www.conventionalcommits.org/) format:
- `feat:` — New feature
- `fix:` — Bug fix
- `docs:` — Documentation changes
- `refactor:` — Code restructuring
- `test:` — Adding or updating tests
- `chore:` — Maintenance tasks

#### 6. Push and Create a Pull Request

```bash
git push origin feature/your-feature-name
```

Then open a Pull Request on GitHub with:
- A clear title and description
- Screenshots for UI changes
- Reference to any related issues

### Development Workflow

1. Pick an issue from the Issues tab or propose a new feature.
2. Discuss the approach in the issue comments.
3. Implement the change in a feature branch.
4. Run tests to ensure nothing is broken.
5. Submit a Pull Request for review.

### Code Review Process

- All PRs require at least one review before merging.
- Address review feedback promptly.
- Keep PRs focused — one feature or fix per PR.
- Ensure CI passes (when configured).

---

## 📜 Code of Conduct

### Our Pledge

We pledge to make participation in this project a harassment-free experience for everyone, regardless of age, body size, disability, ethnicity, gender identity and expression, level of experience, nationality, personal appearance, race, religion, or sexual identity and orientation.

### Our Standards

**Examples of behavior that contributes to a positive environment:**
- Using welcoming and inclusive language
- Being respectful of differing viewpoints and experiences
- Gracefully accepting constructive criticism
- Focusing on what is best for the community
- Showing empathy towards other community members

**Examples of unacceptable behavior:**
- The use of sexualized language or imagery
- Trolling, insulting/derogatory comments, and personal attacks
- Public or private harassment
- Publishing others' private information without explicit permission
- Other conduct which could reasonably be considered inappropriate in a professional setting

### Enforcement

Project maintainers are responsible for clarifying the standards of acceptable behavior and are expected to take appropriate and fair corrective action in response to any instances of unacceptable behavior.

Instances of abusive, harassing, or otherwise unacceptable behavior may be reported by contacting the project team at [vijaykumar2572003@gmail.com](mailto:vijaykumar2572003@gmail.com). All complaints will be reviewed and investigated and will result in a response that is deemed necessary and appropriate to the circumstances.

### Scope

This Code of Conduct applies within all project spaces, and also applies when an individual is representing the project or its community in public spaces.

---

## 🔒 Security Policy

### Supported Versions

| Version | Supported |
|---|---|
| 1.0.x | ✅ Supported |

### Reporting a Vulnerability

We take security seriously. If you discover a security vulnerability, please follow these steps:

1. **Do not** disclose the vulnerability publicly (e.g., by creating a public GitHub issue).
2. Send an email to [vijaykumar2572003@gmail.com](mailto:vijaykumar2572003@gmail.com) with:
   - A description of the vulnerability
   - Steps to reproduce
   - Potential impact
3. You should receive a response within 48 hours.
4. We will work with you to understand and address the issue.

### Security Considerations for This Project

#### Firebase Credentials
- The `google-services.json` file contains Firebase project credentials.
- **Never commit** `google-services.json` to version control if it contains production credentials.
- The file is already listed in `.gitignore`.

#### API Keys & Secrets
- No hardcoded API keys are present in the codebase.
- All Firebase configuration is handled through `google-services.json`.

#### Data Privacy
- User location data (lat/lng) is stored in Firestore and used solely for proximity-based donor matching.
- Phone numbers are stored for emergency contact purposes.
- The app uses `Intent.ACTION_DIAL` for calls — no call data is transmitted through our servers.

#### Firestore Security
- Always restrict Firestore security rules before deploying to production.
- Test mode allows all reads/writes — use only for development.
- Implement proper authentication checks in production rules.

---

## 📝 Changelog

### [1.0.0] — June 2026

#### Added
- 🩸 Intelligent donor discovery with proximity-based search
- 🚨 Emergency SOS broadcast system with real-time sync
- 🏥 Verified hospital and blood bank directory
- 🏆 Donation logging with auto-generated "Certificate of Heroism"
- 🌓 Dark/Light theme support
- 👤 Dual registration (individual donors and hospitals)
- 🔍 Smart scoring algorithm for donor ranking
- 📍 Live location capture for accurate distance matching
- 🎨 100% Jetpack Compose UI with Material3 design
- 📱 5-tab navigation (Search, Profile, Hospitals, Users, History)
- ⚡ Firebase Cloud Firestore real-time synchronization
- 🔐 Firebase Authentication integration
- 📞 One-tap secure call to donors and hospitals
- 🔄 Animated slide overlays for detail screens
- 📊 Skeleton loading animations for search results

---

## ❓ FAQ

### General

**Q: What is Rakta-Vahini?**
A: It's an Android app that connects blood donors with hospitals and individuals in need during emergencies, using real-time location matching.

**Q: Is the app free?**
A: Yes, the app is open-source under the MIT License.

**Q: Which Android versions are supported?**
A: Android 7.0 Nougat (API 24) and above.

### Usage

**Q: How is donor eligibility calculated?**
A: A strict 90-day cooling period is enforced automatically. Donors cannot be matched again until 90 days have passed since their last donation.

**Q: How does the smart scoring work?**
A: Donors are ranked by: `distance score + (responseSpeed × 0.1) + (activeDaysAgo × 0.5) - (freq × 0.2)`. Lower scores are better matches.

**Q: Is my phone number public?**
A: Calls are routed via Android's `Intent.ACTION_DIAL`, which opens the dialer with the number pre-filled. The user must manually press the call button.

**Q: How do I get a Certificate of Heroism?**
A: Log a donation using the "I Donated Today" button in the Profile tab. The certificate is automatically generated.

### Technical

**Q: Does the app work offline?**
A: Currently, the app requires an internet connection for Firebase sync. Offline persistence is not yet implemented.

**Q: How is my data stored?**
A: Data is stored in Firebase Cloud Firestore, a NoSQL database hosted by Google Cloud. See the [Privacy Policy](https://github.com/vijaykumarGK-Developer/rakthavahini) for details.

**Q: Can I contribute?**
A: Yes! See the [Contributing Guidelines](#-contributing-guidelines) section.

---

## 🔧 Troubleshooting

### Common Issues

#### App crashes on launch

| Cause | Solution |
|---|---|
| Missing `google-services.json` | Add your Firebase config file to `app/google-services.json`. |
| Outdated Android Studio | Update to Ladybug (2024.2) or newer. |
| Gradle sync failure | Run `./gradlew clean` and try again, or invalidate caches in Android Studio. |

#### Firestore data not loading

| Cause | Solution |
|---|---|
| Firestore not enabled | Enable Cloud Firestore in Firebase Console. |
| Wrong security rules | Set security rules to test mode for development. |
| No network | Ensure the device has internet connectivity. |

#### Location not working

| Cause | Solution |
|---|---|
| Location permissions denied | Grant location permissions in app settings. |
| GPS disabled | Enable GPS on the device. |
| Emulator without location | Set a mock location in the emulator extended controls. |

#### Build errors

| Cause | Solution |
|---|---|
| JDK version mismatch | Use JDK 17 or later. |
| AGP version incompatible | Use the specified AGP 9.0.1. |
| Dependency conflicts | Run `./gradlew dependencies` to inspect the dependency tree. |

### Getting Help

If you encounter issues not listed here:

1. Check the [GitHub Issues](https://github.com/vijaykumarGK-Developer/rakthavahini/issues) for existing reports.
2. Search the [Discussions](https://github.com/vijaykumarGK-Developer/rakthavahini/discussions) tab.
3. Open a new issue with:
   - Device and Android version
   - Steps to reproduce
   - Logcat output (if applicable)
   - Screenshots or screen recordings

---

<p align="center">
  <i>Rakta-Vahini — Connecting Hearts, Saving Lives.</i>
  <br>
  <a href="https://github.com/vijaykumarGK-Developer/rakthavahini">GitHub</a> •
  <a href="mailto:vijaykumar2572003@gmail.com">Contact</a> •
  <a href="https://github.com/vijaykumarGK-Developer/rakthavahini/issues">Issues</a>
</p>
