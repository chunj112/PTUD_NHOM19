package vn.edu.nhom19.nhakhoa;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.*;
import org.junit.jupiter.api.Test;
import vn.edu.nhom19.nhakhoa.auth.*;
import vn.edu.nhom19.nhakhoa.entity.DemoData.*;
import vn.edu.nhom19.nhakhoa.service.DemoClinicService;

/** Kiểm thử quy tắc DEMO, không thay thế integration test SQL/transaction. */
class DemoBusinessRulesTest {
    private DemoClinicService service(Role role){UserSession s=new UserSession();s.startDemo(role);return new DemoClinicService(s);}
    @Test void missingIdentityOrWrongRoleCannotWrite(){
        DemoClinicService nurse=service(Role.NURSE);
        assertThrows(IllegalArgumentException.class,nurse::exams);
        assertThrows(IllegalArgumentException.class,nurse::patients);
        assertThrows(IllegalArgumentException.class,()->nurse.saveService(new DentalService("DVX","A","Khám","Lần",BigDecimal.ONE,30,"",true),true));
        nurse.dispose();assertThrows(IllegalStateException.class,nurse::employees);
    }
    @Test void oldServiceCannotBeReusedAfterLogoutOrRoleChange(){
        UserSession session=new UserSession();session.startDemo(Role.MANAGER);DemoClinicService service=new DemoClinicService(session);
        session.clear();session.startDemo(Role.RECEPTIONIST);assertThrows(IllegalStateException.class,service::employees);
    }
    @Test void serviceValidationAndSnapshotDoNotDependOnFormattedMoney(){
        DemoClinicService manager=service(Role.MANAGER);
        assertThrows(IllegalArgumentException.class,()->manager.saveService(new DentalService("X","","Khám","Lần",BigDecimal.ONE,30,"",true),true));
        assertThrows(IllegalArgumentException.class,()->manager.saveService(new DentalService("X","A","Khám","Lần",new BigDecimal("-1"),30,"",true),true));
        manager.saveService(new DentalService("X","Dịch vụ thử","Khám","Lần",new BigDecimal("150000.50"),30,"",true),true);
        assertEquals(new BigDecimal("150000.50"),manager.dentalService("X").price);
    }
    @Test void sameEmployeeCannotHaveTwoActiveAssignmentsInSameShiftEvenWithoutTimeOverlap(){
        DemoClinicService s=service(Role.MANAGER);LocalDate day=DemoClinicService.DEMO_DATE.plusDays(20);
        int before=s.assignments().size();
        Assignment a=new Assignment("A","NV002","P01",day,LocalTime.of(8,0),LocalTime.of(9,0),Shift.SANG,"",true);
        s.saveAssignment(a,true);
        assertThrows(IllegalArgumentException.class,()->s.saveAssignment(new Assignment("B","NV002","P02",day,LocalTime.of(10,0),LocalTime.of(11,0),Shift.SANG,"",true),true));
        s.saveAssignment(new Assignment("C","NV005","P01",day,LocalTime.of(8,0),LocalTime.of(9,0),Shift.SANG,"",true),true);
        assertEquals(before+2,s.assignments().size());
        s.cancelAssignment(a);assertFalse(a.active);assertTrue(s.assignments().contains(a));
    }
    @Test void appointmentMustBeInsideAssignmentAndNotOverlap(){
        DemoClinicService s=service(Role.RECEPTIONIST);
        assertThrows(IllegalArgumentException.class,()->s.saveAppointment(new Appointment("BAD","BN001","NV002",DemoClinicService.DEMO_DATE,LocalTime.of(7,0),LocalTime.of(7,30),"","", "DA_XAC_NHAN"),true));
        assertThrows(IllegalArgumentException.class,()->s.saveAppointment(new Appointment("CLASH","BN001","NV002",DemoClinicService.DEMO_DATE,LocalTime.of(9,0),LocalTime.of(9,30),"","", "DA_XAC_NHAN"),true));
    }
    @Test void repeatedAdmissionDoesNotCreateTwoVisits(){
        DemoClinicService s=service(Role.RECEPTIONIST);Appointment a=s.appointments().get(1);
        int count=s.exams().size();Exam first=s.admit(a.patientId,a.dentistId,a.date,a.start,a.reason,a);
        assertSame(first,s.admit(a.patientId,a.dentistId,a.date,a.start,a.reason,a));
        assertEquals(count+1,s.exams().size());assertEquals("DA_DEN",a.status);
    }
    @Test void completionNeedsDiagnosisAndCompletedExamIsReadOnly(){
        DemoClinicService s=service(Role.DENTIST);Exam exam=s.exam("PK001");s.startExam(exam);
        assertThrows(IllegalArgumentException.class,()->s.completeExam(exam));
        s.saveExam(exam,"Triệu chứng","Chẩn đoán","","","36");s.completeExam(exam);
        assertEquals("DA_KHAM",exam.status);
        assertThrows(IllegalArgumentException.class,()->s.saveExam(exam,"A","B","","",""));
        assertThrows(IllegalArgumentException.class,()->s.startExam(exam));
    }
    @Test void onePrescriptionCanContainManyMedicinesWithoutPriceOrStock(){
        DemoClinicService s=service(Role.DENTIST);Exam exam=s.exam("PK003");
        s.saveMedicine(exam,new Medicine("M1","Thuốc A","500mg",10,"viên","Sáng 1","Uống sau ăn"),true);
        String id=exam.prescriptionId;
        s.saveMedicine(exam,new Medicine("M2","Thuốc B","",5,"gói","Sáng 1","Uống sau ăn"),true);
        assertEquals(id,exam.prescriptionId);assertEquals(2,exam.medicines.size());assertEquals(BigDecimal.ZERO,s.performedTotal(exam));
    }
    @Test void invoiceIncludesOnlyPerformedServicesAndKeepsSnapshot(){
        DemoClinicService s=service(Role.RECEPTIONIST);Exam exam=s.exam("PK015");
        exam.treatments.add(new Treatment("PENDING","DV002","Chưa làm",36,new BigDecimal("500000"),false));
        Invoice invoice=s.createInvoice(exam);
        assertEquals(new BigDecimal("300000"),invoice.total);assertEquals(1,invoice.lines.size());
        exam.treatments.get(0).price=new BigDecimal("999999");
        assertEquals(new BigDecimal("300000"),invoice.lines.get(0).price);
        assertSame(invoice,s.createInvoice(exam));
    }
    @Test void paymentNeedsFullAmountManualConfirmationAndCannotRepeat(){
        DemoClinicService s=service(Role.RECEPTIONIST);Invoice invoice=s.invoices().get(0);
        assertThrows(IllegalArgumentException.class,()->s.pay(invoice,"CHUYEN_KHOAN",invoice.total,false));
        assertThrows(IllegalArgumentException.class,()->s.pay(invoice,"TIEN_MAT",new BigDecimal("100"),true));
        assertFalse(invoice.paid);
        s.pay(invoice,"TIEN_MAT",new BigDecimal("500000"),true);assertTrue(invoice.paid);
        assertThrows(IllegalArgumentException.class,()->s.pay(invoice,"TIEN_MAT",invoice.total,true));
    }
    @Test void moneyAcceptsVietnameseAndDecimalInputWithoutMultiplyingCents(){
        assertEquals(new BigDecimal("150000.50"),vn.edu.nhom19.nhakhoa.view.common.ViewAccess.money("150.000,50 ₫"));
        assertEquals(new BigDecimal("150000.50"),vn.edu.nhom19.nhakhoa.view.common.ViewAccess.money("150000.50"));
        assertEquals(new BigDecimal("1000000"),vn.edu.nhom19.nhakhoa.view.common.ViewAccess.money("1.000.000"));
        assertThrows(IllegalArgumentException.class,()->vn.edu.nhom19.nhakhoa.view.common.ViewAccess.money("1.2.3"));
        DemoClinicService s=service(Role.MANAGER);
        assertThrows(IllegalArgumentException.class,()->s.saveService(new DentalService("TOO_BIG","A","Khám","Lần",new BigDecimal("10000000000000000"),30,"",true),true));
    }

}
