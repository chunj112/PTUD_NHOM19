/* DEMO-only reference data. Run AFTER 01_create_schema.sql on a NEW dev DB. */
USE [NhaKhoa_QuanLy];
GO
IF NOT EXISTS (SELECT 1 FROM dbo.PhongKham WHERE maPhong='P01')
 INSERT dbo.PhongKham(maPhong,tenPhong,trangThaiHoatDong) VALUES('P01',N'Phòng khám 01',1);
IF NOT EXISTS (SELECT 1 FROM dbo.PhongKham WHERE maPhong='P02')
 INSERT dbo.PhongKham(maPhong,tenPhong,trangThaiHoatDong) VALUES('P02',N'Phòng khám 02',1);
GO
IF NOT EXISTS (SELECT 1 FROM dbo.DichVu WHERE maDV='DV001')
 INSERT dbo.DichVu(maDV,tenDV,donViTinh,donGia,thoiGianThucHien,moTa,dangApDung)
 VALUES('DV001',N'Cạo vôi răng',N'Lần',250000.00,30,N'Dữ liệu demo',1);
IF NOT EXISTS (SELECT 1 FROM dbo.DichVu WHERE maDV='DV002')
 INSERT dbo.DichVu(maDV,tenDV,donViTinh,donGia,thoiGianThucHien,moTa,dangApDung)
 VALUES('DV002',N'Trám răng',N'Răng',350000.00,45,N'Dữ liệu demo',1);
GO
-- Accounts are NOT seeded with plaintext passwords.
-- Tuong (UC001) implements secure password hashing, then creates demo accounts.
