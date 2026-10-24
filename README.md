# Party Mode

Offline-first party game hub with a native Kotlin/Jetpack Compose Android client and Firebase Firestore device backups.

## Game modes

- Dumb Charades with touch or tilt controls
- Impostor Mime with private role reveals and group voting
- Blank Agent with one no-word player, randomized clues, voting, and a final steal attempt
- Inside Voice with a cooperative yes/no search and hidden helper/saboteur roles
- Crowd Trap with private majority answers and a hidden social manipulator
- Signal Line with spectrum guessing and a hidden Jammer
- Truth or Dare with random-player selection and family or adults-only decks
- Most Likely with private voting and percentage reveals
- Never Have I Ever with five-heart survival
- Fake It trivia with player-written decoys and bluff scoring
- Draw & Guess with colors, brush sizing, erasing, and a 60-second timer
- Would You Rather with private A/B voting
- Twin Word Spy with related-word deduction
- Reverse Charades for one guesser and a room full of actors
- Mime Telephone with a text → video → drawing → guess reveal chain
- Sound Effects Only with dedicated audio-friendly prompts
- 5-Second Bomb with a randomized 30–90 second fuse
- Party Mix playlists that rotate modes automatically

The shared prompt engine automatically selects purpose-built offline decks for Sound, Bomb, Impostor, and Telephone rounds. Match progress and scores remain resumable. Open the app directly into the games gallery: no account, login, or onboarding is required. Custom decks and match sessions back up directly to private Firebase Firestore collections associated with this installation.

## Optional legacy API

The Android app uses Firestore directly; this separate FastAPI service is retained for legacy API clients and is not needed to run the app.

```powershell
cd backend
python -m venv .venv
.venv\Scripts\Activate.ps1
pip install -e ".[dev]"
$env:REPOSITORY_BACKEND="memory"
uvicorn app.main:app --reload
pytest
```

For production, set `REPOSITORY_BACKEND=firestore`, `FIREBASE_PROJECT_ID=project-3d0049ce-fc46-4ebb-a8a`, and either `GOOGLE_APPLICATION_CREDENTIALS` or `FIREBASE_CREDENTIALS_JSON`. Deploy using the included Dockerfile or `render.yaml`. The API accepts verified Firebase ID tokens from Email/Password and Google users.

## Android

Open `android` in Android Studio (JDK 17, Android SDK 35). Firebase configuration is provided by `app/google-services.json`. The native app uses Firestore directly and silently establishes an anonymous Firebase identity to enforce private device ownership. There is no sign-in screen or account management, and no separate API server is required.

Play immediately, including on a first launch without internet. A cached device identity is reused offline; a new installation establishes its identity quietly when a connection becomes available. Pending changes sync automatically. Existing local guest data is adopted by this device identity without interrupting a running game. Data belonging to earlier signed-in accounts is preserved under its original owner and is not imported into device mode. Clearing app data or reinstalling creates a new device identity, so backups do not transfer automatically to another installation.

The existing Firebase project is `project-3d0049ce-fc46-4ebb-a8a`; its default Standard Firestore database uses `asia-south1` (Mumbai). Firestore access rules live in `backend/firestore.mobile.rules`; `backend/firebase.json` selects these rules for deployment.

The Firebase Android config is not a service-account credential and is safe to bundle with the app. Service-account credentials and signing keys must never be committed.

# Party_sharads
