/*
  Sprint 0 - initial SQL Server schema for Nhom19 Dental Clinic
  Derived and adapted from the previous Chapter 4 SQL design.
  15 business tables; X-ray functionality removed (2026-10-20).
  Appointment reason is free text; NO appointment-service FK.
  Multiple personnel MAY be assigned to the same room/shift.
  Note: execute with SSMS/SQLCMD (GO separators), on a NEW development database.
  Run only once. Not yet verified against a live SQL Server.
  Changes to an existing DB require reviewed incremental migration scripts.
  Work still required: end-to-end service transaction/concurrency tests and
  review consistency with final Java domain/Enum definitions.
*/
IF DB_ID(N'NhaKhoa_QuanLy') IS NULL
    EXEC(N'CREATE DATABASE [NhaKhoa_QuanLy]');
GO
USE [NhaKhoa_QuanLy];
GO

-- I. DANH MUC VA NHAN SU ---------------------------------------------------
CREATE TABLE dbo.PhongKham (
    maPhong           VARCHAR(20)      NOT NULL,
    tenPhong          NVARCHAR(100)    NOT NULL,
    trangThaiHoatDong BIT              NOT NULL CONSTRAINT DF_PhongKham_HoatDong DEFAULT (1),
    CONSTRAINT PK_PhongKham PRIMARY KEY (maPhong),
    CONSTRAINT UQ_PhongKham_Ten UNIQUE (tenPhong),
    CONSTRAINT CK_PhongKham_Ten CHECK (LEN(LTRIM(RTRIM(tenPhong))) > 0)
);
GO
CREATE TABLE dbo.NhanVien (
    maNV               VARCHAR(20)     NOT NULL,
    hoTen              NVARCHAR(120)   NOT NULL,
    gioiTinh           NVARCHAR(20)    NOT NULL,
    ngaySinh           DATE            NOT NULL,
    soDienThoai        VARCHAR(15)     NOT NULL,
    diaChi             NVARCHAR(255)   NULL,
    chucVu             VARCHAR(20)     NOT NULL,
    trangThaiLamViec   BIT             NOT NULL CONSTRAINT DF_NhanVien_HoatDong DEFAULT (1),
    email              VARCHAR(254)    NULL,
    CONSTRAINT PK_NhanVien PRIMARY KEY (maNV),
    CONSTRAINT CK_NhanVien_ChucVu CHECK (chucVu IN ('LE_TAN','NHA_SI','Y_TA','NHAN_VIEN_QUAN_LY')),
    CONSTRAINT CK_NhanVien_SDT CHECK (LEN(soDienThoai) BETWEEN 10 AND 11 AND soDienThoai NOT LIKE '%[^0-9]%'),
    CONSTRAINT CK_NhanVien_NgaySinh CHECK (ngaySinh >= '19000101'),
    CONSTRAINT CK_NhanVien_HoTen CHECK (LEN(LTRIM(RTRIM(hoTen))) > 0)
);
GO
CREATE UNIQUE INDEX UX_NhanVien_Email ON dbo.NhanVien(email) WHERE email IS NOT NULL;
GO
CREATE TABLE dbo.TaiKhoan (
    maTaiKhoan        VARCHAR(20)     NOT NULL,
    maNV              VARCHAR(20)     NOT NULL,
    tenDangNhap       VARCHAR(60)     NOT NULL,
    matKhau           VARCHAR(255)    NOT NULL, -- chi luu hash, khong luu mat khau plaintext
    trangThaiHoatDong BIT            NOT NULL CONSTRAINT DF_TaiKhoan_HoatDong DEFAULT (1),
    CONSTRAINT PK_TaiKhoan PRIMARY KEY (maTaiKhoan),
    CONSTRAINT UQ_TaiKhoan_MaNV UNIQUE (maNV),
    CONSTRAINT UQ_TaiKhoan_TenDangNhap UNIQUE (tenDangNhap),
    CONSTRAINT FK_TaiKhoan_NhanVien FOREIGN KEY (maNV) REFERENCES dbo.NhanVien(maNV),
    CONSTRAINT CK_TaiKhoan_TenDangNhap CHECK (LEN(LTRIM(RTRIM(tenDangNhap))) >= 3 AND tenDangNhap NOT LIKE '% %'),
    CONSTRAINT CK_TaiKhoan_MatKhau CHECK (LEN(matKhau) >= 40)
);
GO
CREATE TABLE dbo.PhanCong (
    maPC               VARCHAR(20)    NOT NULL,
    maNV               VARCHAR(20)    NOT NULL,
    maPhong            VARCHAR(20)    NULL,
    ngayLam            DATE           NOT NULL,
    gioBatDau          TIME(0)        NOT NULL,
    gioKetThuc         TIME(0)        NOT NULL,
    caLam              VARCHAR(10)    NOT NULL,
    noiDungCongViec    NVARCHAR(300)  NULL,
    trangThai          VARCHAR(20)    NOT NULL CONSTRAINT DF_PhanCong_TrangThai DEFAULT ('DA_PHAN_CONG'),
    CONSTRAINT PK_PhanCong PRIMARY KEY (maPC),
    CONSTRAINT FK_PhanCong_NhanVien FOREIGN KEY (maNV) REFERENCES dbo.NhanVien(maNV),
    CONSTRAINT FK_PhanCong_PhongKham FOREIGN KEY (maPhong) REFERENCES dbo.PhongKham(maPhong),
    CONSTRAINT CK_PhanCong_Gio CHECK (gioBatDau < gioKetThuc),
    CONSTRAINT CK_PhanCong_CaLam CHECK (caLam IN ('SANG','CHIEU')),
    CONSTRAINT CK_PhanCong_TrangThai CHECK (trangThai IN ('DA_PHAN_CONG','DA_HUY'))
);
GO
-- Mot nhan vien khong co hai phan cong dang hieu luc trong cung mot ca/ngay.
-- Khong UNIQUE maPhong/ngay/ca: mot phong co the co ca nha si va y ta.
CREATE UNIQUE INDEX UX_PhanCong_NhanVien_Ngay_Ca_Active
ON dbo.PhanCong(maNV, ngayLam, caLam) WHERE trangThai = 'DA_PHAN_CONG';
GO

