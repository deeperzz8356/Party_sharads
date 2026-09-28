---
name: Dumb Charades Party Hub
description: A sunlit game-show cue wall for fast, social, pass-the-phone play.
colors:
  ink: "#111118"
  ink-soft: "#252533"
  paper: "#FFFCF5"
  surface: "#FFFFFF"
  surface-soft: "#F2EEE5"
  text-muted: "#5B5963"
  outline: "#CBC5B9"
  cue-pink: "#FF3D8D"
  cue-cobalt: "#1F6FFF"
  cue-yellow: "#FFD629"
  cue-tangerine: "#FF7A2F"
  cue-mint: "#42D99A"
  cue-lilac: "#E7DFFF"
typography:
  display:
    fontFamily: "Roboto, system-ui, sans-serif"
    fontSize: "50sp"
    fontWeight: 900
    lineHeight: "48sp"
    letterSpacing: "-1.2sp"
  headline:
    fontFamily: "Roboto, system-ui, sans-serif"
    fontSize: "34sp"
    fontWeight: 900
    lineHeight: "35sp"
    letterSpacing: "-0.6sp"
  title:
    fontFamily: "Roboto, system-ui, sans-serif"
    fontSize: "20sp"
    fontWeight: 800
    lineHeight: "24sp"
  body:
    fontFamily: "Roboto, system-ui, sans-serif"
    fontSize: "17sp"
    fontWeight: 400
    lineHeight: "24sp"
  label:
    fontFamily: "Roboto, system-ui, sans-serif"
    fontSize: "12sp"
    fontWeight: 900
    letterSpacing: "0.8sp"
rounded:
  xs: "10dp"
  sm: "12dp"
  field: "14dp"
  md: "16dp"
  card: "18dp"
  canvas: "24dp"
  hero: "30dp"
  pill: "999dp"
spacing:
  xs: "5dp"
  sm: "8dp"
  md: "12dp"
  lg: "18dp"
  page: "20dp"
  xl: "22dp"
components:
  button-primary:
    backgroundColor: "{colors.cue-pink}"
    textColor: "{colors.surface}"
    typography: "{typography.label}"
    rounded: "{rounded.md}"
    height: "60dp"
    width: "100%"
  button-yellow-action:
    backgroundColor: "{colors.cue-yellow}"
    textColor: "{colors.ink}"
    typography: "{typography.label}"
    rounded: "{rounded.md}"
    height: "64dp"
    width: "100%"
  game-card:
    backgroundColor: "{colors.cue-cobalt}"
    textColor: "{colors.ink}"
    rounded: "{rounded.md}"
    padding: "15dp"
    width: "1:1"
  chip-selected:
    backgroundColor: "{colors.cue-cobalt}"
    textColor: "{colors.surface}"
    typography: "{typography.label}"
    rounded: "{rounded.pill}"
  chip-idle:
    backgroundColor: "{colors.surface-soft}"
    textColor: "{colors.ink}"
    typography: "{typography.label}"
    rounded: "{rounded.pill}"
  panel:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.ink}"
    rounded: "{rounded.md}"
    padding: "18dp"
  input:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.ink}"
    typography: "{typography.body}"
    rounded: "{rounded.field}"
    height: "56dp"
---

# Design System: Dumb Charades Party Hub

## Overview

**Creative North Star: "The Sunlit Game-Show Cue Wall"**

The interface feels like a wall of brightly printed cue cards waiting backstage: warm white light, square color fields, blunt black lettering, and simple hand-drawn marks. It is funky and energetic without becoming visually noisy. Every screen should remain legible at arm's length and understandable in a loud room where the phone is moving between people.

Material 3 provides the native Android behavior, while authored color, typography, geometry, and Canvas glyphs give the product its own voice. Interaction is direct and physical: cards compress when pressed, chosen controls fill with color, private reveals ask for deliberate taps, and the primary action stays obvious at the end of each setup or handoff.

**Key Characteristics:**

- Warm, sunlit stage instead of a sterile white app canvas.
- Cobalt, pink, yellow, mint, tangerine, and lilac cue cards with ink-first contrast.
- Bold, compact type that reads like game-show prompts rather than editorial copy.
- Square game cards, persistent category controls, and authored Canvas glyphs.
- Player-first setup with compact removable chips and a fixed, full-width Ready action.
- Native Material 3 behavior, large touch targets, and color-independent labels.

## Colors

The palette pairs a warm paper stage with saturated cue-card colors and near-black ink, keeping each game recognizable before its title is read.

### Primary

