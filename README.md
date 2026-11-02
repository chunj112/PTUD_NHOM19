> **Cập nhật 30/10/2026:** Đã nối Controller cho các UI Manager, Receptionist và Dentist hiện có. Giữ Login DEMO, CRUD và quy tắc chạy bằng dữ liệu bộ nhớ, xóa khi logout; chưa kết nối SQL Server hoặc xác thực tài khoản thật. Nurse chưa có FXML trong branch này.
>
> Bàn giao lớp/phương thức, luồng, kiểm thử và cách chạy: [docs/CONTROLLER_IMPLEMENTATION_HANDOFF.md](docs/CONTROLLER_IMPLEMENTATION_HANDOFF.md). Audit: [docs/CONTROLLER_FLOW_AUDIT.md](docs/CONTROLLER_FLOW_AUDIT.md). Các phần Starter/Sprint 0 bên dưới là lịch sử khởi tạo, không phải số lượng class hiện tại.

# DentaFlow — Starter sạch từ đầu | Nhóm 19 IUH

Đây là bộ khởi tạo **mới, tối giản**, phù hợp khi bạn đã xóa toàn bộ `src` cũ. Không phải bản hoàn thiện 14 Use Case.

## A. Khi project hiện tại vẫn còn pom.xml, database/, docs/, .git
1. Sao lưu project và kiểm tra đúng đường dẫn chỉ có ASCII, ví dụ `D:\PTUD\DentaFlow`.
2. Giải nén gói này **ra thư mục tạm**, không giải nén đè tất cả file đang có.
3. Copy `src/` mới vào project hiện tại; sao chép `AGENTS.md`, `PROMPT_START_CODEX.md`, `docs/` tùy nhu cầu.
4. **So sánh** `pom.xml` hiện tại với file trong ZIP, chỉ sửa những gì cần: Java 21, JavaFX, `mainClass=vn.edu.nhom19.nhakhoa.app.Launcher`. Không xóa file pom cũ mà không xem dependencies.
5. **Không ghi đè `database/` nếu đã có database/schema riêng**.
6. Eclipse: `Project > Refresh (F5)` → `Maven > Update Project...` → Run As > Maven build... Goals `clean verify` → cấu hình khác Goals `javafx:run`.
7. Console `BUILD SUCCESS` và ứng dụng mở khung **DentaFlow** là đủ nghiệm thu bootstrap. Lúc này menu là preview, chưa có Controller/Backend.

## B. Nếu bắt đầu cả project từ folder mới
1. Giải nén toàn bộ thư mục `DentaFlow_Rebuild_From_Zero` vào `D:\PTUD\DentaFlow` (tránh đường dẫn tiếng Việt gây lỗi trong Eclipse).
2. Eclipse → File → Import → Maven → Existing Maven Projects → chọn folder chứa `pom.xml` → Finish.
3. Project > Maven > Update Project.
4. Run As → Maven build... → `clean verify`; sau đó → `javafx:run`.
5. Kiểm tra logo, Sidebar, Topbar, thông báo Sprint 0. Không thử đăng nhập thực vì chưa triển khai.
6. Đọc `PROMPT_START_CODEX.md`; từng bước giao Codex sinh FXML/CSS, bốn thành viên viết Controller/Backend.

## C. Vì sao có đúng 2 class Java?
- `app/Launcher.java` là `main()` khởi chạy JavaFX.
- `app/DentaFlowApp.java` là `Application.start(Stage)`, nạp FXML/CSS và gắn icon/Stage. Đó là bootstrap kỹ thuật, không phải Controller hoặc Backend.
- Các package `view/entity/service/database` phía Java **chưa cần tạo**; tạo khi bắt đầu UC, không sinh hàng loạt thư mục rỗng.

## D. Lưu ý
- `src/main/resources` là thư mục tài nguyên của Maven; trong Eclipse có thể cùng lúc hiện `src` và các source folder. Không xóa chỉ vì trông giống lặp.
- `.env.example` chỉ là mẫu, Java `System.getenv` sẽ **không tự đọc** `.env.example`.
- `database/sqlserver` lưu bản SQL Sprint 0 15 bảng để đối chiếu; không tự chạy trên database có dữ liệu.
- `mvn clean verify` xác minh compile + test resource; **không xác minh giao diện mở đúng**. `mvn javafx:run` mới chạy Stage.
- Nếu Maven không khả dụng ở command line, có thể dùng Maven tích hợp trong Eclipse (m2e).
- Nếu đang dùng Git repo cũ, tạo branch khôi phục trước khi copy source và không tạo repository mới.

Tài liệu: `docs/` và các prompt trong `prompts/`. Hãy bắt đầu bằng `PROMPT_START_CODEX.md`.
