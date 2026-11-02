package vn.edu.nhom19.nhakhoa.auth;

/** Phiên chỉ giữ danh tính/vai trò, không lưu mật khẩu, hash hoặc Connection. */
public final class UserSession {
    private Role role;
    private String maNV, hoTen;
    private long version;

    public void batDauPhienDangNhap(KetQuaDangNhap ketQua) {
        if (ketQua == null) throw new IllegalArgumentException("Thiếu kết quả xác thực.");
        maNV = ketQua.layMaNhanVien();
        hoTen = ketQua.layHoTen();
        role = ketQua.layVaiTro();
        version++;
    }

    public boolean isActive() { return role != null; }
    public Role getRole() { return role; }
    public String layMaNhanVien() { return maNV; }
    public String layHoTen() { return hoTen; }
    public long version() { return version; }

    public Role requireRole(long expectedVersion) {
        if (role == null || version != expectedVersion) {
            throw new IllegalStateException("Phiên đăng nhập đã kết thúc. Vui lòng đăng nhập lại.");
        }
        return role;
    }

    /** Tăng version làm mọi Controller/callback của phiên trước hết hiệu lực. */
    public void clear() {
        role = null;
        maNV = null;
        hoTen = null;
        version++;
    }
}
