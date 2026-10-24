# Bottom navigation and rules update — 2026-10-11

- Category navigation moved from the top to a fixed bottom bar with selected tab semantics and56dp minimum targets.
- Larger Hello, let's play welcome; toolbar contains Party mode and Settings.
- Resume is a saved-match card directly beneath the greeting, showing the associated game cover/name.
- Make your own deck now sits with home actions; contextual pack creation follows the existing pack list.
- Rules use a landscape cover aligned to preserve faces, game summary, numbered instructions and a fixed card-pack action. Content scrolls for enlarged text.

Validation: Android app/test builds passed. Physical phone game-flow test passed at normal size. Enlarged-text confirmation passed at320dp logical width and130% font size. Earlier enlarged run exposed a test-only lazy-list lookup problem, corrected by scrolling the list to the requested game. Layout detector returned no findings; git diff check passed. Phone display settings restored and latest APK installed. Firebase behavior unchanged.

APK: artifacts/party-mode-bottom-nav-debug.apk
Screenshots: artifacts/bottom-nav/normal; artifacts/bottom-nav/confirm; artifacts/bottom-nav/home-final.png.
