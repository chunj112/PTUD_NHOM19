# DentaFlow – Nhóm 19 IUH

Desktop Java 21, JavaFX/FXML/CSS, Maven, SQL Server JDBC; kiến trúc Controller → Service → DAO.

- Chỉ đăng nhập bằng tài khoản SQL, Role lấy từ NhanVien. Không có chế độ Demo hoặc chọn Role để bỏ qua xác thực.
- Cơ sở dữ liệu của ứng dụng có tên **DENTAFLOW_DB**. Không tạo database DEV/TEST và không tự dọn dữ liệu dùng chung.
- Copy .env.example → .env, cấu hình local; không commit bí mật. Không tự seed/reset database khi startup/test.
- UI đã xây dựng được giữ lại, không có bản ghi/số liệu nghiệp vụ mẫu. Chức năng chưa có backend hiển thị trống/đang phát triển.
- Build: `mvn clean verify`. Chạy: `mvn javafx:run` hoặc Main class `iuh.se.nhom19.app.KhoiChay` trong Eclipse với JDK 21.

Tên file/class/fx:id/CSS đã chuẩn hóa tiếng Việt không dấu. Đọc [bàn giao đổi tên](BAN_GIAO_CHUAN_HOA_TEN.md) và [bảng tên cũ → tên mới](BANG_DOI_TEN_DENTAFLOW.csv) trước khi nối Controller hoặc tích hợp branch khác. Cấu hình kết nối theo .env.example.

UC001 có đăng nhập SQL thật. Luồng **Phương án A** tách đặt lịch khỏi tiếp nhận: người mới chỉ có LichHen (maBN NULL); khi đến mới tạo/liên kết bệnh nhân, HSBA và phiếu CHO_KHAM. Tiếp nhận có hẹn/không hẹn tạo phiếu và cấp STT tăng dần theo ngày trong cùng transaction. Danh sách Y tá là chỉ đọc, giới hạn theo phòng/ca được phân công và đọc trạng thái trực tiếp từ `PhieuKham`; gọi bệnh nhân diễn ra ngoài ứng dụng. Nha sĩ là người đổi trạng thái khám. Quy tắc giờ đặt lịch hiện dùng slot 45 phút. Nhân sự, phân công CRUD, điều trị, hóa đơn, thanh toán và báo cáo vẫn cần Controller/Service/DAO hoàn chỉnh nếu chưa được nối.

## SQL Server: file nào cần chạy?

Trong Git chỉ có ba script SQL chuẩn ở `database/sqlserver/`:

1. `01_create_schema.sql` – chỉ chạy một lần khi tạo `DENTAFLOW_DB` mới/rỗng. Script yêu cầu SSMS bật **SQLCMD Mode**. Tuyệt đối không chạy lại trên database đang có dữ liệu.
2. `02_seed_reference_data.sql` – thêm các phòng và dịch vụ danh mục còn thiếu sau khi tạo schema. Script này cũng yêu cầu **SQLCMD Mode**.
3. `03_verify_schema.sql` – kiểm tra cấu trúc; chỉ đọc dữ liệu.

Thành viên clone để code trên database đã được nhóm chuẩn bị: cấu hình `.env` rồi chạy ứng dụng; không chạy lại script 01 hoặc script khởi tạo dữ liệu. Chỉ người dựng một database mới/rỗng mới chạy tuần tự 01 → 02 → 03. Không chạy script dọn dữ liệu trong quy trình clone hoặc khởi động ứng dụng.

Migration schema cũ chỉ dành cho database đã tạo trước đây và cần được người phụ trách SQL review riêng; không phải bước clone thông thường. Test SQL nghiệp vụ chỉ chạy khi đặt biến môi trường `DENTAFLOW_SQL_ROLLBACK_IT=1`; toàn bộ dữ liệu tạm nằm trong transaction ngoài luôn rollback. Biến `DENTAFLOW_SQL_WRITE_IT` cũ không còn kích hoạt test.
