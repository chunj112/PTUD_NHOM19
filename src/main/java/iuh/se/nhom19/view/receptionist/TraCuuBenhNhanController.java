package iuh.se.nhom19.view.receptionist;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import iuh.se.nhom19.auth.PhienDangNhap;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.entity.BenhNhan;
import iuh.se.nhom19.service.BenhNhanService;
import iuh.se.nhom19.service.KiemTraNghiepVu;
import iuh.se.nhom19.view.common.CongViecGiaoDien;

/** Tra cứu chỉ đọc; không tạo hồ sơ từ thao tác tìm kiếm. */
public class TraCuuBenhNhanController {
    @FXML private VBox leTanDanhSachBenhNhan;
    @FXML private TextField txtBenhNhanTimKiem;
    @FXML private Button btnTimKiemBenhNhan;
    @FXML private TableView<BenhNhan> tblTraCuuBenhNhan;
    @FXML private TableColumn<BenhNhan,String> cotTraCuuMa,cotTraCuuTen,cotTraCuuSDT,cotTraCuuNgaySinh;
    @FXML private Label lblBenhNhanTraCuuBaoMat,lblChiTietTraCuu;
    private PhienDangNhap phien;
    private BenhNhanService dichVu;
    @FXML private void initialize() {
        cotTraCuuMa.setCellValueFactory(dong->new ReadOnlyStringWrapper(dong.getValue().layMaBenhNhan()));
        cotTraCuuTen.setCellValueFactory(dong->new ReadOnlyStringWrapper(dong.getValue().layHoTen()));
        cotTraCuuSDT.setCellValueFactory(dong->new ReadOnlyStringWrapper(dong.getValue().laySoDienThoai()));
        cotTraCuuNgaySinh.setCellValueFactory(dong->new ReadOnlyStringWrapper(KiemTraNghiepVu.hienNgaySinh(dong.getValue().layNgaySinh())));
        tblTraCuuBenhNhan.setPlaceholder(new Label("Nhập số điện thoại bệnh nhân để tìm hồ sơ."));
        tblTraCuuBenhNhan.getSelectionModel().selectedItemProperty().addListener((thuocTinh,cu,moi)->lblChiTietTraCuu.setText(moi==null?"":moi+" · "+(moi.layNam()?"Nam":"Nữ")+" · "+(moi.layDiaChi()==null?"Chưa có địa chỉ":moi.layDiaChi())));
    }
    public void cauHinh(PhienDangNhap phien,KetNoiCSDL ketNoi) {
        this.phien=phien; dichVu=new BenhNhanService(phien,ketNoi);
        btnTimKiemBenhNhan.setDisable(false); btnTimKiemBenhNhan.setOnAction(suKien->timKiem()); txtBenhNhanTimKiem.setOnAction(suKien->timKiem());
    }
    private void timKiem() {
        String tuKhoa=txtBenhNhanTimKiem.getText(); tblTraCuuBenhNhan.getItems().clear(); lblChiTietTraCuu.setText("");
        if(tuKhoa==null||!tuKhoa.trim().matches("[0-9]{3,11}")){lblBenhNhanTraCuuBaoMat.setText("Nhập từ 3 đến 11 chữ số điện thoại để tìm.");return;}
        CongViecGiaoDien.chay(leTanDanhSachBenhNhan,lblBenhNhanTraCuuBaoMat,phien,()->dichVu.timBenhNhan(tuKhoa),ketQua->{
            tblTraCuuBenhNhan.getItems().setAll(ketQua); tblTraCuuBenhNhan.setPlaceholder(new Label("Không tìm thấy hồ sơ phù hợp."));
            lblBenhNhanTraCuuBaoMat.setText(ketQua.isEmpty()?"Không tìm thấy bệnh nhân với số điện thoại này.":"Tìm thấy "+ketQua.size()+" hồ sơ. Chọn đúng người; không gộp hồ sơ chỉ vì trùng số điện thoại.");
        });
    }
}
