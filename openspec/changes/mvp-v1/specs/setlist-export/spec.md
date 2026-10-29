# Spec Delta

## Purpose

Produces a paper or PDF version of a setlist for the stage or for the band, and sends it to the printer.

## ADDED Requirements

### Requirement: Stage PDF
The system SHALL export a setlist as a stage PDF: a large-type layout with the song number, title, key, change markers, notes and link texts, and the stops with their label and duration.

#### Scenario: Export stage PDF
- **WHEN** the user exports the stage PDF of a setlist with 9 items
- **THEN** a PDF is produced with those 9 items in order and their markers and notes

### Requirement: List PDF
The system SHALL export a setlist as a list PDF: a compact table with number, title, artist, key, tempo, duration and markers per item, followed by the number of songs and the total with stops.

#### Scenario: Export list PDF
- **WHEN** the user exports the list PDF
- **THEN** the PDF shows one row per item and, at the end, the song count and the total with stops

#### Scenario: Incomplete total
- **WHEN** some songs have no duration
- **THEN** the PDF states that the total is incomplete

### Requirement: Share and print
The system SHALL let the user share the generated PDF through the system share sheet and send it to the system print dialog.

#### Scenario: Print
- **WHEN** the user chooses print for a PDF layout
- **THEN** the system print dialog opens with that layout

#### Scenario: Share
- **WHEN** the user chooses share
- **THEN** the system share sheet opens with the PDF
