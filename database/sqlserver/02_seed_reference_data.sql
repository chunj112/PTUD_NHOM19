/* Danh muc va du lieu demo LOCAL cho DENTAFLOW_DB.
   SSMS SQLCMD Mode hoac sqlcmd -b; chay sau file 01.
   Tao phong/dich vu neu thieu va bo sung 3 phieu kham hoan thanh de demo lap hoa don.
   Chi INSERT ban ghi con thieu; khong UPDATE/xoa du lieu dang co. */
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
IF OBJECT_ID(N'dbo.BenhNhan',N'U') IS NULL OR OBJECT_ID(N'dbo.HoSoBenhAn',N'U') IS NULL
   OR OBJECT_ID(N'dbo.PhieuKham',N'U') IS NULL OR OBJECT_ID(N'dbo.ChiTietPhieuKham',N'U') IS NULL
   OR OBJECT_ID(N'dbo.NhanVien',N'U') IS NULL
    THROW 51042, N'Thieu bang nghiep vu. Hay chay file 01_create_schema.sql truoc.', 1;

DECLARE @maNhaSi VARCHAR(20), @maLeTan VARCHAR(20), @soThuTuBN BIGINT;
SELECT TOP (1) @maNhaSi=maNV FROM dbo.NhanVien
 WHERE chucVu='NHA_SI' AND trangThaiLamViec=1 ORDER BY maNV;
SELECT TOP (1) @maLeTan=maNV FROM dbo.NhanVien
 WHERE chucVu='LE_TAN' AND trangThaiLamViec=1 ORDER BY maNV;
IF @maNhaSi IS NULL OR @maLeTan IS NULL
    THROW 51043, N'Can co it nhat mot nha si va mot le tan dang lam viec de tao phieu demo.', 1;
IF NOT EXISTS (SELECT 1 FROM dbo.PhongKham WHERE maPhong='P01' AND trangThaiHoatDong=1)
    THROW 51044, N'Phong P01 khong ton tai hoac dang ngung hoat dong.', 1;
IF NOT EXISTS (SELECT 1 FROM dbo.DichVu WHERE maDV='DV001' AND dangApDung=1)
   OR NOT EXISTS (SELECT 1 FROM dbo.DichVu WHERE maDV='DV002' AND dangApDung=1)
    THROW 51045, N'Can co hai dich vu DV001 va DV002 dang ap dung de tao chi tiet phieu demo.', 1;

DECLARE @BenhNhanDemo TABLE (
    hoTen NVARCHAR(120) NOT NULL, gioiTinh BIT NOT NULL, ngaySinh DATE NOT NULL,
    soDienThoai VARCHAR(15) NOT NULL, diaChi NVARCHAR(255) NOT NULL, maBN VARCHAR(20) NULL
);
INSERT @BenhNhanDemo(hoTen,gioiTinh,ngaySinh,soDienThoai,diaChi) VALUES
 (N'Nguyễn Minh Anh',0,'19880514','0987000001',N'Quận 3, TP. Hồ Chí Minh'),
 (N'Trần Quốc Bảo',1,'19921123','0987000002',N'Quận 10, TP. Hồ Chí Minh'),
 (N'Lê Thu Hà',0,'19860908','0987000003',N'Quận Tân Bình, TP. Hồ Chí Minh');

IF EXISTS (
    SELECT 1 FROM @BenhNhanDemo mau JOIN dbo.BenhNhan bn ON bn.soDienThoai=mau.soDienThoai
    WHERE bn.hoTen<>mau.hoTen
)
    THROW 51046, N'So dien thoai demo da duoc dung cho benh nhan khac. Hay doi so demo sau khi kiem tra du lieu.', 1;

-- Mã BN tiếp tục theo số lớn nhất hiện có; không dựa vào số dòng hoặc xóa/reset dữ liệu.
SELECT @soThuTuBN=ISNULL(MAX(TRY_CONVERT(BIGINT,SUBSTRING(maBN,3,18))),0)
FROM dbo.BenhNhan WITH (UPDLOCK,HOLDLOCK) WHERE maBN LIKE 'BN%';
;WITH BenhNhanCanThem AS (
    SELECT mau.*, ROW_NUMBER() OVER(ORDER BY mau.soDienThoai) AS soMoi
    FROM @BenhNhanDemo mau
    WHERE NOT EXISTS (SELECT 1 FROM dbo.BenhNhan bn WHERE bn.soDienThoai=mau.soDienThoai)
)
INSERT dbo.BenhNhan(maBN,hoTen,gioiTinh,ngaySinh,soDienThoai,diaChi)
SELECT CONCAT('BN',CASE WHEN @soThuTuBN+soMoi<10000
    THEN RIGHT('0000'+CONVERT(VARCHAR(18),@soThuTuBN+soMoi),4)
    ELSE CONVERT(VARCHAR(18),@soThuTuBN+soMoi) END),
    hoTen,gioiTinh,ngaySinh,soDienThoai,diaChi
FROM BenhNhanCanThem;

UPDATE mau SET maBN=bn.maBN
FROM @BenhNhanDemo mau JOIN dbo.BenhNhan bn ON bn.soDienThoai=mau.soDienThoai;

-- Tạo hồ sơ bệnh án còn thiếu; không ghi đè hồ sơ bệnh nhân đã có.
INSERT dbo.HoSoBenhAn(maHSBA,maBN,tienSuBenh,tienSuDiUng)
SELECT CONCAT('HS',LEFT(REPLACE(CONVERT(VARCHAR(36),NEWID()),'-',''),18)),mau.maBN,
       N'Hồ sơ demo phục vụ trình diễn lập hóa đơn.',NULL
