# MoMoney 💸

**MoMoney** is a powerful, offline-first personal finance application for Android designed to help you track expenses, manage budgets, and visualize your financial health. Built with modern Android development standards, it prioritizes user privacy, data security, and seamless cloud synchronization.

<p align="center">
  </p>

## ✨ Key Features

### 🚀 **Core Functionality**
- **Smart Tracking:** Easily add income and expenses with notes, categories, and dates.
- **Offline-First:** Works completely offline using a local Room database. Data syncs automatically to the cloud when you reconnect.
- **Real-Time Cloud Sync:** Seamless multi-device synchronization powered by **Firebase Firestore**. Log in on your tablet, and your phone's data is already there.

### 📊 **Analytics & Budgets**
- **Visual Reports:** Understand your spending habits with interactive **Pie Charts**, **Bar Graphs** (Income vs. Expense), and **Line Charts** (Daily Trends).
- **Budgeting System:** Set monthly limits for categories (e.g., Food, Rent) and get real-time progress bars to stay on track.
- **Smart Alerts:** Receive notifications when you approach or exceed your budget limits.

### 🔒 **Security & Privacy**
- **App Lock:** Secure your financial data with a 6-digit PIN. The app locks instantly when minimized.
- **"Nuclear" Logout:** Privacy-focused logout clears all application data from the device to prevent data leaks between users.
- **Encrypted Storage:** Sensitive preferences are stored using `EncryptedSharedPreferences`.

### 🛠 **Power User Tools**
- **Recurring Transactions:** Set up transactions to repeat Weekly or Monthly automatically.
- **Global Search:** Find any transaction instantly by note, or filter by date range, category, and type.
- **Currency Converter:** Live currency conversion (USD, KES, EUR, etc.) that updates your entire dashboard instantly.
- **Custom Categories:** Create your own categories with custom colors and icons that sync across devices.

## 🏗 Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **Architecture:** MVVM + Clean Architecture
- **Local Database:** [Room](https://developer.android.com/training/data-storage/room) (SQLite)
- **Cloud Backend:** [Firebase Firestore](https://firebase.google.com/docs/firestore) & [Auth](https://firebase.google.com/docs/auth)
- **Dependency Injection:** [Hilt](https://dagger.dev/hilt/)
- **Asynchrony:** [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
- **Networking:** [Retrofit](https://square.github.io/retrofit/) (Exchange Rates API)
- **Charts:** Custom Canvas implementations & Vico
- **Animations:** [Lottie](https://airbnb.io/lottie/#/)

## 📲 Installation

1. Clone the repository:
   ```bash
   git clone [https://github.com/yourusername/momoney.git](https://github.com/yourusername/momoney.git)
