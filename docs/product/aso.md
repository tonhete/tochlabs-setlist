# Store listing and ASO (draft)

Draft listing for Google Play first and the App Store later. The texts describe the planned MVP and MUST be checked against what actually ships before publishing. Limits checked on 2026-10-29 against Google Play Console help (store listing best practice) and Apple's App Store product page guidance.

## Positioning

- **Who it is for:** musicians who play in more than one band and prepare setlists for gigs.
- **Main promise:** one place per band for songs, setlists with exact running time, stage view and gig day sheets.
- **Differentiators to show:** several bands in one app, per-setlist key, tempo, instrument and tuning, links between songs, total with and without stops, free, no ads, open source.
- **Category:** Music & Audio (Google Play); Music (App Store).
- **Languages:** English (default) and Spanish. Both listings are written, not machine-translated screenshots.

## Rules to respect

| Rule | Source of the limit |
|---|---|
| Google Play: title up to 30 characters, short description up to 80, full description up to 4,000 | Google Play Console help |
| Google Play: "Free" and "No Ads" must not appear in the title or developer name; no rankings, "best" or "#1" claims; no unattributed testimonials; avoid emojis, all caps and keyword lists; do not repeat the short description in the full description | Google Play Console help |
| Google Play: screenshots must show the real app; keep text in them minimal and provide a version per supported language | Google Play Console help |
| App Store: name up to 30 characters, subtitle 30, promotional text 170, keywords 100 in total, up to 10 screenshots | Apple product page guidance |
| The listing and its translations follow the same metadata policy | Google Play Console help |

There is no keyword field on Google Play; relevant words must appear naturally in the title, short description and full description.

## Google Play

| Field | English | Spanish |
|---|---|---|
| App name (30) | TochLabs Setlist | TochLabs Setlist |
| Short description (80) | Setlists for musicians in several bands: stage mode, PDF and gig day sheet (74) | Setlists para músicos con varias bandas: modo escenario, PDF y hoja de ruta (75) |

### Full description (English)

```
TochLabs Setlist helps you plan what you play and when you play it, for every band you are in.

Keep each band apart
Create your bands, filter everything by band, and keep one repertoire per band. When a band stops, archive it: its setlists and gigs go to the history and can be restored later.

Build setlists that tell you the truth
Add songs from your own catalogue, drag them into order and insert stops: speech, break, tuning change, encore. Set a pause between songs and see the total time with or without stops. If a song has no duration, the total says it is incomplete instead of guessing.

Adjust each song for each gig
Every song keeps its original key, tempo and tuning. In a setlist you can change the key or tempo, write the instrument and tuning you will use, add a note, and link a song to the next one with a transition such as "end on E". Changes of instrument or tuning are flagged so you are not caught out on stage.

Read it on stage
Stage mode shows one item at a time, large, with its key, tempo, notes and links. Swipe to move on.

Share it with the band
Export a stage PDF or a list PDF, print it, or send a .setlist file. Your bandmates open it in the app and the setlist and its songs are imported without duplicating songs they already have.

Plan the gig day
Add gigs and rehearsals to the calendar with a day sheet: arrival, backline, soundcheck, on stage, pack-up and departure, plus parking, contact and the linked setlist.

Free, no ads, open source
TochLabs Setlist has no ads and no accounts. Your data stays on your device. The source code is public under the GPL-3.0 licence.

Available in English and Spanish.
```

### Full description (Spanish)

```
TochLabs Setlist te ayuda a planear qué tocas y cuándo lo tocas, en cada una de tus bandas.

Cada banda por separado
Crea tus bandas, filtra todo por banda y mantén un repertorio por banda. Cuando una banda se para, archívala: sus setlists y bolos pasan al histórico y puedes restaurarlos cuando quieras.

Setlists que te dicen la verdad
Añade canciones de tu propio catálogo, arrástralas hasta ponerlas en orden e inserta paradas: discurso, descanso, afinación, bis. Fija una pausa entre canciones y consulta la duración total con o sin paradas. Si a una canción le falta la duración, el total avisa de que está incompleto en lugar de inventarse un dato.

Ajusta cada canción para cada bolo
Cada canción guarda su tono, tempo y afinación originales. En un setlist puedes cambiar el tono o el tempo, escribir el instrumento y la afinación que vas a usar, añadir una nota y enlazar una canción con la siguiente con una transición como "acabar en E". Los cambios de instrumento o afinación quedan señalados para que no te pillen por sorpresa en el escenario.

Léelo en el escenario
El modo escenario muestra un elemento cada vez, en grande, con su tono, tempo, notas y enlaces. Desliza para avanzar.

Compártelo con la banda
Exporta un PDF de escenario o de lista, imprímelo o envía un archivo .setlist. Tus compañeros lo abren en la app y se importan el setlist y sus canciones sin duplicar las que ya tengan.

Organiza el día del bolo
Añade bolos y ensayos al calendario con su hoja de ruta: llegada, backline, prueba de sonido, en el escenario, recoger y salida, además del aparcamiento, el contacto y el setlist vinculado.

Gratis, sin anuncios y de código abierto
TochLabs Setlist no tiene anuncios ni cuentas. Tus datos se quedan en tu dispositivo. El código fuente es público con licencia GPL-3.0.

Disponible en español e inglés.
```

## App Store (later)

| Field | English | Spanish |
|---|---|---|
| Name (30) | TochLabs Setlist | TochLabs Setlist |
| Subtitle (30) | Setlists for several bands (26) | Setlists para varias bandas (27) |
| Promotional text (170) | Build setlists, see the exact running time, and read them on stage. Free, no ads, open source. (94) | Crea setlists, conoce la duración exacta y léelos en el escenario. Gratis, sin anuncios y de código abierto. (108) |
| Keywords (100) | setlist,band,musician,gig,song,repertoire,bpm,key,tuning,pdf,calendar,rehearsal,concert,live,stage (98) | setlist,banda,músico,bolo,concierto,canciones,repertorio,bpm,tono,afinación,ensayo,calendario (93) |

The App Store description can reuse the Google Play full description, adjusted to Apple's guidance (concise opening paragraph followed by a short feature list).

## Screenshots plan
Real screens from the app only, per language, with minimal overlay text. Suggested order:
1. Setlist detail with total, pause and badges.
2. Stage mode.
3. Song linked to the next with a transition.
4. Band filter and several bands.
5. Export: PDF and `.setlist` file.
6. Calendar and event roadmap.

## Other assets to prepare
App icon, Google Play feature graphic, short promo video (optional), privacy policy page (the app collects no data; to be published as a simple page in the repository), and the data safety form.

## To validate before publishing
- Final feature set against the text above.
- Icon and graphic sizes from the current Google Play preview-assets help and App Store Connect screenshot specifications.
- Whether the Play Console account type requires a developer address and contact details (not decided yet).