FROM @BenhNhanDemo mau
WHERE NOT EXISTS (SELECT 1 FROM dbo.HoSoBenhAn hs WHERE hs.maBN=mau.maBN);

DECLARE @PhieuDemo TABLE (
    maPhieuKham VARCHAR(20) NOT NULL PRIMARY KEY, maBN VARCHAR(20) NOT NULL,
    lyDo NVARCHAR(500) NOT NULL, chanDoan NVARCHAR(1000) NOT NULL,
    tinhTrangLamSan NVARCHAR(1000) NOT NULL
);
INSERT @PhieuDemo(maPhieuKham,maBN,lyDo,chanDoan,tinhTrangLamSan)
SELECT mau.maPhieuKham,bn.maBN,mau.lyDo,mau.chanDoan,mau.tinhTrangLamSan
FROM (VALUES
 ('PK-DEMO-001','0987000001',N'Cạo vôi và ê buốt răng hàm',N'Viêm nướu nhẹ, ê buốt răng 16',N'Đã cạo vôi; bề mặt răng 16 được làm sạch.'),
 ('PK-DEMO-002','0987000002',N'Đau răng hàm',N'Sâu răng 26, cần phục hồi',N'Đã làm sạch vùng sâu và trám răng 26.'),
 ('PK-DEMO-003','0987000003',N'Khám tổng quát và đau răng',N'Sâu răng 11 và 21',N'Đã trám răng 11 và 21; hướng dẫn vệ sinh răng miệng.')
) mau(maPhieuKham,soDienThoai,lyDo,chanDoan,tinhTrangLamSan)
JOIN dbo.BenhNhan bn ON bn.soDienThoai=mau.soDienThoai;

IF EXISTS (
    SELECT 1 FROM @PhieuDemo mau JOIN dbo.PhieuKham pk ON pk.maPhieuKham=mau.maPhieuKham
    WHERE pk.maBN<>mau.maBN OR pk.trangThaiKham<>'DA_KHAM'
)
    THROW 51047, N'Ma PK-DEMO da ton tai voi du lieu khac. Script dung lai va khong sua phieu hien co.', 1;

INSERT dbo.PhieuKham(maPhieuKham,maBN,maNVNhaSi,maNVLeTan,maLH,maPhong,
    thoiDiemTiepNhan,tinhTrangLamSan,chanDoan,ngayKham,lyDo,trangThaiKham)
SELECT mau.maPhieuKham,mau.maBN,@maNhaSi,@maLeTan,NULL,'P01',
       DATEADD(HOUR,-2,SYSDATETIME()),mau.tinhTrangLamSan,mau.chanDoan,
       CONVERT(DATE,SYSDATETIME()),mau.lyDo,'DA_KHAM'
FROM @PhieuDemo mau
WHERE NOT EXISTS (SELECT 1 FROM dbo.PhieuKham pk WHERE pk.maPhieuKham=mau.maPhieuKham);

DECLARE @DichVuDemo TABLE (
    maPhieuKham VARCHAR(20) NOT NULL, maDV VARCHAR(20) NOT NULL,
    viTriRang INT NOT NULL, ghiChu NVARCHAR(1000) NOT NULL
);
INSERT @DichVuDemo(maPhieuKham,maDV,viTriRang,ghiChu) VALUES
 ('PK-DEMO-001','DV001',0,N'Cạo vôi toàn hàm.'),
 ('PK-DEMO-001','DV002',16,N'Trám mặt nhai răng 16.'),
 ('PK-DEMO-002','DV001',0,N'Cạo vôi toàn hàm.'),
 ('PK-DEMO-002','DV002',26,N'Trám răng 26.'),
 ('PK-DEMO-003','DV002',11,N'Trám răng 11.'),
 ('PK-DEMO-003','DV002',21,N'Trám răng 21.');

INSERT dbo.ChiTietPhieuKham(maPhieuKham,maDV,viTriRang,donGiaApDung,ghiChu,trangThaiThucHien)
SELECT mau.maPhieuKham,mau.maDV,mau.viTriRang,dv.donGia,mau.ghiChu,'DA_THUC_HIEN'
FROM @DichVuDemo mau
JOIN dbo.DichVu dv ON dv.maDV=mau.maDV AND dv.dangApDung=1
WHERE EXISTS (SELECT 1 FROM dbo.PhieuKham pk
              WHERE pk.maPhieuKham=mau.maPhieuKham AND pk.trangThaiKham='DA_KHAM')
  AND NOT EXISTS (SELECT 1 FROM dbo.ChiTietPhieuKham ct
                  WHERE ct.maPhieuKham=mau.maPhieuKham AND ct.maDV=mau.maDV
                    AND ct.viTriRang=mau.viTriRang);
GO
COMMIT TRANSACTION;
GO
-- Khong seed TaiKhoan bang SQL plaintext. Chay cong cu PBKDF2 neu can tao tai khoan.
SELECT pk.maPhieuKham, bn.maBN, bn.hoTen, pk.ngayKham, pk.trangThaiKham,
       COUNT(ct.maCTPK) AS soDichVuDaThucHien,
       SUM(ct.donGiaApDung) AS tongTienDichVuDuKien
FROM dbo.PhieuKham pk
JOIN dbo.BenhNhan bn ON bn.maBN=pk.maBN
LEFT JOIN dbo.ChiTietPhieuKham ct ON ct.maPhieuKham=pk.maPhieuKham
WHERE pk.maPhieuKham IN ('PK-DEMO-001','PK-DEMO-002','PK-DEMO-003')
GROUP BY pk.maPhieuKham,bn.maBN,bn.hoTen,pk.ngayKham,pk.trangThaiKham
ORDER BY pk.maPhieuKham;
