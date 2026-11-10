package iuh.se.nhom19.service;

import java.sql.*;
import java.time.*;
import java.util.*;
import iuh.se.nhom19.auth.*;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.dao.*;
import iuh.se.nhom19.entity.*;

public class LichHenService {
    private final KiemTraNghiepVu quyen;
    private final KetNoiCSDL coSoDuLieu;
    private final BenhNhanDAO benhNhanDAO;
    private final LichHenDAO lichHenDAO;
    private final PhanCongDAO phanCongDAO;
    private final Clock dongHo;

    public LichHenService(PhienDangNhap phien) {
        this(phien, new KetNoiCSDL(), new BenhNhanDAO(), new LichHenDAO(), new PhanCongDAO(), Clock.system(ZoneId.of("Asia/Ho_Chi_Minh")));
    }
    public LichHenService(PhienDangNhap phien, KetNoiCSDL coSoDuLieu) {
        this(phien, coSoDuLieu, new BenhNhanDAO(), new LichHenDAO(), new PhanCongDAO(), Clock.system(ZoneId.of("Asia/Ho_Chi_Minh")));
    }
    public LichHenService(PhienDangNhap phien, KetNoiCSDL coSoDuLieu, BenhNhanDAO benhNhanDAO, LichHenDAO lichHenDAO, PhanCongDAO phanCongDAO, Clock dongHo) {
        quyen = new KiemTraNghiepVu(phien); this.coSoDuLieu = coSoDuLieu; this.benhNhanDAO = benhNhanDAO;
        this.lichHenDAO = lichHenDAO; this.phanCongDAO = phanCongDAO; this.dongHo = dongHo;
    }
    public List<LichHen> layLichHenTheoNgay(LocalDate ngay) throws SQLException { return layDanhSachLichHen(ngay, ngay); }
    public List<LichHen> layDanhSachLichHen(LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        quyen.yeuCau(VaiTro.RECEPTIONIST);
        if (tuNgay == null || denNgay == null || denNgay.isBefore(tuNgay) || denNgay.isAfter(tuNgay.plusDays(31)))
            throw new IllegalArgumentException("Chọn khoảng ngày hợp lệ, tối đa 32 ngày.");
        try (Connection ketNoi = coSoDuLieu.layKetNoi()) { return lichHenDAO.layDanhSachLichHen(ketNoi, tuNgay, denNgay); }
    }
    public List<LichHen> layLichHenTuongLaiDaDen() throws SQLException {
        quyen.yeuCau(VaiTro.RECEPTIONIST);
        LocalDate homNay = LocalDate.now(dongHo);
        try (Connection ketNoi = coSoDuLieu.layKetNoi()) { return lichHenDAO.layLichTuongLaiDaDen(ketNoi, homNay); }
    }
    public List<PhanCong> layDanhSachNhaSi(LocalDate ngay) throws SQLException {
        quyen.yeuCau(VaiTro.RECEPTIONIST);
        if (ngay == null) throw new IllegalArgumentException("Vui lòng chọn ngày.");
        try (Connection ketNoi = coSoDuLieu.layKetNoi()) { return phanCongDAO.layDanhSachNhaSiDangLamViec(ketNoi, ngay); }
    }

    public LichHen timLichHenTheoMa(String maLichHen) throws SQLException {
        quyen.yeuCau(VaiTro.RECEPTIONIST);
        try(Connection ketNoi=coSoDuLieu.layKetNoi()) {
            LichHen lich=lichHenDAO.timLichHenTheoMa(ketNoi,maLichHen);
            if(lich==null)throw new IllegalArgumentException("Không tìm thấy lịch hẹn.");
            return lich;
        }
    }

