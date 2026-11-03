package iuh.se.nhom19;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import iuh.se.nhom19.dao.TaiKhoanDAO;
import iuh.se.nhom19.database.KetNoiCSDL;

/** JDBC giả kiểm tra câu lệnh/tham số và đóng tài nguyên, không gọi SQL Server. */
class TaiKhoanDAOTest {
    @SuppressWarnings("unchecked")
    private <T>T giaLap(Class<T> kieu,InvocationHandler xuLy){return (T)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{kieu},xuLy);}
    private class KetNoiGiaLap extends KetNoiCSDL {
        String cauLenhSQL,thamSo;boolean dongKetNoi,dongLenh,dongDong,coBanGhi=true,loi;int lanNext;
        @Override public Connection layKetNoi(){
            ResultSet dong=giaLap(ResultSet.class,(doiTuong,phuongThuc,cacThamSo)->{
                switch(phuongThuc.getName()){
                    case "next":return coBanGhi&&lanNext++==0;
                    case "getString":return switch(cacThamSo[0].toString()){
                        case "maTaiKhoan"->"TK1";case "tenDangNhap"->"nhanvien";case "matKhau"->"hash-opaque";
                        case "maNV"->"NV1";case "hoTen"->"Nhân viên";case "chucVu"->"NHA_SI";default->null;};
                    case "getBoolean":return true;
                    case "close":dongDong=true;return null;
                    default:return null;
                }
            });
            PreparedStatement lenh=giaLap(PreparedStatement.class,(doiTuong,phuongThuc,cacThamSo)->{
                switch(phuongThuc.getName()){
                    case "setString":assertEquals(1,cacThamSo[0]);thamSo=(String)cacThamSo[1];return null;
                    case "executeQuery":if(loi)throw new SQLException("unit");return dong;
                    case "close":dongLenh=true;return null;
                    default:return null;
                }
            });
            return giaLap(Connection.class,(doiTuong,phuongThuc,cacThamSo)->{
                if(phuongThuc.getName().equals("prepareStatement")){cauLenhSQL=(String)cacThamSo[0];return lenh;}
                if(phuongThuc.getName().equals("close"))dongKetNoi=true;return null;
            });
        }
    }
    @Test void truyVanThamSoKhongNoiDuLieuNhapVaDongTaiNguyen()throws Exception{
        KetNoiGiaLap ketNoi=new KetNoiGiaLap();String duLieuNhap="' OR '1'='1";
        var tk=new TaiKhoanDAO(ketNoi).timTheoTenDangNhap(duLieuNhap);
        assertEquals(duLieuNhap,ketNoi.thamSo);assertFalse(ketNoi.cauLenhSQL.contains(duLieuNhap));assertTrue(ketNoi.cauLenhSQL.endsWith("tenDangNhap=?"));
        assertEquals("NHA_SI",tk.layNhanVien().layChucVu());assertTrue(ketNoi.dongKetNoi&&ketNoi.dongLenh&&ketNoi.dongDong);
    }
    @Test void khongTimThayHoacLoiVanDongTaiNguyen()throws Exception{
        KetNoiGiaLap ketNoi=new KetNoiGiaLap();ketNoi.coBanGhi=false;
        assertNull(new TaiKhoanDAO(ketNoi).timTheoTenDangNhap("missing"));assertTrue(ketNoi.dongKetNoi&&ketNoi.dongLenh&&ketNoi.dongDong);
        KetNoiGiaLap loi=new KetNoiGiaLap();loi.loi=true;assertThrows(SQLException.class,()->new TaiKhoanDAO(loi).timTheoTenDangNhap("user"));
        assertTrue(loi.dongKetNoi&&loi.dongLenh);
    }
}
