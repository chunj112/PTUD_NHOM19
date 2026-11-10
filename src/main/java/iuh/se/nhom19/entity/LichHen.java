package iuh.se.nhom19.entity;

import java.time.*;

/** Dữ liệu LichHen; các đối tượng/tên liên quan được đọc bằng JOIN, không phải cột mới. */
public class LichHen {
    // Thông tin liên hệ tại lúc đặt lịch; maBenhNhan/benhNhan có thể NULL khi người đó chưa đến.
    private String hoTenNguoiHen, soDienThoaiLienHe;
    public String layHoTenNguoiHen() { return hoTenNguoiHen; }
    public void datHoTenNguoiHen(String hoTenNguoiHen) { this.hoTenNguoiHen = hoTenNguoiHen; }
    public String laySoDienThoaiLienHe() { return soDienThoaiLienHe; }
    public void datSoDienThoaiLienHe(String soDienThoaiLienHe) { this.soDienThoaiLienHe = soDienThoaiLienHe; }
    private String maLichHen;
    private String maBenhNhan;
    private String maNhaSi;
    private LocalDate ngayHen;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;
    private String lyDoKham;
    private String ghiChu;
    private String trangThai;
    private BenhNhan benhNhan;
    private String tenNhaSi;
    private String maPhong;

    public String layMaLichHen() { return maLichHen; }
    public void datMaLichHen(String maLichHen) { this.maLichHen = maLichHen; }
    public String layMaBenhNhan() { return maBenhNhan; }
    public void datMaBenhNhan(String maBenhNhan) { this.maBenhNhan = maBenhNhan; }
    public String layMaNhaSi() { return maNhaSi; }
    public void datMaNhaSi(String maNhaSi) { this.maNhaSi = maNhaSi; }
    public LocalDate layNgayHen() { return ngayHen; }
    public void datNgayHen(LocalDate ngayHen) { this.ngayHen = ngayHen; }
    public LocalTime layGioBatDau() { return gioBatDau; }
    public void datGioBatDau(LocalTime gioBatDau) { this.gioBatDau = gioBatDau; }
    public LocalTime layGioKetThuc() { return gioKetThuc; }
    public void datGioKetThuc(LocalTime gioKetThuc) { this.gioKetThuc = gioKetThuc; }
    public String layLyDoKham() { return lyDoKham; }
    public void datLyDoKham(String lyDoKham) { this.lyDoKham = lyDoKham; }
    public String layGhiChu() { return ghiChu; }
    public void datGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public String layTrangThai() { return trangThai; }
    public void datTrangThai(String trangThai) { this.trangThai = trangThai; }
    public BenhNhan layBenhNhan() { return benhNhan; }
    public void datBenhNhan(BenhNhan benhNhan) { this.benhNhan = benhNhan; }
    public String layTenNhaSi() { return tenNhaSi; }
    public void datTenNhaSi(String tenNhaSi) { this.tenNhaSi = tenNhaSi; }
    public String layMaPhong() { return maPhong; }
    public void datMaPhong(String maPhong) { this.maPhong = maPhong; }
}
