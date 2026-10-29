# Spec Delta

## Purpose

Lets a musician who plays in several bands keep each band's repertoire, setlists and events separate, and retire a band without losing its data by mistake.

## ADDED Requirements

### Requirement: Create and edit bands
The system SHALL let the user create a band with a required name and a colour, and edit both later. A band name MUST NOT be empty after trimming whitespace.

#### Scenario: Create a band
- **WHEN** the user saves a new band with the name "Mareas" and a colour
- **THEN** the band appears in the band list and in every band filter

#### Scenario: Reject an empty name
- **WHEN** the user tries to save a band whose name is empty or only whitespace
- **THEN** the system does not save it and indicates that the name is required

### Requirement: Filter by band
The system SHALL offer a band filter on the setlists, calendar and song screens, with an option to show all bands. Only active (non-archived) bands SHALL appear in the filter.

#### Scenario: Filter setlists by band
- **WHEN** the user selects the band "Mareas" in the setlists filter
- **THEN** only setlists of "Mareas" are listed

#### Scenario: Show all bands
- **WHEN** the user selects the "all" option
- **THEN** items of every active band are listed

### Requirement: Archive a band
The system SHALL archive a band instead of deleting it. Archiving MUST move the band together with its setlists and events to the history, and these items MUST disappear from the active lists and filters.

#### Scenario: Archive a band with setlists
- **WHEN** the user archives a band that has setlists and events
- **THEN** the band, its setlists and its events are no longer shown in active screens
- **AND** the band is listed in the history

#### Scenario: Songs are not archived
- **WHEN** a band is archived
- **THEN** its songs remain in the song catalogue

### Requirement: Restore a band
The system SHALL restore an archived band from the history, bringing back its setlists and events exactly as they were.

#### Scenario: Restore from history
- **WHEN** the user restores an archived band
- **THEN** the band, its setlists and its events are shown again in active screens

### Requirement: Permanent deletion only from the history
The system SHALL allow permanent deletion of a band only from the history, never directly from the active band list. Before deleting, the system MUST show how many setlists, events and repertoire entries will be removed and MUST require explicit confirmation. Deletion MUST NOT remove songs from the catalogue.

#### Scenario: Confirm before deleting
- **WHEN** the user chooses to delete an archived band
- **THEN** the system shows the number of setlists, events and repertoire entries that will be deleted and asks for confirmation

#### Scenario: Cancel deletion
- **WHEN** the user cancels the confirmation
- **THEN** nothing is deleted and the band stays in the history

#### Scenario: Confirmed deletion
- **WHEN** the user confirms the deletion
- **THEN** the band, its setlists, its events and its repertoire entries are removed
- **AND** the songs remain in the catalogue

#### Scenario: No direct deletion
- **WHEN** the user looks at the active band list
- **THEN** the only removal action offered is archiving
