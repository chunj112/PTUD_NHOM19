package iuh.se.nhom19.view.receptionist;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.application.Platform;
import iuh.se.nhom19.auth.PhienDangNhap;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.view.common.CongViecGiaoDien;

public class TiepNhanController {
    @FXML private VBox leTanTiepNhan;
    @FXML private Button btnDatLichHen,btnMoTiepNhanKhongHen;
    @FXML private Label lblKetQuaKhongHen;
    @FXML private TabPane tiepNhanCacTab;
    @FXML private Tab tabHomNay, tabTuan;
    // Với fx:include id="abc", FXMLLoader cung cấp Controller con dưới tên abcController.
    @FXML private LichHenNgayController tabHomNayNoiDungController;
    @FXML private LichHenTuanController tabTuanNoiDungController;
    private PhienDangNhap phien;
    private KetNoiCSDL ketNoi;
    private boolean dangChoCanChinh;

    @FXML private void initialize(){
        CongViecGiaoDien.anKhiTrong(lblKetQuaKhongHen);
        tiepNhanCacTab.widthProperty().addListener((thuocTinh,cu,moi)->henCanChinh());
        tiepNhanCacTab.getSelectionModel().selectedItemProperty().addListener((thuocTinh,cu,moi)->henCanChinh());
        for(Tab tab:tiepNhanCacTab.getTabs()) {
            Region noiDung=(Region)tab.getContent();
            noiDung.needsLayoutProperty().addListener((thuocTinh,cu,moi)->{
                if(moi&&tab==tiepNhanCacTab.getSelectionModel().getSelectedItem())henCanChinh();
            });
        }
    }
    private void henCanChinh(){
        if(dangChoCanChinh)return;
        dangChoCanChinh=true;
        Platform.runLater(()->{dangChoCanChinh=false;canChinhChieuCao();});
    }
    private void canChinhChieuCao(){
        if(tiepNhanCacTab.getWidth()<=0||tiepNhanCacTab.getSelectionModel().getSelectedItem()==null)return;
        Region noiDung=(Region)tiepNhanCacTab.getSelectionModel().getSelectedItem().getContent();
        // Đo cả panel/ghi chú theo chiều rộng thực. ScrollPane của AppShell sẽ cuộn toàn trang.
        // Chiều cao cố định làm các nút cuối panel bị clip khi tên/ghi chú dài.
        double chieuCao=Math.ceil(noiDung.prefHeight(tiepNhanCacTab.getWidth()))+68;
        if(Math.abs(tiepNhanCacTab.getPrefHeight()-chieuCao)>1)tiepNhanCacTab.setPrefHeight(chieuCao);
    }

    public void cauHinh(PhienDangNhap phien, KetNoiCSDL ketNoi) {
        this.phien = phien; this.ketNoi = ketNoi;
        tabHomNayNoiDungController.cauHinh(phien, ketNoi);
        tabTuanNoiDungController.cauHinh(phien, ketNoi);
        btnMoTiepNhanKhongHen.setDisable(false);
        btnMoTiepNhanKhongHen.setOnAction(suKien->CongViecGiaoDien.moHopThoai(leTanTiepNhan,"/fxml/receptionist/tiep-nhan-khong-hen.fxml","Tiếp nhận khách không hẹn",boNap->{
            KhongHenController dieuKhien=boNap.getController();
            dieuKhien.cauHinh(phien,ketNoi,phieu->lblKetQuaKhongHen.setText("Tiếp nhận thành công · STT "
                +String.format("%03d",phieu.laySoThuTuKham())+" · Phiếu "+phieu.layMaPhieuKham()+" · Chờ khám"));
        }));
        btnDatLichHen.setDisable(false); btnDatLichHen.setOnAction(suKien -> moDatLich());
        tabTuan.getContent().getProperties().put("tamDung",true);
        tiepNhanCacTab.getSelectionModel().selectedItemProperty().addListener((thuocTinh, cu, moi) -> {
            if(cu!=null)cu.getContent().getProperties().put("tamDung",true);
            if(moi!=null)moi.getContent().getProperties().put("tamDung",false);
            if (moi == tabHomNay) tabHomNayNoiDungController.lamMoi();
            if (moi == tabTuan) tabTuanNoiDungController.lamMoi();
        });
    }
    private void moDatLich() {
        CongViecGiaoDien.moHopThoai(leTanTiepNhan, "/fxml/receptionist/them-lich-hen.fxml", "Đặt lịch hẹn khám", boNap -> {
            DatLichHenController dieuKhien = boNap.getController();
            dieuKhien.cauHinh(phien, ketNoi, lichHen -> {
                tabHomNayNoiDungController.hienThiNgay(lichHen.layNgayHen());
                tiepNhanCacTab.getSelectionModel().select(tabHomNay);
            });
        });
    }
}
