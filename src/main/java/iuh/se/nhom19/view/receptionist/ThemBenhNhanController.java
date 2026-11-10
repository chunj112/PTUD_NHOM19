package iuh.se.nhom19.view.receptionist;

import java.time.*;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import iuh.se.nhom19.auth.PhienDangNhap;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.entity.BenhNhan;
import iuh.se.nhom19.service.*;
import iuh.se.nhom19.view.common.CongViecGiaoDien;

/** Thành phần xác định bệnh nhân trong modal tiếp nhận; không tự lưu bệnh nhân khi nhập form. */
public class ThemBenhNhanController {
    @FXML private VBox themBenhNhanBieuMau, vungTimHoSo, vungBenhNhanMoi;
    @FXML private RadioButton radHoSoCu, radHoSoMoi;
    @FXML private TextField txtTimBenhNhan, txtBenhNhanTen, txtBenhNhanSoDienThoai, txtBenhNhanDiaChi, txtBenhNhanSinhNgay;
    @FXML private ComboBox<String> cboBenhNhanGioiTinh;
    @FXML private ListView<BenhNhan> lstChonBenhNhan;
    @FXML private Button btnTimHoSo;
    @FXML private Label lblHoSoDaChon, lblThongBaoHoSo, lblLoiTenBenhNhan, lblLoiSDTBenhNhan, lblLoiNgaySinh, lblLoiGioiTinh, lblLoiDiaChi;
    private PhienDangNhap phien;
    private BenhNhanService dichVu;
    private final PauseTransition doiNhap = new PauseTransition(Duration.millis(450));

