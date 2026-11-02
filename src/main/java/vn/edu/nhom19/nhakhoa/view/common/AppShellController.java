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
import vn.edu.nhom19.nhakhoa.service.DemoClinicService;
import vn.edu.nhom19.nhakhoa.view.manager.*;
import vn.edu.nhom19.nhakhoa.view.receptionist.*;
import vn.edu.nhom19.nhakhoa.view.dentist.ExaminationController;

/** AppShell DEMO ONLY: chọn menu theo Role để kiểm tra UI, không phải phân quyền bảo mật. */
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
    private ControllerContext context;
    private final Map<Route, ScreenController> screens = new EnumMap<>(Route.class);
    private ScreenController currentScreen;

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
                showPlaceholder("Thông báo", "DEMO ONLY · Chưa triển khai nguồn dữ liệu thông báo.");
            }
        });
    }

    public void configure(UserSession userSession, AppNavigator appNavigator) {
        if (disposed || userSession == null || appNavigator == null) {
            throw new IllegalStateException("Không thể mở AppShell demo.");
        }
        session = userSession;
        navigator = appNavigator;
        sessionVersion = session.version();
        context = new ControllerContext(new DemoClinicService(session), this);
        Role role = session.requireRole(sessionVersion);

        // Ẩn và bỏ chỗ của mọi nhóm, sau đó chỉ bật đúng một nhóm menu.
        hideRoleMenus();
        VBox activeMenu;
        switch (role) {
            case MANAGER: activeMenu = managerMenu; break;
            case RECEPTIONIST: activeMenu = receptionistMenu; break;
            case DENTIST: activeMenu = dentistMenu; break;
            case NURSE: activeMenu = nurseMenu; break;
            default: throw new IllegalStateException("Vai trò demo không hợp lệ.");
        }
        activeMenu.setVisible(true);
        activeMenu.setManaged(true);

        lblRole.setText(role.displayName().toUpperCase(Locale.forLanguageTag("vi")));
        lblBreadcrumbRole.setText(role.displayName());
        lblSidebarName.setText("Demo · " + role.displayName());
        lblSidebarStatus.setText("DEMO ONLY · Phiên giả lập");
        lblAvatarInitials.setText("DM");
        lblCurrentDate.setText(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))
            .format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale.forLanguageTag("vi"))));
        mnuAccount.setText("Demo · " + role.displayName());
        appShell.setDisable(false);
        navigate(Route.DASHBOARD);
    }

    /** Chỉ đổi contentArea; không tạo lại Stage, Scene, Sidebar hoặc Topbar. */
    public void navigate(Route route) {
        if (!isCurrent()) {
            throw new IllegalStateException("Phiên demo đã đóng. Hãy chọn Role trên Login.");
        }
        Role role = session.requireRole(sessionVersion);
        // Chỉ giữ luồng preview nhất quán, không coi kiểm tra này là bảo mật.
        if (route == null || !route.allows(role)) {
            throw new IllegalArgumentException("Màn hình không thuộc vai trò demo đang chọn.");
        }

        // Hồ sơ là modal của UI đã có; không làm mất workspace phía sau.
        if (route == Route.PROFILE && (role == Role.DENTIST || role == Role.RECEPTIONIST)) {
            context.profile();
            return;
        }
        if (!canLeave()) return;
        ScreenController screen = screens.get(route);
        if (screen == null) {
            Parent page;
            var url = getClass().getResource(route.fxml(role));
            try {
                if (route == Route.PASSWORD) {
                    page = placeholder(route.title(), "DEMO ONLY · CHƯA TRIỂN KHAI: Không có tài khoản/mật khẩu thật hoặc AuthService. Không thu/lưu mật khẩu.");
                } else if (url == null) {
                    page = placeholder(route.title(), "DEMO ONLY · CHƯA TRIỂN KHAI: Chưa có FXML cho màn hình/Role này.");
                } else {
                    FXMLLoader loader = new FXMLLoader(url);
                    page = loader.load();
                    ViewAccess.rememberNamespace(page, loader);
                }
                screen = createController(route, new ViewAccess(page));
            } catch (IOException | RuntimeException ex) {
                System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR, "Không nạp được route " + route, ex);
                page = placeholder(route.title(), "Không thể mở màn hình: " + ex.getMessage());
                screen = new ScreenController(new ViewAccess(page), context);
            }
            screens.put(route, screen);
        }
        if (screen instanceof ExaminationController examination) examination.openQueue();
        String refreshError = null;
        try { screen.refresh(); }
        catch (RuntimeException ex) { refreshError = ex.getMessage(); }
        replacePage(screen);
        lblBreadcrumbPage.setText(route.title());
        clearSelection();
        if (buttons.containsKey(route)) buttons.get(route).getStyleClass().add("menu-selected");
        navigator.updateTitle(role.displayName() + " / " + route.title());
        if (refreshError != null) screen.view.error("Chưa thể làm mới dữ liệu: " + refreshError);
    }

    /** Mỗi trang có một Controller trong phiên; không tạo lại Sidebar/Topbar. */
    private ScreenController createController(Route route, ViewAccess view) {
        if (view.find("lblPageMessage") != null) return new ScreenController(view, context);
        switch (route) {
            case DASHBOARD: return new DashboardController(view, context);
            case STAFF: return new EmployeeController(view, context);
            case ASSIGNMENTS: return new AssignmentController(view, context);
            case SERVICES: return new ServiceCatalogController(view, context);
            case REPORTS: return new ReportsController(view, context);
            case INTAKE: return new IntakeController(view, context);
            case PATIENTS: return new PatientController(view, context);
            case CASHIER: return new CashierController(view, context);
            case EXAMINATION: return new ExaminationController(view, context);
            case DENTIST_SCHEDULE:
            case RECEPTIONIST_SCHEDULE: return new WorkScheduleController(view, context);
            default: return new ScreenController(view, context);
        }
    }

    public BorderPane getRoot() { return appShell; }
    public boolean canLeave() { return currentScreen == null || currentScreen.canLeave(); }

    public void replacePage(ScreenController screen) {
        if (!isCurrent()) throw new IllegalStateException("Phiên demo đã đóng.");
        currentScreen = screen;
        contentArea.getChildren().setAll(screen.view.root);
        contentScroll.setVvalue(0);
    }

    public void openExam(String id, boolean resume) {
        if (!isCurrent()) throw new IllegalStateException("Phiên demo đã đóng.");
        if (session.requireRole(sessionVersion) != Role.DENTIST) throw new IllegalArgumentException("Chỉ nha sĩ mở workspace khám.");
        if (!canLeave()) return;
        navigate(Route.EXAMINATION);
        ScreenController screen = screens.get(Route.EXAMINATION);
        // canLeave có thể bị hủy; không điều hướng tiếp nếu vẫn ở trang cũ.
        if (currentScreen == screen && screen instanceof ExaminationController examination) examination.selectExam(id, resume);
    }

    @FXML private void handleLogout() {
        if (isCurrent()) {
            navigator.logout();
        }
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
        if (!canLeave()) return;
        Parent page = placeholder(title, message);
        currentScreen = new ScreenController(new ViewAccess(page), context);
        contentArea.getChildren().setAll(page);
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
        for (ScreenController screen : screens.values()) screen.dispose();
        screens.clear();
        currentScreen = null;
        if (context != null) context.dispose();
        context = null;
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
