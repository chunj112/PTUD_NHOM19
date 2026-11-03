package iuh.se.nhom19.entity;

import java.math.BigDecimal;
import java.util.Objects;

public class DichVu {
    private String maDV;
    private String tenDV;
    private String donViTinh;
    private BigDecimal donGia;
    private Integer thoiGianThucHien;
    private String moTa;
    private boolean dangApDung;

    public DichVu() {
        this.dangApDung = true;
    }

    public DichVu(String maDV, String tenDV, String donViTinh, BigDecimal donGia, Integer thoiGianThucHien, String moTa, boolean dangApDung) {
        this.maDV = maDV;
        this.tenDV = tenDV;
        this.donViTinh = donViTinh;
        this.donGia = donGia;
        this.thoiGianThucHien = thoiGianThucHien;
        this.moTa = moTa;
        this.dangApDung = dangApDung;
    }

    public String getMaDV() {
        return maDV;
    }

    public void setMaDV(String maDV) {
        this.maDV = maDV;
    }

    public String getTenDV() {
        return tenDV;
    }

    public void setTenDV(String tenDV) {
        this.tenDV = tenDV;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public Integer getThoiGianThucHien() {
        return thoiGianThucHien;
    }

    public void setThoiGianThucHien(Integer thoiGianThucHien) {
        this.thoiGianThucHien = thoiGianThucHien;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public boolean isDangApDung() {
        return dangApDung;
    }

    public void setDangApDung(boolean dangApDung) {
        this.dangApDung = dangApDung;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DichVu dichVu = (DichVu) o;
        return Objects.equals(maDV, dichVu.maDV);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maDV);
    }

    @Override
    public String toString() {
        return tenDV != null ? tenDV : (maDV != null ? maDV : "");
    }
}
