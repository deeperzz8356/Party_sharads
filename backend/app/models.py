from datetime import UTC, datetime
from enum import StrEnum
from uuid import UUID, uuid4

from pydantic import BaseModel, Field, field_validator, model_validator


def utc_now() -> datetime:
    return datetime.now(UTC)


class GameMode(StrEnum):
    FOREHEAD_TILT = "FOREHEAD_TILT"
    CLASSIC_TOUCH = "CLASSIC_TOUCH"


class GameType(StrEnum):
    CHARADES = "CHARADES"
    IMPOSTOR_MIME = "IMPOSTOR_MIME"
    BLANK_AGENT = "BLANK_AGENT"
    INSIDE_VOICE = "INSIDE_VOICE"
    CROWD_TRAP = "CROWD_TRAP"
    SIGNAL_LINE = "SIGNAL_LINE"
    TRUTH_OR_DARE = "TRUTH_OR_DARE"
    MOST_LIKELY = "MOST_LIKELY"
    NEVER_HAVE_I_EVER = "NEVER_HAVE_I_EVER"
    FAKE_IT = "FAKE_IT"
    DRAW_AND_GUESS = "DRAW_AND_GUESS"
    WOULD_YOU_RATHER = "WOULD_YOU_RATHER"
    TWIN_WORD_SPY = "TWIN_WORD_SPY"
    REVERSE_CHARADES = "REVERSE_CHARADES"
    MIME_TELEPHONE = "MIME_TELEPHONE"
    SOUND_EFFECTS = "SOUND_EFFECTS"
    BOMB_DEFUSAL = "BOMB_DEFUSAL"


class PromptStatus(StrEnum):
    GUESSED = "GUESSED"
    SKIPPED = "SKIPPED"
    PENALTY = "PENALTY"


class Difficulty(StrEnum):
    EASY = "EASY"
    MEDIUM = "MEDIUM"
    HARD = "HARD"


class Prompt(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    text: str = Field(min_length=1, max_length=120)
    difficulty: Difficulty = Difficulty.MEDIUM
    hint: str | None = Field(default=None, max_length=180)

    @field_validator("text", "hint")
    @classmethod
    def normalize_text(cls, value: str | None) -> str | None:
        return value.strip() if value is not None else None


class DeckSummary(BaseModel):
    id: UUID
    title: str
    description: str
    version: int = Field(ge=1)
    updated_at: datetime
    card_count: int = Field(ge=0)
    is_premium: bool = False
    age_restricted: bool = False
    featured_rank: int | None = None


class Deck(DeckSummary):
    cards: list[Prompt]


class CustomDeckWrite(BaseModel):
    id: UUID | None = None
    title: str = Field(min_length=1, max_length=60)
    prompts: list[Prompt] = Field(min_length=1, max_length=500)
    expected_version: int | None = Field(default=None, ge=1)

    @field_validator("title")
    @classmethod
    def normalize_title(cls, value: str) -> str:
        return " ".join(value.split())

    @model_validator(mode="after")
    def unique_prompts(self) -> "CustomDeckWrite":
        normalized = [item.text.casefold() for item in self.prompts]
        if len(normalized) != len(set(normalized)):
            raise ValueError("Prompt text must be unique within a deck")
        return self


class CustomDeck(Deck):
    owner_id: str
    deleted: bool = False


class PromptResult(BaseModel):
    prompt_id: UUID
    text: str = Field(min_length=1, max_length=120)
    status: PromptStatus
    elapsed_seconds: float = Field(ge=0, le=180)


class Team(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    name: str = Field(min_length=1, max_length=40)
    players: list[str] = Field(min_length=1, max_length=20)
    score: int = 0


class RoundResult(BaseModel):
    index: int = Field(ge=1)
    active_team_id: UUID
    actor_name: str = Field(min_length=1, max_length=60)
    score: int
    prompts: list[PromptResult]
    game_type: GameType = GameType.CHARADES


class GameSessionWrite(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    mode: GameMode
    game_type: GameType = GameType.CHARADES
    playlist: list[GameType] = Field(default_factory=list, max_length=20)
    timer_seconds: int
    total_rounds: int | None
    current_round: int = Field(ge=0)
    selected_deck_ids: list[UUID] = Field(min_length=1)
    teams: list[Team] = Field(min_length=1, max_length=8)
    rounds: list[RoundResult] = Field(default_factory=list)
    completed: bool = False
    updated_at: datetime = Field(default_factory=utc_now)

    @field_validator("timer_seconds")
    @classmethod
    def supported_timer(cls, value: int) -> int:
        if value not in {30, 60, 90, 120}:
            raise ValueError("timer_seconds must be 30, 60, 90, or 120")
        return value

    @field_validator("total_rounds")
    @classmethod
    def supported_rounds(cls, value: int | None) -> int | None:
        if value not in {None, 3, 5, 10}:
            raise ValueError("total_rounds must be 3, 5, 10, or null")
        return value


class GameSession(GameSessionWrite):
    owner_id: str


class UserProfile(BaseModel):
    uid: str
    created_at: datetime
    last_seen_at: datetime


class DeckListResponse(BaseModel):
    server_version: int
    decks: list[DeckSummary]


class DeleteResponse(BaseModel):
    id: UUID
    deleted: bool
    version: int
