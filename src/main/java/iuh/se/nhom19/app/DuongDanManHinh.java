package iuh.se.nhom19.app;

import java.util.Set;
import iuh.se.nhom19.auth.VaiTro;

/** Định tuyến theo vai trò; các Service nghiệp vụ vẫn phải kiểm tra quyền.
 * Các đường dẫn chưa có được hiển thị bằng một FXML placeholder chung. */
public enum DuongDanManHinh {
    TONG_QUAN("Dashboard", "btnTongQuan", null, VaiTro.values()),
    NHAN_SU("Nhân sự", "btnNhanSu", "/fxml/manager/nhan-su.fxml", VaiTro.MANAGER),
    PHAN_CONG("Lịch & Phân công", "btnPhanCong", "/fxml/manager/phan-cong.fxml", VaiTro.MANAGER),
    DICH_VU("Dịch vụ", "btnDichVu", "/fxml/manager/dich-vu.fxml", VaiTro.MANAGER),
    BAO_CAO("Báo cáo", "btnBaoCao", "/fxml/manager/bao-cao.fxml", VaiTro.MANAGER),
    TIEP_NHAN("Tiếp nhận & Lịch hẹn", "btnTrangTiepNhanLichHen", "/fxml/receptionist/tiep-nhan-lich-hen.fxml", VaiTro.RECEPTIONIST),
    THU_NGAN("Thu ngân", "btnThuNgan", "/fxml/receptionist/thu-ngan.fxml", VaiTro.RECEPTIONIST),
    BENH_NHAN("Tra cứu", "btnTraCuuBenhNhan", "/fxml/receptionist/tra-cuu-benh-nhan.fxml", VaiTro.RECEPTIONIST),
    LICH_LE_TAN("Lịch làm việc", "btnLichLeTan", "/fxml/receptionist/lich-lam-viec.fxml", VaiTro.RECEPTIONIST),
    KHAM_BENH("Khám bệnh", "btnKhamBenh", "/fxml/dentist/kham-benh.fxml", VaiTro.DENTIST),
    LICH_NHA_SI("Lịch làm việc", "btnLichNhaSi", "/fxml/dentist/lich-lam-viec.fxml", VaiTro.DENTIST),
    LICH_Y_TA("Lịch làm việc", "btnLichYTa", "/fxml/nurse/lich-lam-viec.fxml", VaiTro.NURSE),
    THONG_TIN_CA_NHAN("Hồ sơ cá nhân", null, "/fxml/common/thong-tin-ca-nhan.fxml", VaiTro.values()),
    MAT_KHAU("Đổi mật khẩu", null, "/fxml/common/doi-mat-khau.fxml", VaiTro.values());

    private final String tieuDe;
    private final String maNut;
    private final String duongDan;
    private final Set<VaiTro> cacVaiTro;

    DuongDanManHinh(String tieuDe, String maNut, String duongDan, VaiTro... cacVaiTro) {
        this.tieuDe = tieuDe; this.maNut = maNut; this.duongDan = duongDan; this.cacVaiTro = Set.of(cacVaiTro);
    }
    public String layTieuDe() { return tieuDe; }
    public String layMaNut() { return maNut; }
    public boolean choPhep(VaiTro vaiTro) { return cacVaiTro.contains(vaiTro); }

    public String layDuongDanFXML(VaiTro vaiTro) {
        return this == TONG_QUAN ? "/fxml/" + vaiTro.name().toLowerCase(java.util.Locale.ROOT) + "/tong-quan.fxml" : duongDan;
    }
}
