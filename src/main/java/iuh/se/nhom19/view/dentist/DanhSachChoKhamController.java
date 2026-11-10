package iuh.se.nhom19.view.dentist;

import java.time.format.DateTimeFormatter;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import iuh.se.nhom19.view.common.TrangThaiGiaoDien;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextArea;
import iuh.se.nhom19.auth.PhienDangNhap;
import iuh.se.nhom19.dao.PhieuKhamDAO;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.entity.PhieuKham;
import iuh.se.nhom19.service.DanhSachChoKhamService;
import iuh.se.nhom19.view.common.CongViecGiaoDien;

public class DanhSachChoKhamController {
    @FXML private VBox danhSachChoKham, chiTietPhieuKham;
    @FXML private TableView<PhieuKham> tblChoKham;
    @FXML private TableColumn<PhieuKham,String> cotSTT, cotMaPhieu, cotBenhNhan, cotTiepNhan, cotPhong, cotLyDo;
    @FXML private Label lblThongBao, lblSoLuongChoKham, lblMaPhieu, lblBenhNhan, lblThoiDiem, lblPhong, lblLyDo, lblNguonTiepNhan, lblTrangThai;
    @FXML private Button btnLamMoiChoKham, btnBatDauKham, btnHoanTatKham;
    @FXML private TextArea txtTinhTrangLamSan, txtChanDoan;
    private PhienDangNhap phien;
    private DanhSachChoKhamService dichVu;
    private final DateTimeFormatter dinhDang = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @FXML private void initialize() {
        cotSTT.setCellValueFactory(dong -> new ReadOnlyStringWrapper(dong.getValue().laySoThuTuKham() == null
            ? "—" : String.format("%03d", dong.getValue().laySoThuTuKham())));
        cotMaPhieu.setCellValueFactory(dong -> new ReadOnlyStringWrapper(dong.getValue().layMaPhieuKham()));
        cotBenhNhan.setCellValueFactory(dong -> new ReadOnlyStringWrapper(dong.getValue().layBenhNhan().layHoTen()));
        cotTiepNhan.setCellValueFactory(dong -> new ReadOnlyStringWrapper(dong.getValue().layThoiDiemTiepNhan().format(dinhDang)));
        cotPhong.setCellValueFactory(dong -> new ReadOnlyStringWrapper(dong.getValue().layMaPhong()));
        cotLyDo.setCellValueFactory(dong -> new ReadOnlyStringWrapper(dong.getValue().layLyDo()));
        tblChoKham.setPlaceholder(new Label("Danh sách chưa được tải."));
        tblChoKham.getSelectionModel().selectedItemProperty().addListener((thuocTinh, cu, moi) -> {
            chiTietPhieuKham.setVisible(false); chiTietPhieuKham.setManaged(false);
            btnBatDauKham.setDisable(true); btnHoanTatKham.setDisable(true);
            if (moi != null) taiChiTiet(moi.layMaPhieuKham());
        });
    }
    public void cauHinh(PhienDangNhap phien, KetNoiCSDL ketNoi) {
        cauHinh(phien, new DanhSachChoKhamService(phien, ketNoi, new PhieuKhamDAO()));
    }
    public void cauHinh(PhienDangNhap phien, DanhSachChoKhamService dichVu) {
        this.phien = phien; this.dichVu = dichVu;
        btnLamMoiChoKham.setDisable(false); btnLamMoiChoKham.setOnAction(suKien -> lamMoi()); lamMoi();
        btnBatDauKham.setOnAction(suKien -> batDauKham());
        btnHoanTatKham.setOnAction(suKien -> hoanTatKham());
    }
    private void lamMoi() {
        tblChoKham.getItems().clear(); lblSoLuongChoKham.setText("Chưa tải dữ liệu");
        tblChoKham.setPlaceholder(new Label("Chưa tải dữ liệu. Kiểm tra thông báo rồi nhấn Làm mới."));
        CongViecGiaoDien.chay(danhSachChoKham, lblThongBao, phien, () -> dichVu.layDanhSachChoKham(), ketQua -> {
            // Hàng chờ lấy lại từ SQL sau mỗi lần mở trang/làm mới; không truyền danh sách từ tài khoản lễ tân.
            tblChoKham.getItems().setAll(ketQua); tblChoKham.setPlaceholder(new Label("Bạn chưa có phiếu chờ hoặc đang khám."));
            lblSoLuongChoKham.setText(ketQua.size() + " phiếu chờ/đang khám");
            lblThongBao.setText("Các phiếu thuộc nha sĩ đang đăng nhập, ưu tiên Chờ khám rồi theo STT.");
        });
    }
    private void taiChiTiet(String maPhieu) {
        CongViecGiaoDien.chay(danhSachChoKham, lblThongBao, phien, () -> dichVu.timPhieuKhamTheoMa(maPhieu), phieu -> {
            lblMaPhieu.setText(phieu.layMaPhieuKham()); lblBenhNhan.setText(phieu.layBenhNhan().toString());
            lblThoiDiem.setText(phieu.layThoiDiemTiepNhan().format(dinhDang)); lblPhong.setText(phieu.layMaPhong());
            lblLyDo.setText(phieu.layLyDo()); TrangThaiGiaoDien.gan(lblTrangThai,phieu.layTrangThaiKham());
            lblNguonTiepNhan.setText(phieu.layMaLichHen() == null ? "Khách không hẹn" : "Lịch hẹn: " + phieu.layMaLichHen());
            txtTinhTrangLamSan.setText(phieu.layTinhTrangLamSan()==null?"":phieu.layTinhTrangLamSan());
            txtChanDoan.setText(phieu.layChanDoan()==null?"":phieu.layChanDoan());
            boolean choKham="CHO_KHAM".equals(phieu.layTrangThaiKham());
            boolean dangKham="DANG_KHAM".equals(phieu.layTrangThaiKham());
            txtTinhTrangLamSan.setDisable(!dangKham); txtChanDoan.setDisable(!dangKham);
            btnBatDauKham.setVisible(choKham); btnBatDauKham.setManaged(choKham); btnBatDauKham.setDisable(!choKham);
            btnHoanTatKham.setVisible(dangKham); btnHoanTatKham.setManaged(dangKham); btnHoanTatKham.setDisable(!dangKham);
            chiTietPhieuKham.setVisible(true); chiTietPhieuKham.setManaged(true);
        });
    }

