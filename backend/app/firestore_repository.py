from datetime import datetime
from uuid import UUID, uuid4

from fastapi import HTTPException, status
from firebase_admin import firestore
from google.cloud.firestore_v1 import FieldFilter

from .auth import initialize_firebase
from .models import CustomDeck, CustomDeckWrite, Deck, DeckSummary, GameSession, GameSessionWrite, UserProfile, utc_now
from .repository import Repository


def _json(model) -> dict:
    return model.model_dump(mode="json")


class FirestoreRepository(Repository):
    def __init__(self) -> None:
        self._db = None

    @property
    def db(self):
        if self._db is None:
            if not initialize_firebase():
                raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Firestore is not configured")
            self._db = firestore.client()
        return self._db

    def bootstrap(self, uid: str) -> UserProfile:
        ref = self.db.collection("users").document(uid)
        snapshot = ref.get()
        now = utc_now()
        created = snapshot.to_dict().get("created_at", now.isoformat()) if snapshot.exists else now.isoformat()
        profile = UserProfile(uid=uid, created_at=created, last_seen_at=now)
        ref.set(_json(profile), merge=True)
        return profile

    def list_decks(self, since_version: int | None) -> tuple[int, list[DeckSummary]]:
        query = self.db.collection("decks")
        if since_version is not None:
            query = query.where(filter=FieldFilter("version", ">", since_version))
        decks = [DeckSummary.model_validate(doc.to_dict()) for doc in query.stream()]
        all_versions = [doc.to_dict().get("version", 1) for doc in self.db.collection("decks").select(["version"]).stream()]
        return max(all_versions, default=0), sorted(decks, key=lambda item: (item.featured_rank or 999, item.title))

    def get_deck(self, deck_id: UUID) -> Deck | None:
        ref = self.db.collection("decks").document(str(deck_id))
        snapshot = ref.get()
        if not snapshot.exists:
            return None
        cards = [card.to_dict() for card in ref.collection("cards").stream()]
        return Deck.model_validate({**snapshot.to_dict(), "cards": cards})

    def list_custom_decks(self, uid: str, include_deleted: bool) -> list[CustomDeck]:
        query = self.db.collection("custom_decks").where(filter=FieldFilter("owner_id", "==", uid))
        decks = [CustomDeck.model_validate(doc.to_dict()) for doc in query.stream()]
        return [deck for deck in decks if include_deleted or not deck.deleted]

    def create_custom_deck(self, uid: str, payload: CustomDeckWrite) -> CustomDeck:
        deck_id = payload.id or uuid4()
        ref = self.db.collection("custom_decks").document(str(deck_id))
        snapshot = ref.get()
        if snapshot.exists:
            existing = CustomDeck.model_validate(snapshot.to_dict())
            if existing.owner_id != uid:
                raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Deck identifier already exists")
            return existing
        deck = CustomDeck(id=deck_id, owner_id=uid, title=payload.title, description="Custom deck", version=1,
                          updated_at=utc_now(), card_count=len(payload.prompts), cards=payload.prompts)
        ref.create(_json(deck))
        return deck

    def _owned(self, uid: str, deck_id: UUID) -> tuple[object, CustomDeck]:
        ref = self.db.collection("custom_decks").document(str(deck_id))
        snapshot = ref.get()
        if not snapshot.exists:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Custom deck not found")
        deck = CustomDeck.model_validate(snapshot.to_dict())
        if deck.owner_id != uid:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Custom deck not found")
        return ref, deck

    def update_custom_deck(self, uid: str, deck_id: UUID, payload: CustomDeckWrite) -> CustomDeck:
        ref, existing = self._owned(uid, deck_id)
        if payload.expected_version != existing.version:
            raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Deck version conflict")
        updated = existing.model_copy(update={"title": payload.title, "cards": payload.prompts,
            "card_count": len(payload.prompts), "version": existing.version + 1, "updated_at": utc_now(), "deleted": False})
        ref.set(_json(updated))
        return updated

    def delete_custom_deck(self, uid: str, deck_id: UUID) -> CustomDeck:
        ref, existing = self._owned(uid, deck_id)
        deleted = existing.model_copy(update={"deleted": True, "version": existing.version + 1, "updated_at": utc_now()})
        ref.set(_json(deleted))
        return deleted

    def upsert_game(self, uid: str, payload: GameSessionWrite) -> GameSession:
        ref = self.db.collection("game_sessions").document(str(payload.id))
        snapshot = ref.get()
        if snapshot.exists and snapshot.to_dict().get("owner_id") != uid:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Game not found")
        game = GameSession(owner_id=uid, **payload.model_dump())
        ref.set(_json(game))
        return game

    def list_games(self, uid: str) -> list[GameSession]:
        query = self.db.collection("game_sessions").where(filter=FieldFilter("owner_id", "==", uid))
        games = [GameSession.model_validate(doc.to_dict()) for doc in query.stream()]
        return sorted(games, key=lambda game: game.updated_at, reverse=True)

    def ready(self) -> bool:
        try:
            next(self.db.collection("decks").limit(1).stream(), None)
            return True
        except Exception:
            return False
