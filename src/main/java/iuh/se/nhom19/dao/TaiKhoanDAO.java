package iuh.se.nhom19.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.entity.NhanVien;
import iuh.se.nhom19.entity.TaiKhoan;

/** UC001 chỉ cần tìm theo tên đăng nhập, không bổ sung CRUD tài khoản. */
public class TaiKhoanDAO {
    private final KetNoiCSDL ketNoiCSDL;

    public TaiKhoanDAO() {
        this(new KetNoiCSDL());
    }

    public TaiKhoanDAO(KetNoiCSDL ketNoiCSDL) {
        this.ketNoiCSDL = Objects.requireNonNull(ketNoiCSDL);
    }

    public TaiKhoan timTheoTenDangNhap(String tenDangNhap) throws SQLException {
        String cauLenh = "SELECT tk.maTaiKhoan, tk.tenDangNhap, tk.matKhau, tk.trangThaiHoatDong, "
                + "nv.maNV, nv.hoTen, nv.chucVu, nv.trangThaiLamViec FROM dbo.TaiKhoan tk "
                + "INNER JOIN dbo.NhanVien nv ON nv.maNV=tk.maNV WHERE tk.tenDangNhap=?";

        // Dấu ? được bind riêng, không nối tên đăng nhập trực tiếp vào SQL.
        // try-with-resources tự đóng cả kết nối, câu lệnh và kết quả, kể cả khi lỗi.
        try (Connection ketNoi = ketNoiCSDL.layKetNoi();
             PreparedStatement lenh = ketNoi.prepareStatement(cauLenh)) {
            lenh.setQueryTimeout(10);
            lenh.setString(1, tenDangNhap);
            try (ResultSet dong = lenh.executeQuery()) {
                if (!dong.next()) return null;

                NhanVien nhanVien = new NhanVien(
                        dong.getString("maNV"), dong.getString("hoTen"),
                        dong.getString("chucVu"), dong.getBoolean("trangThaiLamViec"));
                return new TaiKhoan(
                        dong.getString("maTaiKhoan"), dong.getString("tenDangNhap"),
                        dong.getString("matKhau"), dong.getBoolean("trangThaiHoatDong"), nhanVien);
            }
        }
    }
}
