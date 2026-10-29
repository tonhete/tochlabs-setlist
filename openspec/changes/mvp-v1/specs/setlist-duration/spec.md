# Spec Delta

## Purpose

Tells the musician how long a setlist will last, with or without stops and pauses, and says clearly when the figure is incomplete because some songs have no duration.

## ADDED Requirements

### Requirement: Music-only total
The system SHALL compute the music-only total as the sum of the durations of all songs that have a duration. Stops and pauses between songs MUST NOT be included.

#### Scenario: Songs only
- **WHEN** a setlist has songs of 3:00, 4:00 and 5:00 and a 10 minute break
- **THEN** the music-only total is 12:00

#### Scenario: Empty setlist
- **WHEN** a setlist has no items
- **THEN** the total is zero

### Requirement: Total with stops
The system SHALL compute the total with stops as the music-only total, plus the duration of every stop, plus one pause for every pair of consecutive items, whether songs or stops, that is not a linked pair of songs. Stops are treated as items of the list for this purpose.

#### Scenario: Songs, stop and pauses
- **WHEN** a setlist has songs of 3:00, 4:00 and 5:00, a 1:00 speech between the second and third song, a pause of 20 seconds and no links
- **THEN** the setlist has 3 pairs of consecutive items and the total with stops is 12:00 + 1:00 + 3 × 0:20 = 14:00

#### Scenario: Pause on both sides of a stop
- **WHEN** a song of 3:00, a speech of 1:00 and a song of 4:00 follow each other with a pause of 20 seconds
- **THEN** one pause is counted between the first song and the speech and another between the speech and the second song, and the total is 8:40

#### Scenario: Single item
- **WHEN** a setlist has one item
- **THEN** no pause is added

### Requirement: Linked songs have no pause
No pause SHALL be added between two consecutive songs when the first one is linked to the second. A link that has no effect, because the next item is a stop or there is no next item, MUST NOT change the total.

#### Scenario: Linked pair
- **WHEN** song 1 is linked to song 2 and they are consecutive
- **THEN** no pause is added between them

#### Scenario: Link ignored
- **WHEN** a song is marked as linked but a stop follows it
- **THEN** the pause between that song and the stop is counted as if it were not linked

### Requirement: Configurable pause between songs
Each setlist SHALL have its own pause between songs, in seconds, adjustable from 0 to 120 in steps of 5. New setlists SHALL start with the default pause defined in the app settings, which is 20 seconds until the user changes it.

#### Scenario: Change the pause
- **WHEN** the user increases the pause of a setlist from 20 to 25 seconds
- **THEN** the total with stops is recalculated with 25 seconds per counted pause

#### Scenario: Limits
- **WHEN** the pause is 0 seconds and the user tries to decrease it, or is 120 and the user tries to increase it
- **THEN** the value does not change

### Requirement: Songs without duration
A song without duration SHALL contribute zero to every total. When at least one song of the setlist has no duration, the system MUST show that the total is incomplete and how many songs have no duration, and MUST NOT present the total as exact.

#### Scenario: One song without duration
- **WHEN** a setlist has two songs with duration and one without
- **THEN** the total counts only the two and states "incomplete: 1 song without duration"

#### Scenario: Every duration known
- **WHEN** all songs have a duration
- **THEN** no incomplete warning is shown

### Requirement: Toggle between totals and breakdown
The setlist view SHALL let the user switch between the total with stops and the music-only total, and SHALL show a breakdown of the figure: songs, stops, number of pauses and their length, and number of links without pause. The total SHALL be shown in hours and minutes, and the exact value in minutes and seconds.

#### Scenario: Switch to music only
- **WHEN** the user selects the music-only total
- **THEN** the figure shown excludes stops and pauses and the breakdown states how much time was left out

### Requirement: Overrides do not change durations
Overriding the key or tempo of a setlist item SHALL NOT change the duration used in the totals.

#### Scenario: Faster tempo override
- **WHEN** the user raises the BPM of a song in a setlist
- **THEN** the song's duration in the total stays the same