-- II. BENH NHAN, HO SO BENH AN, LICH HEN -----------------------------------
CREATE TABLE dbo.BenhNhan (
    maBN          VARCHAR(20)     NOT NULL,
    hoTen         NVARCHAR(120)   NOT NULL,
    gioiTinh      BIT             NOT NULL,
    ngaySinh      DATE            NOT NULL,
    soDienThoai   VARCHAR(15)     NOT NULL,
    diaChi        NVARCHAR(255)   NULL,
    CONSTRAINT PK_BenhNhan PRIMARY KEY (maBN),
    CONSTRAINT CK_BenhNhan_Ten CHECK (LEN(LTRIM(RTRIM(hoTen))) > 0),
    CONSTRAINT CK_BenhNhan_SDT CHECK (LEN(soDienThoai) BETWEEN 10 AND 11 AND soDienThoai NOT LIKE '%[^0-9]%'),
    CONSTRAINT CK_BenhNhan_NgaySinh CHECK (ngaySinh >= '19000101')
);
GO
CREATE TABLE dbo.HoSoBenhAn (
    maHSBA               VARCHAR(20)     NOT NULL,
    maBN                 VARCHAR(20)     NOT NULL,
    ngayTao              DATETIME2(0)    NOT NULL CONSTRAINT DF_HSBA_NgayTao DEFAULT (SYSDATETIME()),
    ngayCapNhatGanNhat   DATETIME2(0)    NULL,
    tienSuBenh           NVARCHAR(1000)  NULL,
    tienSuDiUng          NVARCHAR(1000)  NULL,
    ghiChuCanhBao        NVARCHAR(1000)  NULL,
    CONSTRAINT PK_HoSoBenhAn PRIMARY KEY (maHSBA),
    CONSTRAINT UQ_HoSoBenhAn_MaBN UNIQUE (maBN),
    CONSTRAINT FK_HoSoBenhAn_BenhNhan FOREIGN KEY (maBN) REFERENCES dbo.BenhNhan(maBN),
    CONSTRAINT CK_HoSoBenhAn_CapNhat CHECK (ngayCapNhatGanNhat IS NULL OR ngayCapNhatGanNhat >= ngayTao)
);
GO
CREATE TABLE dbo.LichHen (
    maLH          VARCHAR(20)     NOT NULL,
    maBN          VARCHAR(20)     NOT NULL,
    maNVNhaSi     VARCHAR(20)     NOT NULL,
    ngayHen       DATE            NOT NULL,
    gioBatDau     TIME(0)         NOT NULL,
    gioKetThuc    TIME(0)         NOT NULL,
    lyDoKham      NVARCHAR(500)   NULL,
    ghiChu        NVARCHAR(1000)  NULL,
    trangThai     VARCHAR(25)     NOT NULL CONSTRAINT DF_LichHen_TrangThai DEFAULT ('CHO_XAC_NHAN'),
    CONSTRAINT PK_LichHen PRIMARY KEY (maLH),
    CONSTRAINT FK_LichHen_BenhNhan FOREIGN KEY (maBN) REFERENCES dbo.BenhNhan(maBN),
    CONSTRAINT FK_LichHen_NhaSi FOREIGN KEY (maNVNhaSi) REFERENCES dbo.NhanVien(maNV),
    CONSTRAINT CK_LichHen_Gio CHECK (gioBatDau < gioKetThuc),
    CONSTRAINT CK_LichHen_TrangThai CHECK (trangThai IN ('CHO_XAC_NHAN','DA_XAC_NHAN','DA_DEN','DA_HUY'))
);
GO
CREATE INDEX IX_LichHen_NhaSi_Ngay ON dbo.LichHen(maNVNhaSi, ngayHen, gioBatDau);
GO
CREATE INDEX IX_LichHen_BenhNhan ON dbo.LichHen(maBN, ngayHen);
GO

