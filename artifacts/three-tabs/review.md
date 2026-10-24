# Home, Games and My decks — 2026-10-11

Implemented three primary bottom tabs in the approved warm clay theme.

Home owns the big welcome, conditional saved-party Resume card, Browse games, Surprise us and a small My decks shortcut. Games owns all17 raster covers and the three category filters. My decks owns custom-deck creation, an empty state and saved-deck cards with explicit Play charades actions. Settings stays top right. Tabs and independent scroll positions persist while moving to/from rules and the deck editor; system Back from secondary tabs returns Home.

Library creation uses Save deck and returns to My decks. Contextual pack creation retains Save and use. Firebase/device persistence is unchanged.

Validation: app and instrumentation builds passed; physical phone navigation/gameplay checks passed at normal size and320dp width with130% font. Checks included all tabs, creation editor/back, category filters, rules/back, packs/setup, scoring, leave confirmation, Home return, Resume opening the saved match and Quit returning Home. Layout scan returned no findings; git diff check passed. Saved-library insertion uses the existing persistence callback; no test deck was added to the user's data. Phone display settings restored and new APK installed.

APK: artifacts/party-mode-three-tabs-debug.apk
Screenshots: artifacts/three-tabs/normal and artifacts/three-tabs/large-text.
