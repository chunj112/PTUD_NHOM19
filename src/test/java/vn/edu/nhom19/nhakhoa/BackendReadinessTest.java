package vn.edu.nhom19.nhakhoa;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.*;

/** Kiểm tra hợp đồng bàn giao, không sửa hoặc kết nối database. */
class BackendReadinessTest {
    private List<Path> fxml()throws Exception{try(var p=Files.walk(Path.of("src/main/resources/fxml"))){return p.filter(x->x.toString().endsWith(".fxml")).toList();}}
    private Document doc(Path p)throws Exception{
        var f=DocumentBuilderFactory.newInstance();f.setNamespaceAware(true);f.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
        try(var in=Files.newInputStream(p)){return f.newDocumentBuilder().parse(in);}
    }
    @Test void khongConDuongDanDangNhapDemoTrongMain()throws Exception{
        try(var p=Files.walk(Path.of("src/main/java"))){for(Path x:p.filter(x->x.toString().endsWith(".java")).toList()){
            String s=Files.readString(x);
            for(String cam:List.of("openDemoRole(","startDemo(","cheDoDemo(","DENTAFLOW_MODE","CheDoDangNhap"))assertFalse(s.contains(cam),x+" còn "+cam);
        }}
        assertFalse(Files.readString(Path.of(".env.example")).contains("DENTAFLOW_MODE"));
    }
    @Test void giuManHinhNhungKhongConBanGhiVaDuLieuNhapMau()throws Exception{
        List<Path> tep=fxml();assertEquals(87,tep.size());
        for(Path p:tep){Document d=doc(p);assertEquals(0,d.getElementsByTagNameNS("*","HashMap").getLength(),p.toString());
            NodeList all=d.getElementsByTagName("*");
            for(int i=0;i<all.getLength();i++){
                Element e=(Element)all.item(i);String tag=e.getLocalName(),text=e.getAttribute("text");
                if(List.of("TextField","TextArea","PasswordField","editor").contains(tag))assertTrue(text.isEmpty(),p+" còn input mẫu");
                if(tag.equals("Label")){
                    assertFalse(text.matches(".*(?:BN|NV|HD|LH|PK|DV)[0-9]{3,}.*"),p+" còn mã bản ghi mẫu");
                    assertFalse(text.matches(".*[0-9][0-9., ]*\\s*₫.*"),p+" còn số tiền mẫu");
                    assertFalse(text.matches(".*(?:Trần Minh Đức|Nguyễn Lan Anh|Nguyễn Văn A|Lê Hoàng Nam).*"),p+" còn tên mẫu");
                }
            }
        }
        assertFalse(Files.exists(Path.of("src/main/resources/images/receptionist/transfer-qr-demo.png")));
    }
    @Test void nutNghiepVuChuaNoiBackendDeuKhoa()throws Exception{
        for(Path p:fxml())if(!p.getParent().getFileName().toString().equals("common")){
            NodeList nut=doc(p).getElementsByTagNameNS("*","Button");
            for(int i=0;i<nut.getLength();i++)assertEquals("true",((Element)nut.item(i)).getAttribute("disable"),p.toString());
        }
    }
    @Test void sqlKhoiTaoVaDanhMucKhongCoLenhDonDuLieuCu()throws Exception{
        for(String name:List.of("01_create_schema.sql","02_seed_reference_data.sql","03_verify_schema.sql")){
            String sql=Files.readString(Path.of("database/sqlserver",name));assertTrue(sql.contains("DENTAFLOW_DB"));assertFalse(sql.contains("NhaKhoa_QuanLy"));
            String noComments=sql.replaceAll("(?s)/\\*.*?\\*/","").replaceAll("(?m)--.*$","");
            assertFalse(noComments.matches("(?is).*\\b(?:DROP\\s+DATABASE|TRUNCATE|DELETE\\s+FROM)\\b.*"));
        }
        String tao=Files.readString(Path.of("database/sqlserver/01_create_schema.sql"));
        assertTrue(tao.contains(":ON ERROR EXIT"));assertTrue(tao.contains("sys.tables"));assertTrue(tao.contains("PBKDF2-SHA256$%"));
    }
}