-- III. DICH VU VA KHAM ---------------------------------------------------
CREATE TABLE dbo.DichVu (
    maDV                VARCHAR(20)     NOT NULL,
    tenDV               NVARCHAR(150)   NOT NULL,
    donViTinh           NVARCHAR(30)    NOT NULL,
    donGia              DECIMAL(18,2)   NOT NULL,
    thoiGianThucHien    INT             NULL,
    moTa                NVARCHAR(1000)  NULL,
    dangApDung          BIT             NOT NULL CONSTRAINT DF_DichVu_ApDung DEFAULT (1),
    CONSTRAINT PK_DichVu PRIMARY KEY (maDV),
    CONSTRAINT CK_DichVu_Gia CHECK (donGia >= 0),
    CONSTRAINT CK_DichVu_ThoiGian CHECK (thoiGianThucHien IS NULL OR thoiGianThucHien > 0),
    CONSTRAINT CK_DichVu_Ten CHECK (LEN(LTRIM(RTRIM(tenDV))) > 0)
);
GO
CREATE TABLE dbo.PhieuKham (
    maPhieuKham         VARCHAR(20)     NOT NULL,
    maBN                VARCHAR(20)     NOT NULL,
    maNVNhaSi           VARCHAR(20)     NOT NULL,
    maNVLeTan           VARCHAR(20)     NOT NULL,
    maLH                VARCHAR(20)     NULL, -- NULL: khach khong hen
    maPhong             VARCHAR(20)     NULL,
    thoiDiemTiepNhan    DATETIME2(0)    NOT NULL CONSTRAINT DF_PhieuKham_TiepNhan DEFAULT (SYSDATETIME()),
    tinhTrangLamSan     NVARCHAR(1000)  NULL,
    chanDoan            NVARCHAR(1000)  NULL,
    ngayKham            DATE            NULL,
    lyDo                NVARCHAR(500)   NULL,
    ghiChu              NVARCHAR(1000)  NULL,
    trangThaiKham       VARCHAR(20)     NOT NULL CONSTRAINT DF_PhieuKham_TrangThai DEFAULT ('CHO_KHAM'),
    CONSTRAINT PK_PhieuKham PRIMARY KEY (maPhieuKham),
    CONSTRAINT FK_PhieuKham_BenhNhan FOREIGN KEY (maBN) REFERENCES dbo.BenhNhan(maBN),
    CONSTRAINT FK_PhieuKham_NhaSi FOREIGN KEY (maNVNhaSi) REFERENCES dbo.NhanVien(maNV),
    CONSTRAINT FK_PhieuKham_LeTan FOREIGN KEY (maNVLeTan) REFERENCES dbo.NhanVien(maNV),
    CONSTRAINT FK_PhieuKham_LichHen FOREIGN KEY (maLH) REFERENCES dbo.LichHen(maLH),
    CONSTRAINT FK_PhieuKham_PhongKham FOREIGN KEY (maPhong) REFERENCES dbo.PhongKham(maPhong),
    CONSTRAINT CK_PhieuKham_TrangThai CHECK (trangThaiKham IN ('CHO_KHAM','DA_KHAM','DANG_KHAM')),
    CONSTRAINT CK_PhieuKham_DaKham CHECK (trangThaiKham <> 'DA_KHAM' OR (ngayKham IS NOT NULL AND NULLIF(LTRIM(RTRIM(chanDoan)), N'') IS NOT NULL))
);
GO
CREATE UNIQUE INDEX UX_PhieuKham_LichHen ON dbo.PhieuKham(maLH) WHERE maLH IS NOT NULL;
GO
CREATE INDEX IX_PhieuKham_NhaSi_ChoKham ON dbo.PhieuKham(maNVNhaSi, trangThaiKham, thoiDiemTiepNhan);
GO
CREATE INDEX IX_PhieuKham_BenhNhan ON dbo.PhieuKham(maBN, thoiDiemTiepNhan);
GO
-- maCTPK va trangThaiThucHien: hai truong bo sung vat ly, can dong bo vao UML.
CREATE TABLE dbo.ChiTietPhieuKham (
    maCTPK              BIGINT          IDENTITY(1,1) NOT NULL,
    maPhieuKham         VARCHAR(20)     NOT NULL,
    maDV                VARCHAR(20)     NOT NULL,
    viTriRang           INT             NOT NULL CONSTRAINT DF_CTPK_ViTriRang DEFAULT (0),
    donGiaApDung        DECIMAL(18,2)   NOT NULL,
    ghiChu              NVARCHAR(1000)  NULL,
    trangThaiThucHien   VARCHAR(20)     NOT NULL CONSTRAINT DF_CTPK_TrangThai DEFAULT ('CHUA_THUC_HIEN'),
    CONSTRAINT PK_ChiTietPhieuKham PRIMARY KEY (maCTPK),
    CONSTRAINT FK_CTPK_PhieuKham FOREIGN KEY (maPhieuKham) REFERENCES dbo.PhieuKham(maPhieuKham),
    CONSTRAINT FK_CTPK_DichVu FOREIGN KEY (maDV) REFERENCES dbo.DichVu(maDV),
    CONSTRAINT CK_CTPK_Gia CHECK (donGiaApDung >= 0),
    CONSTRAINT CK_CTPK_ViTriRang CHECK (viTriRang = 0 OR ((viTriRang / 10) BETWEEN 1 AND 4 AND (viTriRang % 10) BETWEEN 1 AND 8)),
    CONSTRAINT CK_CTPK_TrangThai CHECK (trangThaiThucHien IN ('CHUA_THUC_HIEN','DA_THUC_HIEN'))
);
GO
CREATE INDEX IX_CTPK_PhieuKham ON dbo.ChiTietPhieuKham(maPhieuKham);
GO

