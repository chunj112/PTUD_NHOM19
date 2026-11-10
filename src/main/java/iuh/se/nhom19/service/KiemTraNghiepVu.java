package iuh.se.nhom19.service;

import java.sql.*;
import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import iuh.se.nhom19.entity.BenhNhan;
import iuh.se.nhom19.auth.*;

/** Kiểm tra dùng chung nhỏ gọn; không giữ Connection hoặc dữ liệu mật khẩu. */
public final class KiemTraNghiepVu {
    private static final DateTimeFormatter DINH_DANG_NGAY_SINH = DateTimeFormatter.ofPattern("dd/MM/uuuu")
        .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DINH_DANG_GIO_HEN = DateTimeFormatter.ofPattern("HH:mm")
        .withResolverStyle(ResolverStyle.STRICT);
    private final PhienDangNhap phien;
    private final long phienBan;
    private final String maNhanVien;

    public KiemTraNghiepVu(PhienDangNhap phien) {
        if (phien == null || !phien.dangHoatDong()) throw new IllegalStateException("Vui lòng đăng nhập.");
        this.phien = phien;
        phienBan = phien.layPhienBan();
        maNhanVien = phien.layMaNhanVien();
    }

    public String yeuCau(VaiTro vaiTro) {
        // Giữ phiên bản lúc tạo Service: logout/login khác không làm Service cũ đổi danh tính.
        // Mã lễ tân/nha sĩ luôn lấy từ phiên đã xác thực, không lấy từ TextField.
        if (phien.yeuCauVaiTro(phienBan) != vaiTro) throw new IllegalStateException("Bạn không có quyền thực hiện thao tác này.");
        return maNhanVien;
    }

    public static String batBuoc(String giaTri, String tenTruong, int doDai) {
        if (giaTri == null || giaTri.isBlank()) throw new IllegalArgumentException("Vui lòng nhập " + tenTruong + ".");
        return tuyChon(giaTri, tenTruong, doDai);
    }

    public static String tuyChon(String giaTri, String tenTruong, int doDai) {
        if (giaTri == null || giaTri.isBlank()) return null;
        String noiDung = giaTri.trim();
        if (noiDung.length() > doDai) throw new IllegalArgumentException(tenTruong + " tối đa " + doDai + " ký tự.");
        return noiDung;
    }

    public static String sinhMa(String tienTo) {
        // Khóa VARCHAR(20); không dùng COUNT/MAX + 1 vì hai máy có thể sinh cùng số.
        return tienTo + UUID.randomUUID().toString().replace("-", "").substring(0, 18).toUpperCase(java.util.Locale.ROOT);
    }

    public static String soDienThoai(String giaTri) {
        if (giaTri == null || !giaTri.trim().matches("0[0-9]{9}")) throw new IllegalArgumentException("Số điện thoại phải bắt đầu bằng 0 và gồm đúng 10 chữ số.");
        return giaTri.trim();
    }

    /** Tra cứu trên UI chỉ nhận các chữ số SĐT, chấp nhận nhập dần từ 3 chữ số. */
    public static String soDienThoaiTimKiem(String giaTri) {
        if (giaTri == null || !giaTri.trim().matches("[0-9]{3,10}"))
            throw new IllegalArgumentException("Nhập từ 3 đến 10 chữ số của số điện thoại.");
        return giaTri.trim();
    }

    /** Đọc ngày sinh nhập trực tiếp, không tự đổi chuỗi khi người dùng đang gõ. */
    public static LocalDate docNgaySinh(String giaTri, LocalDate homNay) {
        try {
            LocalDate ngay = LocalDate.parse(giaTri == null ? "" : giaTri.trim(), DINH_DANG_NGAY_SINH);
            if (ngay.isBefore(LocalDate.of(1900, 1, 1)) || ngay.isAfter(homNay))
                throw new IllegalArgumentException("Ngày sinh phải từ 01/01/1900 đến hôm nay.");
            return ngay;
        } catch (DateTimeParseException loi) {
            throw new IllegalArgumentException("Ngày sinh phải đúng định dạng dd/MM/yyyy và là ngày hợp lệ.");
        }
    }

    /** Đọc giờ bắt đầu theo đồng hồ 24 giờ; thời lượng 45 phút được tính riêng ở luồng đặt lịch. */
    public static LocalTime docGioHen(String giaTri) {
        try {
            return LocalTime.parse(giaTri == null ? "" : giaTri.trim(), DINH_DANG_GIO_HEN);
        } catch (DateTimeParseException loi) {
            throw new IllegalArgumentException("Giờ hẹn phải đúng định dạng HH:mm, ví dụ 08:30.");
        }
    }

    public static String hienNgaySinh(LocalDate ngay) {
        return ngay == null ? "" : DINH_DANG_NGAY_SINH.format(ngay);
    }

    public static BenhNhan thongTinBenhNhanMoi(BenhNhan thongTin, LocalDate homNay) {
        if (thongTin == null) throw new IllegalArgumentException("Vui lòng nhập thông tin bệnh nhân mới.");
        if (thongTin.layNam() == null) throw new IllegalArgumentException("Vui lòng chọn giới tính.");
        if (thongTin.layNgaySinh() == null || thongTin.layNgaySinh().isBefore(LocalDate.of(1900,1,1)) || thongTin.layNgaySinh().isAfter(homNay))
            throw new IllegalArgumentException("Ngày sinh phải từ 01/01/1900 đến hôm nay.");
        // Chỉ chuẩn hóa bản nháp, chưa sinh mã/INSERT. Không tự gán ngày sinh hoặc giới tính giả.
        BenhNhan benhNhan = new BenhNhan();
        benhNhan.datHoTen(batBuoc(thongTin.layHoTen(), "họ tên", 120)); benhNhan.datNam(thongTin.layNam());
        benhNhan.datNgaySinh(thongTin.layNgaySinh()); benhNhan.datSoDienThoai(soDienThoai(thongTin.laySoDienThoai()));
        benhNhan.datDiaChi(tuyChon(thongTin.layDiaChi(), "Địa chỉ", 255)); return benhNhan;
    }

    public static void hoanTac(Connection ketNoi, Exception loi) {
        try { ketNoi.rollback(); } catch (SQLException loiHoanTac) { loi.addSuppressed(loiHoanTac); }
    }
}
