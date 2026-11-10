package iuh.se.nhom19;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.*;
import iuh.se.nhom19.auth.*;
import iuh.se.nhom19.dao.PhieuKhamDAO;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.entity.*;
import iuh.se.nhom19.service.DanhSachChoKhamService;
import iuh.se.nhom19.view.dentist.DanhSachChoKhamController;
import iuh.se.nhom19.view.receptionist.TiepNhanController;

/** Kiểm thử UI với Service/Connection kiểm thử; không được tính là SQL thật. */
class GiaoDienTiepNhanTest {
    private PhienDangNhap phien;
    private Parent giaoDien;
    private CountDownLatch choPhep = new CountDownLatch(1), daVao = new CountDownLatch(1);
    private int soLanDocChiTiet;
    private boolean baoLoiSQL;
    private final KetNoiCSDL camSQL = new KetNoiCSDL() {
        @Override public Connection layKetNoi() throws SQLException { throw new SQLException("SQL không được gọi trong test UI."); }
    };
    @BeforeAll static void khoiTaoFX() throws Exception {
        CountDownLatch sanSang = new CountDownLatch(1);
        try { Platform.startup(sanSang::countDown); } catch (IllegalStateException loi) { sanSang.countDown(); }
        assertTrue(sanSang.await(10, TimeUnit.SECONDS)); trenFX(() -> { Platform.setImplicitExit(false); return null; });
    }
    @AfterEach void dong() throws Exception { choPhep.countDown(); trenFX(() -> { if (phien != null) phien.xoa(); return null; }); }
    private static <T> T trenFX(Callable<T> viec) throws Exception {
        FutureTask<T> tacVu = new FutureTask<>(viec); Platform.runLater(tacVu); return tacVu.get(10, TimeUnit.SECONDS);
    }
    private <T extends Node> T tim(String ma, Class<T> kieu) { return kieu.cast(Objects.requireNonNull(giaoDien.lookup("#" + ma), ma)); }
    private void doiDen(Callable<Boolean> dieuKien) throws Exception {
        for (int lan = 0; lan < 300; lan++) { if (trenFX(dieuKien)) return; Thread.sleep(20); }
        fail("Không nhận được kết quả UI mong đợi.");
    }
    private PhienDangNhap taoPhien(VaiTro vaiTro) {
        PhienDangNhap ketQua = new PhienDangNhap(); ketQua.batDauPhienDangNhap(new KetQuaDangNhap("NV_FIXTURE", "Người kiểm thử", vaiTro)); return ketQua;
    }
    private void moHangCho() throws Exception {
        trenFX(() -> {
            phien = taoPhien(VaiTro.DENTIST);
            FXMLLoader boNap = new FXMLLoader(getClass().getResource("/fxml/dentist/kham-benh.fxml")); giaoDien = boNap.load(); new Scene(giaoDien);
            DanhSachChoKhamService dichVu = new DanhSachChoKhamService(phien, camSQL, new PhieuKhamDAO()) {
                @Override public List<PhieuKham> layDanhSachChoKham() throws SQLException {
                    daVao.countDown();
                    try { if (!choPhep.await(10, TimeUnit.SECONDS)) throw new SQLException("Quá thời gian kiểm thử."); }
                    catch (InterruptedException loi) { Thread.currentThread().interrupt(); throw new SQLException("Bị ngắt."); }
                    if (baoLoiSQL) throw new SQLException("password=KHONG_DUOC_HIEN_THI");
                    return List.of(phieuThu());
                }
                @Override public PhieuKham timPhieuKhamTheoMa(String ma) { soLanDocChiTiet++; return phieuThu(); }
            };
            ((DanhSachChoKhamController) boNap.getController()).cauHinh(phien, dichVu); return null;
        });
        assertTrue(daVao.await(5, TimeUnit.SECONDS));
    }
    private PhieuKham phieuThu() {
        BenhNhan benhNhan = new BenhNhan(); benhNhan.datMaBenhNhan("BN_FIXTURE"); benhNhan.datHoTen("Bệnh nhân kiểm thử UI"); benhNhan.datSoDienThoai("0900000000");
        PhieuKham phieu = new PhieuKham(); phieu.datMaPhieuKham("PK_FIXTURE"); phieu.datBenhNhan(benhNhan);
        phieu.datThoiDiemTiepNhan(LocalDateTime.of(2026, 11, 3, 9, 0)); phieu.datMaPhong("P_FIXTURE"); phieu.datLyDo("Kiểm thử UI"); phieu.datTrangThaiKham("CHO_KHAM"); return phieu;
    }
    @Test void fxmlTiepNhanCoDuControllerConVaNutKhongHen() throws Exception {
        trenFX(() -> {
            phien = taoPhien(VaiTro.RECEPTIONIST);
            FXMLLoader boNap = new FXMLLoader(getClass().getResource("/fxml/receptionist/tiep-nhan-lich-hen.fxml")); Parent goc = boNap.load(); new Scene(goc);goc.applyCss();goc.layout();
            TiepNhanController dieuKhien = boNap.getController(); dieuKhien.cauHinh(phien, camSQL);
            for (String ma : List.of("tabHomNayNoiDungController", "tabTuanNoiDungController")) assertNotNull(boNap.getNamespace().get(ma));
            assertEquals(2,((TabPane)goc.lookup("#tiepNhanCacTab")).getTabs().size());
            assertFalse(((Button)goc.lookup("#btnMoTiepNhanKhongHen")).isDisable());
            assertNull(boNap.getNamespace().get("tabKhongHenTrong"));
            // Phương án A mở modal; form con chỉ nhập bản nháp, không có nút lưu bệnh nhân riêng.
            FXMLLoader modal=new FXMLLoader(getClass().getResource("/fxml/receptionist/tiep-nhan-khong-hen.fxml"));
            Parent gocModal=modal.load(); new Scene(gocModal);gocModal.applyCss();gocModal.layout();
            ((iuh.se.nhom19.view.receptionist.KhongHenController)modal.getController()).cauHinh(phien,camSQL,phieu->fail("Test UI không được ghi SQL."));
            assertNotNull(gocModal.lookup("#lstChonBenhNhan"));
            assertNull(gocModal.lookup("#btnLuuBenhNhan"));
            assertTrue(((Button)gocModal.lookup("#btnTiepNhanKhongHen")).isDisable()); return null;
        });
    }
    @Test void sqlNenKhongKhoaFXVaNhaSiChiDuocBatDauPhieuChoKham() throws Exception {
        moHangCho(); trenFX(() -> { assertTrue(giaoDien.isDisabled()); return null; });
        choPhep.countDown(); doiDen(() -> !giaoDien.isDisabled());
        trenFX(() -> { assertEquals(1, tim("tblChoKham", TableView.class).getItems().size()); tim("tblChoKham", TableView.class).getSelectionModel().select(0); return null; });
        doiDen(() -> tim("chiTietPhieuKham", VBox.class).isVisible());
        trenFX(() -> {
            assertEquals("Chờ khám", tim("lblTrangThai", Label.class).getText()); assertEquals(1, soLanDocChiTiet);
            assertFalse(tim("btnBatDauKham",Button.class).isDisable());
            assertTrue(tim("txtTinhTrangLamSan",TextArea.class).isDisable());
            assertTrue(tim("txtChanDoan",TextArea.class).isDisable());
            assertNull(giaoDien.lookup("#btnGoiBenhNhan"));
            return null;
        });
    }
    @Test void ketQuaCuKhongHienSauLogout() throws Exception {
        moHangCho(); trenFX(() -> { phien.xoa(); return null; }); choPhep.countDown(); doiDen(() -> !giaoDien.isDisabled());
        trenFX(() -> { assertTrue(tim("tblChoKham", TableView.class).getItems().isEmpty()); return null; });
    }
    @Test void loiSQLKhongBaoThanhCongHoacLoChiTietKetNoi() throws Exception {
        baoLoiSQL = true; moHangCho(); choPhep.countDown(); doiDen(() -> !giaoDien.isDisabled());
        trenFX(() -> {
            assertTrue(tim("lblThongBao", Label.class).getText().contains("SQL Server"));
            assertFalse(tim("lblThongBao", Label.class).getText().contains("KHONG_DUOC_HIEN_THI"));
            assertEquals("Chưa tải dữ liệu", tim("lblSoLuongChoKham", Label.class).getText());
            assertTrue(tim("tblChoKham", TableView.class).getItems().isEmpty()); return null;
        });
    }

