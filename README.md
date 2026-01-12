# MoMoney 💸

**MoMoney** is a powerful, **offline-first personal finance application for Android** designed to help users track expenses, manage budgets, and visualize their financial health. Built with modern Android development standards, MoMoney prioritizes **privacy, security, and seamless cloud synchronization** across devices.

<p align="center">
  <!-- App screenshots or banner can go here -->
</p>

---

## ✨ Key Features

### 🚀 **Core Functionality**

* **Smart Transaction Tracking:** Add income or expenses with category, amount, date, notes, payment method, and tags.
* **Offline-First Architecture:** Fully functional without internet using a local **Room** database.
* **Automatic Cloud Sync:** Secure multi-device synchronization using **Firebase Firestore**.
* **Recurring Transactions:** Automate repeating entries (e.g., rent, salary) on a weekly or monthly basis.
* **Quick Entry FAB:** Floating Action Button for fast transaction entry from anywhere in the app.

---

### 📊 **Analytics & Reports**

* **Spending Breakdown:** Interactive **Pie Charts** showing expenses by category.
* **Income vs Expense Comparison:** Monthly **Bar / Line Charts** for clear financial overviews.
* **Spending Trends:** Visualize financial patterns over days and months.
* **Budget Utilization:** Real-time progress bars showing how much of each budget has been used.
* **Data Export:** Export transactions to **CSV, Excel, or PDF** for sharing or offline analysis.

---

### 📅 **Budgets & Planning**

* **Category-Based Budgets:** Set monthly limits per category (Food, Transport, Rent, etc.).
* **Smart Budget Alerts:** Get notified when approaching or exceeding budget thresholds.
* **Real-Time Updates:** Budget usage updates instantly as transactions are added.

---

### 🔍 **Search & Filtering**

* **Global Search:** Instantly find transactions by notes or keywords.
* **Advanced Filters:** Filter by:

  * Date range
  * Category
  * Amount range
  * Payment method
  * Transaction type (Income / Expense)

---

### 🔐 **Security & Privacy**

* **App Lock:** Secure the app using a **6-digit PIN** or **Biometric authentication** (Fingerprint / Face ID).
* **Instant Lock on Background:** App content is hidden when minimized or shown in recents.
* **Nuclear Logout:** Completely wipes local data on logout to prevent cross-user data leaks.
* **Encrypted Storage:** Sensitive preferences stored using `EncryptedSharedPreferences`.
* **Optional Encrypted Database:** Designed to support encrypted Room storage for sensitive data.

---

### ☁️ **Cloud & Backup**

* **Firebase Authentication:** Secure login with Email or Google Sign-In.
* **Firestore Sync:** Real-time cloud sync across multiple devices.
* **Receipt Storage:** Optional photo uploads for transaction receipts via Firebase Storage.
* **Push Notifications:** Budget alerts and reminders powered by **Firebase Cloud Messaging (FCM)**.

---

### 🎨 **UI / UX**

* **Jetpack Compose + Material 3**
* **Dark & Light Mode**
* **Material You Dynamic Colors**
* **Bottom Navigation:** Home, Reports, Budgets, Settings
* **Smooth Animations:** Powered by `androidx.compose.animation` & **Lottie**
* **Onboarding Flow:** Friendly first-time user experience
* **Accessibility Support:** Large fonts & TalkBack compatibility

---

## 🏗 Tech Stack

* **Language:** Kotlin
* **UI:** Jetpack Compose (Material 3)
* **Architecture:** MVVM + Clean Architecture
* **Local Storage:** Room (SQLite)
* **Cloud Backend:** Firebase Firestore & Firebase Auth
* **Dependency Injection:** Hilt
* **Concurrency:** Kotlin Coroutines & Flow
* **Networking:** Retrofit (Exchange Rates API)
* **Charts:** Custom Canvas + Vico
* **Animations:** Lottie

---

## 📲 Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/CodeSpartan007/momoney.git
   ```

2. Open the project in **Android Studio**

3. Add your Firebase configuration (`google-services.json`)

4. Build & Run 🚀

---

> *MoMoney is designed with privacy, reliability, and performance in mind — putting full financial control in the user’s hands.*
