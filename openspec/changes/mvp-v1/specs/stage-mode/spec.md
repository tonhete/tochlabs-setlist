# Spec Delta

## Purpose

Shows a setlist one item at a time, large and with everything the musician needs for each song, so it can be read at a glance while performing.

## ADDED Requirements

### Requirement: Open stage mode
The system SHALL open stage mode from a setlist, starting at its first item. It MUST NOT open for a setlist with no items and MUST tell the user that the setlist is empty.

#### Scenario: Open a setlist
- **WHEN** the user opens stage mode on a setlist with items
- **THEN** the first item is shown together with its position, for example "1 / 9"

#### Scenario: Empty setlist
- **WHEN** the user tries to open stage mode on an empty setlist
- **THEN** stage mode does not open and the user is told the setlist is empty

### Requirement: Song information on stage
For a song item the system SHALL show its number, title, artist, key, tempo and duration using the values of the setlist, and SHALL show the change markers, the note and the link to the next song with its transition text when they exist.

#### Scenario: Song with changes and a note
- **WHEN** the current song changes instrument and has a note
- **THEN** the screen shows the instrument change marker and the note

#### Scenario: Linked song
- **WHEN** the current song is linked to the next one with the text "End on E"
- **THEN** the screen shows the link and "End on E"

#### Scenario: Missing duration
- **WHEN** the current song has no duration
- **THEN** the duration field shows that there is none

### Requirement: Stop information on stage
For a stop item the system SHALL show its type, its duration and its note.

#### Scenario: Break
- **WHEN** the current item is a break of 15 minutes with the note "Drinks and merch"
- **THEN** the screen shows the break, 15 minutes and the note

### Requirement: Move between items
The system SHALL move to the next and previous item with on-screen buttons and with a horizontal swipe. It MUST NOT move before the first or after the last item, and SHALL show the title of the next item as a preview.

#### Scenario: Next item
- **WHEN** the user taps next or swipes left on the third item
- **THEN** the fourth item is shown

#### Scenario: Limits
- **WHEN** the user tries to go back on the first item or forward on the last
- **THEN** the current item stays and the unavailable control is shown as disabled

### Requirement: Leave stage mode
The system SHALL let the user leave stage mode and return to the setlist.

#### Scenario: Exit
- **WHEN** the user leaves stage mode
- **THEN** the setlist view is shown
