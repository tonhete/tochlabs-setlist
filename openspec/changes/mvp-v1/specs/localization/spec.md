# Spec Delta

## Purpose

Makes the whole app usable in Spanish and English from the first release, with dates, times and numbers shown the way each language expects.

## ADDED Requirements

### Requirement: Spanish and English interface
Every user-visible text of the app SHALL be available in Spanish and in English. The app SHALL start in the device language when it is Spanish or English and in English otherwise, unless the user chose a language in the settings.

#### Scenario: Device in Spanish
- **WHEN** the device language is Spanish and the user has not chosen a language
- **THEN** the app is shown in Spanish

#### Scenario: Unsupported device language
- **WHEN** the device language is neither Spanish nor English
- **THEN** the app is shown in English

### Requirement: Localized formats
Dates, times and the names of months and weekdays SHALL follow the selected language. Durations SHALL be shown in hours and minutes or in minutes and seconds, with the unit labels translated.

#### Scenario: Date in Spanish
- **WHEN** the app language is Spanish and a setlist is dated 2026-11-14
- **THEN** the date is shown with the Spanish month name

### Requirement: Built-in labels follow the language
Labels that the app provides, such as stop types, standard milestones, standard tuning and change markers, SHALL be shown in the selected language. Text typed by the user MUST NOT be translated or altered.

#### Scenario: Stop type label
- **WHEN** the language changes from Spanish to English
- **THEN** a stop of type "Descanso" is shown as "Break"
- **AND** the song titles and notes typed by the user are unchanged

### Requirement: Language-independent files
A `.setlist` file SHALL NOT depend on the language of the app that created it, and exports SHALL use the language selected when they are generated.

#### Scenario: Exchange between languages
- **WHEN** a setlist exported from an app in Spanish is imported into an app in English
- **THEN** built-in labels such as stop types are shown in English
