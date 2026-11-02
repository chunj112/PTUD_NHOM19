package vn.edu.nhom19.nhakhoa.service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import vn.edu.nhom19.nhakhoa.auth.*;
import vn.edu.nhom19.nhakhoa.entity.DemoData.*;
import vn.edu.nhom19.nhakhoa.entity.DemoData.Shift;

/**
 * Nghiệp vụ DEMO trong bộ nhớ, tạo riêng cho mỗi phiên Login.
 * Không JDBC, không tài khoản thật, không ghi nhận đã thu tiền thật.
 * Mọi thay đổi được kiểm tra trước rồi mới cập nhật danh sách; thay lớp này
 * bằng Service/DAO có transaction khi nhóm cấu hình SQL Server.
 */
public final class DemoClinicService {
    public static final LocalDate DEMO_DATE = LocalDate.of(2026, 10, 9);
    private final UserSession session;
    private final long version;
    private final ArrayList<Employee> employees = new ArrayList<>();
    private final ArrayList<Patient> patients = new ArrayList<>();
    private final ArrayList<DentalService> services = new ArrayList<>();
    private final ArrayList<Assignment> assignments = new ArrayList<>();
    private final ArrayList<Appointment> appointments = new ArrayList<>();
    private final ArrayList<Exam> exams = new ArrayList<>();
    private final ArrayList<Invoice> invoices = new ArrayList<>();
    private int sequence = 100;
    private boolean disposed;

    public DemoClinicService(UserSession session) {
        this.session = Objects.requireNonNull(session);
        version = session.version();
        session.requireRole(version);
        seed();
    }

    public Role role() {
        if (disposed) throw new IllegalStateException("Phiên dữ liệu DEMO đã đóng.");
        return session.requireRole(version);
    }

    public void require(Role... allowed) {
        Role current = role();
        for (Role role : allowed) if (role == current) return;
        throw new IllegalArgumentException("Vai trò hiện tại không được thực hiện thao tác này.");
    }

    public String currentEmployeeId() {
        switch (role()) {
            case MANAGER: return "NV001";
            case RECEPTIONIST: return "NV004";
            case DENTIST: return "NV002";
            case NURSE: return "NV005";
            default: throw new IllegalStateException("Vai trò không hợp lệ.");
        }
    }

    public List<Employee> employees() { role(); return List.copyOf(employees); }
    public List<DentalService> services() {
        require(Role.MANAGER, Role.DENTIST, Role.RECEPTIONIST); return List.copyOf(services);
    }
    public List<Patient> patients() {
        require(Role.MANAGER, Role.RECEPTIONIST, Role.DENTIST); return List.copyOf(patients);
    }
    public List<Appointment> appointments() {
        require(Role.MANAGER, Role.RECEPTIONIST, Role.DENTIST); return List.copyOf(appointments);
    }
    public List<Exam> exams() {
        require(Role.MANAGER, Role.RECEPTIONIST, Role.DENTIST);
        ArrayList<Exam> result = new ArrayList<>();
        for (Exam exam : exams) {
            if (role() != Role.DENTIST || exam.dentistId.equals(currentEmployeeId())) result.add(exam);
        }
        return result;
    }
    public List<Invoice> invoices() {
        require(Role.MANAGER, Role.RECEPTIONIST); return List.copyOf(invoices);
    }
    public List<Assignment> assignments() {
        ArrayList<Assignment> result = new ArrayList<>();
        for (Assignment item : assignments) {
            if (role() == Role.MANAGER || item.employeeId.equals(currentEmployeeId())) result.add(item);
        }
        return result;
    }
    public List<Employee> dentists() {
        ArrayList<Employee> result = new ArrayList<>();
        for (Employee employee : employees) if (employee.role == Role.DENTIST && employee.active) result.add(employee);
        return result;
    }

