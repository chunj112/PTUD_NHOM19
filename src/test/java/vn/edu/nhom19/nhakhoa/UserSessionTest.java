package vn.edu.nhom19.nhakhoa;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import vn.edu.nhom19.nhakhoa.auth.*;
import vn.edu.nhom19.nhakhoa.app.AppNavigator;

/** Session dùng kết quả xác thực mô phỏng trong UNIT, không phải một chế độ chạy ứng dụng. */
class UserSessionTest {
    @Test void chiBatDauBangKetQuaCoDanhTinhHopLe(){
        UserSession phien=new UserSession();assertFalse(phien.isActive());assertNull(phien.layMaNhanVien());
        assertThrows(IllegalArgumentException.class,()->phien.batDauPhienDangNhap(null));
        for(Role role:Role.values()){
            phien.batDauPhienDangNhap(new KetQuaDangNhap("NV_UNIT","Nhân viên unit",role));
            assertEquals(role,phien.requireRole(phien.version()));assertEquals("NV_UNIT",phien.layMaNhanVien());phien.clear();
        }
        assertThrows(IllegalArgumentException.class,()->new KetQuaDangNhap("","Tên",Role.MANAGER));
    }
    @Test void logoutVoHieuDanhTinhVaPhienCu(){
        UserSession phien=new UserSession();phien.batDauPhienDangNhap(new KetQuaDangNhap("NV1","Người một",Role.MANAGER));long version=phien.version();
        phien.clear();assertFalse(phien.isActive());assertNull(phien.getRole());assertNull(phien.layHoTen());assertNull(phien.layMaNhanVien());
        assertThrows(IllegalStateException.class,()->phien.requireRole(version));
        phien.batDauPhienDangNhap(new KetQuaDangNhap("NV2","Người hai",Role.NURSE));
        assertThrows(IllegalStateException.class,()->phien.requireRole(version));assertEquals(Role.NURSE,phien.requireRole(phien.version()));
    }
    @Test void khongConAPIChonRoleDangNhap(){
        assertThrows(NoSuchMethodException.class,()->UserSession.class.getMethod("startDemo",Role.class));
        assertThrows(NoSuchMethodException.class,()->AppNavigator.class.getMethod("openDemoRole",Role.class));
        assertThrows(ClassNotFoundException.class,()->Class.forName("vn.edu.nhom19.nhakhoa.auth.CheDoDangNhap"));
    }
}
