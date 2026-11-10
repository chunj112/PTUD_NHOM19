package iuh.se.nhom19.service;

import java.time.*;
import java.util.*;
import iuh.se.nhom19.entity.*;

/** Một nơi duy nhất sinh và kiểm tra slot; UI và Service ghi dùng cùng quy tắc. */
public final class QuyTacLichHen {
    public static final int SO_PHUT = 45;
    private QuyTacLichHen() {}
    public static void kiemTraSlot(PhanCong ca, LocalTime tuGio, LocalTime denGio) {
        if(ca==null || tuGio==null || denGio==null
            || tuGio.isBefore(ca.layGioBatDau())
            || !denGio.equals(tuGio.plusMinutes(SO_PHUT))
            || denGio.isAfter(ca.layGioKetThuc()))
            throw new IllegalArgumentException("Giờ hẹn phải kéo dài 45 phút và nằm hoàn toàn trong ca đã chọn.");
    }
    public static List<KhungGio> taoSlot(PhanCong ca,List<LichHen> danhSach,String maBoQua,Clock dongHo) {
        List<KhungGio> ketQua=new ArrayList<>(); LocalDateTime hienTai=LocalDateTime.now(dongHo);
        int cuoi=ca.layGioKetThuc().toSecondOfDay();
        // Dùng số giây để không quay về 00:00 khi cộng giờ; không nối hai ca hoặc dịch slot theo lịch cũ.
        for(int giay=ca.layGioBatDau().toSecondOfDay();giay+SO_PHUT*60<=cuoi;giay+=SO_PHUT*60) {
            LocalTime dau=LocalTime.ofSecondOfDay(giay),den=dau.plusMinutes(SO_PHUT); String trangThai="TRONG";
            if(LocalDateTime.of(ca.layNgayLam(),dau).isBefore(hienTai))trangThai="QUA_GIO";
            for(LichHen hen:danhSach) {
                if(!hen.layMaNhaSi().equals(ca.layMaNhanVien()) || !hen.layNgayHen().equals(ca.layNgayLam()) || hen.layMaLichHen().equals(maBoQua) || "DA_HUY".equals(hen.layTrangThai()))continue;
                if(hen.layGioBatDau().isBefore(den)&&hen.layGioKetThuc().isAfter(dau)){trangThai="DA_DAT";break;}
            }
            ketQua.add(new KhungGio(dau,den,trangThai));
        }
        return ketQua;
    }
}