    @Test void datLichNguoiMoiKhongCoTruongHoSoChinhThucHoacNutTaoHoSo() throws Exception {
        trenFX(()->{
            FXMLLoader boNap=new FXMLLoader(getClass().getResource("/fxml/receptionist/them-lich-hen.fxml")); giaoDien=boNap.load(); new Scene(giaoDien);giaoDien.applyCss();giaoDien.layout();
            assertNotNull(tim("txtHoTenNguoiHen",TextField.class));assertNotNull(tim("txtSoDienThoaiLienHe",TextField.class));
            assertNull(giaoDien.lookup("#radNguoiMoi"));assertNull(giaoDien.lookup("#cboLichHenGio"));assertNull(giaoDien.lookup("#luoiKhungGio"));assertNotNull(giaoDien.lookup("#txtGioHen"));assertNotNull(giaoDien.lookup("#lblKhungGioDuKien"));
            assertNull(giaoDien.lookup("#dpBenhNhanSinhNgay")); assertNull(giaoDien.lookup("#cboBenhNhanGioiTinh"));
            assertNull(giaoDien.lookup("#btnThemBenhNhanLichHen")); assertNull(giaoDien.lookup("#btnLuuBenhNhan")); return null;
        });
    }
    @Test void gioNhapTayDuocTinh45PhutVaTuChoiSaiDinhDangHoacNgoaiCa() throws Exception {
        trenFX(()->{
            FXMLLoader boNap=new FXMLLoader(getClass().getResource("/fxml/receptionist/them-lich-hen.fxml"));giaoDien=boNap.load();new Scene(giaoDien);giaoDien.applyCss();giaoDien.layout();
            PhanCong ca=new PhanCong();ca.datMaPhanCong("PC_UI");ca.datMaNhanVien("NV_UI");ca.datNgayLam(java.time.LocalDate.of(2030,1,7));ca.datGioBatDau(java.time.LocalTime.of(8,0));ca.datGioKetThuc(java.time.LocalTime.of(12,0));
            tim("cboCaLam",ComboBox.class).setValue(ca);
            tim("txtGioHen",TextField.class).setText("08:30");
            assertEquals("08:30 – 09:15 · 45 phút",tim("lblKhungGioDuKien",Label.class).getText());
            assertFalse(tim("btnLuuLichHen",Button.class).isDisable());
            tim("txtGioHen",TextField.class).setText("25:30");
            assertTrue(tim("btnLuuLichHen",Button.class).isDisable());
            assertTrue(tim("lblLoiLich",Label.class).getText().contains("HH:mm"));
            tim("txtGioHen",TextField.class).setText("11:30");
            assertTrue(tim("btnLuuLichHen",Button.class).isDisable());
            assertTrue(tim("lblLoiLich",Label.class).getText().contains("nằm hoàn toàn trong ca"));return null;
        });
    }
}



