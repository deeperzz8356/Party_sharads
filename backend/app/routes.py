from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, Query, status

from .auth import AuthenticatedUser, require_user
from .dependencies import get_repository
from .models import CustomDeck, CustomDeckWrite, Deck, DeckListResponse, DeleteResponse, GameSession, GameSessionWrite, UserProfile
from .repository import Repository

router = APIRouter(prefix="/v1")


@router.post("/auth/bootstrap", response_model=UserProfile)
def bootstrap(user: AuthenticatedUser = Depends(require_user), repo: Repository = Depends(get_repository)):
    return repo.bootstrap(user.uid)


@router.get("/decks", response_model=DeckListResponse)
def list_decks(since_version: int | None = Query(default=None, ge=0, alias="sinceVersion"),
               user: AuthenticatedUser = Depends(require_user), repo: Repository = Depends(get_repository)):
    version, decks = repo.list_decks(since_version)
    return DeckListResponse(server_version=version, decks=decks)


@router.get("/decks/{deck_id}", response_model=Deck)
def get_deck(deck_id: UUID, user: AuthenticatedUser = Depends(require_user), repo: Repository = Depends(get_repository)):
    deck = repo.get_deck(deck_id)
    if deck is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Deck not found")
    return deck


@router.get("/custom-decks", response_model=list[CustomDeck])
def list_custom_decks(include_deleted: bool = False, user: AuthenticatedUser = Depends(require_user),
                      repo: Repository = Depends(get_repository)):
    return repo.list_custom_decks(user.uid, include_deleted)


@router.post("/custom-decks", response_model=CustomDeck, status_code=status.HTTP_201_CREATED)
def create_custom_deck(payload: CustomDeckWrite, user: AuthenticatedUser = Depends(require_user),
                       repo: Repository = Depends(get_repository)):
    return repo.create_custom_deck(user.uid, payload)


@router.put("/custom-decks/{deck_id}", response_model=CustomDeck)
def update_custom_deck(deck_id: UUID, payload: CustomDeckWrite, user: AuthenticatedUser = Depends(require_user),
                       repo: Repository = Depends(get_repository)):
    return repo.update_custom_deck(user.uid, deck_id, payload)


@router.delete("/custom-decks/{deck_id}", response_model=DeleteResponse)
def delete_custom_deck(deck_id: UUID, user: AuthenticatedUser = Depends(require_user),
                       repo: Repository = Depends(get_repository)):
    deck = repo.delete_custom_deck(user.uid, deck_id)
    return DeleteResponse(id=deck.id, deleted=deck.deleted, version=deck.version)


@router.post("/game-sessions", response_model=GameSession)
def save_game(payload: GameSessionWrite, user: AuthenticatedUser = Depends(require_user),
              repo: Repository = Depends(get_repository)):
    return repo.upsert_game(user.uid, payload)


@router.get("/game-sessions", response_model=list[GameSession])
def list_games(user: AuthenticatedUser = Depends(require_user), repo: Repository = Depends(get_repository)):
    return repo.list_games(user.uid)
