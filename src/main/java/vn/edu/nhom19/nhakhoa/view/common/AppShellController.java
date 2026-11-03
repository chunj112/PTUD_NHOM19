package vn.edu.nhom19.nhakhoa.view.common;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import vn.edu.nhom19.nhakhoa.app.AppNavigator;
import vn.edu.nhom19.nhakhoa.app.Route;
import vn.edu.nhom19.nhakhoa.auth.Role;
import vn.edu.nhom19.nhakhoa.auth.UserSession;

/** AppShell dùng danh tính Session đã xác thực; chức năng chưa có backend hiển thị trống, không gán dữ liệu giả. */
public final class AppShellController {
    @FXML private BorderPane appShell;
    @FXML private VBox managerMenu, receptionistMenu, dentistMenu, nurseMenu, navigationMenu;
    @FXML private StackPane contentArea;
    @FXML private ScrollPane contentScroll;
    @FXML private Label lblRole, lblBreadcrumbRole, lblBreadcrumbPage, lblSidebarName,
        lblSidebarStatus, lblAvatarInitials, lblCurrentDate;
    @FXML private MenuButton mnuAccount;
    @FXML private MenuItem miProfile, miChangePassword, miLogout;
    @FXML private Button btnDashboard, btnStaff, btnAssignments, btnServices, btnReports,
        btnIntakeAppointments, btnCashier, btnPatientLookup, btnReceptionistSchedule,
        btnExamination, btnDentistSchedule, btnNurseSchedule, btnNotifications;

    private Map<Route, Button> buttons;
    private UserSession session;
    private AppNavigator navigator;
    private long sessionVersion;
    private boolean disposed;

    @FXML private void initialize() {
        hideRoleMenus();
        appShell.setDisable(true);
        buttons = new EnumMap<>(Route.class);
        buttons.put(Route.DASHBOARD, btnDashboard);
        buttons.put(Route.STAFF, btnStaff);
        buttons.put(Route.ASSIGNMENTS, btnAssignments);
        buttons.put(Route.SERVICES, btnServices);
        buttons.put(Route.REPORTS, btnReports);
        buttons.put(Route.INTAKE, btnIntakeAppointments);
        buttons.put(Route.CASHIER, btnCashier);
        buttons.put(Route.PATIENTS, btnPatientLookup);
        buttons.put(Route.RECEPTIONIST_SCHEDULE, btnReceptionistSchedule);
        buttons.put(Route.EXAMINATION, btnExamination);
        buttons.put(Route.DENTIST_SCHEDULE, btnDentistSchedule);
        buttons.put(Route.NURSE_SCHEDULE, btnNurseSchedule);
        for (Map.Entry<Route, Button> entry : buttons.entrySet()) {
            Route route = entry.getKey();
            entry.getValue().setOnAction(event -> navigate(route));
        }
        miProfile.setOnAction(event -> navigate(Route.PROFILE));
        miChangePassword.setOnAction(event -> navigate(Route.PASSWORD));
        btnNotifications.setOnAction(event -> {
            if (isCurrent()) {
                showPlaceholder("Thông báo", "Chức năng đang phát triển: chưa kết nối nguồn thông báo.");
            }
        });
    }