- **Cue Pink:** The main Material accent, primary action color, selection marker, urgent state, and the signature color for high-energy social games.
- **Show Ink:** The dominant text and dark-panel color; it makes the playful palette feel deliberate and keeps light cue cards readable.

### Secondary

- **Stage Cobalt:** Category selection, informational emphasis, and the principal blue game family.
- **Cue Yellow:** Optimistic hero actions, countdowns, progress, and energetic highlights that need dark ink rather than white text.

### Tertiary

- **Cue Tangerine:** Skip actions, creative categories, and warm secondary game families.
- **Cue Mint:** Success, correct answers, drawing, and sound-oriented games.
- **Agent Lilac:** A deliberately quieter cue color reserved for Blank Agent so the hidden-role game remains distinct.

### Neutral

- **Warm Paper:** The default page background and visible stage around every component.
- **Clean Surface:** Cards, panels, inputs, and other resting controls.
- **Soft Surface:** Idle chips, helper panels, and low-priority tonal separation.
- **Muted Copy:** Supporting instructions and metadata that must recede without becoming faint.
- **Stage Outline:** Borders and dividers on controls that need definition without heavy chrome.
- **Soft Ink:** A secondary dark tone for rare dense or supporting surfaces.

### Named Rules

**The Cue-Card Contrast Rule.** Use Show Ink on every light cue color; reserve white type for dark panels, selected cobalt controls, and pink surfaces whose contrast has been explicitly checked.

**The Color Has a Job Rule.** Color identifies game families, categories, state, or action. Do not scatter accent colors as decoration with no interaction or recognition purpose.

## Typography

**Display Font:** Roboto Black (with system sans-serif fallback)  
**Body Font:** Roboto Regular (with system sans-serif fallback)  
**Label Font:** Roboto Black (with system sans-serif fallback)

**Character:** The system uses native Android sans-serif type with extreme weight contrast: oversized, tightly set black headlines deliver the joke and the action, while open body text carries rules. Uppercase labels act like printed tabs on physical cue cards.

### Hierarchy

- **Display** (Black, 50sp, 48sp line height): Hero commands, player names, winners, and time-critical prompts; keep copy short enough to retain its compact silhouette.
- **Headline** (Black, 34sp, 35sp line height): Page titles and major state changes.
- **Section Headline** (ExtraBold, 26sp, 28sp line height): Rules, questions, and local game phases.
- **Title** (ExtraBold, 20sp, 24sp line height): Card titles and prominent row labels.
- **Body** (Regular, 17sp, 24sp line height): Rules and instructions intended to be read once, quickly.
- **Label** (Black, 11–15sp, up to 1sp tracking): Categories, player counts, settings groups, scores, and compact controls; usually uppercase.

### Named Rules

**The Shout, Then Explain Rule.** Lead with one short black-weight command or state, then follow with one plain-language sentence. Never make players parse a paragraph before they can act.

**The Arm's-Length Rule.** Gameplay prompts and handoff names use headline or display size; 11–13sp type is reserved for metadata, never required instructions.

## Layout

Screens use a single-column phone layout with 20dp horizontal page padding, 22dp vertical padding, and a 14dp vertical rhythm. The title stack remains at the top, content occupies the flexible center, and the primary action sits last at full width. Scroll only the content region when setup or rules exceed the viewport so the action remains predictable.

The game hub is a two-column grid with 12dp gutters. Every game tile holds a strict 1:1 aspect ratio, and an odd final tile leaves an empty grid cell rather than stretching. Category chips form one persistent equal-width row above the grid; changing category replaces the colored section header and card set without moving the controls.

Setup is player-first. Names appear as compact two-column removable chips, while game-dependent options are grouped below in bordered panels. Teams are derived only when the chosen game benefits from them. Maintain touch targets of at least 48dp and use full-width 56–64dp actions for irreversible or phase-changing steps.

Soft off-canvas circles wash the upper-right and lower-left page edges at low opacity. They belong to the stage background and must never compete with content.

## Elevation & Depth

The system is flat and tactile. It uses saturated fills, 1–2dp borders, tonal surfaces, and scale feedback instead of decorative drop shadows. White surfaces sit directly on Warm Paper; a border appears only when a boundary or grouping needs to remain clear. Game tiles compress to 96% while pressed, providing physical feedback without simulated height.

### Named Rules

**The Flat Stage Rule.** Do not add ambient card shadows. Create hierarchy through cue color, warm tonal layering, borders, and scale response.

## Shapes

