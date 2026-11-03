# Bàn giao chuẩn hóa tên DentaFlow

Ngày 03/11/2026. Refactor trên feature/tuan-login, không chuyển nhánh, commit, push hoặc merge. Working tree sạch trước tác vụ. Không thay nghiệp vụ, layout, dữ liệu hiển thị, schema, tài khoản, biến môi trường hoặc thuật toán xác thực.

## 1. Số lượng và mapping

Bước chuẩn hóa tên ban đầu đổi 116 file: 87 FXML, 17 CSS, 7 Java main, 5 Java test. Bước đổi package tiếp theo di chuyển toàn bộ 26 Java (18 main, 8 test). Tính mỗi file một lần so với trước cả hai bước, có 130 file thay đổi đường dẫn/tên (87 FXML + 17 CSS + 26 Java). Giữ đủ 87 FXML và 8 lớp test (43 test case), không thay màn hình nghiệp vụ bằng placeholder. Các Java đã có tên tiếng Việt không dấu tiếp tục dùng tên cũ.

Đổi 1087 trong 1099 fx:id khác nhau, 270 tên CSS custom/marker và 6 biến màu -df-*. Giữ các tiền tố btn/txt/lbl/cbo/dp/tbl/... và thuật ngữ kỹ thuật Email/QR/FDI/NV. Đổi các enum route sang tên tiếng Việt; giữ nguyên MANAGER/RECEPTIONIST/DENTIST/NURSE.

Bảng BANG_DOI_TEN_DENTAFLOW.csv cùng thư mục chứa mapping đầy đủ theo loại: file, class, fx:id, CSS class/property, phương thức, biến và route. Chỉ đổi các định danh đã kiểm kê; Java dùng lexer bảo vệ chuỗi SQL, literal, tên package/API thư viện; FXML chỉ đổi thuộc tính tham chiếu; CSS chỉ đổi selector và biến màu tự định nghĩa.

## 2. Các lớp chính

| Tên cũ | Tên mới |
|---|---|
| app/Launcher.java | app/KhoiChay.java |
| app/DentaFlowApp.java | app/UngDungDentaFlow.java |
| app/AppNavigator.java | app/DieuHuongUngDung.java |
| app/Route.java | app/DuongDanManHinh.java |
| auth/Role.java | auth/VaiTro.java |
| auth/UserSession.java | auth/PhienDangNhap.java |
| view/common/AppShellController.java | view/common/KhungUngDungController.java |
| UserSessionTest.java | PhienDangNhapTest.java |
| ProjectStructureTest.java | CauTrucDuAnTest.java |
| DangNhapFlowTest.java | LuongDangNhapTest.java |
| DangNhapSqlReadOnlyTest.java | DangNhapSQLChiDocTest.java |
| BackendReadinessTest.java | SanSangKetNoiDuLieuTest.java |

Theo yêu cầu tiếp theo, package gốc đổi vn.edu.nhom19.nhakhoa → iuh.se.nhom19; di chuyển 18 Java main và 8 Java test. Các package kỹ thuật con app/auth/config/database/entity/dao/service/view giữ nguyên. Folder fxml/common, manager, receptionist và các đường dẫn role còn thiếu giữ nhóm thư mục cũ; tên folder role bám mã enum khi tính đường dẫn Dashboard. Không đổi tên ảnh, SVG hoặc nội dung nhị phân.

## 3. File tài nguyên tiêu biểu

| Cũ | Mới |
|---|---|
| fxml/common/login.fxml | fxml/common/dang-nhap.fxml |
| fxml/common/app-shell.fxml | fxml/common/khung-ung-dung.fxml |
| fxml/common/page-unavailable.fxml | fxml/common/trang-chua-san-sang.fxml |
| fxml/manager/dashboard.fxml | fxml/manager/tong-quan.fxml |
| fxml/manager/staff.fxml | fxml/manager/nhan-su.fxml |
| fxml/manager/staff-add.fxml | fxml/manager/them-nhan-vien.fxml |
| fxml/manager/assignments.fxml | fxml/manager/phan-cong.fxml |
| fxml/manager/services.fxml | fxml/manager/dich-vu.fxml |
| fxml/manager/reports.fxml | fxml/manager/bao-cao.fxml |
| fxml/receptionist/appointment-add.fxml | fxml/receptionist/them-lich-hen.fxml |
| fxml/receptionist/walk-in.fxml | fxml/receptionist/tiep-nhan-khong-hen.fxml |
| fxml/receptionist/intake-appointments.fxml | fxml/receptionist/tiep-nhan-lich-hen.fxml |
| css/theme.css | css/chu-de.css |
| css/receptionist-intake.css | css/le-tan-tiep-nhan.css |
| css/staff-form.css | css/bieu-mau-nhan-vien.css |

