package vn.edu.nhom19.nhakhoa.entity;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import vn.edu.nhom19.nhakhoa.auth.Role;

/** Dữ liệu của phiên DEMO trong bộ nhớ, không phải bản ghi đã lưu SQL Server.
 * Các lớp nhỏ được gom ở đây để dễ theo dõi luồng; chưa thêm cột vào schema.
 */
public final class DemoData {
    private DemoData() { }
    public enum Shift { SANG, CHIEU }
    public static final class Employee {
        public String id;
        public String name;
        public Role role;
        public LocalDate birthday;
        public String gender;
        public String phone;
        public String email;
        public String address;
        public boolean active;
        public Employee(String id, String name, Role role, LocalDate birthday, String gender, String phone, String email, String address, boolean active) {
            this.id = id;
            this.name = name;
            this.role = role;
            this.birthday = birthday;
            this.gender = gender;
            this.phone = phone;
            this.email = email;
            this.address = address;
            this.active = active;
        }
        @Override public String toString() { return id + " · " + name; }
    }
    public static final class Patient {
        public String id;
        public String name;
        public LocalDate birthday;
        public String gender;
        public String phone;
        public String address;
        public String allergy;
        public Patient(String id, String name, LocalDate birthday, String gender, String phone, String address, String allergy) {
            this.id = id;
            this.name = name;
            this.birthday = birthday;
            this.gender = gender;
            this.phone = phone;
            this.address = address;
            this.allergy = allergy;
        }
        @Override public String toString() { return id + " · " + name; }
    }
    public static final class DentalService {
        public String id;
        public String name;
        public String group;
        public String unit;
        public BigDecimal price;
        public int duration;
        public String description;
        public boolean active;
        public DentalService(String id, String name, String group, String unit, BigDecimal price, int duration, String description, boolean active) {
            this.id = id;
            this.name = name;
            this.group = group;
            this.unit = unit;
            this.price = price;
            this.duration = duration;
            this.description = description;
            this.active = active;
        }
        @Override public String toString() { return id + " · " + name; }
    }
    public static final class Assignment {
        public String id;
        public String employeeId;
        public String room;
        public LocalDate date;
        public LocalTime start;
        public LocalTime end;
        public Shift shift;
        public String work;
        public boolean active;
        public Assignment(String id, String employeeId, String room, LocalDate date, LocalTime start, LocalTime end, Shift shift, String work, boolean active) {
            this.id = id;
            this.employeeId = employeeId;
            this.room = room;
            this.date = date;
            this.start = start;
            this.end = end;
            this.shift = shift;
            this.work = work;
            this.active = active;
        }
    }
    public static final class Appointment {
        public String id;
        public String patientId;
        public String dentistId;
        public LocalDate date;
        public LocalTime start;
        public LocalTime end;
        public String reason;
        public String notes;
        public String status;
        public Appointment(String id, String patientId, String dentistId, LocalDate date, LocalTime start, LocalTime end, String reason, String notes, String status) {
            this.id = id;
            this.patientId = patientId;
            this.dentistId = dentistId;
            this.date = date;
            this.start = start;
            this.end = end;
            this.reason = reason;
            this.notes = notes;
            this.status = status;
        }
    }
    public static final class Treatment {
        public String id;
        public String serviceId;
        public String name;
        public int tooth;
        public BigDecimal price;
        public boolean performed;
        public Treatment(String id, String serviceId, String name, int tooth, BigDecimal price, boolean performed) {
            this.id = id;
            this.serviceId = serviceId;
            this.name = name;
            this.tooth = tooth;
            this.price = price;
            this.performed = performed;
        }
    }
    public static final class Medicine {
        // Chỉ hỗ trợ điền lại form DEMO; khi lưu SQL ghép vào lieuDung/cachDung.
        public String morning="", noon="", afternoon="", night="", route="Uống", userInstructions="";
        public int days=1;
        public String id;
        public String name;
        public String strength;
        public int quantity;
        public String unit;
        public String dosage;
        public String instructions;
        public Medicine(String id, String name, String strength, int quantity, String unit, String dosage, String instructions) {
            this.id = id;
            this.name = name;
            this.strength = strength;
            this.quantity = quantity;
            this.unit = unit;
            this.dosage = dosage;
            this.instructions = instructions;
        }
    }
    public static final class Exam {
        public String id;
        public String patientId;
        public String dentistId;
        public String receptionistId;
        public String appointmentId;
        public String room;
        public LocalDateTime received;
        public String reason;
        public String status;
        public String clinical;
        public String diagnosis;
        public String notes;
        public String advice;
        public String teeth;
        public final ArrayList<Treatment> treatments = new ArrayList<>();
        public final ArrayList<Medicine> medicines = new ArrayList<>();
        public String prescriptionId = "";
        public Exam(String id, String patientId, String dentistId, String receptionistId, String appointmentId, String room, LocalDateTime received, String reason, String status, String clinical, String diagnosis, String notes, String advice, String teeth) {
            this.id = id;
            this.patientId = patientId;
            this.dentistId = dentistId;
            this.receptionistId = receptionistId;
            this.appointmentId = appointmentId;
            this.room = room;
            this.received = received;
            this.reason = reason;
            this.status = status;
            this.clinical = clinical;
            this.diagnosis = diagnosis;
            this.notes = notes;
            this.advice = advice;
            this.teeth = teeth;
        }
    }
    public static final class Invoice {
        public String id;
        public String examId;
        public LocalDateTime created;
        public BigDecimal total;
        public boolean paid;
        public String method;
        public LocalDateTime paidAt;
        public final ArrayList<Treatment> lines = new ArrayList<>();
        public Invoice(String id, String examId, LocalDateTime created, BigDecimal total, boolean paid, String method, LocalDateTime paidAt) {
            this.id = id;
            this.examId = examId;
            this.created = created;
            this.total = total;
            this.paid = paid;
            this.method = method;
            this.paidAt = paidAt;
        }
    }
}
