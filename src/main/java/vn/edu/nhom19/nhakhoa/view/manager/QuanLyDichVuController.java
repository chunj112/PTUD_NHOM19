package vn.edu.nhom19.nhakhoa.view.manager;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vn.edu.nhom19.nhakhoa.auth.Role;
import vn.edu.nhom19.nhakhoa.entity.DichVu;
import vn.edu.nhom19.nhakhoa.service.DichVuService;

public class QuanLyDichVuController {
    @FXML private VBox servicesPage;
    @FXML private Button btnAddService;
    @FXML private TextField txtServiceSearch;
    @FXML private ComboBox<String> cboServiceGroupFilter;
    @FXML private ComboBox<String> cboServiceStatusFilter;

    @FXML private TableView<DichVu> tblServices;
    @FXML private TableColumn<DichVu, String> colServiceId;
    @FXML private TableColumn<DichVu, String> colServiceName;
    @FXML private TableColumn<DichVu, String> colServiceGroup;
    @FXML private TableColumn<DichVu, String> colServiceUnit;
    @FXML private TableColumn<DichVu, String> colServicePrice;
    @FXML private TableColumn<DichVu, String> colServiceStatus;

    @FXML private Label lblServicesRecordCount;
    @FXML private VBox serviceDetailsPane;
    @FXML private VBox selectedServiceDetails;
    @FXML private Label lblServicesDemoNotice;

    private final DichVuService dichVuService = new DichVuService();
    private final ObservableList<DichVu> danhSachHienThi = FXCollections.observableArrayList();
    private List<DichVu> danhSachGoc = new ArrayList<>();
    private final DecimalFormat dinhDangTien = new DecimalFormat("#,### ₫");

    @FXML
    private void initialize() {
        cauHinhBang();
        cauHinhBoLoc();

        if (btnAddService != null) {
            btnAddService.setDisable(false);
            btnAddService.setOnAction(e -> moFormThem());
        }

        if (lblServicesDemoNotice != null) {
            lblServicesDemoNotice.setVisible(false);
            lblServicesDemoNotice.setManaged(false);
        }

        taiDuLieu();
    }

