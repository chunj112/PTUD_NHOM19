package vn.edu.nhom19.nhakhoa.auth;

/** Bốn vai trò để xem thử UI. Role được chọn trên Login demo, chưa lấy từ SQL Server. */
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
