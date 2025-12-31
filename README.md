
# 🐾 Scanimal (Android)

Identify animals and collect their data—fast, accurate, and delightful.  
Built with **Kotlin**, **Jetpack Compose**, and **MVVM**, Scanimal is a multi-screen Android app featuring onboarding, authentication, animal identification, registration, profiles, and a dashboard.  

!Kotlin
!Jetpack Compose
!Android
!License: MIT

---

## ✨ Key Features

- 🔍 **Animal Identification**: Identify species from input (camera/photo or attributes) and return details (name, class, habitat, traits).
- 🗂️ **Structured Data Collection**: Register animals with metadata (region, date, notes) for tracking and research.
- 👤 **User Profiles**: Manage user details and view historical submissions.
- 📊 **Dashboard**: Quick stats and recent identifications.
- 🔐 **Authentication**: Sign up, sign in, and secure sessions.
- 🗺️ **Clean Navigation**: Centralized `AppNavGraph` with typed routes.
- 🎨 **Modern UI**: Compose-based theming and responsive design.

---

## 🏗️ Architecture

**MVVM** + **Jetpack Compose** + **Navigation** - **View** (Compose 
screens) → **ViewModel** (state, business logic) → **Model** (data 
layer) - Unidirectional data flow, state hoisting, sealed UI states. 
app/ ├── manifests/ │ └── AndroidManifest.xml ├── kotlin+java/ │ └── 
com.kingree.scanimal/ │ ├── Model/ │ │ └── data/ # DTOs, repositories, 
local models │ ├── navigation/ │ │ ├── AppNavGraph.kt # Central 
navigation graph │ │ └── Screen/ # Route definitions / sealed classes │ 
├── ui.theme/ # Color, typography, shapes │ ├── view/ # Compose screens 
│ │ ├── DashboardScreen.kt │ │ ├── IdentifyAnimalScreen.kt │ │ ├── 
login_page.kt │ │ ├── MainScreen.kt │ │ ├── ProfileScreen.kt │ │ ├── 
RegisterAnimalScreen.kt │ │ ├── SignUpScreen.kt │ │ └── SplashScreen.kt 
│ ├── viewModel/ # ViewModels │ └── MainActivity.kt # NavHost + app 
entry
---

## 🧰 Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Navigation**: `androidx.navigation.compose`
- **State**: ViewModel + StateFlow
- **Theme**: Material 3 (Compose)
- **(Optional)**: Room / Retrofit / ML Kit (plug-in points provided)

---

## 🚀 Getting Started

### Prerequisites
- Android Studio **Giraffe / Koala** or newer
- JDK 17
- Gradle (via Android Studio)
- Min SDK 24 (customize if needed)

### Setup
1. **Clone the repo**
   ```bash
   git clone https://github.com/<your-org>/scanimal-android.git
   cd scanimal-android
