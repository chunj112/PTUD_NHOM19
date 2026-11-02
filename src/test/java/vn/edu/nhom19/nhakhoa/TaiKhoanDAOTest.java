package vn.edu.nhom19.nhakhoa;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import vn.edu.nhom19.nhakhoa.dao.TaiKhoanDAO;
import vn.edu.nhom19.nhakhoa.database.KetNoiCSDL;

/** JDBC giả kiểm tra câu lệnh/tham số và đóng tài nguyên, không gọi SQL Server. */
class TaiKhoanDAOTest {
    @SuppressWarnings("unchecked")
    private <T>T giaLap(Class<T> kieu,InvocationHandler xuLy){return (T)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{kieu},xuLy);}
    private class KetNoiGiaLap extends KetNoiCSDL {
        String sql,thamSo;boolean dongKetNoi,dongLenh,dongDong,coBanGhi=true,loi;int lanNext;
        @Override public Connection layKetNoi(){
            ResultSet dong=giaLap(ResultSet.class,(p,m,a)->{
                switch(m.getName()){
                    case "next":return coBanGhi&&lanNext++==0;
                    case "getString":return switch(a[0].toString()){
                        case "maTaiKhoan"->"TK1";case "tenDangNhap"->"nhanvien";case "matKhau"->"hash-opaque";
                        case "maNV"->"NV1";case "hoTen"->"Nhân viên";case "chucVu"->"NHA_SI";default->null;};
                    case "getBoolean":return true;
                    case "close":dongDong=true;return null;
                    default:return null;
                }
            });
            PreparedStatement lenh=giaLap(PreparedStatement.class,(p,m,a)->{
                switch(m.getName()){
                    case "setString":assertEquals(1,a[0]);thamSo=(String)a[1];return null;
                    case "executeQuery":if(loi)throw new SQLException("unit");return dong;
                    case "close":dongLenh=true;return null;
                    default:return null;
                }
            });
            return giaLap(Connection.class,(p,m,a)->{
                if(m.getName().equals("prepareStatement")){sql=(String)a[0];return lenh;}
                if(m.getName().equals("close"))dongKetNoi=true;return null;
            });
        }
    }
    @Test void preparedStatementKhongNoiInputVaDongTaiNguyen()throws Exception{
        KetNoiGiaLap ketNoi=new KetNoiGiaLap();String input="' OR '1'='1";
        var tk=new TaiKhoanDAO(ketNoi).timTheoTenDangNhap(input);
        assertEquals(input,ketNoi.thamSo);assertFalse(ketNoi.sql.contains(input));assertTrue(ketNoi.sql.endsWith("tenDangNhap=?"));
        assertEquals("NHA_SI",tk.layNhanVien().layChucVu());assertTrue(ketNoi.dongKetNoi&&ketNoi.dongLenh&&ketNoi.dongDong);
    }
    @Test void khongTimThayHoacLoiVanDongTaiNguyen()throws Exception{
        KetNoiGiaLap ketNoi=new KetNoiGiaLap();ketNoi.coBanGhi=false;
        assertNull(new TaiKhoanDAO(ketNoi).timTheoTenDangNhap("missing"));assertTrue(ketNoi.dongKetNoi&&ketNoi.dongLenh&&ketNoi.dongDong);
        KetNoiGiaLap loi=new KetNoiGiaLap();loi.loi=true;assertThrows(SQLException.class,()->new TaiKhoanDAO(loi).timTheoTenDangNhap("user"));
        assertTrue(loi.dongKetNoi&&loi.dongLenh);
    }
}
