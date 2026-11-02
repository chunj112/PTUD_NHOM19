package vn.edu.nhom19.nhakhoa.view.manager;

import java.util.*;
import javafx.scene.control.Label;
import vn.edu.nhom19.nhakhoa.auth.Role;
import vn.edu.nhom19.nhakhoa.entity.DemoData.*;
import vn.edu.nhom19.nhakhoa.view.common.*;

/** UC004: chỉ lưu thông tin nhân viên DEMO, không tự tạo tài khoản/mật khẩu. */
public final class EmployeeController extends ScreenController {
    private Employee selected;
    public EmployeeController(ViewAccess view,ControllerContext context){
        super(view,context);context.service.require(Role.MANAGER);
        view.choices("cboPositionFilter",List.of("Tất cả chức vụ","Quản lý","Lễ tân","Nha sĩ","Y tá"),"Tất cả chức vụ");
        view.choices("cboStatusFilter",List.of("Tất cả trạng thái","Hoạt động","Ngừng hoạt động"),"Tất cả trạng thái");
        view.on("btnAddStaff",context,()->openForm(null));
        view.on("btnEditStaff",context,()->{if(selected!=null)openForm(selected);});
        for(String id:List.of("txtStaffSearch","cboPositionFilter","cboStatusFilter"))view.changed(id,this::refresh);
        view.table("tblStaff").getSelectionModel().selectedItemProperty().addListener((o,a,b)->{selected=b==null?null:(Employee)b.get("_model");detail();});
    }
    @Override public void refresh(){
        ArrayList<Map<String,Object>> rows=new ArrayList<>();String q=view.text("txtStaffSearch").toLowerCase(Locale.ROOT);
        for(Employee e:context.service.employees()){
            if(!(e.id+" "+e.name+" "+e.phone).toLowerCase(Locale.ROOT).contains(q))continue;
            String role=view.text("cboPositionFilter"),status=view.text("cboStatusFilter");
            if(!role.contains("Tất cả")&&!role.isBlank()&&!role.equals(e.role.displayName()))continue;
            if((status.equals("Hoạt động")&&!e.active)||(status.equals("Ngừng hoạt động")&&e.active))continue;
            Label statusBadge=new Label(e.active?"Hoạt động":"Ngừng hoạt động");
            statusBadge.getStyleClass().add(e.active?"staff-status-active":"staff-status-inactive");
            rows.add(ViewAccess.row(e,e.id,e.name,e.role.displayName(),e.phone,e.email,statusBadge));
        }
        view.paged("tblStaff",rows,"lblRecordCount","btnPreviousPage","btnNextPage","lblCurrentPage");
        view.text("lblStaffCount",rows.size()+" nhân viên DEMO · Chọn một bản ghi để xem thông tin");
        view.text("lblStaffDemoNotice","DEMO ONLY · Đã nối Controller và Service bộ nhớ · Không SQL, không tạo tài khoản thật");
        selected=null;detail();
    }
    private void detail(){
        view.show("staffDetailsPane",selected!=null);view.show("staffSelectedDetails",selected!=null);if(selected==null)return;
        view.text("lblSelectedStaffName",selected.name);view.text("lblSelectedStaffSummary",selected.id+" · "+selected.role.displayName());
        view.text("lblDetailFullName",selected.name);
        view.text("lblDetailPosition",selected.role.displayName());view.text("lblDetailPhone",selected.phone);
        view.text("lblDetailEmail",selected.email);view.text("lblDetailStatus",selected.active?"Hoạt động":"Ngừng hoạt động");
        // Một badge duy nhất; cập nhật màu cả khi đổi từ nhân viên nghỉ sang đang làm.
        var badge=view.find("lblDetailStatus");badge.getStyleClass().remove("inactive");
        if(!selected.active)badge.getStyleClass().add("inactive");
    }
    private void openForm(Employee old){
        boolean creating=old==null;ViewAccess form=ViewAccess.load("manager/staff-"+(creating?"add":"edit")+".fxml");
        String id=creating?context.service.nextId("NV"):old.id;
        form.text("txtStaffId",id);form.text("txtStaffFullName",creating?"":old.name);form.text("txtStaffPhone",creating?"":old.phone);
        form.text("txtStaffEmail",creating?"":old.email);form.text("txtStaffAddress",creating?"":old.address);
        form.date("dpStaffBirthDate",creating?null:old.birthday);
        if(creating)form.date("dpStaffBirthDate").getEditor().clear();
        List<String> roles=new ArrayList<>();for(Role r:Role.values())roles.add(r.displayName());
        form.choices("cboStaffPosition",roles,creating?Role.DENTIST.displayName():old.role.displayName());
        form.choices("cboStaffGender",List.of("Nam","Nữ"),creating?"Nam":old.gender);
        form.choices("cboStaffStatus",List.of("Hoạt động","Ngừng hoạt động"),creating||old.active?"Hoạt động":"Ngừng hoạt động");
        var stage=context.staffDialog(form,creating?"Thêm nhân viên DEMO":"Sửa nhân viên DEMO");
        form.on("btnSaveStaff",context,()->{
            Role role=null;for(Role r:Role.values())if(r.displayName().equals(form.text("cboStaffPosition")))role=r;
            Employee employee=new Employee(id,form.text("txtStaffFullName"),role,form.date("dpStaffBirthDate").getValue(),
                form.text("cboStaffGender"),form.text("txtStaffPhone"),form.text("txtStaffEmail"),form.text("txtStaffAddress"),
                form.text("cboStaffStatus").equals("Hoạt động"));
            context.service.saveEmployee(employee,creating);stage.close();refresh();
        });
    }
}
