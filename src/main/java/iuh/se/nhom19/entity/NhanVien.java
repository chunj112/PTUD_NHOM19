package iuh.se.nhom19.entity;

/** Chỉ giữ các cột nhân viên cần cho xác thực UC001. */
public final class NhanVien {
    private final String maNV,hoTen,chucVu;
    private final boolean dangLamViec;
    public NhanVien(String maNV,String hoTen,String chucVu,boolean dangLamViec) {
        this.maNV=maNV;this.hoTen=hoTen;this.chucVu=chucVu;this.dangLamViec=dangLamViec;
    }
    public String layMaNhanVien(){return maNV;}
    public String layHoTen(){return hoTen;}
    public String layChucVu(){return chucVu;}
    public boolean dangLamViec(){return dangLamViec;}
    @Override public String toString(){return hoTen;}
}
