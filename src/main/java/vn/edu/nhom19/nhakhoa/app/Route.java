package vn.edu.nhom19.nhakhoa.app;

import java.util.Set;
import vn.edu.nhom19.nhakhoa.auth.Role;

/** Định tuyến demo và nhóm menu; không phải bảng phân quyền bảo mật.
 * Các đường dẫn chưa có được hiển thị bằng một FXML placeholder chung. */
public enum Route {
    DASHBOARD("Dashboard", "btnDashboard", null, Role.values()),
    STAFF("Nhân sự", "btnStaff", "/fxml/manager/staff.fxml", Role.MANAGER),
    ASSIGNMENTS("Lịch & Phân công", "btnAssignments", "/fxml/manager/assignments.fxml", Role.MANAGER),
    SERVICES("Dịch vụ", "btnServices", "/fxml/manager/services.fxml", Role.MANAGER),
    REPORTS("Báo cáo", "btnReports", "/fxml/manager/reports.fxml", Role.MANAGER),
    INTAKE("Tiếp nhận & Lịch hẹn", "btnIntakeAppointments", "/fxml/receptionist/intake-appointments.fxml", Role.RECEPTIONIST),
    CASHIER("Thu ngân", "btnCashier", "/fxml/receptionist/cashier.fxml", Role.RECEPTIONIST),
    PATIENTS("Tra cứu", "btnPatientLookup", "/fxml/receptionist/patients.fxml", Role.RECEPTIONIST),
    RECEPTIONIST_SCHEDULE("Lịch làm việc", "btnReceptionistSchedule", "/fxml/receptionist/schedule.fxml", Role.RECEPTIONIST),
    EXAMINATION("Khám bệnh", "btnExamination", "/fxml/dentist/examination.fxml", Role.DENTIST),
    DENTIST_SCHEDULE("Lịch làm việc", "btnDentistSchedule", "/fxml/dentist/schedule.fxml", Role.DENTIST),
    NURSE_SCHEDULE("Lịch làm việc", "btnNurseSchedule", "/fxml/nurse/schedule.fxml", Role.NURSE),
    PROFILE("Hồ sơ cá nhân", null, "/fxml/common/profile.fxml", Role.values()),
    PASSWORD("Đổi mật khẩu", null, "/fxml/common/change-password.fxml", Role.values());

    private final String title;
    private final String buttonId;
    private final String path;
    private final Set<Role> roles;

    Route(String title, String buttonId, String path, Role... roles) {
        this.title = title; this.buttonId = buttonId; this.path = path; this.roles = Set.of(roles);
    }
    public String title() { return title; }
    public String buttonId() { return buttonId; }
    public boolean allows(Role role) { return roles.contains(role); }

    public String fxml(Role role) {
        return this == DASHBOARD ? "/fxml/" + role.name().toLowerCase(java.util.Locale.ROOT) + "/dashboard.fxml" : path;
    }
}
