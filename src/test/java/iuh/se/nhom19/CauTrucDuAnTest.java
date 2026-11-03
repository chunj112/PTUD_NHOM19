package iuh.se.nhom19;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

class CauTrucDuAnTest {
    @Test
    void taiNguyenKhoiDongTonTai() {
        assertNotNull(getClass().getResource("/fxml/common/khung-ung-dung.fxml"));
        assertNotNull(getClass().getResource("/css/chu-de.css"));
        assertNotNull(getClass().getResource("/images/dentaflow-logo.png"));
    }
}
