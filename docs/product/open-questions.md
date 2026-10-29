# Open questions and assumptions (MVP spec review)

Points where the specification fills a gap or follows the prototype and should be confirmed. Delete an entry when it is settled and reflect the decision in the specs.

## Assumptions made in the specs

| # | Topic | What the spec says | Source / reason |
|---|---|---|---|
| 2 | Settings content | Language (device, Spanish, English), default pause between songs, and an About section | Settings screen is in the MVP but has no prototype |
| 3 | Delete a song | Refused while any setlist uses it, listing those setlists | Not in the agreed notes; chosen as the safe option |
| 4 | Remove item, delete setlist | Removing an item keeps the song; deleting a setlist asks for confirmation and leaves a linked event without a setlist | Not in the agreed notes; needed for a usable editor |
| 5 | Event and setlist link | An event links to one setlist of the same band; a setlist is linked from at most one event | Prototype shows a 1 to 1 link |
| 6 | Setlist needs a band | A setlist belongs to one active band; with no bands the user is asked to create one first | Implied by the band filter |
| 7 | Band names | Not required to be unique | Not stated |
| 8 | Song identity on import | Same title and artist, ignoring case and accents | Not stated; simplest reliable rule |
| 9 | No accounts, no cloud | Data stays on the device; sharing is by file | Implied by "free, no ads" and the `.setlist` file |
| 10 | Standard milestones | A new gig offers the seven standard milestones to add; they are not forced | Spec wording says "offer" |
| 11 | Event types | Gig and rehearsal only | Prototype |
| 12 | Language of the documents | Specs and product documents in English (public repository); chat and review in Spanish | Consistent with the English README |
| 13 | Entity names | English names in the data model (Band, Song, Repertoire, Setlist, SetlistItem, Event, EventMilestone) | Code convention; Spanish terms kept in parentheses |

## Decided in review
- **Pause and stops:** a stop is an item like a song. One pause is counted between every pair of consecutive items, stops included; a link between two consecutive songs removes the pause of that pair. (The prototype counted one pause across a stop; the prototype must be updated.)
- **Add to the phone's calendar:** out of the MVP.
- Assumptions 2 to 13 above were accepted as written.

## Prototype items left out of the MVP
- **Add to the phone's calendar** (button on the event screen): out of the MVP; remove it from the prototype or mark it as future.
- **Open the venue in a maps app:** already agreed as a future feature.

## Not decided yet
- Keep the screen awake while stage mode is open.
- Valid range for BPM and for song durations.
- Whether stage mode offers a larger-text option.
- Developer account and contact data shown on the store listing.