    public void configure(UserSession userSession, AppNavigator appNavigator) {
        if (disposed || userSession == null || appNavigator == null) {
            throw new IllegalStateException("Không thể mở AppShell.");
        }
        session = userSession;
        navigator = appNavigator;
        sessionVersion = session.version();
        Role role = session.requireRole(sessionVersion);

        // Ẩn và bỏ chỗ của mọi nhóm, sau đó chỉ bật đúng một nhóm menu.
        hideRoleMenus();
        VBox activeMenu;
        switch (role) {
            case MANAGER: activeMenu = managerMenu; break;
            case RECEPTIONIST: activeMenu = receptionistMenu; break;
            case DENTIST: activeMenu = dentistMenu; break;
            case NURSE: activeMenu = nurseMenu; break;
            default: throw new IllegalStateException("Vai trò không hợp lệ.");
        }
        activeMenu.setVisible(true);
        activeMenu.setManaged(true);

        lblRole.setText(role.displayName().toUpperCase(Locale.forLanguageTag("vi")));
        lblBreadcrumbRole.setText(role.displayName());
        lblSidebarName.setText(session.layHoTen());
        lblSidebarStatus.setText("Đã đăng nhập · "+role.displayName());
        lblSidebarStatus.setWrapText(true);
        lblAvatarInitials.setText(layChuCaiDau(session.layHoTen()));
        lblCurrentDate.setText(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))
            .format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale.forLanguageTag("vi"))));
        mnuAccount.setText(session.layHoTen());
        appShell.setDisable(false);
        navigate(Route.DASHBOARD);
    }

    /** Chỉ đổi contentArea; không tạo lại Stage, Scene, Sidebar hoặc Topbar. */
    public void navigate(Route route) {
        if (!isCurrent()) {
            throw new IllegalStateException("Phiên đăng nhập đã kết thúc. Vui lòng đăng nhập lại.");
        }
        Role role = session.requireRole(sessionVersion);
        // Kiểm tra route ở đây không thay thế kiểm tra quyền của Service/DAO khi các UC chuyển sang SQL.
        if (route == null || !route.allows(role)) {
            throw new IllegalArgumentException("Màn hình không thuộc vai trò của phiên đăng nhập.");
        }

        Parent page;
        var url = getClass().getResource(route.fxml(role));
        try {
            if (url == null) {
                page = placeholder(route.title(),
                    "Chức năng đang phát triển. Màn hình hoặc backend chưa được tích hợp.");
            } else {
                FXMLLoader loader = new FXMLLoader(url);
                page = loader.load();
            }
        } catch (IOException ex) {
            page = placeholder(route.title(), "Không thể nạp giao diện của màn hình.");
        }
        // Các màn hình nghiệp vụ chưa có Controller/Service SQL hiển thị banner; trang đã nối Backend thì hiển thị trực tiếp.
        if (route == Route.SERVICES) {
            contentArea.getChildren().setAll(page);
        } else {
            Label thongBao=new Label("Chức năng đang phát triển · Chưa tải dữ liệu từ hệ thống.");
            thongBao.setWrapText(true);thongBao.getStyleClass().add("info-banner");
            VBox noiDung=new VBox(16,thongBao,page);noiDung.setMinWidth(0);
            contentArea.getChildren().setAll(noiDung);
        }
        contentScroll.setVvalue(0);
        lblBreadcrumbPage.setText(route.title());
        clearSelection();
        if (buttons.containsKey(route)) {
            buttons.get(route).getStyleClass().add("menu-selected");
        }

        // Hai nút Dashboard chỉ nối điều hướng, chưa làm Controller nghiệp vụ của module khác.
        Node reports = page.lookup("#btnViewReports");
        if (reports instanceof Button button) {
            button.setDisable(false);button.setOnAction(event -> navigate(Route.REPORTS));
        }
        Node schedule = page.lookup("#btnViewSchedule");
        if (schedule instanceof Button button) {
            button.setDisable(false);button.setOnAction(event -> navigate(Route.ASSIGNMENTS));
        }
        navigator.updateTitle(role.displayName() + " / " + route.title());
    }

    @FXML private void handleLogout() {
        if (isCurrent()) {
            navigator.logout();
        }
    }

    private String layChuCaiDau(String hoTen) {
        String[] tu=hoTen.trim().split("\\s+");String ketQua="";
        for(int i=Math.max(0,tu.length-2);i<tu.length;i++)ketQua+=tu[i].substring(0,1);
        return ketQua.toUpperCase(Locale.forLanguageTag("vi"));
    }
    private boolean isCurrent() {
        if (disposed || session == null || !session.isActive()) {
            return false;
        }
        return session.version() == sessionVersion;
    }

    private Parent placeholder(String title, String message) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/common/page-unavailable.fxml"));
            Parent root = loader.load();
            ((Label) loader.getNamespace().get("lblPageTitle")).setText(title);
            ((Label) loader.getNamespace().get("lblPageMessage")).setText(message);
            return root;
        } catch (IOException ex) {
            throw new IllegalStateException("Thiếu placeholder chung", ex);
        }
    }

    private void clearSelection() {
        for (Button button : buttons.values()) {
            button.getStyleClass().remove("menu-selected");
        }
    }

    private void showPlaceholder(String title, String message) {
        contentArea.getChildren().setAll(placeholder(title, message));
        contentScroll.setVvalue(0);
        lblBreadcrumbPage.setText(title);
        clearSelection();
        navigator.updateTitle(lblBreadcrumbRole.getText() + " / " + title);
    }

    private void hideRoleMenus() {
        for (VBox menu : List.of(managerMenu, receptionistMenu, dentistMenu, nurseMenu)) {
            menu.setVisible(false);
            menu.setManaged(false);
        }
    }

    /** Xóa phiên nhìn thấy trên UI; Controller cũ không được tiếp tục điều hướng. */
    public void dispose() {
        disposed = true;
        hideRoleMenus();
        contentArea.getChildren().clear();
        appShell.setDisable(true);
        lblRole.setText("");
        lblBreadcrumbRole.setText("");
        lblBreadcrumbPage.setText("");
        lblSidebarName.setText("");
        lblSidebarStatus.setText("");
        lblAvatarInitials.setText("");
        lblCurrentDate.setText("");
        mnuAccount.hide();
        mnuAccount.setText("");
        navigator = null;
        session = null;
    }
}
