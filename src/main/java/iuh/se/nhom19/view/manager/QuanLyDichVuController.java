package iuh.se.nhom19.view.manager;

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
import iuh.se.nhom19.auth.VaiTro;
import iuh.se.nhom19.entity.DichVu;
import iuh.se.nhom19.service.DichVuService;

public class QuanLyDichVuController {
    @FXML private VBox danhMucDichVuTrang;
    @FXML private Button btnThemDichVu;
    @FXML private TextField txtDichVuTimKiem;
    @FXML private ComboBox<String> cboDichVuNhomBoLoc;
    @FXML private ComboBox<String> cboDichVuTinhTrangBoLoc;

    @FXML private TableView<DichVu> tblDanhMucDichVu;
    @FXML private TableColumn<DichVu, String> colDichVuMa;
    @FXML private TableColumn<DichVu, String> colDichVuTen;
    @FXML private TableColumn<DichVu, String> colDichVuNhom;
    @FXML private TableColumn<DichVu, String> colDichVuDonVi;
    @FXML private TableColumn<DichVu, String> colDichVuDonGia;
    @FXML private TableColumn<DichVu, String> colDichVuTinhTrang;

    @FXML private Label lblDanhMucDichVuHoSoSoLuong;
    @FXML private VBox dichVuPhanChiTietKhung;
    @FXML private VBox daChonDichVuPhanChiTiet;

    private final DichVuService dichVuService = new DichVuService();
    private final ObservableList<DichVu> danhSachHienThi = FXCollections.observableArrayList();
    private List<DichVu> danhSachGoc = new ArrayList<>();
    private final DecimalFormat dinhDangTien = new DecimalFormat("#,### ₫");

    @FXML
    private void initialize() {
        cauHinhBang();
        cauHinhBoLoc();

        if (btnThemDichVu != null) {
            btnThemDichVu.setDisable(false);
            btnThemDichVu.setOnAction(e -> moFormThem());
        }

        taiDuLieu();
    }

    private void cauHinhBang() {
        colDichVuMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaDV()));
        colDichVuTen.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenDV()));
        colDichVuNhom.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getMoTa() != null && !d.getValue().getMoTa().isBlank() ? d.getValue().getMoTa() : "—"));
        colDichVuDonVi.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDonViTinh()));
        colDichVuDonGia.setCellValueFactory(d -> {
            BigDecimal gia = d.getValue().getDonGia();
            return new SimpleStringProperty(gia != null ? dinhDangTien.format(gia) : "0 ₫");
        });
        colDichVuTinhTrang.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().isDangApDung() ? "Đang hoạt động" : "Ngừng hoạt động"));

        tblDanhMucDichVu.setItems(danhSachHienThi);

        tblDanhMucDichVu.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> {
            if (moi != null) {
                hienThiChiTiet(moi);
            } else if (dichVuPhanChiTietKhung != null) {
                dichVuPhanChiTietKhung.setVisible(false);
                dichVuPhanChiTietKhung.setManaged(false);
            }
        });
    }

    private void cauHinhBoLoc() {
        if (txtDichVuTimKiem != null) {
            txtDichVuTimKiem.textProperty().addListener((obs, cu, moi) -> apDungBoLoc());
        }
        if (cboDichVuTinhTrangBoLoc != null) {
            cboDichVuTinhTrangBoLoc.valueProperty().addListener((obs, cu, moi) -> apDungBoLoc());
        }
        if (cboDichVuNhomBoLoc != null) {
            cboDichVuNhomBoLoc.valueProperty().addListener((obs, cu, moi) -> apDungBoLoc());
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
        String tuKhoa = txtDichVuTimKiem != null ? txtDichVuTimKiem.getText() : "";
        String trangThai = cboDichVuTinhTrangBoLoc != null ? cboDichVuTinhTrangBoLoc.getValue() : null;
        String nhom = cboDichVuNhomBoLoc != null ? cboDichVuNhomBoLoc.getValue() : null;

        List<DichVu> ketQua = dichVuService.timKiem(danhSachGoc, tuKhoa, trangThai, nhom);
        danhSachHienThi.setAll(ketQua);

        if (lblDanhMucDichVuHoSoSoLuong != null) {
            lblDanhMucDichVuHoSoSoLuong.setText(ketQua.size() + " dịch vụ");
        }
    }

    private void hienThiChiTiet(DichVu dv) {
        if (dichVuPhanChiTietKhung == null) return;
        dichVuPhanChiTietKhung.setVisible(true);
        dichVuPhanChiTietKhung.setManaged(true);

        Parent pane = daChonDichVuPhanChiTiet != null ? daChonDichVuPhanChiTiet : dichVuPhanChiTietKhung;

        datGiaTriLabel(pane, "#lblDaChonDichVuTen", dv.getTenDV());
        datGiaTriLabel(pane, "#lblDaChonDichVuMa", dv.getMaDV());
        datGiaTriLabel(pane, "#lblDaChonDichVuTinhTrang",
                dv.isDangApDung() ? "Đang hoạt động" : "Ngừng hoạt động");
        datGiaTriLabel(pane, "#lblDichVuChiTietNhom",
                dv.getMoTa() != null && !dv.getMoTa().isBlank() ? dv.getMoTa() : "—");
        datGiaTriLabel(pane, "#lblDichVuChiTietDonVi", dv.getDonViTinh());
        datGiaTriLabel(pane, "#lblDichVuChiTietDonGia",
                dv.getDonGia() != null ? dinhDangTien.format(dv.getDonGia()) : "0 ₫");
        datGiaTriLabel(pane, "#lblDichVuChiTietThoiLuong",
                dv.getThoiGianThucHien() != null ? dv.getThoiGianThucHien() + " phút" : "—");
        datGiaTriLabel(pane, "#lblDichVuChiTietMoTa",
                dv.getMoTa() != null && !dv.getMoTa().isBlank() ? dv.getMoTa() : "Không có mô tả");

        Button btnEdit = (Button) pane.lookup("#btnSuaDichVu");
        if (btnEdit != null) {
            btnEdit.setDisable(false);
            btnEdit.setOnAction(e -> moFormSua(dv));
        }

        Button btnToggle = (Button) pane.lookup("#btnDoiTrangThaiDichVu");
        if (btnToggle != null) {
            btnToggle.setDisable(false);
            btnToggle.setText(dv.isDangApDung() ? "Ngừng áp dụng" : "Kích hoạt lại");
            btnToggle.setOnAction(e -> xuLyDoiTrangThai(dv));
        }
    }

    private void xuLyDoiTrangThai(DichVu dv) {
        try {
            boolean trangThaiMoi = !dv.isDangApDung();
            dichVuService.doiTrangThai(dv.getMaDV(), trangThaiMoi, VaiTro.MANAGER);
            dv.setDangApDung(trangThaiMoi);
            hienThiChiTiet(dv);
            tblDanhMucDichVu.refresh();
        } catch (Exception e) {
            hienThongBaoLoi("Lỗi đổi trạng thái: " + e.getMessage());
        }
    }

    private void moFormThem() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/manager/them-dich-vu.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/manager/sua-dich-vu.fxml"));
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
