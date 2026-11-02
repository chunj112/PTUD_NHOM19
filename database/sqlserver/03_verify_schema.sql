/* CHI DOC metadata/du lieu; khong sua schema hoac ghi du lieu.
   SSMS SQLCMD Mode hoac sqlcmd -b. Khong SELECT matKhau/hash ra ket qua. */
:ON ERROR EXIT
USE [DENTAFLOW_DB];
GO
SELECT DB_NAME() AS tenDatabase, COUNT(*) AS soBangNghiepVu
FROM sys.tables WHERE schema_id=SCHEMA_ID('dbo') AND is_ms_shipped=0;
-- Expected baseline: 15 bang. Bang thieu duoc liet ke ben duoi.
WITH BangBatBuoc AS (
    SELECT tenBang FROM (VALUES
        (N'PhongKham'),
        (N'NhanVien'),
        (N'TaiKhoan'),
        (N'PhanCong'),
        (N'BenhNhan'),
        (N'HoSoBenhAn'),
        (N'LichHen'),
        (N'DichVu'),
        (N'PhieuKham'),
        (N'ChiTietPhieuKham'),
        (N'DonThuoc'),
        (N'ChiTietDonThuoc'),
        (N'HoaDon'),
        (N'ChiTietHoaDon'),
        (N'ThanhToan')
    ) ds(tenBang)
)
SELECT tenBang AS bangChuaCo FROM BangBatBuoc
WHERE OBJECT_ID(N'dbo.'+tenBang,N'U') IS NULL;
SELECT name AS tenBang FROM sys.tables WHERE schema_id=SCHEMA_ID('dbo') AND is_ms_shipped=0 ORDER BY name;
SELECT name AS tenView FROM sys.views WHERE schema_id=SCHEMA_ID('dbo') AND is_ms_shipped=0 ORDER BY name;
SELECT name AS tenTrigger, is_disabled FROM sys.triggers WHERE is_ms_shipped=0 ORDER BY name;
SELECT OBJECT_NAME(parent_object_id) AS bang, name AS khoaNgoai, is_disabled, is_not_trusted
FROM sys.foreign_keys ORDER BY bang,name;
SELECT OBJECT_NAME(object_id) AS bang, name AS chiMuc, is_unique, filter_definition
FROM sys.indexes WHERE is_unique=1 AND OBJECT_SCHEMA_NAME(object_id)='dbo' ORDER BY bang,chiMuc;
GO
IF OBJECT_ID(N'dbo.NhanVien',N'U') IS NULL OR OBJECT_ID(N'dbo.TaiKhoan',N'U') IS NULL
    THROW 51042, N'Thieu NhanVien/TaiKhoan; UC001 chua the dang nhap.', 1;
GO
-- Doi chieu Entity/DAO: ten cot, kieu, do dai va nullable (NVARCHAR max_length la byte).
SELECT OBJECT_NAME(c.object_id) AS bang, c.name AS cot, t.name AS kieuSQL, c.max_length, c.is_nullable
FROM sys.columns c JOIN sys.types t ON c.user_type_id=t.user_type_id
WHERE c.object_id IN (OBJECT_ID(N'dbo.NhanVien'),OBJECT_ID(N'dbo.TaiKhoan'))
ORDER BY bang,c.column_id;
SELECT OBJECT_NAME(parent_object_id) AS bang, name AS rangBuoc, definition
FROM sys.check_constraints WHERE parent_object_id IN (OBJECT_ID(N'dbo.NhanVien'),OBJECT_ID(N'dbo.TaiKhoan'));
-- Kiem tra so bo tien to/do dai; Service Java moi kiem tra day du so vong, Base64, salt va hash.
SELECT COUNT(*) AS soTaiKhoanCanKiemTraDinhDang
FROM dbo.TaiKhoan WHERE matKhau NOT LIKE 'PBKDF2-SHA256$%' OR LEN(matKhau)<80;
SELECT tk.maTaiKhoan,tk.tenDangNhap,tk.maNV,nv.hoTen,nv.chucVu,tk.trangThaiHoatDong,nv.trangThaiLamViec
FROM dbo.TaiKhoan tk JOIN dbo.NhanVien nv ON tk.maNV=nv.maNV ORDER BY tk.tenDangNhap;
SELECT maPhong,tenPhong FROM dbo.PhongKham;
SELECT maDV,tenDV,donGia FROM dbo.DichVu;
GO