-- IV. DON THUOC ------------------------------------------------------------
CREATE TABLE dbo.DonThuoc (
    maDonThuoc     VARCHAR(20)      NOT NULL,
    maPhieuKham    VARCHAR(20)      NOT NULL,
    ngayKeDon      DATE             NOT NULL CONSTRAINT DF_DonThuoc_NgayKe DEFAULT (CONVERT(date,SYSDATETIME())),
    loiDan         NVARCHAR(1000)   NULL,
    ngayKetDon     NVARCHAR(60)     NULL, -- ten/loai tu so do; can lam ro y nghia
    ghiChu         NVARCHAR(1000)   NULL,
    CONSTRAINT PK_DonThuoc PRIMARY KEY (maDonThuoc),
    CONSTRAINT UQ_DonThuoc_PhieuKham UNIQUE (maPhieuKham),
    CONSTRAINT FK_DonThuoc_PhieuKham FOREIGN KEY (maPhieuKham) REFERENCES dbo.PhieuKham(maPhieuKham)
);
GO
CREATE TABLE dbo.ChiTietDonThuoc (
    maCTDT       VARCHAR(20)     NOT NULL,
    maDonThuoc   VARCHAR(20)     NOT NULL,
    tenThuoc     NVARCHAR(150)   NOT NULL,
    hamLuong     NVARCHAR(60)    NULL,
    soLuong      INT             NOT NULL,
    donViTinh    NVARCHAR(50)    NOT NULL,
    lieuDung     NVARCHAR(200)   NOT NULL,
    cachDung     NVARCHAR(500)   NOT NULL,
    CONSTRAINT PK_ChiTietDonThuoc PRIMARY KEY (maCTDT),
    CONSTRAINT FK_CTDT_DonThuoc FOREIGN KEY (maDonThuoc) REFERENCES dbo.DonThuoc(maDonThuoc),
    CONSTRAINT CK_CTDT_SoLuong CHECK (soLuong > 0),
    CONSTRAINT CK_CTDT_TenThuoc CHECK (LEN(LTRIM(RTRIM(tenThuoc))) > 0)
);
GO

