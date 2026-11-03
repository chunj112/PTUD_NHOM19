package iuh.se.nhom19;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.util.UUID;
import org.junit.jupiter.api.*;
import iuh.se.nhom19.auth.*;
import iuh.se.nhom19.dao.TaiKhoanDAO;
import iuh.se.nhom19.entity.*;
import iuh.se.nhom19.service.*;

/** UNIT: DAO giả lập, không kết nối và không ghi dữ liệu SQL. */
class DangNhapServiceTest {
    static String matKhau,bam;
    @BeforeAll static void taoDuLieu(){matKhau=UUID.randomUUID().toString();bam=BaoMatMatKhau.bamMatKhau(matKhau);}
    static class KhoGiaLap extends TaiKhoanDAO {
        TaiKhoan taiKhoan;boolean loiSQL;String tenDaNhan;
        @Override public TaiKhoan timTheoTenDangNhap(String ten)throws SQLException{
            tenDaNhan=ten;if(loiSQL)throw new SQLException("Chi tiết nội bộ không được đưa lên UI.");return taiKhoan;
        }
    }
    private TaiKhoan taiKhoan(String chucVu,boolean khoaMo,boolean nhanVienMo){
        return new TaiKhoan("TK_UNIT","nhanvien",bam,khoaMo,new NhanVien("NV_UNIT","Nhân viên kiểm thử",chucVu,nhanVienMo));
    }
    private void dungVaiTro(String chucVu,VaiTro vaiTro){
        KhoGiaLap kho=new KhoGiaLap();kho.taiKhoan=taiKhoan(chucVu,true,true);
        KetQuaDangNhap ketQua=new DangNhapService(kho).dangNhap("  nhanvien  ",matKhau);
        assertEquals(vaiTro,ketQua.layVaiTro());assertEquals("NV_UNIT",ketQua.layMaNhanVien());
        assertEquals("Nhân viên kiểm thử",ketQua.layHoTen());assertEquals("nhanvien",kho.tenDaNhan);
    }
    @Test void TC001_quanLy(){dungVaiTro("NHAN_VIEN_QUAN_LY",VaiTro.MANAGER);}
    @Test void TC002_leTan(){dungVaiTro("LE_TAN",VaiTro.RECEPTIONIST);}
    @Test void TC003_nhaSi(){dungVaiTro("NHA_SI",VaiTro.DENTIST);}
    @Test void TC004_yTa(){dungVaiTro("Y_TA",VaiTro.NURSE);}
    @Test void TC005_saiMatKhau(){
        KhoGiaLap kho=new KhoGiaLap();kho.taiKhoan=taiKhoan("NHA_SI",true,true);
        assertEquals(DangNhapService.SAI_THONG_TIN,assertThrows(LoiDangNhap.class,()->new DangNhapService(kho).dangNhap("nhanvien",UUID.randomUUID().toString())).getMessage());
    }
    @Test void TC006_tenKhongTonTai(){
        assertEquals(DangNhapService.SAI_THONG_TIN,assertThrows(LoiDangNhap.class,()->new DangNhapService(new KhoGiaLap()).dangNhap("khong_ton_tai",matKhau)).getMessage());
    }
    @Test void TC007_taiKhoanNgungHoatDong(){
        KhoGiaLap kho=new KhoGiaLap();kho.taiKhoan=taiKhoan("NHA_SI",false,true);
        assertTrue(assertThrows(LoiDangNhap.class,()->new DangNhapService(kho).dangNhap("nhanvien",matKhau)).getMessage().contains("ngừng hoạt động"));
    }
    @Test void TC008_nhanVienNgungLamViec(){
        KhoGiaLap kho=new KhoGiaLap();kho.taiKhoan=taiKhoan("NHA_SI",true,false);
        assertTrue(assertThrows(LoiDangNhap.class,()->new DangNhapService(kho).dangNhap("nhanvien",matKhau)).getMessage().contains("ngừng hoạt động"));
    }
    @Test void TC009_thieuDuLieuKhongGoiDAO(){
        KhoGiaLap kho=new KhoGiaLap();DangNhapService dichVu=new DangNhapService(kho);
        assertThrows(LoiDangNhap.class,()->dichVu.dangNhap(null,matKhau));assertThrows(LoiDangNhap.class,()->dichVu.dangNhap(" ",matKhau));
        assertThrows(LoiDangNhap.class,()->dichVu.dangNhap("nhanvien",null));assertThrows(LoiDangNhap.class,()->dichVu.dangNhap("nhanvien"," "));
        assertNull(kho.tenDaNhan);
    }
    @Test void TC010_loiSQLKhacSaiXacThucVaKhongLoChiTiet(){
        KhoGiaLap kho=new KhoGiaLap();kho.loiSQL=true;
        LoiDangNhap loi=assertThrows(LoiDangNhap.class,()->new DangNhapService(kho).dangNhap("nhanvien",matKhau));
        assertTrue(loi.getMessage().contains("SQL Server"));assertFalse(loi.getMessage().contains("nội bộ"));assertNull(loi.getCause());
    }
    @Test void TC011_dangXuatXoaDanhTinhVaVoHieuPhienCu(){
        PhienDangNhap phien=new PhienDangNhap();phien.batDauPhienDangNhap(new KetQuaDangNhap("NV1","Tên thật",VaiTro.MANAGER));
        long cu=phien.layPhienBan();phien.xoa();assertFalse(phien.dangHoatDong());assertNull(phien.layVaiTro());assertNull(phien.layMaNhanVien());assertNull(phien.layHoTen());
        assertThrows(IllegalStateException.class,()->phien.yeuCauVaiTro(cu));
    }
    @Test void TC012_manHinhKhongThuocVaiTroBiTuChoi(){
        assertFalse(iuh.se.nhom19.app.DuongDanManHinh.NHAN_SU.choPhep(VaiTro.DENTIST));
        assertFalse(iuh.se.nhom19.app.DuongDanManHinh.KHAM_BENH.choPhep(VaiTro.NURSE));
        assertTrue(iuh.se.nhom19.app.DuongDanManHinh.TONG_QUAN.choPhep(VaiTro.NURSE));
    }
    @Test void TC013_moiTenDangNhapDeuPhaiTraQuaDAO(){
        KhoGiaLap kho=new KhoGiaLap();assertThrows(LoiDangNhap.class,()->new DangNhapService(kho).dangNhap("khong_ton_tai",matKhau));
        assertEquals("khong_ton_tai",kho.tenDaNhan);
    }
    @Test void TC014_dangNhapLaiKhongGiuPhienTruoc(){
        PhienDangNhap phien=new PhienDangNhap();phien.batDauPhienDangNhap(new KetQuaDangNhap("NV1","Người một",VaiTro.MANAGER));long cu=phien.layPhienBan();
        phien.xoa();phien.batDauPhienDangNhap(new KetQuaDangNhap("NV2","Người hai",VaiTro.NURSE));
        assertThrows(IllegalStateException.class,()->phien.yeuCauVaiTro(cu));assertEquals("NV2",phien.layMaNhanVien());assertTrue(phien.dangHoatDong());
    }
    @Test void hashHongVaChucVuLaKhongDuocDangNhap(){
        KhoGiaLap kho=new KhoGiaLap();kho.taiKhoan=new TaiKhoan("TK","user","khong-phai-hash",true,new NhanVien("NV","Tên","NHA_SI",true));
        assertEquals(DangNhapService.SAI_THONG_TIN,assertThrows(LoiDangNhap.class,()->new DangNhapService(kho).dangNhap("user",matKhau)).getMessage());
        kho.taiKhoan=taiKhoan("ROLE_LA",true,true);assertThrows(LoiDangNhap.class,()->new DangNhapService(kho).dangNhap("user",matKhau));
    }
}
