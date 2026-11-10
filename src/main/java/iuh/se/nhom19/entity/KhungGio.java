package iuh.se.nhom19.entity;

import java.time.LocalTime;

/** Khoảng trống tính từ ca và lịch đã lưu; không phải bản ghi PhanCong giả. */
public class KhungGio {
    private final LocalTime gioBatDau, gioKetThuc;
    private final String trangThai;
    public KhungGio(LocalTime gioBatDau, LocalTime gioKetThuc) { this(gioBatDau,gioKetThuc,"TRONG"); }
    public KhungGio(LocalTime gioBatDau, LocalTime gioKetThuc,String trangThai) { this.gioBatDau=gioBatDau; this.gioKetThuc=gioKetThuc; this.trangThai=trangThai; }
    public String layTrangThai() { return trangThai; }
    public boolean conTrong() { return "TRONG".equals(trangThai); }
    public LocalTime layGioBatDau() { return gioBatDau; }
    public LocalTime layGioKetThuc() { return gioKetThuc; }
    @Override public String toString() { return gioBatDau + "–" + gioKetThuc; }
}
