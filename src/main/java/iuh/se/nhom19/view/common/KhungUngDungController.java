package iuh.se.nhom19.view.common;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import iuh.se.nhom19.app.DieuHuongUngDung;
import iuh.se.nhom19.app.DuongDanManHinh;
import iuh.se.nhom19.auth.VaiTro;
import iuh.se.nhom19.auth.PhienDangNhap;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.view.receptionist.TiepNhanController;
import iuh.se.nhom19.view.receptionist.TraCuuBenhNhanController;
import iuh.se.nhom19.view.dentist.DanhSachChoKhamController;
import iuh.se.nhom19.view.nurse.DanhSachChoYTaController;
import iuh.se.nhom19.view.nurse.LichLamViecYTaController;
import iuh.se.nhom19.view.nurse.TongQuanYTaController;

/** AppShell dùng danh tính Session đã xác thực; chức năng chưa có backend hiển thị trống, không gán dữ liệu giả. */
public final class KhungUngDungController {
    @FXML private BorderPane khungUngDung;
    @FXML private VBox thanhBen, chuThuongHieu, khuVucDanhTinh;
    @FXML private HBox khuVucThuongHieu;
    @FXML private ImageView imgThuongHieuLogo;
    @FXML private VBox trinhDonQuanLy, trinhDonLeTan, trinhDonNhaSi, trinhDonYTa, trinhDonDieuHuong;
    @FXML private StackPane vungNoiDung;
    @FXML private ScrollPane vungCuonNoiDung;
    @FXML private Label lblVaiTro, lblVaiTroDuongDan, lblTrangDuongDan, lblTenThanhBen,
        lblTrangThaiThanhBen, lblChuCaiDauHoTen, lblNgayHienTai;
    @FXML private MenuButton mnuTaiKhoan;
    @FXML private MenuItem miThongTinCaNhan, miThayDoiMatKhau, miDangXuat;
    @FXML private Button btnTongQuan, btnNhanSu, btnPhanCong, btnDichVu, btnBaoCao,
        btnTrangTiepNhanLichHen, btnThuNgan, btnTraCuuBenhNhan, btnLichLeTan,
        btnKhamBenh, btnLichNhaSi, btnDanhSachChoYTa, btnLichYTa, btnThuGonThanhBen;

    private Map<DuongDanManHinh, Button> cacNut;
    private PhienDangNhap phienDangNhap;
    private DieuHuongUngDung dieuHuongUngDung;
    private long phienBanDangNhap;
    private boolean daGiaiPhong;
    private boolean thanhBenThuGon;
    private KetNoiCSDL ketNoiNghiepVu;

    @FXML private void initialize() {
        anTrinhDonVaiTro();
        khungUngDung.setDisable(true);
        cacNut = new EnumMap<>(DuongDanManHinh.class);
        cacNut.put(DuongDanManHinh.TONG_QUAN, btnTongQuan);
        cacNut.put(DuongDanManHinh.NHAN_SU, btnNhanSu);
        cacNut.put(DuongDanManHinh.PHAN_CONG, btnPhanCong);
        cacNut.put(DuongDanManHinh.DICH_VU, btnDichVu);
        cacNut.put(DuongDanManHinh.BAO_CAO, btnBaoCao);
        cacNut.put(DuongDanManHinh.TIEP_NHAN, btnTrangTiepNhanLichHen);
        cacNut.put(DuongDanManHinh.THU_NGAN, btnThuNgan);
        cacNut.put(DuongDanManHinh.BENH_NHAN, btnTraCuuBenhNhan);
        cacNut.put(DuongDanManHinh.LICH_LE_TAN, btnLichLeTan);
        cacNut.put(DuongDanManHinh.KHAM_BENH, btnKhamBenh);
        cacNut.put(DuongDanManHinh.LICH_NHA_SI, btnLichNhaSi);
        cacNut.put(DuongDanManHinh.DANH_SACH_CHO_Y_TA, btnDanhSachChoYTa);
        cacNut.put(DuongDanManHinh.LICH_Y_TA, btnLichYTa);
        for (Map.Entry<DuongDanManHinh, Button> muc : cacNut.entrySet()) {
            DuongDanManHinh manHinh = muc.getKey();
            muc.getValue().setOnAction(suKien -> chuyenTrang(manHinh));
        }
        for (Button nut : cacNut.values()) nut.setTooltip(new Tooltip(nut.getText()));
        btnThuGonThanhBen.setTooltip(new Tooltip("Thu gọn menu"));
        btnThuGonThanhBen.setOnAction(suKien -> doiTrangThaiThanhBen());
        miThongTinCaNhan.setOnAction(suKien -> chuyenTrang(DuongDanManHinh.THONG_TIN_CA_NHAN));
        miThayDoiMatKhau.setOnAction(suKien -> chuyenTrang(DuongDanManHinh.MAT_KHAU));
    }

