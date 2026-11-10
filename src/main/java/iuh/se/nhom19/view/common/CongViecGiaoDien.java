package iuh.se.nhom19.view.common;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.*;
import iuh.se.nhom19.auth.PhienDangNhap;

/** Hai thao tác UI dùng chung: chạy JDBC nền và mở form FXML. Không chứa SQL/nghiệp vụ. */
public final class CongViecGiaoDien {
    private CongViecGiaoDien() {}

    public static void anKhiTrong(Label nhan) {
        nhan.visibleProperty().bind(nhan.textProperty().isNotEmpty());
        nhan.managedProperty().bind(nhan.visibleProperty());
    }

    public static <T> void chay(Node vung, Label thongBao, PhienDangNhap phien, Callable<T> congViec, Consumer<T> khiXong) {
        if (phien == null || !phien.dangHoatDong() || Boolean.TRUE.equals(vung.getProperties().get("dangChay"))) return;
        long phienBan = phien.layPhienBan();
        if (!conDuocHienThi(vung, phien, phienBan)) return;
        vung.getProperties().put("dangChay", true); vung.setDisable(true); thongBao.setText("Đang xử lý…");
        // SQL không chạy trên JavaFX Application Thread. Chỉ callback của Task mới cập nhật control.
        Task<T> tacVu = new Task<>() { @Override protected T call() throws Exception { return congViec.call(); } };
        tacVu.setOnSucceeded(suKien -> {
            vung.getProperties().remove("dangChay"); vung.setDisable(false);
            if (!conDuocHienThi(vung, phien, phienBan)) return;
            thongBao.setText(""); khiXong.accept(tacVu.getValue());
        });
        tacVu.setOnFailed(suKien -> {
            vung.getProperties().remove("dangChay"); vung.setDisable(false);
            if (conDuocHienThi(vung, phien, phienBan)) thongBao.setText(thongBaoLoi(tacVu.getException()));
        });
        Thread luong = new Thread(tacVu, "dentaflow-nghiep-vu"); luong.setDaemon(true); luong.start();
    }

    /** Tra cứu gợi ý nền mà không khóa form hoặc làm mất focus ở các trường người dùng đang nhập. */
    public static <T> void chayKhongKhoa(Node vung, Label thongBao, PhienDangNhap phien,
            Callable<T> congViec, BooleanSupplier ketQuaConHieuLuc, Consumer<T> khiXong) {
        if (phien == null || !phien.dangHoatDong()) return;
        long phienBan = phien.layPhienBan();
        if (!conDuocHienThi(vung, phien, phienBan)) return;

        Task<T> tacVu = new Task<>() { @Override protected T call() throws Exception { return congViec.call(); } };
        tacVu.setOnSucceeded(suKien -> {
            if (!conDuocHienThi(vung, phien, phienBan) || !ketQuaConHieuLuc.getAsBoolean()) return;
            thongBao.setText(""); khiXong.accept(tacVu.getValue());
        });
        tacVu.setOnFailed(suKien -> {
            if (conDuocHienThi(vung, phien, phienBan) && ketQuaConHieuLuc.getAsBoolean())
                thongBao.setText(thongBaoLoi(tacVu.getException()));
        });
        Thread luong = new Thread(tacVu, "dentaflow-tra-cuu"); luong.setDaemon(true); luong.start();
    }

    private static boolean conDuocHienThi(Node vung, PhienDangNhap phien, long phienBan) {
        // Kết quả cũ không được đưa lên UI sau logout, đổi tài khoản hoặc đóng modal.
        if (!phien.dangHoatDong() || phien.layPhienBan()!=phienBan) return false;
        // Form con fx:include cũng phải dừng callback khi modal cha đóng hoặc tab cha không còn được chọn.
        for(Node nut=vung;nut!=null;nut=nut.getParent()) {
            if(Boolean.TRUE.equals(nut.getProperties().get("daDong")) || Boolean.TRUE.equals(nut.getProperties().get("tamDung")))return false;
        }
        return true;
    }

    public static String thongBaoLoi(Throwable loi) {
        if (loi instanceof IllegalArgumentException || loi instanceof IllegalStateException) return loi.getMessage();
        if (loi instanceof SQLException sql) {
            if (sql.getErrorCode() == 51002) return "Nha sĩ có lịch hẹn trùng giờ. Vui lòng chọn lại.";
            if (sql.getErrorCode() == 51003) return "Lịch hẹn không nằm trong ca phân công hiện tại.";
            if (sql.getErrorCode() == 2601 || sql.getErrorCode() == 2627) return "Bản ghi đã tồn tại hoặc lịch hẹn đã được tiếp nhận. Hãy làm mới.";
            if (sql.getErrorCode() == 1205 || sql.getErrorCode() == 1222) return "Dữ liệu đang được người khác cập nhật. Hãy làm mới và thử lại.";
            // Không hiện URL, tài khoản SQL hoặc thông tin kỹ thuật từ exception JDBC.
            return "Không thể đọc/lưu dữ liệu SQL Server. Kiểm tra kết nối, quyền và ràng buộc dữ liệu rồi thử lại.";
        }
        return "Không thể thực hiện thao tác. Vui lòng kiểm tra dữ liệu và thử lại.";
    }

