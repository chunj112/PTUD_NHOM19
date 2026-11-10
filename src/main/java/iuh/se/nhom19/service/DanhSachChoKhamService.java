package iuh.se.nhom19.service;

import java.sql.*;
import java.util.*;
import iuh.se.nhom19.auth.*;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.dao.PhieuKhamDAO;
import iuh.se.nhom19.entity.PhieuKham;

public class DanhSachChoKhamService {
    private final KiemTraNghiepVu quyen;
    private final KetNoiCSDL coSoDuLieu;
    private final PhieuKhamDAO phieuKhamDAO;
    public DanhSachChoKhamService(PhienDangNhap phien) { this(phien, new KetNoiCSDL(), new PhieuKhamDAO()); }
    public DanhSachChoKhamService(PhienDangNhap phien, KetNoiCSDL coSoDuLieu, PhieuKhamDAO phieuKhamDAO) {
        quyen = new KiemTraNghiepVu(phien); this.coSoDuLieu = coSoDuLieu; this.phieuKhamDAO = phieuKhamDAO;
    }
    public List<PhieuKham> layDanhSachChoKham() throws SQLException {
        // Không nhận mã nha sĩ từ form. Hai nha sĩ dùng cùng màn hình nhưng có kết quả khác nhau theo Session.
        String maNhaSi = quyen.yeuCau(VaiTro.DENTIST);
        try (Connection ketNoi = coSoDuLieu.layKetNoi()) { return phieuKhamDAO.layDanhSachChoKham(ketNoi, maNhaSi); }
    }
    public PhieuKham timPhieuKhamTheoMa(String maPhieuKham) throws SQLException {
        String maNhaSi = quyen.yeuCau(VaiTro.DENTIST);
        KiemTraNghiepVu.batBuoc(maPhieuKham, "mã phiếu khám", 20);
        try (Connection ketNoi = coSoDuLieu.layKetNoi()) {
            PhieuKham phieu = phieuKhamDAO.timPhieuKhamTheoMa(ketNoi, maPhieuKham, maNhaSi);
            if (phieu == null) throw new IllegalArgumentException("Phiếu không còn chờ khám hoặc không thuộc nha sĩ đang đăng nhập.");
            return phieu;
        }
    }

    public PhieuKham batDauKham(String maPhieuKham) throws SQLException {
        String maNhaSi = quyen.yeuCau(VaiTro.DENTIST);
        KiemTraNghiepVu.batBuoc(maPhieuKham,"mã phiếu khám",20);
        try (Connection ketNoi = coSoDuLieu.layKetNoi()) {
            ketNoi.setAutoCommit(false);
            try {
                phieuKhamDAO.batDauKham(ketNoi,maPhieuKham,maNhaSi);
                quyen.yeuCau(VaiTro.DENTIST); ketNoi.commit();
            } catch (SQLException | RuntimeException loi) { KiemTraNghiepVu.hoanTac(ketNoi,loi); throw loi; }
        }
        return timPhieuKhamTheoMa(maPhieuKham);
    }

    public void hoanTatKham(String maPhieuKham,String tinhTrangLamSan,String chanDoan) throws SQLException {
        String maNhaSi = quyen.yeuCau(VaiTro.DENTIST);
        KiemTraNghiepVu.batBuoc(maPhieuKham,"mã phiếu khám",20);
        String tinhTrang=KiemTraNghiepVu.batBuoc(tinhTrangLamSan,"tình trạng lâm sàng",1000);
        String ketLuan=KiemTraNghiepVu.batBuoc(chanDoan,"chẩn đoán",1000);
        try (Connection ketNoi = coSoDuLieu.layKetNoi()) {
            ketNoi.setAutoCommit(false);
            try {
                phieuKhamDAO.hoanTatKham(ketNoi,maPhieuKham,maNhaSi,tinhTrang,ketLuan,
                    java.time.LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")));
                quyen.yeuCau(VaiTro.DENTIST); ketNoi.commit();
            } catch (SQLException | RuntimeException loi) { KiemTraNghiepVu.hoanTac(ketNoi,loi); throw loi; }
        }
    }
}
