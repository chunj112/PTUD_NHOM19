package iuh.se.nhom19.auth;

/** Phiên chỉ giữ danh tính/vai trò, không lưu mật khẩu, hash hoặc Connection. */
public final class PhienDangNhap {
    private VaiTro vaiTro;
    private String maNV, hoTen;
    private long phienBan;

    public void batDauPhienDangNhap(KetQuaDangNhap ketQua) {
        if (ketQua == null) throw new IllegalArgumentException("Thiếu kết quả xác thực.");
        maNV = ketQua.layMaNhanVien();
        hoTen = ketQua.layHoTen();
        vaiTro = ketQua.layVaiTro();
        phienBan++;
    }

    public boolean dangHoatDong() { return vaiTro != null; }
    public VaiTro layVaiTro() { return vaiTro; }
    public String layMaNhanVien() { return maNV; }
    public String layHoTen() { return hoTen; }
    public long layPhienBan() { return phienBan; }

    public VaiTro yeuCauVaiTro(long phienBanMongDoi) {
        if (vaiTro == null || phienBan != phienBanMongDoi) {
            throw new IllegalStateException("Phiên đăng nhập đã kết thúc. Vui lòng đăng nhập lại.");
        }
        return vaiTro;
    }

    /** Tăng version làm mọi Controller/callback của phiên trước hết hiệu lực. */
    public void xoa() {
        vaiTro = null;
        maNV = null;
        hoTen = null;
        phienBan++;
    }
}