-- V. HOA DON VA THANH TOAN -------------------------------------------------
CREATE TABLE dbo.HoaDon (
    maHD                VARCHAR(20)    NOT NULL,
    maPhieuKham         VARCHAR(20)    NOT NULL,
    maBN                VARCHAR(20)    NOT NULL, -- quan he BenhNhan - HoaDon truc tiep
    maNVLap             VARCHAR(20)    NOT NULL,
    ngayLap             DATETIME2(0)   NOT NULL CONSTRAINT DF_HoaDon_NgayLap DEFAULT (SYSDATETIME()),
    ghiChu              NVARCHAR(1000) NULL,
    trangThaiHoaDon     VARCHAR(20)    NOT NULL CONSTRAINT DF_HoaDon_TrangThai DEFAULT ('CHO_THANH_TOAN'),
    CONSTRAINT PK_HoaDon PRIMARY KEY (maHD),
    CONSTRAINT UQ_HoaDon_PhieuKham UNIQUE (maPhieuKham),
    CONSTRAINT FK_HoaDon_PhieuKham FOREIGN KEY (maPhieuKham) REFERENCES dbo.PhieuKham(maPhieuKham),
    CONSTRAINT FK_HoaDon_BenhNhan FOREIGN KEY (maBN) REFERENCES dbo.BenhNhan(maBN),
    CONSTRAINT FK_HoaDon_NVLap FOREIGN KEY (maNVLap) REFERENCES dbo.NhanVien(maNV),
    CONSTRAINT CK_HoaDon_TrangThai CHECK (trangThaiHoaDon IN ('CHO_THANH_TOAN','DA_THANH_TOAN'))
);
GO
CREATE TABLE dbo.ChiTietHoaDon (
    maCTHD          VARCHAR(20)     NOT NULL,
    maHD            VARCHAR(20)     NOT NULL,
    maCTPK          BIGINT          NOT NULL, -- moi dich vu da thuc hien chi duoc tinh tien 1 lan
    tenDichVu       NVARCHAR(150)   NOT NULL,
    viTriRang       NVARCHAR(20)    NULL,
    soLuong         INT             NOT NULL CONSTRAINT DF_CTHD_SoLuong DEFAULT (1),
    donGiaApDung    DECIMAL(18,2)   NOT NULL,
    thanhTien       AS (CONVERT(DECIMAL(18,2), soLuong * donGiaApDung)) PERSISTED,
    CONSTRAINT PK_ChiTietHoaDon PRIMARY KEY (maCTHD),
    CONSTRAINT UQ_ChiTietHoaDon_CTPK UNIQUE (maCTPK),
    CONSTRAINT FK_CTHD_HoaDon FOREIGN KEY (maHD) REFERENCES dbo.HoaDon(maHD),
    CONSTRAINT FK_CTHD_CTPK FOREIGN KEY (maCTPK) REFERENCES dbo.ChiTietPhieuKham(maCTPK),
    CONSTRAINT CK_CTHD_SoLuong CHECK (soLuong > 0),
    CONSTRAINT CK_CTHD_DonGia CHECK (donGiaApDung >= 0),
    CONSTRAINT CK_CTHD_Ten CHECK (LEN(LTRIM(RTRIM(tenDichVu))) > 0)
);
GO
CREATE INDEX IX_CTHD_HoaDon ON dbo.ChiTietHoaDon(maHD);
GO
CREATE TABLE dbo.ThanhToan (
    maThanhToan           VARCHAR(20)     NOT NULL,
    maHD                  VARCHAR(20)     NOT NULL,
    maNVXacNhan           VARCHAR(20)     NOT NULL,
    ngayThanhToan         DATETIME2(0)    NULL,
    phuongThucThanhToan   VARCHAR(20)     NOT NULL,
    maGiaoDich            NVARCHAR(100)   NULL,
    trangThaiThanhToan    VARCHAR(20)     NOT NULL CONSTRAINT DF_ThanhToan_TrangThai DEFAULT ('CHO_THANH_TOAN'),
    soTien                DECIMAL(18,2)   NOT NULL,
    CONSTRAINT PK_ThanhToan PRIMARY KEY (maThanhToan),
    CONSTRAINT UQ_ThanhToan_HoaDon UNIQUE (maHD),
    CONSTRAINT FK_ThanhToan_HoaDon FOREIGN KEY (maHD) REFERENCES dbo.HoaDon(maHD),
    CONSTRAINT FK_ThanhToan_NhanVien FOREIGN KEY (maNVXacNhan) REFERENCES dbo.NhanVien(maNV),
    CONSTRAINT CK_ThanhToan_PhuongThuc CHECK (phuongThucThanhToan IN ('TIEN_MAT','CHUYEN_KHOAN')),
    CONSTRAINT CK_ThanhToan_TrangThai CHECK (trangThaiThanhToan IN ('CHO_THANH_TOAN','DA_THANH_TOAN')),
    CONSTRAINT CK_ThanhToan_SoTien CHECK (soTien > 0),
    CONSTRAINT CK_ThanhToan_Ngay CHECK (trangThaiThanhToan <> 'DA_THANH_TOAN' OR ngayThanhToan IS NOT NULL)
);
GO
CREATE UNIQUE INDEX UX_ThanhToan_MaGiaoDich
ON dbo.ThanhToan(maGiaoDich)
WHERE maGiaoDich IS NOT NULL AND phuongThucThanhToan = 'CHUYEN_KHOAN';
GO