    public Employee employee(String id) {
        role();
        for (Employee item : employees) if (item.id.equals(id)) return item;
        throw new IllegalArgumentException("Không tìm thấy nhân viên.");
    }
    public Patient patient(String id) {
        require(Role.MANAGER, Role.RECEPTIONIST, Role.DENTIST);
        for (Patient item : patients) if (item.id.equals(id)) return item;
        throw new IllegalArgumentException("Không tìm thấy bệnh nhân.");
    }
    public Exam exam(String id) {
        for (Exam item : exams()) if (item.id.equals(id)) return item;
        throw new IllegalArgumentException("Không tìm thấy phiếu khám được phép truy cập.");
    }
    public DentalService dentalService(String id) {
        for (DentalService item : services()) if (item.id.equals(id)) return item;
        throw new IllegalArgumentException("Không tìm thấy dịch vụ.");
    }
    public String nextId(String prefix) { role(); return prefix + (++sequence); }

    public static String required(String value, String name, int max) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(name + " không được để trống.");
        if (value.trim().length() > max) throw new IllegalArgumentException(name + " tối đa " + max + " ký tự.");
        return value.trim();
    }
    private void validatePerson(String name, LocalDate birthday, String phone, boolean adult) {
        required(name, "Họ tên", 120);
        if (birthday == null || birthday.isBefore(LocalDate.of(1900, 1, 1))
                || birthday.isAfter(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")))) {
            throw new IllegalArgumentException("Ngày sinh không hợp lệ.");
        }
        if (adult && birthday.plusYears(18).isAfter(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")))) {
            throw new IllegalArgumentException("Nhân viên phải đủ 18 tuổi.");
        }
        if (phone == null || !phone.matches("[0-9]{10,11}")) throw new IllegalArgumentException("SĐT phải gồm 10–11 chữ số.");
    }

    public void saveEmployee(Employee item, boolean creating) {
        require(Role.MANAGER);
        validateEmployee(item);
        if (!creating && item.id.equals(currentEmployeeId()) && (!item.active || item.role != Role.MANAGER)) {
            throw new IllegalArgumentException("Không tự vô hiệu hóa hoặc đổi vai trò của phiên quản lý đang dùng.");
        }
        int index = -1;
        for (int i = 0; i < employees.size(); i++) if (employees.get(i).id.equals(item.id)) index = i;
        if (creating && index >= 0) throw new IllegalArgumentException("Mã nhân viên đã tồn tại.");
        if (!creating && index < 0) throw new IllegalArgumentException("Nhân viên không còn tồn tại.");
        if (creating) employees.add(item); else employees.set(index, item);
    }
    private void validateEmployee(Employee item) {
        validatePerson(item.name, item.birthday, item.phone, true);
        if (item.email.length() > 254 || item.address.length() > 255) throw new IllegalArgumentException("Email/địa chỉ vượt độ dài schema.");
        if (item.role == null) throw new IllegalArgumentException("Chọn chức vụ.");
        if (!item.email.isBlank() && !item.email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new IllegalArgumentException("Email không hợp lệ.");
        }
        for (Employee old : employees) if (!old.id.equals(item.id) && !item.email.isBlank()
                && old.email.equalsIgnoreCase(item.email)) throw new IllegalArgumentException("Email đã được sử dụng.");
    }
    public void saveProfile(Employee item) {
        role();
        Employee old = employee(currentEmployeeId());
        if (!item.id.equals(old.id) || item.role != old.role || item.active != old.active) {
            throw new IllegalArgumentException("Không được đổi mã, chức vụ hoặc trạng thái trong hồ sơ cá nhân.");
        }
        validateEmployee(item);
        employees.set(employees.indexOf(old), item);
    }

    public void saveService(DentalService item, boolean creating) {
        require(Role.MANAGER);
        required(item.name, "Tên dịch vụ", 150);
        required(item.unit, "Đơn vị", 30);
        if (item.price == null || item.price.signum() < 0 || item.price.precision() - item.price.scale() > 16 || item.price.scale() > 2) {
            throw new IllegalArgumentException("Đơn giá phải không âm, tối đa DECIMAL(18,2).");
        }
        if (item.description.length() > 1000) throw new IllegalArgumentException("Mô tả tối đa 1000 ký tự.");
        if (item.duration <= 0) throw new IllegalArgumentException("Thời gian thực hiện phải lớn hơn 0.");
        int index = -1;
        for (int i = 0; i < services.size(); i++) if (services.get(i).id.equals(item.id)) index = i;
        if (creating && index >= 0) throw new IllegalArgumentException("Mã dịch vụ đã tồn tại.");
        if (!creating && index < 0) throw new IllegalArgumentException("Không tìm thấy dịch vụ.");
        if (creating) services.add(item); else services.set(index, item);
    }

    public void saveAssignment(Assignment item, boolean creating) {
        require(Role.MANAGER);
        if (!employee(item.employeeId).active) throw new IllegalArgumentException("Nhân viên đã ngừng làm việc.");
        validateHours(item.date, item.start, item.end);
        boolean morning = !item.start.isBefore(LocalTime.of(8, 0)) && !item.end.isAfter(LocalTime.NOON);
        boolean afternoon = !item.start.isBefore(LocalTime.of(13, 30)) && !item.end.isAfter(LocalTime.of(17, 0));
        if (!morning && !afternoon) throw new IllegalArgumentException("Ca phải trong 08:00–12:00 hoặc 13:30–17:00.");
        item.shift = morning ? Shift.SANG : Shift.CHIEU;
        required(item.room, "Phòng", 20);
        if (!List.of("P01", "P02").contains(item.room)) throw new IllegalArgumentException("Phòng không tồn tại trong dữ liệu DEMO.");
        if (item.work.length() > 300) throw new IllegalArgumentException("Nội dung phân công tối đa 300 ký tự.");
        for (Assignment other : assignments) {
            if (!other.id.equals(item.id) && other.active && other.employeeId.equals(item.employeeId)
                    && other.date.equals(item.date) && (other.shift == item.shift
                    || overlaps(item.start, item.end, other.start, other.end))) {
                throw new IllegalArgumentException("Nhân viên đã có phân công hiệu lực cùng ngày/ca hoặc trùng giờ.");
            }
        }
        // Không kiểm tra duy nhất theo phòng: nhiều nhân viên được cùng phòng/cùng ca.
        int index = -1;
        for (int i = 0; i < assignments.size(); i++) if (assignments.get(i).id.equals(item.id)) index = i;
        if (creating && index >= 0) throw new IllegalArgumentException("Mã phân công trùng.");
        if (!creating && index < 0) throw new IllegalArgumentException("Phân công không còn tồn tại.");
        if (!creating) {
            Assignment old = assignments.get(index);
            for (Appointment a : appointments) {
                if (a.dentistId.equals(old.employeeId) && a.date.equals(old.date) && !a.status.equals("DA_HUY")
                        && covers(old, a.start, a.end) && !(item.employeeId.equals(old.employeeId)
                        && item.date.equals(old.date) && covers(item, a.start, a.end))) {
                    throw new IllegalArgumentException("Phân công đang có lịch hẹn; cần xử lý lịch hẹn trước.");
                }
            }
        }
        if (creating) assignments.add(item); else assignments.set(index, item);
    }
    public void cancelAssignment(Assignment item) {
        require(Role.MANAGER);
        if (!assignments.contains(item) || !item.active) throw new IllegalArgumentException("Phân công đã hủy hoặc không tồn tại.");
        for (Appointment a : appointments) if (a.dentistId.equals(item.employeeId) && a.date.equals(item.date)
                && !a.status.equals("DA_HUY") && !a.status.equals("DA_DEN") && covers(item, a.start, a.end)) {
            throw new IllegalArgumentException("Ca còn lịch hẹn chưa tiếp nhận; xử lý lịch hẹn trước khi hủy.");
        }
        item.active = false; // Hủy trạng thái, không xóa bản ghi.
    }
    private static void validateHours(LocalDate day, LocalTime from, LocalTime to) {
        if (day == null || from == null || to == null || !from.isBefore(to)) {
            throw new IllegalArgumentException("Chọn ngày và khoảng giờ bắt đầu/kết thúc hợp lệ.");
        }
    }
    private static boolean overlaps(LocalTime a, LocalTime b, LocalTime c, LocalTime d) {
        return a.isBefore(d) && c.isBefore(b);
    }
    private static boolean covers(Assignment assignment, LocalTime start, LocalTime end) {
        return assignment.active && !start.isBefore(assignment.start) && !end.isAfter(assignment.end);
    }
    public String roomFor(String dentistId, LocalDate day, LocalTime start, LocalTime end) {
        role();
        for (Assignment a : assignments) if (a.employeeId.equals(dentistId) && a.date.equals(day) && covers(a, start, end)) return a.room;
        throw new IllegalArgumentException("Ngoài ca phân công của nha sĩ.");
    }

    public void savePatient(Patient item, boolean creating) {
        require(Role.RECEPTIONIST);
        validatePerson(item.name, item.birthday, item.phone, false);
        if (item.address.length() > 255 || item.allergy.length() > 1000) throw new IllegalArgumentException("Địa chỉ/dị ứng vượt độ dài schema.");
        int index = -1;
        for (int i = 0; i < patients.size(); i++) if (patients.get(i).id.equals(item.id)) index = i;
        if (creating && index >= 0) throw new IllegalArgumentException("Mã bệnh nhân đã tồn tại.");
        if (!creating && index < 0) throw new IllegalArgumentException("Không tìm thấy bệnh nhân.");
        if (creating) patients.add(item); else patients.set(index, item);
    }
    public void saveAppointment(Appointment item, boolean creating) {
        require(Role.RECEPTIONIST);
        patient(item.patientId);
        if (item.reason.length() > 500 || item.notes.length() > 1000) throw new IllegalArgumentException("Lý do tối đa 500, ghi chú tối đa 1000 ký tự.");
        Employee dentist = employee(item.dentistId);
        if (dentist.role != Role.DENTIST || !dentist.active) throw new IllegalArgumentException("Chọn nha sĩ đang làm việc.");
        validateHours(item.date, item.start, item.end);
        roomFor(item.dentistId, item.date, item.start, item.end);
        for (Appointment old : appointments) {
            if (!old.id.equals(item.id) && old.dentistId.equals(item.dentistId) && old.date.equals(item.date)
                    && !old.status.equals("DA_HUY") && overlaps(item.start, item.end, old.start, old.end)) {
                throw new IllegalArgumentException("Nha sĩ đã có lịch hẹn trùng giờ.");
            }
        }
        int index = -1;
        for (int i = 0; i < appointments.size(); i++) if (appointments.get(i).id.equals(item.id)) index = i;
        if (creating && index >= 0) throw new IllegalArgumentException("Mã lịch hẹn trùng.");
        if (!creating && (index < 0 || appointments.get(index).status.equals("DA_DEN") || appointments.get(index).status.equals("DA_HUY"))) {
            throw new IllegalArgumentException("Không được sửa lịch đã tiếp nhận/hủy.");
        }
        if (creating) appointments.add(item); else appointments.set(index, item);
    }
    public void cancelAppointment(Appointment item) { cancelAppointment(item, ""); }
    public void cancelAppointment(Appointment item, String reason) {
        require(Role.RECEPTIONIST);
        if (!appointments.contains(item) || item.status.equals("DA_DEN") || item.status.equals("DA_HUY")) {
            throw new IllegalArgumentException("Lịch đã tiếp nhận/hủy hoặc không còn tồn tại.");
        }
        String note = reason.isBlank() ? item.notes : item.notes + "\nLý do hủy: " + reason.trim();
        if (note.length() > 1000) throw new IllegalArgumentException("Ghi chú hủy tối đa 1000 ký tự.");
        item.notes = note;
        item.status = "DA_HUY";
    }

    /** Tiếp nhận DEMO là một thay đổi nhất quán trong bộ nhớ.
     * Khi thay bằng DAO: INSERT PhieuKham và UPDATE LichHen phải cùng transaction,
     * commit khi cả hai thành công; rollback nếu một bước lỗi.
     */
    public Exam admit(String patientId, String dentistId, LocalDate day, LocalTime start,
            String reason, Appointment appointment) {
        require(Role.RECEPTIONIST);
        patient(patientId);
        String room = roomFor(dentistId, day, start, start.plusMinutes(1));
        if (appointment != null) {
            if (!appointments.contains(appointment) || appointment.status.equals("DA_HUY")) throw new IllegalArgumentException("Lịch hẹn không hợp lệ.");
            for (Exam old : exams) if (appointment.id.equals(old.appointmentId)) return old;
            if (!appointment.patientId.equals(patientId) || !appointment.dentistId.equals(dentistId) || !appointment.date.equals(day)) {
                throw new IllegalArgumentException("Thông tin tiếp nhận không khớp lịch hẹn.");
            }
        }
        for (Exam old : exams) if (old.patientId.equals(patientId) && old.dentistId.equals(dentistId)
                && old.received.toLocalDate().equals(day) && !old.status.equals("DA_KHAM")) {
            if (appointment == null) return old;
            throw new IllegalArgumentException("Bệnh nhân đã tiếp nhận ở phiếu " + old.id + ". Không tạo phiếu trùng.");
        }
        Exam result = new Exam(nextId("PK"), patientId, dentistId, currentEmployeeId(),
            appointment == null ? null : appointment.id, room, day.atTime(start), reason,
            "CHO_KHAM", "", "", "", "", "");
        exams.add(result);
        if (appointment != null) appointment.status = "DA_DEN";
        return result;
    }

    public void startExam(Exam exam) {
        require(Role.DENTIST); ownExam(exam);
        if (exam.status.equals("DA_KHAM")) throw new IllegalArgumentException("Phiếu đã hoàn tất, chỉ đọc.");
        exam.status = "DANG_KHAM";
    }
    private void ownExam(Exam exam) {
        if (!exams.contains(exam) || !exam.dentistId.equals(currentEmployeeId())) {
            throw new IllegalArgumentException("Phiếu khám không thuộc nha sĩ hiện tại.");
        }
    }
    private void writableExam(Exam exam) {
        require(Role.DENTIST); ownExam(exam);
        if (!exam.status.equals("DANG_KHAM")) throw new IllegalArgumentException("Chỉ sửa phiếu đang khám.");
        for (Invoice invoice : invoices) if (invoice.examId.equals(exam.id)) {
            throw new IllegalArgumentException("Phiếu đã lập hóa đơn, không được thay đổi.");
        }
    }
    public void saveExam(Exam exam, String clinical, String diagnosis, String notes, String advice, String teeth) {
        writableExam(exam);
        if (clinical.length() > 1000 || diagnosis.length() > 1000 || notes.length() > 1000 || advice.length() > 1000) {
            throw new IllegalArgumentException("Mỗi nội dung khám tối đa 1000 ký tự.");
        }
        exam.clinical = clinical; exam.diagnosis = diagnosis; exam.notes = notes; exam.advice = advice; exam.teeth = teeth;
    }
    public void completeExam(Exam exam) {
        writableExam(exam);
        required(exam.diagnosis, "Chẩn đoán", 1000);
        exam.status = "DA_KHAM";
    }
    public void saveTreatment(Exam exam, Treatment item, boolean creating) {
        writableExam(exam);
        DentalService service = dentalService(item.serviceId);
        if (!service.active) throw new IllegalArgumentException("Dịch vụ đã ngừng áp dụng.");
        int q = item.tooth / 10, n = item.tooth % 10;
        if (item.tooth != 0 && !(q >= 1 && q <= 4 && n >= 1 && n <= 8)) throw new IllegalArgumentException("Vị trí FDI không hợp lệ.");
        Treatment old = null;
        for (Treatment t : exam.treatments) if (t.id.equals(item.id)) old = t;
        if (!creating && old == null) throw new IllegalArgumentException("Dịch vụ không còn trong phiếu.");
        if (creating && old != null) throw new IllegalArgumentException("Chi tiết dịch vụ trùng.");
        // Giá snapshot khi thêm; sửa cùng dịch vụ giữ giá cũ.
        item.price = old != null && old.serviceId.equals(item.serviceId) ? old.price : service.price;
        item.name = service.name;
        if (creating) exam.treatments.add(item); else exam.treatments.set(exam.treatments.indexOf(old), item);
    }
    public void setPerformed(Exam exam, Treatment item) { writableExam(exam); if (!exam.treatments.contains(item)) throw new IllegalArgumentException("Dịch vụ không thuộc phiếu."); item.performed = !item.performed; }
    public void removeTreatment(Exam exam, Treatment item) { writableExam(exam); exam.treatments.remove(item); }
    public void saveMedicine(Exam exam, Medicine item, boolean creating) {
        writableExam(exam);
        required(item.name, "Tên thuốc", 150); required(item.unit, "Đơn vị", 50);
        required(item.dosage, "Liều dùng", 200); required(item.instructions, "Cách dùng", 500);
        if (item.quantity <= 0) throw new IllegalArgumentException("Số lượng thuốc phải lớn hơn 0.");
        Medicine old = null;
        for (Medicine m : exam.medicines) if (m.id.equals(item.id)) old = m;
        if (!creating && old == null) throw new IllegalArgumentException("Thuốc không còn trong đơn.");
        if (creating && old != null) throw new IllegalArgumentException("Thuốc bị trùng mã.");
        if (exam.prescriptionId.isEmpty()) exam.prescriptionId = nextId("DT");
        if (creating) exam.medicines.add(item); else exam.medicines.set(exam.medicines.indexOf(old), item);
    }
    public void removeMedicine(Exam exam, Medicine item) { writableExam(exam); exam.medicines.remove(item); }
    public BigDecimal performedTotal(Exam exam) {
        role(); BigDecimal total = BigDecimal.ZERO;
        for (Treatment line : exam.treatments) if (line.performed) total = total.add(line.price);
        return total;
    }
    public Invoice createInvoice(Exam exam) {
        require(Role.RECEPTIONIST);
        if (!exams.contains(exam) || !exam.status.equals("DA_KHAM")) throw new IllegalArgumentException("Chỉ lập hóa đơn cho phiếu đã hoàn tất trong dữ liệu hiện tại.");
        for (Invoice old : invoices) if (old.examId.equals(exam.id)) return old;
        if (performedTotal(exam).signum() <= 0) throw new IllegalArgumentException("Chưa có dịch vụ đã thực hiện có giá trị để thu tiền.");
        Invoice invoice = new Invoice(nextId("HD"), exam.id, DEMO_DATE.atTime(10, 0), performedTotal(exam), false, "", null);
        for (Treatment line : exam.treatments) if (line.performed) {
            invoice.lines.add(new Treatment(line.id, line.serviceId, line.name, line.tooth, line.price, true));
        }
        invoices.add(invoice); return invoice;
    }
    public void pay(Invoice invoice, String method, BigDecimal tendered, boolean manuallyConfirmed) {
        require(Role.RECEPTIONIST);
        if (!invoices.contains(invoice) || invoice.paid) throw new IllegalArgumentException("Hóa đơn đã thu tiền hoặc không còn tồn tại.");
        if (!method.equals("TIEN_MAT") && !method.equals("CHUYEN_KHOAN")) throw new IllegalArgumentException("Phương thức không hợp lệ.");
        if (!manuallyConfirmed) throw new IllegalArgumentException("Cần xác nhận đã kiểm tra tiền thực nhận.");
        if (tendered == null || tendered.compareTo(invoice.total) < 0) throw new IllegalArgumentException("Chưa thu đủ tiền.");
        if (method.equals("CHUYEN_KHOAN") && tendered.compareTo(invoice.total) != 0) throw new IllegalArgumentException("Số tiền chuyển khoản phải bằng tổng hóa đơn.");
        invoice.paid = true; invoice.method = method; invoice.paidAt = DEMO_DATE.atTime(10, 30);
        // Thanh toán lưu đúng tổng hóa đơn; tiền mặt dư chỉ dùng tính tiền thối.
    }
    public void dispose() {
        employees.clear(); patients.clear(); services.clear(); assignments.clear(); appointments.clear(); exams.clear(); invoices.clear();
        disposed = true;
    }

    private void seed() {
        employees.add(new Employee("NV001","Lê Hoàng Nam",Role.MANAGER,LocalDate.of(1985,1,1),"Nam","0901000001","nam@example.com","TP. Hồ Chí Minh",true));
        employees.add(new Employee("NV002","Trần Minh Đức",Role.DENTIST,LocalDate.of(1988,8,15),"Nam","0902000002","duc@example.com","TP. Hồ Chí Minh",true));
        employees.add(new Employee("NV003","Nguyễn Lan Anh",Role.DENTIST,LocalDate.of(1990,5,2),"Nữ","0903000003","anh@example.com","TP. Hồ Chí Minh",true));
        employees.add(new Employee("NV004","Phạm Thu Hà",Role.RECEPTIONIST,LocalDate.of(1995,6,20),"Nữ","0904000004","ha@example.com","TP. Hồ Chí Minh",true));
        employees.add(new Employee("NV005","Võ Ngọc Mai",Role.NURSE,LocalDate.of(1996,3,8),"Nữ","0905000005","mai@example.com","TP. Hồ Chí Minh",true));
        for (int i=1;i<=6;i++) patients.add(new Patient("BN00"+i,new String[]{"Nguyễn Văn A","Trần Thị B","Lê Văn C","Phạm Thị D","Đỗ Văn E","Vũ Thị F"}[i-1],
            LocalDate.of(1990+i,6,15),i%2==0?"Nữ":"Nam","091200000"+i,"Địa chỉ DEMO",i==1?"Penicillin":"Không ghi nhận"));
        services.add(new DentalService("DV001","Khám tổng quát","Khám","Lần",new BigDecimal("100000"),30,"Dữ liệu DEMO",true));
        services.add(new DentalService("DV002","Trám răng composite","Phục hồi","Răng",new BigDecimal("500000"),30,"Phục hồi răng sâu",true));
        services.add(new DentalService("DV003","Cạo vôi răng","Dự phòng","Lần",new BigDecimal("300000"),30,"Dữ liệu DEMO",true));
        LocalDate monday = DEMO_DATE.with(java.time.DayOfWeek.MONDAY);
        for (Employee e : employees) for (int day=0;day<13;day++) {
            LocalDate date=monday.plusDays(day);if(date.getDayOfWeek()==DayOfWeek.SUNDAY)continue;
            for (Shift shift : Shift.values()) assignments.add(new Assignment("PC"+(++sequence),e.id,
                e.id.equals("NV003")?"P02":"P01",date,shift==Shift.SANG?LocalTime.of(8,0):LocalTime.of(13,30),
                shift==Shift.SANG?LocalTime.NOON:LocalTime.of(17,0),shift,e.role==Role.RECEPTIONIST?"Tiếp nhận bệnh nhân":"Công việc theo phân công",true));
        }
        for (int i=1;i<=3;i++) appointments.add(new Appointment("LH00"+i,"BN00"+i,"NV002",DEMO_DATE,LocalTime.of(9,0).plusMinutes((i-1)*30),
            LocalTime.of(9,0).plusMinutes(i*30),"Kiểm tra răng","DEMO","DA_XAC_NHAN"));
        exams.add(new Exam("PK001","BN001","NV002","NV004","LH001","P01",DEMO_DATE.atTime(8,55),"Đau răng hàm dưới","CHO_KHAM","","","","",""));
        Exam ongoing=new Exam("PK003","BN003","NV002","NV004","LH003","P01",DEMO_DATE.atTime(8,40),"Điều trị răng sâu","DANG_KHAM","Đau khi ăn nhai","Sâu răng 36","","","36");exams.add(ongoing);
        appointments.get(0).status="DA_DEN";appointments.get(2).status="DA_DEN";
        for(int i=4;i<=6;i++){
            Exam completed=new Exam("PK01"+i,"BN00"+i,"NV002","NV004",null,"P01",DEMO_DATE.atTime(8,i),"Khám định kỳ","DA_KHAM","Khám định kỳ","Cao răng","","","");
            completed.treatments.add(new Treatment("CT"+i,"DV003","Cạo vôi răng",0,new BigDecimal("300000"),true));exams.add(completed);
            if(i!=5){
                Invoice invoice=new Invoice("HD00"+i,completed.id,DEMO_DATE.atTime(9,0),new BigDecimal("300000"),i==6,i==6?"TIEN_MAT":"",i==6?DEMO_DATE.atTime(9,30):null);
                invoice.lines.add(new Treatment("CT"+i,"DV003","Cạo vôi răng",0,new BigDecimal("300000"),true));invoices.add(invoice);
            }
        }
    }
}
