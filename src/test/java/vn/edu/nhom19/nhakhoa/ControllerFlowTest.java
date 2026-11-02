package vn.edu.nhom19.nhakhoa;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.*;
import org.junit.jupiter.api.*;
import vn.edu.nhom19.nhakhoa.app.*;
import vn.edu.nhom19.nhakhoa.auth.*;
import vn.edu.nhom19.nhakhoa.view.common.ViewAccess;

/** Kiểm thử bằng nút UI thật; không mock Controller hoặc dùng cơ sở dữ liệu. */
class ControllerFlowTest {
    private Stage stage;
    private AppNavigator navigator;
    private UserSession session;
    @BeforeAll static void toolkit()throws Exception{
        CountDownLatch ready=new CountDownLatch(1);try{Platform.startup(ready::countDown);}catch(IllegalStateException started){ready.countDown();}
        assertTrue(ready.await(10,TimeUnit.SECONDS));fx(()->{Platform.setImplicitExit(false);return null;});
    }
    private static <T>T fx(Callable<T> work)throws Exception{FutureTask<T> task=new FutureTask<>(work);Platform.runLater(task);return task.get(30,TimeUnit.SECONDS);}
    @BeforeEach void setup()throws Exception{fx(()->{stage=new Stage();session=new UserSession();navigator=new AppNavigator(stage,session);navigator.start();return null;});}
    @AfterEach void close()throws Exception{fx(()->{navigator.close();for(Window window:List.copyOf(Window.getWindows()))if(window instanceof Stage dialog)dialog.close();stage.close();return null;});}
    private ViewAccess ui(){return new ViewAccess(stage.getScene().getRoot());}
    private ViewAccess modal(String title){
        for(Window window:List.copyOf(Window.getWindows()))if(window instanceof Stage dialog&&dialog!=stage&&dialog.getTitle().contains(title))return new ViewAccess(dialog.getScene().getRoot());
        throw new AssertionError("Không có modal: "+title);
    }
    private void click(String id){Button button=ui().button(id);assertNotNull(button,id);assertFalse(button.isDisabled(),id);button.fire();}
    @Test void everyImplementedRouteLoadsItsControllerWithoutFallback()throws Exception{fx(()->{
        Map<Role,List<String>> routes=Map.of(
            Role.MANAGER,List.of("btnStaff","btnAssignments","btnServices","btnReports","btnDashboard"),
            Role.RECEPTIONIST,List.of("btnIntakeAppointments","btnPatientLookup","btnCashier","btnReceptionistSchedule","btnDashboard"),
            Role.DENTIST,List.of("btnExamination","btnDentistSchedule","btnDashboard"));
        for(Role role:routes.keySet()){
            navigator.openDemoRole(role);
            for(String id:routes.get(role)){click(id);assertNull(ui().find("lblPageMessage"),role+" "+id);}
            navigator.logout();
        }return null;
    });}
    @Test void serviceAddEditCancelAndClosedFormCannotSaveAgain()throws Exception{fx(()->{
        navigator.openDemoRole(Role.MANAGER);click("btnServices");
        int count=ui().table("tblServices").getItems().size();
        click("btnAddService");ViewAccess form=modal("Thêm dịch vụ");assertEquals("",form.text("txtServiceName"));
        form.text("txtServiceName","Dịch vụ kiểm thử");form.text("txtServicePrice","150000");form.text("txtServiceDuration","30");
        Button save=form.button("btnSaveService");save.fire();assertTrue(save.isDisabled());save.fire();
        assertEquals(count+1,ui().table("tblServices").getItems().size());
        var row=ui().table("tblServices").getItems().get(count);ui().table("tblServices").getSelectionModel().select(row);
        assertTrue(ui().find("serviceDetailsPane").isVisible());click("btnEditService");
        ViewAccess edit=modal("Sửa dịch vụ");edit.text("txtServiceName","Không được lưu");edit.button("btnCancelServiceForm").fire();
        assertEquals("Dịch vụ kiểm thử",row.get("v1"));
        click("btnEditService");edit=modal("Sửa dịch vụ");edit.text("txtServicePrice","200000");edit.button("btnSaveService").fire();
        assertTrue(ui().table("tblServices").getItems().get(count).get("v4").toString().contains("200.000"));
        return null;
    });}
    @Test void appointmentSelectionAdmissionAndCashPaymentRefreshRealDemoData()throws Exception{fx(()->{
        navigator.openDemoRole(Role.RECEPTIONIST);click("btnIntakeAppointments");
        ViewAccess today=ui().sub("tabTodayContent");assertFalse(today.find("selectedAppointmentPane").isVisible());
        var row=today.table("tblTodayAppointments").getItems().get(1);today.table("tblTodayAppointments").getSelectionModel().select(row);
        assertTrue(today.find("selectedAppointmentPane").isVisible());today.button("btnAdmitAppointment").fire();
        ViewAccess admission=modal("Tiếp nhận theo hẹn");admission.button("btnConfirmAdmission").fire();
        assertEquals("DA_DEN",today.table("tblTodayAppointments").getItems().get(1).get("v4"));
        click("btnCashier");ViewAccess waiting=ui().sub("tabAwaitingPaymentContent");
        assertFalse(waiting.find("selectedInvoicePane").isVisible());
        int before=waiting.table("tblWaitingInvoices").getItems().size();
        waiting.table("tblWaitingInvoices").getSelectionModel().selectFirst();waiting.button("btnPayCash").fire();
        ViewAccess cash=modal("Thu tiền mặt");cash.text("txtCashTendered","500000");assertTrue(cash.text("txtCashChange").contains("200.000"));
        cash.button("btnConfirmCashPayment").fire();assertEquals(before-1,waiting.table("tblWaitingInvoices").getItems().size());
        return null;
    });}
    @Test void dentistDashboardResumesCorrectExamAndDraftSurvivesNavigation()throws Exception{fx(()->{
        navigator.openDemoRole(Role.DENTIST);
        String expected=ui().text("lblNextExamId");click("btnOpenNextExamination");
        assertNotNull(ui().find("examinationTabs"));assertTrue(ui().text("lblPageSubtitle").contains(expected));assertEquals("VC",ui().text("lblPatientAvatar"));
        ViewAccess clinical=ui().sub("tabExamClinicalContent");clinical.text("txtDiagnosis","Chẩn đoán kiểm thử");click("btnSaveDraft");
        click("btnDashboard");click("btnOpenNextExamination");assertEquals("Chẩn đoán kiểm thử",ui().sub("tabExamClinicalContent").text("txtDiagnosis"));
        click("btnSaveDraft");click("btnExamination");assertNull(ui().find("examinationTabs"));
        assertEquals(-1,ui().table("tblWaitingPatients").getSelectionModel().getSelectedIndex());
        assertTrue(ui().button("btnStartExamination").isDisabled());return null;
    });}
    @Test void patientLookupOpensSelectedPatientAndReturnsToResults()throws Exception{fx(()->{
        navigator.openDemoRole(Role.RECEPTIONIST);click("btnPatientLookup");ui().text("txtPatientSearch","BN002");click("btnSearchPatient");
        assertEquals(1,ui().table("tblPatientResults").getItems().size());ui().table("tblPatientResults").getSelectionModel().selectFirst();
        assertTrue(ui().text("lblSelectedPatientName").contains("BN002"));assertFalse(ui().text("lblSelectedPatientName").contains("BN001"));
        click("btnChangePatient");assertNotNull(ui().table("tblPatientResults"));return null;
    });}
    @Test void dentistCanSaveServiceMedicineAndCompleteWithoutChargingMedicine()throws Exception{fx(()->{
        navigator.openDemoRole(Role.DENTIST);click("btnExamination");ui().table("tblWaitingPatients").getSelectionModel().selectFirst();
        ui().sub("selectedPatientDetail").button("btnStartExamination").fire();ViewAccess clinical=ui().sub("tabExamClinicalContent");
        clinical.button("btnAddTreatmentService").fire();ViewAccess service=modal("Thêm dịch vụ điều trị");
        assertEquals("1",service.text("txtServiceQuantity"));service.button("btnSaveService").fire();
        assertEquals(1,clinical.table("tblTreatmentServices").getItems().size());
        Button status=(Button)clinical.table("tblTreatmentServices").getItems().get(0).get("v4");status.fire();
        assertEquals("Đã thực hiện",((Button)clinical.table("tblTreatmentServices").getItems().get(0).get("v4")).getText());
        String serviceTotal=clinical.text("lblServiceTotal");
        clinical.button("btnAddPrescriptionMedicine").fire();ViewAccess medicine=modal("Thêm thuốc");
        assertEquals("",medicine.text("txtMedicineName"));medicine.text("txtMedicineName","Thuốc thử DEMO");medicine.text("txtMedicineQuantity","10");
        medicine.text("txtDoseMorning","1");medicine.text("txtMedicineInstructions","Dữ liệu thử, không phải hướng dẫn điều trị");medicine.button("btnSaveMedicine").fire();
        assertEquals(1,clinical.table("tblPrescriptionMedicines").getItems().size());assertEquals(serviceTotal,clinical.text("lblServiceTotal"));
        clinical.text("txtDiagnosis","Chẩn đoán DEMO");click("btnSaveDraft");click("btnCompleteExam");
        ViewAccess confirm=modal("Hoàn tất khám");confirm.button("btnConfirmCompleteExam").fire();
        assertEquals("Đã khám",ui().text("lblExamStatus"));assertTrue(clinical.root.isDisabled());assertTrue(ui().button("btnCompleteExam").isDisabled());return null;
    });}
    private void answerNextDialog(String text){
        Platform.runLater(()->{
            for(Window window:List.copyOf(Window.getWindows()))if(window.getScene()!=null&&window.getScene().getRoot() instanceof DialogPane pane){
                for(ButtonType type:pane.getButtonTypes())if(type.getText().equals(text)){((Button)pane.lookupButton(type)).fire();return;}
            }
        });
    }
    @Test void unsavedChangesCanCancelNavigationOrBeDiscardedExplicitly()throws Exception{fx(()->{
        navigator.openDemoRole(Role.DENTIST);click("btnOpenNextExamination");String original=ui().sub("tabExamClinicalContent").text("txtDiagnosis");
        ui().sub("tabExamClinicalContent").text("txtDiagnosis","Nội dung chưa lưu");answerNextDialog("Ở lại");click("btnDashboard");
        assertNotNull(ui().find("examinationTabs"));assertEquals("Nội dung chưa lưu",ui().sub("tabExamClinicalContent").text("txtDiagnosis"));
        answerNextDialog("Bỏ thay đổi");click("btnDashboard");assertNotNull(ui().find("nextExamCard"));click("btnOpenNextExamination");
        assertEquals(original,ui().sub("tabExamClinicalContent").text("txtDiagnosis"));return null;
    });}

