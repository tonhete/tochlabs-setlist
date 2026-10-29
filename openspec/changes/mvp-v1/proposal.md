# Proposal

## Why

Musicians who play in several bands keep their setlists in notes apps, spreadsheets or on paper. Per-song details that change from gig to gig (key, tempo, instrument, tuning, transitions) get lost, total running time is guessed, and gig logistics live somewhere else. TochLabs Setlist is a free, ad-free, open-source app that keeps all of this in one place, per band. This change defines the first release (MVP), so that design and code start from an agreed specification.

## What Changes

- Add bands, with a band filter across the whole app, and a history where bands are archived and restored instead of deleted.
- Add a song catalogue entered by hand, with a repertoire per band. Songs store their original key, tempo, tuning and an optional duration.
- Add setlists with name and date: reorderable items (songs and stops), per-setlist overrides of key, tempo, instrument, tuning and note, links between consecutive songs, and a duplicate-song warning.
- Add duration calculation: total with or without stops, a configurable pause between songs, and a clear "incomplete" state when songs have no duration.
- Add a stage mode for reading a setlist song by song while performing.
- Add export to PDF (stage and list layouts) and printing.
- Add a `.setlist` file to share a setlist with the band and import it on another device.
- Add a calendar of events (gigs and rehearsals) with a day sheet of timed milestones and an optional linked setlist.
- Add app settings and full Spanish and English localization.

**Out of scope for the MVP:** online song search or autocomplete from external databases (planned later), opening venues in a maps app, accounts, cloud sync, ads and analytics.

## Capabilities

### New Capabilities
- `bands`: create, edit and filter bands; archive, restore and permanently delete bands through a history.
- `song-catalog`: manage songs entered by hand and the repertoire of each band.
- `setlists`: build and edit setlists with songs, stops, per-setlist overrides, links and change markers.
- `setlist-duration`: compute the total duration of a setlist, with or without stops and pauses.
- `stage-mode`: show a setlist one item at a time for use on stage.
- `setlist-export`: export a setlist as PDF (stage or list layout) and print it.
- `setlist-sharing`: export and import a setlist as a `.setlist` file.
- `event-calendar`: manage gigs and rehearsals with a roadmap of timed milestones, and link a setlist.
- `app-settings`: app-level preferences, including language and the default pause between songs.
- `localization`: Spanish and English user interface and formatting.

### Modified Capabilities

None. This is the first change; there are no existing specs.

## Impact

- New application: greenfield Kotlin Multiplatform codebase (Android first, iOS after Android works). No code exists yet beyond the generated scaffold.
- All data is stored locally on the device; the MVP uses no network and no accounts. Sharing happens through files.
- Affects every module: `core:model`, `core:domain`, `core:data`, `core:ui` and the `feature:*` modules defined in the project structure.
- Supporting product documents live in `docs/product/`: data model, screens and flows, and store listing (ASO). The technical design is `design.md` in this change.
