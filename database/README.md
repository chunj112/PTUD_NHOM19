# SQL Server trong DentaFlow

Ba script SQL tại `sqlserver/` được **sao lại từ bộ Sprint 0 năm 2026**, mô tả 15 bảng và **không có bảng hình X-quang**.

- `01_create_schema.sql`: tạo database `NhaKhoa_QuanLy` và schema; **chỉ chạy trên database phát triển mới**.
- `02_seed_reference_data.sql`: tạo dữ liệu tham chiếu demo.
- `03_verify_schema.sql`: kiểm tra bảng và dữ liệu.

**Nếu database hiện tại đã có dữ liệu, không chạy lại `01_create_schema.sql`.** Xem và so sánh schema trước, migration phải được review. Script gốc ghi rõ chưa được xác minh bằng một phiên SQL Server thật trong môi trường này.

Database không phải một phần của JavaFX UI. DAO/Service/JDBC do thành viên tự lập trình trong Sprint tiếp theo.
