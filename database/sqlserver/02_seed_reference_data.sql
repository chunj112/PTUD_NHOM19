/* Danh muc khoi tao LOCAL, khong phai du lieu benh nhan/lich hen/hoa don.
   SSMS SQLCMD Mode hoac sqlcmd -b; chay thu cong sau file 01.
   Giu phong P01/P02 va hai dich vu tham chieu. Nhom thong nhat don gia truoc khi dung.
   Chi INSERT ma con thieu; khong UPDATE gia/ten/trang thai cua ban ghi da co. */
:ON ERROR EXIT
USE [DENTAFLOW_DB];
GO
IF OBJECT_ID(N'dbo.PhongKham',N'U') IS NULL OR OBJECT_ID(N'dbo.DichVu',N'U') IS NULL
    THROW 51041, N'Chua co schema PhongKham/DichVu. Hay kiem tra file 01.', 1;
SET XACT_ABORT ON;
BEGIN TRANSACTION;
GO
IF NOT EXISTS (SELECT 1 FROM dbo.PhongKham WHERE maPhong='P01')
 INSERT dbo.PhongKham(maPhong,tenPhong,trangThaiHoatDong) VALUES('P01',N'Phòng khám 01',1);
IF NOT EXISTS (SELECT 1 FROM dbo.PhongKham WHERE maPhong='P02')
 INSERT dbo.PhongKham(maPhong,tenPhong,trangThaiHoatDong) VALUES('P02',N'Phòng khám 02',1);
GO
IF NOT EXISTS (SELECT 1 FROM dbo.DichVu WHERE maDV='DV001')
 INSERT dbo.DichVu(maDV,tenDV,donViTinh,donGia,thoiGianThucHien,moTa,dangApDung)
 VALUES('DV001',N'Cạo vôi răng',N'Lần',250000.00,30,N'Danh mục khởi tạo; nhóm rà soát đơn giá trước khi sử dụng.',1);
IF NOT EXISTS (SELECT 1 FROM dbo.DichVu WHERE maDV='DV002')
 INSERT dbo.DichVu(maDV,tenDV,donViTinh,donGia,thoiGianThucHien,moTa,dangApDung)
 VALUES('DV002',N'Trám răng',N'Răng',350000.00,45,N'Danh mục khởi tạo; nhóm rà soát đơn giá trước khi sử dụng.',1);
GO
COMMIT TRANSACTION;
GO
-- Khong seed TaiKhoan bang SQL plaintext. Chay TaoTaiKhoanKiemThu thu cong neu da duoc phep.
