package vn.edu.nhom19.nhakhoa.app;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import vn.edu.nhom19.nhakhoa.auth.Role;
import vn.edu.nhom19.nhakhoa.auth.UserSession;
import vn.edu.nhom19.nhakhoa.view.common.AppShellController;
import vn.edu.nhom19.nhakhoa.view.common.DangNhapController;
import vn.edu.nhom19.nhakhoa.auth.KetQuaDangNhap;
import vn.edu.nhom19.nhakhoa.service.DangNhapService;

/** Giữ một Stage/Scene, chỉ mở không gian làm việc sau xác thực tài khoản SQL. */
public final class AppNavigator implements AutoCloseable {
    private final Stage stage;
    private final UserSession session;
    private DangNhapController loginController;
    private final DangNhapService dichVuDangNhap;
    private AppShellController shellController;
    private boolean closed;

    public AppNavigator(Stage stage, UserSession session) {
        this(stage,session,new DangNhapService());
    }
    /** Cho phép cấp Service khi kiểm thử; ứng dụng mặc định luôn dùng Service/DAO SQL thật. */
    public AppNavigator(Stage stage, UserSession session, DangNhapService dichVuDangNhap) {
        if (stage == null || session == null) {
            throw new IllegalArgumentException("Thiếu cửa sổ hoặc Session.");
        }
        this.stage = stage;
        this.session = session;
        this.dichVuDangNhap=java.util.Objects.requireNonNull(dichVuDangNhap);
    }

    public void start() throws IOException {
        if (closed) throw new IllegalStateException("Bộ điều hướng đã đóng.");
        showLogin();
    }

    private FXMLLoader loader(String path) throws IOException {
        var resource = getClass().getResource(path);
        if (resource == null) {
            throw new IOException("Thiếu FXML: " + path);
        }
        return new FXMLLoader(resource);
    }

    private void install(Parent root, String title) {
        if (stage.getScene() == null) {
            Scene scene = new Scene(root, 1280, 800);
            scene.getStylesheets().add(getClass().getResource("/css/theme.css").toExternalForm());
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(root);
        }
        updateTitle(title);
    }

    /** Tiêu đề không chứa danh tính hoặc thông tin xác thực. */
    public void updateTitle(String pageTitle) {
        stage.setTitle("DentaFlow · "+pageTitle);
    }

    private void showLogin() throws IOException {
        // Bước 1: xóa phiên trước. Nếu load Login lỗi, cũng không giữ dữ liệu AppShell.
        session.clear();
        if (shellController != null) {
            shellController.dispose();
            shellController = null;
        }
        if (loginController != null) {
            loginController.dispose();
            loginController = null;
        }
        if (stage.getScene() != null) {
            stage.getScene().setRoot(new StackPane());
        }
        FXMLLoader loader = loader("/fxml/common/login.fxml");
        Parent root = loader.load();
        loginController = loader.getController();
        loginController.configure(this, dichVuDangNhap);
        install(root, "Đăng nhập");
    }

    /** Không nhận Role hoặc kết quả do bên gọi tự tạo; lấy từ form đã xác thực. */
    public void moSauDangNhap() {
        if (closed || session.isActive() || loginController == null) {
            throw new IllegalStateException("Phiên đăng nhập không còn sẵn sàng.");
        }
        KetQuaDangNhap ketQua=loginController.layKetQuaDaXacThuc();
        if (ketQua == null) {
            throw new IllegalStateException("Chưa xác thực tài khoản.");
        }
        session.batDauPhienDangNhap(ketQua);
        moAppShell();
    }

    private void moAppShell() {
        Role role=session.requireRole(session.version());
        try {
            FXMLLoader loader = loader("/fxml/common/app-shell.fxml");
            Parent root = loader.load();
            shellController = loader.getController();
            shellController.configure(session, this);
            install(root, role.displayName() + " / Dashboard");
            if (loginController != null) {
                loginController.dispose();
                loginController = null;
            }
        } catch (IOException | RuntimeException ex) {
            session.clear();
            if (shellController != null) {
                shellController.dispose();
                shellController = null;
            }
            throw new IllegalStateException("Không thể nạp giao diện sau đăng nhập", ex);
        }
    }

    public void logout() {
        if (closed) {
            return;
        }
        try {
            showLogin();
        } catch (IOException ex) {
            install(new StackPane(new Label("Không thể mở Login. Hãy đóng và mở lại DentaFlow.")),
                    "Lỗi tài nguyên");
        }
    }

    @Override public void close() {
        closed = true;
        session.clear();
        if (loginController != null) {
            loginController.dispose();
            loginController = null;
        }
        if (shellController != null) {
            shellController.dispose();
            shellController = null;
        }
    }
}
