# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

Inferred from the explicit brief: groups of friends and families sharing one Android phone during in-person parties. The primary job is to choose a game quickly, understand its rules without prior knowledge, set up the people in the room, and start playing with minimal interruption.

## Product Purpose

An offline-first, pass-the-phone party-game hub combining acting, social deduction, conversation, trivia, drawing, and rapid-play modes. Success means a mixed group can find a suitable game and begin a round in a few minutes without needing separate apps.

## Positioning

One persistent local roster, one modular prompt engine, and one Party Mix can move the same group across 17 substantially different game loops.

## Operating Context

The app is used in noisy rooms, living rooms, trips, and parties. A phone is passed between players, held to the forehead, used as a private role screen, or placed where the group can see it. Players may be standing and should not need sustained reading.

## Capabilities and Constraints

- Native Kotlin and Jetpack Compose Android application.
- Offline-first decks and resumable sessions with synchronization through the existing Firebase-backed API.
- Supports 2–20 named players depending on the selected mode.
- Adults-only content remains hidden until explicitly unlocked.
- Settings, games, motion controls, camera capture, and drawing features must remain functional. Open directly into games with no account, login, or onboarding screen. Use an automatic per-install device identity for private Firestore backup.
- The current Firebase project must be preserved.

## Brand Commitments

The experience is playful, funky, optimistic, and energetic. The user explicitly requires a light theme, playful illustrated game covers organized by category with open captions rather than a box-heavy dashboard, game rules after choosing a game, and an easier player-first setup that does not force users through formal team creation.

## Evidence on Hand

- Working application source and tests in `android/` and `backend/`.
- Seventeen implemented game modes and bundled prompt collections.
- No external brand assets, logo package, testimonials, usage metrics, or licensed third-party game identities are available and none should be fabricated.

## Product Principles

1. Get a room playing faster than explaining the game aloud.
2. Make every game visually recognizable before its title is read.
3. Treat private reveals, voting, and handoffs as first-class physical interactions.
4. Prefer one shared player roster; derive teams only when a game genuinely requires them.
5. Keep the experience usable offline and inclusive in mixed-age groups.

## Accessibility & Inclusion

Large tap targets, high-contrast text, color-independent labels, readable type at arm's length, and instructions that do not rely on sound alone are required by the physical play context.

## Latest interaction direction — 7 October 2026

Open directly into the game gallery. No account forms, sign-in, sign-out, or onboarding. Each of the 17 games has a separate playful raster cover rather than a vector icon. Validate the first two covers in the app before the complete set. Keep the gallery airy: art, readable titles, concise premises, simple category tabs, and lightweight Settings, Make a deck, Resume, and Party Mix actions. Device backup uses a persisted anonymous Firebase identity behind the scenes; users never manage an account.

## Navigation and match history

Home, Games, My decks and Scores are the primary bottom destinations. Home offers resume and quick discovery; Games owns category browsing; My decks owns creation and saved prompts; Scores shows device-owned recorded match/team results and round details.

## Guided discovery and party setup

Help us choose asks player count, mood and time, then recommends one playable game. Browse games retains all seventeen games, with easy first picks and plain descriptions. Setup gathers people first, then offers Together or supported Two teams. Teams use optional default names, visible membership, shuffle and individual moves. Advanced options are collapsed; a short example precedes play. Saved-game resume identifies the game and round, and private reveals explicitly say who should hold the phone.
