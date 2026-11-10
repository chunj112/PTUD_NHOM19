package iuh.se.nhom19.view.nurse;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import iuh.se.nhom19.auth.PhienDangNhap;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.dao.PhieuKhamDAO;
import iuh.se.nhom19.entity.PhieuKham;
import iuh.se.nhom19.service.DanhSachChoYTaService;
import iuh.se.nhom19.view.common.CongViecGiaoDien;
import iuh.se.nhom19.view.common.TrangThaiGiaoDien;

/** Y tá chỉ xem hàng chờ; controller không có thao tác gọi hay đổi trạng thái phiếu. */
public class DanhSachChoYTaController {
    private static final ZoneId MUI_GIO = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private VBox trangChoYTa;
    @FXML private DatePicker dpNgayCho;
    @FXML private ComboBox<String> cbPhongCho, cbTrangThaiCho;
    @FXML private Button btnLamMoiChoYTa;
    @FXML private Label lblThongTinCa, lblSoLuongCho, lblSoLuongDangKham, lblSoLuongDaKham, lblThongBaoChoYTa;
    @FXML private TableView<PhieuKham> tblChoYTa;
    @FXML private TableColumn<PhieuKham,String> cotSTT, cotBenhNhan, cotSDT, cotThoiDiemTiepNhan,
        cotNhaSi, cotPhong, cotTrangThai;

    private PhienDangNhap phien;
    private DanhSachChoYTaService dichVu;
    private boolean dangNapBoLoc;

    @FXML private void initialize() {
        cotSTT.setCellValueFactory(dong -> new ReadOnlyStringWrapper(
            dong.getValue().laySoThuTuKham() == null ? "—" : String.format("%03d", dong.getValue().laySoThuTuKham())));
        cotBenhNhan.setCellValueFactory(dong -> new ReadOnlyStringWrapper(dong.getValue().layBenhNhan().layHoTen()));
        cotSDT.setCellValueFactory(dong -> new ReadOnlyStringWrapper(dong.getValue().layBenhNhan().laySoDienThoai()));
        cotThoiDiemTiepNhan.setCellValueFactory(dong -> new ReadOnlyStringWrapper(
            dong.getValue().layThoiDiemTiepNhan().toLocalTime().format(DINH_DANG_GIO)));
        cotNhaSi.setCellValueFactory(dong -> new ReadOnlyStringWrapper(dong.getValue().layTenNhaSi()));
        cotPhong.setCellValueFactory(dong -> new ReadOnlyStringWrapper(dong.getValue().layMaPhong()));
        cotTrangThai.setCellValueFactory(dong -> new ReadOnlyStringWrapper(dong.getValue().layTrangThaiKham()));
        TrangThaiGiaoDien.cot(cotTrangThai);
        cbTrangThaiCho.getItems().setAll("Tất cả trạng thái", "CHO_KHAM", "DANG_KHAM", "DA_KHAM");
        cbTrangThaiCho.getSelectionModel().selectFirst();
        TrangThaiGiaoDien.boLoc(cbTrangThaiCho);
        dpNgayCho.setConverter(new StringConverter<>() {
            @Override public String toString(LocalDate ngay) { return ngay == null ? "" : DINH_DANG_NGAY.format(ngay); }
            @Override public LocalDate fromString(String chu) { return chu == null || chu.isBlank() ? null : LocalDate.parse(chu, DINH_DANG_NGAY); }
        });
        dpNgayCho.valueProperty().addListener((thuocTinh, cu, moi) -> { if (!dangNapBoLoc && moi != null) lamMoi(true); });
        cbPhongCho.valueProperty().addListener((thuocTinh, cu, moi) -> { if (!dangNapBoLoc) lamMoi(false); });
        cbTrangThaiCho.valueProperty().addListener((thuocTinh, cu, moi) -> { if (!dangNapBoLoc) lamMoi(false); });
        tblChoYTa.setPlaceholder(new Label("Không có phiếu khám trong phạm vi phân công của bạn."));
    }

    public void cauHinh(PhienDangNhap phien, KetNoiCSDL ketNoi) {
        this.phien = phien;
        dichVu = new DanhSachChoYTaService(phien, ketNoi, new PhieuKhamDAO());
        dangNapBoLoc = true;
        dpNgayCho.setValue(LocalDate.now(MUI_GIO));
        dangNapBoLoc = false;
        btnLamMoiChoYTa.setOnAction(suKien -> lamMoi(true));
        lamMoi(true);
    }

    private void lamMoi(boolean napLaiPhong) {
        LocalDate ngay = dpNgayCho.getValue();
        if (ngay == null) return;
        String phongDaChon = cbPhongCho.getValue();
        String trangThai = layMaTrangThai(cbTrangThaiCho.getValue());
        CongViecGiaoDien.chay(trangChoYTa, lblThongBaoChoYTa, phien, () -> {
            List<String> cacPhong = napLaiPhong ? dichVu.layPhongDuocPhanCong(ngay) : null;
            String phongLoc = cacPhong == null ? phongDaChon
                : (cacPhong.contains(phongDaChon) ? phongDaChon : null);
            List<PhieuKham> danhSach = dichVu.layDanhSach(ngay, phongLoc, trangThai);
            return new DuLieuTaiDanhSach(cacPhong, danhSach, phongLoc);
        }, ketQua -> {
            dangNapBoLoc = true;
            if (ketQua.cacPhong() != null) {
                cbPhongCho.getItems().setAll(ketQua.cacPhong());
                cbPhongCho.setValue(ketQua.phongLoc());
            }
            dangNapBoLoc = false;
            tblChoYTa.getItems().setAll(ketQua.danhSach());
            int cho = dem(ketQua.danhSach(), "CHO_KHAM");
            int dangKham = dem(ketQua.danhSach(), "DANG_KHAM");
            int daKham = dem(ketQua.danhSach(), "DA_KHAM");
            lblSoLuongCho.setText("Chờ khám: " + cho);
            lblSoLuongDangKham.setText("Đang khám: " + dangKham);
            lblSoLuongDaKham.setText("Đã khám: " + daKham);
            String phong = cbPhongCho.getValue() == null ? cbPhongCho.getItems().toString() : cbPhongCho.getValue();
            lblThongTinCa.setText(ngay.format(DINH_DANG_NGAY) + " · Phòng được phân công: "
                + (phong.isBlank() || "[]".equals(phong) ? "chưa có ca" : phong));
        });
    }

    private String layMaTrangThai(String luaChon) {
        return switch (luaChon == null ? "" : luaChon) {
            case "CHO_KHAM", "DANG_KHAM", "DA_KHAM" -> luaChon;
            default -> null;
        };
    }

    private int dem(List<PhieuKham> danhSach, String trangThai) {
        return (int) danhSach.stream().filter(phieu -> trangThai.equals(phieu.layTrangThaiKham())).count();
    }

    private record DuLieuTaiDanhSach(List<String> cacPhong, List<PhieuKham> danhSach, String phongLoc) { }
}
