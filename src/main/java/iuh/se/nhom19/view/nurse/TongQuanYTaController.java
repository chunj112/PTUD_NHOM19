package iuh.se.nhom19.view.nurse;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import iuh.se.nhom19.auth.PhienDangNhap;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.dao.PhieuKhamDAO;
import iuh.se.nhom19.entity.PhanCong;
import iuh.se.nhom19.entity.PhieuKham;
import iuh.se.nhom19.service.DanhSachChoYTaService;
import iuh.se.nhom19.view.common.CongViecGiaoDien;

/** Dashboard Y tá đọc tổng hợp từ phân công và trạng thái thật của phiếu khám. */
public class TongQuanYTaController {
    private static final ZoneId MUI_GIO = ZoneId.of("Asia/Ho_Chi_Minh");
    @FXML private VBox trangTongQuanYTa;
    @FXML private Button btnLamMoiTongQuanYTa;
    @FXML private Label lblCaLamYTa, lblPhongYTa, lblSoChoYTa, lblSoDangKhamYTa, lblSoDaKhamYTa,
        lblSTTChoGanNhat, lblThongBaoTongQuanYTa;
    private PhienDangNhap phien;
    private DanhSachChoYTaService dichVu;

    public void cauHinh(PhienDangNhap phien, KetNoiCSDL ketNoi) {
        this.phien=phien; dichVu=new DanhSachChoYTaService(phien,ketNoi,new PhieuKhamDAO());
        btnLamMoiTongQuanYTa.setOnAction(e->lamMoi()); lamMoi();
    }

    private void lamMoi() {
        LocalDate homNay=LocalDate.now(MUI_GIO);
        CongViecGiaoDien.chay(trangTongQuanYTa,lblThongBaoTongQuanYTa,phien,()->{
            List<PhanCong> ca=dichVu.layLichLamViec(homNay,homNay);
            List<String> phong=dichVu.layPhongDuocPhanCong(homNay);
            List<PhieuKham> phieu=dichVu.layDanhSach(homNay,null,null);
            return new DuLieuDashboard(ca,phong,phieu);
        },this::hienThi);
    }

    private void hienThi(DuLieuDashboard duLieu) {
        lblCaLamYTa.setText(duLieu.cacCa().isEmpty()?"Hôm nay chưa có ca phân công":duLieu.cacCa().stream()
            .map(ca->ca.layTenCa()+" · "+ca.layMaPhong()).collect(java.util.stream.Collectors.joining("\n")));
        lblPhongYTa.setText(duLieu.cacPhong().isEmpty()?"Chưa có phòng được phân công":String.join(", ",duLieu.cacPhong()));
        lblSoChoYTa.setText(Integer.toString(dem(duLieu.phieu(),"CHO_KHAM")));
        lblSoDangKhamYTa.setText(Integer.toString(dem(duLieu.phieu(),"DANG_KHAM")));
        lblSoDaKhamYTa.setText(Integer.toString(dem(duLieu.phieu(),"DA_KHAM")));
        PhieuKham tiepTheo=duLieu.phieu().stream().filter(p->"CHO_KHAM".equals(p.layTrangThaiKham())).findFirst().orElse(null);
        lblSTTChoGanNhat.setText(tiepTheo==null?"Không có STT đang chờ":String.format("%03d · %s",
            tiepTheo.laySoThuTuKham(),tiepTheo.layBenhNhan().layHoTen()));
        lblThongBaoTongQuanYTa.setText("Số liệu theo ngày hôm nay và ca/phòng đã phân công. Danh sách chỉ đọc.");
    }

    private int dem(List<PhieuKham> phieu,String trangThai) {
        return (int)phieu.stream().filter(p->trangThai.equals(p.layTrangThaiKham())).count();
    }

    private record DuLieuDashboard(List<PhanCong> cacCa,List<String> cacPhong,List<PhieuKham> phieu) { }
}
