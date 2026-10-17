# floss-habit — Specification

An Android app that helps people build the habit of flossing and using mouthwash, with home-screen widgets, reminder notifications, streak tracking, and information based only on official sources.

## Goals
- Make logging a habit take one tap, from a widget, a notification, or the app.
- Show progress through separate streaks for flossing and mouthwash.
- Offer benefit and how-to information that comes only from official sources.

## Decisions
| Topic | Decision |
|---|---|
| Languages | Swedish and English, via standard Android string resources |
| Streaks | Separate for floss and mouthwash; each has a current and a longest streak |
| Mouthwash timing | Included, with a short note wherever official sources differ, each linked to its source |
| Sync | Project lives in this GitHub repo (`m-c-v-k/floss-habit`); commits per milestone |
| App name | Floss Streak (dev flavour: "Floss Streak (dev)") |
| App icon | A tooth, as an adaptive icon |

## Tech stack
- Kotlin, Jetpack Compose (Material 3)
- Jetpack Glance for widgets
- Room for local storage (single source of truth shared by app, widgets and notification actions)
- WorkManager for scheduling reminders
- Min SDK 26 (Android 8.0), target SDK 36

## Data model
`HabitLog`: `id`, `habit` (`FLOSS` | `MOUTHWASH`), `timestamp` (UTC), `localDate` (derived in the user's time zone at log time).

Streak rules, calculated per habit:
- A day counts if at least one log exists for that habit on that local date.
- Current streak: consecutive counted days ending today, or ending yesterday if today is not yet done, so a streak is not shown as broken before the day is over.
- Longest streak: the longest run of consecutive counted days.
- Edge cases to test: time zone changes, DST, logging just after midnight, multiple logs on one day.

## Features

### Widgets (Glance)
- One widget per habit, plus an optional combined widget.
- Shows today's status, current streak, and a one-tap "Done" button.
- Writes to the same Room database and refreshes all widgets after a log.

### Notifications
- Daily reminder per habit at a user-chosen time, with a "Done" action button that logs without opening the app.
- Notification channel per habit.
- Android 13+ `POST_NOTIFICATIONS` runtime permission.
- Reminder scheduling must survive reboot and be tested against battery optimisation. Decide between WorkManager and exact alarms on the PC; exact alarms need extra permission handling.

### App
- **Dashboard:** today's checklist for both habits, current and longest streaks, a week or month calendar view.
- **Logging:** log from the dashboard; undo the last log.
- **Learn:** benefits and how-to articles for flossing and mouthwash, including mouthwash timing relative to brushing.
- **Settings:** reminder times, language, notification permission status.

## Content and sources
All health content must come from official sources only. Every article shows a visible "Source" link, and nothing is stated that its source does not support.

Candidate sources, each page to be verified before any text is bundled:
- Swedish: 1177 (Swedish healthcare guide), Folktandvården, Socialstyrelsen
- English: NHS, ADA MouthHealthy

Content rules:
- Paraphrase minimally; keep wording close to the source.
- Where sources differ (for example mouthwash timing), say so briefly and link each source.
- Store each article with: title, body, source name, source URL, language, and date the source was checked.
- Add a short disclaimer that the app is a habit aid and not medical advice.

## Milestones
1. Project scaffold, Room database, streak logic with unit tests.
2. Dashboard and in-app logging.
3. Glance widgets.
4. Notifications with Done action.
5. Learn section with verified source content (sv and en).
6. Polish, accessibility, release prep.

## Open items
- Verify exact source pages and wording for each article.
- Choose the reminder scheduling approach.
- Package name (`com.example.flosshabit` as a placeholder).
- Privacy: all data stays on the device; no accounts or analytics unless decided otherwise.
