package iuh.se.nhom19.view.receptionist;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import javafx.fxml.FXML;
import iuh.se.nhom19.view.common.TrangThaiGiaoDien;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import iuh.se.nhom19.auth.PhienDangNhap;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.entity.LichHen;
import iuh.se.nhom19.service.LichHenService;
import iuh.se.nhom19.view.common.CongViecGiaoDien;

public class LichHenTuanController {
    @FXML private VBox tiepNhanTuan, daChonTuanLichHenKhung;
    @FXML private GridPane lichHenTuanLuoi;
    @FXML private Button btnTruocLichHenTuan, btnTiepLichHenTuan;
    @FXML private DatePicker dpLichHenTuanNgayChon;
    @FXML private ComboBox<String> cboTuanNhaSi, cboTuanTinhTrang;
    @FXML private Label lblTuanKhoangNgay, lblTuanThongBao, lblTuanCanhBaoLichTuongLai;
    @FXML private ChiTietLichHenController tuanLichHenPhanChiTietController;
    @FXML private Label lblKetQuaTiepNhanTuan;
    private PhienDangNhap phien;
    private LichHenService dichVu;
    private LocalDate dauTuan;
    private LocalDate ngayDuocChon;
    private List<LichHen> danhSach = new ArrayList<>();

    @FXML private void initialize(){CongViecGiaoDien.anKhiTrong(lblKetQuaTiepNhanTuan);CongViecGiaoDien.anKhiTrong(lblTuanThongBao);CongViecGiaoDien.anKhiTrong(lblTuanCanhBaoLichTuongLai);}