    @FXML private void initialize() {
        for(Label nhan:java.util.List.of(lblLoiTenBenhNhan,lblLoiSDTBenhNhan,lblLoiNgaySinh,lblLoiGioiTinh,lblLoiDiaChi,lblThongBaoHoSo))CongViecGiaoDien.anKhiTrong(nhan);
        ToggleGroup nhom=new ToggleGroup(); radHoSoCu.setToggleGroup(nhom); radHoSoMoi.setToggleGroup(nhom); radHoSoCu.setSelected(true);
        lstChonBenhNhan.setPlaceholder(new Label("Nhập số điện thoại bệnh nhân để tìm hồ sơ."));
        lstChonBenhNhan.setCellFactory(bang -> new ListCell<>() {
            @Override protected void updateItem(BenhNhan benhNhan, boolean rong) {
                super.updateItem(benhNhan, rong);
                if (rong || benhNhan == null) { setText(null); setGraphic(null); return; }
                Label ten = new Label(benhNhan.layHoTen()); ten.getStyleClass().add("le-tan-chi-tiet-gia-tri");
                Label thongTin = new Label(benhNhan.laySoDienThoai() + " · " + benhNhan.layMaBenhNhan());
                thongTin.getStyleClass().add("le-tan-chu-thich");
                setText(null); setGraphic(new VBox(2, ten, thongTin));
            }
        });
        nhom.selectedToggleProperty().addListener((thuocTinh,cu,moi)->doiLuaChon());
        lstChonBenhNhan.getSelectionModel().selectedItemProperty().addListener((thuocTinh,cu,moi)->lblHoSoDaChon.setText(moi==null?"Chưa chọn hồ sơ.":"Đã xác minh: "+moi.layHoTen()+" · "+moi.laySoDienThoai()+" · "+moi.layMaBenhNhan()));
        txtTimBenhNhan.textProperty().addListener((thuocTinh,cu,moi)->{
            lstChonBenhNhan.getSelectionModel().clearSelection();
            doiNhap.stop();
            if (moi != null && moi.trim().matches("[0-9]{3,11}")) doiNhap.playFromStart();
        });
        doiNhap.setOnFinished(suKien -> timHoSo());
        doiLuaChon();
    }
    public void cauHinh(PhienDangNhap phien,KetNoiCSDL ketNoi,String hoTenGoiY,String soDienThoaiGoiY,String maDaLienKet) {
        this.phien=phien; dichVu=new BenhNhanService(phien,ketNoi);
        txtBenhNhanTen.setText(hoTenGoiY); txtBenhNhanSoDienThoai.setText(soDienThoaiGoiY);
        btnTimHoSo.setDisable(false); btnTimHoSo.setOnAction(suKien->timHoSo()); txtTimBenhNhan.setOnAction(suKien->timHoSo());
        if(maDaLienKet!=null) {
            radHoSoCu.setSelected(true); radHoSoMoi.setDisable(true); radHoSoCu.setDisable(true); txtTimBenhNhan.setDisable(true); btnTimHoSo.setDisable(true);
            CongViecGiaoDien.chay(themBenhNhanBieuMau,lblThongBaoHoSo,phien,()->dichVu.timTheoMa(maDaLienKet),benhNhan->{
                lstChonBenhNhan.getItems().setAll(benhNhan); lstChonBenhNhan.getSelectionModel().select(benhNhan);
                lblThongBaoHoSo.setText("Lịch đã liên kết hồ sơ này. Vui lòng xác minh trước khi tiếp nhận.");
            });
        }
    }
    private void doiLuaChon() {
        boolean cu=radHoSoCu.isSelected(); vungTimHoSo.setVisible(cu); vungTimHoSo.setManaged(cu);
        vungBenhNhanMoi.setVisible(!cu); vungBenhNhanMoi.setManaged(!cu); lblThongBaoHoSo.setText("");
        javafx.application.Platform.runLater(()->CongViecGiaoDien.canChinhHopThoai(themBenhNhanBieuMau));
    }
    private void timHoSo() {
        String tuKhoa=txtTimBenhNhan.getText(); lstChonBenhNhan.getItems().clear();
        if (tuKhoa == null || !tuKhoa.trim().matches("[0-9]{3,11}")) {
            lblThongBaoHoSo.setText("Nhập từ 3 đến 11 chữ số điện thoại để tìm."); return;
        }
        CongViecGiaoDien.chay(themBenhNhanBieuMau,lblThongBaoHoSo,phien,()->dichVu.timBenhNhan(tuKhoa),ketQua->{
            lstChonBenhNhan.getItems().setAll(ketQua); lstChonBenhNhan.setPrefHeight(Math.min(150, Math.max(52, ketQua.size() * 48)));
            lblThongBaoHoSo.setText(ketQua.isEmpty()?"Không tìm thấy bệnh nhân với số điện thoại này.":"Tìm thấy "+ketQua.size()+" hồ sơ. Chọn và xác minh đúng người; không tự liên kết khi trùng SĐT.");
        });
    }
    public String layMaBenhNhanDaChon() {
        if(Boolean.TRUE.equals(themBenhNhanBieuMau.getProperties().get("dangChay")))throw new IllegalArgumentException("Đang tải hồ sơ. Vui lòng chờ.");
        if(!radHoSoCu.isSelected())return null;
        BenhNhan daChon=lstChonBenhNhan.getSelectionModel().getSelectedItem();
        if(daChon==null)throw new IllegalArgumentException("Chọn một hồ sơ bệnh nhân hoặc chuyển sang Tạo hồ sơ mới.");
        return daChon.layMaBenhNhan();
    }
    public BenhNhan layThongTinMoi() {
        if(radHoSoCu.isSelected())return null;
        lblLoiTenBenhNhan.setText("");lblLoiSDTBenhNhan.setText("");lblLoiNgaySinh.setText("");lblLoiGioiTinh.setText("");lblLoiDiaChi.setText("");
        BenhNhan banNhap=new BenhNhan(); banNhap.datHoTen(txtBenhNhanTen.getText()); banNhap.datSoDienThoai(txtBenhNhanSoDienThoai.getText());
        banNhap.datNam(cboBenhNhanGioiTinh.getValue()==null?null:"Nam".equals(cboBenhNhanGioiTinh.getValue()));
        try { banNhap.datNgaySinh(KiemTraNghiepVu.docNgaySinh(txtBenhNhanSinhNgay.getText(), LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")))); }
        catch (IllegalArgumentException loi) { banNhap.datNgaySinh(null); lblLoiNgaySinh.setText(loi.getMessage()); }
        banNhap.datDiaChi(txtBenhNhanDiaChi.getText());
        // Báo lỗi cạnh từng trường; Service vẫn kiểm tra lại trước khi ghi SQL.
        boolean sai=false;
        try{KiemTraNghiepVu.batBuoc(banNhap.layHoTen(),"họ tên",120);}catch(IllegalArgumentException loi){lblLoiTenBenhNhan.setText(loi.getMessage());sai=true;}
        try{KiemTraNghiepVu.soDienThoai(banNhap.laySoDienThoai());}catch(IllegalArgumentException loi){lblLoiSDTBenhNhan.setText(loi.getMessage());sai=true;}
        LocalDate homNay=LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        if(banNhap.layNgaySinh()==null){if(lblLoiNgaySinh.getText().isBlank())lblLoiNgaySinh.setText("Ngày sinh phải đúng dd/MM/yyyy và không ở tương lai.");sai=true;}
        if(banNhap.layNam()==null){lblLoiGioiTinh.setText("Vui lòng chọn giới tính.");sai=true;}
        try{KiemTraNghiepVu.tuyChon(banNhap.layDiaChi(),"Địa chỉ",255);}catch(IllegalArgumentException loi){lblLoiDiaChi.setText(loi.getMessage());sai=true;}
        if(sai)throw new IllegalArgumentException("Kiểm tra các trường được báo lỗi trong thông tin bệnh nhân.");
        return KiemTraNghiepVu.thongTinBenhNhanMoi(banNhap,homNay);
    }
}
