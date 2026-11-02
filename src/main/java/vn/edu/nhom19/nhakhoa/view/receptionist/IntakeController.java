package vn.edu.nhom19.nhakhoa.view.receptionist;

import java.time.*;
import java.util.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import vn.edu.nhom19.nhakhoa.auth.Role;
import vn.edu.nhom19.nhakhoa.entity.DemoData.*;
import vn.edu.nhom19.nhakhoa.service.DemoClinicService;
import vn.edu.nhom19.nhakhoa.view.common.*;

/** UC008/009: lịch hẹn và khách không hẹn cùng tạo PhiếuKham qua Service.
 * Tất cả form dùng bản dữ liệu riêng; chỉ gọi Service khi người dùng xác nhận.
 */
public final class IntakeController extends ScreenController {
    private final ViewAccess today,weekView;
    private ViewAccess walk;
    private LocalDate week=DemoClinicService.DEMO_DATE.with(DayOfWeek.MONDAY);
    private Appointment selected;
    private Patient walkPatient;
    private LocalTime walkTime;
    private final ArrayList<LocalTime> slots=new ArrayList<>();
    public IntakeController(ViewAccess view,ControllerContext context){
        super(view,context);context.service.require(Role.RECEPTIONIST);
        today=view.sub("tabTodayContent");weekView=view.sub("tabWeekContent");walk=view.sub("tabWalkInContent");
        today.date("dpAppointmentDay",DemoClinicService.DEMO_DATE);
        ArrayList<Object> dentists=new ArrayList<>();dentists.add("Tất cả nha sĩ");dentists.addAll(context.service.dentists());
        today.choices("cboAppointmentDentist",dentists,dentists.get(0));weekView.choices("cboWeekDentist",dentists,dentists.get(0));
        weekView.choices("cboWeekStatus",List.of("Tất cả trạng thái","DA_XAC_NHAN","CHO_XAC_NHAN","DA_DEN","DA_HUY"),"Tất cả trạng thái");
        for(String id:List.of("txtAppointmentSearch","dpAppointmentDay","cboAppointmentDentist"))today.changed(id,this::refresh);
        for(String id:List.of("txtWeekPatientSearch","cboWeekDentist","cboWeekStatus"))weekView.changed(id,this::refresh);
        today.table("tblTodayAppointments").getSelectionModel().selectedItemProperty().addListener((o,a,b)->{
            selected=b==null?null:(Appointment)b.get("_model");detail(today,false);
        });
        view.on("btnBookAppointment",context,()->appointmentForm(null));
        weekView.on("btnPreviousAppointmentWeek",context,()->{week=week.minusWeeks(1);refresh();});
        weekView.on("btnNextAppointmentWeek",context,()->{week=week.plusWeeks(1);refresh();});
        weekView.on("btnAppointmentWeekToday",context,()->{week=DemoClinicService.DEMO_DATE.with(DayOfWeek.MONDAY);refresh();});
        bindWalk();
    }
    @Override public void refresh(){
        ArrayList<Map<String,Object>> rows=new ArrayList<>();LocalDate day=today.date("dpAppointmentDay").getValue();
        for(Appointment a:context.service.appointments())if(a.date.equals(day)&&matches(a,today,"txtAppointmentSearch","cboAppointmentDentist")){
            rows.add(ViewAccess.row(a,a.start+"–"+a.end,context.service.patient(a.patientId).name,context.service.employee(a.dentistId).name,a.reason,a.status));
        }
        today.paged("tblTodayAppointments",rows,"tblTodayAppointmentsCount","tblTodayAppointmentsPrevious","tblTodayAppointmentsNext","tblTodayAppointmentsPage");
        selected=null;detail(today,false);detail(weekView,true);
        weekView.text("lblAppointmentWeekRange","Lịch hẹn tuần · "+ViewAccess.DATE.format(week)+" – "+ViewAccess.DATE.format(week.plusDays(6)));
        GridPane grid=(GridPane)weekView.find("appointmentWeekGrid");grid.getChildren().clear();
        for(int i=0;i<7;i++){
            LocalDate date=week.plusDays(i);VBox column=new VBox(8);column.setMinWidth(0);column.getStyleClass().add("rx-calendar-day");
            column.getChildren().add(label(ViewAccess.DATE.format(date),"rx-caption"));
            for(Appointment a:context.service.appointments())if(a.date.equals(date)&&matches(a,weekView,"txtWeekPatientSearch","cboWeekDentist")){
                String status=weekView.text("cboWeekStatus");if(!status.contains("Tất cả")&&!status.equals(a.status))continue;
                VBox card=new VBox(8,label(a.start+"–"+a.end,"rx-calendar-time"),
                    label(context.service.patient(a.patientId).name,"rx-detail-value"),label(context.service.employee(a.dentistId).name,"rx-caption"),label(a.status,"rx-caption"));
                card.setMinWidth(0);card.getStyleClass().add("rx-calendar-appointment");
                card.setOnMouseClicked(event->{if(context.active()){selected=a;detail(weekView,true);}});column.getChildren().add(card);
            }
            ScrollPane scroll=new ScrollPane(column);scroll.setFitToWidth(true);scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);scroll.setPrefHeight(460);scroll.setMinWidth(0);grid.add(scroll,i,0);
        }
    }
    private Label label(String value,String style){Label l=new Label(value);l.setWrapText(true);l.setMinHeight(Region.USE_PREF_SIZE);l.getStyleClass().add(style);return l;}
    private boolean matches(Appointment a,ViewAccess part,String search,String dentist){
        Object doctor=part.combo(dentist).getValue();
        if(doctor instanceof Employee e&&!e.id.equals(a.dentistId))return false;
        return PatientController.matches(context.service.patient(a.patientId),part.text(search))||a.id.toLowerCase(Locale.ROOT).contains(part.text(search).toLowerCase(Locale.ROOT));
    }
    private void detail(ViewAccess part,boolean weekly){
        part.show(weekly?"selectedWeekAppointmentPane":"selectedAppointmentPane",selected!=null);if(selected==null)return;
        Appointment target=selected;Patient patient=context.service.patient(target.patientId);
        part.text("lblAppointmentTitle","Chi tiết lịch hẹn · "+target.id);part.text("lblAppointmentStatus",target.status);
        part.text("lblAppointmentPatient",patient.toString());part.text("lblAppointmentPhone",patient.phone);
        part.text("lblAppointmentAgeGender",Period.between(patient.birthday,DemoClinicService.DEMO_DATE).getYears()+" tuổi · "+patient.gender);
        part.text("lblAppointmentDateTime",ViewAccess.DATE.format(target.date)+" · "+target.start+"–"+target.end);
        part.text("lblAppointmentDentistRoom",context.service.employee(target.dentistId).name);
        part.text("lblAppointmentReason",target.reason);part.text("lblAppointmentAllergy",patient.allergy);
        boolean locked=target.status.equals("DA_HUY")||target.status.equals("DA_DEN");
        part.disable("btnEditAppointment",locked);part.disable("btnCancelAppointment",locked);part.disable("btnAdmitAppointment",locked);
        part.on("btnEditAppointment",context,()->appointmentForm(target));
        part.on("btnCancelAppointment",context,()->{
            ViewAccess form=ViewAccess.load("receptionist/appointment-cancel.fxml");
            form.text("lblDialogTitle","Hủy lịch hẹn "+target.id+"?");form.text("lblCancelAppointmentSummary",patient+" · "+ViewAccess.DATE.format(target.date)+" · "+target.start);
            var stage=context.dialog(form,"Hủy lịch hẹn DEMO");
            form.on("btnConfirmCancelAppointment",context,()->{context.service.cancelAppointment(target,form.text("txtCancelAppointmentReason"));stage.close();refresh();});
        });
        part.on("btnAdmitAppointment",context,()->admission(target));
    }
    private void appointmentForm(Appointment old){
        boolean creating=old==null;ViewAccess form=ViewAccess.load("receptionist/appointment-"+(creating?"add":"edit")+".fxml");
        Patient[] patient={creating?null:context.service.patient(old.patientId)};
        form.text("txtAppointmentPatient",creating?"":patient[0].toString());
        form.choices("cboAppointmentDentist",context.service.dentists(),creating?context.service.dentists().get(0):context.service.employee(old.dentistId));
        form.date("dpAppointmentDate",creating?DemoClinicService.DEMO_DATE:old.date);
        ArrayList<String> times=new ArrayList<>();for(int m=480;m<1020;m+=30)if(m<720||m>=810)times.add(LocalTime.of(m/60,m%60).toString());
        form.choices("cboAppointmentTime",times,creating?"09:00":old.start.toString());
        form.text("txtAppointmentReason",creating?"":old.reason);form.text("txtAppointmentNotes",creating?"":old.notes);
        form.text("lblAppointmentFormRule","DEMO ONLY · Chọn bệnh nhân từ kết quả tìm kiếm; lịch hẹn dài 30 phút, trong ca nha sĩ.");
        Runnable search=()->{
            patient[0]=null;ArrayList<Map<String,Object>> result=new ArrayList<>();String q=form.text("txtAppointmentPatient");
            if(!q.isBlank())for(Patient p:context.service.patients())if(PatientController.matches(p,q))result.add(ViewAccess.row(p,p.id,p.name,p.phone));
            form.rows("tblAppointmentPatientResults",result);
        };
        form.rows("tblAppointmentPatientResults",List.of());
        form.changed("txtAppointmentPatient",search);
        form.table("tblAppointmentPatientResults").getSelectionModel().selectedItemProperty().addListener((o,a,b)->{if(b!=null)patient[0]=(Patient)b.get("_model");});
        var stage=context.dialog(form,creating?"Đặt lịch DEMO":"Sửa lịch DEMO");String id=creating?context.service.nextId("LH"):old.id;
        form.on("btnSaveAppointment",context,()->{
            if(patient[0]==null)throw new IllegalArgumentException("Chọn bệnh nhân trong danh sách kết quả.");
            Employee doctor=(Employee)form.combo("cboAppointmentDentist").getValue();if(doctor==null)throw new IllegalArgumentException("Chọn nha sĩ.");
            LocalTime time=LocalTime.parse(form.text("cboAppointmentTime"));
            context.service.saveAppointment(new Appointment(id,patient[0].id,doctor.id,form.date("dpAppointmentDate").getValue(),time,time.plusMinutes(30),
                DemoClinicService.required(form.text("txtAppointmentReason"),"Lý do khám",500),form.text("txtAppointmentNotes"),creating?"DA_XAC_NHAN":old.status),creating);
            stage.close();refresh();
        });
    }
    private void admission(Appointment target){
        ViewAccess form=ViewAccess.load("receptionist/admission.fxml");Patient p=context.service.patient(target.patientId);
        form.text("txtAdmissionPatient",p.toString());form.text("txtAdmissionAppointment",target.id);
        form.choices("cboAdmissionDentist",List.of(context.service.employee(target.dentistId)),context.service.employee(target.dentistId));
        String room=context.service.roomFor(target.dentistId,target.date,target.start,target.end);
        form.choices("cboAdmissionRoom",List.of(room),room);form.text("txtAdmissionReason",target.reason);
        var stage=context.dialog(form,"Tiếp nhận theo hẹn DEMO");
        form.on("btnConfirmAdmission",context,()->{
            context.service.admit(target.patientId,target.dentistId,target.date,target.start,form.text("txtAdmissionReason"),target);
            stage.close();refresh();
        });
    }
    private void bindWalk(){
        walk.choices("cboWalkInDentist",context.service.dentists(),context.service.dentists().get(0));
        walk.date("dpWalkInDate",DemoClinicService.DEMO_DATE);
        walk.on("btnSearchWalkInPatient",context,this::findWalkPatient);
        walk.on("btnCreateWalkInPatient",context,()->PatientController.openForm(context,null,p->{walkPatient=p;showWalk("found");}));
        walk.on("btnChangeWalkInPatient",context,()->{walkPatient=null;showWalk("");});
        walk.on("btnResetWalkIn",context,()->{walkPatient=null;showWalk("");});
        walk.changed("cboWalkInDentist",this::refreshSlots);walk.changed("dpWalkInDate",this::refreshSlots);
        walk.on("btnAdmitWalkIn",context,()->{
            if(walkPatient==null||walkTime==null)throw new IllegalArgumentException("Chọn bệnh nhân và ca khả dụng.");
            Employee e=(Employee)walk.combo("cboWalkInDentist").getValue();
            context.service.admit(walkPatient.id,e.id,walk.date("dpWalkInDate").getValue(),walkTime,walk.text("txtWalkInReason"),null);
            walkPatient=null;showWalk("");refresh();
        });
        refreshSlots();
    }
    private void findWalkPatient(){
        String q=walk.text("txtWalkInPatientSearch");if(q.isBlank())throw new IllegalArgumentException("Nhập mã, họ tên hoặc SĐT bệnh nhân.");
        ArrayList<Patient> matches=new ArrayList<>();for(Patient p:context.service.patients())if(PatientController.matches(p,q))matches.add(p);
        if(matches.size()>1)throw new IllegalArgumentException("Có nhiều hồ sơ phù hợp. Nhập mã bệnh nhân hoặc SĐT cụ thể.");
        walkPatient=matches.isEmpty()?null:matches.get(0);showWalk(walkPatient==null?"not-found":"found");
    }
    private void showWalk(String state){
        String q=walk.text("txtWalkInPatientSearch");
        walk=ViewAccess.load("receptionist/walk-in"+(state.isEmpty()?"":"-"+state)+".fxml");
        ((TabPane)view.find("intakeTabs")).getTabs().get(2).setContent(walk.root);bindWalk();walk.text("txtWalkInPatientSearch",q);
        if(walkPatient!=null){
            walk.text("lblWalkInSelectedName",walkPatient.name);walk.text("lblWalkInSelectedId",walkPatient.id);
            walk.text("lblWalkInSelectedPhone",walkPatient.phone);walk.text("lblWalkInSelectedAge",walkPatient.gender);
            walk.text("lblWalkInAllergy",walkPatient.allergy);walk.text("txtWalkInReason","");
        }
    }
    private void refreshSlots(){
        slots.clear();walkTime=null;Object value=walk.combo("cboWalkInDentist").getValue();LocalDate date=walk.date("dpWalkInDate").getValue();
        if(walkPatient!=null&&value instanceof Employee e&&date!=null){
            for(int m=480;m<1020&&slots.size()<3;m+=30){
                LocalTime start=LocalTime.of(m/60,m%60);boolean busy=false;
                for(Appointment a:context.service.appointments())if(a.dentistId.equals(e.id)&&a.date.equals(date)&&!a.status.equals("DA_HUY")&&start.isBefore(a.end)&&a.start.isBefore(start.plusMinutes(30)))busy=true;
                if(busy)continue;
                try{context.service.roomFor(e.id,date,start,start.plusMinutes(30));slots.add(start);}
                catch(IllegalArgumentException outsideShift){/* Ca ngoài phân công không xuất hiện trong lựa chọn. */}
            }
        }
        for(int i=0;i<3;i++){
            var node=walk.find("walkInSlot"+i);if(node==null)continue;boolean available=i<slots.size();node.setVisible(available);node.setManaged(available);
            if(available){
                final LocalTime time=slots.get(i);walk.text("lblWalkInSlotTime"+i,time+"–"+time.plusMinutes(30));
                Employee doctor=(Employee)walk.combo("cboWalkInDentist").getValue();
                walk.text("lblWalkInSlotRoom"+i,context.service.roomFor(doctor.id,date,time,time.plusMinutes(30))+" · "+doctor.name);
                node.setOnMouseClicked(event->{if(context.active()){walkTime=time;for(int j=0;j<3;j++)if(walk.find("walkInSlot"+j)!=null)walk.find("walkInSlot"+j).setStyle("");node.setStyle("-fx-border-color: #087E91;");}});
            }
        }
        walk.disable("btnAdmitWalkIn",walkPatient==null||slots.isEmpty());
    }
}
