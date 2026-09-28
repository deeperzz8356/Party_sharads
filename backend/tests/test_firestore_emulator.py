import os
from uuid import uuid4

import pytest


pytestmark = pytest.mark.skipif(
    not os.getenv("FIRESTORE_EMULATOR_HOST"),
    reason="Firebase Firestore emulator is not running",
)


def test_firestore_custom_deck_round_trip(monkeypatch):
    monkeypatch.setenv("FIREBASE_PROJECT_ID", os.getenv("GCLOUD_PROJECT", "dumb-charades-test"))
    from app.config import get_settings
    from app.firestore_repository import FirestoreRepository
    from app.models import CustomDeckWrite, Prompt

    get_settings.cache_clear()
    repository = FirestoreRepository()
    uid = f"emulator-{uuid4()}"
    deck = repository.create_custom_deck(uid, CustomDeckWrite(title="Emulator Deck", prompts=[Prompt(text="Robot dance")]))
    assert repository.list_custom_decks(uid, include_deleted=False)[0].id == deck.id
    deleted = repository.delete_custom_deck(uid, deck.id)
    assert deleted.deleted is True

