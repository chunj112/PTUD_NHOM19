package iuh.se.nhom19;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import iuh.se.nhom19.auth.*;
import iuh.se.nhom19.app.DieuHuongUngDung;

/** Session dùng kết quả xác thực mô phỏng trong UNIT, không phải một chế độ chạy ứng dụng. */
class PhienDangNhapTest {
    @Test void chiBatDauBangKetQuaCoDanhTinhHopLe(){
        PhienDangNhap phien=new PhienDangNhap();assertFalse(phien.dangHoatDong());assertNull(phien.layMaNhanVien());
        assertThrows(IllegalArgumentException.class,()->phien.batDauPhienDangNhap(null));
        for(VaiTro vaiTro:VaiTro.values()){
            phien.batDauPhienDangNhap(new KetQuaDangNhap("NV_UNIT","Nhân viên unit",vaiTro));
            assertEquals(vaiTro,phien.yeuCauVaiTro(phien.layPhienBan()));assertEquals("NV_UNIT",phien.layMaNhanVien());phien.xoa();
        }
        assertThrows(IllegalArgumentException.class,()->new KetQuaDangNhap("","Tên",VaiTro.MANAGER));
    }
    @Test void dangXuatVoHieuDanhTinhVaPhienCu(){
        PhienDangNhap phien=new PhienDangNhap();phien.batDauPhienDangNhap(new KetQuaDangNhap("NV1","Người một",VaiTro.MANAGER));long phienBan=phien.layPhienBan();
        phien.xoa();assertFalse(phien.dangHoatDong());assertNull(phien.layVaiTro());assertNull(phien.layHoTen());assertNull(phien.layMaNhanVien());
        assertThrows(IllegalStateException.class,()->phien.yeuCauVaiTro(phienBan));
        phien.batDauPhienDangNhap(new KetQuaDangNhap("NV2","Người hai",VaiTro.NURSE));
        assertThrows(IllegalStateException.class,()->phien.yeuCauVaiTro(phienBan));assertEquals(VaiTro.NURSE,phien.yeuCauVaiTro(phien.layPhienBan()));
    }
    @Test void khongConAPIChonVaiTroDangNhap(){
        assertThrows(NoSuchMethodException.class,()->PhienDangNhap.class.getMethod("startDemo",VaiTro.class));
        assertThrows(NoSuchMethodException.class,()->DieuHuongUngDung.class.getMethod("openDemoRole",VaiTro.class));
        assertThrows(ClassNotFoundException.class,()->Class.forName("iuh.se.nhom19.auth.CheDoDangNhap"));
    }
}
