# QA Checklist - MinLish Vocabulary App

Checklist này dùng để test thủ công trước buổi demo/chấm đồ án. Luôn chạy backend trước khi test Android.

## 1. Chuẩn Bị Môi Trường

- [ ] Docker đang chạy.
- [ ] PostgreSQL backend đã chạy bằng `docker compose up -d`.
- [ ] Backend đã migration bằng `alembic upgrade head`.
- [ ] Seed Anki đã import, có 30 decks Unit 01 đến Unit 30.
- [ ] FastAPI đang chạy ở `http://localhost:8000`.
- [ ] Android emulator dùng base URL `http://10.0.2.2:8000/api/v1/`.
- [ ] App build được bằng `./gradlew :app:assembleDebug`.
- [ ] Unit test pass bằng `./gradlew :app:testDebugUnitTest`.

## 2. Auth Flow

### Register

- [ ] Mở app lần đầu thấy Splash rồi Onboarding/Login/Register đúng flow.
- [ ] Vào Register.
- [ ] Để trống name/email/password rồi bấm Create Account: form hiển thị validation, không crash.
- [ ] Nhập email sai format: hiển thị lỗi.
- [ ] Nhập password dưới 6 ký tự: hiển thị lỗi.
- [ ] Bỏ checkbox Terms: hiển thị lỗi.
- [ ] Nhập thông tin hợp lệ và đăng ký thành công.
- [ ] Sau register app điều hướng vào Home.

### Login

- [ ] Logout hoặc reinstall app.
- [ ] Login với email/password sai: hiển thị lỗi rõ ràng.
- [ ] Login với tài khoản đúng: vào Home.
- [ ] Tắt backend rồi login: hiển thị `Không thể kết nối máy chủ`.
- [ ] Forgot Password/Google nếu chưa cấu hình không crash và báo fallback.

## 3. Session Và Restart

- [ ] Sau khi login, đóng app và mở lại.
- [ ] App vào Home nếu token còn hợp lệ.
- [ ] Nếu token hết hạn/không hợp lệ, app hiển thị lỗi phiên đăng nhập hoặc quay về Login theo trạng thái hiện có.

## 4. Home Dashboard

- [ ] Home load thành công khi backend chạy.
- [ ] Có daily plan, due reviews, streak, accuracy, total learned.
- [ ] Start Learning mở màn Flashcard.
- [ ] Refresh/Retry hoạt động khi backend lỗi rồi bật lại.
- [ ] Tắt backend: Home không trắng màn, có error state và nút Thử lại.

## 5. Deck Management

### Deck List

- [ ] Vào tab Decks.
- [ ] Thấy seed decks `Unit 01` đến `Unit 30`.
- [ ] Mỗi seed deck có word count khoảng 20.
- [ ] Search `Unit 01`: list lọc đúng.
- [ ] Search chuỗi không tồn tại: hiển thị empty/search-empty state.
- [ ] Tắt backend: có error state và Retry.

### Deck Detail

- [ ] Tap `Unit 01`.
- [ ] Thấy danh sách 20 words.
- [ ] Tìm thấy word `anxious`.
- [ ] Seed deck có read-only badge hoặc không hiện action thêm/sửa/xóa.
- [ ] Back hoạt động đúng.

### Create Deck

- [ ] Bấm Create Deck.
- [ ] Để trống name rồi save: hiển thị validation.
- [ ] Nhập name/description/tags rồi save.
- [ ] Sau save điều hướng tới Deck Detail hoặc Deck List theo app.
- [ ] Deck cá nhân xuất hiện trong tab Decks.

## 6. Word Management

- [ ] Mở deck cá nhân.
- [ ] Bấm Add Word.
- [ ] Để trống word/meaning: hiển thị validation.
- [ ] Thêm word hợp lệ.
- [ ] Word mới xuất hiện trong Deck Detail.
- [ ] Bấm edit word, sửa meaning/example/note rồi save.
- [ ] Reload Deck Detail vẫn thấy data đã sửa.
- [ ] Delete word thành công và list refresh.
- [ ] Vào seed deck không thể thêm/sửa/xóa word.

## 7. Flashcard Learning

- [ ] Bấm tab Learn hoặc Home Start Learning.
- [ ] Nếu có cards, thấy flashcard đầu tiên.
- [ ] Mặt trước hiển thị word/pronunciation.
- [ ] Bấm Show Answer hiển thị meaning/description/example.
- [ ] Bấm Again/Hard/Good/Easy: app submit review và chuyển card tiếp theo.
- [ ] Tắt backend trong lúc submit: app giữ card hiện tại và báo lỗi, không crash.
- [ ] Review hết session: điều hướng Review Results.
- [ ] Nếu không còn card: empty state đẹp, không trắng màn.

## 8. Review Results

- [ ] Sau khi học vài card, thấy màn Review Results.
- [ ] Total reviewed/correct/accuracy khớp session vừa học.
- [ ] Continue Learning quay lại Flashcard.
- [ ] Back Home quay về Home.
- [ ] Không có session data thì màn không crash.

## 9. Progress Analytics

- [ ] Vào tab Progress khi chưa học: thấy zero state đẹp.
- [ ] Học vài card rồi quay lại Progress.
- [ ] Learned words, streak, accuracy, activity, retention cập nhật.
- [ ] Tắt backend: Progress có error state và nút Thử lại.

## 10. Profile & Settings

- [ ] Vào tab Profile.
- [ ] Hiển thị đúng name/email từ backend.
- [ ] Sửa name/goal/level/daily new words rồi Save Profile.
- [ ] Rời màn/quay lại hoặc restart app: profile vẫn giữ data.
- [ ] Đổi reminder time định dạng `HH:mm`, push/email toggle rồi Save Reminders.
- [ ] Nhập giờ sai định dạng: hiển thị validation.
- [ ] Logout: app clear token và quay về Login.
- [ ] Sau logout, restart app không tự vào Home.

## 11. Navigation Và UI Polish

- [ ] Bottom nav active đúng tab.
- [ ] Chuyển tab nhiều lần không tạo back stack kỳ lạ.
- [ ] Back từ detail/form không thoát app bất ngờ.
- [ ] Text không tràn container trên Pixel emulator.
- [ ] Snackbar không che nút chính trong các flow quan trọng.
- [ ] Loading/error/empty state nhất quán.
- [ ] Không còn màn placeholder ở các tab chính.
- [ ] UI vẫn giữ phong cách Stitch: teal/mint, card 24dp, inputs 12dp, button pill.

## 12. Demo Script Gợi Ý

1. Register hoặc Login.
2. Home: giới thiệu daily plan/dashboard.
3. Decks: mở Unit 01, chỉ ra `anxious`.
4. Create Deck: tạo deck cá nhân.
5. Add Word: thêm, sửa, xóa một từ.
6. Learn: học 2-3 flashcards.
7. Review Results: xem summary.
8. Progress: xem số liệu cập nhật.
9. Profile: sửa reminder/profile.
10. Logout.

## 13. Known Limitations V1

- Google login cần Android OAuth client ID/config riêng.
- Forgot Password chưa có endpoint.
- Notification local cần quyền notification trên Android 13+ để hiện thật.
- Audio playback cho Anki media chưa phải trọng tâm v1 nếu chưa có UI player.
- Practice Quiz, CSV/XLSX import/export là should-have/nice-to-have nếu còn thời gian.
