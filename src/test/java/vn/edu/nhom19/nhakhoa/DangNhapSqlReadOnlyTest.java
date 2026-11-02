package vn.edu.nhom19.nhakhoa;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.*;
import java.util.UUID;
import org.junit.jupiter.api.*;
import vn.edu.nhom19.nhakhoa.auth.*;
import vn.edu.nhom19.nhakhoa.config.CauHinhUngDung;
import vn.edu.nhom19.nhakhoa.database.KetNoiCSDL;
import vn.edu.nhom19.nhakhoa.service.*;

/** Tích hợp SQL CHỈ ĐỌC trên DENTAFLOW_DB. Không INSERT/UPDATE/DELETE/seed/DDL. */
class DangNhapSqlReadOnlyTest {
    @BeforeEach void chiChayKhiNhomBatRo(){
        Assumptions.assumeTrue("1".equals(CauHinhUngDung.docGiaTri("DENTAFLOW_SQL_IT")),"Chưa bật kiểm thử SQL chỉ đọc.");
    }
    private String giaTriBatBuoc(String ten){String giaTri=CauHinhUngDung.docGiaTri(ten);assertNotNull(giaTri,"Cần cấu hình "+ten);assertFalse(giaTri.isBlank(),"Thiếu "+ten);return giaTri;}
    private void dungRole(String ma,Role vaiTro)throws Exception{
        try(Connection ketNoi=new KetNoiCSDL().layKetNoi()){assertEquals("DENTAFLOW_DB",ketNoi.getCatalog().toUpperCase(java.util.Locale.ROOT));}
        String ten=giaTriBatBuoc("DENTAFLOW_IT_"+ma+"_USER"),matKhau=giaTriBatBuoc("DENTAFLOW_IT_"+ma+"_PASSWORD");
        KetQuaDangNhap ketQua=new DangNhapService().dangNhap(ten,matKhau);assertEquals(vaiTro,ketQua.layVaiTro());
    }
    @Test void SQL_TC001_manager()throws Exception{dungRole("MANAGER",Role.MANAGER);}
    @Test void SQL_TC002_leTan()throws Exception{dungRole("RECEPTIONIST",Role.RECEPTIONIST);}
    @Test void SQL_TC003_nhaSi()throws Exception{dungRole("DENTIST",Role.DENTIST);}
    @Test void SQL_TC004_yTa()throws Exception{dungRole("NURSE",Role.NURSE);}
    @Test void SQL_TC005_saiMatKhau(){
        String ten=giaTriBatBuoc("DENTAFLOW_IT_MANAGER_USER");
        assertEquals(DangNhapService.SAI_THONG_TIN,assertThrows(LoiDangNhap.class,()->new DangNhapService().dangNhap(ten,UUID.randomUUID().toString())).getMessage());
    }
    @Test void SQL_TC006_tenKhongTonTai(){
        assertEquals(DangNhapService.SAI_THONG_TIN,assertThrows(LoiDangNhap.class,()->new DangNhapService().dangNhap("it_missing_"+UUID.randomUUID(),UUID.randomUUID().toString())).getMessage());
    }
}
