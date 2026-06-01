from io import BytesIO

import openpyxl
from fastapi import UploadFile, status
from sqlalchemy.orm import Session

from app.core.exceptions import ApiError
from app.models.deck import Deck
from app.models.user import User
from app.models.vocabulary_item import VocabularyItem


class ImportExportService:
    def __init__(self, db: Session) -> None:
        self.db = db

    def import_excel(self, user: User, deck: Deck, file: UploadFile) -> int:
        if not file.filename or not file.filename.endswith(".xlsx"):
            raise ApiError(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Chỉ hỗ trợ file định dạng .xlsx (Excel).",
                code="INVALID_FILE_FORMAT",
            )
        
        try:
            content = file.file.read()
            workbook = openpyxl.load_workbook(BytesIO(content), data_only=True)
            sheet = workbook.active
            
            imported_count = 0
            
            # Giả định cấu trúc cột:
            # A: Word, B: Pronunciation, C: Meaning, D: Description, E: Example, F: Collocation, G: Related, H: Note
            # Bắt đầu đọc từ dòng 2 (bỏ qua header)
            for row in sheet.iter_rows(min_row=2, values_only=True):
                # row is a tuple of values
                if not row or not row[0]:  # Cột Word không được trống
                    continue
                    
                word = str(row[0]).strip()
                if not word:
                    continue
                    
                pronunciation = str(row[1]).strip() if len(row) > 1 and row[1] else None
                meaning = str(row[2]).strip() if len(row) > 2 and row[2] else ""
                description = str(row[3]).strip() if len(row) > 3 and row[3] else None
                example = str(row[4]).strip() if len(row) > 4 and row[4] else None
                collocation = str(row[5]).strip() if len(row) > 5 and row[5] else None
                related_words = str(row[6]).strip() if len(row) > 6 and row[6] else None
                note = str(row[7]).strip() if len(row) > 7 and row[7] else None
                
                vocab_item = VocabularyItem(
                    deck_id=deck.id,
                    word=word,
                    pronunciation=pronunciation,
                    meaning=meaning,
                    description=description,
                    example=example,
                    collocation=collocation,
                    related_words=related_words,
                    note=note,
                )
                self.db.add(vocab_item)
                imported_count += 1
                
            self.db.commit()
            return imported_count
            
        except Exception as e:
            self.db.rollback()
            raise ApiError(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Lỗi khi xử lý file Excel: {str(e)}",
                code="EXCEL_PROCESSING_ERROR",
            )
