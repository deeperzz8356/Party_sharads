import logging
import sys
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from pythonjsonlogger.json import JsonFormatter

from .config import get_settings
from .dependencies import get_repository
from .routes import router


def configure_logging() -> None:
    handler = logging.StreamHandler(sys.stdout)
    handler.setFormatter(JsonFormatter("%(asctime)s %(levelname)s %(name)s %(message)s"))
    logging.basicConfig(level=logging.INFO, handlers=[handler], force=True)


@asynccontextmanager
async def lifespan(_: FastAPI):
    configure_logging()
    logging.getLogger(__name__).info("api_started", extra={"environment": get_settings().environment})
    yield


def create_app() -> FastAPI:
    settings = get_settings()
    application = FastAPI(title=settings.app_name, version="1.0.0", lifespan=lifespan)
    application.add_middleware(
        CORSMiddleware,
        allow_origins=settings.allowed_origins,
        allow_credentials=settings.allowed_origins != ["*"],
        allow_methods=["GET", "POST", "PUT", "DELETE"],
        allow_headers=["Authorization", "Content-Type"],
    )
    application.include_router(router)

    @application.exception_handler(Exception)
    async def unhandled_exception(request: Request, exc: Exception):
        logging.getLogger(__name__).exception("unhandled_request_error", extra={"path": request.url.path})
        return JSONResponse(status_code=500, content={"detail": "Internal server error"})

    @application.get("/health")
    def health():
        return {"status": "ok"}

    @application.get("/ready")
    def ready():
        if not get_repository().ready():
            return JSONResponse(status_code=503, content={"status": "not_ready"})
        return {"status": "ready"}

    return application


app = create_app()

