# Spec Delta

## Purpose

Collects the few app-wide preferences a musician may want to change, and shows basic information about the app.

## ADDED Requirements

### Requirement: Language preference
The system SHALL let the user choose the app language between "follow the device", Spanish and English, and SHALL apply the choice immediately.

#### Scenario: Switch to English
- **WHEN** the user selects English in the settings
- **THEN** every screen is shown in English without restarting the app

### Requirement: Default pause between songs
The system SHALL let the user set the default pause between songs, from 0 to 120 seconds in steps of 5, used for new setlists. Changing it MUST NOT modify existing setlists.

#### Scenario: Change the default
- **WHEN** the user sets the default pause to 30 seconds and then creates a setlist
- **THEN** the new setlist starts with a 30 second pause

#### Scenario: Existing setlists keep their value
- **WHEN** the default pause changes
- **THEN** setlists created earlier keep their own pause

### Requirement: About
The settings SHALL show the app version, the licence (GPL-3.0) and a link to the public source code repository.

#### Scenario: Open the about section
- **WHEN** the user opens the about section
- **THEN** the version, the licence name and the repository link are shown
