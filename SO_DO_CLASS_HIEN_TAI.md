# Sơ đồ lớp tổng quát – DentaFlow hiện tại

Sơ đồ thể hiện các lớp chính đang có trong Java source và hướng phụ thuộc giữa các tầng. `Entity` là mô hình dữ liệu dùng chung, không phải tầng xử lý thứ tư.

```mermaid
classDiagram
direction LR

namespace App {
  class UngDungDentaFlow {
    +start(Stage)
    +stop()
  }
  class DieuHuongUngDung {
    -Stage cuaSo
    -PhienDangNhap phienDangNhap
    -DangNhapService dichVuDangNhap
    +khoiDong()
    +moSauDangNhap()
    +dangXuat()
    +close()
  }
  class DuongDanManHinh {
    <<enum>>
    +TONG_QUAN
    +TIEP_NHAN
    +KHAM_BENH
    +DANH_SACH_CHO_Y_TA
  }
}

namespace View_Controller {
  class DangNhapController
  class KhungUngDungController
  class TiepNhanController
  class LichHenNgayController
  class LichHenTuanController
  class DatLichHenController
  class TiepNhanCoHenController
  class KhongHenController
  class TraCuuBenhNhanController
  class DanhSachChoKhamController
  class DanhSachChoYTaController
  class TongQuanYTaController
  class LichLamViecYTaController
  class CongViecGiaoDien {
    <<utility>>
    +chay(...)
    +moHopThoai(...)
    +dong(...)
  }
}

namespace BUS_Service {
  class DangNhapService {
    +dangNhap(tenDangNhap, matKhau) KetQuaDangNhap
  }
  class LichHenService {
    +layDanhSachLichHen(...)
    +datLichHen(...)
    +suaLichHen(...)
    +huyLichHen(...)
  }
  class TiepNhanService {
    +tiepNhanCoHen(...)
    +tiepNhanKhongHen(...)
  }
  class BenhNhanService {
    +timBenhNhan(...)
    +themBenhNhan(...)
  }
  class DanhSachChoKhamService {
    +layDanhSachChoKham()
    +batDauKham(...)
    +hoanTatKham(...)
  }
  class DanhSachChoYTaService {
    +layPhongDuocPhanCong(...)
    +layDanhSach(...)
  }
  class PhanCongService {
    +layDanhSachNhaSi(...)
    +layKhungGioKhaDung(...)
  }
  class QuyTacLichHen {
    <<business rules>>
    +kiemTraSlot(...)
    +taoSlot(...)
  }
  class KiemTraNghiepVu {
    <<business rules and validation>>
    +yeuCau(vaiTro)
    +soDienThoai(...)
    +hoanTac(...)
  }
}

namespace DAO_Data {
  class TaiKhoanDAO
  class BenhNhanDAO
  class HoSoBenhAnDAO
  class LichHenDAO
  class PhanCongDAO
  class PhieuKhamDAO
  class KetNoiCSDL {
    +layKetNoi() Connection
  }
}

namespace Entity_Domain {
  class VaiTro {
    <<enum>>
    MANAGER
    RECEPTIONIST
    DENTIST
    NURSE
  }
  class PhienDangNhap {
    -VaiTro vaiTro
    -String maNV
    -String hoTen
    +batDauPhienDangNhap(...)
    +yeuCauVaiTro(...)
    +xoa()
  }
  class KetQuaDangNhap
  class TaiKhoan
  class NhanVien
  class BenhNhan
  class HoSoBenhAn
  class LichHen
  class PhieuKham
  class PhanCong
  class KhungGio
}

UngDungDentaFlow --> DieuHuongUngDung
DieuHuongUngDung --> DangNhapController : nạp FXML
DieuHuongUngDung --> KhungUngDungController : sau đăng nhập
DieuHuongUngDung --> DangNhapService
DieuHuongUngDung --> PhienDangNhap
KhungUngDungController --> DuongDanManHinh : điều hướng theo Role

DangNhapController --> DangNhapService
TiepNhanController --> DatLichHenController : mở modal
TiepNhanController --> TiepNhanCoHenController : mở modal
TiepNhanController --> KhongHenController : mở modal
TiepNhanController --> LichHenNgayController
TiepNhanController --> LichHenTuanController
DatLichHenController --> LichHenService
DatLichHenController --> PhanCongService : tải ca/khung giờ
TiepNhanCoHenController --> TiepNhanService
KhongHenController --> TiepNhanService
TraCuuBenhNhanController --> BenhNhanService
DanhSachChoKhamController --> DanhSachChoKhamService
DanhSachChoYTaController --> DanhSachChoYTaService
TongQuanYTaController --> DanhSachChoYTaService
TiepNhanController ..> CongViecGiaoDien : tác vụ nền và modal
DatLichHenController ..> CongViecGiaoDien : tác vụ nền và modal
DanhSachChoKhamController ..> CongViecGiaoDien : tác vụ nền

DangNhapService --> TaiKhoanDAO
LichHenService --> BenhNhanDAO
LichHenService --> LichHenDAO
LichHenService --> PhanCongDAO
TiepNhanService --> BenhNhanDAO
TiepNhanService --> HoSoBenhAnDAO
TiepNhanService --> LichHenDAO
TiepNhanService --> PhieuKhamDAO
TiepNhanService --> PhanCongDAO
BenhNhanService --> BenhNhanDAO
DanhSachChoKhamService --> PhieuKhamDAO
DanhSachChoYTaService --> PhieuKhamDAO
DanhSachChoYTaService --> PhanCongDAO
PhanCongService --> PhanCongDAO
PhanCongService --> LichHenDAO
LichHenService ..> KiemTraNghiepVu
TiepNhanService ..> KiemTraNghiepVu
BenhNhanService ..> KiemTraNghiepVu
DanhSachChoKhamService ..> KiemTraNghiepVu
DanhSachChoYTaService ..> KiemTraNghiepVu
LichHenService --> QuyTacLichHen

TaiKhoanDAO --> KetNoiCSDL
BenhNhanDAO --> KetNoiCSDL
HoSoBenhAnDAO --> KetNoiCSDL
LichHenDAO --> KetNoiCSDL
PhanCongDAO --> KetNoiCSDL
PhieuKhamDAO --> KetNoiCSDL

TaiKhoan --> NhanVien : thuộc về
TaiKhoanDAO ..> TaiKhoan : đọc
DangNhapService ..> KetQuaDangNhap : trả kết quả
KetQuaDangNhap ..> PhienDangNhap : tạo phiên
PhienDangNhap --> VaiTro
PhienDangNhap ..> NhanVien : lưu mã và tên nhân viên
LichHenService ..> LichHen : tạo/sửa
TiepNhanService ..> PhieuKham : tạo
TiepNhanService ..> BenhNhan : tìm hoặc tạo
DanhSachChoKhamService ..> PhieuKham : cập nhật trạng thái
DanhSachChoYTaService ..> PhieuKham : chỉ đọc
PhanCongService ..> KhungGio : trả khung khả dụng

BenhNhan "1" -- "1" HoSoBenhAn : có hồ sơ
BenhNhan "1" -- "0..*" PhieuKham : có các lần khám
BenhNhan "0..1" -- "0..*" LichHen : có thể liên kết trước/sau check-in
NhanVien "1" -- "0..*" LichHen : nha sĩ phụ trách
NhanVien "1" -- "0..*" PhieuKham : nha sĩ khám
NhanVien "1" -- "0..*" PhieuKham : lễ tân tiếp nhận
LichHen "0..1" -- "0..1" PhieuKham : tối đa một lần tiếp nhận
NhanVien "1" -- "0..*" PhanCong : được phân công
PhanCong ..> KhungGio : dùng để tính ca trống
```

