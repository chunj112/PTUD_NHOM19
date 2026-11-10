package iuh.se.nhom19.dao;

import java.sql.*;
import java.util.*;
import iuh.se.nhom19.entity.PhieuKham;

public class PhieuKhamDAO {
    /** Cấp STT dùng chung toàn phòng khám; bắt buộc gọi trong transaction tạo phiếu khám. */
    public int capSoThuTuKham(Connection ketNoi, java.time.LocalDate ngayCapSo) throws SQLException {
        if (ketNoi.getAutoCommit()) throw new SQLException("Cấp STT phải nằm trong transaction tạo phiếu khám.");

        // Khóa ứng dụng theo ngày để hai quầy tiếp nhận cùng lúc không lấy cùng số.
        String sqlKhoa = "SET NOCOUNT ON; DECLARE @ketQua INT; "
            + "EXEC @ketQua=sys.sp_getapplock @Resource=?, @LockMode='Exclusive', "
            + "@LockOwner='Transaction', @LockTimeout=15000; SELECT @ketQua";
        try (PreparedStatement lenh = ketNoi.prepareStatement(sqlKhoa)) {
            lenh.setString(1, "DentaFlow:STT:" + ngayCapSo);
            try (ResultSet bang = lenh.executeQuery()) {
                if (!bang.next() || bang.getInt(1) < 0)
                    throw new SQLException("Không lấy được khóa cấp số thứ tự khám trong thời gian cho phép.");
            }
        }

        // Đồng bộ với STT đã tồn tại trong PhieuKham trước khi tăng. Bộ đếm vẫn là
        // nguồn chính nên việc xóa phiếu cũ không làm số đã cấp bị dùng lại.
        String sqlTang = "UPDATE dbo.SoThuTuKhamNgay WITH (UPDLOCK,HOLDLOCK) "
            + "SET soCuoi=CASE WHEN soCuoi < ISNULL((SELECT MAX(pk.soThuTuKham) FROM dbo.PhieuKham pk WITH (UPDLOCK,HOLDLOCK) WHERE pk.ngayCapSo=?),0) "
            + "THEN ISNULL((SELECT MAX(pk.soThuTuKham) FROM dbo.PhieuKham pk WITH (UPDLOCK,HOLDLOCK) WHERE pk.ngayCapSo=?),0)+1 "
            + "ELSE soCuoi+1 END OUTPUT inserted.soCuoi WHERE ngayCapSo=?";
        try (PreparedStatement lenh = ketNoi.prepareStatement(sqlTang)) {
            lenh.setDate(1, java.sql.Date.valueOf(ngayCapSo));
            lenh.setDate(2, java.sql.Date.valueOf(ngayCapSo));
            lenh.setDate(3, java.sql.Date.valueOf(ngayCapSo));
            try (ResultSet bang = lenh.executeQuery()) {
                if (bang.next()) return bang.getInt(1);
            }
        }
        try (PreparedStatement lenh = ketNoi.prepareStatement(
                "INSERT dbo.SoThuTuKhamNgay(ngayCapSo,soCuoi) "
                + "OUTPUT inserted.soCuoi SELECT ?,ISNULL(MAX(soThuTuKham),0)+1 "
                + "FROM dbo.PhieuKham WITH (UPDLOCK,HOLDLOCK) WHERE ngayCapSo=?")) {
            lenh.setDate(1, java.sql.Date.valueOf(ngayCapSo));
            lenh.setDate(2, java.sql.Date.valueOf(ngayCapSo));
            try (ResultSet bang = lenh.executeQuery()) {
                if (!bang.next()) throw new SQLException("Không tạo được bộ đếm STT theo ngày.");
                return bang.getInt(1);
            }
        }
    }

