# Spec Delta

## Purpose

Organises the dates of a band's gigs and rehearsals and gives the musician a day sheet with the timed milestones, logistics and setlist of each event.

## ADDED Requirements

### Requirement: Create and edit events
The system SHALL let the user create an event for an active band with a type (gig or rehearsal), a date, a title, a venue and optional address, parking notes and contact. Title and date MUST be required.

#### Scenario: Create a gig
- **WHEN** the user saves a gig for "Mareas" with a date and the title "Concierto Sala Norte"
- **THEN** the event appears in the calendar on that date

#### Scenario: Missing date
- **WHEN** the user tries to save an event without a date
- **THEN** the event is not saved and the date is indicated as required

### Requirement: Calendar and agenda
The system SHALL show a monthly calendar that marks the days with events, and an agenda that lists the events of the selected day or, when no day is selected, of the whole month. The calendar SHALL respect the band filter.

#### Scenario: Select a day
- **WHEN** the user selects a day with two events
- **THEN** the agenda lists those two events

#### Scenario: Day without events
- **WHEN** the user selects a day with no events
- **THEN** the agenda states that there are no events on those dates

### Requirement: Roadmap of milestones
Each event SHALL have a roadmap: a list of timed milestones ordered by start time. A milestone SHALL have a label, a start time and an optional end time. The system SHALL offer these standard milestones for a gig: arrival, backline (range), soundcheck (range), on stage, gig (range), pack-up and departure. The user MUST be able to add, edit, remove and rename milestones.

#### Scenario: Range milestone
- **WHEN** the user adds the soundcheck from 20:00 to 21:00
- **THEN** the roadmap shows "20:00 – 21:00 soundcheck"

#### Scenario: Ordered by time
- **WHEN** milestones are added in any order
- **THEN** the roadmap lists them by start time

#### Scenario: Custom milestone
- **WHEN** the user adds a milestone with the label "Dinner"
- **THEN** it is listed in the roadmap with the other milestones

### Requirement: Logistics
The event sheet SHALL show the venue, address, parking notes and contact. The MVP SHALL NOT open the address in a maps app.

#### Scenario: Show logistics
- **WHEN** the user opens an event with parking notes and a contact
- **THEN** both are shown in the event sheet

### Requirement: Linked setlist
An event MAY be linked to one setlist of the same band. The event sheet SHALL show the linked setlist and its total, and open it on request. A setlist linked to an event SHALL be marked as having a roadmap in the setlist list.

#### Scenario: Link a setlist
- **WHEN** the user links the setlist "Concierto Sala Norte" to the event
- **THEN** the event sheet shows that setlist with its total with stops

#### Scenario: Setlist of another band
- **WHEN** the user opens the list of setlists to link
- **THEN** only setlists of the event's band are offered

### Requirement: Archived bands
Events of an archived band SHALL NOT be shown in the calendar and SHALL reappear when the band is restored.

#### Scenario: Archive hides events
- **WHEN** a band with events is archived
- **THEN** its events no longer appear in the calendar or the agenda
