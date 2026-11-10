package iuh.se.nhom19;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import iuh.se.nhom19.app.*;
import iuh.se.nhom19.auth.*;
import iuh.se.nhom19.dao.TaiKhoanDAO;
import iuh.se.nhom19.entity.*;
import iuh.se.nhom19.service.*;

/** UI/Service thật với DAO giả; không được tính là kiểm thử SQL. */
class LuongDangNhapTest {
    private Stage cuaSo;private DieuHuongUngDung dieuHuong;private PhienDangNhap phien;private KhoGiaLap kho;
    private static String matKhau,bam;
    static class KhoGiaLap extends TaiKhoanDAO {
        volatile int soLan;TaiKhoan taiKhoan;boolean loiSQL;
        CountDownLatch daVao=new CountDownLatch(1),choPhep;
        @Override public TaiKhoan timTheoTenDangNhap(String ten)throws SQLException {
            soLan++;daVao.countDown();
            if(choPhep!=null)try{if(!choPhep.await(10,TimeUnit.SECONDS))throw new SQLException("timeout");}
                catch(InterruptedException loi){Thread.currentThread().interrupt();throw new SQLException("cancel");}
            if(loiSQL)throw new SQLException("Chi tiết nội bộ SQL");return taiKhoan;
        }
    }
    private static <T>T trenLuongGiaoDien(Callable<T> viec)throws Exception{FutureTask<T> tacVu=new FutureTask<>(viec);Platform.runLater(tacVu);return tacVu.get(10,TimeUnit.SECONDS);}
    @BeforeAll static void khoiTaoJavaFX()throws Exception{
        CountDownLatch sanSang=new CountDownLatch(1);try{Platform.startup(sanSang::countDown);}catch(IllegalStateException loi){sanSang.countDown();}
        assertTrue(sanSang.await(10,TimeUnit.SECONDS));trenLuongGiaoDien(()->{Platform.setImplicitExit(false);return null;});
        matKhau=UUID.randomUUID().toString();bam=BaoMatMatKhau.bamMatKhau(matKhau);
    }
    @BeforeEach void mo()throws Exception{trenLuongGiaoDien(()->{
        cuaSo=new Stage();phien=new PhienDangNhap();kho=new KhoGiaLap();
        dieuHuong=new DieuHuongUngDung(cuaSo,phien,new DangNhapService(kho),new iuh.se.nhom19.database.KetNoiCSDL(){
            @Override public java.sql.Connection layKetNoi()throws java.sql.SQLException{throw new java.sql.SQLException("SQL không được gọi trong test Login.");}
        });dieuHuong.khoiDong();return null;
    });}
    @AfterEach void dong()throws Exception{trenLuongGiaoDien(()->{if(kho.choPhep!=null)kho.choPhep.countDown();dieuHuong.close();cuaSo.close();return null;});}
    private <T extends Node>T timThanhPhan(String ma,Class<T> kieu){
        Parent nutGoc=cuaSo.getScene().getRoot();nutGoc.applyCss();nutGoc.layout();
        return kieu.cast(Objects.requireNonNull(nutGoc.lookup("#"+ma),ma));
    }
    private void nhap(String matKhauNhap){timThanhPhan("txtTenDangNhap",TextField.class).setText("nhanvien");timThanhPhan("txtMatKhau",PasswordField.class).setText(matKhauNhap);}
    private void ganTaiKhoan(String chucVu){kho.taiKhoan=new TaiKhoan("TK","nhanvien",bam,true,new NhanVien("NV_SQL","Nhân viên từ SQL",chucVu,true));}
    private void doiDen(Callable<Boolean> dieuKien)throws Exception{
        for(int i=0;i<400;i++){if(trenLuongGiaoDien(dieuKien))return;Thread.sleep(20);}fail("Không nhận được trạng thái UI mong đợi.");
    }
    @Test void tatCaFXMLVanNapDuocSauDoiTenController()throws Exception{trenLuongGiaoDien(()->{
        int soLuong=0;
        try(var duongDan=java.nio.file.Files.walk(java.nio.file.Path.of("src/main/resources/fxml"))){
            for(var tep:duongDan.filter(p->p.toString().endsWith(".fxml")).toList()){
                Parent nutGoc=new javafx.fxml.FXMLLoader(tep.toUri().toURL()).load();new Scene(nutGoc);nutGoc.applyCss();nutGoc.layout();soLuong++;
            }
        }
        assertEquals(89,soLuong);return null;
    });}
    @Test void chiCoFormTaiKhoanVaBaoThieuDuLieu()throws Exception{trenLuongGiaoDien(()->{
        assertNull(cuaSo.getScene().getRoot().lookup("#vungDemo"));
        for(String ma:new String[]{"btnDemoManager","btnDemoReceptionist","btnDemoDentist","btnDemoNurse"})
            assertNull(cuaSo.getScene().getRoot().lookup("#"+ma));
        assertFalse(phien.dangHoatDong());
        timThanhPhan("btnDangNhap",Button.class).fire();assertTrue(timThanhPhan("lblLoiDangNhap",Label.class).getText().contains("Vui lòng nhập"));
        assertEquals(0,kho.soLan);return null;
    });}
    @Test void khongTheMoTongQuanTrucTiepKhiChuaXacThuc()throws Exception{trenLuongGiaoDien(()->{
        assertThrows(IllegalStateException.class, () -> dieuHuong.moSauDangNhap());
        assertFalse(phien.dangHoatDong());
        assertEquals("gocDangNhap", cuaSo.getScene().getRoot().getId());
        assertEquals(0, kho.soLan);
        return null;
    });}
    @Test void bonVaiTroDangNhapVaDangXuatXoaPhienCu()throws Exception{
        String[] cauLenhSQL={"NHAN_VIEN_QUAN_LY","LE_TAN","NHA_SI","Y_TA"};String[] cacTrinhDon={"trinhDonQuanLy","trinhDonLeTan","trinhDonNhaSi","trinhDonYTa"};
        VaiTro[] cacVaiTro={VaiTro.MANAGER,VaiTro.RECEPTIONIST,VaiTro.DENTIST,VaiTro.NURSE};
        for(int i=0;i<cacVaiTro.length;i++){
            final int viTri=i;Button nutCu=trenLuongGiaoDien(()->{ganTaiKhoan(cauLenhSQL[viTri]);nhap(matKhau);Button nut=timThanhPhan("btnDangNhap",Button.class);timThanhPhan("txtMatKhau",PasswordField.class).fireEvent(new ActionEvent());return nut;});
            doiDen(()->"khungUngDung".equals(cuaSo.getScene().getRoot().getId()));
            trenLuongGiaoDien(()->{
                assertEquals(cacVaiTro[viTri],phien.layVaiTro());assertEquals("NV_SQL",phien.layMaNhanVien());
                assertThrows(IllegalStateException.class, () -> dieuHuong.moSauDangNhap());
                assertEquals(cacVaiTro[viTri], phien.layVaiTro());
                assertEquals("Nhân viên từ SQL",timThanhPhan("lblTenThanhBen",Label.class).getText());
                assertTrue(timThanhPhan("lblTrangThaiThanhBen",Label.class).getText().contains("Đã đăng nhập"));
                for(int j=0;j<cacTrinhDon.length;j++)assertEquals(j==viTri,timThanhPhan(cacTrinhDon[j],javafx.scene.layout.VBox.class).isVisible());
                Scene sceneTruocThuGon=cuaSo.getScene();Parent shellTruocThuGon=sceneTruocThuGon.getRoot();
                String tieuDeTruocThuGon=timThanhPhan("lblTrangDuongDan",Label.class).getText();
                Button btnSidebar=timThanhPhan("btnThuGonThanhBen",Button.class);
                btnSidebar.fire();
                assertSame(sceneTruocThuGon,cuaSo.getScene());assertSame(shellTruocThuGon,sceneTruocThuGon.getRoot());
                assertEquals(64.0,timThanhPhan("thanhBen",javafx.scene.layout.VBox.class).getPrefWidth());
                assertEquals("",timThanhPhan("btnTongQuan",Button.class).getText());
                assertNotNull(timThanhPhan("btnTongQuan",Button.class).getTooltip());
                assertEquals(tieuDeTruocThuGon,timThanhPhan("lblTrangDuongDan",Label.class).getText());
                btnSidebar.fire();
                assertSame(shellTruocThuGon,sceneTruocThuGon.getRoot());assertEquals("Dashboard",timThanhPhan("btnTongQuan",Button.class).getText());
                if(cacVaiTro[viTri]==VaiTro.MANAGER){
                    assertEquals("—",timThanhPhan("lblDanhSachBenhNhanHomNay",Label.class).getText());
                    assertTrue(timThanhPhan("tblGanNhatDanhSachLichHen",TableView.class).getItems().isEmpty());
                    Scene khungCanh = cuaSo.getScene();
                    Parent khungUngDung = khungCanh.getRoot();
                    timThanhPhan("btnNhanSu",Button.class).fire();
                    assertSame(khungCanh, cuaSo.getScene());
                    assertSame(khungUngDung, khungCanh.getRoot());
                    assertEquals("Nhân sự",timThanhPhan("lblTrangDuongDan",Label.class).getText());
                }
                Button traiQuyen=timThanhPhan(cacVaiTro[viTri]==VaiTro.MANAGER?"btnKhamBenh":"btnNhanSu",Button.class);
                assertThrows(IllegalArgumentException.class,()->traiQuyen.getOnAction().handle(new ActionEvent()));
                MenuButton taiKhoan=timThanhPhan("mnuTaiKhoan",MenuButton.class);
                taiKhoan.getItems().stream().filter(muc->"miDangXuat".equals(muc.getId())).findFirst().orElseThrow().fire();
                assertFalse(phien.dangHoatDong());assertNull(phien.layMaNhanVien());assertNull(phien.layHoTen());
                assertTrue(nutCu.isDisabled());nutCu.fireEvent(new ActionEvent());assertFalse(phien.dangHoatDong());return null;
            });
        }
    }
    @Test void truyVanKhongKhoaFXVaKhongGuiHaiLan()throws Exception{
        trenLuongGiaoDien(()->{ganTaiKhoan("NHA_SI");kho.choPhep=new CountDownLatch(1);nhap(matKhau);timThanhPhan("btnDangNhap",Button.class).fire();return null;});
        assertTrue(kho.daVao.await(5,TimeUnit.SECONDS));
        trenLuongGiaoDien(()->{assertTrue(timThanhPhan("btnDangNhap",Button.class).isDisabled());assertTrue(timThanhPhan("vungDangXuLy",javafx.scene.layout.HBox.class).isVisible());
            timThanhPhan("btnDangNhap",Button.class).fireEvent(new ActionEvent());assertEquals(1,kho.soLan);return null;});
        kho.choPhep.countDown();doiDen(()->phien.dangHoatDong());
    }
    @Test void loiKetNoiKhongTaoPhienHayDuongTat()throws Exception{
        trenLuongGiaoDien(()->{kho.loiSQL=true;nhap(matKhau);timThanhPhan("btnDangNhap",Button.class).fire();return null;});
        doiDen(()->timThanhPhan("lblLoiDangNhap",Label.class).isVisible());
        trenLuongGiaoDien(()->{assertFalse(phien.dangHoatDong());String loi=timThanhPhan("lblLoiDangNhap",Label.class).getText();assertTrue(loi.contains("SQL Server"));
            assertFalse(loi.contains("nội bộ"));assertNull(cuaSo.getScene().getRoot().lookup("#btnDemoManager"));assertFalse(timThanhPhan("btnDangNhap",Button.class).isDisabled());return null;});
    }
    @Test void saiMatKhauHienLoiVaXoaNoiDungMatKhau()throws Exception{
        trenLuongGiaoDien(()->{ganTaiKhoan("NHA_SI");nhap(UUID.randomUUID().toString());timThanhPhan("btnDangNhap",Button.class).fire();return null;});
        doiDen(()->timThanhPhan("lblLoiDangNhap",Label.class).isVisible());
        trenLuongGiaoDien(()->{assertFalse(phien.dangHoatDong());assertEquals(DangNhapService.SAI_THONG_TIN,timThanhPhan("lblLoiDangNhap",Label.class).getText());assertEquals("",timThanhPhan("txtMatKhau",PasswordField.class).getText());return null;});
    }
    @Test void ketQuaCuSauDangXuatKhongMoLaiPhien()throws Exception{
        trenLuongGiaoDien(()->{ganTaiKhoan("NHA_SI");kho.choPhep=new CountDownLatch(1);nhap(matKhau);timThanhPhan("btnDangNhap",Button.class).fire();return null;});
        assertTrue(kho.daVao.await(5,TimeUnit.SECONDS));
        trenLuongGiaoDien(()->{dieuHuong.dangXuat();kho.choPhep.countDown();return null;});
        trenLuongGiaoDien(()->{assertFalse(phien.dangHoatDong());assertEquals("gocDangNhap",cuaSo.getScene().getRoot().getId());assertEquals("",timThanhPhan("txtTenDangNhap",TextField.class).getText());return null;});
    }
}
