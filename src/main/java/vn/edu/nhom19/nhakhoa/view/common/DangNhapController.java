package vn.edu.nhom19.nhakhoa.view.common;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import vn.edu.nhom19.nhakhoa.app.AppNavigator;
import vn.edu.nhom19.nhakhoa.auth.KetQuaDangNhap;
import vn.edu.nhom19.nhakhoa.service.DangNhapService;
import vn.edu.nhom19.nhakhoa.service.LoiDangNhap;

/** Controller chỉ đọc form/hiện kết quả; SQL và PBKDF2 chạy trên worker, không trên JavaFX thread. */
public final class DangNhapController {
    @FXML private TextField txtTenDangNhap, txtMatKhauHien;
    @FXML private PasswordField txtMatKhau;
    @FXML private Button btnDangNhap;
    @FXML private Label lblLoiDangNhap;
    @FXML private HBox vungDangXuLy;
    @FXML private CheckBox chkHienMatKhau;
    private AppNavigator navigator;
    private DangNhapService dichVuDangNhap;
    private Task<KetQuaDangNhap> tacVuDangNhap;
    private KetQuaDangNhap ketQuaDaXacThuc;
    private boolean daDong;

    @FXML private void initialize() {
        txtMatKhauHien.textProperty().bindBidirectional(txtMatKhau.textProperty());
        hienThiLoi("");
        capNhatTrangThaiDangNhap(true);
    }
    public void configure(AppNavigator boDieuHuong, DangNhapService dichVu) {
        if (boDieuHuong == null || dichVu == null || daDong) {
            throw new IllegalStateException("Thiếu bộ điều hướng hoặc dịch vụ đăng nhập.");
        }
        navigator = boDieuHuong;
        dichVuDangNhap = dichVu;
        capNhatTrangThaiDangNhap(false);
    }
    @FXML private void xuLyDangNhap() {
        // Một lần chỉ gửi một yêu cầu; Controller đã đóng không được đăng nhập lại.
        if (daDong || tacVuDangNhap != null || navigator == null) return;
        String tenDangNhap = txtTenDangNhap.getText().trim();
        String matKhau = txtMatKhau.getText();
        hienThiLoi("");
        if (tenDangNhap.isBlank() || matKhau.isBlank()) {
            hienThiLoi("Vui lòng nhập tên đăng nhập và mật khẩu.");
            return;
        }

        // JDBC và PBKDF2 chạy nền để giao diện không bị đơ.
        capNhatTrangThaiDangNhap(true);
        DangNhapService dichVu = dichVuDangNhap;
        Task<KetQuaDangNhap> tacVu = new Task<>() {
            @Override protected KetQuaDangNhap call() {
                return dichVu.dangNhap(tenDangNhap, matKhau);
            }
        };
        tacVuDangNhap = tacVu;
        tacVu.setOnSucceeded(suKien -> dangNhapThanhCong(tacVu));
        tacVu.setOnFailed(suKien -> dangNhapThatBai(tacVu));
        Thread luong = new Thread(tacVu, "dentaflow-dang-nhap");
        luong.setDaemon(true);
        luong.start();
    }

    private void dangNhapThanhCong(Task<KetQuaDangNhap> tacVu) {
        if (daDong || tacVuDangNhap != tacVu) return;
        txtMatKhau.clear();
        // Chỉ Service thành công mới tạo được kết quả để Navigator mở AppShell.
        ketQuaDaXacThuc = tacVu.getValue();
        try {
            navigator.moSauDangNhap();
        } catch (RuntimeException loi) {
            ketQuaDaXacThuc = null;
            hienThiLoi("Không mở được giao diện sau đăng nhập. Vui lòng kiểm tra tài nguyên ứng dụng.");
        }
        if (!daDong) {
            tacVuDangNhap = null;
            capNhatTrangThaiDangNhap(false);
        }
    }

    private void dangNhapThatBai(Task<KetQuaDangNhap> tacVu) {
        if (daDong || tacVuDangNhap != tacVu) return;
        tacVuDangNhap = null;
        txtMatKhau.clear();
        capNhatTrangThaiDangNhap(false);
        Throwable loi = tacVu.getException();
        if (loi instanceof LoiDangNhap) {
            hienThiLoi(loi.getMessage());
        } else {
            hienThiLoi("Không thể đăng nhập lúc này. Vui lòng thử lại.");
        }
    }

    /** Navigator chỉ đọc kết quả của lần đăng nhập thành công trên form hiện tại. */
    public KetQuaDangNhap layKetQuaDaXacThuc() {
        return ketQuaDaXacThuc;
    }
    @FXML private void doiHienThiMatKhau() {
        boolean hien = chkHienMatKhau.isSelected();
        txtMatKhau.setVisible(!hien);
        txtMatKhau.setManaged(!hien);
        txtMatKhauHien.setVisible(hien);
        txtMatKhauHien.setManaged(hien);
    }

    private void capNhatTrangThaiDangNhap(boolean xuLy) {
        boolean khoa = xuLy || daDong || navigator == null;
        btnDangNhap.setDisable(khoa);
        txtTenDangNhap.setDisable(khoa);
        txtMatKhau.setDisable(khoa);
        txtMatKhauHien.setDisable(khoa);
        chkHienMatKhau.setDisable(khoa);
        boolean hien = xuLy && navigator != null && !daDong;
        vungDangXuLy.setVisible(hien);
        vungDangXuLy.setManaged(hien);
    }

    private void hienThiLoi(String thongBao) {
        lblLoiDangNhap.setText(thongBao);
        lblLoiDangNhap.setVisible(!thongBao.isBlank());
        lblLoiDangNhap.setManaged(!thongBao.isBlank());
    }

    public void dispose() {
        daDong = true;
        if (tacVuDangNhap != null) tacVuDangNhap.cancel(true);
        tacVuDangNhap = null;
        ketQuaDaXacThuc = null;
        txtMatKhau.clear();
        txtMatKhauHien.textProperty().unbindBidirectional(txtMatKhau.textProperty());
        txtMatKhauHien.clear();
        txtTenDangNhap.clear();
        hienThiLoi("");
        capNhatTrangThaiDangNhap(false);
        navigator = null;
        dichVuDangNhap = null;
    }
}
