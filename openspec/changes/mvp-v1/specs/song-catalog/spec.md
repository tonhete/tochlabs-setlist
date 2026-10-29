# Spec Delta

## Purpose

Keeps a personal catalogue of songs, entered by hand, with their original musical data, and records which bands play each song.

## ADDED Requirements

### Requirement: Create and edit songs
The system SHALL let the user create and edit a song with a required title and these optional fields: artist, original key, original tempo (BPM), original tuning and duration. A title MUST NOT be empty after trimming whitespace.

#### Scenario: Create a song with all data
- **WHEN** the user saves a song with title, artist, key, BPM, tuning and duration
- **THEN** the song is stored in the catalogue with those values

#### Scenario: Create a song with title only
- **WHEN** the user saves a song that has only a title
- **THEN** the song is stored and the missing fields are shown as empty

#### Scenario: Reject an empty title
- **WHEN** the user tries to save a song without a title
- **THEN** the system does not save it and indicates that the title is required

### Requirement: Optional duration
A song's duration SHALL be optional. A song without duration MUST be clearly labelled as having no duration wherever it is listed.

#### Scenario: Save without duration
- **WHEN** the user saves a song without a duration
- **THEN** the song is saved and the user is told that totals will be incomplete

### Requirement: Free-text tuning
Tuning, both on a song and on a setlist item, SHALL be free text of at most 20 characters. When a song has no tuning, the system SHALL treat it as standard tuning.

#### Scenario: Tuning too long
- **WHEN** the user types more than 20 characters in a tuning field
- **THEN** the system keeps only the first 20 characters

#### Scenario: No tuning set
- **WHEN** a song has no tuning
- **THEN** it is treated as standard tuning

### Requirement: Repertoire per band
The system SHALL let the user mark which bands play each song. A song MAY belong to several bands, and the song list SHALL be filterable by band.

#### Scenario: Song shared by two bands
- **WHEN** the user marks a song as played by "Mareas" and "Los Sábados"
- **THEN** the song appears in the repertoire of both bands

#### Scenario: Filter songs by band
- **WHEN** the user selects a band in the song list filter
- **THEN** only songs in that band's repertoire are listed

### Requirement: Search the own catalogue
When adding a song to a setlist, the system SHALL search the user's own catalogue by title and artist, ignoring case and accents. The MVP SHALL NOT search any external song database.

#### Scenario: Accent-insensitive search
- **WHEN** the user types "cancion" and the catalogue contains "Canción de cuna"
- **THEN** that song is among the results

#### Scenario: Song not found
- **WHEN** the search has no results
- **THEN** the system offers to create a new song using the typed text as its title

#### Scenario: Song already in the setlist
- **WHEN** a result is already in the current setlist
- **THEN** the result is marked as already present but can still be added

### Requirement: Originals are independent of setlists
A song's original key, tempo and tuning SHALL stay unchanged when a setlist overrides them. Editing a song's originals MUST NOT change overrides stored in setlists.

#### Scenario: Override does not touch the original
- **WHEN** a setlist item overrides the key of a song
- **THEN** the key stored in the catalogue remains the original

### Requirement: Delete a song only when unused
The system SHALL delete a song only if no setlist uses it, and MUST tell the user which setlists block the deletion otherwise.

#### Scenario: Delete an unused song
- **WHEN** the user deletes a song that appears in no setlist
- **THEN** the song is removed from the catalogue and from every repertoire

#### Scenario: Delete a song used in setlists
- **WHEN** the user deletes a song that appears in one or more setlists
- **THEN** the system does not delete it and lists the setlists that use it
