package vn.edu.nhom19.nhakhoa.app;

import java.net.URL;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import vn.edu.nhom19.nhakhoa.auth.UserSession;

/** Bootstrap giữ nguyên JavaFX; ngoại lệ Prompt 02 cho phần Login/Session của Tường. */
public class DentaFlowApp extends Application {
    private AppNavigator navigator;
    @Override
    public void start(Stage stage) throws Exception {
        // Quyết định UI hiện tại: chọn Role DEMO ONLY, không xác thực SQL hoặc mật khẩu.
        navigator = new AppNavigator(stage, new UserSession());
        navigator.start();

        stage.setMinWidth(1040);
        stage.setMinHeight(650);
        URL icon = getClass().getResource("/images/dentaflow-logo.png");
        if (icon != null) {
            stage.getIcons().add(new Image(icon.toExternalForm()));
        }
        stage.show();
    }

    @Override public void stop() {
        if (navigator != null) navigator.close();
    }
}
