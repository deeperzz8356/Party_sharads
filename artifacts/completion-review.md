# Party Mode completion review — 7 October 2026

The Android app now opens directly into its game gallery. Account forms, sign-in, onboarding, account settings, the separate dashboard, and the HTTP API client are removed. The gallery uses full square artwork with open captions and simple category tabs rather than a box-heavy dashboard.

## Artwork

17 separate covers were generated with the built-in image tool. The first two (Charades and Impostor Mime) were visually inspected and checked in the installed app before the final gallery review. Every game uses bundled raster artwork in the gallery, rules, and player setup. No vector game icons remain.

Prompt manifests: [group A](covers-a-prompts.md), [group B](covers-b-prompts.md).

- `cover_blank_agent.png` — 1254 × 1254
- `cover_bomb_defusal.png` — 1254 × 1254
- `cover_charades.png` — 1254 × 1254
- `cover_crowd_trap.png` — 1254 × 1254
- `cover_draw_and_guess.png` — 1254 × 1254
- `cover_fake_it.png` — 1254 × 1254
- `cover_impostor_mime.png` — 1254 × 1254
- `cover_inside_voice.png` — 1254 × 1254
- `cover_mime_telephone.png` — 1254 × 1254
- `cover_most_likely.png` — 1254 × 1254
- `cover_never_have_i_ever.png` — 1254 × 1254
- `cover_reverse_charades.png` — 1254 × 1254
- `cover_signal_line.png` — 1254 × 1254
- `cover_sound_effects.png` — 1254 × 1254
- `cover_truth_or_dare.png` — 1254 × 1254
- `cover_twin_word_spy.png` — 1254 × 1254
- `cover_would_you_rather.png` — 1254 × 1254

## Firebase

Existing project: `project-3d0049ce-fc46-4ebb-a8a`. Default Standard Firestore database: `asia-south1` (Mumbai). Owner-only rules deployed from `backend/firestore.mobile.rules` and independently reviewed. An invisible, persisted anonymous Firebase identity provides device ownership; there is no account UI. Offline first launch remains playable and retries identity registration quietly. Clearing app data/reinstalling produces a new identity.

Legacy local data is preserved via Room v1→v2 migration and adopted only from guest-owned rows. Earlier account-owned data is isolated. Pending changes are not overwritten by refresh, and stale sync acknowledgments cannot clear newer edits. Device identity activation preserves the active game and setup.

## Verification

- Final Android debug app and instrumentation build: passed.
- Android unit tests: 11 passed (9 engine, 2 tilt).
- Physical Android phone tests: 3 passed. Direct gallery → rules → pack → players → scoring → pause/resume; local data isolation and stale-write protection; live anonymous Firebase owner write/read, cross-device denial, unauthenticated denial, identity reuse and test cleanup.
- Actual app database inspected after tests: version2, 19 device-owned sessions, 0 pending session uploads.
- Backend tests: 8 passed, 2 skipped. Emulator-only tests were skipped because the first-time emulator download was too slow; live Android Firebase checks passed instead.
- All17 PNG resources validated as square images.
- Independent final source review: no material blockers.

The previous email-account live test failed on a server read while reusing one Firestore instance across identity changes. The final anonymous test isolates Firebase instances and passed. The previous startup test was corrected to wait safely for Compose initialization; the final direct-entry test passed.

## Deliverables

- [Installable debug APK](party-mode-debug.apk)
- [Gallery phone preview](party-cover-preview.png)
- [Physical-device test transcript](device-verification.txt)

This is a debug build. Existing user changes were preserved; no branch, commit, or store publication was created.
