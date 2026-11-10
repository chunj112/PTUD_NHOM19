package iuh.se.nhom19;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import iuh.se.nhom19.service.KiemTraNghiepVu;

class DinhDangNgayGioTest {
    private final LocalDate homNay=LocalDate.of(2026,11,3);

    @Test void ngaySinhDungDinhDangVaXuLyNamNhuan(){
        assertEquals(LocalDate.of(2005,8,15),KiemTraNghiepVu.docNgaySinh("15/08/2005",homNay));
        assertEquals(LocalDate.of(2004,2,29),KiemTraNghiepVu.docNgaySinh("29/02/2004",homNay));
        assertEquals("15/08/2005",KiemTraNghiepVu.hienNgaySinh(LocalDate.of(2005,8,15)));
    }

    @Test void ngaySinhKhongChapNhanNgayKhongTonTaiSaiDinhDangHoacTuongLai(){
        assertThrows(IllegalArgumentException.class,()->KiemTraNghiepVu.docNgaySinh("31/02/2005",homNay));
        assertThrows(IllegalArgumentException.class,()->KiemTraNghiepVu.docNgaySinh("29/02/2005",homNay));
        assertThrows(IllegalArgumentException.class,()->KiemTraNghiepVu.docNgaySinh("2005-08-15",homNay));
        assertThrows(IllegalArgumentException.class,()->KiemTraNghiepVu.docNgaySinh("04/11/2026",homNay));
    }

    @Test void gioHenChiNhanHHmm24Gio(){
        assertEquals(LocalTime.of(8,30),KiemTraNghiepVu.docGioHen("08:30"));
        assertThrows(IllegalArgumentException.class,()->KiemTraNghiepVu.docGioHen("25:30"));
        assertThrows(IllegalArgumentException.class,()->KiemTraNghiepVu.docGioHen("08:90"));
        assertThrows(IllegalArgumentException.class,()->KiemTraNghiepVu.docGioHen("8:30"));
    }
}
