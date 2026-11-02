package vn.edu.nhom19.nhakhoa.view.manager;

import java.util.*;
import vn.edu.nhom19.nhakhoa.entity.DemoData.*;
import vn.edu.nhom19.nhakhoa.view.common.*;

/** UC006: tìm/lọc/chọn dịch vụ, mở form và lưu vào Service DEMO. */
public final class ServiceCatalogController extends ScreenController {
    private DentalService selected;
    public ServiceCatalogController(ViewAccess view,ControllerContext context){
        super(view,context);context.service.require(vn.edu.nhom19.nhakhoa.auth.Role.MANAGER);
        view.on("btnAddService",context,()->openForm(null));
        view.on("btnEditService",context,()->{if(selected!=null)openForm(selected);});
        for(String id:List.of("txtServiceSearch","cboServiceGroupFilter","cboServiceStatusFilter"))view.changed(id,this::refresh);
        view.table("tblServices").getSelectionModel().selectedItemProperty().addListener((o,a,b)->{
            selected=b==null?null:(DentalService)b.get("_model");showDetail();
        });
    }
    @Override public void refresh(){
        ArrayList<Map<String,Object>> rows=new ArrayList<>();String q=view.text("txtServiceSearch").toLowerCase(Locale.ROOT);
        for(DentalService s:context.service.services()){
            if(!(s.id+" "+s.name).toLowerCase(Locale.ROOT).contains(q))continue;
            String group=view.text("cboServiceGroupFilter"),status=view.text("cboServiceStatusFilter");
            if(!group.contains("Tất cả")&&!group.isBlank()&&!group.equals(s.group))continue;
            if(status.equals("Đang hoạt động")&&!s.active||status.equals("Ngừng hoạt động")&&s.active)continue;
            rows.add(ViewAccess.row(s,s.id,s.name,s.group,s.unit,ViewAccess.amount(s.price),s.active?"Hoạt động":"Ngừng hoạt động"));
        }
        view.paged("tblServices",rows,"lblServicesRecordCount","btnServicesPreviousPage","btnServicesNextPage","lblServicesCurrentPage");
        view.text("lblServicesDemoNotice","DEMO ONLY · Dữ liệu bộ nhớ, xóa khi đăng xuất · Nhóm dịch vụ chưa có cột SQL.");
        selected=null;showDetail();
    }
    private void showDetail(){
        view.show("serviceDetailsPane",selected!=null);if(selected==null)return;
        view.text("lblSelectedServiceName",selected.name);view.text("lblSelectedServiceId",selected.id);
        view.text("lblSelectedServiceStatus",selected.active?"Hoạt động":"Ngừng hoạt động");
        view.text("lblServiceDetailGroup",selected.group);view.text("lblServiceDetailUnit",selected.unit);
        view.text("lblServiceDetailPrice",ViewAccess.amount(selected.price));view.text("lblServiceDetailDuration",selected.duration+" phút");
        view.text("lblServiceDetailDescription",selected.description);
    }
    private void openForm(DentalService old){
        boolean creating=old==null;
        ViewAccess form=ViewAccess.load("manager/service-"+(creating?"add":"edit")+".fxml");
        String id=creating?context.service.nextId("DV"):old.id;
        form.text("txtServiceId",id);form.text("txtServiceName",creating?"":old.name);
        form.text("txtServicePrice",creating?"":old.price.toPlainString());form.text("txtServiceDuration",creating?"30":old.duration);
        form.text("txtServiceDescription",creating?"":old.description);
        form.combo("cboServiceGroup").setValue(creating?"Khám":old.group);
        form.combo("cboServiceUnit").setValue(creating?"Lần":old.unit);
        form.combo("cboServiceStatus").setValue(creating||old.active?"Hoạt động":"Ngừng hoạt động");
        var stage=context.dialog(form,creating?"Thêm dịch vụ DEMO":"Sửa dịch vụ DEMO");
        form.on("btnSaveService",context,()->{
            int duration;
            try{duration=Integer.parseInt(form.text("txtServiceDuration").replace("phút","").trim());}
            catch(NumberFormatException ex){throw new IllegalArgumentException("Thời gian phải là số phút nguyên.");}
            DentalService value=new DentalService(id,form.text("txtServiceName"),form.text("cboServiceGroup"),
                form.text("cboServiceUnit"),ViewAccess.money(form.text("txtServicePrice")),duration,
                form.text("txtServiceDescription"),form.text("cboServiceStatus").equals("Hoạt động"));
            context.service.saveService(value,creating);stage.close();refresh();
        });
    }
}
