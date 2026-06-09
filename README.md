<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-brightgreen?style=for-the-badge&logo=android" alt="Platform">
  <img src="https://img.shields.io/badge/Language-Kotlin-purple?style=for-the-badge&logo=kotlin" alt="Language">
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose" alt="UI">
  <img src="https://img.shields.io/badge/Backend-Firebase-FFCA28?style=for-the-badge&logo=firebase" alt="Backend">
  <img src="https://img.shields.io/badge/minSDK-24-44CC11?style=for-the-badge&logo=android" alt="minSDK">
  <img src="https://img.shields.io/github/license/vijaykumarGK-Developer/rakthavahini?style=for-the-badge" alt="License">
</p>

<h1 align="center">🩸 Rakta-Vahini</h1>
<h3 align="center">Real-Time Blood Donation Network — Connecting Donors & Saving Lives</h3>

<p align="center">
  <b>Rakta-Vahini</b> (रक्तवाहिनी — "Blood Vessel" in Sanskrit) is an intelligent, real-time Android application that eliminates friction in blood emergencies. It connects verified donors with hospitals and individuals in urgent need — within seconds — using modern Android technologies and real-time cloud synchronization.
</p>

---

## 📋 Table of Contents

- [✨ Key Features](#-key-features)
- [📱 Screenshots](#-screenshots)
- [🏗 Architecture](#-architecture)
- [🛠 Tech Stack](#-tech-stack)
- [📂 Project Structure](#-project-structure)
- [🚀 Getting Started](#-getting-started)
- [⚙️ Configuration](#️-configuration)
- [📖 Usage Guide](#-usage-guide)
- [🗺 Roadmap](#-roadmap)
- [🤝 Contributing](#-contributing)
- [📄 License](#-license)
- [📬 Contact](#-contact)

---

## ✨ Key Features

### 🔍 Intelligent Donor Discovery
| Feature | Description |
|---|---|
| **Proximity-Based Search** | Find donors within a customizable radius (1 km – 100 km) using Haversine distance calculation. |
| **Smart Scoring System** | Donors ranked by a composite score of distance, response speed, and historical donation frequency. |
| **Automated Eligibility** | Enforces a strict **90-day cooling period** to ensure donor health and safety. |
| **Blood Group Filter** | Quickly narrow results by specific blood group type. |

### 🚨 Emergency SOS System
- **One-Tap Broadcast** — Trigger an urgent SOS request specifying blood group and units needed via a modal bottom sheet.
- **Real-Time Sync** — Requests propagate instantly across all connected clients using Cloud Firestore snapshot listeners.
- **Priority Alerts** — SOS broadcasts are surfaced prominently across the app for maximum visibility.

### 🏥 Hospital & Blood Bank Network
- **Verified Directory** — Curated list of verified hospitals and blood banks with address, coordinates, and contact details.
- **One-Tap Communication** — Integrated `Intent.ACTION_DIAL` for immediate phone coordination.
- **Detail Slide-Over** — Animated panel showing full hospital information and call action.

### 🏆 Heroism & Recognition
- **Digital Certificates** — Automated "Certificate of Heroism" generated after every logged donation.
- **Public Donation Feed** — Transparent, real-time `HistoryTab` showing donation activity across the community.

### 🌓 Modern User Experience
- **100% Jetpack Compose UI** — Declarative, fluid, and reactive interface.
- **Dynamic Theming** — Full support for Light and Dark modes with custom blood-red Material3 palette.
- **Haptic Feedback** — Tactile response for critical actions (SOS, Call, Register).
- **Animated Navigation** — Slide overlays for detail screens and modal bottom sheets for SOS.

### 👤 Dual Registration
- **Individual Donors** — Register with personal details, blood group, and location.
- **Hospitals** — Register as a verified institution with facility information.

---

## 📱 Screenshots

```
┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────────┐
│                     │  │                     │  │                     │
│   Splash &          │  │   Smart Donor       │  │   Emergency SOS     │
│   Onboarding        │  │   Discovery         │  │   Broadcast         │
│                     │  │                     │  │                     │
│        🩸           │  │        🔍           │  │        🚨           │
│                     │  │                     │  │                     │
└─────────────────────┘  └─────────────────────┘  └─────────────────────┘
```

> ℹ️ *Replace the placeholder blocks above with actual app screenshots.*

---

## 🏗 Architecture

Rakta-Vahini follows an **MVI (Model-View-Intent)** architecture for predictable, testable, and unidirectional state management.

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   View      │────>│   Intent     │────>│   Model     │
│ (Compose)   │     │ (Actions)    │     │ (State)     │
└─────────────┘     └──────────────┘     └─────────────┘
       ^                                       │
       │                                       │
       └───────────────────────────────────────┘
                    State Flow
```

| Layer | Component | Responsibility |
|---|---|---|
| **View** | Jetpack Compose Screens (`SearchTab`, `ProfileTab`, etc.) | Renders UI from state, collects `StateFlow`, dispatches user intents. |
| **Intent** | User Actions (button taps, text input, SOS trigger) | Encapsulated as method calls on the `AppState` container. |
| **Model** | `AppState` (single state container) | Holds all application state (`Donor`, `Hospital`, `EmergencyRequest`, `DonationLog` collections), manages Firebase real-time listeners, executes business logic (eligibility checks, smart scoring, Haversine distance). |

### Key Design Decisions

- **Single `AppState` container** — Centralized state management avoids prop drilling and makes the data flow observable from any Composable.
- **Firebase snapshot listeners** — Real-time bidirectional sync across all connected devices without polling.
- **Preview mode** — `AppState` includes mock data for rapid UI development in Android Studio previews.

---

## 🛠 Tech Stack

### 📱 Frontend
| Technology | Version | Purpose |
|---|---|---|
| [Kotlin](https://kotlinlang.org/) | 2.0.21 | Primary language (100% Kotlin codebase) |
| [Jetpack Compose](https://developer.android.com/jetpack/compose) | BOM 2024.09.00 | Declarative UI framework |
| [Material 3](https://m3.material.io/) | via BOM | Design system with dynamic theming |
| [Android Gradle Plugin](https://developer.android.com/studio/releases/gradle-plugin) | 9.0.1 | Build system |
| [Gradle Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html) | — | Build configuration |

### ☁️ Backend & Services
| Technology | Version | Purpose |
|---|---|---|
| [Firebase Cloud Firestore](https://firebase.google.com/docs/firestore) | BOM 33.9.0 | Real-time NoSQL database |
| [Firebase Authentication](https://firebase.google.com/docs/auth) | BOM 33.9.0 | Secure user identification |
| [Google Credentials API](https://developers.google.com/android/credentials) | 1.2.2 | Credential manager integration |
| [Google Play Services Auth](https://developers.google.com/android/reference/com/google/android/gms/auth/package-summary) | via credentials | Play Services authentication |

### 🧠 Architecture & Utilities
| Library | Purpose |
|---|---|
| `kotlinx-coroutines-play-services` | Coroutine-friendly Firebase API wrappers |
| `material-icons-extended` | Rich icon set for the UI |
| `lifecycle-runtime-ktx` | Lifecycle-aware coroutine scopes |

---

## 📂 Project Structure

```
rakthavahini/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/greetingcard/
│   │   │   │   ├── MainActivity.kt          # Entry point, AppState init, AppRoot
│   │   │   │   ├── ArtSpace.kt              # Mock artwork data
│   │   │   │   └── ui/
│   │   │   │       └── theme/
│   │   │   │           ├── Color.kt          # Material3 color definitions
│   │   │   │           ├── Theme.kt          # Light/Dark theme configuration
│   │   │   │           └── Type.kt           # Typography scale
│   │   │   ├── res/                         # Resources (drawables, strings, themes)
│   │   │   ├── AndroidManifest.xml
│   │   │   └── google-services.json         # Firebase configuration
│   │   ├── test/                            # Unit tests
│   │   └── androidTest/                     # Instrumented tests
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   ├── wrapper/
│   └── libs.versions.toml                   # Version catalog
├── build.gradle.kts                         # Root build script
├── settings.gradle.kts                      # Module settings
├── gradle.properties                        # Build properties
├── gradlew / gradlew.bat                    # Gradle wrappers
└── README.md                                # You are here
```

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version |
|---|---|
| **Android Studio** | Ladybug (2024.2) or newer |
| **JDK** | 17 or later |
| **Android SDK** | API 24 (Android 7.0 Nougat) minimum |
| **Google Account** | For Firebase project setup |

### Step 1: Clone the Repository

```bash
git clone https://github.com/vijaykumarGK-Developer/rakthavahini.git
cd rakthavahini
```

### Step 2: Set Up Firebase

1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Create a new project (or use an existing one).
3. Register an Android app with package name `com.example.greetingcard`.
4. Download the `google-services.json` file.
5. Place it at `app/google-services.json` (overwrite the placeholder).

### Step 3: Enable Firebase Services

- **Cloud Firestore**: Create a database in test mode.
- **Authentication**: Enable at least one sign-in provider (e.g., Anonymous or Email/Password).

### Step 4: Configure Firestore Security Rules (Development)

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

> ⚠️ **Warning**: Test mode rules allow all reads/writes. Restrict these before deploying to production.

### Step 5: Build & Run

```bash
# Open in Android Studio, sync Gradle, then run:
./gradlew assembleDebug
```

Or open the project in Android Studio, select a device/emulator (API 24+), and click **Run ▶**.

---

## ⚙️ Configuration

### Build Configuration (`build.gradle.kts`)

| Property | Value | Description |
|---|---|---|
| `applicationId` | `com.example.greetingcard` | Unique app identifier |
| `compileSdk` | 36 | Target compilation SDK |
| `minSdk` | 24 | Minimum supported Android version |
| `targetSdk` | 36 | Target runtime SDK |
| `versionCode` | 1 | Internal version number |
| `versionName` | `1.0` | User-facing version name |

### App State Tuning (`AppState`)

| Parameter | Default | Description |
|---|---|---|
| `DONATION_COOLDOWN_DAYS` | 90 | Minimum days between donations |
| Search radius range | 1 km – 100 km | Adjustable via slider in `SearchTab` |
| Smart scoring weights | Distance + Speed + Frequency | Composite ranking algorithm |

### Firestore Collections

| Collection | Documents | Purpose |
|---|---|---|
| `donors` | `Donor` objects | Registered donor profiles |
| `hospitals` | `Hospital` objects | Verified hospital/blood bank directory |
| `emergencyRequests` | `EmergencyRequest` objects | Active SOS broadcasts |
| `donationLogs` | `DonationLog` objects | Historical donation records |

---

## 📖 Usage Guide

### 🩸 As a Donor

1. **Register** — Navigate to the Profile tab and register as an individual donor with your blood group and location.
2. **Enable Location** — Ensure location permissions are granted for proximity-based matching.
3. **Receive Alerts** — SOS requests matching your blood group appear in real time on the Search tab.
4. **Respond** — Contact the requester directly via the in-app dial action.
5. **Log Donations** — After donating, use "I Donated Today" to trigger a Certificate of Heroism.

### 🏥 As a Hospital / Blood Bank

1. **Register** — Use the Profile tab's hospital registration mode to add your facility.
2. **Broadcast Needs** — Use the SOS FAB to request specific blood groups and unit quantities.
3. **Track Responses** — Monitor donor confirmations in real time via the active requests feed.

### 🎖 Earning Recognition

- Each logged donation automatically generates a **Certificate of Heroism** with the donor's name and date.
- The **History tab** displays a public, real-time feed of all donation activity.

---

## 🗺 Roadmap

- [x] Core donor discovery and SOS broadcast
- [x] Hospital directory with one-tap dial
- [x] Donation logging with hero certificates
- [ ] Push notification integration for SOS alerts
- [ ] Multi-language support (Hindi, Kannada, Marathi, Telugu)
- [ ] Blood stock inventory tracking for blood banks
- [ ] Donor rating & feedback system
- [ ] Appointment scheduling for scheduled donations
- [ ] CI/CD pipeline with GitHub Actions
- [ ] End-to-end testing suite

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

### Contribution Workflow

1. **Fork** the repository.
2. **Create a feature branch:**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Commit your changes:**
   ```bash
   git commit -m "feat: add amazing feature"
   ```
4. **Push to the branch:**
   ```bash
   git push origin feature/amazing-feature
   ```
5. **Open a Pull Request** with a clear description of your changes.

### Guidelines

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
- Use meaningful variable and function names.
- Write clear, descriptive commit messages (conventional commits preferred).
- Add/update tests for new functionality.
- Ensure the project builds successfully before submitting.

---

## 📄 License

Distributed under the **MIT License**. See [LICENSE](LICENSE) for more information.

---

## 📬 Contact

**Vijay Kumar G K**

- GitHub: [@vijaykumarGK-Developer](https://github.com/vijaykumarGK-Developer)
- Project: [https://github.com/vijaykumarGK-Developer/rakthavahini](https://github.com/vijaykumarGK-Developer/rakthavahini)

---

<p align="center">
  Made with ❤️ for every life that matters.
  <br><br>
  <b>Rakta-Vahini</b> — <i>Connecting Hearts, Saving Lives.</i>
</p>
