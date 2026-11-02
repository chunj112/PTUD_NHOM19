package vn.edu.nhom19.nhakhoa.view.common;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.StringConverter;

/** Truy cập các control FXML có sẵn, kể cả nội dung Tab chưa được hiển thị.
 * Không dùng reflection; không thay đổi bố cục hoặc tạo lại AppShell.
 */
public final class ViewAccess {
    public final Parent root;
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final Map<String,Integer> pages = new HashMap<>();

    public ViewAccess(Parent root) { this.root = root; }
    public static ViewAccess load(String path) {
        try {
            var url = ViewAccess.class.getResource("/fxml/" + path);
            if (url == null) throw new IllegalStateException("Thiếu FXML: " + path);
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            rememberNamespace(root, loader);
            return new ViewAccess(root);
        } catch (IOException ex) { throw new IllegalStateException("Không thể tải " + path, ex); }
    }
    public Node find(String id) { return findIn(root, id); }
    public static void rememberNamespace(Parent root, FXMLLoader loader) {
        // fx:id của fx:include nằm trong namespace cha, khác Node.id của root file con.
        root.getProperties().put("dentaflow.fxmlNamespace", new HashMap<>(loader.getNamespace()));
    }
    private Node findIn(Node node, String id) {
        Object namespace = node.getProperties().get("dentaflow.fxmlNamespace");
        if (namespace instanceof Map<?,?> names && names.get(id) instanceof Node alias) return alias;
        if (id.equals(node.getId())) return node;
        if (node instanceof TabPane tabs) for (Tab tab : tabs.getTabs()) {
            if (tab.getContent() != null) {
                Node result = findIn(tab.getContent(), id); if (result != null) return result;
            }
        }
        if (node instanceof ScrollPane scroll && scroll.getContent() != null) {
            Node result = findIn(scroll.getContent(), id); if (result != null) return result;
        }
        if (node instanceof Parent parent) for (Node child : parent.getChildrenUnmodifiable()) {
            Node result = findIn(child, id); if (result != null) return result;
        }
        return null;
    }
    public ViewAccess sub(String id) {
        Node node = find(id);
        if (!(node instanceof Parent parent)) throw new IllegalStateException("Thiếu vùng FXML: " + id);
        return new ViewAccess(parent);
    }
    public Button button(String id) { return (Button) find(id); }
    public DatePicker date(String id) { return (DatePicker) find(id); }
    @SuppressWarnings("unchecked")
    public ComboBox<Object> combo(String id) { return (ComboBox<Object>) find(id); }
    @SuppressWarnings("unchecked")
    public TableView<Map<String,Object>> table(String id) { return (TableView<Map<String,Object>>) find(id); }
    public String text(String id) {
        Node n = find(id);
        if (n instanceof TextInputControl input) return input.getText().trim();
        if (n instanceof Label label) return label.getText();
        if (n instanceof ComboBox<?> combo) return combo.getValue() == null ? "" : combo.getValue().toString();
        return "";
    }
    public void text(String id, Object value) {
        Node n = find(id); String s = value == null ? "" : value.toString();
        if (n instanceof Labeled labeled) labeled.setText(s);
        else if (n instanceof TextInputControl input) input.setText(s);
    }
    public void show(String id, boolean visible) {
        Node node = find(id); if (node != null) { node.setVisible(visible); node.setManaged(visible); }
    }
    public void disable(String id, boolean value) { Node n=find(id); if(n!=null)n.setDisable(value); }
    public void choices(String id, List<?> values, Object selected) {
        ComboBox<Object> combo = combo(id); if(combo==null)return;
        combo.setItems(FXCollections.observableArrayList(values));
        combo.setValue(selected);
    }
    public void date(String id, LocalDate value) {
        DatePicker picker = date(id); if(picker==null)return;
        picker.setConverter(new StringConverter<LocalDate>() {
            @Override public String toString(LocalDate date) { return date == null ? "" : DATE.format(date); }
            @Override public LocalDate fromString(String text) { return text == null || text.isBlank() ? null : LocalDate.parse(text,DATE); }
        });
        picker.setValue(value);
    }
    public void on(String id, ControllerContext context, Runnable action) {
        Button button = button(id); if(button==null)return;
        button.setOnAction(event -> {
            if(!context.active() || root.isDisabled())return;
            try { action.run(); } catch(RuntimeException ex) { error(ex.getMessage()); }
        });
    }
    public void changed(String id, Runnable action) {
        Node node = find(id);
        if(node instanceof TextInputControl text)text.textProperty().addListener((o,a,b)->action.run());
        else if(node instanceof ComboBox<?> combo)combo.valueProperty().addListener((o,a,b)->action.run());
        else if(node instanceof DatePicker picker)picker.valueProperty().addListener((o,a,b)->action.run());
    }
    public static Map<String,Object> row(Object model,Object... values) {
        Map<String,Object> row = new HashMap<>();row.put("_model",model);
        for(int i=0;i<values.length;i++)row.put("v"+i,values[i]);return row;
    }
    public Object selected(String tableId) {
        Map<String,Object> row=table(tableId).getSelectionModel().getSelectedItem();
        return row==null?null:row.get("_model");
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    public void rows(String tableId,List<Map<String,Object>> rows) {
        TableView table=table(tableId);if(table==null)return;
        for(int i=0;i<table.getColumns().size();i++){
            final String key="v"+i;TableColumn<Map<String,Object>,Object> column=(TableColumn)table.getColumns().get(i);
            column.setCellValueFactory(cell->new ReadOnlyObjectWrapper<>(cell.getValue().get(key)));
        }
        table.setItems(FXCollections.observableArrayList(rows));
    }
    public void paged(String tableId,List<Map<String,Object>> all,String count,String prev,String next,String page) {
        pages.put(tableId,0); renderPage(tableId,all,count,prev,next,page);
    }
    private void renderPage(String id,List<Map<String,Object>> all,String count,String prev,String next,String page){
        int offset=pages.getOrDefault(id,0),size=8;
        if(offset>=all.size())offset=Math.max(0,(all.size()-1)/size*size);
        final int current=offset;
        rows(id,all.subList(current,Math.min(current+size,all.size())));
        text(count,"Hiển thị "+(all.isEmpty()?0:current+1)+"–"+Math.min(current+size,all.size())+" / "+all.size()+" bản ghi");
        text(page,current/size+1);
        if(button(prev)!=null){button(prev).setDisable(current==0);button(prev).setOnAction(e->{pages.put(id,current-size);renderPage(id,all,count,prev,next,page);});}
        if(button(next)!=null){button(next).setDisable(current+size>=all.size());button(next).setOnAction(e->{pages.put(id,current+size);renderPage(id,all,count,prev,next,page);});}
    }
    public void error(String message) {
        Alert alert=new Alert(Alert.AlertType.ERROR,message==null?"Thao tác không thực hiện được.":message,ButtonType.OK);
        if(root.getScene()!=null)alert.initOwner(root.getScene().getWindow());
        alert.setTitle("DentaFlow · DEMO");alert.setHeaderText("Không thể thực hiện");alert.showAndWait();
    }
    public boolean confirm(String message) {
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION,message,ButtonType.OK,ButtonType.CANCEL);
        if(root.getScene()!=null)alert.initOwner(root.getScene().getWindow());
        alert.setHeaderText("Xác nhận thao tác DEMO");return alert.showAndWait().orElse(ButtonType.CANCEL)==ButtonType.OK;
    }
    public static BigDecimal money(String value) {
        String raw=value.replace("₫", "").replace("đ", "").replace(" ", "").trim();
        if (raw.matches("-?\\d+(\\.\\d{1,2})?")) return new BigDecimal(raw);
        if (raw.matches("-?\\d+(,\\d{1,2})?") || raw.matches("-?\\d{1,3}(\\.\\d{3})+(,\\d{1,2})?")) {
            return new BigDecimal(raw.replace(".", "").replace(",", "."));
        }
        throw new IllegalArgumentException("Số tiền không hợp lệ; ví dụ 150.000 hoặc 150000,50.");
    }
    public static String amount(BigDecimal value){
        java.text.NumberFormat format=java.text.NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        format.setMinimumFractionDigits(value.stripTrailingZeros().scale()>0?2:0);format.setMaximumFractionDigits(2);
        return format.format(value)+" ₫";
    }
    public void export(String fileName,List<String> headings,List<List<String>> rows) {
        FileChooser chooser=new FileChooser();chooser.setInitialFileName(fileName);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV UTF-8","*.csv"));
        File file=chooser.showSaveDialog(root.getScene()==null?null:root.getScene().getWindow());if(file==null)return;
        StringBuilder content=new StringBuilder("\uFEFF");
        content.append("\"DEMO ONLY – dữ liệu trong bộ nhớ, không phải SQL Server\"\r\n");
        appendCsv(content,headings);for(List<String> row:rows)appendCsv(content,row);
        try{Files.writeString(file.toPath(),content,StandardCharsets.UTF_8);}
        catch(IOException ex){throw new IllegalStateException("Không thể xuất file CSV: "+ex.getMessage(),ex);}
    }
    private static void appendCsv(StringBuilder out,List<String> values){
        for(int i=0;i<values.size();i++){
            if(i>0)out.append(',');String s=values.get(i)==null?"":values.get(i);
            if(!s.isEmpty()&&"=+-@".indexOf(s.charAt(0))>=0)s="'"+s;
            out.append('"').append(s.replace("\"","\"\"")).append('"');
        }
        out.append("\r\n");
    }
}
