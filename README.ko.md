[English](README.md) | 한국어

## Just Bible (Android) — 한국어 README

오프라인 한국어 성경 뷰어 템플릿(안드로이드).
KOR1910 VPL 임포트, 권/장/절 탐색, 롱프레스 북마크, OSIS → 한국어 책 이름 매핑을 지원합니다.  
Kotlin, Jetpack Compose, SQLite로 작성되었습니다.

---

### 기능
- 권/장/절 단위 탐색
- 절 롱프레스로 북마크 추가/삭제
- 북마크 목록에서 빠른 이동, 롱프레스로 삭제 확인


---

### 요구 사항
- Java Development Kit (JDK): 17 이상 (예: Homebrew OpenJDK 17)
- Android SDK (Command-line Tools 포함)

---

### 환경 설정 (macOS 예시)
시스템에 맞게 경로를 조정하세요.

```bash
# JDK 17 경로 예시 (Homebrew)
export JAVA_HOME="/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home"

# Android SDK 경로 예시
export ANDROID_SDK_ROOT="$HOME/Library/Android/sdk"

# 선택: PATH 확장
export PATH="$JAVA_HOME/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH"
```

---

### 빌드 방법

```bash
chmod +x ./gradlew

# 디버그 APK
./gradlew --no-daemon assembleDebug

# 릴리스 APK (F-Droid 친화: 서명 없음)
./gradlew --no-daemon assembleRelease
```

**출력 경로**
- 디버그: `app/build/outputs/apk/debug/app-debug.apk`
- 릴리스: `app/build/outputs/apk/release/app-release-unsigned.apk`

---

### 데이터 & 에셋
- 책 이름 매핑 (OSIS/영문 → 한글): `app/src/main/assets/book_names_ko.json`
- 성경 본문 (KOR1910): `app/src/main/assets/kor_vpl/kor_vpl.txt` 또는 `app/src/main/assets/kor_vpl/kor_vpl.xml`

---

### 라이선스
- 소스 코드: GNU General Public License v3.0 (GPLv3) — `LICENSE` 참조
- 서드파티 구성요소: 각 라이선스에 따름 — `NOTICE` 참조
- KOR1910 성경 본문: Public Domain, eBible.org 제공
- Android / Jetpack Compose: Apache License 2.0

---

### 저작권
Copyright (C) 2025  
GPLoyalist, GPLaider

---

### 리포지토리 구조

```text
app/src/main/java/com/opensourcebible/app/  # Kotlin 소스
app/src/main/assets/                       # 데이터 에셋
app/src/main/res/                          # Android 리소스
gradle/, gradlew*, build.gradle.kts        # Gradle 빌드 시스템
settings.gradle.kts
```

---

### 기여
기여를 환영합니다.  
중대한 변경 전에는 이슈를 열거나 PR로 먼저 논의해 주세요.

---

### 감사의 말
- eBible.org — KOR1910 성경 본문 제공 (Public Domain)
- Google LLC — Android 및 Jetpack Compose 라이브러리 (Apache License 2.0)


