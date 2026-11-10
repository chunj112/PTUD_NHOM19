package iuh.se.nhom19;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import iuh.se.nhom19.service.DanhSachChoYTaService;

/** Xác nhận màn hình Y tá chỉ đọc, không có nút gọi hay API thay đổi phiếu. */
class DanhSachChoYTaTest {
    @Test void giaoDienChiCoCotTheoDoiVaNutLamMoi() throws Exception {
        try (InputStream tep = getClass().getResourceAsStream("/fxml/nurse/danh-sach-cho-kham.fxml")) {
            assertNotNull(tep, "Thiếu FXML danh sách chờ của Y tá.");
            var taiLieu = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(tep);
            NodeList cot = taiLieu.getElementsByTagName("TableColumn");
            Set<String> tenCot = new HashSet<>();
            for (int i = 0; i < cot.getLength(); i++) tenCot.add(((Element) cot.item(i)).getAttribute("text"));
            assertEquals(Set.of("STT", "Bệnh nhân", "SĐT", "Giờ tiếp nhận", "Nha sĩ", "Phòng", "Trạng thái khám"), tenCot);
            NodeList nut = taiLieu.getElementsByTagName("Button");
            assertEquals(1, nut.getLength());
            assertEquals("Làm mới", ((Element) nut.item(0)).getAttribute("text"));
            for (int i = 0; i < taiLieu.getElementsByTagName("*").getLength(); i++) {
                Element phanTu = (Element) taiLieu.getElementsByTagName("*").item(i);
                assertFalse(phanTu.hasAttribute("onAction"), "FXML không gắn xử lý hành động Java chưa kiểm soát.");
            }
        }
    }

    @Test void serviceYTaKhongKhaiBaoTacVuGoiHaySuaTrangThai() {
        Set<String> phuongThuc = Arrays.stream(DanhSachChoYTaService.class.getDeclaredMethods())
            .map(Method::getName).collect(Collectors.toSet());
        assertTrue(phuongThuc.containsAll(Set.of("layPhongDuocPhanCong", "layDanhSach")));
        assertTrue(phuongThuc.stream().noneMatch(ten -> ten.toLowerCase().contains("goi")
            || ten.toLowerCase().contains("capnhat") || ten.toLowerCase().contains("batdau")
            || ten.toLowerCase().contains("hoantat")));
    }

    @Test void dashboardYTaChiCoThongTinXemVaLienKetDanhSach() throws Exception {
        try (InputStream tep = getClass().getResourceAsStream("/fxml/nurse/tong-quan.fxml")) {
            assertNotNull(tep, "Thiếu Dashboard Y tá.");
            var taiLieu = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(tep);
            String giaoDien = taiLieu.getDocumentElement().getTextContent().toLowerCase(java.util.Locale.ROOT);
            assertTrue(giaoDien.contains("ca làm hôm nay"));
            assertTrue(giaoDien.contains("phòng được phân công"));
            assertTrue(giaoDien.contains("chờ khám"));
            assertTrue(giaoDien.contains("đang khám"));
            assertTrue(giaoDien.contains("đã khám"));
            assertTrue(giaoDien.contains("stt chờ khám gần nhất"));
            assertTrue(giaoDien.contains("xem danh sách chờ khám"));
            assertFalse(giaoDien.contains("đã gọi"));
            assertFalse(giaoDien.contains("gọi lại"));
            assertFalse(giaoDien.contains("số lần gọi"));
        }
    }
}
