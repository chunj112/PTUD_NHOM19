package iuh.se.nhom19.app;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import iuh.se.nhom19.auth.VaiTro;
import iuh.se.nhom19.auth.PhienDangNhap;
import iuh.se.nhom19.view.common.KhungUngDungController;
import iuh.se.nhom19.view.common.DangNhapController;
import iuh.se.nhom19.auth.KetQuaDangNhap;
import iuh.se.nhom19.service.DangNhapService;
import iuh.se.nhom19.database.KetNoiCSDL;

/** Giữ một Stage/Scene, chỉ mở không gian làm việc sau xác thực tài khoản SQL. */
public final class DieuHuongUngDung implements AutoCloseable {
    private final Stage cuaSo;
    private final PhienDangNhap phienDangNhap;
    private DangNhapController dieuKhienDangNhap;
    private final DangNhapService dichVuDangNhap;
    private KhungUngDungController dieuKhienKhungUngDung;
    private boolean daDong;
    private final KetNoiCSDL ketNoiNghiepVu;

    public DieuHuongUngDung(Stage cuaSo, PhienDangNhap phienDangNhap) {
        this(cuaSo,phienDangNhap,new DangNhapService());
    }
    /** Cho phép cấp Service khi kiểm thử; ứng dụng mặc định luôn dùng Service/DAO SQL thật. */
    public DieuHuongUngDung(Stage cuaSo, PhienDangNhap phienDangNhap, DangNhapService dichVuDangNhap) {
        this(cuaSo, phienDangNhap, dichVuDangNhap, new KetNoiCSDL());
    }

    /** Test có thể cấp kết nối giả lỗi để không truy cập SQL local ngoài ý muốn. Login/PBKDF2 giữ nguyên. */
    public DieuHuongUngDung(Stage cuaSo, PhienDangNhap phienDangNhap, DangNhapService dichVuDangNhap, KetNoiCSDL ketNoiNghiepVu) {
        if (cuaSo == null || phienDangNhap == null) {
            throw new IllegalArgumentException("Thiếu cửa sổ hoặc Session.");
        }
        this.cuaSo = cuaSo;
        this.phienDangNhap = phienDangNhap;
        this.dichVuDangNhap=java.util.Objects.requireNonNull(dichVuDangNhap);
        this.ketNoiNghiepVu = java.util.Objects.requireNonNull(ketNoiNghiepVu);
    }

    public void khoiDong() throws IOException {
        if (daDong) throw new IllegalStateException("Bộ điều hướng đã đóng.");
        hienThiDangNhap();
    }

    private FXMLLoader taoBoNapFXML(String duongDan) throws IOException {
        var taiNguyen = getClass().getResource(duongDan);
        if (taiNguyen == null) {
            throw new IOException("Thiếu FXML: " + duongDan);
        }
        return new FXMLLoader(taiNguyen);
    }

    private void ganGiaoDien(Parent nutGoc, String tieuDe) {
        if (cuaSo.getScene() == null) {
            Scene khungCanh = new Scene(nutGoc, 1280, 800);
            khungCanh.getStylesheets().add(getClass().getResource("/css/chu-de.css").toExternalForm());
            cuaSo.setScene(khungCanh);
        } else {
            cuaSo.getScene().setRoot(nutGoc);
        }
        capNhatTieuDe(tieuDe);
    }

    /** Tiêu đề không chứa danh tính hoặc thông tin xác thực. */
    public void capNhatTieuDe(String tieuDeTrang) {
        cuaSo.setTitle("DentaFlow · "+tieuDeTrang);
    }

    private void hienThiDangNhap() throws IOException {
        // Bước 1: xóa phiên trước. Nếu load Login lỗi, cũng không giữ dữ liệu AppShell.
        phienDangNhap.xoa();
        if (dieuKhienKhungUngDung != null) {
            dieuKhienKhungUngDung.giaiPhong();
            dieuKhienKhungUngDung = null;
        }
        if (dieuKhienDangNhap != null) {
            dieuKhienDangNhap.giaiPhong();
            dieuKhienDangNhap = null;
        }
        if (cuaSo.getScene() != null) {
            cuaSo.getScene().setRoot(new StackPane());
        }
        FXMLLoader boNapFXML = taoBoNapFXML("/fxml/common/dang-nhap.fxml");
        Parent nutGoc = boNapFXML.load();
        dieuKhienDangNhap = boNapFXML.getController();
        dieuKhienDangNhap.cauHinh(this, dichVuDangNhap);
        ganGiaoDien(nutGoc, "Đăng nhập");
    }

    /** Không nhận Role hoặc kết quả do bên gọi tự tạo; lấy từ form đã xác thực. */
    public void moSauDangNhap() {
        if (daDong || phienDangNhap.dangHoatDong() || dieuKhienDangNhap == null) {
            throw new IllegalStateException("Phiên đăng nhập không còn sẵn sàng.");
        }
        KetQuaDangNhap ketQua=dieuKhienDangNhap.layKetQuaDaXacThuc();
        if (ketQua == null) {
            throw new IllegalStateException("Chưa xác thực tài khoản.");
        }
        phienDangNhap.batDauPhienDangNhap(ketQua);
        moKhungUngDung();
    }

    private void moKhungUngDung() {
        VaiTro vaiTro=phienDangNhap.yeuCauVaiTro(phienDangNhap.layPhienBan());
        try {
            FXMLLoader boNapFXML = taoBoNapFXML("/fxml/common/khung-ung-dung.fxml");
            Parent nutGoc = boNapFXML.load();
            dieuKhienKhungUngDung = boNapFXML.getController();
            dieuKhienKhungUngDung.cauHinh(phienDangNhap, this, ketNoiNghiepVu);
            ganGiaoDien(nutGoc, vaiTro.layTenHienThi() + " / Dashboard");
            if (dieuKhienDangNhap != null) {
                dieuKhienDangNhap.giaiPhong();
                dieuKhienDangNhap = null;
            }
        } catch (IOException | RuntimeException loi) {
            phienDangNhap.xoa();
            if (dieuKhienKhungUngDung != null) {
                dieuKhienKhungUngDung.giaiPhong();
                dieuKhienKhungUngDung = null;
            }
            throw new IllegalStateException("Không thể nạp giao diện sau đăng nhập", loi);
        }
    }

    public void dangXuat() {
        if (daDong) {
            return;
        }
        try {
            hienThiDangNhap();
        } catch (IOException loi) {
            ganGiaoDien(new StackPane(new Label("Không thể mở Login. Hãy đóng và mở lại DentaFlow.")),
                    "Lỗi tài nguyên");
        }
    }

    @Override public void close() {
        daDong = true;
        phienDangNhap.xoa();
        if (dieuKhienDangNhap != null) {
            dieuKhienDangNhap.giaiPhong();
            dieuKhienDangNhap = null;
        }
        if (dieuKhienKhungUngDung != null) {
            dieuKhienKhungUngDung.giaiPhong();
            dieuKhienKhungUngDung = null;
        }
    }
}
