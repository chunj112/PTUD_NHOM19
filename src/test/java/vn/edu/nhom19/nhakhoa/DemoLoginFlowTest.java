package vn.edu.nhom19.nhakhoa;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import vn.edu.nhom19.nhakhoa.app.*;
import vn.edu.nhom19.nhakhoa.auth.*;
import vn.edu.nhom19.nhakhoa.view.common.AppShellController;

/** Chỉ kiểm thử UI demo, không kiểm thử hoặc tuyên bố có xác thực database. */
class DemoLoginFlowTest {
    private Stage stage;
    private UserSession session;
    private AppNavigator navigator;

    @BeforeAll static void startToolkit() throws Exception {
        CountDownLatch ready = new CountDownLatch(1);
        try { Platform.startup(ready::countDown); }
        catch (IllegalStateException alreadyStarted) { ready.countDown(); }
        assertTrue(ready.await(10, TimeUnit.SECONDS));
        fx(() -> { Platform.setImplicitExit(false); return null; });
    }

    @BeforeEach void startDemo() throws Exception {
        fx(() -> {
            stage = new Stage();
            session = new UserSession();
            navigator = new AppNavigator(stage, session);
            navigator.start();
            return null;
        });
    }

    @AfterEach void closeDemo() throws Exception {
        fx(() -> { navigator.close(); stage.close(); return null; });
    }

    private static <T> T fx(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }

    private <T extends Node> T node(String id, Class<T> type) {
        Parent root = stage.getScene().getRoot();
        root.applyCss();
        root.layout();
        return type.cast(java.util.Objects.requireNonNull(root.lookup("#" + id), id));
    }

    @Test void loginClearlyStatesDemoAndDoesNotCollectAnyCredentials() throws Exception {
        fx(() -> {
            assertFalse(session.isActive());
            assertTrue(stage.getTitle().contains("DEMO ONLY"));
            assertTrue(node("lblDemoNotice", Label.class).getText().contains("Không xác thực"));
            assertNull(stage.getScene().getRoot().lookup("#txtUsername"));
            assertNull(stage.getScene().getRoot().lookup("#txtPassword"));
            assertNull(stage.getScene().getRoot().lookup("#btnLogin"));
            snapshot("demo-login", 1280, 800);
            snapshot("demo-login-small", 1040, 650);
            return null;
        });
    }

