package iuh.se.nhom19.service;

import java.sql.*;
import java.time.*;
import iuh.se.nhom19.auth.*;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.dao.*;
import iuh.se.nhom19.entity.*;

public class TiepNhanService {
    private final KiemTraNghiepVu quyen;
    private final KetNoiCSDL coSoDuLieu;
    private final BenhNhanDAO benhNhanDAO;
    private final LichHenDAO lichHenDAO;
    private final PhieuKhamDAO phieuKhamDAO;
    private final PhanCongDAO phanCongDAO;
    private final Clock dongHo;

    public TiepNhanService(PhienDangNhap phien) {
        this(phien, new KetNoiCSDL(), new BenhNhanDAO(), new LichHenDAO(), new PhieuKhamDAO(), new PhanCongDAO(), Clock.system(ZoneId.of("Asia/Ho_Chi_Minh")));
    }
    public TiepNhanService(PhienDangNhap phien, KetNoiCSDL coSoDuLieu) {
        this(phien, coSoDuLieu, new BenhNhanDAO(), new LichHenDAO(), new PhieuKhamDAO(), new PhanCongDAO(), Clock.system(ZoneId.of("Asia/Ho_Chi_Minh")));
    }
    public TiepNhanService(PhienDangNhap phien, KetNoiCSDL coSoDuLieu, BenhNhanDAO benhNhanDAO, LichHenDAO lichHenDAO, PhieuKhamDAO phieuKhamDAO, PhanCongDAO phanCongDAO, Clock dongHo) {
        quyen = new KiemTraNghiepVu(phien); this.coSoDuLieu = coSoDuLieu; this.benhNhanDAO = benhNhanDAO;
        this.lichHenDAO = lichHenDAO; this.phieuKhamDAO = phieuKhamDAO; this.phanCongDAO = phanCongDAO; this.dongHo = dongHo;
    }

    public PhieuKham tiepNhanCoHen(String maLichHen) throws SQLException {
        return tiepNhanCoHen(maLichHen,null,null);
    }

    public PhieuKham tiepNhanCoHen(String maLichHen,String maBenhNhanDaChon,BenhNhan benhNhanMoi) throws SQLException {
        String maLeTan = quyen.yeuCau(VaiTro.RECEPTIONIST);
        KiemTraNghiepVu.batBuoc(maLichHen, "lịch hẹn", 20);
        try (Connection ketNoi = coSoDuLieu.layKetNoi()) {
            ketNoi.setAutoCommit(false);
            try {
                LichHen lichHen = lichHenDAO.timLichHenTheoMa(ketNoi, maLichHen);
                if (lichHen == null) throw new IllegalArgumentException("Không tìm thấy lịch hẹn.");
                // Thống nhất thứ tự khóa nha sĩ rồi lịch hẹn để các yêu cầu cùng nha sĩ chờ nhau.
                String maNhaSiBanDau = lichHen.layMaNhaSi();
                phanCongDAO.khoaNhaSi(ketNoi, maNhaSiBanDau);
                lichHenDAO.khoaLichHen(ketNoi, maLichHen);
                lichHen = lichHenDAO.timLichHenTheoMa(ketNoi, maLichHen);
                if (!maNhaSiBanDau.equals(lichHen.layMaNhaSi())) throw new IllegalArgumentException("Lịch vừa được đổi nha sĩ. Vui lòng làm mới.");
                if (!"CHO_XAC_NHAN".equals(lichHen.layTrangThai()) && !"DA_XAC_NHAN".equals(lichHen.layTrangThai()))
                    throw new IllegalArgumentException("Lịch hẹn đã hủy hoặc đã tiếp nhận.");
                if (!LocalDate.now(dongHo).equals(lichHen.layNgayHen())) throw new IllegalArgumentException("Chỉ tiếp nhận lịch hẹn trong ngày hôm nay.");
                // Kiểm tra dưới khóa và UNIQUE maLH trong SQL cùng bảo vệ việc nhấn tiếp nhận hai lần.
                if (phieuKhamDAO.kiemTraDaTiepNhan(ketNoi, maLichHen)) throw new IllegalArgumentException("Lịch hẹn này đã có phiếu khám.");
                PhanCong ca = phanCongDAO.kiemTraCaLam(ketNoi, lichHen.layMaNhaSi(), lichHen.layNgayHen(), lichHen.layGioBatDau(), lichHen.layGioKetThuc(), null);
                String maDaLienKet=lichHen.layMaBenhNhan();
                if(maDaLienKet!=null && (benhNhanMoi!=null || (maBenhNhanDaChon!=null&&!maDaLienKet.equals(maBenhNhanDaChon))))
                    throw new IllegalArgumentException("Lịch đã liên kết hồ sơ; không được thay bằng người khác khi tiếp nhận.");
                // Người mới chỉ có bản nháp trên form. Tạo bệnh nhân/HSBA sau khi lịch, ca và trạng thái đã hợp lệ.
                // Tất cả ghi trên cùng Connection: lưu phiếu lỗi thì rollback cả hồ sơ mới và liên kết maBN.
                BenhNhan benhNhan=xacDinhBenhNhan(ketNoi,maDaLienKet!=null?maDaLienKet:maBenhNhanDaChon,benhNhanMoi);
                lichHenDAO.capNhatBenhNhanChoLichHen(ketNoi,maLichHen,benhNhan.layMaBenhNhan());
                PhieuKham phieu = taoPhieu(benhNhan.layMaBenhNhan(), lichHen.layMaNhaSi(), maLeTan, ca.layMaPhong(), lichHen.layLyDoKham());
                phieu.datMaLichHen(maLichHen); phieu.datGhiChu(lichHen.layGhiChu());
                capSoThuTu(ketNoi, phieu, LocalDate.now(dongHo));
                // Hai bảng dùng CHUNG Connection và transaction. Commit chỉ khi cả hai thao tác thành công.
                // Nếu lưu phiếu lỗi sau khi đổi trạng thái lịch, rollback trả lịch về trạng thái cũ.
                lichHenDAO.capNhatTrangThaiLichHen(ketNoi, maLichHen, LocalDate.now(dongHo));
                phieuKhamDAO.taoPhieuKham(ketNoi, phieu);
                quyen.yeuCau(VaiTro.RECEPTIONIST); ketNoi.commit(); return phieu;
            } catch (SQLException | RuntimeException loi) { KiemTraNghiepVu.hoanTac(ketNoi, loi); throw loi; }
        }
    }

