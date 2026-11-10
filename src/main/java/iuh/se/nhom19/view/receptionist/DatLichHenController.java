package iuh.se.nhom19.view.receptionist;

import java.time.*;
import java.util.*;
import java.util.function.Consumer;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import iuh.se.nhom19.auth.PhienDangNhap;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.entity.*;
import iuh.se.nhom19.service.*;
import iuh.se.nhom19.view.common.CongViecGiaoDien;

/** Một form cho mọi khách hàng; chọn hồ sơ là hành động xác minh, không phải chọn loại người mới/cũ. */
public class DatLichHenController {
    private static final ZoneId MUI_GIO = ZoneId.of("Asia/Ho_Chi_Minh");
    @FXML private BorderPane themLichHenBieuMau;
    @FXML private TextField txtHoTenNguoiHen,txtSoDienThoaiLienHe,txtLichHenLyDo;
    @FXML private ListView<BenhNhan> lstLichHenBenhNhanGoiY;
    @FXML private ComboBox<NhanVien> cboLichHenNhaSi;
    @FXML private ComboBox<PhanCong> cboCaLam;
    @FXML private DatePicker dpLichHenNgay;
    @FXML private TextField txtGioHen;
    @FXML private Label lblKhungGioDuKien;
    @FXML private Button btnHuy,btnLuuLichHen,btnBoLienKet;
    @FXML private Label lblLichHenBieuMauQuyTac,lblNguoiHenDaChon,lblHopThoaiTieuDe;
    @FXML private Label lblLoiHoTen,lblLoiSDT,lblLoiNgay,lblLoiNhaSi,lblLoiCaLam,lblLoiGio;
    private PhienDangNhap phien;
    private BenhNhanService benhNhanService;
    private LichHenService lichHenService;
    private PhanCongService phanCongService;
    private Consumer<LichHen> khiLuu;
    private LichHen lichDangSua;
    private BenhNhan hoSoDaChon;
    private List<PhanCong> danhSachCa=new ArrayList<>();
    private boolean dangGan, khoiPhucLichSua;
    private long lanTimBenhNhan;
    private final PauseTransition doiNhap=new PauseTransition(Duration.millis(600));
    @FXML private void initialize() {
        for(Label nhan:List.of(lblLoiHoTen,lblLoiSDT,lblLoiNgay,lblLoiNhaSi,lblLoiCaLam,lblLoiGio)) {
            // Giữ chỗ lỗi cố định để form không nhảy chiều cao khi thông báo hiện/ẩn.
            nhan.visibleProperty().bind(nhan.textProperty().isNotEmpty());
        }
        lblLichHenBieuMauQuyTac.visibleProperty().bind(lblLichHenBieuMauQuyTac.textProperty().isNotEmpty());
        lstLichHenBenhNhanGoiY.setPlaceholder(new Label("Nhập số điện thoại để tìm hồ sơ có sẵn."));
        lstLichHenBenhNhanGoiY.setCellFactory(bang -> new ListCell<>() {
            @Override protected void updateItem(BenhNhan benhNhan, boolean rong) {
                super.updateItem(benhNhan, rong);
                if (rong || benhNhan == null) { setText(null); setGraphic(null); return; }
                Label ten = new Label(benhNhan.layHoTen()); ten.getStyleClass().add("le-tan-chi-tiet-gia-tri");
                Label thongTin = new Label(benhNhan.laySoDienThoai() + " · " + benhNhan.layMaBenhNhan()); thongTin.getStyleClass().add("le-tan-chu-thich");
                setText(null); setGraphic(new VBox(2, ten, thongTin));
            }
        });
        lstLichHenBenhNhanGoiY.getSelectionModel().selectedItemProperty().addListener((thuocTinh,cu,moi)->{if(moi!=null)chonHoSo(moi);});
        txtHoTenNguoiHen.textProperty().addListener((thuocTinh,cu,moi)->lblLoiHoTen.setText(""));
        txtSoDienThoaiLienHe.textProperty().addListener((thuocTinh,cu,moi)->thongTinThayDoi());
        txtGioHen.textProperty().addListener((thuocTinh,cu,moi)->{lblLoiGio.setText("");lblLichHenBieuMauQuyTac.setText("");capNhatKhungGioDuKien();});
        doiNhap.setOnFinished(suKien->{if(phien!=null&&!Boolean.TRUE.equals(themLichHenBieuMau.getProperties().get("dangChay")))timBenhNhan();});
        cboCaLam.setConverter(new StringConverter<>(){
            @Override public String toString(PhanCong ca){return ca==null?"":ca.layTenCa()+" · "+ca.layMaPhong();}
            @Override public PhanCong fromString(String chu){return null;}
        });
        dpLichHenNgay.setDayCellFactory(bang->new DateCell(){@Override public void updateItem(LocalDate ngay,boolean rong){super.updateItem(ngay,rong);setDisable(rong||ngay==null||ngay.isBefore(LocalDate.now(MUI_GIO)));}});
    }
    private void thongTinThayDoi() {
        if(dangGan)return;
        if(hoSoDaChon!=null&&!hoSoDaChon.laySoDienThoai().equals(txtSoDienThoaiLienHe.getText().trim()))boLienKet();
        lanTimBenhNhan++;
        doiNhap.stop();
        lstLichHenBenhNhanGoiY.getItems().clear();
        lblLoiSDT.setText("");
        if(txtSoDienThoaiLienHe.getText().trim().matches("[0-9]{3,10}")) {
            lblLichHenBieuMauQuyTac.setText("Đang chờ tra cứu hồ sơ theo số điện thoại…");
            doiNhap.playFromStart();
        } else {
            lblLichHenBieuMauQuyTac.setText("Nhập từ 3 đến 10 chữ số để tìm hồ sơ có sẵn.");
        }
    }
    private void chonHoSo(BenhNhan benhNhan) {
        dangGan=true;txtHoTenNguoiHen.setText(benhNhan.layHoTen());txtSoDienThoaiLienHe.setText(benhNhan.laySoDienThoai());dangGan=false;doiNhap.stop();lanTimBenhNhan++;hoSoDaChon=benhNhan;
        lblNguoiHenDaChon.setText("Đã xác minh hồ sơ "+benhNhan.layMaBenhNhan()+" · "+benhNhan.layHoTen());
    }
    private void boLienKet(){hoSoDaChon=null;doiNhap.stop();lanTimBenhNhan++;lstLichHenBenhNhanGoiY.getSelectionModel().clearSelection();lblNguoiHenDaChon.setText("Chưa liên kết hồ sơ. Lịch sẽ chỉ lưu tên và SĐT.");}
    private void cauHinhChung(PhienDangNhap phien,KetNoiCSDL ketNoi,Consumer<LichHen> khiLuu){
        this.phien=phien;this.khiLuu=khiLuu;benhNhanService=new BenhNhanService(phien,ketNoi);lichHenService=new LichHenService(phien,ketNoi);phanCongService=new PhanCongService(phien,ketNoi);
        btnHuy.setDisable(false);btnBoLienKet.setDisable(false);btnLuuLichHen.setDisable(false);
        btnHuy.setOnAction(suKien->CongViecGiaoDien.dong(themLichHenBieuMau));btnBoLienKet.setOnAction(suKien->boLienKet());btnLuuLichHen.setOnAction(suKien->luu());
        dpLichHenNgay.setOnAction(suKien->{if(!dangGan){lblLoiNgay.setText("");lblLichHenBieuMauQuyTac.setText("");khoiPhucLichSua=false;taiNhaSi();}});
        cboLichHenNhaSi.setOnAction(suKien->{if(!dangGan){lblLoiNhaSi.setText("");lblLoiCaLam.setText("");lblLichHenBieuMauQuyTac.setText("");khoiPhucLichSua=false;chonNhaSi();}});
        cboCaLam.setOnAction(suKien->{if(!dangGan){lblLoiCaLam.setText("");lblLichHenBieuMauQuyTac.setText("");khoiPhucLichSua=false;capNhatKhungGioDuKien();}});
    }
    public void cauHinh(PhienDangNhap phien,KetNoiCSDL ketNoi,Consumer<LichHen> khiLuu){cauHinhChung(phien,ketNoi,khiLuu);dangGan=true;dpLichHenNgay.setValue(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")));dangGan=false;taiNhaSi();}
    public void cauHinhSua(PhienDangNhap phien,KetNoiCSDL ketNoi,String ma,Consumer<LichHen> khiLuu){
        cauHinhChung(phien,ketNoi,khiLuu);lblHopThoaiTieuDe.setText("Sửa lịch hẹn");
        CongViecGiaoDien.chay(themLichHenBieuMau,lblLichHenBieuMauQuyTac,phien,()->lichHenService.timLichHenTheoMa(ma),lich->{
            if(!"CHO_XAC_NHAN".equals(lich.layTrangThai())&&!"DA_XAC_NHAN".equals(lich.layTrangThai())){lblLichHenBieuMauQuyTac.setText("Lịch đã hủy/đã đến; không thể sửa.");return;}
            lichDangSua=lich;khoiPhucLichSua=true;dangGan=true;dpLichHenNgay.setValue(lich.layNgayHen());txtHoTenNguoiHen.setText(lich.layHoTenNguoiHen());txtSoDienThoaiLienHe.setText(lich.laySoDienThoaiLienHe());txtLichHenLyDo.setText(lich.layLyDoKham());txtGioHen.setText(lich.layGioBatDau().toString());dangGan=false;
            if(lich.layBenhNhan()!=null)chonHoSo(lich.layBenhNhan());taiNhaSi();
        });
    }
    private void timBenhNhan(){String tim=txtSoDienThoaiLienHe.getText().trim();
        if(!tim.matches("[0-9]{3,10}")){lstLichHenBenhNhanGoiY.getItems().clear();lblLichHenBieuMauQuyTac.setText("Nhập từ 3 đến 10 chữ số điện thoại để tìm hồ sơ.");return;}
        long lan=++lanTimBenhNhan;
        CongViecGiaoDien.chayKhongKhoa(themLichHenBieuMau,lblLichHenBieuMauQuyTac,phien,
            ()->benhNhanService.timBenhNhan(tim),
            ()->lan==lanTimBenhNhan&&tim.equals(txtSoDienThoaiLienHe.getText().trim()),
            ds->{lstLichHenBenhNhanGoiY.getItems().setAll(ds);lstLichHenBenhNhanGoiY.setPrefHeight(Math.min(120,Math.max(48,ds.size()*48)));lblLichHenBieuMauQuyTac.setText(ds.isEmpty()?"Không tìm thấy bệnh nhân với số điện thoại này.":"Có "+ds.size()+" hồ sơ phù hợp. Chọn và xác minh đúng người trước khi liên kết.");});}
    private void capNhatKhungGioDuKien(){
        lblLoiGio.setText("");
        lblKhungGioDuKien.getStyleClass().remove("khung-gio-du-kien-loi");
        String giaTriGio=txtGioHen.getText();
        if(giaTriGio==null||giaTriGio.isBlank()){
            lblKhungGioDuKien.setText("Nhập giờ bắt đầu theo HH:mm");
            return;
        }
        try {
            LocalTime batDau=KiemTraNghiepVu.docGioHen(giaTriGio);
            LocalTime ketThuc=batDau.plusMinutes(45);
            PhanCong ca=cboCaLam.getValue();
            if(ca==null){lblKhungGioDuKien.setText("Chọn ca để xem khoảng giờ");return;}
            if(batDau.isBefore(ca.layGioBatDau())||ketThuc.isAfter(ca.layGioKetThuc())||!batDau.isBefore(ketThuc)){
                lblKhungGioDuKien.setText("45 phút phải nằm trong ca đã chọn");return;
            }
            LocalDate ngay=dpLichHenNgay.getValue();
            if(ngay!=null && LocalDateTime.of(ngay,batDau).isBefore(LocalDateTime.now(MUI_GIO))){
                lblLoiGio.setText("Giờ hẹn đã qua. Chọn giờ hiện tại hoặc giờ sắp tới.");
                lblKhungGioDuKien.getStyleClass().add("khung-gio-du-kien-loi");
                lblKhungGioDuKien.setText("Khung giờ đã qua");return;
            }
            lblKhungGioDuKien.setText(batDau+" – "+ketThuc+" · 45 phút");
        } catch(IllegalArgumentException loi) { lblKhungGioDuKien.setText("Nhập giờ bắt đầu theo HH:mm"); }
    }
    private void taiNhaSi(){LocalDate ngay=dpLichHenNgay.getValue();dangGan=true;cboLichHenNhaSi.getItems().clear();cboCaLam.getItems().clear();dangGan=false;capNhatKhungGioDuKien();
        CongViecGiaoDien.chay(themLichHenBieuMau,lblLichHenBieuMauQuyTac,phien,()->phanCongService.layDanhSachNhaSi(ngay),ds->{
            danhSachCa=ds;dangGan=true;Set<String> maDaCo=new HashSet<>();
            for(PhanCong ca:ds)if(maDaCo.add(ca.layMaNhanVien()))cboLichHenNhaSi.getItems().add(new NhanVien(ca.layMaNhanVien(),ca.layTenNhaSi(),"NHA_SI",true));
            if(khoiPhucLichSua&&lichDangSua!=null)for(NhanVien ns:cboLichHenNhaSi.getItems())if(ns.layMaNhanVien().equals(lichDangSua.layMaNhaSi()))cboLichHenNhaSi.setValue(ns);
            dangGan=false;if(cboLichHenNhaSi.getValue()!=null)chonNhaSi();else lblLichHenBieuMauQuyTac.setText(ds.isEmpty()?"Không có nha sĩ được phân công P01/P02 trong ngày.":"Chọn nha sĩ và ca làm việc.");
        });}
    private void chonNhaSi(){dangGan=true;cboCaLam.getItems().clear();dangGan=false;capNhatKhungGioDuKien();NhanVien ns=cboLichHenNhaSi.getValue();if(ns==null)return;
        dangGan=true;for(PhanCong ca:danhSachCa)if(ca.layMaNhanVien().equals(ns.layMaNhanVien())){cboCaLam.getItems().add(ca);if(khoiPhucLichSua&&lichDangSua!=null&&!ca.layGioBatDau().isAfter(lichDangSua.layGioBatDau())&&!ca.layGioKetThuc().isBefore(lichDangSua.layGioKetThuc()))cboCaLam.setValue(ca);}dangGan=false;
        if(cboCaLam.getValue()!=null){if(khoiPhucLichSua)khoiPhucLichSua=false;capNhatKhungGioDuKien();}}
    private void luu(){
        lblLoiHoTen.setText("");lblLoiSDT.setText("");lblLoiNgay.setText("");lblLoiNhaSi.setText("");lblLoiCaLam.setText("");lblLoiGio.setText("");
        String ten=txtHoTenNguoiHen.getText(),sdt=txtSoDienThoaiLienHe.getText();boolean sai=false;
        try{KiemTraNghiepVu.batBuoc(ten,"họ tên",120);}catch(IllegalArgumentException loi){lblLoiHoTen.setText(loi.getMessage());sai=true;}
        try{KiemTraNghiepVu.soDienThoai(sdt);}catch(IllegalArgumentException loi){lblLoiSDT.setText(loi.getMessage());sai=true;}
        LocalDate ngay=dpLichHenNgay.getValue();
        if(ngay==null){lblLoiNgay.setText("Chọn ngày hẹn.");sai=true;}
        NhanVien nhaSi=cboLichHenNhaSi.getValue();
        if(nhaSi==null){lblLoiNhaSi.setText("Chọn nha sĩ.");sai=true;}
        PhanCong ca=cboCaLam.getValue();
        if(ca==null){lblLoiCaLam.setText("Chọn ca làm việc.");sai=true;}
        LocalTime batDau;
        try{batDau=KiemTraNghiepVu.docGioHen(txtGioHen.getText());}catch(IllegalArgumentException loi){lblLoiGio.setText(loi.getMessage());return;}
        if(sai)return;

        if(LocalDateTime.of(ngay,batDau).isBefore(LocalDateTime.now(MUI_GIO))){
            lblLoiGio.setText("Giờ hẹn đã qua. Chọn giờ hiện tại hoặc giờ sắp tới.");
            lblKhungGioDuKien.getStyleClass().add("khung-gio-du-kien-loi");
            lblKhungGioDuKien.setText("Khung giờ đã qua");return;
        }

        LocalTime ketThuc=batDau.plusMinutes(45);
        if(batDau.isBefore(ca.layGioBatDau())||ketThuc.isAfter(ca.layGioKetThuc())){
            lblLoiGio.setText("Khung 45 phút phải nằm hoàn toàn trong ca đã chọn.");return;
        }
        String ma=hoSoDaChon==null?null:hoSoDaChon.layMaBenhNhan(),lyDo=txtLichHenLyDo.getText();
        // Form không nhận ghi chú mới; khi sửa lịch cũ thì giữ nguyên ghi chú đã lưu.
        String ghiChu=lichDangSua==null?null:lichDangSua.layGhiChu();
        CongViecGiaoDien.chay(themLichHenBieuMau,lblLichHenBieuMauQuyTac,phien,()->lichDangSua==null?lichHenService.datLichHen(ma,ten,sdt,ca.layMaNhanVien(),ngay,batDau,ketThuc,lyDo,ghiChu):lichHenService.suaLichHen(lichDangSua.layMaLichHen(),ma,ten,sdt,ca.layMaNhanVien(),ngay,batDau,ketThuc,lyDo,ghiChu),lich->{khiLuu.accept(lich);CongViecGiaoDien.dong(themLichHenBieuMau);});
    }
}
