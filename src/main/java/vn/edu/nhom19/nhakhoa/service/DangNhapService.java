package vn.edu.nhom19.nhakhoa.service;

import java.sql.SQLException;
import java.util.Objects;
import vn.edu.nhom19.nhakhoa.auth.BaoMatMatKhau;
import vn.edu.nhom19.nhakhoa.auth.KetQuaDangNhap;
import vn.edu.nhom19.nhakhoa.auth.Role;
import vn.edu.nhom19.nhakhoa.dao.TaiKhoanDAO;
import vn.edu.nhom19.nhakhoa.entity.NhanVien;
import vn.edu.nhom19.nhakhoa.entity.TaiKhoan;

/** Xác thực tài khoản SQL, không điều khiển giao diện hoặc lưu Session. */
public class DangNhapService {
    public static final String SAI_THONG_TIN = "Tên đăng nhập hoặc mật khẩu không đúng.";
    private final TaiKhoanDAO taiKhoanDAO;

    public DangNhapService() {
        this(new TaiKhoanDAO());
    }

    public DangNhapService(TaiKhoanDAO taiKhoanDAO) {
        this.taiKhoanDAO = Objects.requireNonNull(taiKhoanDAO);
    }

    public KetQuaDangNhap dangNhap(String tenDangNhap, String matKhau) {
        // 1. Kiểm tra đầu vào; chỉ bỏ khoảng trắng ở tên, không trim mật khẩu.
        if (tenDangNhap == null || tenDangNhap.isBlank() || matKhau == null || matKhau.isBlank()) {
            throw new LoiDangNhap("Vui lòng nhập tên đăng nhập và mật khẩu.");
        }
        if (tenDangNhap.trim().length() > 60 || matKhau.length() > 1024) {
            throw new LoiDangNhap(SAI_THONG_TIN);
        }

        // 2. Mọi tên đăng nhập đều được tìm trong SQL, không có tài khoản đặc biệt.
        TaiKhoan taiKhoan;
        try {
            taiKhoan = taiKhoanDAO.timTheoTenDangNhap(tenDangNhap.trim());
        } catch (SQLException | IllegalStateException loi) {
            // Không đưa exception JDBC hoặc cấu hình kết nối lên màn hình.
            throw new LoiDangNhap("Không thể kết nối hoặc truy vấn SQL Server. Kiểm tra cấu hình và thử lại.");
        }
        if (taiKhoan == null) {
            // Chỉ cân bằng thời gian xử lý; không lưu hash hay tạo tài khoản trong SQL.
            BaoMatMatKhau.bamMatKhau(matKhau);
            throw new LoiDangNhap(SAI_THONG_TIN);
        }

        // 3. Xác minh với salt/hash đã lưu; không đổi mật khẩu trong database.
        if (!BaoMatMatKhau.kiemTraMatKhau(matKhau, taiKhoan.layMatKhauBam())) {
            throw new LoiDangNhap(SAI_THONG_TIN);
        }

        // 4. Cả tài khoản và nhân viên phải còn hoạt động.
        NhanVien nhanVien = taiKhoan.layNhanVien();
        if (!taiKhoan.dangHoatDong() || nhanVien == null || !nhanVien.dangLamViec()) {
            throw new LoiDangNhap("Tài khoản hoặc nhân viên đã ngừng hoạt động. Vui lòng liên hệ quản lý.");
        }

        // 5. Kết quả chỉ mang danh tính và Role, không mang hash tới giao diện.
        Role vaiTro = chuyenVaiTro(nhanVien.layChucVu());
        return new KetQuaDangNhap(nhanVien.layMaNhanVien(), nhanVien.layHoTen(), vaiTro);
    }

    private Role chuyenVaiTro(String chucVu) {
        if (chucVu == null) {
            throw new LoiDangNhap("Chức vụ tài khoản không hợp lệ. Vui lòng liên hệ quản lý.");
        }
        switch (chucVu) {
            case "NHAN_VIEN_QUAN_LY": return Role.MANAGER;
            case "LE_TAN": return Role.RECEPTIONIST;
            case "NHA_SI": return Role.DENTIST;
            case "Y_TA": return Role.NURSE;
            default: throw new LoiDangNhap("Chức vụ tài khoản không hợp lệ. Vui lòng liên hệ quản lý.");
        }
    }
}
