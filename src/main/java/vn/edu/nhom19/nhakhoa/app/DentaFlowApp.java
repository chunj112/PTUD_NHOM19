package vn.edu.nhom19.nhakhoa.app;

import java.net.URL;
import java.util.Objects;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/** Bootstrap JavaFX. Controller và Backend sẽ do các thành viên triển khai. */
public class DentaFlowApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        URL fxml = Objects.requireNonNull(
            getClass().getResource("/fxml/common/app-shell.fxml"),
            "Missing FXML: /fxml/common/app-shell.fxml"
        );
        Parent root = FXMLLoader.load(fxml);
        Scene scene = new Scene(root, 1280, 800);
        URL css = getClass().getResource("/css/theme.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setTitle("DentaFlow - Quan ly phong kham nha khoa");
        stage.setMinWidth(1040);
        stage.setMinHeight(650);
        URL icon = getClass().getResource("/images/dentaflow-logo.png");
        if (icon != null) {
            stage.getIcons().add(new Image(icon.toExternalForm()));
        }
        stage.setScene(scene);
        stage.show();
    }
}
