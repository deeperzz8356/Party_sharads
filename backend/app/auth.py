import json
from dataclasses import dataclass

import firebase_admin
from fastapi import Header, HTTPException, status
from firebase_admin import auth, credentials

from .config import get_settings


@dataclass(frozen=True)
class AuthenticatedUser:
    uid: str


def initialize_firebase() -> bool:
    if firebase_admin._apps:
        return True
    settings = get_settings()
    try:
        if settings.firebase_credentials_json:
            credential = credentials.Certificate(json.loads(settings.firebase_credentials_json))
            firebase_admin.initialize_app(credential, {"projectId": settings.firebase_project_id})
        elif settings.firebase_project_id:
            firebase_admin.initialize_app(options={"projectId": settings.firebase_project_id})
        else:
            return False
        return True
    except (ValueError, json.JSONDecodeError):
        return False


async def require_user(authorization: str | None = Header(default=None)) -> AuthenticatedUser:
    settings = get_settings()
    if settings.auth_disabled and settings.environment != "production":
        return AuthenticatedUser(uid="local-test-user")
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing bearer token")
    if not initialize_firebase():
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Authentication unavailable")
    try:
        decoded = auth.verify_id_token(authorization.removeprefix("Bearer ").strip(), check_revoked=True)
    except Exception as exc:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid bearer token") from exc
    uid = decoded.get("uid")
    if not isinstance(uid, str) or not uid:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid bearer token")
    return AuthenticatedUser(uid=uid)
