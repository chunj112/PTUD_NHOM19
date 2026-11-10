package iuh.se.nhom19.view.receptionist;

import java.time.*;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import iuh.se.nhom19.auth.PhienDangNhap;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.entity.*;
import iuh.se.nhom19.service.*;
import iuh.se.nhom19.view.common.CongViecGiaoDien;

public class KhongHenController {
    @FXML private BorderPane tiepNhanKhongHenBanDau;
    @FXML private ThemBenhNhanController chonBenhNhanController;
    @FXML private TextArea txtKhongHenTrongLyDo,txtKhongHenGhiChu;
    @FXML private ComboBox<PhanCong> cboNhaSiKhongHen;
    @FXML private Button btnHuy,btnTiepNhanKhongHen;
    @FXML private Label lblKhongHenTrongQuyTac,lblThoiDiemKhongHen,lblLoiCaKhongHen,lblLoiLyDoKhongHen;
    private PhienDangNhap phien;
    private PhanCongService phanCongService;
    private TiepNhanService tiepNhanService;
    private Consumer<PhieuKham> khiTiepNhan;

    @FXML private void initialize(){CongViecGiaoDien.anKhiTrong(lblLoiCaKhongHen);CongViecGiaoDien.anKhiTrong(lblLoiLyDoKhongHen);}

    public void cauHinh(PhienDangNhap phien,KetNoiCSDL ketNoi,Consumer<PhieuKham> khiTiepNhan) {
        this.phien=phien; this.khiTiepNhan=khiTiepNhan;
        phanCongService=new PhanCongService(phien,ketNoi); tiepNhanService=new TiepNhanService(phien,ketNoi);
        chonBenhNhanController.cauHinh(phien,ketNoi,"","",null);
        btnHuy.setDisable(false);
        btnHuy.setOnAction(suKien->CongViecGiaoDien.dong(tiepNhanKhongHenBanDau));
        btnTiepNhanKhongHen.setOnAction(suKien->tiepNhan());
        cboNhaSiKhongHen.setOnAction(suKien->btnTiepNhanKhongHen.setDisable(cboNhaSiKhongHen.getValue()==null)); taiDanhSachCaDangLam();
    }
    private void taiDanhSachCaDangLam() {
        lblThoiDiemKhongHen.setText("Tiếp nhận lúc: "+LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        LocalDate ngay=LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        cboNhaSiKhongHen.getItems().clear(); btnTiepNhanKhongHen.setDisable(true);
        CongViecGiaoDien.chay(tiepNhanKhongHenBanDau,lblKhongHenTrongQuyTac,phien,()->phanCongService.layDanhSachNhaSi(ngay),danhSach->{
            LocalTime gio=LocalTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            for(PhanCong ca:danhSach)if(!gio.isBefore(ca.layGioBatDau())&&gio.isBefore(ca.layGioKetThuc()))cboNhaSiKhongHen.getItems().add(ca);
            lblKhongHenTrongQuyTac.setText(cboNhaSiKhongHen.getItems().isEmpty()?"Không có nha sĩ/phòng đang trong ca. Nếu khám ngày khác, sử dụng Đặt lịch.":"Thông tin mới chỉ được lưu khi tiếp nhận thành công. Chọn ca nha sĩ hiện tại.");
        });
    }
    private void tiepNhan() {
        try {
            PhanCong ca=cboNhaSiKhongHen.getValue();
            lblLoiCaKhongHen.setText("");lblLoiLyDoKhongHen.setText("");
            if(ca==null){lblLoiCaKhongHen.setText("Chọn ca nha sĩ đang làm việc.");return;}
            try{KiemTraNghiepVu.batBuoc(txtKhongHenTrongLyDo.getText(),"lý do khám",500);}catch(IllegalArgumentException loi){lblLoiLyDoKhongHen.setText(loi.getMessage());return;}
            String maBenhNhan=chonBenhNhanController.layMaBenhNhanDaChon(); BenhNhan moi=chonBenhNhanController.layThongTinMoi();
            String lyDo=txtKhongHenTrongLyDo.getText(),ghiChu=txtKhongHenGhiChu.getText();
            CongViecGiaoDien.chay(tiepNhanKhongHenBanDau,lblKhongHenTrongQuyTac,phien,
                ()->tiepNhanService.tiepNhanKhongHen(maBenhNhan,moi,ca.layMaNhanVien(),ca.layMaPhong(),lyDo,ghiChu),phieu->{
                    khiTiepNhan.accept(phieu); CongViecGiaoDien.dong(tiepNhanKhongHenBanDau);
                });
        }catch(IllegalArgumentException loi){lblKhongHenTrongQuyTac.setText(loi.getMessage());}
    }
}