    private void batDauKham() {
        PhieuKham daChon=tblChoKham.getSelectionModel().getSelectedItem(); if(daChon==null)return;
        String ma=daChon.layMaPhieuKham();
        CongViecGiaoDien.chay(danhSachChoKham,lblThongBao,phien,()->dichVu.batDauKham(ma),phieu->{
            lblTrangThai.setText(TrangThaiGiaoDien.nhan(phieu.layTrangThaiKham()));
            TrangThaiGiaoDien.gan(lblTrangThai,phieu.layTrangThaiKham());
            daChon.datTrangThaiKham(phieu.layTrangThaiKham()); tblChoKham.refresh();
            txtTinhTrangLamSan.setDisable(false); txtChanDoan.setDisable(false);
            btnBatDauKham.setVisible(false);btnBatDauKham.setManaged(false);
            btnHoanTatKham.setVisible(true);btnHoanTatKham.setManaged(true);btnHoanTatKham.setDisable(false);
            lblThongBao.setText("Phiếu " + ma + " đã chuyển sang trạng thái Đang khám.");
        });
    }

    private void hoanTatKham() {
        PhieuKham daChon=tblChoKham.getSelectionModel().getSelectedItem(); if(daChon==null)return;
        String ma=daChon.layMaPhieuKham(), tinhTrang=txtTinhTrangLamSan.getText(), chanDoan=txtChanDoan.getText();
        CongViecGiaoDien.chay(danhSachChoKham,lblThongBao,phien,()->{
            dichVu.hoanTatKham(ma,tinhTrang,chanDoan); return Boolean.TRUE;
        },ketQua->{
            tblChoKham.getItems().remove(daChon); chiTietPhieuKham.setVisible(false);chiTietPhieuKham.setManaged(false);
            lblSoLuongChoKham.setText(tblChoKham.getItems().size()+" phiếu chờ/đang khám");
            lblThongBao.setText("Phiếu " + ma + " đã hoàn tất khám. Y tá sẽ thấy trạng thái mới sau khi làm mới danh sách.");
        });
    }
}
