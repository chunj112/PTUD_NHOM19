package vn.edu.nhom19.nhakhoa.view.manager;

import java.math.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import vn.edu.nhom19.nhakhoa.auth.Role;
import vn.edu.nhom19.nhakhoa.entity.DemoData.*;
import vn.edu.nhom19.nhakhoa.service.DemoClinicService;
import vn.edu.nhom19.nhakhoa.view.common.*;

/** UC007 DEMO: đọc dữ liệu trong phiên, không SQL và không xuất Excel/PDF.
 * Ba tab chỉ lọc ngày. Khoảng ngày đã Áp dụng được giữ riêng với giá trị đang nhập.
 */
public final class ReportsController extends ScreenController {
    private final ViewAccess revenue, visits, usage;
    private final LocalDate[][] appliedRanges=new LocalDate[3][];
    private List<Map<String,Object>> revenueRows=new ArrayList<>();
    private List<Map<String,Object>> visitRows=new ArrayList<>();
    private List<Map<String,Object>> usageRows=new ArrayList<>();

    public ReportsController(ViewAccess view,ControllerContext context){
        super(view,context);
        context.service.require(Role.MANAGER);
        revenue=view.sub("revenueContent");
        visits=view.sub("visitsContent");
        usage=view.sub("serviceUsageContent");
        for(int tab=0;tab<3;tab++){
            ViewAccess part=part(tab);
            String prefix=prefix(tab);
            appliedRanges[tab]=new LocalDate[]{DemoClinicService.DEMO_DATE.minusDays(8),DemoClinicService.DEMO_DATE};
            part.date("dp"+prefix+"From",appliedRanges[tab][0]);
            part.date("dp"+prefix+"To",appliedRanges[tab][1]);
            final int selectedTab=tab;
            part.on("btnApply"+prefix+"Report",context,()->apply(selectedTab));
        }
        view.on("btnExportReport",context,this::export);
    }
    private ViewAccess part(int tab){return tab==0?revenue:tab==1?visits:usage;}
    private String prefix(int tab){return tab==0?"Revenue":tab==1?"Visits":"ServiceUsage";}
    private boolean between(LocalDate date,LocalDate[] range){
        return !date.isBefore(range[0])&&!date.isAfter(range[1]);
    }
    private void apply(int tab){
        ViewAccess part=part(tab);
        LocalDate from=part.date("dp"+prefix(tab)+"From").getValue();
        LocalDate to=part.date("dp"+prefix(tab)+"To").getValue();
        if(from==null||to==null||from.isAfter(to))throw new IllegalArgumentException("Khoảng thời gian không hợp lệ.");
        // Chỉ thay kỳ của tab vừa Áp dụng. Tab khác và dữ liệu xuất giữ nguyên kỳ cũ.
        LocalDate[] previous=appliedRanges[tab];
        appliedRanges[tab]=new LocalDate[]{from,to};
        try{load(tab);}catch(RuntimeException ex){appliedRanges[tab]=previous;throw ex;}
    }
    @Override public void refresh(){for(int tab=0;tab<3;tab++)load(tab);}
    private void load(int tab){
        if(tab==0)loadRevenue();else if(tab==1)loadVisits();else loadUsage();
    }
    private String period(int tab){
        return ViewAccess.DATE.format(appliedRanges[tab][0])+" – "+ViewAccess.DATE.format(appliedRanges[tab][1]);
    }
    private String percent(int part,int total){
        return total==0?"—":String.format(Locale.forLanguageTag("vi"),"%.1f%%",part*100.0/total);
    }
    private void loadRevenue(){
        LocalDate[] range=appliedRanges[0];
        TreeMap<LocalDate,BigDecimal> totals=new TreeMap<>();
        TreeMap<LocalDate,Integer> counts=new TreeMap<>();
        Map<LocalDate,Integer> completed=new HashMap<>();
        // Nguồn DEMO đã tải đầy đủ: tạo cả những ngày biết chắc không phát sinh.
        // Backend thật phải báo lỗi tải dữ liệu; không dùng catch để biến lỗi thành số 0.
        for(LocalDate day=range[0];!day.isAfter(range[1]);day=day.plusDays(1)){
            totals.put(day,BigDecimal.ZERO);counts.put(day,0);
        }
        BigDecimal paid=BigDecimal.ZERO,pending=BigDecimal.ZERO;
        int count=0;
        for(Invoice invoice:context.service.invoices()){
            if(invoice.paid&&between(invoice.paidAt.toLocalDate(),range)){
                LocalDate day=invoice.paidAt.toLocalDate();
                paid=paid.add(invoice.total);count++;
                totals.put(day,totals.get(day).add(invoice.total));counts.put(day,counts.get(day)+1);
            }else if(!invoice.paid&&between(invoice.created.toLocalDate(),range))pending=pending.add(invoice.total);
        }
        for(Exam exam:context.service.exams())if(exam.status.equals("DA_KHAM")){
            LocalDate day=exam.received.toLocalDate();completed.put(day,completed.getOrDefault(day,0)+1);
        }
        revenueRows=new ArrayList<>();
        for(LocalDate day:totals.keySet())revenueRows.add(ViewAccess.row(day,ViewAccess.DATE.format(day),
            completed.getOrDefault(day,0),counts.get(day),ViewAccess.amount(totals.get(day))));
        revenue.paged("tblRevenue",revenueRows,"lblRevenueRecordCount","btnRevenuePreviousPage","btnRevenueNextPage","lblRevenueCurrentPage");
        revenue.table("tblRevenue").setSelectionModel(null);
        revenue.text("lblRevenueCollected",ViewAccess.amount(paid));
        revenue.text("lblRevenueInvoiceCount",count);
        revenue.text("lblRevenueAverage",ViewAccess.amount(count==0?BigDecimal.ZERO:paid.divide(BigDecimal.valueOf(count),0,RoundingMode.HALF_UP)));
        revenue.text("lblRevenuePending",ViewAccess.amount(pending));
        revenue.text("lblRevenuePendingNote","Hóa đơn chưa thanh toán, theo ngày lập");
        TreeMap<LocalDate,Double> chart=new TreeMap<>();
        totals.forEach((day,value)->chart.put(day,value.doubleValue()));
        plot(revenue,"chartRevenue",chart);
        revenue.text("lblRevenueChartNote",chartNote(0,totals.size())+" · Theo ngày xác nhận tiền; không gồm tiền thuốc.");
    }
    private void loadVisits(){
        LocalDate[] range=appliedRanges[1];
        TreeMap<LocalDate,int[]> daily=new TreeMap<>();
        for(LocalDate day=range[0];!day.isAfter(range[1]);day=day.plusDays(1))daily.put(day,new int[3]);
        int total=0,waiting=0,examining=0,done=0;
        for(Exam exam:context.service.exams()){
            LocalDate day=exam.received.toLocalDate();
            if(!between(day,range))continue;
            int index;
            switch(exam.status){
                case "CHO_KHAM":index=0;waiting++;break;
                case "DANG_KHAM":index=1;examining++;break;
                case "DA_KHAM":index=2;done++;break;
                default:throw new IllegalStateException("Trạng thái phiếu khám chưa được hỗ trợ: "+exam.status);
            }
            daily.get(day)[index]++;total++;
        }
        visitRows=new ArrayList<>();TreeMap<LocalDate,Double> chart=new TreeMap<>();
        for(var entry:daily.entrySet()){
            int[] n=entry.getValue();int all=n[0]+n[1]+n[2];
            visitRows.add(ViewAccess.row(entry.getKey(),ViewAccess.DATE.format(entry.getKey()),all,n[0],n[1],n[2],percent(n[2],all)));
            chart.put(entry.getKey(),(double)all);
        }
        visits.paged("tblVisits",visitRows,"lblVisitsRecordCount","btnVisitsPreviousPage","btnVisitsNextPage","lblVisitsCurrentPage");
        visits.table("tblVisits").setSelectionModel(null);
        visits.text("lblVisitsTotal",total);visits.text("lblVisitsTotalNote",waiting+" phiếu chờ khám");
        visits.text("lblVisitsExamining",examining);visits.text("lblVisitsExaminingNote","Trạng thái DANG_KHAM");
        visits.text("lblVisitsCompleted",done);visits.text("lblVisitsCompletedNote","Trạng thái DA_KHAM");
        visits.text("lblVisitsCompletionRate",percent(done,total));visits.text("lblVisitsCompletionRateNote",done+" / "+total+" phiếu");
        plot(visits,"chartVisits",chart);
        visits.text("lblVisitsChartNote",chartNote(1,daily.size())+" · Chờ khám / Đang khám / Đã khám; không tính trạng thái lịch hẹn.");
    }
    private void loadUsage(){
        LocalDate[] range=appliedRanges[2];
        Map<String,Integer> counts=new HashMap<>();Map<String,BigDecimal> paid=new HashMap<>();
        int total=0,completed=0;
        for(Exam exam:context.service.exams())if(between(exam.received.toLocalDate(),range)){
            if(exam.status.equals("DA_KHAM"))completed++;
            for(Treatment treatment:exam.treatments)if(treatment.performed){
                counts.put(treatment.serviceId,counts.getOrDefault(treatment.serviceId,0)+1);total++;
            }
        }
        for(Invoice invoice:context.service.invoices())if(invoice.paid&&between(invoice.paidAt.toLocalDate(),range)){
            for(Treatment line:invoice.lines)paid.put(line.serviceId,paid.getOrDefault(line.serviceId,BigDecimal.ZERO).add(line.price));
        }
        // Giữ cả dịch vụ thu tiền trong kỳ nhưng được thực hiện ở kỳ trước trong bảng doanh thu.
        Set<String> ids=new HashSet<>(counts.keySet());ids.addAll(paid.keySet());
        ArrayList<String> ranked=new ArrayList<>(ids);
        ranked.sort(Comparator.<String>comparingInt(id->counts.getOrDefault(id,0)).reversed().thenComparing(id->context.service.dentalService(id).name));
        usageRows=new ArrayList<>();BigDecimal revenueTotal=BigDecimal.ZERO;
        for(String id:ranked){
            DentalService service=context.service.dentalService(id);int n=counts.getOrDefault(id,0);
            BigDecimal amount=paid.getOrDefault(id,BigDecimal.ZERO);revenueTotal=revenueTotal.add(amount);
            usageRows.add(ViewAccess.row(service,service.name,n,ViewAccess.amount(service.price),ViewAccess.amount(amount),percent(n,total)));
        }
        usage.paged("tblServiceUsage",usageRows,"lblServiceUsageRecordCount","btnServiceUsagePreviousPage","btnServiceUsageNextPage","lblServiceUsageCurrentPage");
        usage.table("tblServiceUsage").setSelectionModel(null);
        int max=ranked.isEmpty()?0:counts.getOrDefault(ranked.get(0),0);
        usage.text("lblServiceUsageTotal",total);
        usage.text("lblServiceUsagePopular",max==0?"—":context.service.dentalService(ranked.get(0)).name);
        usage.text("lblServiceUsagePopularNote",max+" lượt · "+percent(max,total));
        usage.text("lblServiceUsageRevenue",ViewAccess.amount(revenueTotal));
        usage.text("lblServiceUsageRevenueNote","Theo ngày thanh toán đã xác nhận trong kỳ");
        usage.text("lblServiceUsageAverage",completed==0?"—":String.format(Locale.forLanguageTag("vi"),"%.2f",total/(double)completed));
        usage.text("lblServiceUsageAverageNote",total+" dịch vụ / "+completed+" phiếu hoàn tất");
        usage.text("lblServiceUsageChartSubtitle","Tỷ trọng theo số lượt đã thực hiện; dịch vụ ít phổ biến gộp vào Khác.");
        usage.text("lblServiceUsageChartNote","DEMO · Đã áp dụng "+period(2)+" · Bảng giữ từng dịch vụ. Lượt theo ngày khám, doanh thu theo ngày thu tiền.");
        PieChart chart=(PieChart)usage.find("chartServiceUsage");chart.getData().clear();
        int others=0,shown=0;
        for(String id:ranked){
            int n=counts.getOrDefault(id,0);if(n==0)continue;
            if(shown<4&&n*100.0/total>=5){
                chart.getData().add(new PieChart.Data(context.service.dentalService(id).name+" · "+percent(n,total),n));shown++;
            }else others+=n;
        }
        if(others>0)chart.getData().add(new PieChart.Data("Khác · "+percent(others,total),others));
        usage.show("chartServiceUsage",total>0);usage.show("lblServiceUsageEmpty",total==0);
    }
    private String chartNote(int tab,int days){
        return "DEMO · Đã áp dụng "+period(tab)+" · "+days+" ngày, gồm ngày không phát sinh"+
            (days>14?" · Biểu đồ gộp khoảng liên tiếp; bảng giữ từng ngày":"");
    }
    private void plot(ViewAccess part,String id,TreeMap<LocalDate,Double> daily){
        HBox chart=(HBox)part.find(id);chart.getChildren().clear();
        List<LocalDate> days=new ArrayList<>(daily.keySet());
        int bucket=Math.max(1,(int)Math.ceil(days.size()/14.0));
        ArrayList<String> names=new ArrayList<>();ArrayList<Double> values=new ArrayList<>();
        DateTimeFormatter format=DateTimeFormatter.ofPattern("dd/MM");
        for(int start=0;start<days.size();start+=bucket){
            int end=Math.min(start+bucket,days.size())-1;double value=0;
            for(int i=start;i<=end;i++)value+=daily.get(days.get(i));
            names.add(days.get(start).format(format)+(start==end?"":"–"+days.get(end).format(format)));values.add(value);
        }
        double max=1;for(double value:values)max=Math.max(max,value);
        for(int i=0;i<names.size();i++){
            Label number=new Label(String.format(Locale.forLanguageTag("vi"),"%,.0f",values.get(i)));number.getStyleClass().add("reports-bar-value");number.setWrapText(true);
            Region bar=new Region();bar.getStyleClass().add("reports-bar");bar.setPrefWidth(40);bar.setMaxWidth(40);
            double height=140*values.get(i)/max;bar.setMinHeight(height);bar.setPrefHeight(height);bar.setMaxHeight(height);
            Label name=new Label(names.get(i));name.setWrapText(true);name.getStyleClass().add("reports-axis-label");
            VBox column=new VBox(6,number,bar,name);column.setAlignment(Pos.BOTTOM_CENTER);column.setMinWidth(0);column.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(column,Priority.ALWAYS);chart.getChildren().add(column);
        }
    }
    private void export(){
        int tab=((TabPane)view.find("reportsTabs")).getSelectionModel().getSelectedIndex();
        ViewAccess part=part(tab);String table=tab==0?"tblRevenue":tab==1?"tblVisits":"tblServiceUsage";
        List<Map<String,Object>> source=tab==0?revenueRows:tab==1?visitRows:usageRows;
        ArrayList<String> headings=new ArrayList<>();for(TableColumn<?,?> column:part.table(table).getColumns())headings.add(column.getText());
        ArrayList<List<String>> output=new ArrayList<>();
        for(Map<String,Object> row:source){
            ArrayList<String> cells=new ArrayList<>();for(int i=0;i<headings.size();i++)cells.add(String.valueOf(row.get("v"+i)));output.add(cells);
        }
        // Dùng lại CSV hiện có; không gọi Apply và không đọc lại DatePicker đang chỉnh.
        String filename="bao-cao-"+prefix(tab).toLowerCase(Locale.ROOT)+"-"+appliedRanges[tab][0]+"-"+appliedRanges[tab][1]+"-demo.csv";
        view.export(filename,headings,output);
    }
}
