package iuh.se.nhom19.view.receptionist;

import java.time.*;
import java.util.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import iuh.se.nhom19.view.common.TrangThaiGiaoDien;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import iuh.se.nhom19.auth.PhienDangNhap;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.entity.LichHen;
import iuh.se.nhom19.service.LichHenService;
import iuh.se.nhom19.view.common.CongViecGiaoDien;

public class LichHenNgayController {
    @FXML private VBox tiepNhanHomNay, daChonLichHenKhung;
    @FXML private DatePicker dpLichHenNgayLam;
    @FXML private TextField txtLichHenTimKiem;
    @FXML private ComboBox<String> cboLichHenNhaSi;
    @FXML private TableView<LichHen> tblHomNayDanhSachLichHen;
    @FXML private TableColumn<LichHen,String> tblHomNayDanhSachLichHenCotGio, tblHomNayDanhSachLichHenCotBenhNhan,
        tblHomNayDanhSachLichHenCotNhaSi, tblHomNayDanhSachLichHenCotPhong, tblHomNayDanhSachLichHenCotLyDo, tblHomNayDanhSachLichHenCotTinhTrang;
    @FXML private Label tblHomNayDanhSachLichHenSoLuong, tblHomNayDanhSachLichHenTrang, lblTrangThaiLichHenNgay;
    @FXML private TableColumn<LichHen,String> cotSoDienThoaiNguoiHen;
    @FXML private Button btnLamMoiLichHenNgay;
    @FXML private ChiTietLichHenController daChonLichHenChiTietController;
    @FXML private Label lblKetQuaTiepNhanNgay;
    private PhienDangNhap phien;
    private LichHenService dichVu;
    private List<LichHen> danhSach = new ArrayList<>();

