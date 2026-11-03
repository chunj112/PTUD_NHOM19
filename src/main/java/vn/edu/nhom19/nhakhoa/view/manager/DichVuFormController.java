package vn.edu.nhom19.nhakhoa.view.manager;

import java.math.BigDecimal;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import vn.edu.nhom19.nhakhoa.auth.Role;
import vn.edu.nhom19.nhakhoa.entity.DichVu;
import vn.edu.nhom19.nhakhoa.service.DichVuService;

public class DichVuFormController {
    @FXML private Label lblServiceFormTitle;
    @FXML private Button btnCloseServiceForm;
    @FXML private TextField txtServiceId;
    @FXML private TextField txtServiceName;
    @FXML private ComboBox<String> cboServiceGroup;
    @FXML private ComboBox<String> cboServiceUnit;
    @FXML private TextField txtServiceDuration;
    @FXML private TextField txtServicePrice;
    @FXML private ComboBox<String> cboServiceFdiRequirement;
    @FXML private TextArea txtServiceDescription;
    @FXML private ComboBox<String> cboServiceStatus;
    @FXML private Button btnCancelServiceForm;
    @FXML private Button btnSaveService;

    private DichVuService dichVuService;
    private DichVu dichVuHienTai;
    private boolean laCheDoThem = true;
    private Runnable onSaveThanhCong;

    @FXML
    private void initialize() {
        if (btnCloseServiceForm != null) {
            btnCloseServiceForm.setDisable(false);
            btnCloseServiceForm.setOnAction(e -> dongForm());
        }
        if (btnCancelServiceForm != null) {
            btnCancelServiceForm.setDisable(false);
            btnCancelServiceForm.setOnAction(e -> dongForm());
        }
        if (btnSaveService != null) {
            btnSaveService.setDisable(false);
            btnSaveService.setOnAction(e -> xuLyLuu());
        }
    }

    public void thietLapThemMoi(DichVuService service, Runnable onSaveThanhCong) {
        this.dichVuService = service;
        this.onSaveThanhCong = onSaveThanhCong;
        this.laCheDoThem = true;

        if (lblServiceFormTitle != null) lblServiceFormTitle.setText("Thêm dịch vụ");
        if (btnSaveService != null) btnSaveService.setText("Lưu dịch vụ");

        try {
            if (txtServiceId != null) txtServiceId.setText(service.layMaMoi());
        } catch (SQLException ignored) {
            if (txtServiceId != null) txtServiceId.setText("DV001");
        }

        if (cboServiceUnit != null && cboServiceUnit.getValue() == null) cboServiceUnit.setValue("Lần");
        if (cboServiceGroup != null && cboServiceGroup.getValue() == null) cboServiceGroup.setValue("Khám");
        if (cboServiceStatus != null && cboServiceStatus.getValue() == null) cboServiceStatus.setValue("Hoạt động");
    }

    public void thietLapChinhSua(DichVuService service, DichVu dv, Runnable onSaveThanhCong) {
        this.dichVuService = service;
        this.dichVuHienTai = dv;
        this.onSaveThanhCong = onSaveThanhCong;
        this.laCheDoThem = false;

        if (lblServiceFormTitle != null) lblServiceFormTitle.setText("Sửa dịch vụ");
        if (btnSaveService != null) btnSaveService.setText("Lưu thay đổi");

        if (txtServiceId != null) txtServiceId.setText(dv.getMaDV());
        if (txtServiceName != null) txtServiceName.setText(dv.getTenDV());
        if (cboServiceUnit != null) cboServiceUnit.setValue(dv.getDonViTinh());
        if (txtServiceDuration != null) {
            txtServiceDuration.setText(dv.getThoiGianThucHien() != null ? String.valueOf(dv.getThoiGianThucHien()) : "");
        }
        if (txtServicePrice != null) {
            txtServicePrice.setText(dv.getDonGia() != null ? dv.getDonGia().toPlainString() : "0");
        }
        if (txtServiceDescription != null) txtServiceDescription.setText(dv.getMoTa() != null ? dv.getMoTa() : "");
        if (cboServiceStatus != null) {
            cboServiceStatus.setValue(dv.isDangApDung() ? "Hoạt động" : "Ngừng hoạt động");
        }
    }

    private void xuLyLuu() {
        String ten = txtServiceName != null ? txtServiceName.getText().trim() : "";
        String donVi = cboServiceUnit != null && cboServiceUnit.getValue() != null ? cboServiceUnit.getValue().trim() : "";
        String giaStr = txtServicePrice != null ? txtServicePrice.getText().trim() : "";
        String thoiGianStr = txtServiceDuration != null ? txtServiceDuration.getText().trim() : "";
        String moTa = txtServiceDescription != null ? txtServiceDescription.getText().trim() : "";
        boolean hoatDong = cboServiceStatus == null || "Hoạt động".equals(cboServiceStatus.getValue());

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
                String ma = txtServiceId != null ? txtServiceId.getText().trim() : "";
                DichVu moi = new DichVu(ma, ten, donVi, donGia, thoiGian, moTa, hoatDong);
                dichVuService.themDichVu(moi, Role.MANAGER);
            } else {
                dichVuHienTai.setTenDV(ten);
                dichVuHienTai.setDonViTinh(donVi);
                dichVuHienTai.setDonGia(donGia);
                dichVuHienTai.setThoiGianThucHien(thoiGian);
                dichVuHienTai.setMoTa(moTa);
                dichVuHienTai.setDangApDung(hoatDong);
                dichVuService.capNhatDichVu(dichVuHienTai, Role.MANAGER);
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
        if (btnCancelServiceForm != null && btnCancelServiceForm.getScene() != null) {
            Stage stage = (Stage) btnCancelServiceForm.getScene().getWindow();
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
