## 1. Foundations and quality gates

- [x] 1.1 Add `CLAUDE.md` at the repo root (architecture, module rules, conventions, TDD rule for `core:domain`, how to run tests). Verify: file reviewed against `design.md`.
- [x] 1.2 Add GitHub Actions workflow: build `androidApp`, run all host tests on every push and pull request. Verify: first run is green on `main`.
- [x] 1.3 Add detekt and ktlint to the build and to the workflow; fix findings in existing code. Verify: `./gradlew detekt ktlintCheck` passes locally and in CI.
- [x] 1.4 Add Dependabot configuration for Gradle and GitHub Actions. Verify: file validated by GitHub (no error in the Dependabot tab).
- [ ] 1.5 Add and verify against official release pages the versions of KSP, `kotlinx-coroutines`, `kotlinx-datetime` and `kotlinx-serialization` in `libs.versions.toml`. Verify: sync succeeds.
- [ ] 1.6 Remove template demo code (`Greeting`, `Platform*`, demo tests, demo UI) once the first real screen exists. Verify: build and tests pass.

## 2. Model and domain rules (test first)

- [ ] 2.1 Write tests in `core:domain` for validation (non-blank names and titles, 20-character instrument and tuning, pause 0 to 120 in steps of 5) from the `bands`, `song-catalog` and `app-settings` specs. Verify: tests fail for the right reason.
- [ ] 2.2 Define `core:model` types (ids, Band, Song, Setlist, SetlistItem song/stop, Event, EventMilestone, settings) and implement validation. Verify: 2.1 tests pass.
- [ ] 2.3 Write tests for duration calculation from `setlist-duration` (music-only, with stops, one pause per consecutive pair, linked pairs, stops as items, songs without duration, overrides do not change durations). Verify: tests fail.
- [ ] 2.4 Implement `calculateDuration`. Verify: 2.3 tests pass.
- [ ] 2.5 Write tests for change markers and link effectiveness from `setlists` (instrument, tuning, link only effective before a song, repeated songs). Verify: tests fail.
- [ ] 2.6 Implement `computeMarkers` and link rules. Verify: 2.5 tests pass.
- [ ] 2.7 Write tests and implement text normalisation (case and accent insensitive) used by search and import matching. Verify: tests pass.
- [ ] 2.8 Define repository interfaces and `Clock` in `core:domain`. Verify: modules compile.

## 3. Persistence

- [ ] 3.1 Vertical slice spike: Room 3 plugin and KSP set up, one entity, one DAO, one host test with an in-memory database, running on Android. Verify: test green in Gradle and CI. Decide go/no-go on Room 3 here.
- [ ] 3.2 Implement all entities, DAOs, converters, foreign keys and indexes from `design.md`; export schema v1. Verify: schema file committed.
- [ ] 3.3 Write integration tests for cascades, `RESTRICT` on songs in use, `SET_NULL` on events, and item ordering. Verify: tests fail, then pass with 3.2.
- [ ] 3.4 Implement mappers and repository implementations; atomic use cases (reorder, archive, restore, delete band, delete setlist). Verify: integration tests for each pass.
- [ ] 3.5 Implement `app_settings` storage with defaults. Verify: tests pass.
- [ ] 3.6 Platform database builders (Android, iOS) and Koin module for `core:data`. Verify: app starts on emulator and creates the database.

## 4. App shell

- [ ] 4.1 Theme and shared components in `core:ui` (chips, sheets, duration and date formatting). Verify: preview on emulator.
- [ ] 4.2 Koin `initKoin()`, Navigation 3 with four tabs (Setlists, Calendar, Songs, Bands) and serializable routes. Verify: tabs navigate on emulator, back stack restored after rotation.

## 5. Bands and song catalogue

- [ ] 5.1 Bands: ViewModel tests with fake repositories, then list, create, edit, archive, restore, history and permanent deletion with confirmation. Verify: `bands` scenarios checked manually.
- [ ] 5.2 Songs: ViewModel tests, then create, edit, optional duration, free-text tuning, search, delete only when unused. Verify: `song-catalog` scenarios checked manually.
- [ ] 5.3 Repertoire per band and band filter. Verify: `bands` filter and `song-catalog` repertoire scenarios.

## 6. Setlists

- [ ] 6.1 ViewModel tests, then create and edit setlists, upcoming and past lists, delete. Verify: `setlists` scenarios.
- [ ] 6.2 Add songs and stops as items; duplicate warning; per-setlist overrides. Verify: scenarios checked manually.
- [ ] 6.3 Reorder control with drag and move up/down buttons, remove items, links. Verify: reorder persists after restart.
- [ ] 6.4 Duration totals and breakdown toggle, change markers in the UI. Verify: totals match the domain tests on a sample setlist.

## 7. Stage mode

- [ ] 7.1 ViewModel tests, then stage mode screen with song and stop information, navigation between items, exit. Verify: `stage-mode` scenarios on a real device.

## 8. Calendar

- [ ] 8.1 ViewModel tests, then events, calendar and agenda views, milestones roadmap, logistics, linked setlist, archived bands. Verify: `event-calendar` scenarios.

## 9. Export and sharing

- [ ] 9.1 Layout model for stage and list PDFs with tests in common code. Verify: tests pass.
- [ ] 9.2 Android `PdfRenderer`, share and print. Verify: PDFs open and print from an emulator or device.
- [ ] 9.3 `.setlist` codec with round-trip tests, version check and invalid-file rejection tests. Verify: tests pass.
- [ ] 9.4 Export and import flows (song matching by normalised title and artist, single transaction), Android file association. Verify: export on one device, import on another, no duplicate songs.

## 10. Settings and localization

- [ ] 10.1 Spike: runtime language override with Compose resources on Android. Verify: language changes immediately; record result for iOS.
- [ ] 10.2 Spanish and English strings for all modules, localized formats, built-in labels, language-independent files. Verify: both languages walked through on emulator.
- [ ] 10.3 Settings screen: language, default pause, about. Verify: `app-settings` scenarios.

## 11. Release readiness

- [ ] 11.1 Full manual pass of each spec's main flow on emulator and a real device. Verify: checklist recorded in `docs/`.
- [ ] 11.2 Store listing from `docs/product/aso.md`, privacy statement, app icon. Verify: Play Console accepts the internal test upload.

## 12. iOS (after Android works)

- [ ] 12.1 macOS CI job compiling the iOS framework and running iOS-capable tests. Verify: job green.
- [ ] 12.2 iOS `actual`s: database path, PDF, print, share, incoming file, locale override. Verify: app runs on simulator with all flows.
