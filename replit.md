# TermuxIwx

A native Android application that provides a graphical UI for managing packages and running commands in the Termux environment on Android.

## About

TermuxIwx lets users search, install, remove packages, and execute custom scripts via the Termux `RUN_COMMAND` Intent API — all from a Material Design 3 interface.

## Tech Stack

- **Language**: Java 8
- **Build System**: Gradle 8.2.2
- **Min SDK**: 26 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **UI**: Material Design 3, ViewBinding, RecyclerView

## Running in Replit

This is a native Android app — it **cannot run as a web server** in Replit's preview pane. It must be built into an APK and installed on an Android device running Termux.

### Build the APK

Use the Shell to build:

```bash
./gradlew assembleDebug
```

The output APK will be at:
```
app/build/outputs/apk/debug/app-debug.apk
```

> **Note**: Building requires the Android SDK. Gradle will attempt to download it automatically. This may take several minutes on first run.

### Installing on Device

1. Install [Termux from F-Droid](https://f-droid.org/packages/com.termux/)
2. In Termux, run:
   ```bash
   mkdir -p ~/.termux
   echo "allow-external-apps=true" >> ~/.termux/termux.properties
   ```
3. Restart Termux
4. Transfer and install the APK on your Android device

## Project Structure

```
app/
  src/main/java/com/kztutorial/termuxiwx/
    models/       # Package and script data models
    ui/           # Activities (MainActivity, ConsoleActivity, etc.)
    utils/        # TermuxConnector and helper classes
  src/main/res/   # Layouts, drawables, strings
  AndroidManifest.xml
build.gradle      # Root Gradle config
settings.gradle   # Module inclusion
```

## Developer

Kztutorial99

## User Preferences

(No preferences set yet.)
