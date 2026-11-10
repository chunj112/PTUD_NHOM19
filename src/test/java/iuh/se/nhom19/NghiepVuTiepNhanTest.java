package iuh.se.nhom19;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.*;
import java.time.*;
import org.junit.jupiter.api.Test;
import iuh.se.nhom19.auth.*;
import iuh.se.nhom19.dao.*;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.service.*;

/** Các lỗi đầu vào/phân quyền phải bị chặn trước khi mở JDBC. Không kết nối SQL. */
class NghiepVuTiepNhanTest {
    private final Clock dongHo = Clock.fixed(Instant.parse("2026-11-03T02:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
    private final KetNoiCSDL camSQL = new KetNoiCSDL() {
        @Override public Connection layKetNoi() { fail("Đầu vào không hợp lệ không được chạm SQL."); return null; }
    };
    private PhienDangNhap taoPhien(VaiTro vaiTro) {
        PhienDangNhap phien = new PhienDangNhap(); phien.batDauPhienDangNhap(new KetQuaDangNhap("NV_TEST", "Người kiểm thử", vaiTro)); return phien;
    }
    private LichHenService lichHen(PhienDangNhap phien) { return new LichHenService(phien, camSQL, new BenhNhanDAO(), new LichHenDAO(), new PhanCongDAO(), dongHo); }

    @Test void chanNguoiChuaDangNhap() { assertThrows(IllegalStateException.class, () -> new BenhNhanService(new PhienDangNhap())); }
    @Test void nhaSiKhongDuocTiepNhan() {
        var dichVu = new TiepNhanService(taoPhien(VaiTro.DENTIST), camSQL);
        assertThrows(IllegalStateException.class, () -> dichVu.tiepNhanKhongHen("BN", "NS", "P", "Khám"));
        assertThrows(IllegalStateException.class, () -> dichVu.tiepNhanCoHen("LH"));
    }
    @Test void leTanKhongDuocDocHangChoNhaSi() {
        var dichVu = new DanhSachChoKhamService(taoPhien(VaiTro.RECEPTIONIST), camSQL, new PhieuKhamDAO());
        assertThrows(IllegalStateException.class, dichVu::layDanhSachChoKham);
        assertThrows(IllegalStateException.class, () -> dichVu.timPhieuKhamTheoMa("PK"));
    }
    @Test void leTanKhongDuocDocDanhSachChoCuaYTa() {
        var dichVu = new DanhSachChoYTaService(taoPhien(VaiTro.RECEPTIONIST), camSQL, new PhieuKhamDAO());
        assertThrows(IllegalStateException.class, () -> dichVu.layDanhSach(LocalDate.now(dongHo), "P01", "CHO_KHAM"));
        assertThrows(IllegalStateException.class, () -> dichVu.layPhongDuocPhanCong(LocalDate.now(dongHo)));
    }
    @Test void leTanKhongDuocBatDauHoacHoanTatKhamThayNhaSi() {
        var dichVu = new DanhSachChoKhamService(taoPhien(VaiTro.RECEPTIONIST), camSQL, new PhieuKhamDAO());
        assertThrows(IllegalStateException.class, () -> dichVu.batDauKham("PK"));
        assertThrows(IllegalStateException.class, () -> dichVu.hoanTatKham("PK", "tình trạng", "chẩn đoán"));
    }
    @Test void serviceCuKhongDoiDanhTinhSauDangXuatDangNhapLai() {
        var phien = taoPhien(VaiTro.RECEPTIONIST); var dichVu = new TiepNhanService(phien, camSQL);
        phien.xoa(); phien.batDauPhienDangNhap(new KetQuaDangNhap("NV_KHAC", "Lễ tân khác", VaiTro.RECEPTIONIST));
        assertThrows(IllegalStateException.class, () -> dichVu.tiepNhanCoHen("LH"));
    }
    @Test void chanNgayGioHenKhongHopLe() {
        var dichVu = lichHen(taoPhien(VaiTro.RECEPTIONIST)); LocalDate ngay = LocalDate.of(2026, 11, 3);
        assertThrows(IllegalArgumentException.class, () -> dichVu.datLichHen("BN", "NS", ngay, LocalTime.of(8, 0), LocalTime.of(8, 30), "Khám", null));
        assertThrows(IllegalArgumentException.class, () -> dichVu.datLichHen("BN", "NS", ngay, LocalTime.of(10, 0), LocalTime.of(9, 0), "Khám", null));
        assertThrows(IllegalArgumentException.class, () -> dichVu.datLichHen("BN", "NS", null, LocalTime.NOON, LocalTime.NOON, "Khám", null));
    }
    @Test void chanThieuLyDoKhongHen() {
        var dichVu = new TiepNhanService(taoPhien(VaiTro.RECEPTIONIST), camSQL);
        assertThrows(IllegalArgumentException.class, () -> dichVu.tiepNhanKhongHen("BN", "NS", "P", " "));
    }
    @Test void chanNgaySinhVaSoDienThoaiSai() {
        var dichVu = new BenhNhanService(taoPhien(VaiTro.RECEPTIONIST), camSQL, new BenhNhanDAO(), dongHo);
        assertThrows(IllegalArgumentException.class, () -> dichVu.themBenhNhan("Người thử", true, LocalDate.of(2027, 1, 1), "0901234567", null, null));
        assertThrows(IllegalArgumentException.class, () -> dichVu.themBenhNhan("Người thử", true, LocalDate.of(2000, 1, 1), "abc", null, null));
        assertThrows(IllegalArgumentException.class, () -> dichVu.themBenhNhan("Người thử", null, LocalDate.of(2000, 1, 1), "0901234567", null, null));
    }
    @Test void timKiemKhongDuocTaiToanBoHoSoKhiBoTrong() {
        var dichVu = new BenhNhanService(taoPhien(VaiTro.RECEPTIONIST), camSQL);
        assertThrows(IllegalArgumentException.class, () -> dichVu.timBenhNhan(" "));
    }

    @Test void nguoiMoiDatLichPhaiCoTenVaSoDienThoaiHopLe() {
        var dichVu=lichHen(taoPhien(VaiTro.RECEPTIONIST)); var ngay=LocalDate.of(2026,11,3);
        assertThrows(IllegalArgumentException.class,()->dichVu.datLichHen(null," ","0901234567","NS",ngay,LocalTime.of(10,0),LocalTime.of(10,30),null,null));
        assertThrows(IllegalArgumentException.class,()->dichVu.datLichHen(null,"Người đặt lịch","abc","NS",ngay,LocalTime.of(10,0),LocalTime.of(10,30),null,null));
    }
    @Test void banNhapBenhNhanKhongTuDienNgaySinhHoacGioiTinh() {
        var thongTin=new iuh.se.nhom19.entity.BenhNhan(); thongTin.datHoTen("Bệnh nhân mới"); thongTin.datSoDienThoai("0901234567");
        assertThrows(IllegalArgumentException.class,()->KiemTraNghiepVu.thongTinBenhNhanMoi(thongTin,LocalDate.now(dongHo)));
        thongTin.datNam(true);
        assertThrows(IllegalArgumentException.class,()->KiemTraNghiepVu.thongTinBenhNhanMoi(thongTin,LocalDate.now(dongHo)));
        thongTin.datNgaySinh(LocalDate.of(2000,1,1));
        assertNull(KiemTraNghiepVu.thongTinBenhNhanMoi(thongTin,LocalDate.now(dongHo)).layMaBenhNhan());
    }
    @Test void tenVaSoDienThoaiKhongDuocVuotRangBuocSQL() {
        assertThrows(IllegalArgumentException.class,()->KiemTraNghiepVu.batBuoc("N".repeat(121),"họ tên",120));
        assertThrows(IllegalArgumentException.class,()->KiemTraNghiepVu.soDienThoai("090000000000"));
        assertEquals("0901234567",KiemTraNghiepVu.soDienThoai(" 0901234567 "));
    }
}