Danh sách đầy đủ 116 file ở các dòng loại file trong CSV. Những route chưa có FXML như Dentist/Nurse/Profile vẫn chưa có, chỉ cập nhật tên đường dẫn dự kiến sang tiếng Việt; không tạo màn hình mới hoặc xóa xử lý trang chưa sẵn sàng.

## 4. fx:id và CSS quan trọng

| Cũ | Mới |
|---|---|
| loginRoot | gocDangNhap |
| appShell | khungUngDung |
| contentArea | vungNoiDung |
| contentScroll | vungCuonNoiDung |
| managerMenu / receptionistMenu | trinhDonQuanLy / trinhDonLeTan |
| dentistMenu / nurseMenu | trinhDonNhaSi / trinhDonYTa |
| btnStaff / btnAssignments | btnNhanSu / btnPhanCong |
| btnIntakeAppointments | btnTrangTiepNhanLichHen (mở trang) |
| btnAdmitAppointment | btnTiepNhanLichHen (thao tác tiếp nhận) |
| btnBookAppointment | btnDatLichHen |
| btnAdmitWalkIn | btnTiepNhanKhongHen |
| txtWalkInPatientSearch | txtTimBenhNhanKhongHen |
| cboWalkInDentist | cboNhaSiKhongHen |
| .rx-field | .truong-nhap |
| .staff-form | .bieu-mau-nhan-vien |
| .menu-selected | .trinh-don-da-chon |
| -df-primary | -df-mau-chinh |

txtTenDangNhap, txtMatKhau, btnDangNhap và các fx:id đã là tiếng Việt tiếp tục giữ. Cập nhật đồng bộ @FXML field, lookup/getNamespace, onAction, fx:controller, fx:include, toggleGroup=$..., CSS selector và getStyleClass(). Có kiểm tra collision; không nhập hai tên cũ khác nghĩa thành cùng một định danh.

## 5. API điều hướng và phiên

| Cũ | Mới |
|---|---|
| AppNavigator.start() | DieuHuongUngDung.khoiDong() |
| showLogin / updateTitle / logout | hienThiDangNhap / capNhatTieuDe / dangXuat |
| configure / dispose | cauHinh / giaiPhong |
| navigate / handleLogout | chuyenTrang / xuLyDangXuat |
| isCurrent / hideRoleMenus | laPhienHienTai / anTrinhDonVaiTro |
| UserSession.isActive / getRole | PhienDangNhap.dangHoatDong / layVaiTro |
| requireRole / version / clear | yeuCauVaiTro / layPhienBan / xoa |
| Role.displayName | VaiTro.layTenHienThi |
| Route.title / buttonId / allows / fxml | DuongDanManHinh.layTieuDe / layMaNut / choPhep / layDuongDanFXML |

Luồng vẫn là DangNhapController → DangNhapService → TaiKhoanDAO → KetNoiCSDL → SQL. Thành công tạo PhienDangNhap → KhungUngDungController → Dashboard theo Role. Logout xóa danh tính, tăng phiên bản và vô hiệu callback cũ. Sai mật khẩu/trạng thái/SQL lỗi không tạo phiên. Không bổ sung backend hoặc dữ liệu mẫu.

## 6. Tên bắt buộc giữ nguyên

- Java/JDBC/JavaFX/JUnit/Maven API và import thư viện: DriverManager, Connection, FXMLLoader, Scene, Stage, PreparedStatement, @Test... không dịch.
- main(String[]), Application.start/stop, Task.call, AutoCloseable.close, initialize của FXMLLoader giữ nguyên hợp đồng thư viện. Riêng start của bộ điều hướng tự viết được đổi khoiDong, không đụng Thread.start hoặc Platform.startup.
- Thuộc tính FXML chuẩn fx:id/fx:controller/styleClass/onAction/stylesheets, class JavaFX, tên property và constant của control giữ nguyên.
- CSS skin .root/.label/.table-view/.table-cell/.arrow-button/.tab/...; pseudo-class :hover/:focused/:selected/...; thuộc tính -fx-* và giá trị CSS chuẩn giữ nguyên.
- Role constants, mã trạng thái/cột/bảng SQL, giá trị username/account, key .env, chuỗi định dạng PBKDF2 và tham số KDF giữ nguyên. Không dịch dữ liệu hoặc mật khẩu.
- Các chuỗi tên API cũ trong test phủ định (kiểm tra không có bypass) giữ đúng tên lịch sử; không tạo alias lớp/API cũ trong production.

