package vn.edu.nhom19.nhakhoa;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import vn.edu.nhom19.nhakhoa.auth.BaoMatMatKhau;

class BaoMatMatKhauTest {
    @Test void dungSaiVaSaltRieng(){
        String matKhau=UUID.randomUUID().toString();String a=BaoMatMatKhau.bamMatKhau(matKhau),b=BaoMatMatKhau.bamMatKhau(matKhau);
        assertTrue(BaoMatMatKhau.kiemTraMatKhau(matKhau,a));assertFalse(BaoMatMatKhau.kiemTraMatKhau(UUID.randomUUID().toString(),a));
        assertNotEquals(a,b);assertTrue(a.length()<=255);assertTrue(a.startsWith("PBKDF2-SHA256$600000$"));
    }
    @Test void vectorDocLapTuPythonHashlib(){
        // Vector kiểm thử cố định, không phải mật khẩu/tài khoản được tạo trong SQL.
        assertTrue(BaoMatMatKhau.kiemTraMatKhau("vector UC001 khong phai tai khoan","PBKDF2-SHA256$600000$MTIzNDU2Nzg5MGFiY2RlZg==$RMkHHvByHtcsyg5ujv/jv9HwaL6rGniGDJ2EUisy7zk="));
    }
    @Test void dinhDangHongKhongNemDuLieuBiMat(){
        for(String bam:new String[]{"", "plaintext", "PBKDF2-SHA256$1$AA==$AA==", "PBKDF2-SHA256$999999999$AA==$AA==", "PBKDF2-SHA256$x$AA==$AA==", "PBKDF2-SHA256$600000$?$?", "PBKDF2-SHA256$600000$AA==$AA==", "x".repeat(256)})
            assertFalse(BaoMatMatKhau.kiemTraMatKhau("unit",bam));
        assertFalse(BaoMatMatKhau.kiemTraMatKhau("unit",null));assertFalse(BaoMatMatKhau.kiemTraMatKhau(null,"unit"));
    }
    @Test void khongTrimMatKhau(){
        String matKhau=" "+UUID.randomUUID()+" ";String bam=BaoMatMatKhau.bamMatKhau(matKhau);
        assertTrue(BaoMatMatKhau.kiemTraMatKhau(matKhau,bam));assertFalse(BaoMatMatKhau.kiemTraMatKhau(matKhau.trim(),bam));
    }
}
