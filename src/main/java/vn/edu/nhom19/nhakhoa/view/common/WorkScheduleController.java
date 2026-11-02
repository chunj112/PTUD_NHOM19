package vn.edu.nhom19.nhakhoa.view.common;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import vn.edu.nhom19.nhakhoa.entity.DemoData.*;
import vn.edu.nhom19.nhakhoa.service.DemoClinicService;

/** UC003: chỉ đọc phân công của nhân viên trong phiên; không có thao tác sửa lịch. */
public final class WorkScheduleController extends ScreenController {
    private final ViewAccess day,weekView;
    private LocalDate week=DemoClinicService.DEMO_DATE.with(DayOfWeek.MONDAY);
    private final boolean dentist;
    public WorkScheduleController(ViewAccess view,ControllerContext context){
        super(view,context);dentist=context.service.role()==vn.edu.nhom19.nhakhoa.auth.Role.DENTIST;
        day=view.sub("tabScheduleDayContent");weekView=view.sub("tabScheduleWeekContent");
        weekView.on("btnPreviousScheduleWeek",context,()->{week=week.minusWeeks(1);refresh();});
        weekView.on("btnNextScheduleWeek",context,()->{week=week.plusWeeks(1);refresh();});
        weekView.on("btnScheduleToday",context,()->{week=DemoClinicService.DEMO_DATE.with(DayOfWeek.MONDAY);refresh();});
        view.choices("cboScheduleWeek",List.of("Tuần mẫu hiện tại","Tuần mẫu trước","Tuần mẫu sau"),"Tuần mẫu hiện tại");
        view.changed("cboScheduleWeek",()->{
            int offset=view.combo("cboScheduleWeek").getSelectionModel().getSelectedIndex();
            week=DemoClinicService.DEMO_DATE.with(DayOfWeek.MONDAY).plusWeeks(offset==1?-1:offset==2?1:0);refresh();
        });
        day.table("tblDailyShifts").getSelectionModel().selectedItemProperty().addListener((o,a,b)->{if(b!=null)detail(day,(Assignment)b.get("_model"));});
    }
    @Override public void refresh(){
        ArrayList<Map<String,Object>> daily=new ArrayList<>(),upcoming=new ArrayList<>();
        Assignment first=null;
        for(Assignment a:context.service.assignments())if(a.active){
            if(a.date.equals(DemoClinicService.DEMO_DATE)){daily.add(ViewAccess.row(a,a.start+"–"+a.end,a.room,a.work,"Đã phân công"));if(first==null)first=a;}
            if(a.date.isAfter(DemoClinicService.DEMO_DATE))upcoming.add(ViewAccess.row(a,ViewAccess.DATE.format(a.date),a.start+"–"+a.end,a.room,a.work));
        }
        day.rows("tblDailyShifts",daily);day.text("tblDailyShiftsCount","Hiển thị "+daily.size()+" / "+daily.size()+" bản ghi");
        day.text("lblDayScheduleDate","Lịch ngày DEMO · "+ViewAccess.DATE.format(DemoClinicService.DEMO_DATE));
        day.text("lblScheduleDayTitle","Lịch ngày DEMO · "+ViewAccess.DATE.format(DemoClinicService.DEMO_DATE));
        for(ViewAccess part:List.of(day,weekView)){
            part.rows("tblUpcomingShifts",upcoming);part.text("tblUpcomingShiftsCount","Hiển thị "+upcoming.size()+" / "+upcoming.size()+" bản ghi");
            if(first!=null)detail(part,first);
        }
        GridPane grid=(GridPane)weekView.find(dentist?"dentistWeekCalendar":"receptionistScheduleGrid");grid.getChildren().clear();
        weekView.text("lblScheduleWeek",ViewAccess.DATE.format(week)+" – "+ViewAccess.DATE.format(week.plusDays(6)));
        String prefix=dentist?"dt-":"rx-";
        for(int i=0;i<7;i++){
            LocalDate date=week.plusDays(i);VBox column=new VBox(8);column.setMinWidth(0);column.getStyleClass().add(prefix+"schedule-day");
            column.getChildren().add(label(date.format(DateTimeFormatter.ofPattern("EEE dd/MM",Locale.forLanguageTag("vi"))),prefix+"caption"));
            boolean any=false;
            for(Assignment a:context.service.assignments())if(a.active&&a.date.equals(date)){
                VBox card=new VBox(6);card.setMinWidth(0);card.getStyleClass().add(dentist?"dt-shift-card":"rx-shift-card");
                card.getChildren().addAll(label(a.start+"–\n"+a.end,prefix+"shift-time"),label(a.work,prefix+"caption"),label(a.room,prefix+"caption"));
                card.setOnMouseClicked(event->{if(context.active())detail(weekView,a);});column.getChildren().add(card);any=true;
            }
            if(!any)column.getChildren().add(label("Không có ca",prefix+"caption"));
            grid.add(column,i,0);
        }
    }
    private Label label(String text,String style){Label l=new Label(text);l.setWrapText(true);l.setMinHeight(Region.USE_PREF_SIZE);l.getStyleClass().add(style);return l;}
    private void detail(ViewAccess part,Assignment a){
        Employee e=context.service.employee(a.employeeId);
        part.text(dentist?"lblShiftEmployee":"lblReceptionistEmployee",e.toString());
        part.text(dentist?"lblShiftTime":"lblReceptionistShift",ViewAccess.DATE.format(a.date)+" · "+a.start+"–"+a.end);
        part.text(dentist?"lblShiftRoom":"lblReceptionistTimeRoom",a.room);
        part.text(dentist?"lblShiftWork":"lblReceptionistWork",a.work);
        part.text("lblShiftManagerNote","DEMO · Chỉ hiển thị nội dung phân công, chưa có cột ghi chú riêng.");
        part.text("lblShiftStatus","Đã phân công");part.text("lblScheduleStatus","Đã phân công");
    }
}
