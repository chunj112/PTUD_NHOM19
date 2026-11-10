package iuh.se.nhom19.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import iuh.se.nhom19.auth.PhienDangNhap;
import iuh.se.nhom19.auth.VaiTro;
import iuh.se.nhom19.dao.PhieuKhamDAO;
import iuh.se.nhom19.dao.PhanCongDAO;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.entity.PhieuKham;

/** Chỉ đọc hàng chờ thuộc phòng/ca mà Y tá hiện tại được phân công. */
public class DanhSachChoYTaService {
    private static final Set<String> TRANG_THAI_HOP_LE = Set.of("CHO_KHAM", "DANG_KHAM", "DA_KHAM");
    private final KiemTraNghiepVu quyen;
    private final KetNoiCSDL coSoDuLieu;
    private final PhieuKhamDAO phieuKhamDAO;
    private final PhanCongDAO phanCongDAO;

    public DanhSachChoYTaService(PhienDangNhap phien) {
        this(phien, new KetNoiCSDL(), new PhieuKhamDAO(), new PhanCongDAO());
    }

    public DanhSachChoYTaService(PhienDangNhap phien, KetNoiCSDL coSoDuLieu, PhieuKhamDAO phieuKhamDAO) {
        this(phien, coSoDuLieu, phieuKhamDAO, new PhanCongDAO());
    }

    public DanhSachChoYTaService(PhienDangNhap phien, KetNoiCSDL coSoDuLieu, PhieuKhamDAO phieuKhamDAO, PhanCongDAO phanCongDAO) {
        quyen = new KiemTraNghiepVu(phien);
        this.coSoDuLieu = java.util.Objects.requireNonNull(coSoDuLieu);
        this.phieuKhamDAO = java.util.Objects.requireNonNull(phieuKhamDAO);
        this.phanCongDAO = java.util.Objects.requireNonNull(phanCongDAO);
    }

    public List<String> layPhongDuocPhanCong(LocalDate ngay) throws SQLException {
        String maYTa = quyen.yeuCau(VaiTro.NURSE);
        if (ngay == null) throw new IllegalArgumentException("Vui lòng chọn ngày.");
        try (Connection ketNoi = coSoDuLieu.layKetNoi()) {
            List<String> phong = phieuKhamDAO.layPhongDuocPhanCongYTa(ketNoi, maYTa, ngay);
            quyen.yeuCau(VaiTro.NURSE);
            return phong;
        }
    }

    public List<iuh.se.nhom19.entity.PhanCong> layLichLamViec(LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        String maYTa = quyen.yeuCau(VaiTro.NURSE);
        if (tuNgay == null || denNgay == null || denNgay.isBefore(tuNgay))
            throw new IllegalArgumentException("Khoảng ngày xem lịch không hợp lệ.");
        try (Connection ketNoi = coSoDuLieu.layKetNoi()) {
            List<iuh.se.nhom19.entity.PhanCong> lich = phanCongDAO.layLichCaNhan(ketNoi, maYTa, tuNgay, denNgay);
            quyen.yeuCau(VaiTro.NURSE);
            return lich;
        }
    }

    public List<PhieuKham> layDanhSach(LocalDate ngay, String maPhong, String trangThai) throws SQLException {
        String maYTa = quyen.yeuCau(VaiTro.NURSE);
        if (ngay == null) throw new IllegalArgumentException("Vui lòng chọn ngày.");
        if (trangThai != null && !TRANG_THAI_HOP_LE.contains(trangThai))
            throw new IllegalArgumentException("Trạng thái lọc không hợp lệ.");
        try (Connection ketNoi = coSoDuLieu.layKetNoi()) {
            List<String> phongDuocCap = phieuKhamDAO.layPhongDuocPhanCongYTa(ketNoi, maYTa, ngay);
            if (maPhong != null && !phongDuocCap.contains(maPhong))
                throw new IllegalArgumentException("Bạn không được phân công tại phòng đã chọn trong ngày này.");
            List<PhieuKham> ketQua = phieuKhamDAO.layDanhSachChoYTa(ketNoi, maYTa, ngay, maPhong, trangThai);
            quyen.yeuCau(VaiTro.NURSE);
            return ketQua;
        }
    }
}
