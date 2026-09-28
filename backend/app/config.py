from functools import lru_cache

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    app_name: str = "Dumb Charades API"
    environment: str = "development"
    repository_backend: str = "firestore"
    firebase_project_id: str | None = None
    firebase_credentials_json: str | None = None
    allowed_origins: list[str] = Field(default_factory=lambda: ["*"])
    auth_disabled: bool = False


@lru_cache
def get_settings() -> Settings:
    return Settings()

