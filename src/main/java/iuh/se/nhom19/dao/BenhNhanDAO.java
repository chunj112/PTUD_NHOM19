package iuh.se.nhom19.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import iuh.se.nhom19.entity.BenhNhan;
import iuh.se.nhom19.entity.HoSoBenhAn;

public class BenhNhanDAO {
    /** Cấp mã BN tăng dần trong transaction; khóa dải mã để các quầy không lấy trùng số. */
    public String sinhMaBenhNhan(Connection ketNoi) throws SQLException {
        String sql = "SELECT ISNULL(MAX(TRY_CONVERT(BIGINT, SUBSTRING(maBN,3,18))),0) "
            + "FROM dbo.BenhNhan WITH (UPDLOCK,HOLDLOCK) WHERE maBN LIKE 'BN%'";
        long soThuTu;
        try (PreparedStatement lenh = ketNoi.prepareStatement(sql);
             ResultSet bang = lenh.executeQuery()) {
            bang.next();
            soThuTu = bang.getLong(1) + 1;
        }
        // VARCHAR(20) cho phép tiền tố BN và tối đa 18 chữ số; không cắt số khi vượt 4 chữ số.
        if (soThuTu > 999_999_999_999_999_999L) throw new SQLException("Đã hết số thứ tự mã bệnh nhân.");
        return String.format(java.util.Locale.ROOT, "BN%04d", soThuTu);
    }

    public List<BenhNhan> timBenhNhan(Connection ketNoi, String tuKhoa) throws SQLException {
        // SĐT đầy đủ trả mọi hồ sơ trùng số để lễ tân tự xác minh; nhập dần chỉ gợi ý tối đa 20 hồ sơ.
        boolean soDayDu = tuKhoa != null && tuKhoa.matches("0[0-9]{9}");
        String sql = soDayDu
            ? "SELECT * FROM dbo.BenhNhan WHERE soDienThoai=? ORDER BY hoTen,maBN"
            : "SELECT TOP (20) * FROM dbo.BenhNhan WHERE CHARINDEX(?,soDienThoai)>0 ORDER BY hoTen,maBN";
        List<BenhNhan> danhSach = new ArrayList<>();
        // PreparedStatement tách dữ liệu nhập khỏi câu SQL; CHARINDEX tìm ký tự thật, không coi %/_ là wildcard.
        // try-with-resources đóng Statement/ResultSet. Service sở hữu Connection nên DAO không đóng Connection này.
        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setString(1, tuKhoa);
            try (ResultSet bang = lenh.executeQuery()) {
                while (bang.next()) danhSach.add(docBenhNhan(bang));
            }
        }
        return danhSach;
    }

    public BenhNhan timTheoMa(Connection ketNoi, String maBenhNhan) throws SQLException {
        try (PreparedStatement lenh = ketNoi.prepareStatement("SELECT * FROM dbo.BenhNhan WHERE maBN=?")) {
            lenh.setString(1, maBenhNhan);
            try (ResultSet bang = lenh.executeQuery()) { return bang.next() ? docBenhNhan(bang) : null; }
        }
    }

    public void themBenhNhan(Connection ketNoi, BenhNhan benhNhan) throws SQLException {
        try (PreparedStatement lenh = ketNoi.prepareStatement("INSERT dbo.BenhNhan(maBN,hoTen,gioiTinh,ngaySinh,soDienThoai,diaChi) VALUES(?,?,?,?,?,?)")) {
            lenh.setString(1, benhNhan.layMaBenhNhan()); lenh.setString(2, benhNhan.layHoTen());
            lenh.setBoolean(3, benhNhan.layNam()); lenh.setDate(4, Date.valueOf(benhNhan.layNgaySinh()));
            lenh.setString(5, benhNhan.laySoDienThoai()); lenh.setString(6, benhNhan.layDiaChi());
            lenh.executeUpdate();
        }
    }

    public void themHoSo(Connection ketNoi, HoSoBenhAn hoSo) throws SQLException {
        new HoSoBenhAnDAO().taoHoSoBenhAn(ketNoi, hoSo);
    }

    public void khoaBenhNhan(Connection ketNoi, String maBenhNhan) throws SQLException {
        // Khóa bệnh nhân trước khi kiểm tra/tạo hồ sơ để hai lần tiếp nhận không cùng tạo HSBA mới.
        try (PreparedStatement lenh = ketNoi.prepareStatement("SELECT maBN FROM dbo.BenhNhan WITH(UPDLOCK,HOLDLOCK) WHERE maBN=?")) {
            lenh.setString(1, maBenhNhan);
            try (ResultSet bang = lenh.executeQuery()) { if (!bang.next()) throw new IllegalArgumentException("Bệnh nhân không tồn tại."); }
        }
    }

    public String layDiUng(Connection ketNoi, String maBenhNhan) throws SQLException {
        try (PreparedStatement lenh = ketNoi.prepareStatement("SELECT tienSuDiUng FROM dbo.HoSoBenhAn WHERE maBN=?")) {
            lenh.setString(1, maBenhNhan);
            try (ResultSet bang = lenh.executeQuery()) { return bang.next() ? bang.getString(1) : null; }
        }
    }

    public static BenhNhan docBenhNhan(ResultSet bang) throws SQLException {
        // Mỗi dòng ResultSet được chuyển thành một Entity; DATE giữ nguyên ngày bằng LocalDate.
        BenhNhan benhNhan = new BenhNhan();
        benhNhan.datMaBenhNhan(bang.getString("maBN")); benhNhan.datHoTen(bang.getString("hoTen"));
        benhNhan.datNam(bang.getBoolean("gioiTinh")); benhNhan.datNgaySinh(bang.getDate("ngaySinh").toLocalDate());
        benhNhan.datSoDienThoai(bang.getString("soDienThoai")); benhNhan.datDiaChi(bang.getString("diaChi"));
        return benhNhan;
    }
}