-- VI. VIEW CHO THU NGAN, BAO CAO -----------------------------------------
-- Tong tien la thuoc tinh dan xuat, KHONG luu Double trong HoaDon.
CREATE OR ALTER VIEW dbo.v_HoaDonTongTien AS
SELECT h.maHD, h.maBN, h.maPhieuKham, h.ngayLap, h.trangThaiHoaDon,
       CAST(COALESCE(SUM(ct.thanhTien),0) AS DECIMAL(18,2)) AS tongTien
FROM dbo.HoaDon AS h
LEFT JOIN dbo.ChiTietHoaDon AS ct ON ct.maHD = h.maHD
GROUP BY h.maHD, h.maBN, h.maPhieuKham, h.ngayLap, h.trangThaiHoaDon;
GO
CREATE OR ALTER VIEW dbo.v_LichLamViec AS
SELECT pc.maPC, pc.maNV, nv.hoTen, nv.chucVu,
       pc.ngayLam, pc.caLam, pc.gioBatDau, pc.gioKetThuc,
       pc.maPhong, pk.tenPhong, pc.noiDungCongViec, pc.trangThai
FROM dbo.PhanCong AS pc
JOIN dbo.NhanVien AS nv ON nv.maNV = pc.maNV
LEFT JOIN dbo.PhongKham AS pk ON pk.maPhong = pc.maPhong;
GO
CREATE OR ALTER VIEW dbo.v_BaoCaoDoanhThu AS
SELECT CONVERT(date,t.ngayThanhToan) AS ngay,
       COUNT_BIG(*) AS soGiaoDich,
       CAST(SUM(t.soTien) AS DECIMAL(18,2)) AS doanhThu
FROM dbo.ThanhToan t
WHERE t.trangThaiThanhToan = 'DA_THANH_TOAN'
GROUP BY CONVERT(date,t.ngayThanhToan);
GO
CREATE OR ALTER VIEW dbo.v_BaoCaoLuotKham AS
SELECT pk.ngayKham, COUNT_BIG(*) AS soLuotKham
FROM dbo.PhieuKham pk WHERE pk.trangThaiKham = 'DA_KHAM'
GROUP BY pk.ngayKham;
GO
CREATE OR ALTER VIEW dbo.v_BaoCaoDichVu AS
SELECT dv.maDV, dv.tenDV, COUNT_BIG(*) AS soLuotThucHien,
       CAST(COALESCE(SUM(ct.donGiaApDung),0) AS DECIMAL(18,2)) AS tongGiaTri
FROM dbo.ChiTietPhieuKham ct
JOIN dbo.DichVu dv ON dv.maDV = ct.maDV
WHERE ct.trangThaiThucHien = 'DA_THUC_HIEN'
GROUP BY dv.maDV, dv.tenDV;
GO

-- VII. RANG BUOC LIEN BANG BANG TRIGGER ----------------------------------
-- Khong cho phep hai phan cong hieu luc bi chong khoang gio cung ngay / nhan vien.
CREATE OR ALTER TRIGGER dbo.trg_PhanCong_KiemTraTrungGio
ON dbo.PhanCong AFTER INSERT, UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1 FROM inserted i
        JOIN dbo.PhanCong pc ON pc.maNV=i.maNV AND pc.ngayLam=i.ngayLam
           AND pc.maPC<>i.maPC AND pc.trangThai='DA_PHAN_CONG'
        WHERE i.trangThai='DA_PHAN_CONG'
          AND i.gioBatDau<pc.gioKetThuc AND pc.gioBatDau<i.gioKetThuc)
        THROW 51014, N'Nhan vien khong duoc co hai phan cong trung gio.', 1;
END;
GO

-- Ngay sinh phai khong o tuong lai; nhan vien toi thieu 18 tuoi.
CREATE OR ALTER TRIGGER dbo.trg_NhanVien_KiemTraNgaySinh
ON dbo.NhanVien AFTER INSERT, UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM inserted i
               WHERE DATEADD(YEAR,18,i.ngaySinh) > CONVERT(date,SYSDATETIME()))
       THROW 51015, N'Nhan vien phai du 18 tuoi.', 1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_BenhNhan_KiemTraNgaySinh
