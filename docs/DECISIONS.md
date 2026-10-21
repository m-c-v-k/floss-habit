# Decisions log

Newest first. Format: date, decision, why. Product decisions made before this log are in the Decisions table in `SPEC.md`.

- **2026-10-17**: Min SDK 26 (Android 8.0), target SDK 36. Notification channels (one per habit), `java.time` (used in `:core`) and adaptive icons are all built in from 26, so no compatibility paths or desugaring are needed; it still covers about 97% of active devices.
- **2026-10-17**: App name **Floss Streak**, icon a tooth. The name says what the app tracks. The package name stays `com.example.flosshabit` for now; renaming it is a separate decision.
- **2026-10-17**: Same git and coding standards as Android_Training_App (see `CLAUDE.md`): protected `dev` and `main`, feature branches squash-merged into `dev`, `dev` merged into `main` for a prod release, prod releases tagged `vX.Y.Z`. Keeps one way of working across Marcus's apps.
- **2026-10-17**: APKs are built by GitHub Actions and published as rolling releases `dev-latest` / `prod-latest`, signed with a keystore stored as GitHub secrets; `dev` and `prod` flavours install side by side.
- **2026-10-17**: Pure-Kotlin `:core` module for domain logic (streaks) next to the Android `:app` module. Cloud sessions cannot reach the Android SDK, so testable logic must build without it; this is the one deliberate split.
