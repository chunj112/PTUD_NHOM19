package iuh.se.nhom19.view.nurse;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import iuh.se.nhom19.auth.PhienDangNhap;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.entity.PhanCong;
import iuh.se.nhom19.service.DanhSachChoYTaService;
import iuh.se.nhom19.view.common.CongViecGiaoDien;

/** Lịch cá nhân của Y tá chỉ xem; quyền sửa/phân công thuộc vai trò Quản lý. */
public class LichLamViecYTaController {
    private static final ZoneId MUI_GIO = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    @FXML private VBox trangLichYTa;
    @FXML private DatePicker dpNgayLichYTa;
    @FXML private Button btnTuanTruocYTa, btnTuanSauYTa, btnHomNayYTa, btnLamMoiLichYTa;
    @FXML private Label lblKhoangTuanYTa, lblThongBaoLichYTa;
    @FXML private TableView<PhanCong> tblLichYTa;
    @FXML private TableColumn<PhanCong,String> cotNgay, cotCa, cotGio, cotPhong, cotCongViec, cotTrangThai;
    private PhienDangNhap phien;
    private DanhSachChoYTaService dichVu;
    private boolean dangNap;

    @FXML private void initialize() {
        cotNgay.setCellValueFactory(dong -> new ReadOnlyStringWrapper(dong.getValue().layNgayLam().format(NGAY)));
        cotCa.setCellValueFactory(dong -> new ReadOnlyStringWrapper(dong.getValue().layTenCa()));
        cotGio.setCellValueFactory(dong -> new ReadOnlyStringWrapper(dong.getValue().layGioBatDau()+"–"+dong.getValue().layGioKetThuc()));
        cotPhong.setCellValueFactory(dong -> new ReadOnlyStringWrapper(dong.getValue().layMaPhong()==null?"—":dong.getValue().layMaPhong()));
        cotCongViec.setCellValueFactory(dong -> new ReadOnlyStringWrapper(dong.getValue().layNoiDungCongViec()==null?"—":dong.getValue().layNoiDungCongViec()));
        cotTrangThai.setCellValueFactory(dong -> new ReadOnlyStringWrapper("Đã phân công"));
        dpNgayLichYTa.valueProperty().addListener((p,cu,moi)->{if(!dangNap&&moi!=null)lamMoi();});
        btnTuanTruocYTa.setOnAction(e->doiTuan(-1)); btnTuanSauYTa.setOnAction(e->doiTuan(1));
        btnHomNayYTa.setOnAction(e->{dangNap=true;dpNgayLichYTa.setValue(LocalDate.now(MUI_GIO));dangNap=false;lamMoi();});
        btnLamMoiLichYTa.setOnAction(e->lamMoi());
        tblLichYTa.setPlaceholder(new Label("Bạn chưa có ca được phân công trong tuần này."));
    }

    public void cauHinh(PhienDangNhap phien, KetNoiCSDL ketNoi) {
        this.phien=phien; dichVu=new DanhSachChoYTaService(phien,ketNoi,new iuh.se.nhom19.dao.PhieuKhamDAO());
        dangNap=true;dpNgayLichYTa.setValue(LocalDate.now(MUI_GIO));dangNap=false;lamMoi();
    }

    private void doiTuan(int buoc) {
        LocalDate ngay=dpNgayLichYTa.getValue()==null?LocalDate.now(MUI_GIO):dpNgayLichYTa.getValue();
        dangNap=true;dpNgayLichYTa.setValue(ngay.plusWeeks(buoc));dangNap=false;lamMoi();
    }

    private void lamMoi() {
        LocalDate chon=dpNgayLichYTa.getValue(); if(chon==null)return;
        LocalDate dauTuan=chon.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate cuoiTuan=dauTuan.plusDays(6);
        lblKhoangTuanYTa.setText(dauTuan.format(NGAY)+" – "+cuoiTuan.format(NGAY));
        CongViecGiaoDien.chay(trangLichYTa,lblThongBaoLichYTa,phien,
            ()->dichVu.layLichLamViec(dauTuan,cuoiTuan),this::hienLich);
    }

    private void hienLich(List<PhanCong> lich) {
        tblLichYTa.getItems().setAll(lich);
        lblThongBaoLichYTa.setText(lich.size()+" ca trong tuần · Chỉ xem theo lịch phân công của bạn.");
    }
}
