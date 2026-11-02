package vn.edu.nhom19.nhakhoa.view.dentist;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.function.Consumer;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import vn.edu.nhom19.nhakhoa.auth.Role;
import vn.edu.nhom19.nhakhoa.entity.DemoData.*;
import vn.edu.nhom19.nhakhoa.service.DemoClinicService;
import vn.edu.nhom19.nhakhoa.view.common.*;

/**
 * UC013/014 DEMO: hàng chờ -> chọn phiếu -> workspace đúng bệnh nhân.
 * Chỉ Service quyết định trạng thái/giá snapshot; Controller đọc form và cập nhật UI.
 * Dữ liệu nhập chưa lưu được cảnh báo khi đổi trang/đăng xuất; không tạo phiếu mới khi tiếp tục.
 */
public final class ExaminationController extends ScreenController {
    private final ViewAccess queue;
    private ViewAccess clinical;
    private Exam current;
    private boolean dirty;
    private final TreeSet<Integer> teeth=new TreeSet<>();
    public ExaminationController(ViewAccess view,ControllerContext context){
        super(view,context);context.service.require(Role.DENTIST);queue=view;
        queue.date("dpWaitingDate",DemoClinicService.DEMO_DATE);
        queue.choices("cboWaitingRoom",List.of("Tất cả phòng","P01","P02"),"Tất cả phòng");
        for(String id:List.of("txtWaitingPatientSearch","dpWaitingDate","cboWaitingRoom"))queue.changed(id,this::queueRows);
        queue.table("tblWaitingPatients").getSelectionModel().selectedItemProperty().addListener((o,a,b)->detail(b==null?null:(Exam)b.get("_model")));
    }
    public void openQueue(){current=null;dirty=false;view=queue;queueRows();}
    @Override public void refresh(){if(current==null)queueRows();}
    private void queueRows(){
        ArrayList<Map<String,Object>> rows=new ArrayList<>();String q=queue.text("txtWaitingPatientSearch").toLowerCase(Locale.ROOT);
        for(Exam e:context.service.exams()){
            if(e.status.equals("DA_KHAM")||!e.received.toLocalDate().equals(queue.date("dpWaitingDate").getValue()))continue;
            if(!(e.id+" "+context.service.patient(e.patientId)).toLowerCase(Locale.ROOT).contains(q))continue;
            String room=queue.text("cboWaitingRoom");if(!room.contains("Tất cả")&&!room.equals(e.room))continue;
            rows.add(ViewAccess.row(e,e.received.toLocalTime(),e.id,context.service.patient(e.patientId).toString(),e.reason,e.status));
        }
        queue.paged("tblWaitingPatients",rows,"tblWaitingPatientsCount","tblWaitingPatientsPrevious","tblWaitingPatientsNext","tblWaitingPatientsPage");
        detail(null);
    }
    private void detail(Exam exam){
        queue.show("noPatientPanel",exam==null);queue.show("selectedPatientDetail",exam!=null);
        VBox host=(VBox)queue.find("selectedPatientDetail");host.getChildren().clear();
        if(exam==null)return;
        Patient p=context.service.patient(exam.patientId);ViewAccess detail=ViewAccess.load("dentist/queue-patient-detail.fxml");
        detail.text("lblSelectedPatientId",p.id+" · "+exam.id);detail.text("lblSelectedPatientName",p.name);
        detail.text("lblSelectedPatientAgeGender",Period.between(p.birthday,DemoClinicService.DEMO_DATE).getYears()+" tuổi · "+p.gender);
        detail.text("lblSelectedPatientStatus",exam.status.equals("DANG_KHAM")?"Đang khám":"Chờ khám");
        detail.text("lblPatientAllergy",p.allergy);detail.text("lblPatientReason",exam.reason);
        detail.text("lblPatientIntakeTime",exam.received.toLocalTime()+" · "+context.service.employee(exam.receptionistId).name);
        detail.text("btnStartExamination",exam.status.equals("DANG_KHAM")?"Tiếp tục khám":"Bắt đầu khám");
        detail.on("btnStartExamination",context,()->openWorkspace(exam));
        host.getChildren().setAll(detail.root);
    }
    public void selectExam(String id,boolean resume){
        Exam e=context.service.exam(id);
        if(resume){openWorkspace(e);return;}
        openQueue();boolean found=false;
        for(Map<String,Object> row:queue.table("tblWaitingPatients").getItems())if(row.get("_model")==e){queue.table("tblWaitingPatients").getSelectionModel().select(row);found=true;break;}
        if(!found)detail(e);
        context.replace(this);
    }
    private void openWorkspace(Exam exam){
        context.service.startExam(exam);current=exam;view=ViewAccess.load("dentist/examination-workspace.fxml");
        clinical=view.sub("tabExamClinicalContent");dirty=false;
        Patient p=context.service.patient(exam.patientId);
        view.text("lblPageSubtitle","Phiếu "+exam.id+" · Dữ liệu DEMO trong bộ nhớ");
        String[] nameParts=p.name.trim().split("\\s+");String initials="";
        for(int i=Math.max(0,nameParts.length-2);i<nameParts.length;i++)initials+=nameParts[i].substring(0,1);
        view.text("lblPatientAvatar",initials.toUpperCase(Locale.forLanguageTag("vi")));
        view.text("lblPatientName",p.toString());view.text("lblPatientDemographics",ViewAccess.DATE.format(p.birthday)+" · "+p.gender+" · "+exam.id);
        view.text("lblExamStatus","Đang khám");view.text("lblExamDentist",context.service.employee(exam.dentistId).name);
        view.text("lblExamRoom",exam.room);view.text("lblExamReason",exam.reason);view.text("lblExamAllergy",p.allergy);
        clinical.text("txtClinicalCondition",exam.clinical);clinical.text("txtDiagnosis",exam.diagnosis);clinical.text("txtTreatmentNotes",exam.notes);clinical.text("txtDoctorAdvice",exam.advice);
        teeth.clear();if(!exam.teeth.isBlank())for(String t:exam.teeth.split(","))teeth.add(Integer.parseInt(t.trim()));
        renderTeeth();
        for(String id:List.of("txtClinicalCondition","txtDiagnosis","txtTreatmentNotes","txtDoctorAdvice"))clinical.changed(id,()->dirty=true);
        clinical.on("clinicalTeethSelect",context,()->toothDialog(teeth,false,value->{teeth.clear();teeth.addAll(value);dirty=true;renderTeeth();}));
        clinical.on("btnAddTreatmentService",context,()->treatmentForm(null));clinical.on("btnAddPrescriptionMedicine",context,()->medicineForm(null));
        view.on("btnSaveDraft",context,()->{saveDraft();view.text("lblExamSaveStatus","Đã lưu nháp DEMO trong bộ nhớ · Chưa ghi SQL");});
        view.on("btnCompleteExam",context,this::complete);
        bindReadonly(p,exam);refreshRows();
        ((TabPane)view.find("examinationTabs")).getSelectionModel().select(2);
        dirty=false;context.replace(this);
    }
    private void bindReadonly(Patient p,Exam exam){
        ViewAccess overview=view.sub("tabExamOverviewContent"),record=view.sub("tabExamMedicalRecordContent"),history=view.sub("tabExamHistoryContent");
        overview.text("lblOverviewPatient",p.toString());overview.text("lblOverviewBirthGender",ViewAccess.DATE.format(p.birthday)+" · "+p.gender);
        overview.text("lblOverviewPhone",p.phone);overview.text("lblOverviewIntake",exam.received.toString());
        overview.text("lblOverviewReason",exam.reason);overview.text("lblOverviewMedicalHistory",p.allergy);
        overview.text("lblLastExamDoctor","Chưa có lần khám trước");overview.text("lblLastExamCode","—");overview.text("lblLastDiagnosis","—");overview.text("lblLastService","—");overview.text("lblLastPrescription","—");
        record.text("lblRecordAllergy",p.allergy);record.text("lblRecordAllergyWarning",p.allergy);
        record.text("lblSystemicHistory","Chưa có dữ liệu tiền sử trong phiên DEMO");record.text("lblDentalHistory","Chưa có dữ liệu lịch sử điều trị");
        record.text("lblRecordNotes","");record.text("lblRecordCreated","Hồ sơ DEMO · "+p.id);record.text("lblSavedExamCount","0 lần");record.text("lblRecordUpdated","—");
        for(String id:List.of("lblSavedExamDate","lblSavedExamDentist","lblSavedSymptoms","lblSavedClinicalResult","lblSavedDiagnosis","lblSavedNotes"))record.text(id,"—");
        for(String id:List.of("lblHistoryVisit","lblHistoryDiagnosis","lblHistoryServices","lblHistoryPrescription","lblHistoryNotes"))history.text(id,"Chưa có lịch sử phù hợp");
        history.date("dpHistoryFrom",DemoClinicService.DEMO_DATE.minusYears(10));history.date("dpHistoryTo",DemoClinicService.DEMO_DATE);
        ArrayList<Object> doctors=new ArrayList<>();doctors.add("Tất cả nha sĩ");doctors.addAll(context.service.dentists());history.choices("cboHistoryDentist",doctors,doctors.get(0));
        Runnable loadHistory=()->{
            LocalDate from=history.date("dpHistoryFrom").getValue(),to=history.date("dpHistoryTo").getValue();
            if(from==null||to==null||from.isAfter(to)){history.rows("tblExamHistory",List.of());return;}
            ArrayList<Map<String,Object>> rows=new ArrayList<>();int count=0;
            for(Exam old:context.service.exams())if(old.patientId.equals(p.id)&&old.status.equals("DA_KHAM")&&!old.id.equals(exam.id)){
                count++;Object doctor=history.combo("cboHistoryDentist").getValue();
                if(old.received.toLocalDate().isBefore(from)||old.received.toLocalDate().isAfter(to)||(doctor instanceof Employee d&&!d.id.equals(old.dentistId)))continue;
                rows.add(ViewAccess.row(old,ViewAccess.DATE.format(old.received.toLocalDate()),context.service.employee(old.dentistId).name,old.diagnosis,old.treatments.size()+" dịch vụ",old.medicines.size()+" thuốc"));
                overview.text("lblLastExamDoctor",old.received.toLocalDate()+" · "+context.service.employee(old.dentistId).name);overview.text("lblLastExamCode",old.id);
                overview.text("lblLastDiagnosis",old.diagnosis);overview.text("lblLastService",old.treatments.size()+" dịch vụ");overview.text("lblLastPrescription",old.medicines.size()+" thuốc");
                record.text("lblSavedExamDate",old.received.toString());record.text("lblSavedExamDentist",context.service.employee(old.dentistId).name);
                record.text("lblSavedSymptoms",old.reason);record.text("lblSavedClinicalResult",old.clinical);record.text("lblSavedDiagnosis",old.diagnosis);record.text("lblSavedNotes",old.notes);
            }
            record.text("lblSavedExamCount",count+" lần");history.rows("tblExamHistory",rows);history.text("tblExamHistoryCount","Hiển thị "+rows.size()+" / "+rows.size());
        };
        for(String id:List.of("dpHistoryFrom","dpHistoryTo","cboHistoryDentist"))history.changed(id,loadHistory);loadHistory.run();
        history.table("tblExamHistory").getSelectionModel().selectedItemProperty().addListener((o,a,b)->{
            if(b==null)return;Exam old=(Exam)b.get("_model");history.text("lblHistoryVisit",old.id+" · "+old.received.toLocalDate());
            history.text("lblHistoryDiagnosis",old.diagnosis);history.text("lblHistoryServices",old.treatments.size()+" dịch vụ");history.text("lblHistoryPrescription",old.medicines.size()+" thuốc");history.text("lblHistoryNotes",old.notes);
        });
    }
    private void saveDraft(){
        context.service.saveExam(current,clinical.text("txtClinicalCondition"),clinical.text("txtDiagnosis"),clinical.text("txtTreatmentNotes"),clinical.text("txtDoctorAdvice"),teethText());
        dirty=false;
    }
    private String teethText(){StringJoiner result=new StringJoiner(",");for(int tooth:teeth)result.add(String.valueOf(tooth));return result.toString();}
    private void renderTeeth(){
        ArrayList<Integer> values=new ArrayList<>(teeth);
        for(int i=0;i<2;i++){
            final int index=i;String id=i==0?"clinicalTeethRemove36":"clinicalTeethRemove37";
            clinical.show(id,i<values.size());if(i>=values.size())continue;
            int value=values.get(i);clinical.text(id,value==0?"Toàn hàm ×":value+" ×");
            clinical.on(id,context,()->{teeth.remove(values.get(index));dirty=true;renderTeeth();});
        }
        clinical.text("lblFdiRule","FDI đã chọn: "+(teeth.isEmpty()?"Chưa chọn":teethText())+" · 0 = toàn hàm · DEMO");
    }
    private Button action(String label,Runnable action){
        Button button=new Button(label);button.getStyleClass().addAll("secondary-button","dt-mini-button");
        button.setOnAction(event->{if(context.active())try{action.run();}catch(RuntimeException ex){view.error(ex.getMessage());}});
        return button;
    }
    private void refreshRows(){
        ArrayList<Map<String,Object>> services=new ArrayList<>(),medicines=new ArrayList<>();BigDecimal total=BigDecimal.ZERO;int quantity=0;
        for(Treatment t:current.treatments){
            Button status=action(t.performed?"Đã thực hiện":"Chưa thực hiện",()->{context.service.setPerformed(current,t);refreshRows();});
            HBox actions=new HBox(6,action("Sửa",()->treatmentForm(t)),action("Xóa",()->{
                if(view.confirm("Bỏ dịch vụ "+t.name+" khỏi phiếu DEMO?")){context.service.removeTreatment(current,t);refreshRows();}
            }));
            services.add(ViewAccess.row(t,t.name,t.tooth==0?"Toàn hàm":t.tooth,1,ViewAccess.amount(t.price),status,actions));total=total.add(t.price);
        }
        for(Medicine m:current.medicines){
            medicines.add(ViewAccess.row(m,m.name,m.strength,m.quantity,m.unit,m.dosage,m.instructions,
                new HBox(6,action("Sửa",()->medicineForm(m)),action("Xóa",()->removeMedicine(m)))));quantity+=m.quantity;
        }
        clinical.rows("tblTreatmentServices",services);clinical.rows("tblPrescriptionMedicines",medicines);
        clinical.text("lblServiceTotal",ViewAccess.amount(total));
        for(ViewAccess part:List.of(clinical,view.sub("tabExamOverviewContent"))){
            part.text("lblSummaryServices",services.size()+" dịch vụ · "+ViewAccess.amount(total));part.text("lblSummaryTotal",ViewAccess.amount(total));
            part.text("lblSummaryPrescription",medicines.size()+" thuốc");part.text("lblSummaryMedicineQuantity",quantity+" đơn vị");
            part.text("lblSummaryAllergy",context.service.patient(current.patientId).allergy);part.text("lblPreviousVisit","Xem lịch sử của bệnh nhân đang chọn");
        }
        String allergy=context.service.patient(current.patientId).allergy;
        boolean hasAllergy=!allergy.isBlank()&&!allergy.equalsIgnoreCase("Không ghi nhận");
        clinical.text("lblPrescriptionAllergy",allergy);clinical.show("lblPrescriptionAllergy",hasAllergy);
        for(ViewAccess part:List.of(clinical,view.sub("tabExamOverviewContent")))part.show("lblSummaryAllergy",hasAllergy);
        view.sub("tabExamMedicalRecordContent").show("lblRecordAllergyWarning",hasAllergy);
    }
    private void treatmentForm(Treatment old){
        boolean creating=old==null;ViewAccess form=ViewAccess.load("dentist/treatment-service-"+(creating?"add":"edit")+".fxml");
        ArrayList<DentalService> services=new ArrayList<>();for(DentalService s:context.service.services())if(s.active||old!=null&&s.id.equals(old.serviceId))services.add(s);
        if(services.isEmpty())throw new IllegalStateException("Chưa có dịch vụ đang áp dụng.");
        DentalService selected=creating?services.get(0):context.service.dentalService(old.serviceId);
        form.choices("cboTreatmentService",services,selected);
        form.text("txtServiceQuantity","1");form.disable("txtServiceQuantity",true);
        int[] tooth={creating?0:old.tooth};
        Runnable price=()->{DentalService s=(DentalService)form.combo("cboTreatmentService").getValue();if(s!=null)form.text("txtServiceUnitPrice",ViewAccess.amount(old!=null&&old.serviceId.equals(s.id)?old.price:s.price));};
        form.changed("cboTreatmentService",price);price.run();
        form.text("serviceTeethRemove36",tooth[0]==0?"Toàn hàm":tooth[0]);
        form.on("serviceTeethRemove36",context,()->{tooth[0]=0;form.text("serviceTeethRemove36","Toàn hàm");});
        form.on("serviceTeethSelect",context,()->toothDialog(Set.of(tooth[0]),true,chosen->{tooth[0]=chosen.isEmpty()?0:chosen.iterator().next();form.text("serviceTeethRemove36",tooth[0]==0?"Toàn hàm":tooth[0]);}));
        var stage=context.dialog(form,creating?"Thêm dịch vụ điều trị DEMO":"Sửa dịch vụ điều trị DEMO");
        form.on("btnSaveService",context,()->{
            DentalService s=(DentalService)form.combo("cboTreatmentService").getValue();if(s==null)throw new IllegalArgumentException("Chọn dịch vụ.");
            context.service.saveTreatment(current,new Treatment(creating?context.service.nextId("CT"):old.id,s.id,s.name,tooth[0],s.price,!creating&&old.performed),creating);
            stage.close();refreshRows();
        });
    }
    private void medicineForm(Medicine old){
        boolean creating=old==null;ViewAccess form=ViewAccess.load("dentist/prescription-medicine-"+(creating?"add":"edit")+".fxml");
        form.text("txtMedicineName",creating?"":old.name);form.text("txtMedicineStrength",creating?"":old.strength);
        form.text("txtMedicineQuantity",creating?"":old.quantity);
        form.choices("cboMedicineUnit",List.of("viên","gói","chai","ống"),creating?"viên":old.unit);
        form.choices("cboMedicineRoute",List.of("Uống","Bôi","Ngậm","Nhỏ"),creating?"Uống":old.route);
        form.text("txtMedicineDays",creating?"1":old.days);
        form.text("txtDoseMorning",creating?"":old.morning);form.text("txtDoseNoon",creating?"":old.noon);
        form.text("txtDoseAfternoon",creating?"":old.afternoon);form.text("txtDoseNight",creating?"":old.night);
        form.text("txtMedicineInstructions",creating?"":old.userInstructions);
        var stage=context.dialog(form,creating?"Thêm thuốc DEMO":"Sửa thuốc DEMO");
        form.on("btnSaveMedicine",context,()->{
            int qty,days;try{qty=Integer.parseInt(form.text("txtMedicineQuantity"));days=Integer.parseInt(form.text("txtMedicineDays"));}catch(NumberFormatException ex){throw new IllegalArgumentException("Số lượng và số ngày phải là số nguyên.");}
            if(days<=0)throw new IllegalArgumentException("Số ngày phải lớn hơn 0.");
            BigDecimal doses=BigDecimal.ZERO;
            for(String id:List.of("txtDoseMorning","txtDoseNoon","txtDoseAfternoon","txtDoseNight")){
                String s=form.text(id);if(s.isBlank())continue;BigDecimal n;
                try{n=new BigDecimal(s.replace(",","."));}catch(NumberFormatException ex){throw new IllegalArgumentException("Liều dùng phải là số không âm.");}
                if(n.signum()<0)throw new IllegalArgumentException("Liều dùng không được âm.");doses=doses.add(n);
            }
            if(doses.signum()<=0)throw new IllegalArgumentException("Nhập ít nhất một liều trong ngày.");
            String dosage="Sáng "+form.text("txtDoseMorning")+"; Trưa "+form.text("txtDoseNoon")+"; Chiều "+form.text("txtDoseAfternoon")+"; Tối "+form.text("txtDoseNight")+"; "+days+" ngày";
            Medicine m=new Medicine(creating?context.service.nextId("TH"):old.id,form.text("txtMedicineName"),form.text("txtMedicineStrength"),qty,form.text("cboMedicineUnit"),dosage,form.text("cboMedicineRoute")+" · "+form.text("txtMedicineInstructions"));
            m.morning=form.text("txtDoseMorning");m.noon=form.text("txtDoseNoon");m.afternoon=form.text("txtDoseAfternoon");m.night=form.text("txtDoseNight");
            m.days=days;m.route=form.text("cboMedicineRoute");m.userInstructions=form.text("txtMedicineInstructions");
            context.service.saveMedicine(current,m,creating);stage.close();refreshRows();
        });
    }
    private void removeMedicine(Medicine medicine){
        ViewAccess form=ViewAccess.load("dentist/prescription-medicine-remove.fxml");
        form.text("lblMedicineToRemove",medicine.name+" · "+medicine.quantity+" "+medicine.unit);form.text("lblRemoveMedicineNotice","Bỏ thuốc khỏi đơn DEMO của "+context.service.patient(current.patientId)+" · "+current.id);
        var stage=context.dialog(form,"Bỏ thuốc DEMO");
        form.on("btnConfirmRemoveMedicine",context,()->{context.service.removeMedicine(current,medicine);stage.close();refreshRows();});
    }
    private void toothDialog(Set<Integer> selected,boolean single,Consumer<Set<Integer>> accepted){
        ViewAccess form=ViewAccess.load("dentist/tooth-selection-popover.fxml");
        CheckBox whole=(CheckBox)form.find("chkWholeMouth");whole.setSelected(selected.contains(0));
        for(int q=1;q<=4;q++)for(int n=1;n<=8;n++){
            final int tooth=q*10+n;ToggleButton button=(ToggleButton)form.find("btnTooth"+tooth);button.setSelected(selected.contains(tooth));
            button.setOnAction(event->{
                whole.setSelected(false);
                if(single&&button.isSelected())for(int a=1;a<=4;a++)for(int b=1;b<=8;b++)if(a*10+b!=tooth)((ToggleButton)form.find("btnTooth"+(a*10+b))).setSelected(false);
            });
        }
        whole.setOnAction(event->{if(whole.isSelected())for(int q=1;q<=4;q++)for(int n=1;n<=8;n++)((ToggleButton)form.find("btnTooth"+(q*10+n))).setSelected(false);});
        var stage=context.dialog(form,"Chọn răng FDI");
        form.on("btnConfirmTeeth",context,()->{
            Set<Integer> result=new TreeSet<>();if(whole.isSelected())result.add(0);
            else for(int q=1;q<=4;q++)for(int n=1;n<=8;n++)if(((ToggleButton)form.find("btnTooth"+(q*10+n))).isSelected())result.add(q*10+n);
            if(single&&result.size()!=1)throw new IllegalArgumentException("Mỗi chi tiết dịch vụ chỉ có một vị trí răng hoặc toàn hàm theo schema.");
            accepted.accept(result);stage.close();
        });
    }
    private void complete(){
        DemoClinicService.required(clinical.text("txtDiagnosis"),"Chẩn đoán",1000);
        ViewAccess form=ViewAccess.load("dentist/exam-complete-confirm.fxml");Patient p=context.service.patient(current.patientId);
        form.text("lblCompletePatient",p+" · "+current.id);form.text("lblConfirmPatient",p.toString());form.text("lblConfirmExam",current.id);
        form.text("lblConfirmDiagnosis",clinical.text("txtDiagnosis"));form.text("lblConfirmDentist",context.service.employee(current.dentistId).name);
        form.text("lblConfirmTotal",ViewAccess.amount(context.service.performedTotal(current)));
        form.text("lblConfirmCompleteRule","DEMO ONLY · Hoàn tất phiếu trong bộ nhớ. Hóa đơn sau này chỉ lấy dịch vụ đã thực hiện.");
        ArrayList<Map<String,Object>> rows=new ArrayList<>();for(Treatment t:current.treatments)rows.add(ViewAccess.row(t,t.name+" · "+(t.performed?"Đã thực hiện":"Chưa thực hiện"),1,t.performed?ViewAccess.amount(t.price):"Không lập hóa đơn"));
        for(Medicine m:current.medicines)rows.add(ViewAccess.row(m,m.name,m.quantity,"Không tính tiền"));
        form.rows("tblCompleteExamItems",rows);
        var stage=context.dialog(form,"Hoàn tất khám DEMO");
        form.on("btnConfirmCompleteExam",context,()->{
            saveDraft();context.service.completeExam(current);dirty=false;stage.close();
            clinical.root.setDisable(true);view.disable("btnSaveDraft",true);view.disable("btnCompleteExam",true);
            view.text("lblExamStatus","Đã khám");view.text("lblExamSaveStatus","Đã hoàn tất DEMO · Nội dung chỉ đọc");
        });
    }
    @Override public boolean canLeave(){
        if(!dirty)return true;
        ButtonType save=new ButtonType("Lưu nháp"),discard=new ButtonType("Bỏ thay đổi"),cancel=new ButtonType("Ở lại",ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION,"Nội dung khám chưa lưu. Bạn muốn xử lý thế nào?",save,discard,cancel);
        if(view.root.getScene()!=null)alert.initOwner(view.root.getScene().getWindow());alert.setHeaderText("Phiếu "+current.id+" · DEMO");
        ButtonType choice=alert.showAndWait().orElse(cancel);
        if(choice==cancel)return false;
        if(choice==save)try{saveDraft();}catch(RuntimeException ex){view.error(ex.getMessage());return false;}
        dirty=false;return true;
    }
    @Override public void dispose(){current=null;clinical=null;teeth.clear();dirty=false;}
}
