package iuh.se.nhom19.view.manager;

import java.math.BigDecimal;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import iuh.se.nhom19.auth.VaiTro;
import iuh.se.nhom19.entity.DichVu;
import iuh.se.nhom19.service.DichVuService;

public class DichVuFormController {
    @FXML private Label lblDichVuBieuMauTieuDe;
    @FXML private Button btnDongDichVuBieuMau;
    @FXML private TextField txtDichVuMa;
    @FXML private TextField txtDichVuTen;
    @FXML private ComboBox<String> cboDichVuNhom;
    @FXML private ComboBox<String> cboDichVuDonVi;
    @FXML private TextField txtDichVuThoiLuong;
    @FXML private TextField txtDichVuDonGia;
    @FXML private ComboBox<String> cboDichVuFdiDieuKien;
    @FXML private TextArea txtDichVuMoTa;
    @FXML private ComboBox<String> cboDichVuTinhTrang;
    @FXML private Button btnHuyDichVuBieuMau;
    @FXML private Button btnLuuDichVu;

    private DichVuService dichVuService;
    private DichVu dichVuHienTai;
    private boolean laCheDoThem = true;
    private Runnable onSaveThanhCong;

    @FXML
    private void initialize() {
        if (btnDongDichVuBieuMau != null) {
            btnDongDichVuBieuMau.setDisable(false);
            btnDongDichVuBieuMau.setOnAction(e -> dongForm());
        }
        if (btnHuyDichVuBieuMau != null) {
            btnHuyDichVuBieuMau.setDisable(false);
            btnHuyDichVuBieuMau.setOnAction(e -> dongForm());
        }
        if (btnLuuDichVu != null) {
            btnLuuDichVu.setDisable(false);
            btnLuuDichVu.setOnAction(e -> xuLyLuu());
        }
    }

    public void thietLapThemMoi(DichVuService service, Runnable onSaveThanhCong) {
        this.dichVuService = service;
        this.onSaveThanhCong = onSaveThanhCong;
        this.laCheDoThem = true;

        if (lblDichVuBieuMauTieuDe != null) lblDichVuBieuMauTieuDe.setText("Thêm dịch vụ");
        if (btnLuuDichVu != null) btnLuuDichVu.setText("Lưu dịch vụ");

        try {
            if (txtDichVuMa != null) txtDichVuMa.setText(service.layMaMoi());
        } catch (SQLException ignored) {
            if (txtDichVuMa != null) txtDichVuMa.setText("DV001");
        }

        if (cboDichVuDonVi != null && cboDichVuDonVi.getValue() == null) cboDichVuDonVi.setValue("Lần");
        if (cboDichVuNhom != null && cboDichVuNhom.getValue() == null) cboDichVuNhom.setValue("Khám");
        if (cboDichVuTinhTrang != null && cboDichVuTinhTrang.getValue() == null) cboDichVuTinhTrang.setValue("Hoạt động");
    }

    public void thietLapChinhSua(DichVuService service, DichVu dv, Runnable onSaveThanhCong) {
        this.dichVuService = service;
        this.dichVuHienTai = dv;
        this.onSaveThanhCong = onSaveThanhCong;
        this.laCheDoThem = false;

        if (lblDichVuBieuMauTieuDe != null) lblDichVuBieuMauTieuDe.setText("Sửa dịch vụ");
        if (btnLuuDichVu != null) btnLuuDichVu.setText("Lưu thay đổi");

        if (txtDichVuMa != null) txtDichVuMa.setText(dv.getMaDV());
        if (txtDichVuTen != null) txtDichVuTen.setText(dv.getTenDV());
        if (cboDichVuDonVi != null) cboDichVuDonVi.setValue(dv.getDonViTinh());
        if (txtDichVuThoiLuong != null) {
            txtDichVuThoiLuong.setText(dv.getThoiGianThucHien() != null ? String.valueOf(dv.getThoiGianThucHien()) : "");
        }
        if (txtDichVuDonGia != null) {
            txtDichVuDonGia.setText(dv.getDonGia() != null ? dv.getDonGia().toPlainString() : "0");
        }
        if (txtDichVuMoTa != null) txtDichVuMoTa.setText(dv.getMoTa() != null ? dv.getMoTa() : "");
        if (cboDichVuTinhTrang != null) {
            cboDichVuTinhTrang.setValue(dv.isDangApDung() ? "Hoạt động" : "Ngừng hoạt động");
        }
    }

    private void xuLyLuu() {
        String ten = txtDichVuTen != null ? txtDichVuTen.getText().trim() : "";
        String donVi = cboDichVuDonVi != null && cboDichVuDonVi.getValue() != null ? cboDichVuDonVi.getValue().trim() : "";
        String giaStr = txtDichVuDonGia != null ? txtDichVuDonGia.getText().trim() : "";
        String thoiGianStr = txtDichVuThoiLuong != null ? txtDichVuThoiLuong.getText().trim() : "";
        String moTa = txtDichVuMoTa != null ? txtDichVuMoTa.getText().trim() : "";
        boolean hoatDong = cboDichVuTinhTrang == null || "Hoạt động".equals(cboDichVuTinhTrang.getValue());

        if (ten.isEmpty()) {
            hienThongBaoLoi("Tên dịch vụ không được để trống.");
            return;
        }
        if (donVi.isEmpty()) {
            hienThongBaoLoi("Vui lòng chọn hoặc nhập đơn vị tính.");
            return;
        }

        BigDecimal donGia;
        try {
            donGia = new BigDecimal(giaStr);
            if (donGia.compareTo(BigDecimal.ZERO) < 0) {
                hienThongBaoLoi("Đơn giá dịch vụ phải lớn hơn hoặc bằng 0.");
                return;
            }
        } catch (Exception e) {
            hienThongBaoLoi("Đơn giá không hợp lệ. Vui lòng nhập số.");
            return;
        }

        Integer thoiGian = null;
        if (!thoiGianStr.isEmpty()) {
            try {
                thoiGian = Integer.parseInt(thoiGianStr);
                if (thoiGian <= 0) {
                    hienThongBaoLoi("Thời gian thực hiện phải lớn hơn 0 phút.");
                    return;
                }
            } catch (NumberFormatException e) {
                hienThongBaoLoi("Thời gian thực hiện phải là số nguyên.");
                return;
            }
        }

        try {
            if (laCheDoThem) {
                String ma = txtDichVuMa != null ? txtDichVuMa.getText().trim() : "";
                DichVu moi = new DichVu(ma, ten, donVi, donGia, thoiGian, moTa, hoatDong);
                dichVuService.themDichVu(moi, VaiTro.MANAGER);
            } else {
                dichVuHienTai.setTenDV(ten);
                dichVuHienTai.setDonViTinh(donVi);
                dichVuHienTai.setDonGia(donGia);
                dichVuHienTai.setThoiGianThucHien(thoiGian);
                dichVuHienTai.setMoTa(moTa);
                dichVuHienTai.setDangApDung(hoatDong);
                dichVuService.capNhatDichVu(dichVuHienTai, VaiTro.MANAGER);
            }

            if (onSaveThanhCong != null) {
                onSaveThanhCong.run();
            }
            dongForm();
        } catch (Exception e) {
            hienThongBaoLoi("Không thể lưu dịch vụ: " + e.getMessage());
        }
    }

    private void dongForm() {
        if (btnHuyDichVuBieuMau != null && btnHuyDichVuBieuMau.getScene() != null) {
            Stage stage = (Stage) btnHuyDichVuBieuMau.getScene().getWindow();
            if (stage != null) stage.close();
        }
    }

    private void hienThongBaoLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