## 7. Kiểm tra thực tế

- Maven clean verify cuối: 43 test, 37 đạt, 6 SQL chỉ đọc bỏ qua, 0 failure/error, exit 0. Không bỏ/vô hiệu test để build xanh.
- Nạp/applyCss/layout đủ 87 FXML; kiểm tra field inject đúng fx:id và 5 tham chiếu onAction trên hai Controller hiện có, không thiếu sự kiện.
- Không trùng fx:id trong cùng file; mọi fx:include/CSS/ảnh còn tồn tại.
- Đối chiếu ngược mapping từng node/thuộc tính FXML và từng rule CSS với bản trước: layout, text, kích thước, padding, màu, giá trị style và cấu trúc con giữ nguyên. 17 CSS chỉ khác tên selector/custom property.
- Bốn ảnh render (Nhân sự, Dashboard Manager, Dashboard Receptionist, chuyển khoản) trùng pixel với ảnh QA cũ. Đã xem ảnh Nhân sự/Login mới.
- Có 4 styleClass vốn là marker chưa có rule riêng trước refactor: dashboard-metric-card, login-scroll, matrix-afternoon, staff-details. Giữ marker theo mapping, không phát sinh selector mất tham chiếu mới hoặc tự thêm rule thay giao diện.
- Unit/UI kiểm tra bốn Role, đăng nhập bằng Enter, sai mật khẩu/SQL lỗi, chặn route trái Role, giữ Scene/AppShell khi chuyển trang, logout/account khác, chống submit lặp/callback cũ đều đạt bằng DAO fixture.
- Maven javafx:run JDK21 dùng Main mới KhoiChay mở cửa sổ DentaFlow · Đăng nhập; đóng đúng cửa sổ kiểm thử, Maven exit 0. Không tác động cửa sổ Lễ tân cũ đang mở của người dùng.
- SQL script, .env.example, .gitignore và các PNG/SVG không đổi byte. Không đọc/in mật khẩu .env; không chạy seed, truy vấn hoặc ghi dữ liệu SQL. Sáu test SQL bỏ qua do cấu hình opt-in chưa bật trong lần verify, chưa tuyên bố SQL end-to-end sau refactor.
- Cảnh báo SLF4J NOP/JavaFX unnamed module vẫn thuộc runtime hiện tại, không lỗi test/FXML. git diff --check đạt.

## 8. Cách chạy và tích hợp tiếp

1. Eclipse nhấn F5 để refresh file đổi tên. Maven → Update Project nếu IDE còn giữ cache tên cũ.
2. Maven goal javafx:run giữ nguyên; pom.xml chỉ đổi mainClass từ vn.edu.nhom19.nhakhoa.app.Launcher sang iuh.se.nhom19.app.KhoiChay. Dependency/plugin/version giữ nguyên.
3. Nếu Java Application Run Configuration cũ trỏ Launcher, đổi Main class thành iuh.se.nhom19.app.KhoiChay. Công cụ tạo tài khoản dùng Main class iuh.se.nhom19.database.TaoTaiKhoanKiemThu. Các cấu hình JUnit riêng cần chọn lại lớp test đã đổi tên.
4. Có thể dùng -Djavafx.executable=D:/Apps/JDK_21/bin/java.exe trên máy hiện tại; máy khác chọn đúng Java21 của họ. Working directory vẫn folder pom.xml/.env.
5. Đóng app phiên bản cũ rồi chạy lại để dùng class/FXML mới. Các branch backend chưa merge phải cập nhật import, Session API, route, fx:id và resource path theo CSV; không tự merge các branch đó trong tác vụ này.
6. Tài liệu UI/Word cũ còn nêu tên tiếng Anh cần được chủ tài liệu cập nhật theo mapping. Source/test/pom/README hiện đã dùng tên mới. Không tự sửa bộ Word hoặc các tài liệu lịch sử trong tác vụ refactor code.
7. Trước PR kiểm tra git diff và bảng mapping; file cũ có thể hiện D và file mới ?? khi chưa stage. Đây là đổi tên native chưa stage; agent chưa thực hiện git add/commit/push/merge.

