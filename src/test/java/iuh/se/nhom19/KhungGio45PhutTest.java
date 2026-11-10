package iuh.se.nhom19;
import static org.junit.jupiter.api.Assertions.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import iuh.se.nhom19.entity.*;
import iuh.se.nhom19.service.QuyTacLichHen;

class KhungGio45PhutTest {
    private final Clock dongHo=Clock.fixed(Instant.parse("2026-11-03T00:00:00Z"),ZoneId.of("Asia/Ho_Chi_Minh"));
    private PhanCong ca(String tu,String den){var c=new PhanCong();c.datMaNhanVien("NS");c.datNgayLam(LocalDate.of(2026,11,3));c.datGioBatDau(LocalTime.parse(tu));c.datGioKetThuc(LocalTime.parse(den));return c;}
    private LichHen hen(String tu,String den,String tt){var h=new LichHen();h.datMaLichHen("LH");h.datMaNhaSi("NS");h.datNgayHen(LocalDate.of(2026,11,3));h.datGioBatDau(LocalTime.parse(tu));h.datGioKetThuc(LocalTime.parse(den));h.datTrangThai(tt);return h;}
    @Test void sangNamSlotKhongVuotCa(){var ds=QuyTacLichHen.taoSlot(ca("08:00","12:00"),List.of(),null,dongHo);assertEquals(5,ds.size());assertEquals(LocalTime.of(11,45),ds.getLast().layGioKetThuc());}
    @Test void chieuBonSlot(){assertEquals(4,QuyTacLichHen.taoSlot(ca("13:30","17:00"),List.of(),null,dongHo).size());}
    @Test void caBatDauLeKhongEpVeTamGio(){var c=ca("08:15","12:00");var ds=QuyTacLichHen.taoSlot(c,List.of(),null,dongHo);assertEquals(LocalTime.of(8,15),ds.getFirst().layGioBatDau());assertDoesNotThrow(()->QuyTacLichHen.kiemTraSlot(c,LocalTime.of(8,15),LocalTime.of(9,0)));}
    @Test void lichCuChanMoiSlotChongNhau(){var ds=QuyTacLichHen.taoSlot(ca("08:00","12:00"),List.of(hen("08:15","09:00","DA_DEN")),null,dongHo);assertFalse(ds.get(0).conTrong());assertFalse(ds.get(1).conTrong());assertTrue(ds.get(2).conTrong());}
    @Test void lichHuyTraLaiVaSuaKhongTrungChinhNo(){var c=ca("08:00","12:00");assertTrue(QuyTacLichHen.taoSlot(c,List.of(hen("08:00","08:45","DA_HUY")),null,dongHo).getFirst().conTrong());assertTrue(QuyTacLichHen.taoSlot(c,List.of(hen("08:00","08:45","DA_XAC_NHAN")),"LH",dongHo).getFirst().conTrong());}
    @Test void gioDaQuaBiVoHieu(){var ds=QuyTacLichHen.taoSlot(ca("06:00","09:00"),List.of(),null,dongHo);assertFalse(ds.getFirst().conTrong());assertTrue(ds.getLast().conTrong());}
    @Test void chapNhanGioBatDauBatKyNeuDu45PhutTrongCa(){var c=ca("08:00","12:00");assertDoesNotThrow(()->QuyTacLichHen.kiemTraSlot(c,LocalTime.of(8,30),LocalTime.of(9,15)));assertDoesNotThrow(()->QuyTacLichHen.kiemTraSlot(c,LocalTime.of(8,15),LocalTime.of(9,0)));assertThrows(IllegalArgumentException.class,()->QuyTacLichHen.kiemTraSlot(c,LocalTime.of(8,0),LocalTime.of(8,30)));assertThrows(IllegalArgumentException.class,()->QuyTacLichHen.kiemTraSlot(c,LocalTime.of(11,30),LocalTime.of(12,15)));}
}