    /** Thu gọn Sidebar ngay trên Scene hiện tại, không nạp lại trang hoặc làm mới form. */
    private void doiTrangThaiThanhBen() {
        thanhBenThuGon = !thanhBenThuGon;
        if (thanhBenThuGon) {
            thanhBen.getStyleClass().add("thu-gon");
            thanhBen.setMinWidth(64); thanhBen.setPrefWidth(64); thanhBen.setMaxWidth(68);
            chuThuongHieu.setVisible(false); chuThuongHieu.setManaged(false);
            khuVucDanhTinh.setVisible(false); khuVucDanhTinh.setManaged(false);
            lblVaiTro.setVisible(false); lblVaiTro.setManaged(false);
            khuVucThuongHieu.setAlignment(javafx.geometry.Pos.CENTER); khuVucThuongHieu.setSpacing(0);
            btnThuGonThanhBen.setText("›"); btnThuGonThanhBen.setAccessibleText("Mở rộng menu");
            btnThuGonThanhBen.setMinWidth(20);btnThuGonThanhBen.setPrefWidth(20);btnThuGonThanhBen.setMaxWidth(20);
            btnThuGonThanhBen.setTooltip(new Tooltip("Mở rộng menu"));
            imgThuongHieuLogo.setFitWidth(22); imgThuongHieuLogo.setFitHeight(22);
            for (Button nut : cacNut.values()) nut.setText("");
        } else {
            thanhBen.getStyleClass().remove("thu-gon");
            thanhBen.setMinWidth(232); thanhBen.setPrefWidth(232); thanhBen.setMaxWidth(255);
            chuThuongHieu.setVisible(true); chuThuongHieu.setManaged(true);
            khuVucDanhTinh.setVisible(true); khuVucDanhTinh.setManaged(true);
            lblVaiTro.setVisible(true); lblVaiTro.setManaged(true);
            khuVucThuongHieu.setAlignment(javafx.geometry.Pos.CENTER_LEFT); khuVucThuongHieu.setSpacing(10);
            btnThuGonThanhBen.setText("‹"); btnThuGonThanhBen.setAccessibleText("Thu gọn menu");
            btnThuGonThanhBen.setMinWidth(28);btnThuGonThanhBen.setPrefWidth(28);btnThuGonThanhBen.setMaxWidth(28);
            btnThuGonThanhBen.setTooltip(new Tooltip("Thu gọn menu"));
            imgThuongHieuLogo.setFitWidth(38); imgThuongHieuLogo.setFitHeight(38);
            for (Map.Entry<DuongDanManHinh, Button> muc : cacNut.entrySet())
                muc.getValue().setText(muc.getValue().getTooltip().getText());
        }
    }

    public void cauHinh(PhienDangNhap phienCanDung, DieuHuongUngDung dieuHuongCanDung) {
        cauHinh(phienCanDung, dieuHuongCanDung, new KetNoiCSDL());
    }

