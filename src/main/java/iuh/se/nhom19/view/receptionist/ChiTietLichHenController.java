package iuh.se.nhom19.view.receptionist;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import iuh.se.nhom19.view.common.TrangThaiGiaoDien;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import iuh.se.nhom19.auth.PhienDangNhap;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.entity.LichHen;
import iuh.se.nhom19.view.common.CongViecGiaoDien;

public class ChiTietLichHenController {
    @FXML private VBox lichHenChiTiet;
    @FXML private Label lblLichHenTieuDe,lblLichHenTinhTrang,lblLichHenBenhNhan,lblLichHenSoDienThoai,lblLichHenTuoiGioiTinh,lblLichHenNgayGio,lblLichHenNhaSiPhong,lblLichHenLyDo,lblLichHenDiUng;
    @FXML private Button btnTiepNhanLichHen,btnSuaLichHen,btnHuyLichHen;
    @FXML private Button btnDongChiTiet;
    @FXML private Label lblGhiChuLichHen;
    public void khiDong(Runnable dong){btnDongChiTiet.setDisable(false);btnDongChiTiet.setOnAction(suKien->dong.run());}
    private PhienDangNhap phien;
    private KetNoiCSDL ketNoi;
    private LichHen daChon;
    private Consumer<String> khiCapNhat;
    public void cauHinh(PhienDangNhap phien,KetNoiCSDL ketNoi,Consumer<String> khiCapNhat) {
        this.phien=phien; this.ketNoi=ketNoi; this.khiCapNhat=khiCapNhat;
        btnTiepNhanLichHen.setOnAction(suKien->moTiepNhan()); btnSuaLichHen.setOnAction(suKien->moSua()); btnHuyLichHen.setOnAction(suKien->moHuy());
    }
    public void hienThi(LichHen lich) {
        daChon=lich; lblGhiChuLichHen.setText("Ghi chú: "+(lich.layGhiChu()==null?"Chưa có":lich.layGhiChu())); lblLichHenTieuDe.setText(lich.layMaLichHen()); TrangThaiGiaoDien.gan(lblLichHenTinhTrang,lich.layTrangThai());
        // Snapshot lịch luôn có tên/SĐT dù chưa có BenhNhan; không tạo đối tượng bệnh nhân giả để hiển thị.
        lblLichHenBenhNhan.setText(lich.layHoTenNguoiHen()); lblLichHenSoDienThoai.setText(lich.laySoDienThoaiLienHe());
        var benhNhan=lich.layBenhNhan();
        lblLichHenTuoiGioiTinh.setText(benhNhan==null?"Chưa có hồ sơ bệnh nhân":benhNhan.layMaBenhNhan()+" · "+Period.between(benhNhan.layNgaySinh(),LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))).getYears()+" tuổi · "+(benhNhan.layNam()?"Nam":"Nữ"));
        lblLichHenNgayGio.setText(lich.layNgayHen().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))+" · "+lich.layGioBatDau()+"–"+lich.layGioKetThuc());
        String phong=lich.layMaPhong()==null?"Chưa xác định phòng":lich.layMaPhong();
        lblLichHenNhaSiPhong.setText(lich.layTenNhaSi()+" · "+phong); lblLichHenLyDo.setText(lich.layLyDoKham()==null?"Chưa ghi lý do":lich.layLyDoKham());
        lblLichHenDiUng.setVisible(false); lblLichHenDiUng.setManaged(false);
        boolean conCho="CHO_XAC_NHAN".equals(lich.layTrangThai())||"DA_XAC_NHAN".equals(lich.layTrangThai());
        boolean trongNgay=LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).equals(lich.layNgayHen());
        btnTiepNhanLichHen.setDisable(!conCho||!trongNgay);
        btnTiepNhanLichHen.setTooltip(!conCho?new Tooltip("Lịch này không còn ở trạng thái chờ tiếp nhận."):
            !trongNgay?new Tooltip(lich.layNgayHen().isAfter(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                ?"Không thể tiếp nhận lịch của ngày tương lai.":"Không thể tiếp nhận lịch đã qua ngày."):null);
        btnSuaLichHen.setDisable(!conCho); btnHuyLichHen.setDisable(!conCho);
    }
    private void moTiepNhan() {
        if(daChon==null)return;
        CongViecGiaoDien.moHopThoai(lichHenChiTiet,"/fxml/receptionist/tiep-nhan.fxml","Tiếp nhận bệnh nhân có lịch hẹn",boNap->{
            TiepNhanCoHenController dieuKhien=boNap.getController();
            dieuKhien.cauHinh(phien,ketNoi,daChon.layMaLichHen(),phieu->khiCapNhat.accept("Tiếp nhận thành công · STT "
                +String.format("%03d",phieu.laySoThuTuKham())+" · Phiếu "+phieu.layMaPhieuKham()+" · Chờ khám"));
        });
    }
    private void moSua() {
        if(daChon==null)return;
        CongViecGiaoDien.moHopThoai(lichHenChiTiet,"/fxml/receptionist/sua-lich-hen.fxml","Sửa lịch hẹn",boNap->{
            DatLichHenController dieuKhien=(DatLichHenController)boNap.getNamespace().get("bieuMauSuaController");
            dieuKhien.cauHinhSua(phien,ketNoi,daChon.layMaLichHen(),lich->khiCapNhat.accept("Đã cập nhật lịch "+lich.layMaLichHen()));
        });
    }
    private void moHuy() {
        if(daChon==null)return;
        String maLichHen=daChon.layMaLichHen();
        CongViecGiaoDien.moHopThoai(lichHenChiTiet,"/fxml/receptionist/huy-lich-hen.fxml","Hủy lịch hẹn",boNap->{
            HuyLichHenController dieuKhien=boNap.getController(); dieuKhien.cauHinh(phien,ketNoi,maLichHen,()->khiCapNhat.accept("Đã hủy lịch "+maLichHen));
        });
    }
}
