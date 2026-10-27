package vn.edu.nhom19.nhakhoa.view.common;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import vn.edu.nhom19.nhakhoa.app.AppNavigator;
import vn.edu.nhom19.nhakhoa.auth.Role;

/** DEMO ONLY: chọn Role để xem UI. Không kiểm tra tên đăng nhập hoặc mật khẩu. */
public final class LoginController {
    @FXML private Button btnDemoManager;
    @FXML private Button btnDemoReceptionist;
    @FXML private Button btnDemoDentist;
    @FXML private Button btnDemoNurse;
    @FXML private Label lblError;

    private AppNavigator navigator;
    private boolean opening;
    private boolean disposed;

    @FXML private void initialize() {
        showError("");
        setButtonsDisabled(true); // Đợi AppNavigator được cấp sau khi FXMLLoader.load() xong.
    }

    public void configure(AppNavigator appNavigator) {
        if (appNavigator == null) {
            throw new IllegalArgumentException("Thiếu bộ điều hướng.");
        }
        navigator = appNavigator;
        setButtonsDisabled(false);
    }

    /** Cả bốn nút gọi cùng handler; userData trong FXML chỉ chứa mã Role demo. */
    @FXML private void handleDemoRole(ActionEvent event) {
        if (disposed || opening || navigator == null) {
            return;
        }
        Button button = (Button) event.getSource();
        String roleCode = (String) button.getUserData();
        Role role;
        if (roleCode == null) {
            showError("Vai trò demo không hợp lệ.");
            return;
        }
        switch (roleCode) {
            case "MANAGER": role = Role.MANAGER; break;
            case "RECEPTIONIST": role = Role.RECEPTIONIST; break;
            case "DENTIST": role = Role.DENTIST; break;
            case "NURSE": role = Role.NURSE; break;
            default:
                showError("Vai trò demo không hợp lệ.");
                return;
        }

        // Chỉ tạo phiên demo trong bộ nhớ rồi mở AppShell; không có JDBC hoặc AuthService.
        opening = true;
        setButtonsDisabled(true);
        showError("");
        try {
            navigator.openDemoRole(role);
        } catch (RuntimeException ex) {
            opening = false;
            setButtonsDisabled(false);
            showError("Không thể mở giao diện demo. Hãy kiểm tra tài nguyên FXML.");
        }
    }

    private void setButtonsDisabled(boolean disabled) {
        btnDemoManager.setDisable(disabled);
        btnDemoReceptionist.setDisable(disabled);
        btnDemoDentist.setDisable(disabled);
        btnDemoNurse.setDisable(disabled);
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(!message.isBlank());
        lblError.setManaged(!message.isBlank());
    }

    public void dispose() {
        disposed = true;
        navigator = null;
        setButtonsDisabled(true);
        showError("");
    }
}