    @Test void managerCreatesEmployeeAndAssignmentThenCancelsAssignment()throws Exception{fx(()->{
        navigator.openDemoRole(Role.MANAGER);click("btnStaff");int before=ui().table("tblStaff").getItems().size();click("btnAddStaff");
        ViewAccess form=modal("Thêm nhân viên");assertEquals("",form.text("txtStaffFullName"));form.text("txtStaffFullName","Nhân viên DEMO thử");
        form.text("txtStaffPhone","0909000009");form.date("dpStaffBirthDate",java.time.LocalDate.of(1995,1,1));form.button("btnSaveStaff").fire();
        assertEquals(before+1,ui().table("tblStaff").getItems().size());ui().table("tblStaff").getSelectionModel().selectLast();assertTrue(ui().find("staffSelectedDetails").isVisible());
        click("btnAssignments");click("btnCreateAssignment");form=modal("Thêm phân công");form.date("dpAssignmentDate",java.time.LocalDate.of(2026,11,2));
        form.text("txtAssignmentWork","Ca thử DEMO");form.button("btnSaveAssignment").fire();assertEquals("Ca thử DEMO",ui().text("lblAssignmentWork"));
        assertTrue(ui().find("assignmentDetailsSlot").isVisible());click("btnRequestCancelAssignment");modal("Hủy phân công").button("btnConfirmCancelAssignment").fire();
        assertFalse(ui().find("assignmentDetailsSlot").isVisible());return null;
    });}
    @Test void receptionistBooksAndCancelsAppointmentAndConfirmsTransferInTwoSteps()throws Exception{fx(()->{
        navigator.openDemoRole(Role.RECEPTIONIST);click("btnIntakeAppointments");click("btnBookAppointment");ViewAccess form=modal("Đặt lịch");
        form.text("txtAppointmentPatient","BN002");form.table("tblAppointmentPatientResults").getSelectionModel().selectFirst();
        form.combo("cboAppointmentTime").setValue("14:00");form.text("txtAppointmentReason","Lý do DEMO");form.button("btnSaveAppointment").fire();
        ViewAccess today=ui().sub("tabTodayContent");assertEquals(4,today.table("tblTodayAppointments").getItems().size());
        today.table("tblTodayAppointments").getSelectionModel().selectLast();today.button("btnCancelAppointment").fire();modal("Hủy lịch hẹn").button("btnConfirmCancelAppointment").fire();
        assertEquals("DA_HUY",today.table("tblTodayAppointments").getItems().get(3).get("v4"));
        click("btnCashier");ViewAccess waiting=ui().sub("tabAwaitingPaymentContent");int before=waiting.table("tblWaitingInvoices").getItems().size();
        waiting.table("tblWaitingInvoices").getSelectionModel().selectFirst();waiting.button("btnPayTransfer").fire();ViewAccess transfer=modal("Chuyển khoản DEMO");
        transfer.button("btnConfirmTransferReceived").fire();assertEquals(before,waiting.table("tblWaitingInvoices").getItems().size());
        modal("Xác nhận chuyển khoản").button("btnConfirmTransferPayment").fire();assertEquals(before-1,waiting.table("tblWaitingInvoices").getItems().size());return null;
    });}
    @Test void profileEditsOnlyCurrentDemoEmployeeAndLogoutClearsIt()throws Exception{fx(()->{
        navigator.openDemoRole(Role.DENTIST);((MenuButton)ui().find("mnuAccount")).getItems().get(0).fire();ViewAccess profile=modal("Hồ sơ DEMO");
        String original=profile.text("lblProfileName");profile.button("btnEditProfile").fire();ViewAccess edit=modal("Sửa hồ sơ");
        edit.text("txtProfileName","Tên sửa trong DEMO");edit.button("btnSaveProfile").fire();profile=modal("Hồ sơ DEMO");assertEquals("Tên sửa trong DEMO",profile.text("lblProfileName"));
        navigator.logout();assertFalse(session.isActive());assertFalse(profile.root.isVisible()&&profile.root.getScene().getWindow().isShowing());
        navigator.openDemoRole(Role.DENTIST);((MenuButton)ui().find("mnuAccount")).getItems().get(0).fire();assertEquals(original,modal("Hồ sơ DEMO").text("lblProfileName"));return null;
    });}

    @Test void allExistingFxmlStillLoadWithTheirStylesheets()throws Exception{fx(()->{
        int count=0;
        try(var paths=java.nio.file.Files.walk(java.nio.file.Path.of("src/main/resources/fxml"))){
            for(var file:paths.filter(p->p.toString().endsWith(".fxml")).toList()){
                javafx.scene.Parent root=new javafx.fxml.FXMLLoader(file.toUri().toURL()).load();
                new javafx.scene.Scene(root);root.applyCss();root.layout();count++;
            }
        }
        assertTrue(count>=132,"Không bỏ qua modal hoặc state FXML khi kiểm tra");return null;
    });}

}
