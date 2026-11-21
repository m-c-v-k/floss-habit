# floss-habit — Specification

An Android app that helps people build daily habits they choose themselves (flossing, mouthwash, drinking water, eating fruit, ...), with home-screen widgets, reminder notifications, streak tracking, and, for habits built from a dental template, information based only on official sources.

The app started as a flossing and mouthwash tracker. Floss and Mouthwash stay as the first two habits for existing installs and as starter templates for new ones.

## Goals
- Let users create their own daily habits, either yes/no ("floss") or counted toward a daily target ("drink 8 glasses of water").
- Make logging a habit take one tap, from a widget, a notification, or the app.
- Show progress through a separate streak for each habit.
- Offer benefit and how-to information for dental templates, from official sources only.

## Decisions
| Topic | Decision |
|---|---|
| Scope | General daily habit tracker with user-created habits (2026-11-21, see `docs/DECISIONS.md`) |
| Habit goal | Every habit has a daily target N ≥ 1. Yes/no habits are target 1; counted habits are target N, each log adding one |
| Frequency | Daily habits only for now; "x times a week" is out of scope |
| Target changes | Streaks and history are always judged against the habit's current target |
| Languages | Swedish and English, via standard Android string resources. User-typed habit names are shown as typed; template names are translated |
| Streaks | Separate per habit; each has a current and a longest streak |
| Mouthwash timing | Included in the Mouthwash template's articles, with a short note wherever official sources differ, each linked to its source |
| Sync | Project lives in this GitHub repo (`m-c-v-k/floss-habit`); commits per milestone |
| App name | Floss Streak for now (dev flavour: "Floss Streak (dev)"); a general name is an open item |
| App icon | A tooth, as an adaptive icon, until the new name is chosen |
| Package name | `com.mcvk.flosshabit` (dev flavour adds `.dev`); kept after widening, because users never see it and changing it would wipe existing installs |
| Reminders | WorkManager; no exact alarms |
| Content sources | Government and public-sector sources only, from the country of each language: Swedish from Sweden, English from the UK |

## Tech stack
- Kotlin, Jetpack Compose (Material 3)
- Jetpack Glance for widgets
- Room for local storage (single source of truth shared by app, widgets and notification actions)
- WorkManager for scheduling reminders
- Min SDK 26 (Android 8.0), target SDK 36

## Data model
`Habit`: `id`, `name`, `template` (nullable template key, for example `FLOSS`), `icon`, `color`, `dailyTarget` (≥ 1), `unit` (optional label, for example "glasses"), `reminderTime` (optional local time), `sortOrder`, `archived`, `createdAt`.

`HabitLog`: `id`, `habitId`, `timestamp` (UTC), `localDate` (derived in the user's time zone at log time). One log is one unit toward the target.

Archiving hides a habit and keeps its logs; deleting a habit (with confirmation) also deletes its logs.

Room v1 → v2 migration: create the `habit` table, insert Floss and Mouthwash (target 1, templates `FLOSS` and `MOUTHWASH`), and replace `habit_log.habit` (enum name) with `habit_id`, mapping each existing log to its habit. No logs are lost.

Streak rules, calculated per habit:
- A day counts if the number of logs for that habit on that local date is at least the habit's daily target.
- Current streak: consecutive counted days ending today, or ending yesterday if today is not yet done, so a streak is not shown as broken before the day is over.
- Longest streak: the longest run of consecutive counted days.
- Edge cases to test: time zone changes, DST, logging just after midnight, multiple logs on one day, a partly met target, a target changed after logging.

## Features

### Habits
- Add a habit from a template (Floss, Mouthwash, Drink water, Eat fruit, ...) or from scratch: name, icon, colour, daily target, optional unit, optional reminder time.
- Edit, reorder, archive and delete habits.
- A fresh install starts with no habits and offers the templates; existing installs keep Floss and Mouthwash.

### Widgets (Glance)
- One widget instance per habit, chosen when the widget is placed, plus an optional combined widget for several habits.
- Shows today's progress (done, or "5/8" for counted habits), current streak, and a one-tap "Done" / "+1" button.
- Writes to the same Room database and refreshes all widgets after a log.

### Notifications
- Optional daily reminder per habit at a user-chosen time, with a "Done" / "+1" action button that logs without opening the app.
- One notification channel per habit, created and removed with the habit.
- Android 13+ `POST_NOTIFICATIONS` runtime permission.
- Reminders are scheduled with WorkManager, which survives reboot and needs no exact-alarm permission. Delivery is inexact (a reminder may arrive some minutes late); test it against battery optimisation.

### App
- **Dashboard:** today's list of active habits in the user's order, with progress toward each target, and current and longest streaks.
- **History:** a heatmap of logs per day for each habit, with the total number of days the target was met (all time). Ranges: the last 7 days (default), the current calendar month, and all time (from the first log). Month and all time are shown as Monday-first calendar weeks. Shading has four steps: no logs, under half the target, at least half, and target met. A target-1 habit shows only "no logs" and "target met".
- **Logging:** log from the dashboard as often as wanted (every log is kept for the history; the streak counts the day once the target is met); undo the last log.
- **Learn:** benefits and how-to articles attached to dental templates (Floss and Mouthwash, including mouthwash timing relative to brushing). A habit made from such a template shows its articles; other habits have none.
- **Settings:** language, notification permission status. Reminder times are set per habit.

## Content and sources
All health content must come from official government or public-sector sources only, from the country of the language: Swedish text from Swedish sources, English text from UK sources. Every article shows a visible "Source" link, and nothing is stated that its source does not support. Articles belong to templates, never to user-made habits; a new template gets articles only once its sources are verified.

Candidate sources, each page to be verified before any text is bundled:
- Swedish (Sweden): 1177 (the regions' healthcare guide), Folktandvården (public dental care), Socialstyrelsen
- English (UK): NHS (nhs.uk), GOV.UK (for example "Delivering better oral health")

Content rules:
- Paraphrase minimally; keep wording close to the source.
- Where sources differ (for example mouthwash timing), say so briefly and link each source.
- Store each article with: template, title, body, source name, source URL, language, and date the source was checked.
- Add a short disclaimer that the app is a habit aid and not medical advice.

## Milestones
1. Project scaffold, Room database, streak logic with unit tests. *(done)*
2. Dashboard and in-app logging, history heatmap. *(done)*
3. Custom habits, as PR-sized steps:
   1. `:core`: `Habit` becomes data with an id and a daily target; streak and history logic judge days against the target; tests for partial and changed targets.
   2. Room v2: `habit` table, `habit_log.habit_id`, migration seeding Floss and Mouthwash, migration test.
   3. Dashboard and history driven by the habit list, with "5/8" progress and target-based shading.
   4. Add, edit, reorder, archive and delete habits, with templates.
4. Glance widgets (per habit, chosen at placement).
5. Notifications with Done action (per habit).
6. Learn section with verified source content for dental templates (sv and en).
7. Polish, accessibility, release prep, including the new app name and icon.

## Open items
- Choose a general app name and icon to replace "Floss Streak" and the tooth.
- Pick the starter template list and their default targets and icons.
- Verify exact source pages and wording for each article.
- Privacy: no accounts or analytics unless decided otherwise. Data is stored on the device and included in Android backup, which the user can turn off in system settings (see `docs/DECISIONS.md`).
