# Sơ đồ Use Case tổng quát – DentaFlow hiện tại

Sơ đồ này phản ánh các vai trò và màn hình/luồng đã có trong source hiện tại. Các chức năng có giao diện nhưng backend chưa hoàn chỉnh được đánh dấu **UI** để không nhầm là nghiệp vụ đã chạy đầy đủ.

```mermaid
flowchart LR
    QL[Quản lý]
    LT[Lễ tân]
    NS[Nha sĩ]
    YT[Y tá]

    subgraph DF[Hệ thống DentaFlow]
        DangNhap([Đăng nhập và đăng xuất])
        Dashboard([Xem Dashboard theo vai trò])
        NhanSu([Quản lý nhân sự và tài khoản · UI])
        PhanCong([Quản lý lịch phân công · UI])
        DichVu([Quản lý dịch vụ · UI])
        BaoCao([Xem báo cáo · UI])
        DatLich([Đặt, sửa và hủy lịch hẹn])
        TraCuuBN([Tra cứu và tạo hồ sơ bệnh nhân])
        TiepNhanHen([Tiếp nhận bệnh nhân có hẹn])
        TiepNhanVang([Tiếp nhận bệnh nhân không hẹn])
        LichHen([Xem lịch hẹn ngày và tuần])
        LichCaNhan([Xem lịch làm việc cá nhân])
        ChoKham([Xem danh sách chờ khám])
        KhamBenh([Bắt đầu khám và hoàn tất khám])
        ChoYTa([Xem danh sách chờ theo phòng/ca · chỉ đọc])
        ThuNgan([Thu ngân, hóa đơn và thanh toán · UI])
        TaiKhoan([Xem hồ sơ cá nhân và đổi mật khẩu · UI/route])
    end

    QL --- DangNhap
    QL --- Dashboard
    QL --- NhanSu
    QL --- PhanCong
    QL --- DichVu
    QL --- BaoCao
    QL --- TaiKhoan

    LT --- DangNhap
    LT --- Dashboard
    LT --- DatLich
    LT --- TraCuuBN
    LT --- TiepNhanHen
    LT --- TiepNhanVang
    LT --- LichHen
    LT --- LichCaNhan
    LT --- ThuNgan
    LT --- TaiKhoan

    NS --- DangNhap
    NS --- Dashboard
    NS --- LichCaNhan
    NS --- ChoKham
    NS --- KhamBenh
    NS --- TaiKhoan

    YT --- DangNhap
    YT --- Dashboard
    YT --- LichCaNhan
    YT --- ChoYTa
    YT --- TaiKhoan

    TiepNhanHen -. "tạo phiếu khám CHO_KHAM" .-> ChoKham
    TiepNhanVang -. "tạo phiếu khám CHO_KHAM" .-> ChoKham
    ChoKham -. "Nha sĩ xử lý phiếu" .-> KhamBenh

    classDef implemented fill:#e8f5f2,stroke:#087e91,color:#173042;
    classDef ui fill:#fff5df,stroke:#c58a18,color:#173042;
    class DangNhap,DatLich,TraCuuBN,TiepNhanHen,TiepNhanVang,LichHen,LichCaNhan,ChoKham,KhamBenh,ChoYTa implemented;
    class NhanSu,PhanCong,DichVu,BaoCao,ThuNgan,TaiKhoan ui;
```

## Ghi chú phạm vi

- Sơ đồ gom nhóm theo màn hình/luồng hiện có trong source; chưa đánh số UC để tránh nhầm với 14 UC chính thức trong báo cáo môn học. Cần đối chiếu bảng UC chính thức trước khi chèn vào báo cáo.
- Đăng nhập SQL thật, đặt/sửa/hủy lịch, tra cứu/tạo hồ sơ bệnh nhân, tiếp nhận, cấp số khám, danh sách chờ Nha sĩ/Y tá và cập nhật trạng thái khám đã có lớp xử lý trong source.
- Nhân sự, CRUD phân công, dịch vụ, báo cáo, thu ngân/hóa đơn/thanh toán và một số màn hình tài khoản hiện mới có route/UI hoặc chưa có bộ Controller–Service–DAO đầy đủ; sơ đồ đánh dấu UI.
- Y tá chỉ xem hàng chờ. Việc gọi bệnh nhân thực hiện ngoài phần mềm; Y tá không đổi trạng thái phiếu khám.
- Dashboard được ánh xạ theo vai trò trong điều hướng. Nội dung dữ liệu thực tế tùy từng màn hình và mức backend đã nối.
