package vn.edu.nhom19.nhakhoa.auth;

/** Bốn vai trò ứng dụng; UC001 mapping từ NhanVien.chucVu sau khi xác thực. */
public enum Role {
    MANAGER("Quản lý"),
    RECEPTIONIST("Lễ tân"),
    DENTIST("Nha sĩ"),
    NURSE("Y tá");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
