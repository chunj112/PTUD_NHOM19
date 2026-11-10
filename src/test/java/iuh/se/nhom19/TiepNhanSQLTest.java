package iuh.se.nhom19;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.lang.reflect.*;
import org.junit.jupiter.api.*;
import iuh.se.nhom19.auth.*;
import iuh.se.nhom19.database.KetNoiCSDL;
import iuh.se.nhom19.dao.*;
import iuh.se.nhom19.entity.*;
import iuh.se.nhom19.service.*;

/** SQL thật nhưng toàn bộ fixture nằm trong transaction ngoài luôn ROLLBACK. Không tạo phòng/tài khoản. */
class TiepNhanSQLTest {
    private PhienDangNhap phien(String ma,VaiTro vaiTro){var p=new PhienDangNhap();p.batDauPhienDangNhap(new KetQuaDangNhap(ma,"Kiểm thử rollback",vaiTro));return p;}
    @Test void datLichVaHaiLuongTiepNhanKhongDeLaiDuLieu() throws Exception {
        Assumptions.assumeTrue("1".equals(System.getenv("DENTAFLOW_SQL_ROLLBACK_IT")),"Chưa bật kiểm thử SQL rollback.");
        try(Connection that=new KetNoiCSDL().layKetNoi()) {
            assertEquals("DENTAFLOW_DB",that.getCatalog());
            String leTan=nhanVien(that,"LE_TAN"),nhaSi=nhanVien(that,"NHA_SI"),yTa=nhanVien(that,"Y_TA");
            try(var lenh=that.createStatement();var bang=lenh.executeQuery("SELECT COUNT(*) FROM dbo.PhongKham WHERE maPhong='P01' AND trangThaiHoatDong=1")){bang.next();assertEquals(1,bang.getInt(1),"Cần P01 hoạt động.");}
            Map<String,Long> truoc=thongKe(that);
            that.setAutoCommit(false);
            // Facade chỉ dùng trong test: commit/close của Service không kết thúc transaction ngoài.
            // Mỗi thao tác ghi có Savepoint; rollback của Service vẫn phải hoàn tác đúng thao tác lỗi.
            Savepoint[] moc={null};
            Connection trongTest=(Connection)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Connection.class},(doiTuong,ham,thamSo)->{
                switch(ham.getName()) {
                    case "setAutoCommit": if(Boolean.TRUE.equals(thamSo[0]))throw new SQLException("Test không cho bật autocommit");moc[0]=that.setSavepoint();return null;
                    case "commit": moc[0]=null;return null;
                    case "rollback": if(moc[0]!=null)that.rollback(moc[0]);return null;
                    case "close": return null;
                    default: try{return ham.invoke(that,thamSo);}catch(InvocationTargetException loi){throw loi.getCause();}
                }
            });
            KetNoiCSDL ketNoi=new KetNoiCSDL(){@Override public Connection layKetNoi(){return trongTest;}};
            try {
                LocalDate ngay=LocalDate.now().plusDays(1);
                while(coCa(that,nhaSi,ngay)||coCa(that,yTa,ngay))ngay=ngay.plusDays(1);
                try(var lenh=that.prepareStatement("INSERT dbo.PhanCong(maPC,maNV,maPhong,ngayLam,gioBatDau,gioKetThuc,caLam) VALUES(?,?,'P01',?,'08:00','12:00','SANG')")){
                    lenh.setString(1,KiemTraNghiepVu.sinhMa("PC"));lenh.setString(2,nhaSi);lenh.setDate(3,java.sql.Date.valueOf(ngay));lenh.executeUpdate();
                }
                try(var lenh=that.prepareStatement("INSERT dbo.PhanCong(maPC,maNV,maPhong,ngayLam,gioBatDau,gioKetThuc,caLam) VALUES(?,?,'P01',?,'08:00','12:00','SANG')")){
                    lenh.setString(1,KiemTraNghiepVu.sinhMa("PC"));lenh.setString(2,yTa);lenh.setDate(3,java.sql.Date.valueOf(ngay));lenh.executeUpdate();
                }
                Clock dongHo=Clock.fixed(ngay.atTime(8,0).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant(),ZoneId.of("Asia/Ho_Chi_Minh"));
                var lt=phien(leTan,VaiTro.RECEPTIONIST); var bnDAO=new BenhNhanDAO();var lhDAO=new LichHenDAO();var pcDAO=new PhanCongDAO();var pkDAO=new PhieuKhamDAO();
                LocalDate ngayMoiChuaCoSo=LocalDate.of(2100,1,1);
                while(soCuoiStt(that,ngayMoiChuaCoSo)>0)ngayMoiChuaCoSo=ngayMoiChuaCoSo.plusDays(1);
                assertEquals(1,pkDAO.capSoThuTuKham(that,ngayMoiChuaCoSo),"Ngày mới bắt đầu lại từ 001.");
                assertEquals(2,pkDAO.capSoThuTuKham(that,ngayMoiChuaCoSo),"Cùng ngày tăng dần theo số đã dùng.");
                assertEquals(1,pkDAO.capSoThuTuKham(that,ngayMoiChuaCoSo.plusDays(1)),"Ngày kế tiếp có bộ đếm riêng và bắt đầu lại từ 001.");
                var lich=new LichHenService(lt,ketNoi,bnDAO,lhDAO,pcDAO,dongHo);
                var tiep=new TiepNhanService(lt,ketNoi,bnDAO,lhDAO,pkDAO,pcDAO,dongHo);
                long bnTruoc=dem(that,"BenhNhan"),hsTruoc=dem(that,"HoSoBenhAn");
                LichHen hen=lich.datLichHen(null,"Kiểm thử rollback","0900000000",nhaSi,ngay,LocalTime.of(8,0),LocalTime.of(8,45),null,null);
                assertNull(hen.layMaBenhNhan());assertEquals(bnTruoc,dem(that,"BenhNhan"));assertEquals(hsTruoc,dem(that,"HoSoBenhAn"));
                assertThrows(IllegalArgumentException.class,()->lich.datLichHen(null,"Trùng","0900000000",nhaSi,hen.layNgayHen(),LocalTime.of(8,0),LocalTime.of(8,45),null,null));
                assertThrows(IllegalArgumentException.class,()->lich.datLichHen(null,"Chồng","0900000000",nhaSi,hen.layNgayHen(),LocalTime.of(8,15),LocalTime.of(9,0),null,null));
                int sttTruoc=soCuoiStt(that,ngay);
                PhieuKham pk=tiep.tiepNhanCoHen(hen.layMaLichHen(),null,banNhap());assertEquals("CHO_KHAM",pk.layTrangThaiKham());
                assertEquals(sttTruoc+1,pk.laySoThuTuKham());assertEquals(ngay,pk.layNgayCapSo());
                assertEquals("DA_DEN",lich.timLichHenTheoMa(hen.layMaLichHen()).layTrangThai());assertThrows(IllegalArgumentException.class,()->tiep.tiepNhanCoHen(hen.layMaLichHen()));
                // Dùng lại chính hồ sơ vừa tạo trong transaction: đặt lịch không tạo bản sao BN/HSBA.
                BenhNhan daCo=bnDAO.timTheoMa(that,pk.layMaBenhNhan());
                long soBN=dem(that,"BenhNhan"),soHS=dem(that,"HoSoBenhAn");
                LichHen henCu=lich.datLichHen(daCo.layMaBenhNhan(),daCo.layHoTen(),daCo.laySoDienThoai(),nhaSi,ngay,LocalTime.of(10,15),LocalTime.of(11,0),null,null);
                PhieuKham pkThuHai=tiep.tiepNhanCoHen(henCu.layMaLichHen());
                assertEquals(daCo.layMaBenhNhan(),pkThuHai.layMaBenhNhan());assertEquals(sttTruoc+2,pkThuHai.laySoThuTuKham());
                assertEquals(soBN,dem(that,"BenhNhan"));assertEquals(soHS,dem(that,"HoSoBenhAn"));
                long soLichTruoc=dem(that,"LichHen");
                PhieuKham kh=tiep.tiepNhanKhongHen(null,banNhap(),nhaSi,"P01","Khám","Ghi chú");assertNull(kh.layMaLichHen());
                assertEquals(sttTruoc+3,kh.laySoThuTuKham());assertEquals(ngay,kh.layNgayCapSo());
                assertEquals(soLichTruoc,dem(that,"LichHen"));
                assertThrows(IllegalArgumentException.class,()->tiep.tiepNhanKhongHen(kh.layMaBenhNhan(),nhaSi,"P01","Trùng"));
                // Chỉ thay trạng thái fixture chưa commit để kiểm tra tiếp nhận hồ sơ cũ đã khám xong.
                try(var lenh=that.prepareStatement("UPDATE dbo.PhieuKham SET trangThaiKham='DA_KHAM',ngayKham=CONVERT(date,thoiDiemTiepNhan),chanDoan=N'Fixture rollback' WHERE maPhieuKham=?")){lenh.setString(1,kh.layMaPhieuKham());lenh.executeUpdate();}
                assertEquals(kh.layMaBenhNhan(),tiep.tiepNhanKhongHen(kh.layMaBenhNhan(),nhaSi,"P01","Tái khám").layMaBenhNhan());
                var hangCho=new DanhSachChoKhamService(phien(nhaSi,VaiTro.DENTIST),ketNoi,pkDAO);
                assertEquals(pk.layMaPhieuKham(),hangCho.timPhieuKhamTheoMa(pk.layMaPhieuKham()).layMaPhieuKham());
                var hangChoYTa=new DanhSachChoYTaService(phien(yTa,VaiTro.NURSE),ketNoi,pkDAO);
                assertTrue(hangChoYTa.layPhongDuocPhanCong(ngay).contains("P01"));
                assertTrue(hangChoYTa.layDanhSach(ngay,"P01","CHO_KHAM").stream().anyMatch(p->p.layMaPhieuKham().equals(pk.layMaPhieuKham())));
                assertTrue(hangChoYTa.layDanhSach(ngay,"P01","CHO_KHAM").stream().anyMatch(p->p.layMaPhieuKham().equals(kh.layMaPhieuKham())),
                    "Phiếu tiếp nhận không hẹn phải nằm trong danh sách đúng ca/phòng.");
                assertThrows(IllegalArgumentException.class,()->hangChoYTa.layDanhSach(ngay,"P02",null));
                assertEquals("DANG_KHAM",hangCho.batDauKham(pk.layMaPhieuKham()).layTrangThaiKham());
                assertTrue(hangChoYTa.layDanhSach(ngay,"P01","DANG_KHAM").stream().anyMatch(p->p.layMaPhieuKham().equals(pk.layMaPhieuKham())));
                hangCho.hoanTatKham(pk.layMaPhieuKham(),"Tình trạng kiểm thử", "Chẩn đoán kiểm thử");
                assertTrue(hangChoYTa.layDanhSach(ngay,"P01","DA_KHAM").stream().anyMatch(p->p.layMaPhieuKham().equals(pk.layMaPhieuKham())));
                var ngoaiQuyen=new DanhSachChoKhamService(phien("KHONG_PHAI_NHA_SI_NAY",VaiTro.DENTIST),ketNoi,pkDAO);
                assertThrows(IllegalArgumentException.class,()->ngoaiQuyen.timPhieuKhamTheoMa(pk.layMaPhieuKham()));
                long truocLoi=dem(that,"BenhNhan");int sttTruocLoi=soCuoiStt(that,ngay);
                PhieuKhamDAO loiDAO=new PhieuKhamDAO(){@Override public void taoPhieuKham(Connection c,PhieuKham p)throws SQLException{super.taoPhieuKham(c,p);throw new SQLException("Lỗi kiểm thử sau INSERT");}};
                assertThrows(SQLException.class,()->new TiepNhanService(lt,ketNoi,bnDAO,lhDAO,loiDAO,pcDAO,dongHo).tiepNhanKhongHen(null,banNhap(),nhaSi,"P01","Rollback"));
                assertEquals(truocLoi,dem(that,"BenhNhan"));assertEquals(sttTruocLoi,soCuoiStt(that,ngay),"Lỗi phải hoàn tác cả STT đã dành trước đó.");
                LichHen henLoi=lich.datLichHen(null,"Tiếp nhận phải rollback","0900000000",nhaSi,ngay,LocalTime.of(9,30),LocalTime.of(10,15),null,null);
                long hsTruocLoi=dem(that,"HoSoBenhAn"),pkTruocLoi=dem(that,"PhieuKham");
                assertThrows(SQLException.class,()->new TiepNhanService(lt,ketNoi,bnDAO,lhDAO,loiDAO,pcDAO,dongHo).tiepNhanCoHen(henLoi.layMaLichHen(),null,banNhap()));
                LichHen giuNguyen=lich.timLichHenTheoMa(henLoi.layMaLichHen());
                assertNull(giuNguyen.layMaBenhNhan());assertEquals("CHO_XAC_NHAN",giuNguyen.layTrangThai());
                assertEquals(truocLoi,dem(that,"BenhNhan"));assertEquals(hsTruocLoi,dem(that,"HoSoBenhAn"));assertEquals(pkTruocLoi,dem(that,"PhieuKham"));
                LichHen huy=lich.datLichHen(null,"Hủy","0900000000",nhaSi,ngay,LocalTime.of(8,45),LocalTime.of(9,30),null,null);
                lich.suaLichHen(huy.layMaLichHen(),null,"Sửa","0900000000",nhaSi,ngay,LocalTime.of(8,45),LocalTime.of(9,30),null,"Ghi chú gốc");
                assertThrows(IllegalArgumentException.class,()->lich.suaLichHen(huy.layMaLichHen(),null,"Sửa trùng","0900000000",nhaSi,hen.layNgayHen(),LocalTime.of(8,0),LocalTime.of(8,45),null,null));
                lich.huyLichHen(huy.layMaLichHen(),"Đổi ngày khám");assertTrue(lich.timLichHenTheoMa(huy.layMaLichHen()).layGhiChu().contains("Ghi chú gốc"));assertTrue(lich.timLichHenTheoMa(huy.layMaLichHen()).layGhiChu().contains("Lý do hủy: Đổi ngày khám"));assertEquals("DA_HUY",lich.timLichHenTheoMa(huy.layMaLichHen()).layTrangThai());
                lich.datLichHen(null,"Slot được trả lại","0900000000",nhaSi,ngay,LocalTime.of(8,45),LocalTime.of(9,30),null,null);
                // Kết nối khác không thể vượt qua khóa của yêu cầu thứ nhất đang giữ.
                // Giới hạn chờ chỉ dành cho test; luôn rollback cả hai, không commit dữ liệu vào DB dùng chung.
                try(Connection thuHai=new KetNoiCSDL().layKetNoi()) {
                    try(var lenh=thuHai.createStatement()){lenh.execute("SET LOCK_TIMEOUT 300");}
                    KetNoiCSDL ketNoiThuHai=new KetNoiCSDL(){@Override public Connection layKetNoi(){return thuHai;}};
                    var lichThuHai=new LichHenService(lt,ketNoiThuHai,bnDAO,lhDAO,pcDAO,dongHo);
                    SQLException loiKhoa=assertThrows(SQLException.class,()->lichThuHai.datLichHen(null,"Đồng thời","0900000000",nhaSi,hen.layNgayHen(),LocalTime.of(8,0),LocalTime.of(8,45),null,null));
                    assertEquals(1222,loiKhoa.getErrorCode());
                    assertTrue(iuh.se.nhom19.view.common.CongViecGiaoDien.thongBaoLoi(loiKhoa).contains("người khác"));
                }
            }finally{that.rollback();}
            that.setAutoCommit(true);assertEquals(truoc,thongKe(that),"Kiểm thử không được để lại dữ liệu");
        }
    }
    private Map<String,Long> thongKe(Connection c)throws SQLException{Map<String,Long> k=new TreeMap<>();for(String b:List.of("PhongKham","NhanVien","PhanCong","LichHen","BenhNhan","HoSoBenhAn","PhieuKham","TaiKhoan","SoThuTuKhamNgay"))k.put(b,dem(c,b));return k;}
    private int soCuoiStt(Connection c,LocalDate ngay)throws SQLException{try(var s=c.prepareStatement("SELECT soCuoi FROM dbo.SoThuTuKhamNgay WHERE ngayCapSo=?")){s.setDate(1,java.sql.Date.valueOf(ngay));try(var r=s.executeQuery()){return r.next()?r.getInt(1):0;}}}
    private String nhanVien(Connection c,String cv)throws SQLException{try(var s=c.prepareStatement("SELECT TOP(1) maNV FROM dbo.NhanVien WHERE chucVu=? AND trangThaiLamViec=1 ORDER BY maNV")){s.setString(1,cv);try(var r=s.executeQuery()){assertTrue(r.next(),"Thiếu nhân viên "+cv);return r.getString(1);}}}
    private boolean coCa(Connection c,String nv,LocalDate ngay)throws SQLException{try(var s=c.prepareStatement("SELECT COUNT(*) FROM dbo.PhanCong WHERE maNV=? AND ngayLam=? AND trangThai='DA_PHAN_CONG'")){s.setString(1,nv);s.setDate(2,java.sql.Date.valueOf(ngay));try(var r=s.executeQuery()){r.next();return r.getInt(1)>0;}}}
    private long dem(Connection c,String bang)throws SQLException{try(var s=c.createStatement();var r=s.executeQuery("SELECT COUNT_BIG(*) FROM dbo."+bang)){r.next();return r.getLong(1);}}
    private BenhNhan banNhap(){var b=new BenhNhan();b.datHoTen("Kiểm thử rollback không lưu");b.datSoDienThoai("0900000000");b.datNgaySinh(LocalDate.of(2000,1,1));b.datNam(true);return b;}
}
