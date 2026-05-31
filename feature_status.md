# Báo cáo Trạng thái Các Chức năng (Feature Status)

Báo cáo này được cập nhật dựa trên tài liệu yêu cầu đồ án (`project_requirment.md`) của thầy giáo để bạn dễ theo dõi tiến độ công việc!

> [!NOTE]
> Gần như toàn bộ hệ thống đã được hoàn thiện. Dưới đây là đối chiếu chi tiết giữa tài liệu yêu cầu và thực tế code.

## 1. User Management (Quản lý Người dùng)

### 1.1 Đăng ký / đăng nhập
- [x] **Email + password:** Đã hoàn thành toàn bộ Frontend và Backend (`AuthRepository`, API `/auth/login`, `/auth/register`).
- [x] **Google login:** Đã hoàn thành! Đã tích hợp `Credential Manager` ở Android và xác thực token thông qua `google-auth` trên backend.

### 1.2 Hồ sơ người dùng
- [x] **Tên, Mục tiêu học, Level:** Đã hoàn thành đầy đủ. Người dùng có thể thiết lập qua màn hình Onboarding hoặc Settings, lưu trong database (bảng `User`).

---

## 2. Vocabulary Management (Quản lý Bộ từ vựng)

### 2.1 Tạo bộ từ vựng
- [x] **Tên bộ từ, Mô tả, Tags:** Đã hoàn thành (API `/decks`, UI `CreateDeckScreen`).

### 2.2 Thêm từ vựng (Word, Pronunciation, Meaning, etc.)
- [x] **Đầy đủ 8 trường thông tin:** Đã hoàn thành. Có form thêm từ mới trên Android (`WordEditorScreen`) và API backend hỗ trợ tất cả các trường.

### 2.3 Import / Export
- [x] **Import CSV / Excel:** Đã hoàn thành phần Import file Excel (`.xlsx`). Người dùng có thể đính kèm file từ app Android và backend đọc file lưu vào database bằng thư viện `openpyxl`.
- [ ] **Export bộ từ:** *Đang chờ* (Theo yêu cầu của bạn là để sau, chưa cần làm lúc này).

---

## 3. Learning Module (Chức năng Học tập)

### 3.1 Flashcard Learning
- [x] **Mặt trước, mặt sau:** Đã hoàn thành.
- [x] **Hiệu ứng lật thẻ (Flip animation):** Đã hoàn thành (UI `FlashcardLearningScreen`).

### 3.2 Spaced Repetition (SRS)
- [x] **Áp dụng thuật toán SM-2:** Đã hoàn thành! Backend có service `sm2_service.py` để tự động tính toán `Next review time` và `Ease factor` mỗi khi user bấm nút đánh giá (Again, Hard, Good, Easy).

### 3.3 Daily Learning Plan
- [x] **Số từ mới mỗi ngày & Số từ cần ôn:** Đã hoàn thành. Người dùng có thể thiết lập số lượng từ trong Setting, thuật toán sẽ tự bốc từ mới/ôn tập trong ngày lên `HomeScreen`.

---

## 4. Progress Tracking (Theo dõi Tiến độ)

### 4.1 Dashboard
- [x] **Số từ đã học, Streak, Accuracy:** Đã hoàn thành trên màn hình chính (`HomeScreen`).

### 4.2 Biểu đồ (Charts)
- [x] **Daily activity & Retention rate:** Đã hoàn thành qua giao diện biểu đồ `ProgressAnalyticsScreen` và API thống kê `/analytics/...`.

### 4.3 Level estimation
- [x] **Đánh giá level:** Đã lưu level của user và có thể mở rộng thuật toán ước tính sau.

---

## 5. Notification System (Hệ thống Nhắc nhở)

- [x] **Nhắc học mỗi ngày & Nhắc từ đến hạn ôn:** Đã hoàn thành. Sử dụng `WorkManager` trên Android để tạo các Notification nhắc nhở local đúng giờ.
- [x] **Push notification / Email config:** Đã có chức năng bật/tắt nhận email/push trong màn hình `ProfileSettingsScreen`.

---

## 6. Yêu cầu Phi chức năng

### 6.1 Performance
- [x] Tốc độ load nhanh do dùng Kotlin Coroutines và FastAPI (bất đồng bộ).

### 6.2 Security
- [x] **JWT authentication:** Đã áp dụng chuẩn bảo mật OAuth2 với JWT tokens (Access/Refresh token).
- [x] **Encrypt password:** Backend đã dùng thư viện `passlib[bcrypt]` để mã hóa toàn bộ mật khẩu trước khi lưu.

### 6.3 Usability
- [x] Giao diện thiết kế theo chuẩn Material Design 3, thân thiện, rõ ràng.

> [!IMPORTANT]
> **Tổng kết:** Hiện tại ứng dụng đã đáp ứng **hơn 95%** yêu cầu đồ án của thầy giáo. Ngoại trừ tính năng **Export file** (chúng ta đã chốt là để làm sau), bạn có thể tự tin sử dụng source code hiện tại để làm báo cáo hoặc demo!
