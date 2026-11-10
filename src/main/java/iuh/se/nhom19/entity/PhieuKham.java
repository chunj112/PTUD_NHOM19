package iuh.se.nhom19.entity;

import java.time.*;

/** Dữ liệu PhieuKham; các đối tượng/tên liên quan được đọc bằng JOIN, không phải cột mới. */
public class PhieuKham {
    private String maPhieuKham;
    private String maBenhNhan;
    private String maNhaSi;
    private String maLeTan;
    private String maLichHen;
    private String maPhong;
    private LocalDateTime thoiDiemTiepNhan;
    private String tinhTrangLamSan;
    private String chanDoan;
    private LocalDate ngayKham;
    private LocalDate ngayCapSo;
    private Integer soThuTuKham;
    private String lyDo;
    private String ghiChu;
    private String trangThaiKham;
    private BenhNhan benhNhan;
    private String tenNhaSi;

    public String layMaPhieuKham() { return maPhieuKham; }
    public void datMaPhieuKham(String maPhieuKham) { this.maPhieuKham = maPhieuKham; }
    public String layMaBenhNhan() { return maBenhNhan; }
    public void datMaBenhNhan(String maBenhNhan) { this.maBenhNhan = maBenhNhan; }
    public String layMaNhaSi() { return maNhaSi; }
    public void datMaNhaSi(String maNhaSi) { this.maNhaSi = maNhaSi; }
    public String layMaLeTan() { return maLeTan; }
    public void datMaLeTan(String maLeTan) { this.maLeTan = maLeTan; }
    public String layMaLichHen() { return maLichHen; }
    public void datMaLichHen(String maLichHen) { this.maLichHen = maLichHen; }
    public String layMaPhong() { return maPhong; }
    public void datMaPhong(String maPhong) { this.maPhong = maPhong; }
    public LocalDateTime layThoiDiemTiepNhan() { return thoiDiemTiepNhan; }
    public void datThoiDiemTiepNhan(LocalDateTime thoiDiemTiepNhan) { this.thoiDiemTiepNhan = thoiDiemTiepNhan; }
    public String layTinhTrangLamSan() { return tinhTrangLamSan; }
    public void datTinhTrangLamSan(String tinhTrangLamSan) { this.tinhTrangLamSan = tinhTrangLamSan; }
    public String layChanDoan() { return chanDoan; }
    public void datChanDoan(String chanDoan) { this.chanDoan = chanDoan; }
    public LocalDate layNgayKham() { return ngayKham; }
    public void datNgayKham(LocalDate ngayKham) { this.ngayKham = ngayKham; }
    public LocalDate layNgayCapSo() { return ngayCapSo; }
    public void datNgayCapSo(LocalDate ngayCapSo) { this.ngayCapSo = ngayCapSo; }
    public Integer laySoThuTuKham() { return soThuTuKham; }
    public void datSoThuTuKham(Integer soThuTuKham) { this.soThuTuKham = soThuTuKham; }
    public String layLyDo() { return lyDo; }
    public void datLyDo(String lyDo) { this.lyDo = lyDo; }
    public String layGhiChu() { return ghiChu; }
    public void datGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public String layTrangThaiKham() { return trangThaiKham; }
    public void datTrangThaiKham(String trangThaiKham) { this.trangThaiKham = trangThaiKham; }
    public BenhNhan layBenhNhan() { return benhNhan; }
    public void datBenhNhan(BenhNhan benhNhan) { this.benhNhan = benhNhan; }
    public String layTenNhaSi() { return tenNhaSi; }
    public void datTenNhaSi(String tenNhaSi) { this.tenNhaSi = tenNhaSi; }
}
