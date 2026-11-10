package iuh.se.nhom19.dao;

import java.sql.*;
import java.time.*;
import java.util.*;
import iuh.se.nhom19.entity.PhanCong;

public class PhanCongDAO {
    public List<PhanCong> layDanhSachNhaSiDangLamViec(Connection ketNoi, LocalDate ngay) throws SQLException {
        String sql = "SELECT pc.*,nv.hoTen,p.tenPhong,(SELECT COUNT(*) FROM dbo.PhieuKham pk WHERE pk.maNVNhaSi=pc.maNV AND pk.trangThaiKham='CHO_KHAM') AS soCho FROM dbo.PhanCong pc JOIN dbo.NhanVien nv ON nv.maNV=pc.maNV "
            + "JOIN dbo.PhongKham p ON p.maPhong=pc.maPhong WHERE pc.ngayLam=? AND pc.trangThai='DA_PHAN_CONG' "
            + "AND p.maPhong IN ('P01','P02') AND nv.chucVu='NHA_SI' AND nv.trangThaiLamViec=1 AND p.trangThaiHoatDong=1 ORDER BY nv.hoTen,pc.gioBatDau,pc.maPC";
        List<PhanCong> danhSach = new ArrayList<>();
        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setDate(1, java.sql.Date.valueOf(ngay));
            try (ResultSet bang = lenh.executeQuery()) {
                while (bang.next()) {
                    PhanCong phanCong = new PhanCong();
                    phanCong.datMaPhanCong(bang.getString("maPC")); phanCong.datMaNhanVien(bang.getString("maNV"));
                    phanCong.datMaPhong(bang.getString("maPhong")); phanCong.datNgayLam(bang.getDate("ngayLam").toLocalDate());
                    phanCong.datGioBatDau(bang.getTime("gioBatDau").toLocalTime()); phanCong.datGioKetThuc(bang.getTime("gioKetThuc").toLocalTime());
                    phanCong.datCaLam(bang.getString("caLam")); phanCong.datTrangThai(bang.getString("trangThai"));
                    phanCong.datNoiDungCongViec(bang.getString("noiDungCongViec"));
                    phanCong.datTenNhaSi(bang.getString("hoTen")); phanCong.datTenPhong(bang.getString("tenPhong")); phanCong.datSoCho(bang.getInt("soCho"));
                    danhSach.add(phanCong);
                }
            }
        }
        return danhSach;
    }

    /** Lịch làm cá nhân theo khoảng ngày, dùng chung bảng PhanCong cho các vai trò. */
    public List<PhanCong> layLichCaNhan(Connection ketNoi, String maNhanVien, LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        String sql = "SELECT pc.*,nv.hoTen,p.tenPhong FROM dbo.PhanCong pc "
            + "JOIN dbo.NhanVien nv ON nv.maNV=pc.maNV LEFT JOIN dbo.PhongKham p ON p.maPhong=pc.maPhong "
            + "WHERE pc.maNV=? AND pc.ngayLam BETWEEN ? AND ? AND pc.trangThai='DA_PHAN_CONG' "
            + "AND nv.trangThaiLamViec=1 ORDER BY pc.ngayLam,pc.gioBatDau,pc.maPC";
        List<PhanCong> danhSach = new ArrayList<>();
        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setString(1, maNhanVien); lenh.setDate(2, java.sql.Date.valueOf(tuNgay));
            lenh.setDate(3, java.sql.Date.valueOf(denNgay));
            try (ResultSet bang = lenh.executeQuery()) {
                while (bang.next()) {
                    PhanCong pc = new PhanCong();
                    pc.datMaPhanCong(bang.getString("maPC")); pc.datMaNhanVien(bang.getString("maNV"));
                    pc.datMaPhong(bang.getString("maPhong")); pc.datNgayLam(bang.getDate("ngayLam").toLocalDate());
                    pc.datGioBatDau(bang.getTime("gioBatDau").toLocalTime()); pc.datGioKetThuc(bang.getTime("gioKetThuc").toLocalTime());
                    pc.datCaLam(bang.getString("caLam")); pc.datTrangThai(bang.getString("trangThai"));
                    pc.datNoiDungCongViec(bang.getString("noiDungCongViec")); pc.datTenNhanVien(bang.getString("hoTen"));
                    pc.datTenPhong(bang.getString("tenPhong")); danhSach.add(pc);
                }
            }
        }
        return danhSach;
    }

    public void khoaNhaSi(Connection ketNoi, String maNhaSi) throws SQLException {
        // Khóa đến cuối transaction: hai lễ tân đặt lịch cùng nha sĩ phải kiểm tra/lưu lần lượt.
        // Chỉ SELECT trước rồi INSERT mà không khóa có thể cho cả hai cùng thấy một khoảng giờ trống.
        try (PreparedStatement lenh = ketNoi.prepareStatement("SELECT maNV FROM dbo.NhanVien WITH (UPDLOCK,HOLDLOCK) WHERE maNV=? AND chucVu='NHA_SI' AND trangThaiLamViec=1")) {
            lenh.setString(1, maNhaSi);
            try (ResultSet bang = lenh.executeQuery()) {
                if (!bang.next()) throw new IllegalArgumentException("Nha sĩ không tồn tại hoặc đã ngừng làm việc.");
            }
        }
    }

    public PhanCong kiemTraCaLam(Connection ketNoi, String maNhaSi, LocalDate ngay, LocalTime tuGio, LocalTime denGio, String maPhong) throws SQLException {
        // Giữ khóa phân công/phòng đến commit, tránh dùng ca vừa bị hủy hoặc phòng vừa ngừng hoạt động.
        String sql = "SELECT pc.maPC FROM dbo.PhanCong pc WITH (UPDLOCK,HOLDLOCK) JOIN dbo.PhongKham p WITH (HOLDLOCK) ON p.maPhong=pc.maPhong "
            + "WHERE pc.maNV=? AND pc.ngayLam=? AND pc.trangThai='DA_PHAN_CONG' AND p.trangThaiHoatDong=1 "
            + "AND p.maPhong IN ('P01','P02') AND pc.gioBatDau<=? AND pc.gioKetThuc>=? AND (? IS NULL OR pc.maPhong=?)";
        String maPhanCong = null;
        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setString(1, maNhaSi); lenh.setDate(2, java.sql.Date.valueOf(ngay));
            lenh.setTime(3, Time.valueOf(tuGio)); lenh.setTime(4, Time.valueOf(denGio));
            lenh.setString(5, maPhong); lenh.setString(6, maPhong);
            try (ResultSet bang = lenh.executeQuery()) { if (bang.next()) maPhanCong = bang.getString(1); }
        }
        if (maPhanCong == null) throw new IllegalArgumentException("Không có ca phân công/phòng hoạt động phù hợp cho thời gian đã chọn.");
        for (PhanCong phanCong : layDanhSachNhaSiDangLamViec(ketNoi, ngay)) {
            if (maPhanCong.equals(phanCong.layMaPhanCong())) return phanCong;
        }
        throw new SQLException("Không đọc được ca đã kiểm tra.");
    }
}