    public void cauHinh(PhienDangNhap phienCanDung, DieuHuongUngDung dieuHuongCanDung, KetNoiCSDL ketNoiNghiepVu) {
        if (daGiaiPhong || phienCanDung == null || dieuHuongCanDung == null) {
            throw new IllegalStateException("Không thể mở AppShell.");
        }
        phienDangNhap = phienCanDung;
        dieuHuongUngDung = dieuHuongCanDung;
        this.ketNoiNghiepVu = ketNoiNghiepVu;
        phienBanDangNhap = phienDangNhap.layPhienBan();
        VaiTro vaiTro = phienDangNhap.yeuCauVaiTro(phienBanDangNhap);

        // Ẩn và bỏ chỗ của mọi nhóm, sau đó chỉ bật đúng một nhóm menu.
        anTrinhDonVaiTro();
        VBox trinhDonDangDung;
        switch (vaiTro) {
            case MANAGER: trinhDonDangDung = trinhDonQuanLy; break;
            case RECEPTIONIST: trinhDonDangDung = trinhDonLeTan; break;
            case DENTIST: trinhDonDangDung = trinhDonNhaSi; break;
            case NURSE: trinhDonDangDung = trinhDonYTa; break;
            default: throw new IllegalStateException("Vai trò không hợp lệ.");
        }
        trinhDonDangDung.setVisible(true);
        trinhDonDangDung.setManaged(true);

        lblVaiTro.setText(vaiTro.layTenHienThi().toUpperCase(Locale.forLanguageTag("vi")));
        lblVaiTroDuongDan.setText(vaiTro.layTenHienThi());
        lblTenThanhBen.setText(phienDangNhap.layHoTen());
        lblTrangThaiThanhBen.setText("Đã đăng nhập · "+vaiTro.layTenHienThi());
        lblTrangThaiThanhBen.setWrapText(true);
        lblChuCaiDauHoTen.setText(layChuCaiDau(phienDangNhap.layHoTen()));
        lblNgayHienTai.setText(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))
            .format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale.forLanguageTag("vi"))));
        mnuTaiKhoan.setText(phienDangNhap.layHoTen());
        khungUngDung.setDisable(false);
        chuyenTrang(DuongDanManHinh.TONG_QUAN);
    }

    /** Chỉ đổi contentArea; không tạo lại Stage, Scene, Sidebar hoặc Topbar. */
    public void chuyenTrang(DuongDanManHinh manHinh) {
        if (!laPhienHienTai()) {
            throw new IllegalStateException("Phiên đăng nhập đã kết thúc. Vui lòng đăng nhập lại.");
        }
        VaiTro vaiTro = phienDangNhap.yeuCauVaiTro(phienBanDangNhap);
        // Kiểm tra route ở đây không thay thế kiểm tra quyền của Service/DAO khi các UC chuyển sang SQL.
        if (manHinh == null || !manHinh.choPhep(vaiTro)) {
            throw new IllegalArgumentException("Màn hình không thuộc vai trò của phiên đăng nhập.");
        }

        Parent trang;
        var duongDanTaiNguyen = getClass().getResource(manHinh.layDuongDanFXML(vaiTro));
        try {
            if (duongDanTaiNguyen == null) {
                trang = taoTrangChuaSanSang(manHinh.layTieuDe(),
                    "Chức năng đang phát triển. Màn hình hoặc backend chưa được tích hợp.");
            } else {
                FXMLLoader boNapFXML = new FXMLLoader(duongDanTaiNguyen);
                trang = boNapFXML.load();
                // Cấp cùng Session đã xác thực cho Controller nghiệp vụ; không tạo Session mới theo Role.
                Object dieuKhienTrang = boNapFXML.getController();
                if (dieuKhienTrang instanceof TiepNhanController dieuKhien) dieuKhien.cauHinh(phienDangNhap, ketNoiNghiepVu);
                if (dieuKhienTrang instanceof TraCuuBenhNhanController dieuKhien) dieuKhien.cauHinh(phienDangNhap, ketNoiNghiepVu);
                if (dieuKhienTrang instanceof DanhSachChoKhamController dieuKhien) dieuKhien.cauHinh(phienDangNhap, ketNoiNghiepVu);
                if (dieuKhienTrang instanceof DanhSachChoYTaController dieuKhien) dieuKhien.cauHinh(phienDangNhap, ketNoiNghiepVu);
                if (dieuKhienTrang instanceof LichLamViecYTaController dieuKhien) dieuKhien.cauHinh(phienDangNhap, ketNoiNghiepVu);
                if (dieuKhienTrang instanceof TongQuanYTaController dieuKhien) dieuKhien.cauHinh(phienDangNhap, ketNoiNghiepVu);
            }
        } catch (IOException loi) {
            trang = taoTrangChuaSanSang(manHinh.layTieuDe(), "Không thể nạp giao diện của màn hình.");
        }
        // Nạp nội dung trang vào khung dùng chung.
        VBox noiDung = new VBox(trang);
        noiDung.setMinWidth(0);
        for(Node trangCu:vungNoiDung.getChildren())trangCu.getProperties().put("daDong",true);
        vungNoiDung.getChildren().setAll(noiDung);
        vungCuonNoiDung.setVvalue(0);
        lblTrangDuongDan.setText(manHinh.layTieuDe());
        xoaLuaChon();
        if (cacNut.containsKey(manHinh)) {
            cacNut.get(manHinh).getStyleClass().add("trinh-don-da-chon");
        }

        // Hai nút Dashboard chỉ nối điều hướng, chưa làm Controller nghiệp vụ của module khác.
        Node nutBaoCao = trang.lookup("#btnXemBaoCao");
        if (nutBaoCao instanceof Button nut) {
            nut.setDisable(false);nut.setOnAction(suKien -> chuyenTrang(DuongDanManHinh.BAO_CAO));
        }
        Node nutLich = trang.lookup("#btnViewSchedule");
        if (nutLich instanceof Button nut) {
            nut.setDisable(false);nut.setOnAction(suKien -> chuyenTrang(DuongDanManHinh.PHAN_CONG));
        }
        Node nutChoYTa = trang.lookup("#btnXemDanhSachChoYTa");
        if (nutChoYTa instanceof Button nut) {
            nut.setDisable(false); nut.setOnAction(suKien -> chuyenTrang(DuongDanManHinh.DANH_SACH_CHO_Y_TA));
        }
        dieuHuongUngDung.capNhatTieuDe(vaiTro.layTenHienThi() + " / " + manHinh.layTieuDe());
    }

    @FXML private void xuLyDangXuat() {
        if (laPhienHienTai()) {
            dieuHuongUngDung.dangXuat();
        }
    }

    private String layChuCaiDau(String hoTen) {
        String[] tu=hoTen.trim().split("\\s+");String ketQua="";
        for(int i=Math.max(0,tu.length-2);i<tu.length;i++)ketQua+=tu[i].substring(0,1);
        return ketQua.toUpperCase(Locale.forLanguageTag("vi"));
    }
    private boolean laPhienHienTai() {
        if (daGiaiPhong || phienDangNhap == null || !phienDangNhap.dangHoatDong()) {
            return false;
        }
        return phienDangNhap.layPhienBan() == phienBanDangNhap;
    }

    private Parent taoTrangChuaSanSang(String tieuDe, String thongBao) {
        try {
            FXMLLoader boNapFXML = new FXMLLoader(getClass().getResource("/fxml/common/trang-chua-san-sang.fxml"));
            Parent nutGoc = boNapFXML.load();
            ((Label) boNapFXML.getNamespace().get("lblTrangTieuDe")).setText(tieuDe);
            ((Label) boNapFXML.getNamespace().get("lblTrangThongBao")).setText(thongBao);
            return nutGoc;
        } catch (IOException loi) {
            throw new IllegalStateException("Thiếu placeholder chung", loi);
        }
    }

    private void xoaLuaChon() {
        for (Button nut : cacNut.values()) {
            nut.getStyleClass().remove("trinh-don-da-chon");
        }
    }

    private void anTrinhDonVaiTro() {
        for (VBox trinhDon : List.of(trinhDonQuanLy, trinhDonLeTan, trinhDonNhaSi, trinhDonYTa)) {
            trinhDon.setVisible(false);
            trinhDon.setManaged(false);
        }
    }

    /** Xóa phiên nhìn thấy trên UI; Controller cũ không được tiếp tục điều hướng. */
    public void giaiPhong() {
        daGiaiPhong = true;
        // Đóng cả form con khi logout; callback của phiên cũ cũng bị chặn theo phiên bản Session.
        if (khungUngDung.getScene() != null) {
            var cuaSoChinh = khungUngDung.getScene().getWindow();
            for (var cuaSo : List.copyOf(javafx.stage.Window.getWindows())) {
                if (cuaSo instanceof javafx.stage.Stage cuaSoCon && cuaSoCon.getOwner() == cuaSoChinh
                    && Boolean.TRUE.equals(cuaSoCon.getProperties().get("hopThoaiNghiepVu"))) cuaSoCon.close();
            }
        }
        anTrinhDonVaiTro();
        vungNoiDung.getChildren().clear();
        khungUngDung.setDisable(true);
        lblVaiTro.setText("");
        lblVaiTroDuongDan.setText("");
        lblTrangDuongDan.setText("");
        lblTenThanhBen.setText("");
        lblTrangThaiThanhBen.setText("");
        lblChuCaiDauHoTen.setText("");
        lblNgayHienTai.setText("");
        mnuTaiKhoan.hide();
        mnuTaiKhoan.setText("");
        dieuHuongUngDung = null;
        phienDangNhap = null;
    }
}
