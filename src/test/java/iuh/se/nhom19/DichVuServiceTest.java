package iuh.se.nhom19;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import iuh.se.nhom19.auth.VaiTro;
import iuh.se.nhom19.dao.DichVuDAO;
import iuh.se.nhom19.entity.DichVu;
import iuh.se.nhom19.service.DichVuService;

import static org.junit.jupiter.api.Assertions.*;

public class DichVuServiceTest {
    private FakeDichVuDAO fakeDAO;
    private DichVuService service;

    @BeforeEach
    void setUp() {
        fakeDAO = new FakeDichVuDAO();
        service = new DichVuService(fakeDAO);
    }

    @Test
    void themDichVu_ThanhCong_KhiLaManager() throws SQLException {
        DichVu dv = new DichVu("DV100", "Nhổ răng khôn", "Răng", new BigDecimal("1000000"), 45, "Tiểu phẫu", true);
        service.themDichVu(dv, VaiTro.MANAGER);

        DichVu saved = service.timTheoMa("DV100");
        assertNotNull(saved);
        assertEquals("Nhổ răng khôn", saved.getTenDV());
        assertEquals(new BigDecimal("1000000"), saved.getDonGia());
    }

    @Test
    void themDichVu_TuDongSinhMa_KhiChuaCoMa() throws SQLException {
        DichVu dv = new DichVu(null, "Tẩy trắng răng", "Lần", new BigDecimal("1500000"), 60, "Thẩm mỹ", true);
        service.themDichVu(dv, VaiTro.MANAGER);

        assertEquals("DV099", dv.getMaDV());
        assertNotNull(service.timTheoMa("DV099"));
    }

    @Test
    void themDichVu_TuChoi_KhiKhongPhaiManager() {
        DichVu dv = new DichVu("DV101", "Trám răng", "Răng", new BigDecimal("300000"), 30, "", true);
        assertThrows(IllegalArgumentException.class, () -> service.themDichVu(dv, VaiTro.RECEPTIONIST));
        assertThrows(IllegalArgumentException.class, () -> service.themDichVu(dv, VaiTro.DENTIST));
        assertThrows(IllegalArgumentException.class, () -> service.themDichVu(dv, VaiTro.NURSE));
    }

    @Test
    void themDichVu_TuChoi_KhiDonGiaAm_TC006_01() {
        DichVu dv = new DichVu("DV102", "Dịch vụ lỗi", "Lần", new BigDecimal("-100000"), 30, "", true);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.themDichVu(dv, VaiTro.MANAGER));
        assertTrue(ex.getMessage().contains("lớn hơn hoặc bằng 0"));
    }

    @Test
    void themDichVu_TuChoi_KhiTenRong() {
        DichVu dv = new DichVu("DV103", "   ", "Lần", new BigDecimal("100000"), 30, "", true);
        assertThrows(IllegalArgumentException.class, () -> service.themDichVu(dv, VaiTro.MANAGER));
    }

    @Test
    void themDichVu_TuChoi_KhiDonViTinhRong() {
        DichVu dv = new DichVu("DV104", "Tên đúng", "  ", new BigDecimal("100000"), 30, "", true);
        assertThrows(IllegalArgumentException.class, () -> service.themDichVu(dv, VaiTro.MANAGER));
    }

    @Test
    void capNhatDichVu_ThanhCong() throws SQLException {
        DichVu dv = new DichVu("DV001", "Cạo vôi ban đầu", "Lần", new BigDecimal("200000"), 30, "", true);
        fakeDAO.themDichVu(dv);

        DichVu update = new DichVu("DV001", "Cạo vôi răng nâng cao", "Lần", new BigDecimal("350000"), 40, "Đã cập nhật", true);
        service.capNhatDichVu(update, VaiTro.MANAGER);

        DichVu after = service.timTheoMa("DV001");
        assertEquals("Cạo vôi răng nâng cao", after.getTenDV());
        assertEquals(new BigDecimal("350000"), after.getDonGia());
    }

    @Test
    void doiTrangThai_ThanhCong() throws SQLException {
        DichVu dv = new DichVu("DV001", "Cạo vôi", "Lần", new BigDecimal("200000"), 30, "", true);
        fakeDAO.themDichVu(dv);

        service.doiTrangThai("DV001", false, VaiTro.MANAGER);
        assertFalse(service.timTheoMa("DV001").isDangApDung());

        service.doiTrangThai("DV001", true, VaiTro.MANAGER);
        assertTrue(service.timTheoMa("DV001").isDangApDung());
    }

    @Test
    void timKiem_LocTheoTuKhoaVaTrangThai() {
        List<DichVu> list = List.of(
                new DichVu("DV001", "Cạo vôi răng", "Lần", new BigDecimal("250000"), 30, "Dự phòng", true),
                new DichVu("DV002", "Trám răng composite", "Răng", new BigDecimal("500000"), 45, "Phục hồi", false),
                new DichVu("DV003", "Nhổ răng khôn", "Răng", new BigDecimal("1000000"), 60, "Phẫu thuật", true)
        );

        List<DichVu> res1 = service.timKiem(list, "trám", "Tất cả trạng thái", "Tất cả nhóm dịch vụ");
        assertEquals(1, res1.size());
        assertEquals("DV002", res1.get(0).getMaDV());

        List<DichVu> res2 = service.timKiem(list, "", "Đang hoạt động", "Tất cả nhóm dịch vụ");
        assertEquals(2, res2.size());

        List<DichVu> res3 = service.timKiem(list, "", "Ngừng hoạt động", "Tất cả nhóm dịch vụ");
        assertEquals(1, res3.size());
        assertEquals("DV002", res3.get(0).getMaDV());
    }

    private static class FakeDichVuDAO extends DichVuDAO {
        private final List<DichVu> store = new ArrayList<>();

        @Override
        public List<DichVu> layTatCa() {
            return new ArrayList<>(store);
        }

        @Override
        public List<DichVu> layDichVuDangApDung() {
            List<DichVu> active = new ArrayList<>();
            for (DichVu d : store) {
                if (d.isDangApDung()) active.add(d);
            }
            return active;
        }

        @Override
        public DichVu timTheoMa(String maDV) {
            for (DichVu d : store) {
                if (d.getMaDV().equalsIgnoreCase(maDV)) return d;
            }
            return null;
        }

        @Override
        public boolean themDichVu(DichVu dv) {
            store.add(dv);
            return true;
        }

        @Override
        public boolean capNhatDichVu(DichVu dv) {
            for (int i = 0; i < store.size(); i++) {
                if (store.get(i).getMaDV().equalsIgnoreCase(dv.getMaDV())) {
                    store.set(i, dv);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean doiTrangThai(String maDV, boolean dangApDung) {
            DichVu dv = timTheoMa(maDV);
            if (dv != null) {
                dv.setDangApDung(dangApDung);
                return true;
            }
            return false;
        }

        @Override
        public String sinhMaDichVuMoi() {
            return "DV099";
        }
    }
}
