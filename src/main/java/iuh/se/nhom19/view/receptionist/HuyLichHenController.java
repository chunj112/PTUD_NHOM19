package iuh.se.nhom19.view.receptionist;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import iuh.se.nhom19.auth.PhienDangNhap;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.service.LichHenService;
import iuh.se.nhom19.view.common.CongViecGiaoDien;

public class HuyLichHenController {
    @FXML private VBox huyLichHenBieuMau;
    @FXML private Label lblHuyLichHenTomTat,lblHuyLichHenThongBao;
    @FXML private Button btnDong,btnHuy,btnXacNhanHuyLichHen;
    @FXML private TextField txtHuyLichHenLyDo;
    public void cauHinh(PhienDangNhap phien,KetNoiCSDL ketNoi,String maLichHen,Runnable khiHuy) {
        LichHenService dichVu=new LichHenService(phien,ketNoi);
        btnDong.setDisable(false); btnHuy.setDisable(false);
        btnDong.setOnAction(suKien->CongViecGiaoDien.dong(huyLichHenBieuMau)); btnHuy.setOnAction(suKien->CongViecGiaoDien.dong(huyLichHenBieuMau));
        CongViecGiaoDien.chay(huyLichHenBieuMau,lblHuyLichHenThongBao,phien,()->dichVu.timLichHenTheoMa(maLichHen),lich->{
            lblHuyLichHenTomTat.setText(lich.layHoTenNguoiHen()+" · "+lich.laySoDienThoaiLienHe()+"\n"+lich.layNgayHen()+" · "+lich.layGioBatDau()+"–"+lich.layGioKetThuc());
            lblHuyLichHenThongBao.setText("Hủy chỉ đổi trạng thái lịch. Không xóa bệnh nhân hoặc hồ sơ.");
            btnXacNhanHuyLichHen.setDisable(!"CHO_XAC_NHAN".equals(lich.layTrangThai())&&!"DA_XAC_NHAN".equals(lich.layTrangThai()));
        });
        btnXacNhanHuyLichHen.setOnAction(suKien->{
            if(txtHuyLichHenLyDo.getText().isBlank()){lblHuyLichHenThongBao.setText("Vui lòng nhập lý do hủy.");return;}
            String lyDo=txtHuyLichHenLyDo.getText();
            CongViecGiaoDien.chay(huyLichHenBieuMau,lblHuyLichHenThongBao,phien,()->{dichVu.huyLichHen(maLichHen,lyDo);return true;},ketQua->{khiHuy.run();CongViecGiaoDien.dong(huyLichHenBieuMau);});
        });
    }
}
