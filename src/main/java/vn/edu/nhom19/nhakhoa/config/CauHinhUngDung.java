package vn.edu.nhom19.nhakhoa.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/** Biến môi trường ưu tiên hơn .env local. Không đọc .env.example hoặc in cấu hình bí mật. */
public final class CauHinhUngDung {
    private CauHinhUngDung() {}
    public static String docGiaTri(String tenBien) {
        String giaTri=System.getenv(tenBien);
        if(giaTri!=null)return giaTri;
        Path tep=Path.of(".env");
        if(!Files.isRegularFile(tep))return null;
        try {
            for(String dong:Files.readAllLines(tep,StandardCharsets.UTF_8)) {
                if(dong.isBlank()||dong.stripLeading().startsWith("#"))continue;
                int viTri=dong.indexOf('=');
                if(viTri<0||!dong.substring(0,viTri).trim().equals(tenBien))continue;
                // Dấu = còn lại thuộc giá trị; không thay escape hoặc ký tự trong mật khẩu.
                giaTri=dong.substring(viTri+1);
                if(giaTri.length()>=2&&((giaTri.startsWith("\"")&&giaTri.endsWith("\""))||
                        (giaTri.startsWith("'")&&giaTri.endsWith("'"))))giaTri=giaTri.substring(1,giaTri.length()-1);
                return giaTri;
            }
            return null;
        } catch(IOException loi) {throw new IllegalStateException("Không đọc được file cấu hình local .env.");}
    }
}
