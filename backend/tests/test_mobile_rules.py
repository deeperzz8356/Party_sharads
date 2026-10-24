"""Run via Firebase Auth + Firestore emulators; no production test accounts."""
import os
from uuid import uuid4

import httpx
import pytest


pytestmark = pytest.mark.skipif(
    not os.getenv("FIRESTORE_EMULATOR_HOST") or not os.getenv("FIREBASE_AUTH_EMULATOR_HOST"),
    reason="Firebase Auth and Firestore emulators are required",
)


def test_native_backup_rules_owner_boundaries_and_validation():
    project = os.getenv("GCLOUD_PROJECT", "demo-party-mode")
    auth_host = os.environ["FIREBASE_AUTH_EMULATOR_HOST"]
    store_host = os.environ["FIRESTORE_EMULATOR_HOST"]
    base = f"http://{store_host}/v1/projects/{project}/databases/(default)/documents"
    with httpx.Client(timeout=20) as client:
        identities = []
        for _ in range(2):
            response = client.post(
                f"http://{auth_host}/identitytoolkit.googleapis.com/v1/accounts:signUp?key=emulator",
                json={"returnSecureToken": True},
            )
            response.raise_for_status()
            identities.append(response.json())
        owner, stranger = identities
        owner_headers = {"Authorization": f"Bearer {owner['idToken']}"}
        stranger_headers = {"Authorization": f"Bearer {stranger['idToken']}"}

        for kind, limit in (("custom_decks", 200_000), ("game_sessions", 500_000)):
            identifier = str(uuid4())
            path = f"players/{owner['localId']}/{kind}/{identifier}"
            name = f"projects/{project}/databases/(default)/documents/{path}"

            def write(headers, *, payload="{}", extra=False, document_id=None, timestamp=False):
                fields = {"id": {"stringValue": document_id or identifier}, "payload": {"stringValue": payload}}
                if extra:
                    fields["role"] = {"stringValue": "admin"}
                if timestamp:
                    fields["updatedAt"] = {"timestampValue": "2000-01-01T00:00:00Z"}
                update = {"update": {"name": name, "fields": fields}}
                if not timestamp:
                    update["updateTransforms"] = [{"fieldPath": "updatedAt", "setToServerValue": "REQUEST_TIME"}]
                return client.post(f"{base}:commit", headers=headers, json={"writes": [update]})

            assert write(stranger_headers).status_code == 403
            assert write({}).status_code == 403
            assert write(owner_headers, extra=True).status_code == 403
            assert write(owner_headers, payload="x" * (limit + 1)).status_code == 403
            assert write(owner_headers, timestamp=True).status_code == 403
            assert write(owner_headers).status_code == 200
            assert client.get(f"{base}/{path}", headers=owner_headers).status_code == 200
            assert client.get(f"{base}/{path}", headers=stranger_headers).status_code == 403
            assert client.get(f"{base}/{path}").status_code == 403
            assert client.get(f"{base}/players/{owner['localId']}/{kind}", headers=owner_headers).status_code == 200
            assert client.get(f"{base}/players/{owner['localId']}/{kind}", headers=stranger_headers).status_code == 403
            assert write(owner_headers, payload='{"changed":true}').status_code == 200
            assert write(owner_headers, extra=True).status_code == 403
            assert write(owner_headers, document_id=str(uuid4())).status_code == 403
            assert write(owner_headers, payload="x" * (limit + 1)).status_code == 403
