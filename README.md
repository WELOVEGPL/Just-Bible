
[English](README.md) | [한국어](README.ko.md)

## Just Bible (Android)

An offline Korean Bible viewer template for Android.
Supports KOR1910 VPL import, book/chapter/verse navigation, bookmarking (long press), and OSIS-to-Korean book name mapping.
Built with Kotlin, Jetpack Compose, and SQLite.

---

### Features
- Browse by book, chapter, and verse
- Long-press verse to add/remove bookmarks
- Bookmark list with quick navigation; long-press to confirm deletion


---

### Requirements
- Java Development Kit (JDK): 17 or higher (e.g., Homebrew OpenJDK 17)
- Android SDK (including Command-line Tools)

---

### Environment Setup (macOS Example)

Adjust paths according to your system.

```bash
# JDK 17 path example (Homebrew)
export JAVA_HOME="/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home"

# Android SDK path example
export ANDROID_SDK_ROOT="$HOME/Library/Android/sdk"

# Optional: extend PATH
export PATH="$JAVA_HOME/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH"
```

---

### Build Instructions

```bash
chmod +x ./gradlew

# Debug APK
./gradlew --no-daemon assembleDebug

# Release APK (F-Droid friendly: unsigned)
./gradlew --no-daemon assembleRelease
```

**Output Paths**
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release-unsigned.apk`

---

### Data & Assets
- Book name mapping (OSIS/English → Korean): `app/src/main/assets/book_names_ko.json`
- Bible text (KOR1910): `app/src/main/assets/kor_vpl/kor_vpl.txt` or `app/src/main/assets/kor_vpl/kor_vpl.xml`

---

### License
- Source code: GNU General Public License v3.0 (GPLv3) — see `LICENSE`
- Third-party components: Licensed under their respective licenses — see `NOTICE`
- KOR1910 Bible text: Public Domain, provided by eBible.org
- Android / Jetpack Compose: Apache License 2.0

---

### Copyright

Copyright (C) 2025  
GPLoyalist, GPLaider

---

### Repository Structure

```text
app/src/main/java/com/opensourcebible/app/  # Kotlin source code
app/src/main/assets/                       # Data assets
app/src/main/res/                          # Android resources
gradle/, gradlew*, build.gradle.kts        # Gradle build system
settings.gradle.kts
```

---

### Contributing

Contributions are welcome.  
Please open an issue or submit a pull request for discussion before making significant changes.

---

### Acknowledgements
- eBible.org — for providing the KOR1910 Bible text (Public Domain)
- Google LLC — for Android and Jetpack Compose libraries (Apache License 2.0)