The core silhouette is a generously rounded rectangle: 16dp for primary cards, panels, and buttons. Smaller numbered markers and category headers use 10–12dp corners; fields use 14dp; large gameplay canvases use 24dp; hero containers may reach 30dp. Pills and countdown disks are fully circular. Square cards stay mathematically square even though their corners are softened.

Borders are functional and confident: 2dp ink on home shortcuts, 2dp translucent accent on grouped panels, and 1dp neutral outline on quiet metrics. Authored glyphs use thick rounded strokes and simple circles, arcs, and lines; do not substitute unrelated stock icons simply for convenience.

## Components

### Buttons

- **Shape:** Tactile rounded rectangle (16dp) with a 56–64dp height and full-width treatment for phase-changing actions.
- **Primary:** Cue Pink with white text for standard progression; hero entry actions may use Cue Yellow with Show Ink.
- **Press / Focus:** Preserve native Material 3 state treatment; card-like actions also compress to 96% on press. Focus must remain visible independently of fill color.
- **Secondary:** Outlined buttons sit on the page surface for optional creation, import, resume, or recording actions. Text buttons are reserved for low-priority exits such as quitting a match.

### Chips

- **Style:** Fully rounded, black-weight 12sp labels. Idle chips use Soft Surface with Show Ink.
- **State:** Selected chips fill with Stage Cobalt and use white text, or with the active category color and its contrast-safe foreground. Selection is always stated by the label and fill, never color alone.
- **Persistence:** Game-hub category chips remain visible above the scrolling cue-card grid.

### Cards / Containers

- **Corner Style:** 16dp for the default family; maintain a strict square aspect ratio for game cards.
- **Background:** Saturated cue colors for choices and signatures; Clean Surface for content panels; Show Ink for Party Mix.
- **Shadow Strategy:** No ambient shadows; use borders, fill, and press scale.
- **Border:** Use 2dp only when the component needs separation from a similar-value neighbor.
- **Internal Padding:** 15dp for compact square cards and 18–22dp for panels and hero cards.

### Inputs / Fields

- **Style:** Material 3 outlined field on Clean Surface, gently rounded corners (14dp), single line for player names, and 56dp control height.
- **Focus:** Retain the Material 3 focus outline and primary accent. The adjacent Add action matches the field height.
- **Error / Disabled:** Disable Add when the name is blank; trim input, cap names at 30 characters, and reject duplicates without changing the compact flow.

### Navigation

Navigation is task-forward rather than chrome-forward: page title, optional eyebrow, then the screen's immediate choice. Category filters provide local navigation within the game hub; route-level movement uses prominent labeled actions. Avoid a persistent bottom bar because play is a linear, pass-the-phone sequence.

### Game Cue Card

Each game is a square colored card containing a 48dp authored Canvas glyph, a black-weight title, an explicit player-range label, and one short premise. It compresses on touch and routes to a dedicated rule page before setup. The visual structure stays fixed across all games; identity comes from color, glyph, and concise copy.

### Player Chip Setup

Added names form compact, removable two-column chips. Tapping the visible pink × removes a player. Options appear only when relevant to the selected game, teams are auto-derived, and the full-width Ready action remains the clear completion point.

### Rule Step

Rules use a cue-colored 42dp numbered square beside one 17sp instruction. Keep steps concrete and short. End with a soft helper panel explaining what the app handles and a full-width action into deck selection.

## Do's and Don'ts

### Do:

- **Do** use the warm paper stage and low-opacity corner washes to keep light screens optimistic rather than clinical.
- **Do** keep game cards square, use the stable glyph/title/player-count/premise anatomy, and preserve two-column scanning.
- **Do** show per-game rules before deck and roster setup.
- **Do** retain persistent category controls and pair every color family with a written category or game label.
- **Do** prioritize the shared player roster, revealing team controls only for games that benefit from teams.
- **Do** use authored Canvas marks with thick, rounded geometry for game identity.
- **Do** preserve Material 3 semantics, native focus/state behavior, and large pass-the-phone targets.

### Don't:

- **Don't** introduce gradients, glass effects, neon-on-black styling, or ambient card shadows.
- **Don't** turn the hub into a generic list, carousel, or icon-only grid; the visible square cue wall is the product's central browsing pattern.
- **Don't** replace game glyphs with a mixed collection of emoji or third-party icon styles.
- **Don't** force formal team creation before collecting the people in the room.
- **Don't** hide rules, player counts, selected categories, or the Ready action behind ambiguous gestures.
- **Don't** use tiny supporting type for gameplay instructions or rely on sound alone to signal a phase change.
