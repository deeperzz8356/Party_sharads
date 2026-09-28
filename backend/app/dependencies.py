from functools import lru_cache

from .config import get_settings
from .firestore_repository import FirestoreRepository
from .repository import InMemoryRepository, Repository


@lru_cache
def get_repository() -> Repository:
    if get_settings().repository_backend.casefold() == "memory":
        return InMemoryRepository()
    return FirestoreRepository()