    // Giữ lời gọi đã có cho bệnh nhân cũ; tất cả vẫn đi qua một hàm lưu theo Phương án A.
    public LichHen datLichHen(String maBenhNhan,String maNhaSi,LocalDate ngay,LocalTime tuGio,LocalTime denGio,String lyDo,String ghiChu) throws SQLException {
        return datLichHen(maBenhNhan,null,null,maNhaSi,ngay,tuGio,denGio,lyDo,ghiChu);
    }
    public LichHen datLichHen(String maBenhNhan,String hoTen,String soDienThoai,String maNhaSi,LocalDate ngay,LocalTime tuGio,LocalTime denGio,String lyDo,String ghiChu) throws SQLException {
        return luuLichHen(null,maBenhNhan,hoTen,soDienThoai,maNhaSi,ngay,tuGio,denGio,lyDo,ghiChu);
    }
    public LichHen suaLichHen(String maLichHen,String maBenhNhan,String hoTen,String soDienThoai,String maNhaSi,LocalDate ngay,LocalTime tuGio,LocalTime denGio,String lyDo,String ghiChu) throws SQLException {
        KiemTraNghiepVu.batBuoc(maLichHen,"mã lịch hẹn",20);
        return luuLichHen(maLichHen,maBenhNhan,hoTen,soDienThoai,maNhaSi,ngay,tuGio,denGio,lyDo,ghiChu);
    }
    private LichHen luuLichHen(String maLichHen,String maBenhNhan,String hoTen,String soDienThoai,String maNhaSi,LocalDate ngay,LocalTime tuGio,LocalTime denGio,String lyDo,String ghiChu) throws SQLException {
        quyen.yeuCau(VaiTro.RECEPTIONIST);
        KiemTraNghiepVu.batBuoc(maNhaSi,"nha sĩ",20);
        if(ngay==null||tuGio==null||denGio==null||!tuGio.isBefore(denGio))throw new IllegalArgumentException("Giờ bắt đầu phải trước giờ kết thúc.");
        if(LocalDateTime.of(ngay,tuGio).isBefore(LocalDateTime.now(dongHo)))throw new IllegalArgumentException("Không đặt lịch trong quá khứ.");
        LichHen lich=new LichHen();
        // Lịch mới lấy mã theo ngày hẹn trong transaction; lịch sửa giữ nguyên khóa hiện có.
        if(maLichHen!=null)lich.datMaLichHen(maLichHen);
        lich.datMaBenhNhan(KiemTraNghiepVu.tuyChon(maBenhNhan,"Mã bệnh nhân",20)); lich.datMaNhaSi(maNhaSi);
        lich.datNgayHen(ngay); lich.datGioBatDau(tuGio); lich.datGioKetThuc(denGio);
        lich.datLyDoKham(KiemTraNghiepVu.tuyChon(lyDo,"Lý do khám",500)); lich.datGhiChu(KiemTraNghiepVu.tuyChon(ghiChu,"Ghi chú",1000));
        lich.datTrangThai("CHO_XAC_NHAN");
        if(lich.layMaBenhNhan()==null) {
            lich.datHoTenNguoiHen(KiemTraNghiepVu.batBuoc(hoTen,"họ tên người đặt lịch",120));
            lich.datSoDienThoaiLienHe(KiemTraNghiepVu.soDienThoai(soDienThoai));
        }
        try(Connection ketNoi=coSoDuLieu.layKetNoi()) {
            ketNoi.setAutoCommit(false);
            try {
                if(maLichHen==null)phanCongDAO.khoaNhaSi(ketNoi,maNhaSi);
                else lich.datTrangThai(khoaLichCoTheSua(ketNoi,maLichHen,maNhaSi).layTrangThai());
                if(lich.layMaBenhNhan()!=null) {
                    BenhNhan cu=benhNhanDAO.timTheoMa(ketNoi,lich.layMaBenhNhan());
                    if(cu==null)throw new IllegalArgumentException("Bệnh nhân không tồn tại.");
                    if(hoTen!=null && (!cu.layHoTen().equals(hoTen.trim()) || !cu.laySoDienThoai().equals(soDienThoai==null?null:soDienThoai.trim())))
                        throw new IllegalArgumentException("Thông tin đã khác hồ sơ được chọn. Xác minh lại hoặc bỏ liên kết.");
                    lich.datHoTenNguoiHen(cu.layHoTen()); lich.datSoDienThoaiLienHe(cu.laySoDienThoai());
                }
                PhanCong ca=phanCongDAO.kiemTraCaLam(ketNoi,maNhaSi,ngay,tuGio,denGio,null);
                QuyTacLichHen.kiemTraSlot(ca,tuGio,denGio);
                // Có thể đã chờ khóa của lễ tân khác; kiểm tra lại đồng hồ ngay trước khi ghi.
                if(LocalDateTime.of(ngay,tuGio).isBefore(LocalDateTime.now(dongHo)))throw new IllegalArgumentException("Khung giờ vừa qua. Vui lòng chọn lại.");
                if(lichHenDAO.kiemTraTrungLich(ketNoi,maNhaSi,ngay,tuGio,denGio,maLichHen))throw new IllegalArgumentException("Nha sĩ đã có lịch hẹn trùng giờ.");
                // Người mới chỉ lưu tên/SĐT trong LichHen, maBN NULL. Không gọi INSERT BenhNhan/HSBA/PhieuKham.
                if(maLichHen==null) {
                    lich.datMaLichHen(lichHenDAO.sinhMaLichHen(ketNoi,ngay));
                    lichHenDAO.themLichHen(ketNoi,lich);
                } else lichHenDAO.suaLichHen(ketNoi,lich);
                quyen.yeuCau(VaiTro.RECEPTIONIST); ketNoi.commit(); return lich;
            }catch(SQLException|RuntimeException loi){KiemTraNghiepVu.hoanTac(ketNoi,loi);throw loi;}
        }
    }
    public void huyLichHen(String maLichHen) throws SQLException {
        huyLichHen(maLichHen,null);
    }
    public void huyLichHen(String maLichHen,String lyDoHuy) throws SQLException {
        quyen.yeuCau(VaiTro.RECEPTIONIST);
        String lyDo=lyDoHuy==null?null:KiemTraNghiepVu.batBuoc(lyDoHuy,"lý do hủy",250);
        try(Connection ketNoi=coSoDuLieu.layKetNoi()) {
            ketNoi.setAutoCommit(false);
            try {
                LichHen lich=khoaLichCoTheSua(ketNoi,maLichHen,null);
                if(lyDo==null)lichHenDAO.huyLichHen(ketNoi,maLichHen);
                else {
                    // Schema chưa có cột lý do hủy riêng: nối vào ghi chú, không ghi đè/mất ghi chú cũ.
                    String ghiChu=(lich.layGhiChu()==null?"":lich.layGhiChu()+"\n")+"Lý do hủy: "+lyDo;
                    if(ghiChu.length()>1000)throw new IllegalArgumentException("Ghi chú và lý do hủy vượt 1000 ký tự. Rút gọn ghi chú/lý do trước khi hủy.");
                    lichHenDAO.huyLichHen(ketNoi,maLichHen,ghiChu);
                }
                quyen.yeuCau(VaiTro.RECEPTIONIST); ketNoi.commit();
            }catch(SQLException|RuntimeException loi){KiemTraNghiepVu.hoanTac(ketNoi,loi);throw loi;}
        }
    }
    private LichHen khoaLichCoTheSua(Connection ketNoi,String maLichHen,String maNhaSiMoi) throws SQLException {
        LichHen lich=lichHenDAO.timLichHenTheoMa(ketNoi,maLichHen);
        if(lich==null)throw new IllegalArgumentException("Không tìm thấy lịch hẹn.");
        String maCu=lich.layMaNhaSi();
        // Khi đổi nha sĩ, khóa cả hai theo thứ tự mã cố định để giảm deadlock giữa hai lần đổi ngược nhau.
        if(maNhaSiMoi!=null&&maNhaSiMoi.compareTo(maCu)<0)phanCongDAO.khoaNhaSi(ketNoi,maNhaSiMoi);
        phanCongDAO.khoaNhaSi(ketNoi,maCu);
        if(maNhaSiMoi!=null&&maNhaSiMoi.compareTo(maCu)>0)phanCongDAO.khoaNhaSi(ketNoi,maNhaSiMoi);
        lichHenDAO.khoaLichHen(ketNoi,maLichHen); lich=lichHenDAO.timLichHenTheoMa(ketNoi,maLichHen);
        if(!maCu.equals(lich.layMaNhaSi()))throw new IllegalArgumentException("Lịch vừa đổi nha sĩ. Vui lòng làm mới.");
        if(!"CHO_XAC_NHAN".equals(lich.layTrangThai())&&!"DA_XAC_NHAN".equals(lich.layTrangThai()))throw new IllegalArgumentException("Lịch đã hủy/đã tiếp nhận, không được sửa hoặc hủy.");
        return lich;
    }
}
