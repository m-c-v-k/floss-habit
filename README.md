# floss-habit

Android app (Kotlin, Jetpack Compose, Glance) that helps you build the habit of flossing and using mouthwash, with widgets, reminders and streaks.

Status: planning. See [SPEC.md](SPEC.md) for the full specification and milestones.

## Getting started
1. Clone the repo.
2. Open it in Android Studio.
3. Follow the milestones in `SPEC.md`.

## Branches and releases
- Work happens on `feature/*`, `fix/*` or `docs/*` branches cut from `dev`, merged into `dev` by pull request.
- `dev` publishes a dev APK (pre-release `dev-latest`); `main` publishes the prod APK (`prod-latest`).
- A prod release is a pull request from `dev` into `main`, tagged `vX.Y.Z`.

Working agreements: [CLAUDE.md](CLAUDE.md). Decisions: [docs/DECISIONS.md](docs/DECISIONS.md).
