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
import vn.edu.nhom19.nhakhoa.view.common.LoginController;

/** Điều hướng demo: giữ một Stage/Scene và xóa UI cũ khi đăng xuất. Không xác thực tài khoản. */
public final class AppNavigator implements AutoCloseable {
    private final Stage stage;
    private final UserSession session;
    private LoginController loginController;
    private AppShellController shellController;
    private boolean closed;

    public AppNavigator(Stage stage, UserSession session) {
        if (stage == null || session == null) {
            throw new IllegalArgumentException("Thiếu cửa sổ hoặc Session.");
        }
        this.stage = stage;
        this.session = session;
    }

    public void start() throws IOException {
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

    /** Ghi DEMO ONLY ở tiêu đề mọi màn hình, kể cả placeholder. */
    public void updateTitle(String pageTitle) {
        stage.setTitle("DentaFlow · DEMO ONLY · " + pageTitle);
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
        loginController.configure(this);
        install(root, "Đăng nhập demo");
    }

    /** Điểm nối duy nhất từ nút Role demo. Sprint Backend sẽ thay bằng kết quả AuthService thật. */
    public void openDemoRole(Role role) {
        if (closed || session.isActive()) {
            throw new IllegalStateException("Hãy quay về Login trước khi chọn vai trò demo.");
        }
        try {
            session.startDemo(role);
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
            throw new IllegalStateException("Không thể nạp AppShell demo", ex);
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
