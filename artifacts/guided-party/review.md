# Guided party review

Implemented guided game discovery, easy first picks, plain game premises, difficulty labels, people-first setup, Together/Two teams, optional team names, shuffle/move membership, collapsed advanced options, a concrete pre-game example, clearer private handoffs and game/round-specific resume.

Build and 19 local unit tests passed. The complete physical-device play/setup/history flow passed at normal density and at density 540 with 130% text. Chooser confirmation passed at normal density after correcting the test's expected title to Sound Effects Only. Display changes caused a test Activity restart during the first isolated large-text confirmation; the settled rerun is recorded separately.

Screenshots were inspected for player confirmation, team membership, recommendation and example. Large-text screens scroll while keeping the main action available. Test runs created marked Smoke Test player names and scored test matches on the device; existing data was preserved. Cloud configuration was unchanged. Density and text settings are restored after each pass.

Installable build: artifacts/party-mode-guided-party-debug.apk.
Settled large-text chooser confirmation: passed (1 test). Both chooser sizes now pass.