ON dbo.BenhNhan AFTER INSERT, UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM inserted i WHERE i.ngaySinh > CONVERT(date,SYSDATETIME()))
       THROW 51016, N'Ngay sinh benh nhan khong duoc o tuong lai.', 1;
END;
GO
-- Valid roles, make sure a schedule exists and dentist not doubly booked.
CREATE OR ALTER TRIGGER dbo.trg_LichHen_KiemTra
ON dbo.LichHen AFTER INSERT, UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM inserted i JOIN dbo.NhanVien nv ON nv.maNV=i.maNVNhaSi
               WHERE nv.chucVu <> 'NHA_SI' OR nv.trangThaiLamViec=0)
        THROW 51001, N'Lich hen phai duoc phan cho nha si dang lam viec.', 1;

    IF EXISTS (
        SELECT 1 FROM inserted i
        JOIN dbo.LichHen other ON other.maNVNhaSi=i.maNVNhaSi
             AND other.ngayHen=i.ngayHen AND other.maLH<>i.maLH
             AND other.trangThai<>'DA_HUY' AND i.trangThai<>'DA_HUY'
             AND i.gioBatDau<other.gioKetThuc AND other.gioBatDau<i.gioKetThuc)
        THROW 51002, N'Nha si co lich hen trung gio.', 1;
    IF EXISTS (
        SELECT 1 FROM inserted i
        WHERE i.trangThai<>'DA_HUY' AND NOT EXISTS (
            SELECT 1 FROM dbo.PhanCong pc
            WHERE pc.maNV=i.maNVNhaSi AND pc.ngayLam=i.ngayHen
              AND pc.trangThai='DA_PHAN_CONG'
              AND pc.gioBatDau<=i.gioBatDau AND pc.gioKetThuc>=i.gioKetThuc))
        THROW 51003, N'Lich hen nam ngoai ca phan cong cua nha si.', 1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_PhieuKham_KiemTra
ON dbo.PhieuKham AFTER INSERT, UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM inserted i JOIN dbo.NhanVien nv ON nv.maNV=i.maNVNhaSi
               WHERE nv.chucVu<>'NHA_SI')
        THROW 51004, N'Nguoi phu trach phieu kham phai la nha si.', 1;
    IF EXISTS (SELECT 1 FROM inserted i JOIN dbo.NhanVien nv ON nv.maNV=i.maNVLeTan
               WHERE nv.chucVu<>'LE_TAN')
        THROW 51005, N'Nguoi tiep nhan phai la le tan.', 1;
    IF EXISTS (
        SELECT 1 FROM inserted i JOIN dbo.LichHen lh ON lh.maLH=i.maLH
        WHERE lh.maBN<>i.maBN OR lh.maNVNhaSi<>i.maNVNhaSi)
        THROW 51006, N'Lich hen va phieu kham phai cung benh nhan/nha si.', 1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_HoaDon_KiemTra
ON dbo.HoaDon AFTER INSERT, UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1 FROM inserted i JOIN dbo.PhieuKham pk ON pk.maPhieuKham=i.maPhieuKham
        WHERE pk.maBN<>i.maBN OR pk.trangThaiKham<>'DA_KHAM')
        THROW 51007, N'Chi lap hoa don cho phieu kham hoan tat va dung benh nhan.', 1;
    IF EXISTS (SELECT 1 FROM inserted i JOIN dbo.NhanVien nv ON nv.maNV=i.maNVLap
               WHERE nv.chucVu<>'LE_TAN')
        THROW 51008, N'Nguoi lap hoa don phai la le tan.', 1;
    -- Trạng thái phải phản ánh khoản đã nhận, trừ thao tác do trigger ThanhToan.
    IF EXISTS (
        SELECT 1 FROM inserted i
        WHERE i.trangThaiHoaDon='DA_THANH_TOAN' AND NOT EXISTS (
           SELECT 1 FROM dbo.ThanhToan t
           WHERE t.maHD=i.maHD AND t.trangThaiThanhToan='DA_THANH_TOAN'))
        THROW 51009, N'Hoa don khong duoc DA_THANH_TOAN neu chua co thanh toan thanh cong.', 1;
END;
GO
-- Khoa gia va noi dung dich vu da chuyen vao chi tiet hoa don.
CREATE OR ALTER TRIGGER dbo.trg_CTPK_KhoaSauLapHoaDon
ON dbo.ChiTietPhieuKham AFTER UPDATE, DELETE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM deleted d
               JOIN dbo.ChiTietHoaDon hd ON hd.maCTPK=d.maCTPK)
      THROW 51017, N'Dich vu da lap hoa don khong duoc sua/xoa truc tiep.', 1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_ChiTietHoaDon_KiemTra
