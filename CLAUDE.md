# TochLabs Setlist

Free, ad-free, open-source (GPL-3.0) setlist manager for musicians in several bands. Kotlin Multiplatform: Compose Multiplatform UI, Koin, Room 3. Android first, iOS after Android works.

Source of truth, in this order: `openspec/changes/mvp-v1/specs/` (behavior), `openspec/changes/mvp-v1/design.md` (technical decisions), `openspec/changes/mvp-v1/tasks.md` (work plan), `docs/product/` (data model, screens, ASO). Do not reopen decided points; change a spec or the design first, then the code.

## Architecture

One-way dependencies, enforced by Gradle modules:

```
androidApp -> shared -> feature:* -> core:ui, core:domain, core:model
                     -> core:data -> core:domain -> core:model
```

- `core:model`: immutable domain types. Depends on nothing.
- `core:domain`: repository interfaces, pure business rules, use cases. No Android, no Room, no DI.
- `core:data`: Room database, DAOs, mappers, repository implementations, `.setlist` codec, settings.
- `core:ui`: theme, shared components, shared strings.
- `feature:*` (setlists, songs, bands, calendar, stage, share): screens + ViewModels. Never depend on `core:data`.
- `shared`: composition root (Koin, navigation, `App()`, iOS framework `Shared`).
- `androidApp`: thin Android host (AGP 9 requires it separate).
- `build-logic`: convention plugins `tochlabs.kmp.library`, `tochlabs.compose`, `tochlabs.kmp.feature`. Versions only in `gradle/libs.versions.toml`.

Data flow: Room -> repository `Flow` -> ViewModel -> `StateFlow<UiState>` -> Compose. Writes are ViewModel functions calling `suspend` repositories or use cases. No business rule in composables.

## Conventions

- Code, comments, commits and docs in English. App strings in Spanish and English via Compose resources.
- Base package `com.tochlabs.setlist.<module>` (derived by the convention plugin).
- Immutable models, read-only exposed state, validation returns typed results (no exceptions for user input).
- Inject `Clock`; never read the current time directly in domain code.
- Platform code only behind small interfaces (`expect/actual` or injected), see `design.md` decision 7.
- Pin every dependency in the version catalog; check the official release page before adding or bumping.
- Commits: conventional style (`feat:`, `fix:`, `docs:`, `test:`, `build:`, `ci:`, `refactor:`).

## Testing (TDD in core:domain)

- No production code in `core:domain` without a failing test first. Name tests after the spec scenario they cover.
- Bug fix = first a test that reproduces it.
- `core:data`: integration tests with an in-memory database. ViewModels: tests with fake repositories. No UI tests in the MVP.
- Run tests (Windows PowerShell, repo root): `.\gradlew.bat testAndroidHostTest`; one module: `.\gradlew.bat :core:domain:testAndroidHostTest`.
- iOS compiles only on macOS; `kotlin.native.ignoreDisabledTargets=true` is set so Windows/Linux builds skip it.

## Workflow (autonomous)

- Specs are managed with OpenSpec (`openspec.cmd` on Windows). Validate with `openspec.cmd validate mvp-v1 --strict`.
- Work through `tasks.md` in order using `/opsx:apply mvp-v1`, several task groups per run, without waiting for approval between tasks. For each task: write the test first where `tasks.md` says so, implement, run `.\gradlew.bat ktlintCheck detekt testAndroidHostTest --continue`, fix failures, tick the checkbox only when its verification passes.
- Commit once per finished task group (conventional message, one logical change), push when the group is green. Never commit failing code. Check the GitHub Actions run after pushing and fix it if it fails.
- Check the current version of any library or tool on its official page before adding it.
- Report briefly when a group ends: what was done, what failed and how it was fixed. Explain new concepts in 2-3 lines comparing with Java/.NET.
- Stop and ask only when: a decision contradicts a spec or `design.md` and cannot be resolved by editing them reasonably; an irreversible action is needed (deleting data, force push, changing repo settings); installing software or touching anything outside the project folder; a spike (such as task 3.1) fails and the design must change.
- Never request, read or store the user's credentials.
