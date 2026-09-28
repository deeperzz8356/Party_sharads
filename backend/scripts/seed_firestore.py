from firebase_admin import firestore

from app.auth import initialize_firebase
from app.seed_data import built_in_decks


def main() -> None:
    if not initialize_firebase():
        raise RuntimeError("Firebase configuration is required")
    db = firestore.client()
    for deck in built_in_decks():
        ref = db.collection("decks").document(str(deck.id))
        ref.set(deck.model_dump(mode="json", exclude={"cards"}))
        batch = db.batch()
        count = 0
        for card in deck.cards:
            batch.set(ref.collection("cards").document(str(card.id)), card.model_dump(mode="json"))
            count += 1
            if count == 400:
                batch.commit()
                batch = db.batch()
                count = 0
        if count:
            batch.commit()
        print(f"Seeded {deck.title}: {len(deck.cards)} cards")


if __name__ == "__main__":
    main()

