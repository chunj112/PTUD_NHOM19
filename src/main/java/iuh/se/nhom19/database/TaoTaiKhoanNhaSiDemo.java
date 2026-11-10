package iuh.se.nhom19.database;

import java.sql.*;
import java.util.*;
import iuh.se.nhom19.auth.BaoMatMatKhau;
import iuh.se.nhom19.config.CauHinhUngDung;

/** Tạo riêng tài khoản cho hai hồ sơ nha sĩ demo đã có; không tạo hoặc sửa hồ sơ nhân viên. */
public final class TaoTaiKhoanNhaSiDemo {
    private static final String[][] NHA_SI = {
        {"NV_DEMO_NS01", "TK_DEMO_NS01", "DENTAFLOW_DEMO_NS01_USER", "DENTAFLOW_DEMO_NS01_PASSWORD"},
        {"NV_DEMO_NS02", "TK_DEMO_NS02", "DENTAFLOW_DEMO_NS02_USER", "DENTAFLOW_DEMO_NS02_PASSWORD"}
    };

    private TaoTaiKhoanNhaSiDemo() {}

    public static void main(String[] thamSo) {
        try {
            if (thamSo.length != 1 || !"--xac-nhan-tao-tai-khoan-nha-si-demo".equals(thamSo[0]))
                throw new IllegalArgumentException("Chạy với tham số --xac-nhan-tao-tai-khoan-nha-si-demo sau khi đã kiểm tra .env.");
            if (!"true".equalsIgnoreCase(CauHinhUngDung.docGiaTri("DENTAFLOW_ALLOW_ACCOUNT_SEED")))
                throw new IllegalArgumentException("Chưa bật DENTAFLOW_ALLOW_ACCOUNT_SEED=true trong .env.");

            try (Connection ketNoi = new KetNoiCSDL().layKetNoi()) {
                if (!"DENTAFLOW_DB".equalsIgnoreCase(ketNoi.getCatalog()))
                    throw new IllegalArgumentException("Công cụ chỉ hỗ trợ database DENTAFLOW_DB.");
                ketNoi.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                ketNoi.setAutoCommit(false);
                List<String> thongBao = new ArrayList<>();
                try {
                    for (String[] mau : NHA_SI) thongBao.add(taoTaiKhoanNeuThieu(ketNoi, mau));
                    ketNoi.commit();
                } catch (SQLException | RuntimeException loi) {
                    try { ketNoi.rollback(); } catch (SQLException loiRollback) { /* Không in thông tin kết nối. */ }
                    throw loi;
                }
                for (String ketQua : thongBao) System.out.println(ketQua);
            }
        } catch (SQLException loi) {
            System.err.println("Không tạo được tài khoản nha sĩ demo. Kiểm tra kết nối, mã nhân viên và ràng buộc trong database.");
            System.exit(1);
        } catch (IllegalArgumentException | IllegalStateException loi) {
            System.err.println(loi.getMessage());
            System.exit(1);
        }
    }

    private static String taoTaiKhoanNeuThieu(Connection ketNoi, String[] mau) throws SQLException {
        String maNhanVien = mau[0];
        String maTaiKhoan = mau[1];
        String tenDangNhap = giaTriBatBuoc(mau[2]);
        String matKhau = giaTriBatBuoc(mau[3]);
        if (tenDangNhap.length() < 3 || tenDangNhap.length() > 60 || tenDangNhap.chars().anyMatch(Character::isWhitespace))
            throw new IllegalArgumentException("Tên đăng nhập cần 3–60 ký tự và không chứa khoảng trắng: " + mau[2] + ".");
        if (matKhau.length() < 12 || matKhau.length() > 1024)
            throw new IllegalArgumentException("Mật khẩu trong " + mau[3] + " cần dài ít nhất 12 ký tự.");

        try (PreparedStatement lenh = ketNoi.prepareStatement(
                "SELECT chucVu,trangThaiLamViec FROM dbo.NhanVien WITH (UPDLOCK,HOLDLOCK) WHERE maNV=?")) {
            lenh.setString(1, maNhanVien);
            try (ResultSet dong = lenh.executeQuery()) {
                if (!dong.next() || !"NHA_SI".equals(dong.getString("chucVu")) || !dong.getBoolean("trangThaiLamViec"))
                    throw new IllegalArgumentException("Không tìm thấy hồ sơ nha sĩ đang làm việc " + maNhanVien + ". Hãy chạy script thêm nha sĩ trước.");
            }
        }

        try (PreparedStatement lenh = ketNoi.prepareStatement(
                "SELECT maTaiKhoan,maNV,tenDangNhap FROM dbo.TaiKhoan WITH (UPDLOCK,HOLDLOCK) "
                + "WHERE maNV=? OR tenDangNhap=? OR maTaiKhoan=?")) {
            lenh.setString(1, maNhanVien); lenh.setString(2, tenDangNhap); lenh.setString(3, maTaiKhoan);
            try (ResultSet dong = lenh.executeQuery()) {
                if (dong.next()) {
                    if (maNhanVien.equals(dong.getString("maNV")) && tenDangNhap.equals(dong.getString("tenDangNhap"))
                            && maTaiKhoan.equals(dong.getString("maTaiKhoan"))) {
                        return tenDangNhap + ": tài khoản đã tồn tại, giữ nguyên mật khẩu và trạng thái.";
                    }
                    throw new IllegalArgumentException("Tên đăng nhập hoặc mã tài khoản đã được dùng; không ghi đè dữ liệu.");
                }
            }
        }

        String matKhauDaBam = BaoMatMatKhau.bamMatKhau(matKhau);
        try (PreparedStatement lenh = ketNoi.prepareStatement(
                "INSERT dbo.TaiKhoan(maTaiKhoan,maNV,tenDangNhap,matKhau,trangThaiHoatDong) VALUES(?,?,?,?,1)")) {
            lenh.setString(1, maTaiKhoan); lenh.setString(2, maNhanVien);
            lenh.setString(3, tenDangNhap); lenh.setString(4, matKhauDaBam);
            lenh.executeUpdate();
        }
        return tenDangNhap + ": đã tạo tài khoản cho " + maNhanVien + " với mật khẩu đã băm PBKDF2.";
    }

    private static String giaTriBatBuoc(String tenBien) {
        String giaTri = CauHinhUngDung.docGiaTri(tenBien);
        if (giaTri == null || giaTri.isBlank()) throw new IllegalArgumentException("Thiếu " + tenBien + " trong .env.");
        return giaTri;
    }
}
