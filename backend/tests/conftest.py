import os

os.environ["REPOSITORY_BACKEND"] = "memory"
os.environ["AUTH_DISABLED"] = "true"
os.environ["ENVIRONMENT"] = "test"

import pytest
from fastapi.testclient import TestClient

from app.config import get_settings
from app.dependencies import get_repository
from app.main import app


@pytest.fixture(autouse=True)
def fresh_repository():
    get_settings.cache_clear()
    get_repository.cache_clear()
    yield
    get_repository.cache_clear()


@pytest.fixture
def client():
    with TestClient(app) as test_client:
        yield test_client

