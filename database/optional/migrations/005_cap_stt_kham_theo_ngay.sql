/* Bo sung STT kham theo ngay cho database da co.
   Phieu cu duoc giu nguyen; bo dem se dong bo voi STT da co neu tung duoc gan.
   Khong chay neu database da co hai cot nay voi kieu khac du kien. */
USE [DENTAFLOW_DB];
GO
SET XACT_ABORT ON;
IF DB_NAME() <> 'DENTAFLOW_DB' THROW 51501, N'Sai database.', 1;
IF OBJECT_ID(N'dbo.PhieuKham',N'U') IS NULL THROW 51502, N'Thieu bang PhieuKham.', 1;

BEGIN TRY
    BEGIN TRANSACTION;

    IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID(N'dbo.PhieuKham') AND name='ngayCapSo'
               AND (TYPE_NAME(user_type_id)<>'date' OR is_nullable<>1))
        THROW 51503, N'PhieuKham.ngayCapSo da ton tai nhung khong dung kieu DATE NULL.', 1;
    IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID(N'dbo.PhieuKham') AND name='soThuTuKham'
               AND (TYPE_NAME(user_type_id)<>'int' OR is_nullable<>1))
        THROW 51504, N'PhieuKham.soThuTuKham da ton tai nhung khong dung kieu INT NULL.', 1;

    IF COL_LENGTH('dbo.PhieuKham','ngayCapSo') IS NULL
        ALTER TABLE dbo.PhieuKham ADD ngayCapSo DATE NULL;
    IF COL_LENGTH('dbo.PhieuKham','soThuTuKham') IS NULL
        ALTER TABLE dbo.PhieuKham ADD soThuTuKham INT NULL;

    IF OBJECT_ID(N'dbo.CK_PhieuKham_STT',N'C') IS NULL
        ALTER TABLE dbo.PhieuKham WITH CHECK ADD CONSTRAINT CK_PhieuKham_STT
        CHECK ((ngayCapSo IS NULL AND soThuTuKham IS NULL) OR (ngayCapSo IS NOT NULL AND soThuTuKham IS NOT NULL AND soThuTuKham > 0));
    ALTER TABLE dbo.PhieuKham WITH CHECK CHECK CONSTRAINT CK_PhieuKham_STT;

    IF EXISTS (SELECT 1 FROM dbo.PhieuKham WHERE soThuTuKham IS NOT NULL GROUP BY ngayCapSo,soThuTuKham HAVING COUNT_BIG(*)>1)
        THROW 51505, N'Du lieu STT hien co bi trung; migration dung lai, khong thay doi du lieu.', 1;
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.PhieuKham') AND name=N'UX_PhieuKham_STT_Ngay')
        CREATE UNIQUE INDEX UX_PhieuKham_STT_Ngay ON dbo.PhieuKham(ngayCapSo,soThuTuKham) WHERE soThuTuKham IS NOT NULL;

    IF OBJECT_ID(N'dbo.SoThuTuKhamNgay',N'U') IS NULL
        CREATE TABLE dbo.SoThuTuKhamNgay (
            ngayCapSo DATE NOT NULL CONSTRAINT PK_SoThuTuKhamNgay PRIMARY KEY,
            soCuoi INT NOT NULL,
            CONSTRAINT CK_SoThuTuKhamNgay_So CHECK (soCuoi >= 0)
        );
    ELSE IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID(N'dbo.SoThuTuKhamNgay') AND name='ngayCapSo'
                        AND TYPE_NAME(user_type_id)='date' AND is_nullable=0)
         OR NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID(N'dbo.SoThuTuKhamNgay') AND name='soCuoi'
                        AND TYPE_NAME(user_type_id)='int' AND is_nullable=0)
        THROW 51506, N'Bang SoThuTuKhamNgay da ton tai voi cau truc khong dung; can kiem tra thu cong.', 1;

    -- Bảo đảm bộ đếm không lùi sau các STT đã ghi trước đó. Không xóa hay đổi phiếu cũ.
    ;WITH STTTheoNgay AS (
        SELECT ngayCapSo, MAX(soThuTuKham) AS soCuoi
        FROM dbo.PhieuKham
        WHERE ngayCapSo IS NOT NULL AND soThuTuKham IS NOT NULL
        GROUP BY ngayCapSo
    )
    UPDATE dem WITH (UPDLOCK,HOLDLOCK)
       SET dem.soCuoi=CASE WHEN dem.soCuoi < ds.soCuoi THEN ds.soCuoi ELSE dem.soCuoi END
    FROM dbo.SoThuTuKhamNgay dem JOIN STTTheoNgay ds ON ds.ngayCapSo=dem.ngayCapSo;

    ;WITH STTTheoNgay AS (
        SELECT ngayCapSo, MAX(soThuTuKham) AS soCuoi
        FROM dbo.PhieuKham
        WHERE ngayCapSo IS NOT NULL AND soThuTuKham IS NOT NULL
        GROUP BY ngayCapSo
    )
    INSERT dbo.SoThuTuKhamNgay(ngayCapSo,soCuoi)
    SELECT ds.ngayCapSo,ds.soCuoi
    FROM STTTheoNgay ds
    WHERE NOT EXISTS (SELECT 1 FROM dbo.SoThuTuKhamNgay dem WHERE dem.ngayCapSo=ds.ngayCapSo);

    COMMIT TRANSACTION;
    PRINT N'Migration 005 hoàn tất. Phiếu cũ được giữ nguyên; bộ đếm đồng bộ STT hiện có.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
