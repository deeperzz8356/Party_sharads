from abc import ABC, abstractmethod
from copy import deepcopy
from datetime import datetime
from threading import RLock
from uuid import UUID, uuid4

from fastapi import HTTPException, status

from .models import CustomDeck, CustomDeckWrite, Deck, DeckSummary, GameSession, GameSessionWrite, UserProfile, utc_now
from .seed_data import built_in_decks


class Repository(ABC):
    @abstractmethod
    def bootstrap(self, uid: str) -> UserProfile:
        raise NotImplementedError
    @abstractmethod
    def list_decks(self, since_version: int | None) -> tuple[int, list[DeckSummary]]:
        raise NotImplementedError
    @abstractmethod
    def get_deck(self, deck_id: UUID) -> Deck | None:
        raise NotImplementedError
    @abstractmethod
    def list_custom_decks(self, uid: str, include_deleted: bool) -> list[CustomDeck]:
        raise NotImplementedError
    @abstractmethod
    def create_custom_deck(self, uid: str, payload: CustomDeckWrite) -> CustomDeck:
        raise NotImplementedError
    @abstractmethod
    def update_custom_deck(self, uid: str, deck_id: UUID, payload: CustomDeckWrite) -> CustomDeck:
        raise NotImplementedError
    @abstractmethod
    def delete_custom_deck(self, uid: str, deck_id: UUID) -> CustomDeck:
        raise NotImplementedError
    @abstractmethod
    def upsert_game(self, uid: str, payload: GameSessionWrite) -> GameSession:
        raise NotImplementedError
    @abstractmethod
    def list_games(self, uid: str) -> list[GameSession]:
        raise NotImplementedError
    @abstractmethod
    def ready(self) -> bool:
        raise NotImplementedError


class InMemoryRepository(Repository):
    def __init__(self) -> None:
        self._lock = RLock()
        self._decks = {deck.id: deck for deck in built_in_decks()}
        self._custom: dict[UUID, CustomDeck] = {}
        self._games: dict[UUID, GameSession] = {}
        self._users: dict[str, UserProfile] = {}

    def bootstrap(self, uid: str) -> UserProfile:
        with self._lock:
            now = utc_now()
            existing = self._users.get(uid)
            profile = UserProfile(uid=uid, created_at=existing.created_at if existing else now, last_seen_at=now)
            self._users[uid] = profile
            return profile

    def list_decks(self, since_version: int | None) -> tuple[int, list[DeckSummary]]:
        current = max(deck.version for deck in self._decks.values())
        decks = [DeckSummary(**deck.model_dump(exclude={"cards"})) for deck in self._decks.values() if since_version is None or deck.version > since_version]
        return current, sorted(decks, key=lambda item: (item.featured_rank or 999, item.title))

    def get_deck(self, deck_id: UUID) -> Deck | None:
        deck = self._decks.get(deck_id)
        return deepcopy(deck) if deck else None

    def list_custom_decks(self, uid: str, include_deleted: bool) -> list[CustomDeck]:
        return [deepcopy(deck) for deck in self._custom.values() if deck.owner_id == uid and (include_deleted or not deck.deleted)]

    def create_custom_deck(self, uid: str, payload: CustomDeckWrite) -> CustomDeck:
        now = utc_now()
        deck_id = payload.id or uuid4()
        existing = self._custom.get(deck_id)
        if existing:
            if existing.owner_id != uid:
                raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Deck identifier already exists")
            return deepcopy(existing)
        deck = CustomDeck(id=deck_id, owner_id=uid, title=payload.title, description="Custom deck", version=1,
                          updated_at=now, card_count=len(payload.prompts), cards=payload.prompts)
        with self._lock:
            self._custom[deck.id] = deck
        return deepcopy(deck)

    def _owned(self, uid: str, deck_id: UUID) -> CustomDeck:
        deck = self._custom.get(deck_id)
        if not deck or deck.owner_id != uid:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Custom deck not found")
        return deck

    def update_custom_deck(self, uid: str, deck_id: UUID, payload: CustomDeckWrite) -> CustomDeck:
        with self._lock:
            existing = self._owned(uid, deck_id)
            if payload.expected_version != existing.version:
                raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Deck version conflict")
            updated = existing.model_copy(update={"title": payload.title, "cards": payload.prompts,
                "card_count": len(payload.prompts), "version": existing.version + 1, "updated_at": utc_now(), "deleted": False})
            self._custom[deck_id] = updated
            return deepcopy(updated)

    def delete_custom_deck(self, uid: str, deck_id: UUID) -> CustomDeck:
        with self._lock:
            existing = self._owned(uid, deck_id)
            deleted = existing.model_copy(update={"deleted": True, "version": existing.version + 1, "updated_at": utc_now()})
            self._custom[deck_id] = deleted
            return deepcopy(deleted)

    def upsert_game(self, uid: str, payload: GameSessionWrite) -> GameSession:
        existing = self._games.get(payload.id)
        if existing and existing.owner_id != uid:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Game not found")
        game = GameSession(owner_id=uid, **payload.model_dump())
        with self._lock:
            self._games[game.id] = game
        return deepcopy(game)

    def list_games(self, uid: str) -> list[GameSession]:
        games = [deepcopy(game) for game in self._games.values() if game.owner_id == uid]
        return sorted(games, key=lambda game: game.updated_at, reverse=True)

    def ready(self) -> bool:
        return bool(self._decks)
