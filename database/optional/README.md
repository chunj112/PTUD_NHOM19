# SQL tùy chọn và bảo trì

Các file trong thư mục này không nằm trong quy trình cài đặt thường ngày. Ba file dùng cho khởi tạo/kiểm tra ở `database/sqlserver/` là:

1. `01_create_schema.sql` – tạo schema cuối cùng trên database mới/rỗng.
2. `02_seed_reference_data.sql` – bổ sung phòng/dịch vụ tham chiếu và phiếu khám hoàn tất mẫu để demo lập hóa đơn.
3. `03_verify_schema.sql` – chỉ đọc để kiểm tra schema và danh mục.

## Thao tác tùy chọn

- `04_phan_cong_phuc_vu_tiep_nhan.sql` – bổ sung ca cho nha sĩ đã có.
- `05_don_du_lieu_kiem_thu.sql` – xem/dọn các fixture kiểm thử theo mã đã định danh.
- `06_cap_nhat_benh_nhan_mau.sql` – đổi tên/SĐT của sáu hồ sơ mẫu cụ thể.
- `07_them_bac_si_mau_va_ca.sql` – thêm nha sĩ demo và phân công ca.
- `08_xoa_toan_bo_lich_va_phieu_kham.sql` – thao tác xóa toàn bộ lịch, phiếu khám và dữ liệu phụ thuộc; chỉ chạy khi chủ động dọn database.

## Migration cũ

`migrations/004_lich_hen_chua_co_benh_nhan.sql` chỉ dùng để nâng cấp database đã tạo từ schema cũ. Database mới chạy `01_create_schema.sql` đã có cấu trúc cuối cùng, không chạy lại migration 004.

`migrations/005_cap_stt_kham_theo_ngay.sql` dùng cho database đã có schema trước khi thêm STT khám. Database mới tạo bằng `01_create_schema.sql` đã có sẵn cấu trúc này nên không chạy migration 005. Migration thêm hai cột STT nullable, chỉ mục duy nhất và bộ đếm ngày; giữ nguyên nội dung phiếu cũ và đồng bộ bộ đếm với STT đã có nếu tồn tại.

Y tá chỉ xem danh sách theo ca/phòng được phân công. Không có bảng trạng thái gọi, lịch sử gọi hoặc nút gọi bệnh nhân. Danh sách đọc trạng thái thật từ `PhieuKham`; lịch có hẹn được đối chiếu theo giờ hẹn, tiếp nhận không hẹn theo giờ tiếp nhận. Chỉ Nha sĩ được chuyển phiếu từ `CHO_KHAM` sang `DANG_KHAM` và `DA_KHAM`.

Các script tùy chọn không tự chạy khi ứng dụng khởi động. Trước khi chạy script có UPDATE/DELETE, đọc nội dung, kiểm tra database đích và sao lưu dữ liệu cần giữ.