ON dbo.ChiTietHoaDon AFTER INSERT, UPDATE, DELETE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1 FROM inserted i
        JOIN dbo.HoaDon hd ON hd.maHD=i.maHD
        JOIN dbo.ChiTietPhieuKham ct ON ct.maCTPK=i.maCTPK
        WHERE ct.maPhieuKham<>hd.maPhieuKham OR ct.trangThaiThucHien<>'DA_THUC_HIEN'
            OR ct.donGiaApDung<>i.donGiaApDung OR i.soLuong<>1)
        THROW 51010, N'Chi tiet hoa don phai la dich vu da thuc hien cua cung phieu kham, so luong 1, dung don gia.', 1;
    IF EXISTS (
        SELECT 1 FROM (SELECT maHD FROM inserted UNION SELECT maHD FROM deleted) x
        JOIN dbo.HoaDon h ON h.maHD=x.maHD
        WHERE h.trangThaiHoaDon='DA_THANH_TOAN')
        THROW 51011, N'Khong duoc sua chi tiet hoa don da thanh toan.', 1;
END;
GO
CREATE OR ALTER TRIGGER dbo.trg_ThanhToan_KiemTraVaDongBo
ON dbo.ThanhToan AFTER INSERT, UPDATE, DELETE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM inserted i JOIN dbo.NhanVien nv ON nv.maNV=i.maNVXacNhan
               WHERE nv.chucVu <> 'LE_TAN')
        THROW 51012, N'Chi le tan duoc xac nhan thanh toan.', 1;
    IF EXISTS (
        SELECT 1 FROM inserted i
        JOIN dbo.v_HoaDonTongTien v ON v.maHD=i.maHD
        WHERE i.trangThaiThanhToan='DA_THANH_TOAN'
          AND (v.tongTien<=0 OR i.soTien<>v.tongTien))
        THROW 51013, N'Thanh toan thanh cong phai thu du mot lan va dung tong tien hoa don.', 1;

    -- Dong bo trang thai hoa don theo giao dich da xac nhan.
    UPDATE hd
    SET trangThaiHoaDon = CASE WHEN EXISTS (
       SELECT 1 FROM dbo.ThanhToan t
       WHERE t.maHD=hd.maHD AND t.trangThaiThanhToan='DA_THANH_TOAN')
       THEN 'DA_THANH_TOAN' ELSE 'CHO_THANH_TOAN' END
    FROM dbo.HoaDon hd
    JOIN (SELECT maHD FROM inserted UNION SELECT maHD FROM deleted) changed
       ON changed.maHD=hd.maHD;
END;
GO

-- VIII. VI DU THAO TAC AN TOAN ---------------------------------------------
-- Khi tiep nhan khach khong hen: INSERT PhieuKham(maLH=NULL) sau khi da co
-- BenhNhan, HoSoBenhAn, PhanCong nha si phu hop.
-- Khi kham: tao ChiTietPhieuKham, sau do chuyen trangThaiThucHien = DA_THUC_HIEN.
-- Khi lap hoa don: INSERT HoaDon, sau do INSERT ChiTietHoaDon SELECT tu
-- ChiTietPhieuKham trangThaiThucHien = DA_THUC_HIEN.
-- Khi thu ngan kiem tra tien mat/sao ke chuyen khoan xong:
--   INSERT ThanhToan (..., trangThaiThanhToan='DA_THANH_TOAN',
--       soTien = tongTien tu v_HoaDonTongTien, ngayThanhToan=SYSDATETIME()).
--   Trigger kiem tra du tien va dong bo HoaDon trong cung giao dich.
-- Gia ca duoc lay tu DichVu khi tao ChiTietPhieuKham; khong tinh lai gia cu.
-- Khong co tien thuoc trong ChiTietHoaDon hay ThanhToan.

-- IX. MOT SO TRUY VAN KIEM TRA ---------------------------------------------
-- SELECT * FROM dbo.v_LichLamViec WHERE maNV = 'NV001';
-- SELECT * FROM dbo.v_HoaDonTongTien WHERE maBN = 'BN001';
-- SELECT * FROM dbo.v_BaoCaoDoanhThu ORDER BY ngay DESC;
-- SELECT * FROM dbo.v_BaoCaoLuotKham ORDER BY ngayKham DESC;
-- SELECT * FROM dbo.v_BaoCaoDichVu ORDER BY soLuotThucHien DESC;
