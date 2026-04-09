# AGENTS.md

## Cursor Cloud specific instructions

### Project overview
This is **TheClassicMagicBall** — an Android Magic 8-Ball simulator app (`net.gerosyab.magicball`). It uses Android SDK 27, Java 8, Gradle 4.4, and Android Gradle Plugin 3.1.3. There are no test source files in the repo despite test dependencies being declared.

### Environment requirements
- **JDK 8** (`/usr/lib/jvm/java-8-openjdk-amd64`) — required by Android Gradle Plugin 3.1.3. JDK 9+ will fail.
- **Android SDK** (`/opt/android-sdk`) with platform `android-27`, build-tools `27.0.3`, and `platform-tools`.
- Environment variables are set in `~/.bashrc`: `JAVA_HOME`, `ANDROID_HOME`, `ANDROID_SDK_ROOT`, `PATH`.

### Key commands
| Task | Command |
|---|---|
| Build debug APK | `./gradlew assembleDebug` |
| Lint | `./gradlew lint` |
| Unit tests | `./gradlew test` (no test sources exist — exits immediately) |
| Full clean build | `./gradlew clean assembleDebug lint test` |

### Gotchas
- The `sdkmanager` CLI requires JDK 11+ to run, but the Gradle build requires JDK 8. When installing new SDK packages, temporarily set `JAVA_HOME` to JDK 21 (`/usr/lib/jvm/java-21-openjdk-amd64`).
- `jcenter()` is used as a repository — it is in read-only mode. Dependency resolution still works but no new artifacts will ever be published there.
- There are no instrumentation tests (`androidTest`) or unit tests (`test`) source files. The `./gradlew test` task completes as NO-SOURCE.
- The app cannot be run end-to-end in the cloud VM because it requires an Android emulator or physical device (not available in this environment). The APK build is the primary verification step.
- Lint reports 35 issues (pre-existing) — see `app/build/reports/lint-results.html`.
