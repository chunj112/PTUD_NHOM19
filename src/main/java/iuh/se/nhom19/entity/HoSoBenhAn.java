package iuh.se.nhom19.entity;

import java.time.*;

/** Dữ liệu HoSoBenhAn; các đối tượng/tên liên quan được đọc bằng JOIN, không phải cột mới. */
public class HoSoBenhAn {
    private String maHoSo;
    private String maBenhNhan;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhatGanNhat;
    private String tienSuBenh;
    private String tienSuDiUng;
    private String ghiChuCanhBao;

    public String layMaHoSo() { return maHoSo; }
    public void datMaHoSo(String maHoSo) { this.maHoSo = maHoSo; }
    public String layMaBenhNhan() { return maBenhNhan; }
    public void datMaBenhNhan(String maBenhNhan) { this.maBenhNhan = maBenhNhan; }
    public LocalDateTime layNgayTao() { return ngayTao; }
    public void datNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
    public LocalDateTime layNgayCapNhatGanNhat() { return ngayCapNhatGanNhat; }
    public void datNgayCapNhatGanNhat(LocalDateTime ngayCapNhatGanNhat) { this.ngayCapNhatGanNhat = ngayCapNhatGanNhat; }
    public String layTienSuBenh() { return tienSuBenh; }
    public void datTienSuBenh(String tienSuBenh) { this.tienSuBenh = tienSuBenh; }
    public String layTienSuDiUng() { return tienSuDiUng; }
    public void datTienSuDiUng(String tienSuDiUng) { this.tienSuDiUng = tienSuDiUng; }
    public String layGhiChuCanhBao() { return ghiChuCanhBao; }
    public void datGhiChuCanhBao(String ghiChuCanhBao) { this.ghiChuCanhBao = ghiChuCanhBao; }
}
