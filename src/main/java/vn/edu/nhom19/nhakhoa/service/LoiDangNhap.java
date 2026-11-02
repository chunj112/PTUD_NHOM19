package vn.edu.nhom19.nhakhoa.service;

/** Chỉ thông điệp đã kiểm soát được đưa lên Login; không chứa SQL/URL/hash/mật khẩu. */
public final class LoiDangNhap extends RuntimeException {
    public LoiDangNhap(String thongBao){super(thongBao);}
}
