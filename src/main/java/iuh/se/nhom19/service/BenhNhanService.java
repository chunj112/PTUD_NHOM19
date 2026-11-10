package iuh.se.nhom19.service;

import java.sql.*;
import java.time.*;
import java.util.*;
import iuh.se.nhom19.auth.*;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.dao.BenhNhanDAO;
import iuh.se.nhom19.entity.*;

public class BenhNhanService {
    private final KiemTraNghiepVu quyen;
    private final KetNoiCSDL coSoDuLieu;
    private final BenhNhanDAO benhNhanDAO;
    private final Clock dongHo;

    public BenhNhanService(PhienDangNhap phien) {
        this(phien, new KetNoiCSDL(), new BenhNhanDAO(), Clock.system(ZoneId.of("Asia/Ho_Chi_Minh")));
    }
    public BenhNhanService(PhienDangNhap phien, KetNoiCSDL coSoDuLieu) {
        this(phien, coSoDuLieu, new BenhNhanDAO(), Clock.system(ZoneId.of("Asia/Ho_Chi_Minh")));
    }
    public BenhNhanService(PhienDangNhap phien, KetNoiCSDL coSoDuLieu, BenhNhanDAO benhNhanDAO, Clock dongHo) {
        quyen = new KiemTraNghiepVu(phien); this.coSoDuLieu = coSoDuLieu; this.benhNhanDAO = benhNhanDAO; this.dongHo = dongHo;
    }

    public List<BenhNhan> timBenhNhan(String tuKhoa) throws SQLException {
        quyen.yeuCau(VaiTro.RECEPTIONIST);
        String noiDung = KiemTraNghiepVu.soDienThoaiTimKiem(tuKhoa);
        // Connection chỉ sống trong một thao tác, được đóng cả khi truy vấn lỗi.
        try (Connection ketNoi = coSoDuLieu.layKetNoi()) { return benhNhanDAO.timBenhNhan(ketNoi, noiDung); }
    }

    public BenhNhan timTheoMa(String maBenhNhan) throws SQLException {
        quyen.yeuCau(VaiTro.RECEPTIONIST);
        KiemTraNghiepVu.batBuoc(maBenhNhan,"mã bệnh nhân",20);
        try(Connection ketNoi=coSoDuLieu.layKetNoi()) {
            BenhNhan benhNhan=benhNhanDAO.timTheoMa(ketNoi,maBenhNhan);
            if(benhNhan==null)throw new IllegalArgumentException("Bệnh nhân không tồn tại.");
            return benhNhan;
        }
    }

    public BenhNhan themBenhNhan(String hoTen, Boolean nam, LocalDate ngaySinh, String soDienThoai, String diaChi, String diUng) throws SQLException {
        quyen.yeuCau(VaiTro.RECEPTIONIST);
        BenhNhan benhNhan = new BenhNhan();
        benhNhan.datHoTen(KiemTraNghiepVu.batBuoc(hoTen, "họ tên", 120));
        if (nam == null) throw new IllegalArgumentException("Vui lòng chọn giới tính.");
        if (ngaySinh == null || ngaySinh.isBefore(LocalDate.of(1900, 1, 1)) || ngaySinh.isAfter(LocalDate.now(dongHo)))
            throw new IllegalArgumentException("Ngày sinh phải từ 01/01/1900 đến hôm nay.");
        benhNhan.datNam(nam); benhNhan.datNgaySinh(ngaySinh);
        benhNhan.datSoDienThoai(KiemTraNghiepVu.soDienThoai(soDienThoai));
        benhNhan.datDiaChi(KiemTraNghiepVu.tuyChon(diaChi, "Địa chỉ", 255));
        try (Connection ketNoi = coSoDuLieu.layKetNoi()) {
            ketNoi.setAutoCommit(false);
            try {
                benhNhan.datMaBenhNhan(benhNhanDAO.sinhMaBenhNhan(ketNoi));
                HoSoBenhAn hoSo = new HoSoBenhAn(); hoSo.datMaHoSo(KiemTraNghiepVu.sinhMa("HS"));
                hoSo.datMaBenhNhan(benhNhan.layMaBenhNhan()); hoSo.datTienSuDiUng(KiemTraNghiepVu.tuyChon(diUng, "Dị ứng", 1000));
                // Bệnh nhân và hồ sơ phải cùng tồn tại. Không tạo phiếu khám khi chỉ lưu thông tin.
                benhNhanDAO.themBenhNhan(ketNoi, benhNhan); benhNhanDAO.themHoSo(ketNoi, hoSo);
                quyen.yeuCau(VaiTro.RECEPTIONIST); ketNoi.commit(); return benhNhan;
            } catch (SQLException | RuntimeException loi) { KiemTraNghiepVu.hoanTac(ketNoi, loi); throw loi; }
        }
    }
}
