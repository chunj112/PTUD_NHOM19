package iuh.se.nhom19.auth;

import java.security.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** PBKDF2-SHA256$soVong$saltBase64$hashBase64, dài dưới VARCHAR(255). */
public final class BaoMatMatKhau {
    public static final int SO_VONG_LAP=600_000;
    private static final SecureRandom NGAU_NHIEN=new SecureRandom();
    private BaoMatMatKhau() {}
    public static String bamMatKhau(String matKhau) {
        if(matKhau==null||matKhau.isBlank()||matKhau.length()>1024)throw new IllegalArgumentException("Mật khẩu không hợp lệ.");
        byte[] muoi=new byte[16];NGAU_NHIEN.nextBytes(muoi);
        // Hash là kết quả dẫn xuất một chiều. Salt riêng ngăn hai tài khoản cùng mật khẩu có cùng hash.
        byte[] bam=danXuat(matKhau,muoi,SO_VONG_LAP);
        try {return "PBKDF2-SHA256$"+SO_VONG_LAP+"$"+Base64.getEncoder().encodeToString(muoi)+"$"+Base64.getEncoder().encodeToString(bam);}
        finally {Arrays.fill(bam,(byte)0);Arrays.fill(muoi,(byte)0);}
    }
    public static boolean kiemTraMatKhau(String matKhau,String chuoiDaLuu) {
        if(matKhau==null||matKhau.isEmpty()||matKhau.length()>1024||chuoiDaLuu==null||chuoiDaLuu.length()>255)return false;
        byte[] muoi=null,bamDaLuu=null,bamThu=null;
        try {
            String[] phan=chuoiDaLuu.split("\\$",-1);
            if(phan.length!=4||!phan[0].equals("PBKDF2-SHA256"))return false;
            int soVong=Integer.parseInt(phan[1]);
            // Không chấp nhận định dạng yếu hoặc số vòng tùy ý làm treo tiến trình khi DB bị sửa sai.
            if(soVong<SO_VONG_LAP||soVong>2_000_000)return false;
            muoi=Base64.getDecoder().decode(phan[2]);bamDaLuu=Base64.getDecoder().decode(phan[3]);
            if(muoi.length!=16||bamDaLuu.length!=32)return false;
            bamThu=danXuat(matKhau,muoi,soVong);
            // Không so sánh plaintext với SQL; so hai mảng hash cùng độ dài bằng phép so thời gian ổn định.
            return MessageDigest.isEqual(bamDaLuu,bamThu);
        } catch(IllegalArgumentException loi) {return false;}
        finally {if(muoi!=null)Arrays.fill(muoi,(byte)0);if(bamDaLuu!=null)Arrays.fill(bamDaLuu,(byte)0);if(bamThu!=null)Arrays.fill(bamThu,(byte)0);}
    }
    private static byte[] danXuat(String matKhau,byte[] muoi,int soVong) {
        char[] kyTu=matKhau.toCharArray();PBEKeySpec yeuCau=new PBEKeySpec(kyTu,muoi,soVong,256);
        try {return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(yeuCau).getEncoded();}
        catch(GeneralSecurityException loi) {throw new IllegalStateException("Java không hỗ trợ thuật toán bảo mật mật khẩu cần thiết.");}
        finally {yeuCau.clearPassword();Arrays.fill(kyTu,'\0');}
    }
}