    public boolean coPhieuDangHoatDong(Connection ketNoi,String maBenhNhan) throws SQLException {
        // Bệnh nhân đã được khóa trong Service; giữ khóa truy vấn đến commit để chặn nhận trùng từ hai quầy.
        try(PreparedStatement lenh=ketNoi.prepareStatement("SELECT TOP(1) maPhieuKham FROM dbo.PhieuKham WITH(UPDLOCK,HOLDLOCK) WHERE maBN=? AND trangThaiKham IN ('CHO_KHAM','DANG_KHAM')")) {
            lenh.setString(1,maBenhNhan);try(ResultSet bang=lenh.executeQuery()){return bang.next();}
        }
    }
    private static final String SELECT = "SELECT pk.*,bn.hoTen,bn.gioiTinh,bn.ngaySinh,bn.soDienThoai,bn.diaChi,nv.hoTen AS tenNhaSi "
        + "FROM dbo.PhieuKham pk JOIN dbo.BenhNhan bn ON bn.maBN=pk.maBN JOIN dbo.NhanVien nv ON nv.maNV=pk.maNVNhaSi ";

    public boolean kiemTraDaTiepNhan(Connection ketNoi, String maLichHen) throws SQLException {
        try (PreparedStatement lenh = ketNoi.prepareStatement("SELECT maPhieuKham FROM dbo.PhieuKham WITH(UPDLOCK,HOLDLOCK) WHERE maLH=?")) {
            lenh.setString(1, maLichHen);
            try (ResultSet bang = lenh.executeQuery()) { return bang.next(); }
        }
    }

    public void taoPhieuKham(Connection ketNoi, PhieuKham phieu) throws SQLException {
        if (phieu.layNgayCapSo() == null || phieu.laySoThuTuKham() == null)
            throw new IllegalArgumentException("Phiếu khám phải được cấp STT trước khi lưu.");
        try (PreparedStatement lenh = ketNoi.prepareStatement("INSERT dbo.PhieuKham(maPhieuKham,maBN,maNVNhaSi,maNVLeTan,maLH,maPhong,thoiDiemTiepNhan,lyDo,ghiChu,ngayCapSo,soThuTuKham,trangThaiKham) VALUES(?,?,?,?,?,?,?,?,?,?,?,'CHO_KHAM')")) {
            lenh.setString(1, phieu.layMaPhieuKham()); lenh.setString(2, phieu.layMaBenhNhan());
            lenh.setString(3, phieu.layMaNhaSi()); lenh.setString(4, phieu.layMaLeTan());
            // setString với null tạo SQL NULL: khách không hẹn không có bản ghi LichHen giả.
            lenh.setString(5, phieu.layMaLichHen()); lenh.setString(6, phieu.layMaPhong());
            lenh.setTimestamp(7, Timestamp.valueOf(phieu.layThoiDiemTiepNhan()));
            lenh.setString(8, phieu.layLyDo()); lenh.setString(9, phieu.layGhiChu());
            lenh.setDate(10, java.sql.Date.valueOf(phieu.layNgayCapSo())); lenh.setInt(11, phieu.laySoThuTuKham()); lenh.executeUpdate();
        }
    }

    public List<PhieuKham> layDanhSachChoKham(Connection ketNoi, String maNhaSi) throws SQLException {
        List<PhieuKham> danhSach = new ArrayList<>();
        try (PreparedStatement lenh = ketNoi.prepareStatement(SELECT + "WHERE pk.maNVNhaSi=? AND pk.trangThaiKham IN ('CHO_KHAM','DANG_KHAM') "
                + "ORDER BY CASE pk.trangThaiKham WHEN 'CHO_KHAM' THEN 0 ELSE 1 END,pk.soThuTuKham,pk.thoiDiemTiepNhan")) {
            lenh.setString(1, maNhaSi);
            try (ResultSet bang = lenh.executeQuery()) { while (bang.next()) danhSach.add(docPhieu(bang)); }
        }
        return danhSach;
    }

