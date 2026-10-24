# Party Mode design system

## Direction

The approved visual world is a **warm clay party shelf**: the seventeen illustrated clay covers lead discovery, surrounded by cream, rounded lettering and tactile native controls. The app should feel like the lively room in its artwork: welcoming, funny, colorful and easy to use. This replaces the earlier neon cue-card direction.

## Palette

- Cream canvas: `#FFF6E9`
- Warm white surface: `#FFFCF6`
- Apricot tonal surface: `#F5E6D5`
- Cocoa text: `#35251F`; supporting text: `#67514A`
- Terracotta primary action: `#A43C2D` with white text
- Clay coral: `#E7654F`
- Denim selection: `#365E9D` with white text
- Sunny yellow: `#F4C75D` with cocoa text
- Soft peach: `#F2AB7B`; sage: `#9AC7AA`
- Quiet outline: `#D9BFA9`

Use semantic Material color roles for page text, resting controls and selection. Yellow, peach and sage always use cocoa foregrounds. High contrast remains a separate black/white scheme; gallery, navigation and captions follow its semantic colors.

## Typography

Bundled Nunito provides medium, semibold, bold, extra-bold and black weights. The friendly rounded forms connect the interface to the clay characters. Use 30/34sp black page headings, 20/24sp extra-bold section headings, 17/24sp body instructions, 15/21sp supporting text and 11–13sp metadata. Gameplay prompts retain large arm's-length sizes. Native window typography also uses Nunito.

## Home, Games, My decks and Scores

Four bottom destinations use an authored24dp outline icon family (home, controller, cards, trophy), readable labels, selected semantics and56dp minimum targets. Settings is an accessible gear at the top right. Repeated Party mode mastheads and per-page hello greetings are removed; each toolbar names its current destination. Each page preserves its scroll position; system Back from secondary tabs returns Home.

Home pairs Good times start here with a three-cover tilted photo collage, a saved-party Resume card, Browse games, a sunny Surprise us strip and a quiet deck shortcut. The collage is one accessible browse action.

Games leads with category filters and all17 approved covers. My decks is a studio with warm yellow, tilted example prompt cards and Create a deck. Saved decks live below it; empty copy stays concise. Saving in the studio returns to the collection, while contextual pack creation retains Save and use.

Scores is a saved-match history, newest first. Show dates, game names, finished/in-progress state, per-team recorded points, winners/ties only for completed matches and expandable round details. One-group games show Group score. No invented scores or cross-game ranking. Empty history explains how to record a score. History is local-first and scoped to the existing device owner; no database migration or Firebase changes.

## Rules, packs and people

Preparatory screens expose a labeled Back action with accessible name Back. Rules use a landscape cover, a readable game title and compact premise/player summary, followed by three numbered instructions. The full-width card-pack action stays outside the scrolling instructions. Pack rows use warm-white surfaces, a restrained colored initial and readable neutral card counts.

Player setup first asks who's playing, then confirms the party. Together is the default; supported games offer Two teams with actual player lists, shuffle and individual move controls. Team names are optional. Names trim to thirty characters, duplicates receive inline feedback, and the party has a twenty-player maximum. Removal targets are at least 48dp and announce the player's name. Detailed game options stay collapsed initially. The full-width Start playing action validates the roster and both groups, followed by one concrete example before the first round.

Home leads with Help us choose: player count, mood and available time produce one suitable recommendation with Play this and Show another. Changing recommendations resets the scroll so the title remains visible. Games offers three easy first picks before category browsing; every cover includes a plain premise and an Easy to start or Needs explaining label. Resume names the saved game and round.

Avoid a dashboard of nested panels. Group options with simple section headings and breathing space. Standard surface and button corners are 22dp; tabs use 14dp and fields 14dp. No gradients, ambient shadows or decorative icon collections.

## Play and navigation

Preserve all seventeen game flows and their private handoffs. Role cards use uniform color so observers cannot infer secret identities. Reverse Charades reveals every next prompt to actors before handing to the guesser; hints remain hidden during guessing. The final ten seconds use a readable light pink urgency wash in the normal theme.

System Back during a round asks whether to leave. Cancel preserves the current game phase; confirmed departure pauses the runtime before returning to the gallery. Ordinary setup Back keeps native navigation behavior. Buttons state their action, dialogs protect deliberate decisions, and motion stays brief and functional.

## Assets and native shell

Use the seventeen `cover_*` drawable resources for game artwork; do not replace them with emoji, random stock icons or generated interface screenshots. The existing party mascot remains the launcher mark. App label is Party Mode. Launch background and system bars match the cream canvas, with dark native system-bar icons in the light theme. Font license is bundled at `assets/licenses/nunito-ofl.txt`.

## Verification

Parent task owns a bounded physical-device pass of gallery, rules, packs, setup, visible Back, and gameplay safety plus Android build/test validation. Source-only inspection is not visual verification.
