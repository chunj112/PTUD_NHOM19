package iuh.se.nhom19.app;

import java.net.URL;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import iuh.se.nhom19.auth.PhienDangNhap;

/** Khởi động màn hình Login và đóng bộ điều hướng khi ứng dụng kết thúc. */
public class UngDungDentaFlow extends Application {
    private DieuHuongUngDung dieuHuongUngDung;
    @Override
    public void start(Stage cuaSo) throws Exception {
        // UC001 luôn xác thực bằng tài khoản SQL Server; không có đường đăng nhập theo Role.
        dieuHuongUngDung = new DieuHuongUngDung(cuaSo, new PhienDangNhap());
        dieuHuongUngDung.khoiDong();

        cuaSo.setMinWidth(1040);
        cuaSo.setMinHeight(650);
        URL bieuTuong = getClass().getResource("/images/dentaflow-logo.png");
        if (bieuTuong != null) {
            cuaSo.getIcons().add(new Image(bieuTuong.toExternalForm()));
        }
        cuaSo.show();
    }

    @Override public void stop() {
        if (dieuHuongUngDung != null) dieuHuongUngDung.close();
    }
}
