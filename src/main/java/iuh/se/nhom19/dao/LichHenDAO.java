package iuh.se.nhom19.dao;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import iuh.se.nhom19.entity.LichHen;

public class LichHenDAO {
    // LEFT JOIN giữ lịch người mới chưa có maBN; tên/SĐT hiển thị luôn đọc từ snapshot LichHen.
    private static final String SELECT = "SELECT lh.*,bn.hoTen,bn.gioiTinh,bn.ngaySinh,bn.soDienThoai,bn.diaChi,nv.hoTen AS tenNhaSi,COALESCE(pk.maPhong,ca.maPhong) AS maPhongLichHen "
        + "FROM dbo.LichHen lh LEFT JOIN dbo.BenhNhan bn ON bn.maBN=lh.maBN JOIN dbo.NhanVien nv ON nv.maNV=lh.maNVNhaSi "
        + "LEFT JOIN dbo.PhieuKham pk ON pk.maLH=lh.maLH "
        + "OUTER APPLY (SELECT TOP (1) pc.maPhong FROM dbo.PhanCong pc WHERE pc.maNV=lh.maNVNhaSi AND pc.ngayLam=lh.ngayHen "
        + "AND pc.trangThai='DA_PHAN_CONG' AND pc.gioBatDau<=lh.gioBatDau AND pc.gioKetThuc>=lh.gioKetThuc ORDER BY pc.gioBatDau) ca ";

    public List<LichHen> layDanhSachLichHen(Connection ketNoi, LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        List<LichHen> danhSach = new ArrayList<>();
        try (PreparedStatement lenh = ketNoi.prepareStatement(SELECT + "WHERE lh.ngayHen BETWEEN ? AND ? AND lh.trangThai<>'DA_HUY' ORDER BY lh.ngayHen,lh.gioBatDau,lh.maLH")) {
            lenh.setDate(1, java.sql.Date.valueOf(tuNgay)); lenh.setDate(2, java.sql.Date.valueOf(denNgay));
            try (ResultSet bang = lenh.executeQuery()) { while (bang.next()) danhSach.add(docLichHen(bang)); }
        }
        return danhSach;
    }

    /** Trả các bản ghi tương lai mang trạng thái đã đến để nhân viên rà soát; không sửa dữ liệu. */
    public List<LichHen> layLichTuongLaiDaDen(Connection ketNoi, LocalDate homNay) throws SQLException {
        List<LichHen> danhSach = new ArrayList<>();
        try (PreparedStatement lenh = ketNoi.prepareStatement(SELECT + "WHERE lh.ngayHen>? AND lh.trangThai='DA_DEN' ORDER BY lh.ngayHen,lh.gioBatDau,lh.maLH")) {
            lenh.setDate(1, java.sql.Date.valueOf(homNay));
            try (ResultSet bang = lenh.executeQuery()) { while (bang.next()) danhSach.add(docLichHen(bang)); }
        }
        return danhSach;
    }

    public LichHen timLichHenTheoMa(Connection ketNoi, String maLichHen) throws SQLException {
        try (PreparedStatement lenh = ketNoi.prepareStatement(SELECT + "WHERE lh.maLH=?")) {
            lenh.setString(1, maLichHen);
            try (ResultSet bang = lenh.executeQuery()) { return bang.next() ? docLichHen(bang) : null; }
        }
    }

    public void khoaLichHen(Connection ketNoi, String maLichHen) throws SQLException {
        try (PreparedStatement lenh = ketNoi.prepareStatement("SELECT maLH FROM dbo.LichHen WITH(UPDLOCK,HOLDLOCK) WHERE maLH=?")) {
            lenh.setString(1, maLichHen);
            try (ResultSet bang = lenh.executeQuery()) { if (!bang.next()) throw new IllegalArgumentException("Không tìm thấy lịch hẹn."); }
        }
    }

    public boolean kiemTraTrungLich(Connection ketNoi, String maNhaSi, LocalDate ngay, LocalTime tuGio, LocalTime denGio) throws SQLException {
        return kiemTraTrungLich(ketNoi, maNhaSi, ngay, tuGio, denGio, null);
    }

