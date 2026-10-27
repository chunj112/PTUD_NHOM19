USE [NhaKhoa_QuanLy];
GO
SELECT COUNT(*) AS soBangNghiepVu
FROM sys.tables WHERE schema_id=SCHEMA_ID('dbo');
-- Expected: 15 after 01_create_schema.sql.
SELECT name AS tenBang FROM sys.tables WHERE schema_id=SCHEMA_ID('dbo') ORDER BY name;
SELECT OBJECT_ID(N'dbo.PhieuKhamAnh',N'U') AS xquangTable; -- must be NULL
SELECT OBJECT_ID(N'dbo.LichLamViec',N'U') AS lichLamViecTable; -- must be NULL
SELECT maPhong,tenPhong FROM dbo.PhongKham;
SELECT maDV,tenDV,donGia FROM dbo.DichVu;
GO
