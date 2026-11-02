package vn.edu.nhom19.nhakhoa.view.receptionist;

import java.math.BigDecimal;
import java.util.*;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import vn.edu.nhom19.nhakhoa.auth.Role;
import vn.edu.nhom19.nhakhoa.entity.DemoData.*;
import vn.edu.nhom19.nhakhoa.service.DemoClinicService;
import vn.edu.nhom19.nhakhoa.view.common.*;

/** UC012 DEMO: lập hóa đơn từ snapshot dịch vụ đã làm, thu đủ một lần.
 * Không tích hợp ngân hàng, không ghi nhận thanh toán thật và không tính tiền thuốc.
 */
public final class CashierController extends ScreenController {
    private final ViewAccess ready,waiting,paid;
    public CashierController(ViewAccess view,ControllerContext context){
        super(view,context);context.service.require(Role.RECEPTIONIST);
        ready=view.sub("tabReadyInvoiceContent");waiting=view.sub("tabAwaitingPaymentContent");paid=view.sub("tabPaidContent");
        for(ViewAccess part:List.of(ready,waiting,paid)){
            part.date("dpCashierDate",DemoClinicService.DEMO_DATE);part.changed("txtCashierSearch",this::refresh);part.changed("dpCashierDate",this::refresh);
        }
        paid.choices("cboCashierPaymentMethod",List.of("Tất cả","TIEN_MAT","CHUYEN_KHOAN"),"Tất cả");
        paid.changed("cboCashierPaymentMethod",this::refresh);
        ready.table("tblReadyExams").getSelectionModel().selectedItemProperty().addListener((o,a,b)->{if(b==null)ready.show("draftInvoiceDetails",false);else readyDetail((Exam)b.get("_model"));});
        waiting.table("tblWaitingInvoices").getSelectionModel().selectedItemProperty().addListener((o,a,b)->invoiceDetail(waiting,b==null?null:(Invoice)b.get("_model")));
        paid.table("tblPaidInvoices").getSelectionModel().selectedItemProperty().addListener((o,a,b)->invoiceDetail(paid,b==null?null:(Invoice)b.get("_model")));
    }
    private boolean matches(ViewAccess part,Exam exam,java.time.LocalDate date){
        String q=part.text("txtCashierSearch").toLowerCase(Locale.ROOT);
        return (exam.id+" "+context.service.patient(exam.patientId)).toLowerCase(Locale.ROOT).contains(q)&&date.equals(part.date("dpCashierDate").getValue());
    }
    @Override public void refresh(){
        ArrayList<Map<String,Object>> unbilled=new ArrayList<>(),unpaid=new ArrayList<>(),done=new ArrayList<>();
        for(Exam e:context.service.exams())if(e.status.equals("DA_KHAM")&&matches(ready,e,e.received.toLocalDate())){
            boolean billed=false;for(Invoice i:context.service.invoices())if(i.examId.equals(e.id))billed=true;
            if(!billed)unbilled.add(ViewAccess.row(e,e.id,context.service.patient(e.patientId).toString(),"Đã khám"));
        }
        for(Invoice i:context.service.invoices()){
            Exam e=context.service.exam(i.examId);
            if(!i.paid&&matches(waiting,e,i.created.toLocalDate()))unpaid.add(ViewAccess.row(i,i.id,context.service.patient(e.patientId).toString(),ViewAccess.DATE.format(i.created.toLocalDate()),ViewAccess.amount(i.total),"Chờ thanh toán"));
            if(i.paid&&matches(paid,e,i.paidAt.toLocalDate())){
                String method=paid.text("cboCashierPaymentMethod");
                if(method.equals("Tất cả")||method.equals(i.method))done.add(ViewAccess.row(i,i.id,context.service.patient(e.patientId).toString(),ViewAccess.DATE.format(i.paidAt.toLocalDate()),ViewAccess.amount(i.total),i.method,"Đã thanh toán"));
            }
        }
        ready.paged("tblReadyExams",unbilled,"tblReadyExamsCount","tblReadyExamsPrevious","tblReadyExamsNext","tblReadyExamsPage");
        waiting.paged("tblWaitingInvoices",unpaid,"tblWaitingInvoicesCount","tblWaitingInvoicesPrevious","tblWaitingInvoicesNext","tblWaitingInvoicesPage");
        paid.paged("tblPaidInvoices",done,"tblPaidInvoicesCount","tblPaidInvoicesPrevious","tblPaidInvoicesNext","tblPaidInvoicesPage");
        ready.show("draftInvoiceDetails",false);invoiceDetail(waiting,null);invoiceDetail(paid,null);
    }
    private void readyDetail(Exam exam){
        ready.show("draftInvoiceDetails",true);ready.text("lblInvoiceDetailTitle","Chi phí · "+exam.id);
        ready.text("lblInvoicePatient",context.service.patient(exam.patientId).toString());ready.text("lblInvoiceExamDentist",context.service.employee(exam.dentistId).name);
        ready.text("lblInvoiceExamTime",ViewAccess.DATE.format(exam.received.toLocalDate()));ready.text("lblInvoiceServiceTotal",ViewAccess.amount(context.service.performedTotal(exam)));
        ready.text("lblInvoiceAmount",ViewAccess.amount(context.service.performedTotal(exam)));
        ArrayList<Map<String,Object>> rows=new ArrayList<>();for(Treatment t:exam.treatments)if(t.performed)rows.add(line(t));
        ready.rows("tblInvoiceServices",rows);
        ready.on("btnCreateInvoice",context,()->{
            Invoice invoice=context.service.createInvoice(exam);refresh();
            ((TabPane)view.find("cashierTabs")).getSelectionModel().select(1);invoiceDetail(waiting,invoice);
        });
        ready.on("btnPrintEstimate",context,()->print(exam,null));
    }
    private Map<String,Object> line(Treatment t){return ViewAccess.row(t,t.name,t.tooth==0?"Toàn hàm":t.tooth,1,ViewAccess.amount(t.price),ViewAccess.amount(t.price));}
    private void invoiceDetail(ViewAccess part,Invoice invoice){
        part.show("selectedInvoicePane",invoice!=null);part.show("invoiceSelectionHint",invoice==null);if(invoice==null)return;
        Exam exam=context.service.exam(invoice.examId);
        part.text("lblInvoiceDetailTitle","Hóa đơn DEMO · "+invoice.id);part.text("lblInvoiceDetailStatus",invoice.paid?"Đã thanh toán DEMO":"Chờ thanh toán");
        part.text("lblInvoicePatient",context.service.patient(exam.patientId).toString());part.text("lblInvoiceExamDentist",exam.id+" · "+context.service.employee(exam.dentistId).name);
        part.text("lblInvoiceCreated",invoice.created.toString());part.text("lblInvoiceServiceTotal",ViewAccess.amount(invoice.total));part.text("lblInvoiceAmount",ViewAccess.amount(invoice.total));
        ArrayList<Map<String,Object>> rows=new ArrayList<>();for(Treatment t:invoice.lines)rows.add(line(t));part.rows("tblInvoiceServices",rows);
        part.text("tblInvoiceServicesCount","Hiển thị "+rows.size()+" / "+rows.size()+" bản ghi");
        part.text("lblInvoicePaidMethod",invoice.method);part.text("lblInvoicePaidAt",invoice.paidAt==null?"—":invoice.paidAt.toString());
        part.text("lblInvoiceConfirmedBy","NV004 · Phạm Thu Hà · DEMO");part.text("lblInvoicePaidConfirmation","DEMO ONLY · Thay đổi trong bộ nhớ, không xác nhận tiền thật.");
        part.on("btnPayCash",context,()->cash(invoice));part.on("btnPayTransfer",context,()->transfer(invoice));
        part.on("btnPrintInvoice",context,()->print(exam,invoice));
    }
    private void cash(Invoice invoice){
        if(invoice.paid)throw new IllegalArgumentException("Hóa đơn đã thanh toán.");
        ViewAccess form=ViewAccess.load("receptionist/payment-cash.fxml");
        form.text("lblCashInvoice",invoice.id);form.text("lblCashPatient",context.service.patient(context.service.exam(invoice.examId).patientId).toString());
        form.text("lblCashAmount",ViewAccess.amount(invoice.total));form.text("txtCashTendered","");form.text("txtCashChange","—");
        form.text("lblCashPaymentRule","DEMO ONLY · Xác nhận chỉ cập nhật bộ nhớ, không ghi nhận đã thu tiền thật.");
        form.changed("txtCashTendered",()->{
            try{BigDecimal value=ViewAccess.money(form.text("txtCashTendered")).subtract(invoice.total);form.text("txtCashChange",value.signum()<0?"Chưa đủ":ViewAccess.amount(value));}
            catch(IllegalArgumentException invalid){form.text("txtCashChange","—");}
        });
        var stage=context.dialog(form,"Thu tiền mặt DEMO");
        form.on("btnConfirmCashPayment",context,()->{
            context.service.pay(invoice,"TIEN_MAT",ViewAccess.money(form.text("txtCashTendered")),true);stage.close();refresh();
        });
    }
    private void transfer(Invoice invoice){
        if(invoice.paid)throw new IllegalArgumentException("Hóa đơn đã thanh toán.");
        ViewAccess form=ViewAccess.load("receptionist/payment-transfer.fxml");
        form.text("lblTransferInvoicePatient",invoice.id+" · "+context.service.patient(context.service.exam(invoice.examId).patientId));
        form.text("lblTransferAmount",ViewAccess.amount(invoice.total));form.text("lblTransferContent",invoice.id);
        form.text("lblTransferInstructions","DEMO ONLY · QR minh họa. Không chuyển tiền thật.");
        form.text("lblTransferVerification","Không có tích hợp ngân hàng. Nút xác nhận chỉ thay đổi dữ liệu DEMO trong bộ nhớ.");
        var stage=context.dialog(form,"Chuyển khoản DEMO");
        form.on("btnConfirmTransferReceived",context,()->{
            ViewAccess confirm=ViewAccess.load("receptionist/payment-transfer-confirm.fxml");
            confirm.text("lblConfirmTransferPatient",context.service.patient(context.service.exam(invoice.examId).patientId).toString());
            confirm.text("lblConfirmTransferInvoice",invoice.id);confirm.text("lblConfirmTransferAmount",ViewAccess.amount(invoice.total));
            confirm.text("lblConfirmTransferWarning","Chỉ xác nhận DEMO sau bước kiểm tra thủ công; không có giao dịch ngân hàng thật.");
            var dialog=context.dialog(confirm,"Xác nhận chuyển khoản DEMO",stage);
            confirm.on("btnBackToTransfer",context,dialog::close);
            confirm.on("btnConfirmTransferPayment",context,()->{context.service.pay(invoice,"CHUYEN_KHOAN",invoice.total,true);dialog.close();stage.close();refresh();});
        });
    }
    private void print(Exam exam,Invoice invoice){
        PrinterJob job=PrinterJob.createPrinterJob();if(job==null)throw new IllegalStateException("Máy chưa có máy in khả dụng.");
        VBox document=new VBox(10,new Label("DENTAFLOW · DEMO ONLY"),new Label(invoice==null?"BẢNG CHI PHÍ DEMO":invoice.id),
            new Label(context.service.patient(exam.patientId).toString()));
        List<Treatment> lines=invoice==null?exam.treatments:invoice.lines;
        for(Treatment t:lines)if(t.performed)document.getChildren().add(new Label(t.name+" · "+ViewAccess.amount(t.price)));
        document.getChildren().add(new Label("Tổng: "+ViewAccess.amount(invoice==null?context.service.performedTotal(exam):invoice.total)));
        if(job.showPrintDialog(view.root.getScene().getWindow())){
            if(!job.printPage(document)){job.cancelJob();throw new IllegalStateException("Không in được tài liệu.");}
            if(!job.endJob())throw new IllegalStateException("Không hoàn tất được tác vụ in.");
        }
    }
}
