package iuh.se.nhom19.dao;

import java.sql.*;
import iuh.se.nhom19.entity.HoSoBenhAn;

public class HoSoBenhAnDAO {
    public HoSoBenhAn timTheoMaBenhNhan(Connection ketNoi, String maBenhNhan) throws SQLException {
        try (PreparedStatement lenh = ketNoi.prepareStatement("SELECT * FROM dbo.HoSoBenhAn WHERE maBN=?")) {
            lenh.setString(1, maBenhNhan);
            try (ResultSet bang = lenh.executeQuery()) {
                if (!bang.next()) return null;
                HoSoBenhAn hoSo = new HoSoBenhAn();
                hoSo.datMaHoSo(bang.getString("maHSBA")); hoSo.datMaBenhNhan(maBenhNhan);
                hoSo.datNgayTao(bang.getTimestamp("ngayTao").toLocalDateTime());
                if (bang.getTimestamp("ngayCapNhatGanNhat") != null) hoSo.datNgayCapNhatGanNhat(bang.getTimestamp("ngayCapNhatGanNhat").toLocalDateTime());
                hoSo.datTienSuBenh(bang.getString("tienSuBenh")); hoSo.datTienSuDiUng(bang.getString("tienSuDiUng"));
                hoSo.datGhiChuCanhBao(bang.getString("ghiChuCanhBao")); return hoSo;
            }
        }
    }
    public void taoHoSoBenhAn(Connection ketNoi, HoSoBenhAn hoSo) throws SQLException {
        // Service truyền cùng Connection; DAO chỉ đóng Statement, không commit hay đóng transaction của Service.
        try (PreparedStatement lenh = ketNoi.prepareStatement("INSERT dbo.HoSoBenhAn(maHSBA,maBN,tienSuDiUng) VALUES(?,?,?)")) {
            lenh.setString(1, hoSo.layMaHoSo()); lenh.setString(2, hoSo.layMaBenhNhan());
            lenh.setString(3, hoSo.layTienSuDiUng()); lenh.executeUpdate();
        }
    }
}
