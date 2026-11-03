package iuh.se.nhom19.database;

import java.sql.*;
import java.util.Properties;
import iuh.se.nhom19.config.CauHinhUngDung;

/** Mỗi DAO mở/đóng kết nối của riêng thao tác; không có Connection static dùng chung. */
public class KetNoiCSDL {
    public Connection layKetNoi() throws SQLException {
        String diaChi=CauHinhUngDung.docGiaTri("DENTAFLOW_DB_URL");
        String tenNguoiDung=CauHinhUngDung.docGiaTri("DENTAFLOW_DB_USER");
        String matKhau=CauHinhUngDung.docGiaTri("DENTAFLOW_DB_PASSWORD");
        if(diaChi==null||!diaChi.startsWith("jdbc:sqlserver://")||tenNguoiDung==null||tenNguoiDung.isBlank()||matKhau==null||matKhau.isEmpty())
            throw new SQLException("Chưa cấu hình đầy đủ kết nối SQL Server.","08001");
        Properties thuocTinh=new Properties();
        thuocTinh.setProperty("user",tenNguoiDung);thuocTinh.setProperty("password",matKhau);
        thuocTinh.setProperty("loginTimeout","10");thuocTinh.setProperty("socketTimeout","15000");
        String diaChiThuong=diaChi.toLowerCase(java.util.Locale.ROOT);
        if(!diaChiThuong.matches(".*;\\s*encrypt\\s*=.*"))thuocTinh.setProperty("encrypt","true");
        if(!diaChiThuong.matches(".*;\\s*trustservercertificate\\s*=.*"))thuocTinh.setProperty("trustServerCertificate","false");
        // URL local có thể chọn trustServerCertificate=true theo hướng dẫn, không dùng cho production.
        return DriverManager.getConnection(diaChi,thuocTinh);
    }
}
