package vn.edu.nhom19.nhakhoa.view.receptionist;

import java.util.*;
import java.util.function.Consumer;
import vn.edu.nhom19.nhakhoa.auth.Role;
import vn.edu.nhom19.nhakhoa.entity.DemoData.*;
import vn.edu.nhom19.nhakhoa.service.DemoClinicService;
import vn.edu.nhom19.nhakhoa.view.common.*;

/** UC010/011: tìm hồ sơ, mở đúng bệnh nhân, sửa hành chính; nội dung khám chỉ đọc. */
public final class PatientController extends ScreenController {
    private String query="";
    private Patient current;
    public PatientController(ViewAccess view,ControllerContext context){super(view,context);context.service.require(Role.RECEPTIONIST);bindSearch();}
    private void bindSearch(){
        view.text("txtPatientSearch",query);
        view.disable("btnSearchPatient",query.isBlank());
        ViewAccess searchView=view;
        searchView.changed("txtPatientSearch",()->searchView.disable("btnSearchPatient",searchView.text("txtPatientSearch").isBlank()));
        view.on("btnSearchPatient",context,()->{query=view.text("txtPatientSearch");search();});
        if(view.find("txtPatientSearch") instanceof javafx.scene.control.TextField input)input.setOnAction(e->view.button("btnSearchPatient").fire());
    }
    private void search(){
        if(query.isBlank()){view.error("Nhập mã, tên hoặc SĐT bệnh nhân.");return;}
        view=ViewAccess.load("receptionist/patients-results.fxml");bindSearch();
        ArrayList<Map<String,Object>> rows=new ArrayList<>();
        for(Patient p:context.service.patients())if(matches(p,query))rows.add(ViewAccess.row(p,p.id,p.name,ViewAccess.DATE.format(p.birthday),p.phone,p.gender,lastVisit(p)));
        view.paged("tblPatientResults",rows,"tblPatientResultsCount","tblPatientResultsPrevious","tblPatientResultsNext","tblPatientResultsPage");
        view.table("tblPatientResults").getSelectionModel().selectedItemProperty().addListener((o,a,b)->{if(b!=null)open((Patient)b.get("_model"));});
        context.replace(this);
    }
    public static boolean matches(Patient p,String q){return (p.id+" "+p.name+" "+p.phone).toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT));}
    private String lastVisit(Patient p){String result="—";for(Exam e:context.service.exams())if(e.patientId.equals(p.id))result=ViewAccess.DATE.format(e.received.toLocalDate());return result;}
    public void open(Patient patient){
        current=patient;view=ViewAccess.load("receptionist/patient-record.fxml");
        view.on("btnChangePatient",context,()->{current=null;search();});
        view.on("btnEditPatient",context,()->openForm(context,current,p->{current=p;open(p);}));
        for(String id:List.of("dpHistoryFrom","dpPatientInvoiceFrom"))view.date(id,DemoClinicService.DEMO_DATE.minusYears(10));
        for(String id:List.of("dpHistoryTo","dpPatientInvoiceTo"))view.date(id,DemoClinicService.DEMO_DATE);
        ArrayList<Object> dentists=new ArrayList<>();dentists.add("Tất cả nha sĩ");dentists.addAll(context.service.dentists());
        view.choices("cboHistoryDentist",dentists,dentists.get(0));
        view.choices("cboPatientInvoiceStatus",List.of("Tất cả","Chờ thanh toán","Đã thanh toán"),"Tất cả");
        view.choices("cboPatientInvoiceMethod",List.of("Tất cả","TIEN_MAT","CHUYEN_KHOAN"),"Tất cả");
        view.on("btnApplyHistoryFilter",context,this::refresh);view.on("btnApplyPatientInvoiceFilter",context,this::refresh);
        view.table("tblPatientHistory").getSelectionModel().selectedItemProperty().addListener((o,a,b)->showHistory(b==null?null:(Exam)b.get("_model")));
        view.table("tblPatientInvoices").getSelectionModel().selectedItemProperty().addListener((o,a,b)->showInvoice(b==null?null:(Invoice)b.get("_model")));
        refresh();context.replace(this);
    }
    @Override public void refresh(){
        if(current==null)return;
        view.text("lblSelectedPatientName",current.toString());view.text("lblSelectedPatientSummary","SĐT: "+current.phone+" · "+ViewAccess.DATE.format(current.birthday)+" · "+current.gender);view.text("lblPatientId",current.id);view.text("lblPatientFullName",current.name);
        view.text("lblPatientBirthDate",ViewAccess.DATE.format(current.birthday));view.text("lblPatientGender",current.gender);
        view.text("lblPatientPhone",current.phone);view.text("lblPatientEmail","Không có cột email trong schema");
        view.text("lblPatientAddress",current.address);view.text("lblPatientAllergy",current.allergy);
        view.text("lblPatientCreated","Hồ sơ DEMO trong bộ nhớ");view.text("lblPatientRecentExam",lastVisit(current));
        view.text("lblPatientRecentDentist","—");view.text("lblPatientNextAppointment","Không có lịch hẹn tiếp theo");
        view.text("lblPatientCurrentStatus","Không có phiếu đang khám");
        ArrayList<Map<String,Object>> history=new ArrayList<>(),invoiceRows=new ArrayList<>();
        for(Exam e:context.service.exams())if(e.patientId.equals(current.id)){
            view.text("lblPatientRecentDentist",context.service.employee(e.dentistId).name);
            if(!e.status.equals("DA_KHAM"))view.text("lblPatientCurrentStatus",e.id+" · "+e.status);
            if(inRange(e.received.toLocalDate(),"dpHistoryFrom","dpHistoryTo")){
                Object doctor=view.combo("cboHistoryDentist").getValue();
                if(!(doctor instanceof Employee d)||d.id.equals(e.dentistId))history.add(ViewAccess.row(e,ViewAccess.DATE.format(e.received.toLocalDate()),e.id,context.service.employee(e.dentistId).name,e.reason,e.diagnosis,e.status));
            }
        }
        for(Appointment a:context.service.appointments())if(a.patientId.equals(current.id)&&!a.status.equals("DA_HUY")&&!a.status.equals("DA_DEN")){
            view.text("lblPatientNextAppointment",a.id+" · "+ViewAccess.DATE.format(a.date)+" · "+a.start);break;
        }
        for(Invoice i:context.service.invoices())if(context.service.exam(i.examId).patientId.equals(current.id)&&inRange(i.created.toLocalDate(),"dpPatientInvoiceFrom","dpPatientInvoiceTo")){
            String status=view.text("cboPatientInvoiceStatus"),method=view.text("cboPatientInvoiceMethod");
            if(status.equals("Đã thanh toán")&&!i.paid||status.equals("Chờ thanh toán")&&i.paid)continue;
            if(!method.equals("Tất cả")&&!method.equals(i.method))continue;
            invoiceRows.add(ViewAccess.row(i,i.id,ViewAccess.DATE.format(i.created.toLocalDate()),ViewAccess.amount(i.total),i.method,i.paid?"Đã thanh toán":"Chờ thanh toán",i.paid?"NV004":"—"));
        }
        view.rows("tblPatientHistory",history);view.text("tblPatientHistoryCount","Hiển thị "+history.size()+" bản ghi");
        view.rows("tblPatientInvoices",invoiceRows);view.text("tblPatientInvoicesCount","Hiển thị "+invoiceRows.size()+" bản ghi");
        showHistory(null);showInvoice(null);
    }
    private void showHistory(Exam exam){
        for(String id:List.of("lblHistoryDetailTitle","lblHistoryExamDate","lblHistoryDentist","lblHistorySymptoms","lblHistoryDiagnosis","lblHistoryNotes","lblHistoryServices","lblHistoryPrescription"))view.text(id,"—");
        view.text("lblHistoryDetailTitle",exam==null?"Chưa chọn phiếu khám":"Chi tiết phiếu · "+exam.id);
        if(exam==null)return;
        view.text("lblHistoryExamDate",exam.received.toString());view.text("lblHistoryDentist",context.service.employee(exam.dentistId).name);
        view.text("lblHistorySymptoms",exam.clinical);view.text("lblHistoryDiagnosis",exam.diagnosis);view.text("lblHistoryNotes",exam.notes);
        StringJoiner services=new StringJoiner("; "),medicines=new StringJoiner("; ");
        for(Treatment t:exam.treatments)services.add(t.name+" · "+(t.performed?"Đã thực hiện":"Chưa thực hiện"));
        for(Medicine m:exam.medicines)medicines.add(m.name+" · "+m.quantity+" "+m.unit);
        view.text("lblHistoryServices",services.toString());view.text("lblHistoryPrescription",medicines.toString());
    }
    private void showInvoice(Invoice invoice){
        view.rows("tblPatientInvoiceServices",List.of());
        for(String id:List.of("lblPatientInvoiceTitle","lblPatientInvoiceExam","lblPatientInvoiceCreated","lblPatientInvoiceTotal","lblPatientInvoiceMethod","lblPatientInvoicePaidAt","lblPatientInvoiceConfirmer"))view.text(id,"—");
        view.text("lblPatientInvoiceTitle",invoice==null?"Chưa chọn hóa đơn":"Hóa đơn DEMO · "+invoice.id);
        if(invoice==null)return;
        view.text("lblPatientInvoiceExam",current+" · "+invoice.examId);view.text("lblPatientInvoiceCreated",invoice.created.toString());
        view.text("lblPatientInvoiceTotal",ViewAccess.amount(invoice.total));view.text("lblPatientInvoiceMethod",invoice.method);
        view.text("lblPatientInvoicePaidAt",invoice.paidAt==null?"Chưa thanh toán":invoice.paidAt.toString());view.text("lblPatientInvoiceConfirmer",invoice.paid?context.service.employee("NV004").name:"—");
        ArrayList<Map<String,Object>> lines=new ArrayList<>();for(Treatment t:invoice.lines)lines.add(ViewAccess.row(t,t.name,t.tooth,1,ViewAccess.amount(t.price),ViewAccess.amount(t.price)));
        view.rows("tblPatientInvoiceServices",lines);
    }
    private boolean inRange(java.time.LocalDate date,String from,String to){
        java.time.LocalDate a=view.date(from).getValue(),b=view.date(to).getValue();
        if(a==null||b==null||a.isAfter(b))throw new IllegalArgumentException("Khoảng ngày không hợp lệ.");
        return !date.isBefore(a)&&!date.isAfter(b);
    }
    public static void openForm(ControllerContext context,Patient old,Consumer<Patient> saved){
        boolean creating=old==null;ViewAccess form=ViewAccess.load("receptionist/patient-"+(creating?"add":"edit")+".fxml");
        String id=creating?context.service.nextId("BN"):old.id;
        form.text("txtPatientId",id);form.text("txtPatientName",creating?"":old.name);form.text("txtPatientPhone",creating?"":old.phone);
        form.text("txtPatientAddress",creating?"":old.address);form.date("dpPatientBirthDate",creating?null:old.birthday);
        form.choices("cboPatientGender",List.of("Nam","Nữ"),creating?"Nam":old.gender);
        form.text("txtPatientEmail","");form.disable("txtPatientEmail",true); // BenhNhan chưa có email trong schema.
        form.text("txtPatientReportedAllergy",creating?"":old.allergy);
        var stage=context.dialog(form,creating?"Thêm bệnh nhân DEMO":"Sửa bệnh nhân DEMO");
        form.on("btnSavePatient",context,()->{
            Patient p=new Patient(id,form.text("txtPatientName"),form.date("dpPatientBirthDate").getValue(),
                form.text("cboPatientGender"),form.text("txtPatientPhone"),form.text("txtPatientAddress"),
                creating?form.text("txtPatientReportedAllergy"):old.allergy);
            context.service.savePatient(p,creating);stage.close();saved.accept(p);
        });
    }
}
