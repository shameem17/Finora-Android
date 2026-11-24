# Finora for Android

Native Android port of the offline personal-finance tracker [Finora (iOS)](https://github.com/shameem17/Finora).
All data stays on the device: the app declares no `INTERNET` permission and disables backup.

**Stack:** Kotlin 2.0, Jetpack Compose (Material 3), Navigation-Compose, Room (KSP), DataStore, coroutines/Flow.
**Architecture:** MVVM-C, the same as iOS. A `Coordinator` owns each tab's navigation, ViewModels expose `StateFlow`,
repositories are interfaces, and dependencies are wired by hand in `AppContainer`.

```
app/src/main/java/com/shameem/finora
  app/          Application, activity, settings, dependency container, tabs
  coordinator/  Coordinator protocol, app/main/tab coordinators
  core/         design system (iOS palette, Manrope), shared UI, utilities
  services/     persistence (Room), date, loan reminders (AlarmManager)
  screens/      home, transaction, accounts, categories, income/expense (cashflow), savings, loan, settings, onboarding
                each: model / repository / viewmodel / view / coordinator
```

## Status

All nine iOS tabs and onboarding are ported with the same business rules (validation messages, atomic balance
adjustments, cascading deletes) and visual design.

**This code has not been compiled yet.** It was written in an environment with no Android SDK or Gradle access, so
expect a few compile fixes on the first build in Android Studio (imports, small signature mismatches).
Open the project, sync Gradle, build, then run `./gradlew test` (pure-JVM tests for the loan, insight and net-worth logic).

Known gaps compared with iOS:
- Loan due-date reminders use an inexact `AlarmManager` alarm at 09:00; Android 13+ asks for notification permission from Settings.
- SF Symbols are mapped to the closest Material icons (`core/ui/SfSymbols.kt`); a few glyphs differ.
- Detail screens are navigation destinations that slide up, standing in for iOS modal sheets.
