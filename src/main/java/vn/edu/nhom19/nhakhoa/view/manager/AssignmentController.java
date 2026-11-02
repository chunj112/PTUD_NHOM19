package vn.edu.nhom19.nhakhoa.view.manager;

import java.time.*;
import java.util.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import vn.edu.nhom19.nhakhoa.auth.Role;
import vn.edu.nhom19.nhakhoa.entity.DemoData.*;
import vn.edu.nhom19.nhakhoa.service.DemoClinicService;
import vn.edu.nhom19.nhakhoa.view.common.*;

/** UC005 DEMO: ma trận hàng nhân viên, cột ngày, sáng trên chiều.
 * Giữ kích thước cột FXML; dùng lại detail, modal và Service hiện có.
 */
public final class AssignmentController extends ScreenController {
    private LocalDate week=DemoClinicService.DEMO_DATE.with(DayOfWeek.MONDAY);
    private Assignment selected;
    private Button selectedCard;
    public AssignmentController(ViewAccess view,ControllerContext context){
        super(view,context);context.service.require(Role.MANAGER);
        view.choices("cboAssignmentPosition",List.of("Tất cả chức vụ","Quản lý","Lễ tân","Nha sĩ","Y tá"),"Tất cả chức vụ");
        view.choices("cboAssignmentRoom",List.of("Tất cả phòng","P01","P02"),"Tất cả phòng");
        for(String id:List.of("txtAssignmentSearch","cboAssignmentPosition","cboAssignmentRoom"))view.changed(id,this::refresh);
        view.on("btnMatrixPreviousWeek",context,()->{week=week.minusWeeks(1);refresh();});
        view.on("btnMatrixNextWeek",context,()->{week=week.plusWeeks(1);refresh();});
        view.on("btnMatrixToday",context,()->{week=DemoClinicService.DEMO_DATE.with(DayOfWeek.MONDAY);refresh();});
        view.on("btnCreateAssignment",context,()->form(null));
        view.on("btnEditAssignment",context,()->{if(selected!=null)form(selected);});
        view.on("btnRequestCancelAssignment",context,this::cancel);
    }
    private boolean matches(Assignment a){
        Employee e=context.service.employee(a.employeeId);
        String q=view.text("txtAssignmentSearch").toLowerCase(Locale.ROOT);
        String position=view.text("cboAssignmentPosition"),room=view.text("cboAssignmentRoom");
        return a.active && (e.id+" "+e.name+" "+e.phone).toLowerCase(Locale.ROOT).contains(q)
            && (position.contains("Tất cả")||position.equals(e.role.displayName()))
            && (room.contains("Tất cả")||room.equals(a.room));
    }
    @Override public void refresh(){
        GridPane matrix=(GridPane)view.find("assignmentMatrix");
        if(matrix==null)throw new IllegalStateException("Thiếu bảng assignmentMatrix trong giao diện Phân công.");
        // Chỉ thay dữ liệu các hàng; giữ nguyên 9 cột và hàng tiêu đề trong FXML.
        matrix.getChildren().removeIf(node->GridPane.getRowIndex(node)!=null&&GridPane.getRowIndex(node)>0);
        while(matrix.getRowConstraints().size()>1)matrix.getRowConstraints().removeLast();
        String[] headers={"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        for(int i=0;i<7;i++){
            LocalDate date=week.plusDays(i);
            view.text("lblMatrix"+headers[i]+"Header",(i==6?"CN":"T"+(i+2))+" · "+String.format("%02d",date.getDayOfMonth()));
            var header=view.find("lblMatrix"+headers[i]+"Header");
            header.getStyleClass().remove("matrix-friday-header");
            if(date.equals(DemoClinicService.DEMO_DATE))header.getStyleClass().add("matrix-friday-header");
        }
        ArrayList<Assignment> filtered=new ArrayList<>();
        for(Assignment assignment:context.service.assignments())
            if(matches(assignment)&&!assignment.date.isBefore(week)&&!assignment.date.isAfter(week.plusDays(6)))filtered.add(assignment);
        filtered.sort(Comparator.comparing((Assignment a)->a.shift).thenComparing(a->a.start).thenComparing(a->a.id));
        selectedCard=null;int row=1;
        for(Employee employee:context.service.employees()){
            String query=view.text("txtAssignmentSearch").toLowerCase(Locale.ROOT);
            String position=view.text("cboAssignmentPosition"),room=view.text("cboAssignmentRoom");
            if(!(employee.id+" "+employee.name+" "+employee.phone).toLowerCase(Locale.ROOT).contains(query))continue;
            if(!position.contains("Tất cả")&&!position.equals(employee.role.displayName()))continue;
            boolean hasAssignment=false;
            for(Assignment a:filtered)if(a.employeeId.equals(employee.id))hasAssignment=true;
            if(!room.contains("Tất cả")&&!hasAssignment)continue;
            RowConstraints height=new RowConstraints(112,112,112);height.setValignment(javafx.geometry.VPos.TOP);
            matrix.getRowConstraints().add(height);
            VBox name=new VBox(2,label(employee.name,"matrix-employee-name"),label(employee.id,"assignment-caption"));
            name.getStyleClass().add("matrix-employee-cell");name.setMinWidth(0);matrix.add(name,0,row);
            StackPane role=new StackPane(label(employee.role.displayName(),"assignment-caption"));
            role.setAlignment(javafx.geometry.Pos.TOP_LEFT);role.getStyleClass().add("matrix-position-cell");matrix.add(role,1,row);
            for(int day=0;day<7;day++){
                LocalDate date=week.plusDays(day);VBox cell=new VBox(4);cell.setMinWidth(0);
                cell.getStyleClass().add("matrix-day-cell");cell.setUserData(employee.id);
                if(date.equals(DemoClinicService.DEMO_DATE))cell.getStyleClass().add("matrix-friday-cell");
                for(Assignment a:filtered)if(a.employeeId.equals(employee.id)&&a.date.equals(date))cell.getChildren().add(card(a));
                if(cell.getChildren().isEmpty())cell.getChildren().add(label("—","assignment-caption"));
                matrix.add(cell,day+2,row);
            }
            row++;
        }
        if(selected!=null&&!filtered.contains(selected))selected=null;
        detail();
        view.text("lblMatrixWeekRange",ViewAccess.DATE.format(week)+" – "+ViewAccess.DATE.format(week.plusDays(6)));
        view.text("lblAssignmentsDemoNotice","DEMO ONLY · "+filtered.size()+" phân công trong bộ nhớ · Hàng nhân viên / cột ngày · Không SQL");
    }
    private Label label(String text,String style){
        Label label=new Label(text);label.setWrapText(true);label.setMinWidth(0);
        label.getStyleClass().add(style);return label;
    }
    private Button card(Assignment a){
        Employee employee=context.service.employee(a.employeeId);
        Button button=new Button();button.setId("assignmentCard_"+a.id);button.setUserData(a);
        button.setMinWidth(0);button.setMaxWidth(Double.MAX_VALUE);button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.getStyleClass().addAll("matrix-shift",a.shift==Shift.SANG?"matrix-morning":"matrix-afternoon");
        VBox content=new VBox(2);content.setMinWidth(0);content.setPrefWidth(60);
        content.getChildren().addAll(label(a.shift==Shift.SANG?"☀ Ca sáng":"☾ Ca chiều","matrix-shift-title"),label(a.room,"matrix-shift-room"));
        button.setGraphic(content);button.setAccessibleText(employee.name+" · "+a.date+" · "+a.start+"–"+a.end+" · "+a.room);
        button.setOnAction(event->{if(context.active())selectAssignment(a);});
        if(a==selected){button.getStyleClass().add("assignment-selected");selectedCard=button;}
        return button;
    }
    private void selectAssignment(Assignment a){
        if(selectedCard!=null)selectedCard.getStyleClass().remove("assignment-selected");
        selected=a;selectedCard=(Button)view.find("assignmentCard_"+a.id);
        if(selectedCard!=null)selectedCard.getStyleClass().add("assignment-selected");
        detail();
    }
    private void detail(){
        view.show("assignmentDetailsSlot",selected!=null);if(selected==null)return;
        Employee e=context.service.employee(selected.employeeId);
        view.text("lblSelectedAssignmentEmployee",e.name);view.text("lblSelectedAssignmentEmployeeSummary",e.id+" · "+e.role.displayName());
        view.text("lblSelectedEmployeeStatus",e.active?"Hoạt động":"Ngừng hoạt động");
        var badge=view.find("lblSelectedEmployeeStatus");badge.getStyleClass().remove("inactive");if(!e.active)badge.getStyleClass().add("inactive");
        view.text("lblAssignmentDate",ViewAccess.DATE.format(selected.date));view.text("lblAssignmentShift",selected.shift==Shift.SANG?"Sáng":"Chiều");
        view.text("lblAssignmentTime",selected.start+"–"+selected.end);view.text("lblAssignmentRoom",selected.room);
        view.text("lblAssignmentWork",selected.work);view.text("lblAssignmentStatus",selected.active?"Đã phân công":"Đã hủy");
    }
    private void form(Assignment old){
        boolean creating=old==null;ViewAccess form=ViewAccess.load("manager/assignment-"+(creating?"add":"edit")+".fxml");
        form.choices("cboAssignmentEmployee",context.service.employees(),creating?context.service.employee("NV002"):context.service.employee(old.employeeId));
        form.date("dpAssignmentDate",creating?DemoClinicService.DEMO_DATE:old.date);
        ArrayList<String> times=new ArrayList<>();for(int minute=8*60;minute<=17*60;minute+=30)times.add(LocalTime.of(minute/60,minute%60).toString());
        form.choices("cboAssignmentStartTime",times,creating?"08:00":old.start.toString());
        form.choices("cboAssignmentEndTime",times,creating?"12:00":old.end.toString());
        form.choices("cboAssignmentRoom",List.of("P01","P02"),creating?"P01":old.room);
        form.text("txtAssignmentWork",creating?"":old.work);
        String id=creating?context.service.nextId("PC"):old.id;
        var stage=context.dialog(form,creating?"Thêm phân công DEMO":"Sửa phân công DEMO");
        form.on("btnSaveAssignment",context,()->{
            Employee employee=(Employee)form.combo("cboAssignmentEmployee").getValue();
            if(employee==null)throw new IllegalArgumentException("Chọn nhân viên.");
            Assignment value=new Assignment(id,employee.id,form.text("cboAssignmentRoom"),form.date("dpAssignmentDate").getValue(),
                LocalTime.parse(form.text("cboAssignmentStartTime")),LocalTime.parse(form.text("cboAssignmentEndTime")),Shift.SANG,form.text("txtAssignmentWork"),true);
            context.service.saveAssignment(value,creating);stage.close();week=value.date.with(DayOfWeek.MONDAY);refresh();selectAssignment(value);
        });
    }
    private void cancel(){
        if(selected==null)return;Assignment target=selected;ViewAccess form=ViewAccess.load("manager/assignment-cancel.fxml");
        form.text("lblCancelEmployee",context.service.employee(target.employeeId).name);form.text("lblCancelDate",ViewAccess.DATE.format(target.date));
        form.text("lblCancelShift",target.shift+" · "+target.start+"–"+target.end);form.text("lblCancelRoom",target.room);
        var stage=context.dialog(form,"Hủy phân công DEMO");
        form.on("btnConfirmCancelAssignment",context,()->{context.service.cancelAssignment(target);stage.close();refresh();});
    }
}
