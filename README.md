# All-in-One Utility Android App 📱

This Android application is a multifunctional utility tool that brings together **video playback**, **PDF generation**, **local API mocking**, and **BLE (Bluetooth Low Energy) simulation**. It's designed to help developers, designers, and general users with common everyday tasks and debugging needs.

---

## 🚀 Features Overview

### 🎬 Video Player

A powerful, gesture-controlled video player built with ExoPlayer.

- Play videos from internal storage
- List and organize videos by folders
- Show video count per folder
- Adjust volume and brightness using gestures
- Change playback speed, audio track, and subtitles
- Seek forward/backward with gesture controls
- Supports **"Open With"** via intent filters
- Uses `MediaMetadataRetriever` for optimal resolution and layout

**Tech Used:** ExoPlayer, Gestures, Intent Filters, MediaMetadataRetriever

---

### 📄 PDF Generator

Create multi-page PDF files with full customization of text and images.

- Add text with rich formatting (bold, italic, underline)
- Change font size and color (includes fast color-pick presets)
- Insert and crop images
- Reuse selected images easily
- Generate PDFs that preserve layout using Bitmap rendering
- Save output PDF to device storage

**Tech Used:** Custom canvas + layout drawing, Bitmap rendering, Flask-style color picker

---

### 🔧 API Generator (Mock API Server)

Generate and test REST-like APIs locally for quick debugging and UI testing.

- Create custom endpoints (`GET`, `POST`) with response bodies
- Useful for frontend devs and API simulation in app prototypes
- Run a local server and respond to requests from other apps or devices

**Current Status:** Basic endpoint creation and response logic implemented

**Upcoming:**
- Header & authentication customization
- Predefined mock data support

**Tech Used:** NanoHTTPD, Dynamic route handling

---

### 📡 BLE Manager (Bluetooth Low Energy Simulator)

Create a simulated BLE server with full control over characteristics and responses.

- Set a custom BLE device name and random UID generator
- Add multiple characteristics with specific controls (read, write, notify)
- Define request-response behavior for BLE read/notify
- Ideal for BLE debugging without physical hardware

**Current Status:** Server setup & request-response pair logic implemented

**Upcoming:**
- Real-time log viewer
- File-based BLE responses (send images, PDFs, etc.)
- scanning and act as client

**Tech Used:** Android BLE APIs

---

## 📚 Built With

- Kotlin
- Android Jetpack Components (Activity, Fragment, ViewModel)
- ExoPlayer
- NanoHTTPD
- Android BLE API
- Canvas/Bitmap for PDF rendering
- UI Components: TextView, EditText, Dialogs, Drawers, Popups, Toasts, RecyclerView, etc.

---

## 📦 Use Cases

- 🎥 Watch and manage videos with intuitive controls
- 📄 Generate professional PDFs on the go
- 🌐 Test frontend UIs by mocking backend APIs locally
- 📡 Simulate BLE interactions for IoT apps without external dependencies

---

## 🛠️ Setup

1. Clone this repository:
   ```bash
   git clone https://github.com/Suhaibvp/simple_world.git

2. open the project in your ide 
        eg: android studio
3. Run the APK to build and you're good to go.

4. Alternatively, if you're not planning to build the project manually, the APK file is available in the Demo apk folder.
    Simply install it on your Android device and enjoy the features.



 ## 🔮 Roadmap
1. API Header customization & bearer auth

2. BLE communication logging UI

3. BLE file responses (PDF, image, etc.)

4. Theme customization (dark/light mode)

5. Export/import BLE/API profiles


## 📷 Screenshots


## 🤝 Contributing
PRs and suggestions are welcome! Please open issues or discussions if you have feature ideas or bugs to report.

## 📄 License
This project is licensed under the MIT License. See the LICENSE file for details.


## 👨‍💻 Author
Developed by Suhaib VP.
Contact: [suhaibvp9895@gmail.com]
LinkedIn/GitHub: [Linkdin](https://www.linkedin.com/in/suhaib-vp)/[Github](https://github.com/Suhaibvp)