    public void cauHinh(PhienDangNhap phien, KetNoiCSDL ketNoi) {
        this.phien = phien; dichVu = new LichHenService(phien, ketNoi);
        tuanLichHenPhanChiTietController.cauHinh(phien, ketNoi, thongBao -> { lamMoi(); lblKetQuaTiepNhanTuan.setText(thongBao); });
        LocalDate homNay=LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        dauTuan = homNay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        dpLichHenTuanNgayChon.setConverter(new StringConverter<>() {
            private final DateTimeFormatter dinhDang=DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override public String toString(LocalDate ngay){return ngay==null?"":dinhDang.format(ngay);}
            @Override public LocalDate fromString(String chu){return chu==null||chu.isBlank()?null:LocalDate.parse(chu,dinhDang);}
        });
        ngayDuocChon=homNay;dpLichHenTuanNgayChon.setValue(homNay);
        cboTuanTinhTrang.getItems().setAll("Tất cả trạng thái", "Chờ xác nhận", "Đã xác nhận", "Đã đến"); cboTuanTinhTrang.setValue("Tất cả trạng thái");
        btnTruocLichHenTuan.setDisable(false); btnTiepLichHenTuan.setDisable(false);
        btnTruocLichHenTuan.setOnAction(suKien -> chonNgay(ngayDuocChon.minusWeeks(1)));
        btnTiepLichHenTuan.setOnAction(suKien -> chonNgay(ngayDuocChon.plusWeeks(1)));
        dpLichHenTuanNgayChon.setOnAction(suKien -> chonNgay(dpLichHenTuanNgayChon.getValue()));
        tuanLichHenPhanChiTietController.khiDong(()->{daChonTuanLichHenKhung.setVisible(false);daChonTuanLichHenKhung.setManaged(false);});
        cboTuanNhaSi.setOnAction(suKien -> veLich()); cboTuanTinhTrang.setOnAction(suKien -> veLich()); lamMoi();
    }
    private void chonNgay(LocalDate ngay){
        if(ngay==null)return;
        ngayDuocChon=ngay;dpLichHenTuanNgayChon.setValue(ngay);
        dauTuan=ngay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        lamMoi();
    }
    public void lamMoi() {
        if (dichVu == null) return;
        daChonTuanLichHenKhung.setManaged(false); daChonTuanLichHenKhung.setVisible(false);
        var tuNgay = dauTuan; danhSach.clear(); lichHenTuanLuoi.getChildren().clear();
        DateTimeFormatter dinhDang=DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblTuanKhoangNgay.setText(tuNgay.format(dinhDang)+" – "+tuNgay.plusDays(6).format(dinhDang));
        CongViecGiaoDien.chay(tiepNhanTuan, lblTuanThongBao, phien, () -> {
            return new KetQuaTuan(dichVu.layDanhSachLichHen(tuNgay, tuNgay.plusDays(6)),dichVu.layLichHenTuongLaiDaDen());
        }, ketQua -> {
            danhSach = ketQua.lich();
            if(ketQua.batThuong().isEmpty())lblTuanCanhBaoLichTuongLai.setText("");
            else lblTuanCanhBaoLichTuongLai.setText("Cần rà soát "+ketQua.batThuong().size()+" lịch tương lai đang mang trạng thái Đã đến: "+ketQua.batThuong().stream().map(LichHen::layMaLichHen).toList()+". Dữ liệu chưa bị tự sửa.");
            cboTuanNhaSi.getItems().setAll("Tất cả nha sĩ");
            for (LichHen lichHen : danhSach) {
                String nhaSi = lichHen.layMaNhaSi() + " · " + lichHen.layTenNhaSi();
                if (!cboTuanNhaSi.getItems().contains(nhaSi)) cboTuanNhaSi.getItems().add(nhaSi);
            }
            if(cboTuanNhaSi.getValue()==null||!cboTuanNhaSi.getItems().contains(cboTuanNhaSi.getValue()))cboTuanNhaSi.setValue("Tất cả nha sĩ");
            veLich();
        });
    }
    private void veLich() {
        if (dauTuan == null) return;
        lichHenTuanLuoi.getChildren().clear();
        String nhaSi = cboTuanNhaSi.getValue(), trangThai = cboTuanTinhTrang.getValue();
        for (int ngay = 0; ngay < 7; ngay++) {
            LocalDate ngayHen = dauTuan.plusDays(ngay); VBox cot = new VBox(10); cot.getStyleClass().add("le-tan-lich-ngay-lam"); cot.setMinWidth(0); cot.setPrefWidth(0); cot.setMaxWidth(Double.MAX_VALUE);
            // Tô nền nhẹ cho cột ngày hiện tại, không tác động kiểu chữ của tiêu đề.
            if (ngayHen.equals(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")))) {
                cot.getStyleClass().add("le-tan-lich-hom-nay");
            }
            String[] thu={"Thứ Hai","Thứ Ba","Thứ Tư","Thứ Năm","Thứ Sáu","Thứ Bảy","Chủ nhật"};
            Label tieuDe = new Label(thu[ngay]+"\n"+ngayHen.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"))); cot.getChildren().add(tieuDe);
            int soLuong = 0;
            for (LichHen lichHen : danhSach) {
                if (!ngayHen.equals(lichHen.layNgayHen())) continue;
                if (nhaSi != null && !nhaSi.equals("Tất cả nha sĩ") && !nhaSi.startsWith(lichHen.layMaNhaSi() + " · ")) continue;
                if (trangThai != null && !trangThai.equals("Tất cả trạng thái") && !trangThai.equals(TrangThaiGiaoDien.nhan(lichHen.layTrangThai()))) continue;
                VBox noiDung=new VBox(3); noiDung.setMinWidth(0); noiDung.setMaxWidth(Double.MAX_VALUE);
                Label gio=new Label(lichHen.layGioBatDau()+"–"+lichHen.layGioKetThuc());gio.setMinWidth(0);gio.setMaxWidth(Double.MAX_VALUE);gio.setTextOverrun(OverrunStyle.ELLIPSIS);
                Label ten=new Label(lichHen.layHoTenNguoiHen());ten.setMinWidth(0);ten.setMaxWidth(Double.MAX_VALUE);ten.setTextOverrun(OverrunStyle.ELLIPSIS);
                Label nhan=new Label();nhan.setMinWidth(0);nhan.setMaxWidth(Double.MAX_VALUE);nhan.setTextOverrun(OverrunStyle.ELLIPSIS);TrangThaiGiaoDien.gan(nhan,lichHen.layTrangThai());
                Label nhaSiPhong=new Label("Nha sĩ: "+lichHen.layTenNhaSi()+" · Phòng: "+(lichHen.layMaPhong()==null?"—":lichHen.layMaPhong()));
                nhaSiPhong.setMinWidth(0);nhaSiPhong.setMaxWidth(Double.MAX_VALUE);nhaSiPhong.setTextOverrun(OverrunStyle.ELLIPSIS);nhaSiPhong.getStyleClass().add("the-lich-tuan-thong-tin");
                noiDung.getChildren().addAll(gio,ten,nhaSiPhong,nhan);
                Button the=new Button();the.setGraphic(noiDung);the.setMinWidth(0);the.setPrefWidth(0);the.setMaxWidth(Double.MAX_VALUE);
                the.getStyleClass().add("the-lich-tuan");the.setTooltip(new Tooltip(lichHen.layHoTenNguoiHen()+"\n"+lichHen.laySoDienThoaiLienHe()+"\n"+lichHen.layTenNhaSi()+" · "+(lichHen.layMaPhong()==null?"Chưa xác định phòng":lichHen.layMaPhong())));
                noiDung.prefWidthProperty().bind(the.widthProperty().subtract(24));
                the.setOnAction(suKien->{tuanLichHenPhanChiTietController.hienThi(lichHen);daChonTuanLichHenKhung.setManaged(true);daChonTuanLichHenKhung.setVisible(true);});
                cot.getChildren().add(the); soLuong++;
            }
            if (soLuong == 0) cot.getChildren().add(new Label("Không có lịch"));
            lichHenTuanLuoi.add(cot, ngay, 0);
        }
    }
    private record KetQuaTuan(List<LichHen> lich,List<LichHen> batThuong) {}
}
