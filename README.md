# DentaFlow – Nhóm 19 IUH

Desktop Java 21, JavaFX/FXML/CSS, Maven, SQL Server JDBC; kiến trúc Controller → Service → DAO.

- Chỉ đăng nhập bằng tài khoản SQL, Role lấy từ NhanVien. Không có chế độ Demo hoặc chọn Role để bỏ qua xác thực.
- Mỗi thành viên dùng SQL Server local riêng, cùng database **DENTAFLOW_DB** và cùng bộ script trong Git.
- Copy .env.example → .env, cấu hình local; không commit bí mật. Không tự seed/reset database khi startup/test.
- UI đã xây dựng được giữ lại, không có bản ghi/số liệu nghiệp vụ mẫu. Chức năng chưa có backend hiển thị trống/đang phát triển.
- Build: `mvn clean verify`. Chạy: `mvn javafx:run` hoặc Main class `vn.edu.nhom19.nhakhoa.app.Launcher` trong Eclipse với JDK 21.

Đọc [HUONG_DAN_NHOM_CODE_BACKEND.md](HUONG_DAN_NHOM_CODE_BACKEND.md) trước khi khởi tạo local, tạo account, code Entity/DAO/Service/Controller hoặc tạo PR. [Hướng dẫn đăng nhập](HUONG_DAN_DANG_NHAP.md).

UC001 đã có source và unit/UI tests; SQL end-to-end và bộ script phải được kiểm chứng trên local của thành viên. Các UC còn lại chưa được tuyên bố có backend. File 01 chỉ cho DB mới/rỗng, không chạy lại trên DB có dữ liệu; thay đổi DB hiện hữu dùng migration đã review và sao lưu.
