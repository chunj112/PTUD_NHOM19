package iuh.se.nhom19.view.common;

import javafx.scene.control.*;
import javafx.util.StringConverter;

/** Chỉ đổi cách trình bày; mã trạng thái trong Entity/SQL không thay đổi. */
public final class TrangThaiGiaoDien {
    private TrangThaiGiaoDien() {}
    public static String nhan(String ma) {
        if(ma==null)return "";
        return switch(ma){case "CHO_XAC_NHAN"->"Chờ xác nhận";case "DA_XAC_NHAN"->"Đã xác nhận";case "DA_DEN"->"Đã đến";case "DA_HUY"->"Đã hủy";case "KHONG_DEN"->"Không đến";case "CHO_KHAM"->"Chờ khám";case "DANG_KHAM"->"Đang khám";case "DA_KHAM"->"Đã khám";default->ma;};
    }
    private static String mau(String ma){if(ma==null)return "xam";return switch(ma){case "CHO_XAC_NHAN"->"vang";case "DA_XAC_NHAN","DANG_KHAM"->"xanh-duong";case "DA_DEN","DA_KHAM"->"xanh-la";case "DA_HUY"->"do";case "CHO_KHAM"->"cam";default->"xam";};}
    public static void gan(Label nhan,String ma){nhan.setText(nhan(ma));nhan.getStyleClass().removeIf(ten->ten.startsWith("nhan-tt-"));if(!nhan.getStyleClass().contains("nhan-trang-thai"))nhan.getStyleClass().add("nhan-trang-thai");nhan.getStyleClass().add("nhan-tt-"+mau(ma));}
    public static <T> void cot(TableColumn<T,String> cot){cot.setCellFactory(bang->new TableCell<>(){
        private final Label nhan=new Label();
        @Override protected void updateItem(String ma,boolean rong){super.updateItem(ma,rong);setText(null);if(rong||ma==null)setGraphic(null);else{gan(nhan,ma);setGraphic(nhan);}}
    });}
    public static void boLoc(ComboBox<String> boLoc){boLoc.setConverter(new StringConverter<>(){
        @Override public String toString(String ma){return nhan(ma);}
        @Override public String fromString(String chu){return chu;}
    });}
    public static <T> void cotChu(TableColumn<T,String> cot){cot.setCellFactory(bang->new TableCell<>(){
        {getStyleClass().add("chu-rut-gon");setTextOverrun(OverrunStyle.ELLIPSIS);}
        @Override protected void updateItem(String chu,boolean rong){super.updateItem(chu,rong);setText(rong?null:chu);setTooltip(rong||chu==null||chu.isBlank()?null:new Tooltip(chu));setWrapText(false);}
    });}
}
