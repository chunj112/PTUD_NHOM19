package vn.edu.nhom19.nhakhoa.database;

import java.io.Console;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import vn.edu.nhom19.nhakhoa.auth.BaoMatMatKhau;
import vn.edu.nhom19.nhakhoa.config.CauHinhUngDung;

/** Công cụ tạo tài khoản mẫu theo xác nhận của nhóm; ứng dụng và test không tự gọi. */
public final class TaoTaiKhoanKiemThu {
    private static final String[][] TAI_KHOAN={
        {"manager_test","TST_NV_QL","TST_TK_QL","NHAN_VIEN_QUAN_LY","DENTAFLOW_TEST_MANAGER_PASSWORD"},
        {"receptionist_test","TST_NV_LT","TST_TK_LT","LE_TAN","DENTAFLOW_TEST_RECEPTIONIST_PASSWORD"},
        {"dentist_test","TST_NV_NS","TST_TK_NS","NHA_SI","DENTAFLOW_TEST_DENTIST_PASSWORD"},
        {"nurse_test","TST_NV_YT","TST_TK_YT","Y_TA","DENTAFLOW_TEST_NURSE_PASSWORD"}
    };
    private TaoTaiKhoanKiemThu() {}
    public static void main(String[] thamSo) {
        try {
            if(thamSo.length!=1||!thamSo[0].equals("--xac-nhan-them-tai-khoan"))
                throw new IllegalArgumentException("Chỉ chạy sau khi nhóm đồng ý thêm tài khoản, với --xac-nhan-them-tai-khoan.");
            if(!"true".equalsIgnoreCase(CauHinhUngDung.docGiaTri("DENTAFLOW_ALLOW_ACCOUNT_SEED")))
                throw new IllegalArgumentException("Chưa bật DENTAFLOW_ALLOW_ACCOUNT_SEED=true sau xác nhận của nhóm.");
            List<String> thongBao=new ArrayList<>();
            try(Connection ketNoi=new KetNoiCSDL().layKetNoi()) {
                if(!"DENTAFLOW_DB".equalsIgnoreCase(ketNoi.getCatalog()))throw new IllegalArgumentException("Công cụ chỉ hỗ trợ database local DENTAFLOW_DB đã xác nhận.");
                ketNoi.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);ketNoi.setAutoCommit(false);
                try {
                    for(String[] mau:TAI_KHOAN) {
                        if(daCoTaiKhoan(ketNoi,mau[0],mau[3])){thongBao.add(mau[0]+": đã có, không thay đổi mật khẩu/dữ liệu.");continue;}
                        taoNhanVienNeuThieu(ketNoi,mau);
                        String bam=layMatKhauDaBam(mau[0],mau[4]);
                        try(PreparedStatement lenh=ketNoi.prepareStatement("INSERT INTO dbo.TaiKhoan(maTaiKhoan,maNV,tenDangNhap,matKhau,trangThaiHoatDong) VALUES(?,?,?,?,1)")) {
                            lenh.setString(1,mau[2]);lenh.setString(2,mau[1]);lenh.setString(3,mau[0]);lenh.setString(4,bam);lenh.executeUpdate();
                        }
                        thongBao.add(mau[0]+": đã tạo với hash PBKDF2 và salt riêng.");
                    }
                    ketNoi.commit();
                } catch(SQLException|RuntimeException loi) {
                    try{ketNoi.rollback();}catch(SQLException loiRollback){/* Không in exception chứa cấu hình kết nối. */}
                    throw loi;
                }
            }
            for(String thongTin:thongBao)System.out.println(thongTin);
        } catch(SQLException loi) {
            System.err.println("Không seed được tài khoản kiểm thử. Kiểm tra kết nối, quyền INSERT và ràng buộc; không thay đổi dữ liệu cũ.");System.exit(1);
        } catch(IllegalArgumentException|IllegalStateException loi) {
            System.err.println(loi.getMessage());System.exit(1);
        }
    }
    private static boolean daCoTaiKhoan(Connection ketNoi,String tenDangNhap,String chucVu) throws SQLException {
        try(PreparedStatement lenh=ketNoi.prepareStatement("SELECT nv.chucVu FROM dbo.TaiKhoan tk JOIN dbo.NhanVien nv ON tk.maNV=nv.maNV WHERE tk.tenDangNhap=?")) {
            lenh.setString(1,tenDangNhap);
            try(ResultSet dong=lenh.executeQuery()) {
                if(!dong.next())return false;
                if(!chucVu.equals(dong.getString(1)))throw new IllegalArgumentException("Tên tài khoản kiểm thử đã tồn tại với chức vụ khác; không ghi đè.");
                return true;
            }
        }
    }
    private static void taoNhanVienNeuThieu(Connection ketNoi,String[] mau) throws SQLException {
        try(PreparedStatement lenh=ketNoi.prepareStatement("SELECT chucVu,trangThaiLamViec FROM dbo.NhanVien WHERE maNV=?")) {
            lenh.setString(1,mau[1]);
            try(ResultSet dong=lenh.executeQuery()) {
                if(dong.next()){
                    if(!mau[3].equals(dong.getString(1))||!dong.getBoolean(2))throw new IllegalArgumentException("Mã nhân viên kiểm thử đã tồn tại nhưng không phù hợp; không ghi đè.");
                    return;
                }
            }
        }
        try(PreparedStatement lenh=ketNoi.prepareStatement("INSERT INTO dbo.NhanVien(maNV,hoTen,gioiTinh,ngaySinh,soDienThoai,diaChi,chucVu,trangThaiLamViec,email) VALUES(?,?,?,?,?,?,?,1,NULL)")) {
            lenh.setString(1,mau[1]);lenh.setString(2,"Nhân viên kiểm thử "+mau[0]);lenh.setString(3,"Nam");
            lenh.setDate(4,java.sql.Date.valueOf(LocalDate.of(1990,1,1)));lenh.setString(5,"0900000000");
            lenh.setString(6,"Tài khoản mẫu UC001 được nhóm cho phép");lenh.setString(7,mau[3]);lenh.executeUpdate();
        }
    }
    private static String layMatKhauDaBam(String tenDangNhap,String tenBien) {
        String matKhau=CauHinhUngDung.docGiaTri(tenBien);char[] nhap=null;
        try {
            if(matKhau==null||matKhau.isEmpty()){
                Console banDieuKhien=System.console();
                if(banDieuKhien==null)throw new IllegalArgumentException("Thiếu cấu hình "+tenBien+"; Eclipse không có Console nhập mật khẩu ẩn.");
                nhap=banDieuKhien.readPassword("Mật khẩu kiểm thử cho %s (ít nhất 12 ký tự): ",tenDangNhap);
                if(nhap==null)throw new IllegalArgumentException("Đã hủy nhập mật khẩu kiểm thử.");
                matKhau=new String(nhap);
            }
            if(matKhau.length()<12)throw new IllegalArgumentException("Mật khẩu kiểm thử cần ít nhất 12 ký tự.");
            return BaoMatMatKhau.bamMatKhau(matKhau);
        } finally {if(nhap!=null)Arrays.fill(nhap,'\0');}
    }
}
