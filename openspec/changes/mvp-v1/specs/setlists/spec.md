# Spec Delta

## Purpose

Lets a musician build the list of songs and stops for a performance, adjust each song for that performance, and see at a glance what changes from one song to the next.

## ADDED Requirements

### Requirement: Create and edit setlists
The system SHALL let the user create a setlist belonging to one active band, with a required name and an optional date, and edit them later. A name MUST NOT be empty after trimming whitespace.

#### Scenario: Create a setlist
- **WHEN** the user creates a setlist for the band "Mareas" named "Concierto Sala Norte" with a date
- **THEN** the setlist is saved for that band and listed with its date

#### Scenario: Setlist without date
- **WHEN** the user saves a setlist without a date
- **THEN** it is saved and shown as having no date

### Requirement: Upcoming and past setlists
The system SHALL list setlists that have no date or a date from today onwards as upcoming, ordered by date with undated ones last. Setlists dated before today SHALL be grouped as past, newest first, and hidden until the user chooses to show them.

#### Scenario: Past setlists collapsed
- **WHEN** the user opens the setlist list and some setlists are dated before today
- **THEN** only upcoming setlists are shown, together with a control that shows the number of past ones

#### Scenario: Show past setlists
- **WHEN** the user chooses to show past setlists
- **THEN** they are listed newest first

### Requirement: Songs and stops as items
A setlist SHALL be an ordered list of items, each either a song from the catalogue or a stop. A stop SHALL have a type (speech, break, tuning change or encore), a duration and an optional note.

#### Scenario: Add a song
- **WHEN** the user adds a catalogue song to a setlist
- **THEN** it is appended as the last item

#### Scenario: Add a stop
- **WHEN** the user adds a break with a duration of 15 minutes
- **THEN** a stop of type break and 15 minutes is appended as the last item

### Requirement: Reorder and remove items
The system SHALL let the user change the order of items by dragging them and by move-up and move-down controls, and SHALL let the user remove an item from a setlist without deleting the song from the catalogue.

#### Scenario: Reorder by dragging
- **WHEN** the user drags the third item to the first position
- **THEN** the order is updated and the positions are renumbered

#### Scenario: Remove a song from a setlist
- **WHEN** the user removes a song item
- **THEN** the item disappears from the setlist and the song stays in the catalogue

### Requirement: Per-setlist overrides
For each song in a setlist the system SHALL let the user override the key, the tempo (BPM), the instrument, the tuning and add a note. Instrument and tuning MUST be free text of at most 20 characters. An item without an override SHALL use the song's original value.

#### Scenario: Override the key
- **WHEN** the user sets the key of one setlist item to "G" while the song's original key is "A"
- **THEN** the item shows "G" and the original "A" stays visible as reference

#### Scenario: Instrument and tuning limits
- **WHEN** the user types more than 20 characters as instrument or tuning
- **THEN** only the first 20 characters are kept

### Requirement: Change markers
The system SHALL mark an item when, compared with the previous song in the setlist, its instrument or tuning changes, and when its key or tempo differs from the song's original. The first song SHALL be compared against standard tuning. The markers MUST be available in the setlist view, in stage mode and in exports.

#### Scenario: Instrument change
- **WHEN** song 2 uses "Strato" and song 3 uses "Acústica"
- **THEN** song 3 is marked as an instrument change to "Acústica"

#### Scenario: Tuning change
- **WHEN** a song uses a tuning different from the previous song's tuning
- **THEN** the song is marked as a tuning change

#### Scenario: Key differs from original
- **WHEN** an item's key differs from the song's original key
- **THEN** the item is marked with the setlist key and the original key

### Requirement: Links between songs
The system SHALL let the user link a song to the next item, with a transition text such as "end on E". A link SHALL only take effect when the next item is a song, and it MUST be shown in the setlist view, in stage mode and in exports. The transition text SHALL be optional.

#### Scenario: Link two consecutive songs
- **WHEN** the user links song 1 to song 2 with the text "End on A and drums come in"
- **THEN** both songs are shown as linked and the text is displayed

#### Scenario: Link broken by a stop
- **WHEN** a stop is placed between two linked songs
- **THEN** the link has no effect until the songs are consecutive again

### Requirement: Duplicate songs allowed with a warning
The system SHALL allow the same song to appear more than once in a setlist. Every repeated occurrence MUST be marked as repeated, and the user MUST be warned when adding one.

#### Scenario: Add a song already present
- **WHEN** the user adds a song that is already in the setlist
- **THEN** the song is added and the user is warned that it was already there

#### Scenario: Repeated marker
- **WHEN** a song appears twice in a setlist
- **THEN** the second occurrence is marked as repeated

### Requirement: Delete a setlist
The system SHALL delete a setlist only after explicit confirmation. Deleting a setlist MUST NOT delete its songs, and a linked event MUST remain without a setlist.

#### Scenario: Confirmed deletion
- **WHEN** the user confirms the deletion of a setlist linked to an event
- **THEN** the setlist is removed, the songs remain in the catalogue and the event remains with no linked setlist