    public boolean kiemTraTrungLich(Connection ketNoi, String maNhaSi, LocalDate ngay, LocalTime tuGio, LocalTime denGio, String maBoQua) throws SQLException {
        // Hai khoảng [bắt đầu,kết thúc) trùng khi đầu mới < cuối cũ và đầu cũ < cuối mới.
        // Kết thúc lúc 09:00 và bắt đầu lúc 09:00 không trùng; lịch đã hủy không chiếm chỗ.
        String sql = "SELECT TOP (1) maLH FROM dbo.LichHen WITH(UPDLOCK,HOLDLOCK) WHERE maNVNhaSi=? AND ngayHen=? AND trangThai<>'DA_HUY' AND gioBatDau<? AND gioKetThuc>? AND (? IS NULL OR maLH<>?)";
        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setString(1, maNhaSi); lenh.setDate(2, java.sql.Date.valueOf(ngay));
            lenh.setTime(3, Time.valueOf(denGio)); lenh.setTime(4, Time.valueOf(tuGio));
            lenh.setString(5, maBoQua); lenh.setString(6, maBoQua);
            try (ResultSet bang = lenh.executeQuery()) { return bang.next(); }
        }
    }

    /** Sinh mã LH-YYYYMMDD-NNN theo ngày hẹn; khóa dải mã để hai lượt đặt đồng thời không cấp trùng. */
    public String sinhMaLichHen(Connection ketNoi, LocalDate ngayHen) throws SQLException {
        String tienTo = "LH-" + DateTimeFormatter.BASIC_ISO_DATE.format(ngayHen) + "-";
        String sql = "SELECT ISNULL(MAX(TRY_CONVERT(BIGINT, SUBSTRING(maLH,13,8))),0) "
            + "FROM dbo.LichHen WITH (UPDLOCK,HOLDLOCK) WHERE maLH LIKE ?";
        long soThuTu;
        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setString(1, tienTo + "%");
            try (ResultSet bang = lenh.executeQuery()) {
                bang.next();
                soThuTu = bang.getLong(1) + 1;
            }
        }
        if (soThuTu > 99_999_999L) throw new SQLException("Đã hết số thứ tự mã lịch hẹn trong ngày.");
        return String.format(Locale.ROOT, "%s%03d", tienTo, soThuTu);
    }

    public void themLichHen(Connection ketNoi, LichHen lichHen) throws SQLException {
        try (PreparedStatement lenh = ketNoi.prepareStatement("INSERT dbo.LichHen(maLH,maBN,maNVNhaSi,ngayHen,gioBatDau,gioKetThuc,lyDoKham,ghiChu,trangThai,hoTenNguoiHen,soDienThoaiLienHe) VALUES(?,?,?,?,?,?,?,?,?,?,?)")) {
            lenh.setString(1, lichHen.layMaLichHen()); lenh.setString(2, lichHen.layMaBenhNhan()); lenh.setString(3, lichHen.layMaNhaSi());
            lenh.setDate(4, java.sql.Date.valueOf(lichHen.layNgayHen())); lenh.setTime(5, Time.valueOf(lichHen.layGioBatDau()));
            lenh.setTime(6, Time.valueOf(lichHen.layGioKetThuc())); lenh.setString(7, lichHen.layLyDoKham());
            lenh.setString(8, lichHen.layGhiChu()); lenh.setString(9, lichHen.layTrangThai());
            lenh.setString(10, lichHen.layHoTenNguoiHen()); lenh.setString(11, lichHen.laySoDienThoaiLienHe()); lenh.executeUpdate();
        }
    }

    public void capNhatTrangThaiLichHen(Connection ketNoi, String maLichHen, LocalDate ngayTiepNhan) throws SQLException {
        // Điều kiện ngày nằm ngay trong câu UPDATE để không thể đánh dấu lịch tương lai là đã đến.
        try (PreparedStatement lenh = ketNoi.prepareStatement("UPDATE dbo.LichHen SET trangThai='DA_DEN' WHERE maLH=? AND trangThai IN ('CHO_XAC_NHAN','DA_XAC_NHAN') AND ngayHen=?")) {
            lenh.setString(1, maLichHen); lenh.setDate(2, java.sql.Date.valueOf(ngayTiepNhan));
            if (lenh.executeUpdate() != 1) throw new IllegalArgumentException("Lịch hẹn đã hủy, đã tiếp nhận hoặc vừa được thay đổi.");
        }
    }

    public void capNhatBenhNhanChoLichHen(Connection ketNoi, String maLichHen, String maBenhNhan) throws SQLException {
        try (PreparedStatement lenh = ketNoi.prepareStatement("UPDATE dbo.LichHen SET maBN=? WHERE maLH=? AND (maBN IS NULL OR maBN=?) AND trangThai IN ('CHO_XAC_NHAN','DA_XAC_NHAN')")) {
            lenh.setString(1, maBenhNhan); lenh.setString(2, maLichHen); lenh.setString(3, maBenhNhan);
            if (lenh.executeUpdate()!=1) throw new IllegalArgumentException("Lịch đã thay đổi hồ sơ hoặc không còn được tiếp nhận.");
        }
    }

    public void suaLichHen(Connection ketNoi, LichHen lichHen) throws SQLException {
        String sql="UPDATE dbo.LichHen SET maBN=?,hoTenNguoiHen=?,soDienThoaiLienHe=?,maNVNhaSi=?,ngayHen=?,gioBatDau=?,gioKetThuc=?,lyDoKham=?,ghiChu=? WHERE maLH=? AND trangThai IN ('CHO_XAC_NHAN','DA_XAC_NHAN')";
        try (PreparedStatement lenh=ketNoi.prepareStatement(sql)) {
            lenh.setString(1,lichHen.layMaBenhNhan()); lenh.setString(2,lichHen.layHoTenNguoiHen()); lenh.setString(3,lichHen.laySoDienThoaiLienHe());
            lenh.setString(4,lichHen.layMaNhaSi()); lenh.setDate(5,java.sql.Date.valueOf(lichHen.layNgayHen()));
            lenh.setTime(6,Time.valueOf(lichHen.layGioBatDau())); lenh.setTime(7,Time.valueOf(lichHen.layGioKetThuc()));
            lenh.setString(8,lichHen.layLyDoKham()); lenh.setString(9,lichHen.layGhiChu()); lenh.setString(10,lichHen.layMaLichHen());
            if(lenh.executeUpdate()!=1)throw new IllegalArgumentException("Không được sửa lịch đã hủy/đã tiếp nhận.");
        }
    }
    public void huyLichHen(Connection ketNoi, String maLichHen) throws SQLException {
        // Hủy chỉ đổi trạng thái lịch. Không DELETE bệnh nhân, HSBA hay phiếu khám.
        try (PreparedStatement lenh=ketNoi.prepareStatement("UPDATE dbo.LichHen SET trangThai='DA_HUY' WHERE maLH=? AND trangThai IN ('CHO_XAC_NHAN','DA_XAC_NHAN')")) {
            lenh.setString(1,maLichHen);
            if(lenh.executeUpdate()!=1)throw new IllegalArgumentException("Không được hủy lịch đã hủy/đã tiếp nhận.");
        }
    }

    public void huyLichHen(Connection ketNoi,String maLichHen,String ghiChu) throws SQLException {
        try(PreparedStatement lenh=ketNoi.prepareStatement("UPDATE dbo.LichHen SET trangThai='DA_HUY',ghiChu=? WHERE maLH=? AND trangThai IN ('CHO_XAC_NHAN','DA_XAC_NHAN')")) {
            lenh.setString(1,ghiChu);lenh.setString(2,maLichHen);
            if(lenh.executeUpdate()!=1)throw new IllegalArgumentException("Không được hủy lịch đã hủy/đã tiếp nhận.");
        }
    }

    private LichHen docLichHen(ResultSet bang) throws SQLException {
        LichHen lichHen = new LichHen();
        lichHen.datMaLichHen(bang.getString("maLH")); lichHen.datMaBenhNhan(bang.getString("maBN"));
        lichHen.datMaNhaSi(bang.getString("maNVNhaSi")); lichHen.datNgayHen(bang.getDate("ngayHen").toLocalDate());
        lichHen.datGioBatDau(bang.getTime("gioBatDau").toLocalTime()); lichHen.datGioKetThuc(bang.getTime("gioKetThuc").toLocalTime());
        lichHen.datLyDoKham(bang.getString("lyDoKham")); lichHen.datGhiChu(bang.getString("ghiChu")); lichHen.datTrangThai(bang.getString("trangThai"));
        lichHen.datHoTenNguoiHen(bang.getString("hoTenNguoiHen")); lichHen.datSoDienThoaiLienHe(bang.getString("soDienThoaiLienHe"));
        if (lichHen.layMaBenhNhan() != null) lichHen.datBenhNhan(BenhNhanDAO.docBenhNhan(bang)); lichHen.datTenNhaSi(bang.getString("tenNhaSi"));
        lichHen.datMaPhong(bang.getString("maPhongLichHen"));
        return lichHen;
    }
}
