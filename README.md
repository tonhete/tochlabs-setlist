# TochLabs Setlist

A free, ad-free, open-source setlist manager for musicians who play in more than one band.

> **Status:** early development. The repository currently contains the project scaffold only; features are being built following a spec-driven workflow (specification and design before code).

## Planned MVP

- **Bands** with their own repertoire, and a filter to switch between them
- **Song catalogue** (entered by hand): key, BPM, tuning and duration per song
- **Setlists** with name and date: drag-and-drop ordering, stops (speech, break, tuning, encore), links between songs with transition notes, per-song instrument, tuning and notes, and total duration with or without stops
- **Stage mode** for reading the setlist on stage
- **Export** to PDF, print, and share a `.setlist` file with the rest of the band
- **Calendar** of events with a day sheet: arrival, backline, soundcheck, stage time, gig, load-out, parking and contact
- **Band history**: archive and restore bands instead of deleting them
- English and Spanish from day one

## Tech stack

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform/) with [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) for the UI
- Android first; iOS as soon as the Android version works
- Koin (dependency injection) and Room KMP (persistence), to be added with the first features
- Gradle with convention plugins (`build-logic`) and a version catalog (`gradle/libs.versions.toml`)

## Project structure

```
androidApp/      Android host application (entry point)
iosApp/          iOS host application (Xcode project)
shared/          App root: root UI, navigation, DI; exported to iOS as the "Shared" framework
core/
  model/         Domain entities
  domain/        Business rules and use cases
  data/          Persistence and repositories
  ui/            Shared UI components and theme
feature/
  setlists/  songs/  bands/  calendar/  stage/  share/
build-logic/     Gradle convention plugins
```

## Getting started

Requirements: Android Studio (with the Kotlin Multiplatform plugin) and a recent JDK. Gradle provisions the JDK toolchain it needs. Building the iOS app requires macOS and Xcode.

```bash
# Build the Android debug app
./gradlew :androidApp:assembleDebug

# Run the unit tests
./gradlew testAndroidHostTest
```

Or open the project in Android Studio and run the `androidApp` configuration on an emulator or device.

## License

Licensed under the [GNU General Public License v3.0](LICENSE).
