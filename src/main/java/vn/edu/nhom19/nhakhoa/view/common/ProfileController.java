package vn.edu.nhom19.nhakhoa.view.common;

import java.util.List;
import javafx.scene.Parent;
import vn.edu.nhom19.nhakhoa.auth.Role;
import vn.edu.nhom19.nhakhoa.entity.DemoData.*;

/** UC002 DEMO: chỉ đổi thông tin được phép; không lấy/lưu mật khẩu. */
public final class ProfileController {
    private ProfileController(){}
    public static void open(ControllerContext context,Parent owner){
        Role role=context.service.role();
        if(role!=Role.DENTIST&&role!=Role.RECEPTIONIST){
            new ViewAccess(owner).error("Role này chưa có FXML hồ sơ cá nhân. Không tạo giao diện mới trong lượt Controller.");return;
        }
        String folder=role==Role.DENTIST?"dentist/":"receptionist/";
        ViewAccess form=ViewAccess.load(folder+"profile-drawer.fxml");
        Employee e=context.service.employee(context.service.currentEmployeeId());
        form.text("lblProfileName",e.name);form.text("lblProfileFullName",e.name);
        form.text("lblProfileIdentity",e.id+" · "+e.role.displayName());form.text("lblProfileIdRole",e.id+" · "+e.role.displayName());
        form.text("lblProfileId",e.id);form.text("lblProfileEmployeeId",e.id);form.text("lblProfileRole",e.role.displayName());
        form.text("lblProfilePhone",e.phone);form.text("lblProfileEmail",e.email);form.text("lblProfileBirthDate",ViewAccess.DATE.format(e.birthday));
        form.text("lblProfileGender",e.gender);form.text("lblProfileAddress",e.address);
        var stage=context.dialog(form,"Hồ sơ DEMO");
        Runnable edit=()->{stage.close();edit(context,folder,e);};
        form.on("btnEditProfile",context,edit);form.on("btnEditPersonalProfile",context,edit);
        form.on("btnChangePassword",context,()->form.error("Login DEMO không có mật khẩu. Chưa có AuthService/hash để đổi mật khẩu thật."));
    }
    private static void edit(ControllerContext context,String folder,Employee old){
        ViewAccess form=ViewAccess.load(folder+"profile-edit.fxml");
        form.text("txtProfileEmployeeId",old.id);form.text("txtProfileRole",old.role.displayName());form.text("txtProfilePermission",old.role.name());
        form.text("txtProfileName",old.name);form.text("txtProfilePhone",old.phone);form.text("txtProfileEmail",old.email);
        form.date("dpProfileBirthDate",old.birthday);form.choices("cboProfileGender",List.of("Nam","Nữ"),old.gender);
        form.text("txtProfileAddress",old.address);
        var stage=context.dialog(form,"Sửa hồ sơ DEMO");
        form.on("btnSaveProfile",context,()->{
            context.service.saveProfile(new Employee(old.id,form.text("txtProfileName"),old.role,form.date("dpProfileBirthDate").getValue(),
                form.text("cboProfileGender"),form.text("txtProfilePhone"),form.text("txtProfileEmail"),form.text("txtProfileAddress"),old.active));
            stage.close();open(context,form.root);
        });
    }
}
