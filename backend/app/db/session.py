from collections.abc import Generator

from sqlalchemy import create_engine
from sqlalchemy.orm import Session, sessionmaker

from app.core.config import get_settings

settings = get_settings()

engine = create_engine(
    settings.database_url,
    pool_pre_ping=True,
)

SessionLocal = sessionmaker(
    bind=engine,
    autocommit=False, # SQLAlchemy không tự commit sau mỗi thao tác mà phải gọi commit() để commit
    autoflush=False, # SQLAlchemy không tự flush sau mỗi thao tác mà phải gọi flush() để flush
    expire_on_commit=False, # Nếu expire_on_commit=False, bạn có thể trả user ra response dễ hơn, không bị cần reload lại ngay.
)


def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
