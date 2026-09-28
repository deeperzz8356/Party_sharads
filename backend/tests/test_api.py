from uuid import uuid4


def test_app_starts_and_is_ready(client):
    assert client.get("/health").json() == {"status": "ok"}
    assert client.get("/ready").status_code == 200


def test_builtin_decks_include_full_and_mode_specific_sets(client):
    response = client.get("/v1/decks")
    assert response.status_code == 200
    summaries = response.json()["decks"]
    assert len(summaries) == 22
    assert {"Truth or Dare", "Most Likely", "Never Have I Ever", "Fake It Trivia", "Draw & Guess", "Would You Rather", "Twin Words"}.issubset({deck["title"] for deck in summaries})
    restricted = [deck for deck in summaries if deck["age_restricted"]]
    assert len(restricted) == 2
    deck = client.get(f"/v1/decks/{summaries[0]['id']}").json()
    assert len(deck["cards"]) == 200
    assert client.get("/v1/decks?sinceVersion=1").json()["decks"] == []


def test_custom_deck_lifecycle_and_conflict(client):
    deck_id = str(uuid4())
    payload = {"id": deck_id, "title": "Family Night", "prompts": [{"text": "A sleepy penguin"}, {"text": "Robot chef"}]}
    created_response = client.post("/v1/custom-decks", json=payload)
    assert created_response.status_code == 201
    created = created_response.json()
    assert created["version"] == 1
    assert created["id"] == deck_id
    assert client.post("/v1/custom-decks", json=payload).json()["id"] == deck_id

    conflict = client.put(f"/v1/custom-decks/{created['id']}", json={**payload, "expected_version": 9})
    assert conflict.status_code == 409

    updated = client.put(f"/v1/custom-decks/{created['id']}", json={**payload, "title": "Family Fun", "expected_version": 1})
    assert updated.status_code == 200
    assert updated.json()["version"] == 2

    deleted = client.delete(f"/v1/custom-decks/{created['id']}")
    assert deleted.json() == {"id": created["id"], "deleted": True, "version": 3}
    assert client.get("/v1/custom-decks").json() == []
    assert len(client.get("/v1/custom-decks?include_deleted=true").json()) == 1


def test_duplicate_custom_prompts_are_rejected(client):
    response = client.post("/v1/custom-decks", json={"title": "Duplicates", "prompts": [{"text": "Dance"}, {"text": "dance"}]})
    assert response.status_code == 422


def test_game_validation_and_round_trip(client):
    deck_id = client.get("/v1/decks").json()["decks"][0]["id"]
    team_id = str(uuid4())
    payload = {
        "mode": "CLASSIC_TOUCH", "timer_seconds": 60, "total_rounds": 3, "current_round": 0,
        "selected_deck_ids": [deck_id], "teams": [{"id": team_id, "name": "Mimes", "players": ["Asha"]}]
    }
    saved = client.post("/v1/game-sessions", json=payload)
    assert saved.status_code == 200
    assert saved.json()["owner_id"] == "local-test-user"
    assert len(client.get("/v1/game-sessions").json()) == 1

    payload["timer_seconds"] = 45
    assert client.post("/v1/game-sessions", json=payload).status_code == 422