## 9. File bàn giao mới

- BANG_DOI_TEN_DENTAFLOW.csv: bảng đầy đủ.
- BAN_GIAO_CHUAN_HOA_TEN.md: tài liệu này.
- README cập nhật Main class và liên kết bàn giao có thật, thay các liên kết hướng dẫn đã thiếu từ trước tác vụ.

Không có chức năng backend mới, không schema migration. Phần chưa tích hợp UI Dentist/Nurse/Profile giữ đúng tình trạng trước refactor. Cần dùng credential SQL local hợp lệ để tự nghiệm thu lại đăng nhập thật sau khi nhóm áp dụng nhánh này.

## 10. Cập nhật package gốc

Package chính thức hiện là iuh.se.nhom19. Java main nằm src/main/java/iuh/se/nhom19, test nằm src/test/java/iuh/se/nhom19. Cập nhật package/import, FQCN trong test, hai fx:controller FXML và mainClass Maven. Tên class, fx:id, CSS và 87 đường dẫn FXML đã chuẩn hóa ở bước trước giữ nguyên.

Run Configuration Java Application cũ cần chọn lại iuh.se.nhom19.app.KhoiChay hoặc iuh.se.nhom19.database.TaoTaiKhoanKiemThu. Maven goal javafx:run, Working directory và .env giữ nguyên. Các cấu hình JUnit cũ cần chọn lại package/lớp. F5 và Maven Update Project nếu Eclipse còn hiển thị tên cũ.

Maven groupId vn.edu.nhom19 vẫn là tọa độ artifact, độc lập với package Java; không đổi dependency, plugin hoặc version. Không đổi SQL, ảnh, key môi trường hoặc tài khoản. Bảng mapping đã thêm 26 dòng package/file và cập nhật cột tên mới của các file Java từng đổi tên.

Kiểm tra lại sau đổi package: clean verify 43 test (37 đạt, 6 SQL bỏ qua, 0 lỗi), đủ 87 FXML nạp được; javafx:run mở Login bằng iuh.se.nhom19.app.KhoiChay và kết thúc exit 0. Không chạy SQL/seed, không commit/push/merge.

## 11. Dọn thông báo đang phát triển và nút chuông

Theo yêu cầu tiếp theo, đã bỏ nút btnThongBao cùng ImageView/Tooltip trong common/khung-ung-dung.fxml. Ảnh bell.png và phần Java xử lý nút đã được người dùng bỏ trước lượt này; FXML nay không còn tham chiếu ảnh bị thiếu. KhungUngDungController chỉ được cập nhật comment và khoảng trắng cho phù hợp nội dung trang không còn banner.

Đã bỏ 29 Label thông báo lặp “Chức năng đang phát triển · Chưa tải dữ liệu từ hệ thống.” trong 27 FXML Manager/Receptionist. Cấu trúc bảng, bộ lọc, modal và sự kiện còn dùng được giữ nguyên. Login đã không còn banner trước lượt này; giữ tiêu đề Đăng nhập, mô tả tài khoản và lblLoiDangNhap để báo lỗi thật.

CSS chu-de.css khôi phục hover/focus của menu tài khoản sau khi người dùng bỏ rule chung với nút chuông. Bỏ rule tong-quan-phu-de không còn dùng trong quan-ly-tong-quan.css. Những class chú thích dùng chung vẫn phục vụ thành phần khác nên giữ lại. BANG_DOI_TEN_DENTAFLOW.csv đánh dấu các fx:id/class/method bị gỡ để tránh dùng nhầm khi tích hợp Controller.

Giữ trạng thái bảng chưa tải dữ liệu, trang fallback khi route chưa có UI và cơ chế đăng nhập/Session/đăng xuất. Việc bỏ banner không có nghĩa các chức năng nghiệp vụ đã có backend.

Kiểm tra: 87 FXML nạp/applyCss/layout được; không thiếu CSS/ảnh/include. Đã render và xem Login/Nhân sự sau chỉnh sửa. Không truy cập SQL, không đổi cấu hình kết nối, pom.xml hay các test. Đóng ứng dụng cũ, F5 trong Eclipse rồi chạy lại javafx:run để xem phiên bản mới.

Maven clean verify sau dọn thông báo: exit 0; 43 test, 37 đạt, 6 SQL opt-in bỏ qua, 0 failure/error. Chưa kiểm thử đăng nhập trực tiếp với SQL Server trong lượt này. Không stage/commit/push/merge.