    /** Danh sách chỉ đọc theo ca/phòng Y tá được phân công; trạng thái lấy trực tiếp từ PhieuKham. */
    public List<PhieuKham> layDanhSachChoYTa(Connection ketNoi, String maYTa, java.time.LocalDate ngay,
            String maPhong, String trangThai) throws SQLException {
        String sql = "SELECT pk.*,bn.hoTen,bn.gioiTinh,bn.ngaySinh,bn.soDienThoai,bn.diaChi,nv.hoTen AS tenNhaSi "
            + "FROM dbo.PhieuKham pk JOIN dbo.BenhNhan bn ON bn.maBN=pk.maBN "
            + "JOIN dbo.NhanVien nv ON nv.maNV=pk.maNVNhaSi LEFT JOIN dbo.LichHen lh ON lh.maLH=pk.maLH "
            + "WHERE pk.ngayCapSo=? AND (? IS NULL OR pk.maPhong=?) AND (? IS NULL OR pk.trangThaiKham=?) "
            + "AND EXISTS (SELECT 1 FROM dbo.PhanCong pc JOIN dbo.NhanVien yt ON yt.maNV=pc.maNV "
            + "JOIN dbo.PhongKham p ON p.maPhong=pc.maPhong WHERE pc.maNV=? AND pc.ngayLam=pk.ngayCapSo "
            + "AND pc.maPhong=pk.maPhong AND pc.trangThai='DA_PHAN_CONG' AND yt.chucVu='Y_TA' "
            + "AND yt.trangThaiLamViec=1 AND p.trangThaiHoatDong=1 "
            // Lịch có hẹn thuộc ca theo giờ hẹn; khách không hẹn thuộc ca theo giờ tiếp nhận.
            + "AND COALESCE(lh.gioBatDau,CONVERT(TIME,pk.thoiDiemTiepNhan))>=pc.gioBatDau "
            + "AND COALESCE(lh.gioKetThuc,CONVERT(TIME,pk.thoiDiemTiepNhan))<=pc.gioKetThuc) "
            + "ORDER BY CASE pk.trangThaiKham WHEN 'CHO_KHAM' THEN 0 WHEN 'DANG_KHAM' THEN 1 ELSE 2 END,pk.soThuTuKham";
        List<PhieuKham> danhSach = new ArrayList<>();
        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setDate(1, java.sql.Date.valueOf(ngay));
            lenh.setString(2, maPhong); lenh.setString(3, maPhong);
            lenh.setString(4, trangThai); lenh.setString(5, trangThai);
            lenh.setString(6, maYTa);
            try (ResultSet bang = lenh.executeQuery()) {
                while (bang.next()) {
                    PhieuKham phieu = docPhieu(bang);
                    phieu.datMaPhong(bang.getString("maPhong"));
                    danhSach.add(phieu);
                }
            }
        }
        return danhSach;
    }

    /** Trả về phòng thuộc ca được phân công của Y tá trong ngày; UI không tự bịa phòng. */
    public List<String> layPhongDuocPhanCongYTa(Connection ketNoi, String maYTa, java.time.LocalDate ngay) throws SQLException {
        List<String> danhSach = new ArrayList<>();
        String sql = "SELECT DISTINCT pc.maPhong FROM dbo.PhanCong pc "
            + "JOIN dbo.NhanVien nv ON nv.maNV=pc.maNV JOIN dbo.PhongKham p ON p.maPhong=pc.maPhong "
            + "WHERE pc.maNV=? AND pc.ngayLam=? AND pc.trangThai='DA_PHAN_CONG' "
            + "AND nv.chucVu='Y_TA' AND nv.trangThaiLamViec=1 AND p.trangThaiHoatDong=1 "
            + "AND p.maPhong IN ('P01','P02') ORDER BY pc.maPhong";
        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setString(1, maYTa); lenh.setDate(2, java.sql.Date.valueOf(ngay));
            try (ResultSet bang = lenh.executeQuery()) { while (bang.next()) danhSach.add(bang.getString(1)); }
        }
        return danhSach;
    }

    public PhieuKham timPhieuKhamTheoMa(Connection ketNoi, String maPhieuKham, String maNhaSi) throws SQLException {
        // Lọc người sở hữu ngay trong SQL; biết mã phiếu không cho phép đọc phiếu của nha sĩ khác.
        try (PreparedStatement lenh = ketNoi.prepareStatement(SELECT + "WHERE pk.maPhieuKham=? AND pk.maNVNhaSi=? AND pk.trangThaiKham IN ('CHO_KHAM','DANG_KHAM')")) {
            lenh.setString(1, maPhieuKham); lenh.setString(2, maNhaSi);
            try (ResultSet bang = lenh.executeQuery()) { return bang.next() ? docPhieu(bang) : null; }
        }
    }

    public void batDauKham(Connection ketNoi, String maPhieuKham, String maNhaSi) throws SQLException {
        try (PreparedStatement lenh = ketNoi.prepareStatement("UPDATE dbo.PhieuKham SET trangThaiKham='DANG_KHAM' "
                + "WHERE maPhieuKham=? AND maNVNhaSi=? AND trangThaiKham='CHO_KHAM'")) {
            lenh.setString(1, maPhieuKham); lenh.setString(2, maNhaSi);
            if (lenh.executeUpdate() != 1) throw new IllegalArgumentException("Phiếu không còn chờ khám hoặc không thuộc nha sĩ đang đăng nhập.");
        }
    }

    public void hoanTatKham(Connection ketNoi, String maPhieuKham, String maNhaSi,
            String tinhTrangLamSan, String chanDoan, java.time.LocalDate ngayKham) throws SQLException {
        try (PreparedStatement lenh = ketNoi.prepareStatement("UPDATE dbo.PhieuKham SET tinhTrangLamSan=?,chanDoan=?,ngayKham=?,trangThaiKham='DA_KHAM' "
                + "WHERE maPhieuKham=? AND maNVNhaSi=? AND trangThaiKham='DANG_KHAM'")) {
            lenh.setString(1, tinhTrangLamSan); lenh.setString(2, chanDoan);
            lenh.setDate(3, java.sql.Date.valueOf(ngayKham)); lenh.setString(4, maPhieuKham); lenh.setString(5, maNhaSi);
            if (lenh.executeUpdate() != 1) throw new IllegalArgumentException("Chỉ hoàn tất phiếu đang khám của nha sĩ hiện tại.");
        }
    }

    private PhieuKham docPhieu(ResultSet bang) throws SQLException {
        PhieuKham phieu = new PhieuKham();
        phieu.datMaPhieuKham(bang.getString("maPhieuKham")); phieu.datMaBenhNhan(bang.getString("maBN"));
        phieu.datMaNhaSi(bang.getString("maNVNhaSi")); phieu.datMaLeTan(bang.getString("maNVLeTan"));
        phieu.datMaLichHen(bang.getString("maLH")); phieu.datMaPhong(bang.getString("maPhong"));
        phieu.datThoiDiemTiepNhan(bang.getTimestamp("thoiDiemTiepNhan").toLocalDateTime());
        phieu.datTrangThaiKham(bang.getString("trangThaiKham")); phieu.datLyDo(bang.getString("lyDo")); phieu.datGhiChu(bang.getString("ghiChu"));
        phieu.datTinhTrangLamSan(bang.getString("tinhTrangLamSan")); phieu.datChanDoan(bang.getString("chanDoan"));
        if (bang.getDate("ngayKham") != null) phieu.datNgayKham(bang.getDate("ngayKham").toLocalDate());
        if (bang.getDate("ngayCapSo") != null) phieu.datNgayCapSo(bang.getDate("ngayCapSo").toLocalDate());
        int soThuTu = bang.getInt("soThuTuKham"); if (!bang.wasNull()) phieu.datSoThuTuKham(soThuTu);
        phieu.datBenhNhan(BenhNhanDAO.docBenhNhan(bang)); phieu.datTenNhaSi(bang.getString("tenNhaSi"));
        return phieu;
    }
}
