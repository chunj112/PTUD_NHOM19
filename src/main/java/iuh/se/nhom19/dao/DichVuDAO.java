package iuh.se.nhom19.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.entity.DichVu;

public class DichVuDAO {
    private final KetNoiCSDL ketNoiCSDL;

    public DichVuDAO() {
        this(new KetNoiCSDL());
    }

    public DichVuDAO(KetNoiCSDL ketNoiCSDL) {
        this.ketNoiCSDL = Objects.requireNonNull(ketNoiCSDL);
    }

    public List<DichVu> layTatCa() throws SQLException {
        List<DichVu> danhSach = new ArrayList<>();
        String sql = "SELECT maDV, tenDV, donViTinh, donGia, thoiGianThucHien, moTa, dangApDung "
                   + "FROM dbo.DichVu ORDER BY maDV ASC";

        try (Connection con = ketNoiCSDL.layKetNoi();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                danhSach.add(anhXaDichVu(rs));
            }
        }
        return danhSach;
    }

    public List<DichVu> layDichVuDangApDung() throws SQLException {
        List<DichVu> danhSach = new ArrayList<>();
        String sql = "SELECT maDV, tenDV, donViTinh, donGia, thoiGianThucHien, moTa, dangApDung "
                   + "FROM dbo.DichVu WHERE dangApDung = 1 ORDER BY tenDV ASC";

        try (Connection con = ketNoiCSDL.layKetNoi();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                danhSach.add(anhXaDichVu(rs));
            }
        }
        return danhSach;
    }

    public DichVu timTheoMa(String maDV) throws SQLException {
        String sql = "SELECT maDV, tenDV, donViTinh, donGia, thoiGianThucHien, moTa, dangApDung "
                   + "FROM dbo.DichVu WHERE maDV = ?";

        try (Connection con = ketNoiCSDL.layKetNoi();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maDV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return anhXaDichVu(rs);
                }
            }
        }
        return null;
    }

    public boolean themDichVu(DichVu dv) throws SQLException {
        String sql = "INSERT INTO dbo.DichVu (maDV, tenDV, donViTinh, donGia, thoiGianThucHien, moTa, dangApDung) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ketNoiCSDL.layKetNoi();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dv.getMaDV());
            ps.setString(2, dv.getTenDV());
            ps.setString(3, dv.getDonViTinh());
            ps.setBigDecimal(4, dv.getDonGia());
            if (dv.getThoiGianThucHien() != null) {
                ps.setInt(5, dv.getThoiGianThucHien());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setString(6, dv.getMoTa());
            ps.setBoolean(7, dv.isDangApDung());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean capNhatDichVu(DichVu dv) throws SQLException {
        String sql = "UPDATE dbo.DichVu "
                   + "SET tenDV = ?, donViTinh = ?, donGia = ?, thoiGianThucHien = ?, moTa = ?, dangApDung = ? "
                   + "WHERE maDV = ?";

        try (Connection con = ketNoiCSDL.layKetNoi();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dv.getTenDV());
            ps.setString(2, dv.getDonViTinh());
            ps.setBigDecimal(3, dv.getDonGia());
            if (dv.getThoiGianThucHien() != null) {
                ps.setInt(4, dv.getThoiGianThucHien());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, dv.getMoTa());
            ps.setBoolean(6, dv.isDangApDung());
            ps.setString(7, dv.getMaDV());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean doiTrangThai(String maDV, boolean dangApDung) throws SQLException {
        String sql = "UPDATE dbo.DichVu SET dangApDung = ? WHERE maDV = ?";

        try (Connection con = ketNoiCSDL.layKetNoi();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, dangApDung);
            ps.setString(2, maDV);
            return ps.executeUpdate() > 0;
        }
    }

    public String sinhMaDichVuMoi() throws SQLException {
        String sql = "SELECT maDV FROM dbo.DichVu WHERE maDV LIKE 'DV%'";
        int max = 0;

        try (Connection con = ketNoiCSDL.layKetNoi();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString("maDV");
                if (ma != null && ma.startsWith("DV")) {
                    try {
                        int num = Integer.parseInt(ma.substring(2));
                        if (num > max) max = num;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return String.format("DV%03d", max + 1);
    }

    private DichVu anhXaDichVu(ResultSet rs) throws SQLException {
        DichVu dv = new DichVu();
        dv.setMaDV(rs.getString("maDV"));
        dv.setTenDV(rs.getString("tenDV"));
        dv.setDonViTinh(rs.getString("donViTinh"));
        dv.setDonGia(rs.getBigDecimal("donGia"));
        int thoiGian = rs.getInt("thoiGianThucHien");
        dv.setThoiGianThucHien(rs.wasNull() ? null : thoiGian);
        dv.setMoTa(rs.getString("moTa"));
        dv.setDangApDung(rs.getBoolean("dangApDung"));
        return dv;
    }
}
