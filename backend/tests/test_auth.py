from app.auth import AuthenticatedUser, require_user
from app.config import get_settings
from app.main import app


def test_protected_endpoint_rejects_missing_token_when_auth_enabled(client, monkeypatch):
    monkeypatch.setenv("AUTH_DISABLED", "false")
    get_settings.cache_clear()
    response = client.get("/v1/custom-decks")
    assert response.status_code == 401
    monkeypatch.setenv("AUTH_DISABLED", "true")
    get_settings.cache_clear()


def test_custom_deck_ownership_is_isolated(client):
    app.dependency_overrides[require_user] = lambda: AuthenticatedUser(uid="owner-one")
    created = client.post("/v1/custom-decks", json={"title": "Private", "prompts": [{"text": "Secret prompt"}]}).json()
    app.dependency_overrides[require_user] = lambda: AuthenticatedUser(uid="owner-two")
    assert client.get("/v1/custom-decks").json() == []
    assert client.delete(f"/v1/custom-decks/{created['id']}").status_code == 404
    app.dependency_overrides.clear()


def test_configured_auth_reports_unavailable_without_firebase_project(client, monkeypatch):
    monkeypatch.setenv("AUTH_DISABLED", "false")
    monkeypatch.delenv("FIREBASE_PROJECT_ID", raising=False)
    get_settings.cache_clear()
    response = client.get("/v1/decks", headers={"Authorization": "Bearer invalid"})
    assert response.status_code == 503
    monkeypatch.setenv("AUTH_DISABLED", "true")
    get_settings.cache_clear()

