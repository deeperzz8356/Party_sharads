# Playful hub and scoreboard — 2026-10-11

Home uses a tilted collage of approved clay game covers, a tighter Good times start here headline and the established warm palette. Deck studio has yellow, example prompt postcards and clear creation/collection sections. Repeated Party mode mastheads and hello greetings were removed. Four labeled outline icons now identify Home, Games, My decks and Scores; Settings uses an accessible gear.

Scoreboard reads saved matches for the current device owner, newest first. It shows game/date, finished or in-progress state, team/player names, recorded points, completed winners/ties and expandable rounds. One-team games show Group score. The query considers the latest100 stored sessions and omits unfinished sessions with no recorded rounds. No new database version or Firebase changes were required.

Validation:
- Android application and instrumentation builds passed.
- All14 unit tests passed, including three score tests for recorded totals, penalties, ties, unfinished matches and one-group scores.
- Normal physical-phone UI/gameplay/saved-score test and storage owner-isolation test passed (2 tests).
- The first larger-text run stopped at a test trying to access an off-screen lazy-list Browse games button. The test now scrolls the container to that button.
- Final cleanup corrected singular point/round labels, added date year, made the artwork collage one accessible action and set system-bar icon contrast from the theme.
- Final build and unit tests passed; phone disconnected before final installation/larger-text confirmation. The earlier physical checks used the previous build of the same redesign. Final APK was packaged but is not yet installed on the phone.
- Display settings were restored in the earlier connected test's finally block. No user data was cleared. Instrumentation saved a one-point smoke-test match, clearly identified by Smoke Test player names, to verify scoreboard persistence.
- Batched normal screenshots were visually reviewed; layout scan and git diff check passed.

Deliverable: artifacts/party-mode-playful-scoreboard-debug.apk (debug-signed).
Preview screenshots: artifacts/playful-hub/normal/00-home.png,05-my-decks.png,07-saved-scores.png.
