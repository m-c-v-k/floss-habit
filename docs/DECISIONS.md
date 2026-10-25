# Decisions log

Newest first. Format: date, decision, why. Product decisions made before this log are in the Decisions table in `SPEC.md`.

- **2026-10-25**: A habit can be logged more than once a day. Every log is kept so the history can show a heatmap of logs per day; streaks still count each day once. After the first log the button reads "Log again"; undo removes the last log.
- **2026-10-25**: History view defaults to the last 7 days, with month and all-time views.
- **2026-10-25**: Room database v1 with one `habit_log` table: `id` (auto), `habit` (enum name), `timestamp` (epoch millis, UTC), `local_date` (ISO text, fixed at log time), indexed on (`habit`, `local_date`). Text dates stay readable and sort correctly. Time comes from `java.time.Clock` and the zone is read at each log, so travel applies without a restart; no custom clock interface.
- **2026-10-25**: App data is included in Android backup (`allowBackup="true"` with backup rules), so streaks survive a phone change. It stays optional: users turn backup on or off in system settings, and on Android 12+ cloud backup only runs when it is end-to-end encrypted. No in-app toggle for now.
- **2026-10-25**: Toolchain matches Android_Training_App: Gradle 9.4.1, AGP 9.2.1 (built-in Kotlin), Kotlin 2.2.10, compile SDK 36.1, Java 17. One set of versions across Marcus's apps; compile SDK 36.1 because the local `android-36` platform has no `android.jar`.
- **2026-10-25**: No dependency-injection library; objects are created by hand in one place. Keeps the build small until a real need appears.
- **2026-10-21**: Health content only from government or public-sector sources in the country of each language: Swedish text from Swedish sources (1177, Folktandvården, Socialstyrelsen), English text from UK sources (NHS, GOV.UK). ADA MouthHealthy is dropped as a US, non-government source. Keeps each language's advice consistent with the health system its readers use.
- **2026-10-21**: Reminders use WorkManager, not exact alarms. It survives reboot and needs no exact-alarm permission; a reminder arriving a few minutes late is fine for a daily habit.
- **2026-10-21**: Package name `com.mcvk.flosshabit` (same pattern as `com.mcvk.trainingapp`), replacing the `com.example.flosshabit` placeholder. Set before the `:app` module exists, because the applicationId cannot change after release.
- **2026-10-17**: Min SDK 26 (Android 8.0), target SDK 36. Notification channels (one per habit), `java.time` (used in `:core`) and adaptive icons are all built in from 26, so no compatibility paths or desugaring are needed; it still covers about 97% of active devices.
- **2026-10-17**: App name **Floss Streak**, icon a tooth. The name says what the app tracks. The package name stays `com.example.flosshabit` for now; renaming it is a separate decision.
- **2026-10-17**: Same git and coding standards as Android_Training_App (see `CLAUDE.md`): protected `dev` and `main`, feature branches squash-merged into `dev`, `dev` merged into `main` for a prod release, prod releases tagged `vX.Y.Z`. Keeps one way of working across Marcus's apps.
- **2026-10-17**: APKs are built by GitHub Actions and published as rolling releases `dev-latest` / `prod-latest`, signed with a keystore stored as GitHub secrets; `dev` and `prod` flavours install side by side.
- **2026-10-17**: Pure-Kotlin `:core` module for domain logic (streaks) next to the Android `:app` module. Cloud sessions cannot reach the Android SDK, so testable logic must build without it; this is the one deliberate split.