    private void cauHinhBang() {
        colServiceId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaDV()));
        colServiceName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenDV()));
        colServiceGroup.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getMoTa() != null && !d.getValue().getMoTa().isBlank() ? d.getValue().getMoTa() : "—"));
        colServiceUnit.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDonViTinh()));
        colServicePrice.setCellValueFactory(d -> {
            BigDecimal gia = d.getValue().getDonGia();
            return new SimpleStringProperty(gia != null ? dinhDangTien.format(gia) : "0 ₫");
        });
        colServiceStatus.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().isDangApDung() ? "Đang hoạt động" : "Ngừng hoạt động"));

        tblServices.setItems(danhSachHienThi);

        tblServices.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> {
            if (moi != null) {
                hienThiChiTiet(moi);
            } else if (serviceDetailsPane != null) {
                serviceDetailsPane.setVisible(false);
                serviceDetailsPane.setManaged(false);
            }
        });
    }

    private void cauHinhBoLoc() {
        if (txtServiceSearch != null) {
            txtServiceSearch.textProperty().addListener((obs, cu, moi) -> apDungBoLoc());
        }
        if (cboServiceStatusFilter != null) {
            cboServiceStatusFilter.valueProperty().addListener((obs, cu, moi) -> apDungBoLoc());
        }
        if (cboServiceGroupFilter != null) {
            cboServiceGroupFilter.valueProperty().addListener((obs, cu, moi) -> apDungBoLoc());
        }
    }

    public void taiDuLieu() {
        try {
            danhSachGoc = dichVuService.layDanhSach();
            apDungBoLoc();
        } catch (Exception e) {
            hienThongBaoLoi("Lỗi tải danh mục dịch vụ: " + e.getMessage());
        }
    }

    private void apDungBoLoc() {
        String tuKhoa = txtServiceSearch != null ? txtServiceSearch.getText() : "";
        String trangThai = cboServiceStatusFilter != null ? cboServiceStatusFilter.getValue() : null;
        String nhom = cboServiceGroupFilter != null ? cboServiceGroupFilter.getValue() : null;

        List<DichVu> ketQua = dichVuService.timKiem(danhSachGoc, tuKhoa, trangThai, nhom);
        danhSachHienThi.setAll(ketQua);

        if (lblServicesRecordCount != null) {
            lblServicesRecordCount.setText(ketQua.size() + " dịch vụ");
        }
    }

    private void hienThiChiTiet(DichVu dv) {
        if (serviceDetailsPane == null || selectedServiceDetails == null) return;
        serviceDetailsPane.setVisible(true);
        serviceDetailsPane.setManaged(true);

        datGiaTriLabel(selectedServiceDetails, "#lblSelectedServiceName", dv.getTenDV());
        datGiaTriLabel(selectedServiceDetails, "#lblSelectedServiceId", dv.getMaDV());
        datGiaTriLabel(selectedServiceDetails, "#lblSelectedServiceStatus",
                dv.isDangApDung() ? "Đang hoạt động" : "Ngừng hoạt động");
        datGiaTriLabel(selectedServiceDetails, "#lblServiceDetailGroup",
                dv.getMoTa() != null && !dv.getMoTa().isBlank() ? dv.getMoTa() : "—");
        datGiaTriLabel(selectedServiceDetails, "#lblServiceDetailUnit", dv.getDonViTinh());
        datGiaTriLabel(selectedServiceDetails, "#lblServiceDetailPrice",
                dv.getDonGia() != null ? dinhDangTien.format(dv.getDonGia()) : "0 ₫");
        datGiaTriLabel(selectedServiceDetails, "#lblServiceDetailDuration",
                dv.getThoiGianThucHien() != null ? dv.getThoiGianThucHien() + " phút" : "—");
        datGiaTriLabel(selectedServiceDetails, "#lblServiceDetailDescription",
                dv.getMoTa() != null && !dv.getMoTa().isBlank() ? dv.getMoTa() : "Không có mô tả");

        Button btnEdit = (Button) selectedServiceDetails.lookup("#btnEditService");
        if (btnEdit != null) {
            btnEdit.setDisable(false);
            btnEdit.setOnAction(e -> moFormSua(dv));
        }

        Button btnToggle = (Button) selectedServiceDetails.lookup("#btnToggleServiceStatus");
        if (btnToggle != null) {
            btnToggle.setDisable(false);
            btnToggle.setText(dv.isDangApDung() ? "Ngừng áp dụng" : "Kích hoạt lại");
            btnToggle.setOnAction(e -> xuLyDoiTrangThai(dv));
        }
    }

    private void xuLyDoiTrangThai(DichVu dv) {
        try {
            boolean trangThaiMoi = !dv.isDangApDung();
            dichVuService.doiTrangThai(dv.getMaDV(), trangThaiMoi, Role.MANAGER);
            dv.setDangApDung(trangThaiMoi);
            hienThiChiTiet(dv);
            tblServices.refresh();
        } catch (Exception e) {
            hienThongBaoLoi("Lỗi đổi trạng thái: " + e.getMessage());
        }
    }

    private void moFormThem() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/manager/service-add.fxml"));
            Parent root = loader.load();
            DichVuFormController formCtrl = loader.getController();
            formCtrl.thietLapThemMoi(dichVuService, this::taiDuLieu);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Thêm dịch vụ mới");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            hienThongBaoLoi("Không thể mở form thêm dịch vụ: " + e.getMessage());
        }
    }

    private void moFormSua(DichVu dv) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/manager/service-edit.fxml"));
            Parent root = loader.load();
            DichVuFormController formCtrl = loader.getController();
            formCtrl.thietLapChinhSua(dichVuService, dv, () -> {
                taiDuLieu();
                hienThiChiTiet(dv);
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Sửa dịch vụ");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            hienThongBaoLoi("Không thể mở form sửa dịch vụ: " + e.getMessage());
        }
    }

    private void datGiaTriLabel(Parent root, String selector, String giaTri) {
        Label lbl = (Label) root.lookup(selector);
        if (lbl != null) {
            lbl.setText(giaTri != null ? giaTri : "—");
        }
    }

    private void hienThongBaoLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
