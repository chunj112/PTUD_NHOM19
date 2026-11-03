package iuh.se.nhom19.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import iuh.se.nhom19.auth.VaiTro;
import iuh.se.nhom19.dao.DichVuDAO;
import iuh.se.nhom19.entity.DichVu;

public class DichVuService {
    private final DichVuDAO dichVuDAO;

    public DichVuService() {
        this(new DichVuDAO());
    }

    public DichVuService(DichVuDAO dichVuDAO) {
        this.dichVuDAO = Objects.requireNonNull(dichVuDAO);
    }

    public List<DichVu> layDanhSach() throws SQLException {
        return dichVuDAO.layTatCa();
    }

    public List<DichVu> layDichVuDangApDung() throws SQLException {
        return dichVuDAO.layDichVuDangApDung();
    }

    public DichVu timTheoMa(String maDV) throws SQLException {
        if (maDV == null || maDV.isBlank()) return null;
        return dichVuDAO.timTheoMa(maDV.trim());
    }

    public void themDichVu(DichVu dv, VaiTro vaiTro) throws SQLException {
        kiemTraQuyenQuanLy(vaiTro);
        kiemTraDuLieu(dv);

        if (dv.getMaDV() == null || dv.getMaDV().isBlank()) {
            dv.setMaDV(dichVuDAO.sinhMaDichVuMoi());
        } else {
            dv.setMaDV(dv.getMaDV().trim());
            if (dichVuDAO.timTheoMa(dv.getMaDV()) != null) {
                throw new IllegalArgumentException("Mã dịch vụ " + dv.getMaDV() + " đã tồn tại.");
            }
        }

        dichVuDAO.themDichVu(dv);
    }

    public void capNhatDichVu(DichVu dv, VaiTro vaiTro) throws SQLException {
        kiemTraQuyenQuanLy(vaiTro);
        if (dv == null || dv.getMaDV() == null || dv.getMaDV().isBlank()) {
            throw new IllegalArgumentException("Mã dịch vụ không hợp lệ.");
        }
        kiemTraDuLieu(dv);

        if (dichVuDAO.timTheoMa(dv.getMaDV().trim()) == null) {
            throw new IllegalArgumentException("Không tìm thấy dịch vụ cần cập nhật.");
        }

        dichVuDAO.capNhatDichVu(dv);
    }

    public void doiTrangThai(String maDV, boolean dangApDung, VaiTro vaiTro) throws SQLException {
        kiemTraQuyenQuanLy(vaiTro);
        if (maDV == null || maDV.isBlank()) {
            throw new IllegalArgumentException("Mã dịch vụ không hợp lệ.");
        }
        dichVuDAO.doiTrangThai(maDV.trim(), dangApDung);
    }

    public String layMaMoi() throws SQLException {
        return dichVuDAO.sinhMaDichVuMoi();
    }

    public List<DichVu> timKiem(List<DichVu> goc, String tuKhoa, String trangThaiFilter, String nhomFilter) {
        if (goc == null) return new ArrayList<>();
        List<DichVu> ketQua = new ArrayList<>();
        String kw = tuKhoa != null ? tuKhoa.trim().toLowerCase() : "";

        for (DichVu dv : goc) {
            boolean khopTuKhoa = kw.isEmpty()
                    || (dv.getMaDV() != null && dv.getMaDV().toLowerCase().contains(kw))
                    || (dv.getTenDV() != null && dv.getTenDV().toLowerCase().contains(kw));

            boolean khopTrangThai = true;
            if ("Đang hoạt động".equals(trangThaiFilter)) {
                khopTrangThai = dv.isDangApDung();
            } else if ("Ngừng hoạt động".equals(trangThaiFilter)) {
                khopTrangThai = !dv.isDangApDung();
            }

            boolean khopNhom = true;
            if (nhomFilter != null && !nhomFilter.isBlank() && !"Tất cả nhóm dịch vụ".equals(nhomFilter)) {
                khopNhom = dv.getMoTa() != null && dv.getMoTa().toLowerCase().contains(nhomFilter.toLowerCase());
            }

            if (khopTuKhoa && khopTrangThai && khopNhom) {
                ketQua.add(dv);
            }
        }
        return ketQua;
    }

    private void kiemTraQuyenQuanLy(VaiTro vaiTro) {
        if (vaiTro != VaiTro.MANAGER) {
            throw new IllegalArgumentException("Chỉ Quản lý mới có quyền thao tác danh mục dịch vụ.");
        }
    }

    private void kiemTraDuLieu(DichVu dv) {
        if (dv == null) {
            throw new IllegalArgumentException("Dữ liệu dịch vụ không được để trống.");
        }
        if (dv.getTenDV() == null || dv.getTenDV().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên dịch vụ không được để trống.");
        }
        if (dv.getDonViTinh() == null || dv.getDonViTinh().trim().isEmpty()) {
            throw new IllegalArgumentException("Đơn vị tính không được để trống.");
        }
        if (dv.getDonGia() == null || dv.getDonGia().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Đơn giá dịch vụ phải lớn hơn hoặc bằng 0.");
        }
        if (dv.getThoiGianThucHien() != null && dv.getThoiGianThucHien() <= 0) {
            throw new IllegalArgumentException("Thời gian thực hiện phải lớn hơn 0 phút.");
        }
    }
}