    @Test void everyRoleButtonNavigatesAndLogoutResetsUiAcrossRepeatedLogins() throws Exception {
        fx(() -> {
            Scene scene = stage.getScene();
            for (Role role : Role.values()) {
                String buttonId;
                String groupId;
                String pageButton;
                switch (role) {
                    case MANAGER:
                        buttonId = "btnDemoManager"; groupId = "managerMenu"; pageButton = "btnStaff"; break;
                    case RECEPTIONIST:
                        buttonId = "btnDemoReceptionist"; groupId = "receptionistMenu"; pageButton = "btnIntakeAppointments"; break;
                    case DENTIST:
                        buttonId = "btnDemoDentist"; groupId = "dentistMenu"; pageButton = "btnExamination"; break;
                    case NURSE:
                        buttonId = "btnDemoNurse"; groupId = "nurseMenu"; pageButton = "btnNurseSchedule"; break;
                    default: throw new AssertionError();
                }
                Button oldLoginButton = node(buttonId, Button.class);
                oldLoginButton.fire();
                assertTrue(session.isActive());
                assertEquals(role, session.getRole());
                assertSame(scene, stage.getScene());
                assertTrue(stage.getTitle().contains("DEMO ONLY"));
                assertEquals("Dashboard", node("lblBreadcrumbPage", Label.class).getText());
                assertEquals("DEMO ONLY · Phiên giả lập", node("lblSidebarStatus", Label.class).getText());
                for (String id : new String[]{"managerMenu", "receptionistMenu", "dentistMenu", "nurseMenu"}) {
                    VBox group = node(id, VBox.class);
                    assertEquals(id.equals(groupId), group.isVisible());
                    assertEquals(id.equals(groupId), group.isManaged());
                }
                Parent shell = stage.getScene().getRoot();
                StackPane oldContent = node("contentArea", StackPane.class);
                Label oldIdentity = node("lblSidebarName", Label.class);
                snapshot("demo-shell-" + role.name().toLowerCase(), 1280, 800);
                snapshot("demo-shell-small-" + role.name().toLowerCase(), 1040, 650);

                node(pageButton, Button.class).fire();
                assertSame(shell, stage.getScene().getRoot());
                assertTrue(node("lblPageMessage", Label.class).getText().contains("CHƯA TRIỂN KHAI"));
                node("btnDashboard", Button.class).fire();
                assertTrue(node("btnDashboard", Button.class).getStyleClass().contains("menu-selected"));

                // Kiểm tra mục tài khoản cũng là placeholder, không báo thành công giả.
                MenuButton account = node("mnuAccount", MenuButton.class);
                account.getItems().get(0).fire();
                assertEquals("Hồ sơ cá nhân", node("lblBreadcrumbPage", Label.class).getText());
                account.getItems().get(1).fire();
                assertEquals("Đổi mật khẩu", node("lblBreadcrumbPage", Label.class).getText());
                for (MenuItem item : account.getItems()) {
                    if ("miLogout".equals(item.getId())) {
                        item.fire();
                        break;
                    }
                }
                assertFalse(session.isActive());
                assertNull(session.getRole());
                assertSame(scene, stage.getScene());
                assertNotSame(shell, stage.getScene().getRoot());
                assertTrue(oldContent.getChildren().isEmpty());
                assertEquals("", oldIdentity.getText());
                assertTrue(oldLoginButton.isDisabled());
                oldLoginButton.fire();
                assertFalse(session.isActive());
                assertNotNull(node("btnDemoManager", Button.class));
                assertFalse(node("btnDemoManager", Button.class).isDisabled());
            }
            return null;
        });
    }

    @Test void invalidDemoButtonDataDoesNotCreateASession() throws Exception {
        fx(() -> {
            Button button = node("btnDemoManager", Button.class);
            button.setUserData("NOT_A_ROLE");
            button.fireEvent(new ActionEvent());
            assertFalse(session.isActive());
            assertTrue(node("lblError", Label.class).getText().contains("không hợp lệ"));
            button.setUserData("MANAGER");
            button.fire();
            assertEquals(Role.MANAGER, session.getRole());
            return null;
        });
    }

    @Test void oldShellCannotNavigateAfterItsDemoSessionWasCleared() throws Exception {
        fx(() -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/common/app-shell.fxml"));
            Parent root = loader.load();
            AppShellController controller = loader.getController();
            assertThrows(IllegalStateException.class, () -> controller.navigate(Route.DASHBOARD));
            session.startDemo(Role.NURSE);
            controller.configure(session, navigator);
            assertThrows(IllegalArgumentException.class, () -> controller.navigate(Route.STAFF));
            session.clear();
            session.startDemo(Role.MANAGER);
            assertThrows(IllegalStateException.class, () -> controller.navigate(Route.DASHBOARD));
            controller.dispose();
            assertTrue(root.isDisabled());
            assertThrows(IllegalStateException.class, () -> controller.navigate(Route.DASHBOARD));
            return null;
        });
    }

    private void snapshot(String name, int width, int height) throws Exception {
        String folder = System.getProperty("dentaflow.qa.output");
        if (folder == null) return;
        Parent root = stage.getScene().getRoot();
        root.resize(width, height);
        root.applyCss();
        root.layout();
        var image = root.snapshot(null, null);
        BufferedImage png = new BufferedImage((int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < png.getHeight(); y++) {
            for (int x = 0; x < png.getWidth(); x++) {
                png.setRGB(x, y, image.getPixelReader().getArgb(x, y));
            }
        }
        Files.createDirectories(Path.of(folder));
        ImageIO.write(png, "png", Path.of(folder, name + ".png").toFile());
    }
}
