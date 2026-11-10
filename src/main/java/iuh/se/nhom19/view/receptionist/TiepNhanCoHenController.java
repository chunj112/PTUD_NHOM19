package iuh.se.nhom19.view.receptionist;

import java.util.function.Consumer;
import javafx.fxml.FXML;
import iuh.se.nhom19.view.common.TrangThaiGiaoDien;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import iuh.se.nhom19.auth.PhienDangNhap;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.entity.*;
import iuh.se.nhom19.service.*;
import iuh.se.nhom19.view.common.CongViecGiaoDien;

public class TiepNhanCoHenController {
    @FXML private BorderPane tiepNhanBieuMau;
    @FXML private ThemBenhNhanController chonBenhNhanController;
    @FXML private Label lblTomTatLichHen,lblTiepNhanKetQua,lblTrangThaiLichHen,lblCaLichHen;
    @FXML private Button btnHuy,btnXacNhanTiepNhan;
    private PhienDangNhap phien;
    private TiepNhanService dichVu;
    private String maLichHen;
    private Consumer<PhieuKham> khiTiepNhan;
    private record TomTat(LichHen lich,String ca) {}
    public void cauHinh(PhienDangNhap phien,KetNoiCSDL ketNoi,String maLichHen,Consumer<PhieuKham> khiTiepNhan) {
        this.phien=phien; this.maLichHen=maLichHen; this.khiTiepNhan=khiTiepNhan; dichVu=new TiepNhanService(phien,ketNoi);
        btnHuy.setDisable(false); btnHuy.setOnAction(suKien->CongViecGiaoDien.dong(tiepNhanBieuMau));
        btnXacNhanTiepNhan.setOnAction(suKien->xacNhan());
        CongViecGiaoDien.chay(tiepNhanBieuMau,lblTiepNhanKetQua,phien,()->{
            LichHen lich=new LichHenService(phien,ketNoi).timLichHenTheoMa(maLichHen);
            return new TomTat(lich,new PhanCongService(phien,ketNoi).layTomTatCa(lich));
        },tomTat->{
            LichHen lich=tomTat.lich();
            lblTomTatLichHen.setText(lich.layMaLichHen()+" · "+lich.layHoTenNguoiHen()+" · "+lich.laySoDienThoaiLienHe()+"\n"+lich.layNgayHen().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))+" · "+lich.layGioBatDau()+"–"+lich.layGioKetThuc()+"\nNha sĩ: "+lich.layTenNhaSi()+"\nLý do: "+(lich.layLyDoKham()==null?"Chưa ghi":lich.layLyDoKham()));
            lblCaLichHen.setText(tomTat.ca());TrangThaiGiaoDien.gan(lblTrangThaiLichHen,lich.layTrangThai());
            lblTiepNhanKetQua.setText(lich.layMaBenhNhan()==null?"Người đặt lịch chưa có hồ sơ bệnh nhân. Vui lòng kiểm tra thông tin trước khi tiếp nhận.":"Xác minh hồ sơ hiện có trước khi tiếp nhận.");
            chonBenhNhanController.cauHinh(phien,ketNoi,lich.layHoTenNguoiHen(),lich.laySoDienThoaiLienHe(),lich.layMaBenhNhan());
            boolean choTiepNhan="CHO_XAC_NHAN".equals(lich.layTrangThai())||"DA_XAC_NHAN".equals(lich.layTrangThai());
            boolean trongNgay=java.time.LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).equals(lich.layNgayHen());
            btnXacNhanTiepNhan.setDisable(!choTiepNhan||!trongNgay);
            if(choTiepNhan&&!trongNgay)lblTiepNhanKetQua.setText(lich.layNgayHen().isAfter(java.time.LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")))
                ?"Lịch hẹn thuộc ngày tương lai. Chỉ có thể tiếp nhận vào đúng ngày hẹn."
                :"Lịch hẹn đã qua ngày. Không thể tiếp nhận từ lịch này.");
        });
    }
    private void xacNhan() {
        try {
            String maBenhNhan=chonBenhNhanController.layMaBenhNhanDaChon(); BenhNhan moi=chonBenhNhanController.layThongTinMoi();
            CongViecGiaoDien.chay(tiepNhanBieuMau,lblTiepNhanKetQua,phien,()->dichVu.tiepNhanCoHen(maLichHen,maBenhNhan,moi),phieu->{
                khiTiepNhan.accept(phieu); CongViecGiaoDien.dong(tiepNhanBieuMau);
            });
        }catch(IllegalArgumentException loi){lblTiepNhanKetQua.setText(loi.getMessage());}
    }
}
