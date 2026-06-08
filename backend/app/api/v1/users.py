from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import get_current_user
from app.db.session import get_db
from app.models.user import User
from app.schemas.user import UpdateUserRequest, UserResponse
from app.services.auth_service import AuthService

router = APIRouter(prefix="/users", tags=["users"])

# Hàm trả về User nhưng FastAPI sẽ tự convert thành UserResponse
@router.get("/me", response_model=UserResponse)
# Anootated: biến này có kiểu User, và nó được lấy từ dependency Depends(get_current_user)
# một thứ mà hàm/class cần dùng làm việc
# hàm get_me trả về user hiện tại. Nhưng nó cần biết user hiện tại là ai. nó không tự biết. nó cần một hàm khác giúp nó tìm ra user hiện tại.
def get_me(current_user: Annotated[User, Depends(get_current_user)]) -> User: # depends trước khi chạy hàm get_me thì phải chạy hàm get_current_user
    return current_user

@rouer.patch("/me", response_model=UserResponse)
def update_me(
    request: UpdateUserRequest,
    current_user: Annotated[User, Depends(get_current_user)],
    db: Annotated[Session, Depends(get_db)],
) -> User:
    return AuthService(db).update_user(current_user, request)

