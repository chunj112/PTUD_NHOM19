package iuh.se.nhom19.entity;

import java.time.*;

/** Dữ liệu PhanCong; các đối tượng/tên liên quan được đọc bằng JOIN, không phải cột mới. */
public class PhanCong {
    private String maPhanCong;
    private String maNhanVien;
    private String maPhong;
    private LocalDate ngayLam;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;
    private String caLam;
    private String noiDungCongViec;
    private String trangThai;
    private String tenNhaSi;
    private String tenNhanVien;
    private String tenPhong;

    public String layMaPhanCong() { return maPhanCong; }
    public void datMaPhanCong(String maPhanCong) { this.maPhanCong = maPhanCong; }
    public String layMaNhanVien() { return maNhanVien; }
    public void datMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }
    public String layMaPhong() { return maPhong; }
    public void datMaPhong(String maPhong) { this.maPhong = maPhong; }
    public LocalDate layNgayLam() { return ngayLam; }
    public void datNgayLam(LocalDate ngayLam) { this.ngayLam = ngayLam; }
    public LocalTime layGioBatDau() { return gioBatDau; }
    public void datGioBatDau(LocalTime gioBatDau) { this.gioBatDau = gioBatDau; }
    public LocalTime layGioKetThuc() { return gioKetThuc; }
    public void datGioKetThuc(LocalTime gioKetThuc) { this.gioKetThuc = gioKetThuc; }
    public String layCaLam() { return caLam; }
    public void datCaLam(String caLam) { this.caLam = caLam; }
    public String layNoiDungCongViec() { return noiDungCongViec; }
    public void datNoiDungCongViec(String noiDungCongViec) { this.noiDungCongViec = noiDungCongViec; }
    public String layTrangThai() { return trangThai; }
    public void datTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String layTenNhaSi() { return tenNhaSi; }
    public void datTenNhaSi(String tenNhaSi) { this.tenNhaSi = tenNhaSi; }
    public String layTenNhanVien() { return tenNhanVien == null ? tenNhaSi : tenNhanVien; }
    public void datTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }
    public String layTenPhong() { return tenPhong; }
    public void datTenPhong(String tenPhong) { this.tenPhong = tenPhong; }
    private int soCho;
    public int laySoCho() { return soCho; }
    public void datSoCho(int soCho) { this.soCho=soCho; }
    public String layTenCa() { return ("SANG".equals(caLam)?"Ca sáng":"Ca chiều")+" – "+gioBatDau+" đến "+gioKetThuc; }
    @Override public String toString() { return tenNhaSi+" · "+maPhong+" · "+layTenCa()+" · "+soCho+" chờ"; }
}
