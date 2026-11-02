package vn.edu.nhom19.nhakhoa.view.common;

import java.util.ArrayList;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.*;
import vn.edu.nhom19.nhakhoa.app.Route;
import vn.edu.nhom19.nhakhoa.service.DemoClinicService;

/** Dữ liệu và điều hướng chung của một phiên; không phải DI framework. */
public final class ControllerContext {
    public final DemoClinicService service;
    private final AppShellController shell;
    private final ArrayList<Stage> dialogs=new ArrayList<>();
    private boolean disposed;
    public ControllerContext(DemoClinicService service,AppShellController shell){this.service=service;this.shell=shell;}
    public boolean active(){if(disposed)return false;try{service.role();return true;}catch(IllegalStateException ex){return false;}}
    public void navigate(Route route){if(active())shell.navigate(route);}
    public void openExam(String id,boolean resume){if(active())shell.openExam(id,resume);}
    public void replace(ScreenController screen){if(active())shell.replacePage(screen);}
    public void profile(){if(active())ProfileController.open(this,shell.getRoot());}
    private Window dialogOwner(){
        return dialogs.isEmpty() ? (shell.getRoot().getScene()==null?null:shell.getRoot().getScene().getWindow()) : dialogs.getLast();
    }
    public Stage dialog(ViewAccess view,String title){return dialog(view,title,dialogOwner());}
    /** Riêng form Nhân sự: đo kích thước sau CSS và mở đầy đủ, không bọc ScrollPane. */
    public Stage staffDialog(ViewAccess view,String title){return dialog(view,title,dialogOwner(),true);}
    public Stage dialog(ViewAccess view,String title,Window owner){return dialog(view,title,owner,false);}
    private Stage dialog(ViewAccess view,String title,Window owner,boolean fullStaffForm){
        if(!active())throw new IllegalStateException("Phiên đã đóng.");
        Stage stage=new Stage();
        if(owner!=null){stage.initOwner(owner);stage.initModality(Modality.WINDOW_MODAL);}
        if(fullStaffForm){
            stage.setScene(new Scene(view.root));
            stage.setResizable(false);
        }else{
            ScrollPane scroll=new ScrollPane(view.root);scroll.setFitToWidth(true);
            double width=view.root.prefWidth(-1);if(width<=0)width=680;
            stage.setScene(new Scene(scroll,width+18,Math.min(760,Math.max(300,view.root.prefHeight(width)+20))));
        }
        stage.setTitle("DentaFlow · DEMO ONLY · "+title);
        stage.setOnHidden(event->{dialogs.remove(stage);view.root.setDisable(true);});dialogs.add(stage);
        String[] cancel={"btnClose","btnCancel","btnCloseStaffForm","btnCancelStaffForm","btnCloseServiceForm","btnCancelServiceForm","btnCancelAssignmentForm","btnKeepAssignment","btnKeepMedicine"};
        for(String id:cancel)view.on(id,this,stage::close);
        stage.show();
        if(fullStaffForm)fitStaffDialog(stage,view,owner);
        return stage;
    }
    private void fitStaffDialog(Stage stage,ViewAccess view,Window owner){
        // Stage đã có Scene/skin: prefHeight lúc này gồm padding, font và chiều cao thật của các ô.
        view.root.applyCss();stage.sizeToScene();
        Screen screen=Screen.getPrimary();
        if(owner!=null){
            var screens=Screen.getScreensForRectangle(owner.getX(),owner.getY(),owner.getWidth(),owner.getHeight());
            if(!screens.isEmpty())screen=screens.getFirst();
        }
        var bounds=screen.getVisualBounds();
        if(stage.getHeight()>bounds.getHeight()-24){
            view.root.getStyleClass().add("staff-form-compact");
            view.root.applyCss();stage.sizeToScene();
        }
        double x=owner==null?bounds.getMinX()+(bounds.getWidth()-stage.getWidth())/2:owner.getX()+(owner.getWidth()-stage.getWidth())/2;
        double y=owner==null?bounds.getMinY()+(bounds.getHeight()-stage.getHeight())/2:owner.getY()+(owner.getHeight()-stage.getHeight())/2;
        stage.setX(Math.max(bounds.getMinX()+12,Math.min(x,bounds.getMaxX()-stage.getWidth()-12)));
        stage.setY(Math.max(bounds.getMinY()+12,Math.min(y,bounds.getMaxY()-stage.getHeight()-12)));
    }
    public void dispose(){
        disposed=true;
        for(Stage stage:new ArrayList<>(dialogs))stage.close();
        dialogs.clear();service.dispose();
    }
}
