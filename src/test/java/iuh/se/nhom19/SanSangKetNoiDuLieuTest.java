package iuh.se.nhom19;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.*;

/** Kiểm tra hợp đồng bàn giao, không sửa hoặc kết nối database. */
class SanSangKetNoiDuLieuTest {
    private List<Path> layDuongDanFXML()throws Exception{try(var duongDanTep=Files.walk(Path.of("src/main/resources/fxml"))){return duongDanTep.filter(tepDangXet->tepDangXet.toString().endsWith(".fxml")).toList();}}
    private Document docFXML(Path duongDanTep)throws Exception{
        var boTaoXML=DocumentBuilderFactory.newInstance();boTaoXML.setNamespaceAware(true);boTaoXML.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
        try(var luongDoc=Files.newInputStream(duongDanTep)){return boTaoXML.newDocumentBuilder().parse(luongDoc);}
    }
    @Test void khongConDuongDanDangNhapGiaLapTrongNguonChinh()throws Exception{
        try(var duongDanTep=Files.walk(Path.of("src/main/java"))){for(Path tepDangXet:duongDanTep.filter(tepDangXet->tepDangXet.toString().endsWith(".java")).toList()){
            String noiDungJava=Files.readString(tepDangXet);
            for(String cam:List.of("openDemoRole(","startDemo(","cheDoDemo(","DENTAFLOW_MODE","CheDoDangNhap"))assertFalse(noiDungJava.contains(cam),tepDangXet+" còn "+cam);
        }}
        assertFalse(Files.readString(Path.of(".env.example")).contains("DENTAFLOW_MODE"));
    }
    @Test void giuManHinhNhungKhongConBanGhiVaDuLieuNhapMau()throws Exception{
        List<Path> tep=layDuongDanFXML();assertEquals(89,tep.size());
        for(Path duongDanTep:tep){Document taiLieuFXML=docFXML(duongDanTep);assertEquals(0,taiLieuFXML.getElementsByTagNameNS("*","HashMap").getLength(),duongDanTep.toString());
            NodeList tatCa=taiLieuFXML.getElementsByTagName("*");
            for(int i=0;i<tatCa.getLength();i++){
                Element phanTu=(Element)tatCa.item(i);String tenThe=phanTu.getLocalName(),noiDungChu=phanTu.getAttribute("text");
                if(List.of("TextField","TextArea","PasswordField","editor").contains(tenThe))assertTrue(noiDungChu.isEmpty(),duongDanTep+" còn input mẫu");
                if(tenThe.equals("Label")){
                    assertFalse(noiDungChu.matches(".*(?:BN|NV|HD|LH|PK|DV)[0-9]{3,}.*"),duongDanTep+" còn mã bản ghi mẫu");
                    assertFalse(noiDungChu.matches(".*[0-9][0-9., ]*\\s*₫.*"),duongDanTep+" còn số tiền mẫu");
                    assertFalse(noiDungChu.matches(".*(?:Trần Minh Đức|Nguyễn Lan Anh|Nguyễn Văn A|Lê Hoàng Nam).*"),duongDanTep+" còn tên mẫu");
                }
            }
        }
        assertFalse(Files.exists(Path.of("src/main/resources/images/receptionist/transfer-qr-demo.png")));
    }
    @Test void nutNghiepVuChuaKetNoiDuLieuDeuKhoa()throws Exception{
        for(Path duongDanTep:layDuongDanFXML())if(!duongDanTep.getParent().getFileName().toString().equals("common")){
            NodeList nut=docFXML(duongDanTep).getElementsByTagNameNS("*","Button");
            for(int i=0;i<nut.getLength();i++)assertEquals("true",((Element)nut.item(i)).getAttribute("disable"),duongDanTep.toString());
        }
    }
    @Test void sqlKhoiTaoVaDanhMucKhongCoLenhDonDuLieuCu()throws Exception{
        for(String ten:List.of("01_create_schema.sql","02_seed_reference_data.sql","03_verify_schema.sql")){
            String cauLenhSQL=Files.readString(Path.of("database/sqlserver",ten));assertTrue(cauLenhSQL.contains("DENTAFLOW_DB"));assertFalse(cauLenhSQL.contains("NhaKhoa_QuanLy"));
            String boChuThich=cauLenhSQL.replaceAll("(?s)/\\*.*?\\*/","").replaceAll("(?m)--.*$","");
            assertFalse(boChuThich.matches("(?is).*\\b(?:DROP\\s+DATABASE|TRUNCATE|DELETE\\s+FROM)\\b.*"));
        }
        String tao=Files.readString(Path.of("database/sqlserver/01_create_schema.sql"));
        assertTrue(tao.contains(":ON ERROR EXIT"));assertTrue(tao.contains("sys.tables"));assertTrue(tao.contains("PBKDF2-SHA256$%"));
    }
}
