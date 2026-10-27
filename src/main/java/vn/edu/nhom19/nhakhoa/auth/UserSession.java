package vn.edu.nhom19.nhakhoa.auth;

/**
 * DEMO ONLY: chỉ nhớ Role đang xem UI, không phải bằng chứng xác thực.
 * Không giữ tài khoản, mật khẩu, hash hoặc kết nối database.
 */
public final class UserSession {
    private Role role;
    private long version;

    public void startDemo(Role selectedRole) {
        if (selectedRole == null) {
            throw new IllegalArgumentException("Cần chọn vai trò demo.");
        }
        role = selectedRole;
        version++;
    }

    public boolean isActive() {
        return role != null;
    }

    public Role getRole() {
        return role;
    }

    public long version() {
        return version;
    }

    /**
     * Chặn Controller cũ sau logout để tránh dùng nhầm UI.
     * Đây là kiểm tra vòng đời demo, không phải kiểm tra bảo mật.
     */
    public Role requireRole(long expectedVersion) {
        if (role == null || version != expectedVersion) {
            throw new IllegalStateException("Phiên demo không còn hoạt động. Hãy chọn lại vai trò trên Login.");
        }
        return role;
    }

    public void clear() {
        role = null;
        version++;
    }
}
