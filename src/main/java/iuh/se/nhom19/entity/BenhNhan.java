package iuh.se.nhom19.entity;

import java.time.*;

/** Dữ liệu BenhNhan; các đối tượng/tên liên quan được đọc bằng JOIN, không phải cột mới. */
public class BenhNhan {
    private String maBenhNhan;
    private String hoTen;
    private Boolean nam;
    private LocalDate ngaySinh;
    private String soDienThoai;
    private String diaChi;

    public String layMaBenhNhan() { return maBenhNhan; }
    public void datMaBenhNhan(String maBenhNhan) { this.maBenhNhan = maBenhNhan; }
    public String layHoTen() { return hoTen; }
    public void datHoTen(String hoTen) { this.hoTen = hoTen; }
    public Boolean layNam() { return nam; }
    public void datNam(Boolean nam) { this.nam = nam; }
    public LocalDate layNgaySinh() { return ngaySinh; }
    public void datNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }
    public String laySoDienThoai() { return soDienThoai; }
    public void datSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String layDiaChi() { return diaChi; }
    public void datDiaChi(String diaChi) { this.diaChi = diaChi; }
    @Override public String toString() { return maBenhNhan + " · " + hoTen + " · " + soDienThoai; }
}
