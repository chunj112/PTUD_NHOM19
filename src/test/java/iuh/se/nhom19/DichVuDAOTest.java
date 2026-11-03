package iuh.se.nhom19;

import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import iuh.se.nhom19.config.CauHinhUngDung;
import iuh.se.nhom19.dao.DichVuDAO;
import iuh.se.nhom19.entity.DichVu;

import static org.junit.jupiter.api.Assertions.*;

public class DichVuDAOTest {
    private DichVuDAO dao;

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue("1".equals(CauHinhUngDung.docGiaTri("DENTAFLOW_SQL_IT")),
                "Chưa bật kiểm thử SQL.");
        dao = new DichVuDAO();
    }

    @Test
    void layTatCa_DocDuLieuTuDatabase() throws SQLException {
        List<DichVu> danhSach = dao.layTatCa();
        assertNotNull(danhSach);
        assertFalse(danhSach.isEmpty(), "Database phải có ít nhất dịch vụ mẫu DV001/DV002.");
    }

    @Test
    void timTheoMa_TonTaiDV001() throws SQLException {
        DichVu dv = dao.timTheoMa("DV001");
        assertNotNull(dv);
        assertEquals("DV001", dv.getMaDV());
        assertNotNull(dv.getTenDV());
        assertNotNull(dv.getDonGia());
    }

    @Test
    void layDichVuDangApDung_ChiLayDangApDung() throws SQLException {
        List<DichVu> danhSach = dao.layDichVuDangApDung();
        assertNotNull(danhSach);
        for (DichVu dv : danhSach) {
            assertTrue(dv.isDangApDung(), "Dịch vụ phải có trạng thái dangApDung = true");
        }
    }

    @Test
    void sinhMaDichVuMoi_KhongNullVaBatDauBangDV() throws SQLException {
        String maMoi = dao.sinhMaDichVuMoi();
        assertNotNull(maMoi);
        assertTrue(maMoi.startsWith("DV"));
    }
}
