package vn.edu.nhom19.nhakhoa.view.common;

import java.math.BigDecimal;
import java.util.*;
import vn.edu.nhom19.nhakhoa.app.Route;
import vn.edu.nhom19.nhakhoa.auth.Role;
import vn.edu.nhom19.nhakhoa.entity.DemoData.*;
import vn.edu.nhom19.nhakhoa.service.DemoClinicService;

/** Dashboard lấy cùng dữ liệu với màn hình nghiệp vụ, không duy trì bản mock riêng. */
public final class DashboardController extends ScreenController {
    public DashboardController(ViewAccess view,ControllerContext context){super(view,context);}
    @Override public void refresh(){
        Role role=context.service.role();
        if(role==Role.DENTIST){dentist();return;}
        if(role!=Role.MANAGER&&role!=Role.RECEPTIONIST)return;
        int appointments=0,arrived=0,waiting=0,completed=0,pending=0,performed=0,ready=0;
        BigDecimal revenue=BigDecimal.ZERO;
        ArrayList<Map<String,Object>> rows=new ArrayList<>(),activity=new ArrayList<>();
        for(Appointment a:context.service.appointments())if(a.date.equals(DemoClinicService.DEMO_DATE)&&!a.status.equals("DA_HUY")){
            appointments++;if(a.status.equals("DA_DEN"))arrived++;
            Patient p=context.service.patient(a.patientId);Employee e=context.service.employee(a.dentistId);
            if(role==Role.MANAGER)rows.add(ViewAccess.row(a,a.start,p.name,e.name,a.reason,a.status));
            else rows.add(ViewAccess.row(a,a.start,p.name,e.name,a.reason,a.status));
        }
        for(Exam e:context.service.exams())if(e.received.toLocalDate().equals(DemoClinicService.DEMO_DATE)){
            if(e.status.equals("CHO_KHAM"))waiting++;
            if(e.status.equals("DA_KHAM")){completed++;boolean billed=false;for(Invoice i:context.service.invoices())if(i.examId.equals(e.id))billed=true;if(!billed)ready++;}
            for(Treatment t:e.treatments)if(t.performed)performed++;
            activity.add(ViewAccess.row(e,e.received.toLocalTime(),context.service.patient(e.patientId).name,e.reason,e.status));
        }
        for(Invoice i:context.service.invoices()){if(!i.paid)pending++;else if(i.paidAt.toLocalDate().equals(DemoClinicService.DEMO_DATE))revenue=revenue.add(i.total);}
        view.text("lblAppointmentsToday",appointments);
        if(role==Role.MANAGER){
            int patientsToday=0,patientsBefore=0,appointmentsBefore=0,servicesBefore=0;
            BigDecimal revenueBefore=BigDecimal.ZERO;
            var yesterday=DemoClinicService.DEMO_DATE.minusDays(1);
            // DEMO đã nạp đủ trong bộ nhớ: không có phát sinh là 0, không phải lỗi tải SQL.
            for(Exam exam:context.service.exams()){
                if(exam.received.toLocalDate().equals(DemoClinicService.DEMO_DATE))patientsToday++;
                if(exam.received.toLocalDate().equals(yesterday)){
                    patientsBefore++;for(Treatment treatment:exam.treatments)if(treatment.performed)servicesBefore++;
                }
            }
            for(Appointment appointment:context.service.appointments())
                if(appointment.date.equals(yesterday)&&!appointment.status.equals("DA_HUY"))appointmentsBefore++;
            for(Invoice invoice:context.service.invoices())
                if(invoice.paid&&invoice.paidAt.toLocalDate().equals(yesterday))revenueBefore=revenueBefore.add(invoice.total);
            view.text("lblPatientsToday",patientsToday);view.text("lblPatientsHint","Phiếu tiếp nhận hôm nay · DEMO");
            view.text("lblPatientsComparison",countComparison(patientsToday,patientsBefore,"lượt"));
            view.text("lblAppointmentsComparison",countComparison(appointments,appointmentsBefore,"lịch"));
            view.text("lblServicesComparison",countComparison(performed,servicesBefore,"dịch vụ"));
            BigDecimal difference=revenue.subtract(revenueBefore);
            String comparison="Không thay đổi so với hôm qua";
            if(difference.signum()!=0){
                String change=revenueBefore.signum()==0?ViewAccess.amount(difference.abs()):
                    difference.abs().multiply(BigDecimal.valueOf(100)).divide(revenueBefore,1,java.math.RoundingMode.HALF_UP)+"%";
                comparison=(difference.signum()>0?"↑ ":"↓ ")+change+" so với hôm qua";
            }
            view.text("lblRevenueComparison",comparison);
            view.text("lblAppointmentsHint",arrived+" đã tiếp nhận");view.text("lblRevenueToday",ViewAccess.amount(revenue));
            view.text("lblRevenueTrend","DEMO · Chỉ hóa đơn đã xác nhận tiền");view.text("lblServicesToday",performed);view.text("lblServicesTrend","Dịch vụ đã thực hiện");
            view.text("lblReceived",arrived+" / "+appointments+" lịch hẹn");view.text("lblCompleted",completed+" phiếu");
            view.text("lblUnpaid",pending+" hóa đơn");view.text("lblPendingAppointments",(appointments-arrived)+" lịch chưa đến trong dữ liệu DEMO");
            view.rows("tblRecentAppointments",rows);view.table("tblRecentAppointments").setSelectionModel(null);
            view.rows("tblTodayActivity",activity);view.text("lblAppointmentsCount","Hiển thị "+rows.size()+" / "+rows.size());
            view.on("btnViewReports",context,()->context.navigate(Route.REPORTS));
        }else{
            view.text("lblAppointmentsTodayNote",(appointments-arrived)+" chưa tiếp nhận");view.text("lblArrived",arrived);
            view.text("lblArrivedNote","Lịch hẹn đã tiếp nhận");view.text("lblWaitingExams",waiting);view.text("lblPendingInvoices",pending);
            view.text("lblPendingIntake",(appointments-arrived)+" lịch hẹn");view.text("lblReadyToInvoice",ready+" ca hoàn tất");
            view.text("lblAwaitingPayment",pending+" hóa đơn");view.text("lblNextAppointment",rows.isEmpty()?"Không có lịch hẹn":rows.get(0).get("v0")+" · "+rows.get(0).get("v1"));
            view.text("lblNextDentist",rows.isEmpty()?"—":rows.get(0).get("v2"));
            view.rows("tblUpcomingAppointments",rows);view.text("tblUpcomingAppointmentsCount","Hiển thị "+rows.size()+" / "+rows.size());
            view.on("btnViewAppointments",context,()->context.navigate(Route.INTAKE));
            view.on("btnOpenCashier",context,()->context.navigate(Route.CASHIER));
        }
    }
    private String countComparison(int today,int yesterday,String unit){
        int change=today-yesterday;
        if(change==0)return "Không thay đổi so với hôm qua";
        return (change>0?"↑ ":"↓ ")+Math.abs(change)+" "+unit+" so với hôm qua";
    }
    private void dentist(){
        int waiting=0,ongoing=0,done=0;Exam priority=null;
        for(Exam e:context.service.exams())if(e.received.toLocalDate().equals(DemoClinicService.DEMO_DATE)){
            if(e.status.equals("CHO_KHAM"))waiting++;
            if(e.status.equals("DANG_KHAM")){ongoing++;if(priority==null||!priority.status.equals("DANG_KHAM"))priority=e;}
            if(e.status.equals("DA_KHAM"))done++;
        }
        if(priority==null)for(Exam e:context.service.exams())if(e.received.toLocalDate().equals(DemoClinicService.DEMO_DATE)&&e.status.equals("CHO_KHAM")){priority=e;break;}
        view=ViewAccess.load(priority==null?"dentist/dashboard-unselected.fxml":priority.status.equals("DANG_KHAM")?"dentist/dashboard.fxml":"dentist/dashboard-waiting.fxml");
        view.text("lblWaitingPatients",waiting);view.text("lblExaminingPatients",ongoing);view.text("lblCompletedPatients",done);view.text("lblTotalExams",waiting+ongoing+done);
        if(priority!=null){
            view.text("lblNextPatientName",context.service.patient(priority.patientId).name);view.text("lblNextExamId",priority.id);
            view.text("lblNextExamTime",priority.received.toLocalTime()+" · "+ViewAccess.DATE.format(priority.received.toLocalDate()));
            view.text("lblNextExamReason",priority.reason);view.text("lblNextExamStatus",priority.status.equals("DANG_KHAM")?"Đang khám":"Chờ khám");
        }
        final Exam target=priority;
        view.on("btnOpenNextExamination",context,()->{if(target==null)context.navigate(Route.EXAMINATION);else context.openExam(target.id,target.status.equals("DANG_KHAM"));});
        view.on("btnOpenDentistSchedule",context,()->context.navigate(Route.DENTIST_SCHEDULE));
        view.text("lblMorningShift","Không có ca");view.text("lblAfternoonShift","Không có ca");
        for(Assignment a:context.service.assignments())if(a.active&&a.date.equals(DemoClinicService.DEMO_DATE)){
            boolean morning=a.shift==Shift.SANG;view.text(morning?"lblMorningShift":"lblAfternoonShift",a.start+"–"+a.end);
            view.text(morning?"lblMorningShiftRoom":"lblAfternoonShiftRoom",(morning?"Ca sáng":"Ca chiều")+" · "+a.room);
        }
        view.text("lblDemoNotice","DEMO ONLY · Dữ liệu trong bộ nhớ, xóa khi logout · "+ViewAccess.DATE.format(DemoClinicService.DEMO_DATE));
    }
}
