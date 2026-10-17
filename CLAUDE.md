# floss-habit: working agreements

Kotlin + Jetpack Compose Android app (with Glance widgets) for building a flossing and mouthwash habit. Plan and scope: `SPEC.md`. Keep that file current when scope changes.

## Environment

- Windows, PowerShell. Gradle daemon uses JDK 21; Android SDK at the path in `local.properties` (not committed).
- Modules: `:core` is pure Kotlin (habits, logs, streaks) and builds anywhere, including cloud sessions that cannot reach the Android SDK. The Android `:app` module depends on it and is built on the PC.
- Two product flavours in `:app`: `dev` (applicationId suffix `.dev`, app name ending "(dev)", built from `dev`) and `prod` (built from `main`). Both install side by side.
- Build: `.\gradlew.bat assembleDevDebug` (or `assembleProdDebug`). Unit tests: `.\gradlew.bat :core:test testDevDebugUnitTest`. Lint: `.\gradlew.bat lintDevDebug`.
- CI (`.github/workflows/build.yml`) runs build, unit tests and lint on every PR and push to `dev`/`main`; a PR should not be merged with a red build. Until `:app` exists it runs `./gradlew test`.
- Instrumented tests (`app/src/androidTest`: DB migrations, DAOs) don't run in CI. Run them locally on the emulator for every PR that touches the database: `.\gradlew.bat connectedDevDebugAndroidTest`.
- APK publishing (`.github/workflows/release-apk.yml`): every push to `dev`/`main` builds a signed release APK and replaces the rolling GitHub Release `dev-latest` (pre-release) / `prod-latest`. `versionCode` = CI run number. Signing key comes from GitHub secrets created by `scripts/setup-signing.ps1` (the keystore lives outside the repo; never commit it).
- `gh` lives at `C:\Program Files\GitHub CLI\gh.exe` (may not be on PATH in already-open terminals).
- Version catalog in `gradle/libs.versions.toml`; add dependencies there, not inline.
- Keep the `google()` repository limited to `com.android`, `com.google` and `androidx` groups, so Kotlin dependencies resolve without dl.google.com.

## Git workflow

- Two long-lived branches: **`dev`** (dev release, latest integrated work) and **`main`** (prod release, stable). Never commit directly to either.
- One branch per change, cut from `dev`, named `feature/<short-name>`, `fix/<short-name>` or `docs/<short-name>`.
- Small, focused commits with imperative messages ("Add streak calculator").
- Open a pull request **into `dev`** for every branch; describe what changed and how it was tested. Squash-merge.
- When `dev` is stable (builds, tests pass, manually checked), open a PR `dev` -> `main` for a prod release. Merge with a merge commit (not squash) so `dev` and `main` don't diverge. Tag prod releases `vX.Y.Z`.
- Hotfixes for prod go on `fix/*` from `main`, PR into `main`, then merge `main` back into `dev`.
- Before opening a PR: the app builds, unit tests pass, no unrelated changes.
- Use the `gh` CLI for commits/push/pull/PRs. Commit and push only when asked (or when a milestone step explicitly includes it). Never force-push or rewrite history on `dev` or `main`.
- Never commit `local.properties`, keystores, or secrets.

## Code conventions

- Package root `com.example.flosshabit` (placeholder until the final name is chosen, see `SPEC.md`); group by feature (`dashboard/`, `widgets/`, `reminders/`, `learn/`, ...) with `data/`, `ui/`, `domain/` inside when needed.
- MVVM: Composables are stateless where possible; ViewModels expose `StateFlow<UiState>` and take events as functions.
- **Build for reversal:** prefer choices that are cheap to undo (small commits, additive migrations, no big-bang rewrites).
- **Loosely coupled, not over-modular:** `:core` + `:app` only; small functions and classes with one job; interfaces only at real seams (storage, clock, reminders); features never reach into each other's internals. Keep domain logic (streaks, scheduling rules) pure Kotlin in `:core` so it is unit-testable.
- Keep it simple: no new library, module or abstraction unless it solves a current problem.
- UI in Swedish and English: all user-facing text lives in `strings.xml` (`values/` English, `values-sv/` Swedish), never hard-coded in Composables.
- Health content comes from official sources only, each with a visible source link (rules in `SPEC.md`).
- Match the style of surrounding code; default Kotlin official style.
- Add unit tests for non-trivial logic (streaks, date and time zone handling, mappers). Add Room tests for DAOs.
- Room schema changes require a migration and an exported schema; never edit a released schema in place.

## How we work together (Claude + Marcus)

- Plan first for anything bigger than a small fix: propose approach, wait for agreement, then implement.
- Work in small steps; each milestone in `SPEC.md` is split into PR-sized pieces.
- Ask before decisions that are hard to reverse (data model changes, dependencies, renaming the package).
- After changes, build and run tests, and report honestly what passed and what did not.
- Record decisions that matter in `docs/DECISIONS.md` (short entries: date, decision, why).
