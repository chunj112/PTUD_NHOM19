package iuh.se.nhom19.auth;

/** Bốn vai trò ứng dụng; UC001 mapping từ NhanVien.chucVu sau khi xác thực. */
public enum VaiTro {
    MANAGER("Quản lý"),
    RECEPTIONIST("Lễ tân"),
    DENTIST("Nha sĩ"),
    NURSE("Y tá");

    private final String tenHienThi;

    VaiTro(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String layTenHienThi() {
        return tenHienThi;
    }
}