## Những điểm sơ đồ phản ánh đúng source

- Các Controller thuộc GUI gọi Service; Service kiểm tra vai trò/nghiệp vụ và gọi DAO; DAO dùng JDBC thông qua `KetNoiCSDL`.
- `TiepNhanService` xử lý cả có hẹn và không hẹn, tạo/liên kết hồ sơ rồi tạo `PhieuKham` trong transaction.
- `DanhSachChoYTaService` chỉ đọc phiếu khám theo quyền/phòng được phân công; trạng thái được cập nhật bởi luồng Nha sĩ.
- `QuyTacLichHen` hiện áp dụng lịch hẹn 45 phút cố định.
- Một số lớp dùng cho chức năng đăng nhập, tiếp nhận, lịch hẹn và danh sách chờ có đầy đủ tầng hơn các module quản lý.

## Những phần chưa thể hiện thành Java class hiện tại

Schema SQL có thêm các bảng `DichVu`, `ChiTietPhieuKham`, `DonThuoc`, `ChiTietDonThuoc`, `HoaDon`, `ChiTietHoaDon`, `ThanhToan`, `PhongKham` và các view báo cáo. Source hiện chưa có Entity/DAO/Service đầy đủ tương ứng cho các module đó. Nhân sự, phân công quản lý, dịch vụ, báo cáo và thu ngân cũng chưa có backend hoàn chỉnh theo README. Vì vậy không nên vẽ các lớp backend này như thể đã triển khai.