    public PhieuKham tiepNhanKhongHen(String maBenhNhan, String maNhaSi, String maPhong, String lyDo) throws SQLException {
        return tiepNhanKhongHen(maBenhNhan,null,maNhaSi,maPhong,lyDo);
    }

    public PhieuKham tiepNhanKhongHen(String maBenhNhan,BenhNhan benhNhanMoi,String maNhaSi,String maPhong,String lyDo) throws SQLException {
        return tiepNhanKhongHen(maBenhNhan,benhNhanMoi,maNhaSi,maPhong,lyDo,null);
    }
    public PhieuKham tiepNhanKhongHen(String maBenhNhan,BenhNhan benhNhanMoi,String maNhaSi,String maPhong,String lyDo,String ghiChu) throws SQLException {
        String maLeTan = quyen.yeuCau(VaiTro.RECEPTIONIST);
        KiemTraNghiepVu.batBuoc(maNhaSi, "nha sĩ", 20);
        KiemTraNghiepVu.batBuoc(maPhong, "phòng", 20);
        if(!"P01".equals(maPhong)&&!"P02".equals(maPhong))throw new IllegalArgumentException("Chỉ tiếp nhận tại phòng P01 hoặc P02.");
        String ghiChuKham=KiemTraNghiepVu.tuyChon(ghiChu,"Ghi chú",1000);
        String lyDoKham = KiemTraNghiepVu.batBuoc(lyDo, "lý do khám", 500);
        try (Connection ketNoi = coSoDuLieu.layKetNoi()) {
            ketNoi.setAutoCommit(false);
            try {
                phanCongDAO.khoaNhaSi(ketNoi, maNhaSi);

                // Lấy thời điểm sau khi chờ khóa, không dùng giờ cũ từ lúc mở modal/yêu cầu.
                LocalDateTime hienTai = LocalDateTime.now(dongHo).withNano(0);

                // Khách không hẹn vào hàng chờ hiện tại, không tạo lịch giả và không đặt ngày tương lai.
                // Kiểm tra thêm một giây để thời điểm đúng cuối ca không bị tính là còn làm việc.
                if (!hienTai.plusSeconds(1).toLocalDate().equals(hienTai.toLocalDate())) throw new IllegalArgumentException("Đã hết ngày tiếp nhận.");
                phanCongDAO.kiemTraCaLam(ketNoi, maNhaSi, hienTai.toLocalDate(), hienTai.toLocalTime(), hienTai.plusSeconds(1).toLocalTime(), maPhong);
                BenhNhan benhNhan=xacDinhBenhNhan(ketNoi,maBenhNhan,benhNhanMoi);
                if(phieuKhamDAO.coPhieuDangHoatDong(ketNoi,benhNhan.layMaBenhNhan()))throw new IllegalArgumentException("Bệnh nhân đang có phiếu chờ/đang khám. Không tiếp nhận trùng.");
                PhieuKham phieu = taoPhieu(benhNhan.layMaBenhNhan(), maNhaSi, maLeTan, maPhong, lyDoKham);
                phieu.datGhiChu(ghiChuKham);
                phieu.datThoiDiemTiepNhan(hienTai);
                capSoThuTu(ketNoi, phieu, hienTai.toLocalDate());
                phieuKhamDAO.taoPhieuKham(ketNoi, phieu);
                quyen.yeuCau(VaiTro.RECEPTIONIST); ketNoi.commit(); return phieu;
            } catch (SQLException | RuntimeException loi) { KiemTraNghiepVu.hoanTac(ketNoi, loi); throw loi; }
        }
    }