    public static void moHopThoai(Node chuSoHuu, String tepFXML, String tieuDe, Consumer<FXMLLoader> cauHinh) {
        try {
            FXMLLoader boNap = new FXMLLoader(CongViecGiaoDien.class.getResource(tepFXML));
            Parent noiDung = boNap.load();
            Stage cuaSo = new Stage(); cuaSo.initOwner(chuSoHuu.getScene().getWindow()); cuaSo.initModality(Modality.WINDOW_MODAL);
            cuaSo.setTitle("DentaFlow · " + tieuDe);
            // Hiện đủ form khi màn hình cho phép; chỉ cuộn nếu form cao hơn vùng làm việc thực tế.
            boolean coVungCuonNoi = noiDung.getStyleClass().contains("hop-thoai-co-cuon-noi");
            Parent gocCanh;
            if (coVungCuonNoi) gocCanh = noiDung;
            else {
                ScrollPane cuon = new ScrollPane(noiDung); cuon.setFitToWidth(true); cuon.setFitToHeight(true); cuon.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                cuon.getStyleClass().add("khung-ung-dung"); gocCanh = cuon;
            }
            Scene khungCanh = new Scene(gocCanh); cuaSo.setScene(khungCanh);
            khungCanh.getStylesheets().add(CongViecGiaoDien.class.getResource("/css/chu-de.css").toExternalForm());
            // Áp CSS từ ScrollPane gốc để skin của các control/form con được tạo trước khi đo chiều cao.
            // Chỉ applyCss trên content khi ScrollPane chưa có skin có thể đo form thành vài trăm pixel.
            khungCanh.getRoot().applyCss();
            noiDung.applyCss();
            var chu=cuaSo.getOwner();
            var cacManHinh=Screen.getScreensForRectangle(chu.getX(),chu.getY(),Math.max(1,chu.getWidth()),Math.max(1,chu.getHeight()));
            // Cửa sổ cha mới mở/ở ngoài màn hình có thể chưa khớp Screen nào; không gọi getFirst trên danh sách rỗng.
            Screen manHinh=cacManHinh.isEmpty()?Screen.getPrimary():cacManHinh.getFirst();
            var vungHienThi=manHinh.getVisualBounds();
            cuaSo.setWidth(Math.min(noiDung.prefWidth(-1)+36,vungHienThi.getWidth()-40));
            cuaSo.setHeight(Math.min(noiDung.prefHeight(noiDung.prefWidth(-1))+55,vungHienThi.getHeight()-50));
            cuaSo.setX(vungHienThi.getMinX()+(vungHienThi.getWidth()-cuaSo.getWidth())/2);
            cuaSo.setY(vungHienThi.getMinY()+(vungHienThi.getHeight()-cuaSo.getHeight())/2);
            cuaSo.getProperties().put("hopThoaiNghiepVu", true);
            cuaSo.setOnHidden(suKien -> noiDung.getProperties().put("daDong", true));
            cauHinh.accept(boNap); cuaSo.show();
        } catch (IOException loi) { throw new IllegalStateException("Không thể mở form nghiệp vụ. Kiểm tra FXML.", loi); }
    }

    public static void dong(Node noiDung) { noiDung.getScene().getWindow().hide(); }

    public static void canChinhHopThoai(Node thanhPhan) {
        // Chuyển từ tìm hồ sơ sang nhập người mới làm form cao hơn; tận dụng màn hình trước khi cần cuộn.
        if(thanhPhan.getScene()==null || !(thanhPhan.getScene().getWindow() instanceof Stage cuaSo)
            || !cuaSo.isShowing() || !Boolean.TRUE.equals(cuaSo.getProperties().get("hopThoaiNghiepVu")))return;
        Parent noiDung;
        if(cuaSo.getScene().getRoot() instanceof ScrollPane cuon && cuon.getContent() instanceof Parent benTrong) noiDung=benTrong;
        else return;
        cuon.applyCss(); noiDung.applyCss();
        var cacManHinh=Screen.getScreensForRectangle(cuaSo.getX(),cuaSo.getY(),cuaSo.getWidth(),cuaSo.getHeight());
        var vung=(cacManHinh.isEmpty()?Screen.getPrimary():cacManHinh.getFirst()).getVisualBounds();
        double chieuCao=Math.min(noiDung.prefHeight(noiDung.prefWidth(-1))+55,vung.getHeight()-50);
        cuaSo.setHeight(chieuCao); cuaSo.setY(Math.max(vung.getMinY(),Math.min(cuaSo.getY(),vung.getMaxY()-chieuCao)));
    }
}
