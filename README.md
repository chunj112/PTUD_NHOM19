# DentaFlow – Nhóm 19 IUH

Desktop Java 21, JavaFX/FXML/CSS, Maven, SQL Server JDBC; kiến trúc Controller → Service → DAO.

- Chỉ đăng nhập bằng tài khoản SQL, Role lấy từ NhanVien. Không có chế độ Demo hoặc chọn Role để bỏ qua xác thực.
- Mỗi thành viên dùng SQL Server local riêng, cùng database **DENTAFLOW_DB** và cùng bộ script trong Git.
- Copy .env.example → .env, cấu hình local; không commit bí mật. Không tự seed/reset database khi startup/test.
- UI đã xây dựng được giữ lại, không có bản ghi/số liệu nghiệp vụ mẫu. Chức năng chưa có backend hiển thị trống/đang phát triển.
- Build: `mvn clean verify`. Chạy: `mvn javafx:run` hoặc Main class `vn.edu.nhom19.nhakhoa.app.Launcher` trong Eclipse với JDK 21.
