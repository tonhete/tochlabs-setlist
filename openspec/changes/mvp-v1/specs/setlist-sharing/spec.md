# Spec Delta

## Purpose

Lets a musician send a complete setlist to the rest of the band as a single file and lets the receiver import it into their own app.

## ADDED Requirements

### Requirement: Export a .setlist file
The system SHALL export a setlist as a `.setlist` file that contains the order of items, the stops, the links and transition texts, the notes, the per-setlist overrides, the pause between songs and the catalogue data of every song used. The file MUST be shareable through the system share sheet.

#### Scenario: Export a file
- **WHEN** the user chooses to share a setlist as a file
- **THEN** a `.setlist` file is created with a descriptive name and its summary shows the number of songs, stops and the band

### Requirement: Import a .setlist file
The system SHALL import a `.setlist` file into a new setlist of an active band chosen by the user, recreating every item, link, note and override. Importing MUST NOT modify any existing setlist.

#### Scenario: Import into a band
- **WHEN** the user opens a valid `.setlist` file and picks the band "Mareas"
- **THEN** a new setlist with the same content is created for "Mareas"

#### Scenario: Existing setlist untouched
- **WHEN** a setlist with the same name already exists
- **THEN** the import creates another setlist and leaves the existing one unchanged

### Requirement: No duplicate songs on import
When importing, the system SHALL reuse songs that already exist in the receiver's catalogue and SHALL create only the songs that are missing. A song SHALL be considered the same when its title and artist match, ignoring case and accents.

#### Scenario: Song already in the catalogue
- **WHEN** the file contains a song whose title and artist match a catalogue song
- **THEN** the existing song is used and no duplicate is created

#### Scenario: New song
- **WHEN** the file contains a song that is not in the catalogue
- **THEN** it is added to the catalogue and to the repertoire of the chosen band

### Requirement: Reject invalid files
The system SHALL reject a file that is not a valid `.setlist` file, and a file produced by a newer, incompatible version of the app, and MUST explain the reason without changing any data.

#### Scenario: Corrupt file
- **WHEN** the user opens a corrupt file with the `.setlist` extension
- **THEN** nothing is imported and the user is told the file cannot be read

#### Scenario: Newer format
- **WHEN** the file declares a format version the app does not support
- **THEN** nothing is imported and the user is told to update the app