    private BenhNhan xacDinhBenhNhan(Connection ketNoi,String maBenhNhan,BenhNhan thongTinMoi) throws SQLException {
        maBenhNhan=KiemTraNghiepVu.tuyChon(maBenhNhan,"Mã bệnh nhân",20);
        if(maBenhNhan!=null&&thongTinMoi!=null)throw new IllegalArgumentException("Chỉ chọn hồ sơ cũ hoặc tạo mới, không chọn cả hai.");
        BenhNhan benhNhan;
        if(maBenhNhan!=null) {
            benhNhanDAO.khoaBenhNhan(ketNoi,maBenhNhan);
            benhNhan=benhNhanDAO.timTheoMa(ketNoi,maBenhNhan);
        } else {
            benhNhan=KiemTraNghiepVu.thongTinBenhNhanMoi(thongTinMoi,LocalDate.now(dongHo));
            benhNhan.datMaBenhNhan(benhNhanDAO.sinhMaBenhNhan(ketNoi)); benhNhanDAO.themBenhNhan(ketNoi,benhNhan);
        }
        HoSoBenhAnDAO hoSoDAO=new HoSoBenhAnDAO();
        if(hoSoDAO.timTheoMaBenhNhan(ketNoi,benhNhan.layMaBenhNhan())==null) {
            // Bệnh nhân cũ có thể thiếu HSBA; chỉ tạo phần còn thiếu, không ghi đè hồ sơ đã có.
            HoSoBenhAn hoSo=new HoSoBenhAn(); hoSo.datMaHoSo(KiemTraNghiepVu.sinhMa("HS"));
            hoSo.datMaBenhNhan(benhNhan.layMaBenhNhan()); hoSoDAO.taoHoSoBenhAn(ketNoi,hoSo);
        }
        return benhNhan;
    }

    private PhieuKham taoPhieu(String maBenhNhan, String maNhaSi, String maLeTan, String maPhong, String lyDo) {
        PhieuKham phieu = new PhieuKham(); phieu.datMaPhieuKham(KiemTraNghiepVu.sinhMa("PK"));
        phieu.datMaBenhNhan(maBenhNhan); phieu.datMaNhaSi(maNhaSi); phieu.datMaLeTan(maLeTan); phieu.datMaPhong(maPhong);
        phieu.datThoiDiemTiepNhan(LocalDateTime.now(dongHo).withNano(0)); phieu.datLyDo(lyDo); phieu.datTrangThaiKham("CHO_KHAM");
        return phieu;
    }

    private void capSoThuTu(Connection ketNoi, PhieuKham phieu, LocalDate ngay) throws SQLException {
        // Bộ đếm và phiếu khám cùng transaction; lỗi lưu bất cứ bước nào sẽ rollback cả STT.
        phieu.datNgayCapSo(ngay);
        phieu.datSoThuTuKham(phieuKhamDAO.capSoThuTuKham(ketNoi, ngay));
    }
}
