# 🚌 Vidyarthi-Bus

> Crowdsourced Bus Alert System for Rural College Students

## 📱 About

Vidyarthi-Bus is an Android app that helps college students in remote
villages know the crowd status of their college bus **before it arrives**.

Students already on the bus report the crowd level with a single tap.
Students waiting at upcoming stops see a real-time color-coded indicator
and can decide whether to wait or find an alternative.

## 🎯 Problem Statement

Students in remote villages depend entirely on specific college buses
for daily commute. When a bus is full or cancelled, students miss exams
and lectures with no alternative information available.

## ✨ Features

- 🔐 College email authentication
- 🚌 Real-time crowd meter (Green / Yellow / Red)
- 📍 GPS proximity verification
- ⏱️ Auto-expiry of reports after 15 minutes
- 📞 Shared auto contact alternatives
- 🔔 Push notifications for bus status changes
- ❌ Bus cancellation alerts

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Backend | Firebase Realtime Database |
| Auth | Firebase Authentication |
| Location | Google Fused Location Provider |
| Notifications | Firebase Cloud Messaging |
| Functions | Firebase Cloud Functions |


## 🚀 Setup Instructions

### Prerequisites
- Android Studio Hedgehog or newer
- Firebase account
- Android device / emulator (API 26+)

### Steps

1. Clone the repository
   ```bash
   git clone https://github.com/YourUsername/VidyarthiBus.git
   
2. Open in Android Studio

3. Create a Firebase project at https://console.firebase.google.com

4. Download google-services.json and place in app/ folder

5. Enable Firebase Authentication (Email/Password)

6. Create Firebase Realtime Database

7. Import seed data from seed.json

8. Run the app