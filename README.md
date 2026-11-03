# DentaFlow – Nhóm 19 IUH

Desktop Java 21, JavaFX/FXML/CSS, Maven, SQL Server JDBC; kiến trúc Controller → Service → DAO.

- Chỉ đăng nhập bằng tài khoản SQL, Role lấy từ NhanVien. Không có chế độ Demo hoặc chọn Role để bỏ qua xác thực.
- Mỗi thành viên dùng SQL Server local riêng, cùng database **DENTAFLOW_DB** và cùng bộ script trong Git.
- Copy .env.example → .env, cấu hình local; không commit bí mật. Không tự seed/reset database khi startup/test.
- UI đã xây dựng được giữ lại, không có bản ghi/số liệu nghiệp vụ mẫu. Chức năng chưa có backend hiển thị trống/đang phát triển.
- Build: `mvn clean verify`. Chạy: `mvn javafx:run` hoặc Main class `iuh.se.nhom19.app.KhoiChay` trong Eclipse với JDK 21.

Tên file/class/fx:id/CSS đã chuẩn hóa tiếng Việt không dấu. Đọc [bàn giao đổi tên](BAN_GIAO_CHUAN_HOA_TEN.md) và [bảng tên cũ → tên mới](BANG_DOI_TEN_DENTAFLOW.csv) trước khi nối Controller hoặc tích hợp branch khác. Cấu hình kết nối theo .env.example; chỉ chạy file 01 trên DB mới/rỗng bằng SSMS SQLCMD Mode.

UC001 đã có source và unit/UI tests; SQL end-to-end và bộ script phải được kiểm chứng trên local của thành viên. Các UC còn lại chưa được tuyên bố có backend. File 01 chỉ cho DB mới/rỗng, không chạy lại trên DB có dữ liệu; thay đổi DB hiện hữu dùng migration đã review và sao lưu.
