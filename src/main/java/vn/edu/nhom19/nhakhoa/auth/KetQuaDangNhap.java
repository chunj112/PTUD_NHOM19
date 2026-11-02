package vn.edu.nhom19.nhakhoa.auth;

/** Kết quả xác thực thành công, không mang mật khẩu hoặc hash. */
public final class KetQuaDangNhap {
    private final String maNV,hoTen;
    private final Role vaiTro;
    public KetQuaDangNhap(String maNV,String hoTen,Role vaiTro) {
        if(maNV==null||maNV.isBlank()||hoTen==null||hoTen.isBlank()||vaiTro==null)
            throw new IllegalArgumentException("Kết quả đăng nhập thiếu thông tin nhân viên.");
        this.maNV=maNV;this.hoTen=hoTen;this.vaiTro=vaiTro;
    }
    public String layMaNhanVien(){return maNV;}
    public String layHoTen(){return hoTen;}
    public Role layVaiTro(){return vaiTro;}
}
