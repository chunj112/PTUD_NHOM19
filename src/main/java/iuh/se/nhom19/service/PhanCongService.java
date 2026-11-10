package iuh.se.nhom19.service;

import java.sql.*;
import java.time.*;
import java.util.*;
import iuh.se.nhom19.auth.*;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.dao.*;
import iuh.se.nhom19.entity.*;

/** Chỉ đọc ca/khung trống phục vụ đặt lịch, không tạo hệ thống phân công khác. */
public class PhanCongService {
    private final KiemTraNghiepVu quyen;
    private final KetNoiCSDL coSoDuLieu;
    public PhanCongService(PhienDangNhap phien, KetNoiCSDL coSoDuLieu) { quyen=new KiemTraNghiepVu(phien); this.coSoDuLieu=coSoDuLieu; }
    public List<PhanCong> layDanhSachNhaSi(LocalDate ngay) throws SQLException {
        quyen.yeuCau(VaiTro.RECEPTIONIST);
        if(ngay==null)throw new IllegalArgumentException("Vui lòng chọn ngày.");
        try(Connection ketNoi=coSoDuLieu.layKetNoi()){return new PhanCongDAO().layDanhSachNhaSiDangLamViec(ketNoi,ngay);}
    }
    public List<KhungGio> layKhungGioKhaDung(PhanCong caDaChon, String maLichBoQua) throws SQLException {
        quyen.yeuCau(VaiTro.RECEPTIONIST);
        if(caDaChon==null)throw new IllegalArgumentException("Chọn ca nha sĩ.");
        try(Connection ketNoi=coSoDuLieu.layKetNoi()) {
            PhanCong ca=null;
            for(PhanCong hienTai:new PhanCongDAO().layDanhSachNhaSiDangLamViec(ketNoi,caDaChon.layNgayLam()))
                if(hienTai.layMaPhanCong().equals(caDaChon.layMaPhanCong()))ca=hienTai;
            if(ca==null)throw new IllegalArgumentException("Ca đã thay đổi. Vui lòng tải lại.");
            List<LichHen> lich=new LichHenDAO().layDanhSachLichHen(ketNoi,ca.layNgayLam(),ca.layNgayLam());
            return QuyTacLichHen.taoSlot(ca,lich,maLichBoQua,Clock.system(ZoneId.of("Asia/Ho_Chi_Minh")));
        }
    }

    public String layTomTatCa(LichHen lich) throws SQLException {
        // Đọc ca thật để hiển thị, không suy ra giờ ca từ khung 45 phút của lịch hẹn.
        for(PhanCong ca:layDanhSachNhaSi(lich.layNgayHen())) {
            if(ca.layMaNhanVien().equals(lich.layMaNhaSi()) && !ca.layGioBatDau().isAfter(lich.layGioBatDau())
                && !ca.layGioKetThuc().isBefore(lich.layGioKetThuc()))return ca.layTenCa()+" · "+ca.layMaPhong();
        }
        return "Không còn ca phân công phù hợp. Cần kiểm tra trước khi tiếp nhận.";
    }
}
