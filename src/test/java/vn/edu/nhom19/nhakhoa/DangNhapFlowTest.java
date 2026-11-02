package vn.edu.nhom19.nhakhoa;

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
import vn.edu.nhom19.nhakhoa.app.*;
import vn.edu.nhom19.nhakhoa.auth.*;
import vn.edu.nhom19.nhakhoa.dao.TaiKhoanDAO;
import vn.edu.nhom19.nhakhoa.entity.*;
import vn.edu.nhom19.nhakhoa.service.*;

/** UI/Service thật với DAO giả; không được tính là kiểm thử SQL. */
class DangNhapFlowTest {
    private Stage cuaSo;private AppNavigator dieuHuong;private UserSession phien;private KhoGiaLap kho;
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
    private static <T>T fx(Callable<T> viec)throws Exception{FutureTask<T> tacVu=new FutureTask<>(viec);Platform.runLater(tacVu);return tacVu.get(10,TimeUnit.SECONDS);}
    @BeforeAll static void toolkit()throws Exception{
        CountDownLatch sanSang=new CountDownLatch(1);try{Platform.startup(sanSang::countDown);}catch(IllegalStateException loi){sanSang.countDown();}
        assertTrue(sanSang.await(10,TimeUnit.SECONDS));fx(()->{Platform.setImplicitExit(false);return null;});
        matKhau=UUID.randomUUID().toString();bam=BaoMatMatKhau.bamMatKhau(matKhau);
    }
    @BeforeEach void mo()throws Exception{fx(()->{
        cuaSo=new Stage();phien=new UserSession();kho=new KhoGiaLap();
        dieuHuong=new AppNavigator(cuaSo,phien,new DangNhapService(kho));dieuHuong.start();return null;
    });}
    @AfterEach void dong()throws Exception{fx(()->{if(kho.choPhep!=null)kho.choPhep.countDown();dieuHuong.close();cuaSo.close();return null;});}
    private <T extends Node>T node(String id,Class<T> kieu){
        Parent root=cuaSo.getScene().getRoot();root.applyCss();root.layout();
        return kieu.cast(Objects.requireNonNull(root.lookup("#"+id),id));
    }
    private void nhap(String matKhauNhap){node("txtTenDangNhap",TextField.class).setText("nhanvien");node("txtMatKhau",PasswordField.class).setText(matKhauNhap);}
    private void ganTaiKhoan(String chucVu){kho.taiKhoan=new TaiKhoan("TK","nhanvien",bam,true,new NhanVien("NV_SQL","Nhân viên từ SQL",chucVu,true));}
    private void doiDen(Callable<Boolean> dieuKien)throws Exception{
        for(int i=0;i<400;i++){if(fx(dieuKien))return;Thread.sleep(20);}fail("Không nhận được trạng thái UI mong đợi.");
    }
    @Test void tatCaFXMLVanNapDuocSauDoiTenController()throws Exception{fx(()->{
        int soLuong=0;
        try(var duongDan=java.nio.file.Files.walk(java.nio.file.Path.of("src/main/resources/fxml"))){
            for(var tep:duongDan.filter(p->p.toString().endsWith(".fxml")).toList()){
                Parent root=new javafx.fxml.FXMLLoader(tep.toUri().toURL()).load();new Scene(root);root.applyCss();root.layout();soLuong++;
            }
        }
        assertEquals(87,soLuong);return null;
    });}
    @Test void chiCoFormTaiKhoanVaBaoThieuDuLieu()throws Exception{fx(()->{
        assertNull(cuaSo.getScene().getRoot().lookup("#vungDemo"));
        for(String id:new String[]{"btnDemoManager","btnDemoReceptionist","btnDemoDentist","btnDemoNurse"})
            assertNull(cuaSo.getScene().getRoot().lookup("#"+id));
        assertFalse(phien.isActive());
        node("btnDangNhap",Button.class).fire();assertTrue(node("lblLoiDangNhap",Label.class).getText().contains("Vui lòng nhập"));
        assertEquals(0,kho.soLan);return null;
    });}
    @Test void khongTheMoDashboardTrucTiepKhiChuaXacThuc()throws Exception{fx(()->{
        assertThrows(IllegalStateException.class, () -> dieuHuong.moSauDangNhap());
        assertFalse(phien.isActive());
        assertEquals("loginRoot", cuaSo.getScene().getRoot().getId());
        assertEquals(0, kho.soLan);
        return null;
    });}
    @Test void bonRoleQuaServiceEnterVaLogoutXoaPhienCu()throws Exception{
        String[] sql={"NHAN_VIEN_QUAN_LY","LE_TAN","NHA_SI","Y_TA"};String[] menus={"managerMenu","receptionistMenu","dentistMenu","nurseMenu"};
        Role[] roles={Role.MANAGER,Role.RECEPTIONIST,Role.DENTIST,Role.NURSE};
        for(int i=0;i<roles.length;i++){
            final int viTri=i;Button nutCu=fx(()->{ganTaiKhoan(sql[viTri]);nhap(matKhau);Button nut=node("btnDangNhap",Button.class);node("txtMatKhau",PasswordField.class).fireEvent(new ActionEvent());return nut;});
            doiDen(()->"appShell".equals(cuaSo.getScene().getRoot().getId()));
            fx(()->{
                assertEquals(roles[viTri],phien.getRole());assertEquals("NV_SQL",phien.layMaNhanVien());
                assertThrows(IllegalStateException.class, () -> dieuHuong.moSauDangNhap());
                assertEquals(roles[viTri], phien.getRole());
                assertEquals("Nhân viên từ SQL",node("lblSidebarName",Label.class).getText());
                assertTrue(node("lblSidebarStatus",Label.class).getText().contains("Đã đăng nhập"));
                for(int j=0;j<menus.length;j++)assertEquals(j==viTri,node(menus[j],javafx.scene.layout.VBox.class).isVisible());
                if(roles[viTri]==Role.MANAGER){
                    assertEquals("—",node("lblPatientsToday",Label.class).getText());
                    assertTrue(node("tblRecentAppointments",TableView.class).getItems().isEmpty());
                    Scene scene = cuaSo.getScene();
                    Parent shell = scene.getRoot();
                    node("btnStaff",Button.class).fire();
                    assertSame(scene, cuaSo.getScene());
                    assertSame(shell, scene.getRoot());
                    assertEquals("Nhân sự",node("lblBreadcrumbPage",Label.class).getText());
                }
                Button traiQuyen=node(roles[viTri]==Role.MANAGER?"btnExamination":"btnStaff",Button.class);
                assertThrows(IllegalArgumentException.class,()->traiQuyen.getOnAction().handle(new ActionEvent()));
                MenuButton taiKhoan=node("mnuAccount",MenuButton.class);
                taiKhoan.getItems().stream().filter(item->"miLogout".equals(item.getId())).findFirst().orElseThrow().fire();
                assertFalse(phien.isActive());assertNull(phien.layMaNhanVien());assertNull(phien.layHoTen());
                assertTrue(nutCu.isDisabled());nutCu.fireEvent(new ActionEvent());assertFalse(phien.isActive());return null;
            });
        }
    }
    @Test void truyVanKhongKhoaFXVaKhongGuiHaiLan()throws Exception{
        fx(()->{ganTaiKhoan("NHA_SI");kho.choPhep=new CountDownLatch(1);nhap(matKhau);node("btnDangNhap",Button.class).fire();return null;});
        assertTrue(kho.daVao.await(5,TimeUnit.SECONDS));
        fx(()->{assertTrue(node("btnDangNhap",Button.class).isDisabled());assertTrue(node("vungDangXuLy",javafx.scene.layout.HBox.class).isVisible());
            node("btnDangNhap",Button.class).fireEvent(new ActionEvent());assertEquals(1,kho.soLan);return null;});
        kho.choPhep.countDown();doiDen(()->phien.isActive());
    }
    @Test void loiKetNoiKhongTaoPhienHayDuongTat()throws Exception{
        fx(()->{kho.loiSQL=true;nhap(matKhau);node("btnDangNhap",Button.class).fire();return null;});
        doiDen(()->node("lblLoiDangNhap",Label.class).isVisible());
        fx(()->{assertFalse(phien.isActive());String loi=node("lblLoiDangNhap",Label.class).getText();assertTrue(loi.contains("SQL Server"));
            assertFalse(loi.contains("nội bộ"));assertNull(cuaSo.getScene().getRoot().lookup("#btnDemoManager"));assertFalse(node("btnDangNhap",Button.class).isDisabled());return null;});
    }
    @Test void saiMatKhauHienLoiVaXoaNoiDungMatKhau()throws Exception{
        fx(()->{ganTaiKhoan("NHA_SI");nhap(UUID.randomUUID().toString());node("btnDangNhap",Button.class).fire();return null;});
        doiDen(()->node("lblLoiDangNhap",Label.class).isVisible());
        fx(()->{assertFalse(phien.isActive());assertEquals(DangNhapService.SAI_THONG_TIN,node("lblLoiDangNhap",Label.class).getText());assertEquals("",node("txtMatKhau",PasswordField.class).getText());return null;});
    }
    @Test void callbackCuSauLogoutKhongMoLaiPhien()throws Exception{
        fx(()->{ganTaiKhoan("NHA_SI");kho.choPhep=new CountDownLatch(1);nhap(matKhau);node("btnDangNhap",Button.class).fire();return null;});
        assertTrue(kho.daVao.await(5,TimeUnit.SECONDS));
        fx(()->{dieuHuong.logout();kho.choPhep.countDown();return null;});
        fx(()->{assertFalse(phien.isActive());assertEquals("loginRoot",cuaSo.getScene().getRoot().getId());assertEquals("",node("txtTenDangNhap",TextField.class).getText());return null;});
    }
}
