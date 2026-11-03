package iuh.se.nhom19.entity;

/** Hash chỉ tồn tại ở tầng xác thực, không đưa vào Session hoặc log. */
public final class TaiKhoan {
    private final String maTaiKhoan,tenDangNhap,matKhauBam;
    private final boolean hoatDong;
    private final NhanVien nhanVien;
    public TaiKhoan(String maTaiKhoan,String tenDangNhap,String matKhauBam,boolean hoatDong,NhanVien nhanVien) {
        this.maTaiKhoan=maTaiKhoan;this.tenDangNhap=tenDangNhap;this.matKhauBam=matKhauBam;
        this.hoatDong=hoatDong;this.nhanVien=nhanVien;
    }
    public String layMaTaiKhoan(){return maTaiKhoan;}
    public String layTenDangNhap(){return tenDangNhap;}
    public String layMatKhauBam(){return matKhauBam;}
    public boolean dangHoatDong(){return hoatDong;}
    public NhanVien layNhanVien(){return nhanVien;}
}