    @FXML private void initialize() {
        CongViecGiaoDien.anKhiTrong(lblKetQuaTiepNhanNgay);
        CongViecGiaoDien.anKhiTrong(lblTrangThaiLichHenNgay);
        TrangThaiGiaoDien.cot(tblHomNayDanhSachLichHenCotTinhTrang);
        TrangThaiGiaoDien.cotChu(tblHomNayDanhSachLichHenCotBenhNhan);
        TrangThaiGiaoDien.cotChu(tblHomNayDanhSachLichHenCotLyDo);
        cotSoDienThoaiNguoiHen.setCellValueFactory(dong -> new ReadOnlyStringWrapper(dong.getValue().laySoDienThoaiLienHe()));
        tblHomNayDanhSachLichHenCotGio.setCellValueFactory(dong -> new ReadOnlyStringWrapper(dong.getValue().layGioBatDau() + "–" + dong.getValue().layGioKetThuc()));
        tblHomNayDanhSachLichHenCotBenhNhan.setCellValueFactory(dong -> new ReadOnlyStringWrapper(dong.getValue().layHoTenNguoiHen()));
        tblHomNayDanhSachLichHenCotNhaSi.setCellValueFactory(dong -> new ReadOnlyStringWrapper(dong.getValue().layTenNhaSi()));
        tblHomNayDanhSachLichHenCotPhong.setCellValueFactory(dong -> new ReadOnlyStringWrapper(
            dong.getValue().layMaPhong()==null?"—":dong.getValue().layMaPhong()));
        tblHomNayDanhSachLichHenCotLyDo.setCellValueFactory(dong -> new ReadOnlyStringWrapper(dong.getValue().layLyDoKham()));
        tblHomNayDanhSachLichHenCotTinhTrang.setCellValueFactory(dong -> new ReadOnlyStringWrapper(dong.getValue().layTrangThai()));
        tblHomNayDanhSachLichHen.setPlaceholder(new Label("Chọn ngày rồi nhấn Làm mới."));
        tblHomNayDanhSachLichHen.getSelectionModel().selectedItemProperty().addListener((thuocTinh, cu, moi) -> {
            daChonLichHenKhung.setVisible(moi != null); daChonLichHenKhung.setManaged(moi != null);
            if (moi != null) daChonLichHenChiTietController.hienThi(moi);
        });
    }
    public void cauHinh(PhienDangNhap phien, KetNoiCSDL ketNoi) {
        this.phien = phien; dichVu = new LichHenService(phien, ketNoi);
        daChonLichHenChiTietController.cauHinh(phien, ketNoi, thongBao -> { lamMoi(); lblKetQuaTiepNhanNgay.setText(thongBao); });
        daChonLichHenChiTietController.khiDong(()->tblHomNayDanhSachLichHen.getSelectionModel().clearSelection());
        dpLichHenNgayLam.setValue(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        dpLichHenNgayLam.setOnAction(suKien -> lamMoi());
        txtLichHenTimKiem.textProperty().addListener((thuocTinh, cu, moi) -> locDanhSach());
        cboLichHenNhaSi.setOnAction(suKien -> locDanhSach());
        btnLamMoiLichHenNgay.setDisable(false); btnLamMoiLichHenNgay.setOnAction(suKien -> lamMoi()); lamMoi();
    }
    public void hienThiNgay(LocalDate ngay) { dpLichHenNgayLam.setValue(ngay); lamMoi(); }
    public void lamMoi() {
        if (dichVu == null) return;
        var ngay = dpLichHenNgayLam.getValue();
        // Bỏ kết quả cũ ngay khi tải ngày khác; lỗi SQL không được trông như danh sách mới đã tải thành công.
        danhSach.clear(); tblHomNayDanhSachLichHen.getItems().clear(); tblHomNayDanhSachLichHenSoLuong.setText("Chưa tải dữ liệu");
        tblHomNayDanhSachLichHen.setPlaceholder(new Label("Chưa tải dữ liệu. Xem thông báo và thử Làm mới."));
        CongViecGiaoDien.chay(tiepNhanHomNay, lblTrangThaiLichHenNgay, phien, () -> dichVu.layLichHenTheoNgay(ngay), ketQua -> {
            if (!Objects.equals(ngay, dpLichHenNgayLam.getValue())) { lamMoi(); return; }
            danhSach = ketQua; cboLichHenNhaSi.getItems().setAll("Tất cả nha sĩ");
            for (LichHen lichHen : danhSach) {
                String nhaSi = lichHen.layMaNhaSi() + " · " + lichHen.layTenNhaSi();
                if (!cboLichHenNhaSi.getItems().contains(nhaSi)) cboLichHenNhaSi.getItems().add(nhaSi);
            }
            cboLichHenNhaSi.setValue("Tất cả nha sĩ"); locDanhSach();
            // Không hiện thông báo thành công sau khi tải; chỉ giữ thông báo lỗi trong Label này.
            lblTrangThaiLichHenNgay.setText("");
        });
    }
    private void locDanhSach() {
        String tuKhoa = txtLichHenTimKiem.getText().trim().toLowerCase(Locale.ROOT), nhaSi = cboLichHenNhaSi.getValue();
        List<LichHen> ketQua = new ArrayList<>();
        for (LichHen lichHen : danhSach) {
            String soDienThoai = lichHen.laySoDienThoaiLienHe()==null?"":lichHen.laySoDienThoaiLienHe().toLowerCase(Locale.ROOT);
            if (soDienThoai.contains(tuKhoa) && (nhaSi == null || nhaSi.equals("Tất cả nha sĩ") || nhaSi.startsWith(lichHen.layMaNhaSi() + " · "))) ketQua.add(lichHen);
        }
        tblHomNayDanhSachLichHen.getItems().setAll(ketQua); tblHomNayDanhSachLichHen.setPlaceholder(new Label("Không có lịch hẹn phù hợp."));
        tblHomNayDanhSachLichHenSoLuong.setText(ketQua.size() + " lịch hẹn · Tìm theo SĐT"); tblHomNayDanhSachLichHenTrang.setText("1");
    }
}
