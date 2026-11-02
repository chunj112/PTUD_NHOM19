package vn.edu.nhom19.nhakhoa;

import static org.junit.jupiter.api.Assertions.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import org.junit.jupiter.api.*;
import vn.edu.nhom19.nhakhoa.app.*;
import vn.edu.nhom19.nhakhoa.auth.*;
import vn.edu.nhom19.nhakhoa.entity.DemoData.*;
import vn.edu.nhom19.nhakhoa.view.common.*;
import vn.edu.nhom19.nhakhoa.view.manager.ReportsController;
import vn.edu.nhom19.nhakhoa.service.DemoClinicService;

/** Hồi quy cho lỗi bỏ tab UC005 và các quyết định refinement Manager. */
class ManagerRefinementTest {
    private Stage stage;
    private AppNavigator navigator;
    private ViewAccess ui(){return new ViewAccess(stage.getScene().getRoot());}
    private static <T>T fx(Callable<T> work)throws Exception{
        FutureTask<T> task=new FutureTask<>(work);Platform.runLater(task);return task.get(30,TimeUnit.SECONDS);
    }
    @BeforeAll static void toolkit()throws Exception{
        CountDownLatch ready=new CountDownLatch(1);
        try{Platform.startup(ready::countDown);}catch(IllegalStateException started){ready.countDown();}
        assertTrue(ready.await(10,TimeUnit.SECONDS));fx(()->{Platform.setImplicitExit(false);return null;});
    }
    @BeforeEach void setup()throws Exception{fx(()->{
        stage=new Stage();navigator=new AppNavigator(stage,new UserSession());navigator.start();navigator.openDemoRole(Role.MANAGER);return null;
    });}
    @AfterEach void close()throws Exception{fx(()->{
        navigator.close();for(Window window:List.copyOf(Window.getWindows()))if(window instanceof Stage dialog)dialog.close();return null;
    });}
    private ViewAccess modal(String title){
        for(Window window:List.copyOf(Window.getWindows()))if(window instanceof Stage dialog&&dialog!=stage&&dialog.getTitle().contains(title))
            return new ViewAccess(dialog.getScene().getRoot());
        throw new AssertionError("Thiếu modal "+title);
    }
    private List<Button> cards(Node node){
        ArrayList<Button> result=new ArrayList<>();
        if(node instanceof Button button&&button.getUserData() instanceof Assignment)result.add(button);
        if(node instanceof Parent parent)for(Node child:parent.getChildrenUnmodifiable())result.addAll(cards(child));
        return result;
    }
    @Test void assignmentCardSelectEditAndCancelWithoutRemovedTab()throws Exception{fx(()->{
        ui().button("btnAssignments").fire();assertNull(ui().find("assignmentTabs"));assertNull(ui().find("weekCalendar"));
        assertFalse(ui().find("assignmentDetailsSlot").isVisible());
        Button card=cards(ui().find("assignmentMatrix")).stream().filter(b->((Assignment)b.getUserData()).employeeId.equals("NV001")).findFirst().orElseThrow();
        Assignment assignment=(Assignment)card.getUserData();card.fire();
        assertTrue(ui().find("assignmentDetailsSlot").isVisible());assertTrue(card.getStyleClass().contains("assignment-selected"));
        assertEquals("Lê Hoàng Nam",ui().text("lblSelectedAssignmentEmployee"));
        ui().button("btnEditAssignment").fire();ViewAccess edit=modal("Sửa phân công");
        edit.text("txtAssignmentWork","Công việc đã sửa DEMO");edit.button("btnSaveAssignment").fire();
        assertEquals("Công việc đã sửa DEMO",ui().text("lblAssignmentWork"));
        ui().button("btnRequestCancelAssignment").fire();ViewAccess cancel=modal("Hủy phân công");
        cancel.button("btnConfirmCancelAssignment").fire();
        assertFalse(ui().find("assignmentDetailsSlot").isVisible());assertNull(ui().find("assignmentCard_"+assignment.id));
        ui().button("btnMatrixNextWeek").fire();ui().button("btnMatrixNextWeek").fire();assertTrue(cards(ui().find("assignmentMatrix")).isEmpty());
        ui().button("btnMatrixToday").fire();assertFalse(cards(ui().find("assignmentMatrix")).isEmpty());return null;
    });}
    @Test void assignmentMatrixKeepsEmployeeRowsDayColumnsAndShiftOrder()throws Exception{fx(()->{
        ui().button("btnAssignments").fire();GridPane matrix=(GridPane)ui().find("assignmentMatrix");
        assertEquals(9,matrix.getColumnConstraints().size());assertNull(ui().find("assignmentDays"));
        ui().root.applyCss();ui().root.layout();List<Button> all=cards(matrix);assertFalse(all.isEmpty());
        assertTrue(all.stream().allMatch(b->Math.abs(b.getHeight()-44)<1),"Card ma trận phải cùng chiều cao 44px");
        Map<Integer,String> employeeByRow=new HashMap<>();Map<Integer,LocalDate> dateByColumn=new HashMap<>();
        for(Node node:matrix.getChildren())if(node.getStyleClass().contains("matrix-day-cell")){
            assertInstanceOf(VBox.class,node);int row=GridPane.getRowIndex(node),column=GridPane.getColumnIndex(node);
            assertTrue(row>=1);assertTrue(column>=2&&column<=8);Assignment previous=null;
            for(Button card:cards(node)){
                Assignment value=(Assignment)card.getUserData();
                String oldEmployee=employeeByRow.putIfAbsent(row,value.employeeId);
                if(oldEmployee!=null)assertEquals(oldEmployee,value.employeeId,"Một hàng chỉ có một nhân viên");
                LocalDate oldDate=dateByColumn.putIfAbsent(column,value.date);
                if(oldDate!=null)assertEquals(oldDate,value.date,"Một cột chỉ có một ngày");
                if(previous!=null){assertTrue(value.shift.compareTo(previous.shift)>=0);assertFalse(value.start.isBefore(previous.start));}
                assertTrue(card.getStyleClass().contains("matrix-shift"));previous=value;
            }
        }
        for(int column=3;column<=7;column++)assertEquals(dateByColumn.get(column-1).plusDays(1),dateByColumn.get(column));
        ui().combo("cboAssignmentPosition").setValue("Nha sĩ");
        assertTrue(cards(matrix).stream().allMatch(b->List.of("NV002","NV003").contains(((Assignment)b.getUserData()).employeeId)));
        ui().text("txtAssignmentSearch","NV002");assertFalse(cards(matrix).isEmpty());
        assertTrue(cards(matrix).stream().allMatch(b->((Assignment)b.getUserData()).employeeId.equals("NV002")));
        ui().combo("cboAssignmentRoom").setValue("P02");assertTrue(cards(matrix).isEmpty());return null;
    });}
    @Test void staffModalShowsEntireFormAndDetailLeavesRoomForAllColumns()throws Exception{fx(()->{
        stage.setWidth(1280);ui().button("btnStaff").fire();ui().table("tblStaff").getSelectionModel().selectFirst();
        ui().root.applyCss();ui().root.layout();
        assertNull(ui().find("lblDetailStaffId"));
        assertEquals(ui().find("lblSelectedStaffSummary").getParent(),ui().find("lblDetailStatus").getParent());
        assertEquals(280,((Region)ui().find("staffDetailsPane")).getWidth(),1);
        TableView<?> table=ui().table("tblStaff");
        assertTrue(table.getColumns().stream().mapToDouble(TableColumn::getWidth).sum()<=table.getWidth()+1,"Không được chèn mất cột cuối");
        ui().button("btnAddStaff").fire();ViewAccess form=modal("Thêm nhân viên");
        assertEquals("addStaffForm",form.root.getId());assertFalse(form.root instanceof ScrollPane);
        assertEquals("",form.date("dpStaffBirthDate").getEditor().getText());
        var scene=form.root.getScene();scene.getRoot().applyCss();scene.getRoot().layout();
        assertTrue(scene.getHeight()>=form.root.prefHeight(scene.getWidth())-1,"Cửa sổ phải đủ chiều cao form sau CSS");
        assertTrue(form.button("btnSaveStaff").localToScene(form.button("btnSaveStaff").getBoundsInLocal()).getMaxY()<=scene.getHeight()+1);
        form.button("btnCancelStaffForm").fire();return null;
    });}
    @Test void reportsKeepZeroDaysAndOnlyApplyCommittedDates()throws Exception{fx(()->{
        ui().button("btnReports").fire();ViewAccess revenue=ui().sub("revenueContent");ViewAccess visits=ui().sub("visitsContent");
        assertNull(visits.find("cboVisitsDentist"));assertNull(visits.find("cboVisitsStatus"));
        assertEquals(8,revenue.table("tblRevenue").getItems().size());assertTrue(revenue.text("lblRevenueRecordCount").contains("/ 9"));
        assertEquals("01/10/2026",revenue.table("tblRevenue").getItems().get(0).get("v0"));
        assertEquals("0 ₫",revenue.table("tblRevenue").getItems().get(0).get("v3"));
        assertEquals(9,((HBox)revenue.find("chartRevenue")).getChildren().size());
        revenue.date("dpRevenueFrom",LocalDate.of(2026,10,9));
        ui().button("btnDashboard").fire();ui().button("btnReports").fire();
        assertTrue(revenue.text("lblRevenueRecordCount").contains("/ 9")); // chưa bấm Áp dụng
        revenue.button("btnApplyRevenueReport").fire();assertEquals(1,revenue.table("tblRevenue").getItems().size());
        assertEquals("09/10/2026",revenue.table("tblRevenue").getItems().get(0).get("v0"));
        assertTrue(visits.text("lblVisitsRecordCount").contains("/ 9")); // kỳ của tab khác giữ nguyên
        assertEquals(6,visits.table("tblVisits").getColumns().size());
        revenue.date("dpRevenueFrom",LocalDate.of(2026,1,1));revenue.button("btnApplyRevenueReport").fire();
        assertTrue(revenue.text("lblRevenueRecordCount").contains("/ 282"));assertTrue(((HBox)revenue.find("chartRevenue")).getChildren().size()<=14);
        return null;
    });}
    @Test void servicePieUsesPerformedCountAndHasEmptyState()throws Exception{fx(()->{
        ui().button("btnReports").fire();ViewAccess usage=ui().sub("serviceUsageContent");
        assertNull(usage.find("cboServiceUsageService"));assertNull(usage.find("cboServiceUsageDentist"));
        PieChart pie=(PieChart)usage.find("chartServiceUsage");assertFalse(pie.getData().isEmpty());
        double total=pie.getData().stream().mapToDouble(PieChart.Data::getPieValue).sum();
        assertEquals(Integer.parseInt(usage.text("lblServiceUsageTotal")),total);
        assertTrue(pie.getData().stream().allMatch(d->d.getName().contains("%")));
        usage.date("dpServiceUsageFrom",LocalDate.of(2026,10,1));usage.date("dpServiceUsageTo",LocalDate.of(2026,10,1));
        usage.button("btnApplyServiceUsageReport").fire();assertTrue(pie.getData().isEmpty());
        assertFalse(pie.isVisible());assertTrue(usage.find("lblServiceUsageEmpty").isVisible());return null;
    });}
    @Test void servicePieGroupsMinorServicesWithoutDroppingDetailRows()throws Exception{fx(()->{
        UserSession session=new UserSession();session.startDemo(Role.MANAGER);
        DemoClinicService service=new DemoClinicService(session);
        for(Exam exam:service.exams())exam.treatments.clear();
        Exam exam=service.exam("PK014");int[] counts={60,20,10,5,4,1};
        for(int i=0;i<counts.length;i++){
            String id="QA"+i;
            service.saveService(new DentalService(id,"Dịch vụ QA "+i,"Khám","Lần",new java.math.BigDecimal("1000"),30,"DEMO",true),true);
            for(int j=0;j<counts[i];j++)exam.treatments.add(new Treatment(id+"_"+j,id,"Dịch vụ QA "+i,0,new java.math.BigDecimal("1000"),true));
        }
        exam.treatments.add(new Treatment("chua-thuc-hien","QA0","Chưa thực hiện",0,new java.math.BigDecimal("1000"),false));
        ViewAccess reports=ViewAccess.load("manager/reports.fxml");
        ReportsController controller=new ReportsController(reports,new ControllerContext(service,null));controller.refresh();
        ViewAccess usage=reports.sub("serviceUsageContent");PieChart pie=(PieChart)usage.find("chartServiceUsage");
        assertEquals("100",usage.text("lblServiceUsageTotal"));assertEquals(5,pie.getData().size());
        assertEquals(100,pie.getData().stream().mapToDouble(PieChart.Data::getPieValue).sum());
        assertTrue(pie.getData().stream().anyMatch(d->d.getName().startsWith("Khác")&&d.getPieValue()==5));
        assertEquals(7,usage.table("tblServiceUsage").getItems().size()); // 6 dịch vụ + 1 dòng thu tiền kỳ này
        assertFalse(usage.table("tblServiceUsage").getItems().stream().anyMatch(row->row.get("v0").equals("Khác")));
        service.dispose();session.clear();return null;
    });}
    @Test void staffStatusAndServiceFormsHaveNoRemovedControls()throws Exception{fx(()->{
        ui().button("btnStaff").fire();ui().table("tblStaff").getSelectionModel().select(1);
        assertNull(ui().find("btnStaffAccountPermissions"));assertNull(ui().find("lblSelectedStaffStatusBadge"));
        ui().button("btnEditStaff").fire();ViewAccess edit=modal("Sửa nhân viên");
        edit.combo("cboStaffStatus").setValue("Ngừng hoạt động");edit.button("btnSaveStaff").fire();
        ui().table("tblStaff").getSelectionModel().select(1);assertTrue(ui().find("lblDetailStatus").getStyleClass().contains("inactive"));
        ui().table("tblStaff").getSelectionModel().selectFirst();assertFalse(ui().find("lblDetailStatus").getStyleClass().contains("inactive"));
        ui().button("btnServices").fire();ui().button("btnAddService").fire();ViewAccess add=modal("Thêm dịch vụ");
        assertNull(add.find("cboServiceFdiRequirement"));add.button("btnCancelServiceForm").fire();
        ui().table("tblServices").getSelectionModel().selectFirst();assertNull(ui().find("lblServiceDetailFdiRequirement"));
        ui().button("btnEditService").fire();assertNull(modal("Sửa dịch vụ").find("cboServiceFdiRequirement"));return null;
    });}
}
