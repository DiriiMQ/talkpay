# TalkPay

TalkPay is an Android application that reads notifications and uses Text-to-Speech (TTS) to announce specific information extracted from the notifications. The app is built using Kotlin and leverages Android's notification listener service and TTS capabilities.

## Features

- **Notification Listener**: Monitors notifications from specific apps.
- **Text-to-Speech**: Converts extracted information from notifications into speech.
- **Foreground Service**: Ensures the TTS service runs in the background.

## Permissions

The app requires the following permissions:

- `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`
- `android.permission.FOREGROUND_SERVICE`
- `android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK`

These permissions are declared in the `AndroidManifest.xml` file.

## Components

### MainActivity

The main entry point of the app. It handles the UI and checks for notification listener permissions.

### NotificationListener

A service that listens for notifications and extracts relevant information.

### TTSService

A service that uses Text-to-Speech to announce the extracted information.

### Utils

A utility class that provides methods for extracting information from notification texts and checking if a service is running.

## Usage

1. **Grant Notification Access**: When the app is launched, it will prompt the user to grant notification access.
2. **Start/Stop TTS Service**: The user can start or stop the TTS service using the provided button in the app's UI.
3. **Turn Off Doze Mode**: To ensure the app receives notifications in sleep mode on Android 14, you need to turn off Doze mode. You can do this by running the following ADB command:
   ```sh
   adb shell dumpsys deviceidle disable
    ```
   
## Code Structure

- `MainActivity.kt`: Handles the main UI and permission checks.
- `NotificationListener.kt`: Listens for notifications and processes them.
- `TTSService.kt`: Manages the Text-to-Speech functionality.
- `Utils.kt`: Contains utility functions for text extraction and service checks.

## Installation

1. Clone the repository.
2. Open the project in Android Studio.
3. Build and run the app on an Android device or emulator.

## Building the APK

To release an APK file in Android Studio, follow these steps:

1. **Build the Project**:
    - Open your project in Android Studio.
    - Go to `Build` > `Build Bundle(s) / APK(s)` > `Build APK(s)`.

2. **Generate Signed APK**:
    - Go to `Build` > `Generate Signed Bundle / APK`.
    - Select `APK` and click `Next`.

3. **Create or Choose a Key Store**:
    - If you don't have a key store, click `Create new...` and fill in the required details.
    - If you already have a key store, click `Choose existing...` and select your key store file.

4. **Enter Key Store Information**:
    - Enter the key store password, key alias, and key password.
    - Click `Next`.

5. **Select Build Variants**:
    - Choose the build variant (usually `release`).
    - Click `Finish`.

6. **Locate the APK**:
    - Once the build is complete, a notification will appear.
    - Click `locate` to find the generated APK file in the `app/build/outputs/apk/release/` directory.

## Optimizing APK Size

To optimize the APK size, consider the following best practices:

1. **Remove Unused Resources**: Use the `resConfig` property in your `build.gradle` to specify the configurations you need.
2. **Enable ProGuard**: Shrinks, optimizes, and obfuscates your code.
3. **Use Android App Bundles**: Generates optimized APKs for different device configurations.
4. **Optimize Images**: Use vector drawables and compress PNG/JPEG files.
5. **Remove Debug Information**: Exclude debug information from the release build.
6. **Use R8**: The code shrinker and obfuscator that replaces ProGuard.
7. **Avoid Using Large Libraries**: Use only necessary parts of libraries or find smaller alternatives.
8. **Split APKs by ABI**: Generate separate APKs for different CPU architectures.
9. **Remove Unused Code**: Use tools like `Lint` to identify and remove unused code.
10. **Optimize Third-Party Libraries**: Use ProGuard rules to remove unused code from third-party libraries.

## License

This project is licensed under the MIT License. See the `LICENSE` file for more details.
