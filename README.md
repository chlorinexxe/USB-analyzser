# CablePulse Cable Analyzer Pro (Android)

CablePulse is a premium diagnostic analyzer app for USB Type-C cables and power specifications. It parses hardware attributes, simulates and probes capabilities such as **USB Power Delivery up to 240W Extended Power Range (EPR)**, **DisplayPort Alternate Mode**, and **SuperSpeed Core Bus configurations**, complete with custom interactive themes, typography controls, and real-time oscilloscopes.

This guide details how to set up the project in **Visual Studio Code (VS Code)** and how to build, transfer, and **install the APK on your Android device**.

---

## 🚀 Setting Up the Project in VS Code

While Android Studio is the default IDE for Android development, you can easily develop, view, and build this Jetpack Compose project from within **VS Code** using the command line and specific extensions.

### 1. Prerequisites
Before opening the project, ensure you have the following installed on your machine:
*   **Java Development Kit (JDK 17)**: Android Gradle builds require JDK 17. You can check your version with `java -version` or download it from [Adoptium (Eclipse Temurin)](https://adoptium.net/).
*   **Android SDK (Optional for CLI, Required for Emulator/ADB run)**: Installing Android Studio is recommended to get the SDK command-line tools. Make sure `$ANDROID_HOME` is set in your environment variables.

### 2. Recommended VS Code Extensions
Open VS Code, press `Ctrl+P` (or `Cmd+P`), and run the following commands to install language support for Kotlin and Gradle:
*   **Kotlin (by fwcd)**: Provides syntax highlighting, code completion, and diagnostics.
    ```bash
    ext install fwcd.kotlin
    ```
*   **Extension Pack for Java (by Microsoft)**: For project model parsing and debugger tooling.
    ```bash
    ext install vscjava.vscode-java-pack
    ```
*   **Gradle for Java (by Microsoft)**: Simplifies managing task automation and build dependency maps.
    ```bash
    ext install vscjava.vscode-gradle
    ```

### 3. Importing and Compilation
1. Open this directory in VS Code (`File` > `Open Folder...`).
2. Open an integrated terminal (`Ctrl + ` ` ` or `Cmd + ` ` `).
3. To compile the app and download all necessary dependencies, execute the Gradle wrapper:
    *   **macOS / Linux:**
        ```bash
        ./gradlew assembleDebug
        ```
    *   **Windows (PowerShell):**
        ```powershell
        .\gradlew.bat assembleDebug
        ```
    *   **Windows (CMD / Legacy Command Prompt):**
        ```cmd
        gradlew.bat assembleDebug
        ```
4. Upon successful completion, your standard debug APK is output directly to:
    ```
    app/build/outputs/apk/debug/app-debug.apk
    ```

---

## 📱 How to Install the APK on Your Android Phone

To install your built application onto your physical handset, follow either of the methods below.

### Method A: Direct File Transfer (Easiest)

#### Step 1: Transfer the APK
You can copy the generated `app-debug.apk` file from folder `app/build/outputs/apk/debug/` to your phone using any of these techniques:
*   **USB Data Cable**: Connect your phone to your PC. Set the USB preferences on the device to **File Transfer / Android Auto (MTP)** and drag and drop `app-debug.apk` into your phone's internal storage or **Downloads** directory.
*   **Cloud Drive**: Upload `app-debug.apk` to Google Drive, Dropbox, or OneDrive from your computer, then download it on your phone.
*   **Local Sharing Services**: Send it directly to yourself via WhatsApp Web, Telegram, Email, or LocalSend/Snapdrop.

#### Step 2: Allow Unknown Source Installations
Since this application is not compiled from the official Google Play Store (it is signed with a local debug keyset), your Android OS will request special permission before execution:
1. Open your Android device's **Settings**.
2. Go to **Security** or **Privacy** > **Install Unknown Apps** (or search "Install Unknown Apps" in the settings search bar).
3. Locate the app you are using to launch the APK (e.g., **Files by Google**, **My Files**, or **Chrome** if you downloaded it from a cloud drive) and toggle **Allow from this source** to **ON**.

#### Step 3: Trigger Installation
1. Open your physical phone's **File Manager** (Files, Solid Explorer, etc.) and open the **Downloads** or location directory where the file was saved.
2. Tap on `app-debug.apk`.
3. If Google Play Protect displays a diagnostic window stating *"Blocked by Play Protect: This app was built for an older version of Android/is unrecognized"*:
    *   Tap **More Details** or **Details**.
    *   Tap **Install Anyway**.
4. Confirm by tapping **Install**, wait for the progress bar to finish, and tap **Open**!

---

### Method B: Deploying via ADB (Fastest for Developers)

If you have USB Debugging turned on and possess the Android Debug Bridge (`adb`) tools configured on your development terminal, you can perform wireless or wired silent installation:

1. Enable **Developer Options** and turn on **USB Debugging** on your device.
2. Connect your phone via a USB cable.
3. Check that the phone is recognized correctly on your terminal:
    ```bash
    adb devices
    ```
4. Install or upgrade the application instantly:
    ```bash
    adb install app/build/outputs/apk/debug/app-debug.apk
    ```
    *(If upgrading an existing install, use `-r` flag: `adb install -r app/build/outputs/apk/debug/app-debug.apk`)*

---

## 🛠️ Tech Stack & Key Modules
*   **Jetpack Compose**: Declarative, dynamic user UI components.
*   **Material Design 3 (M3)**: Responsive component sizing schemas, interactive theme selections (CyberTeal, Matrix, Nebula, Luxury Gold), and customizable layout parameters.
*   **ViewModel & StateFlow**: Modern single source-of-truth state container design.
*   **Diagnostics Platform**: Built-in automated diagnostic sweep simulator checking for hardware core bus structures.
