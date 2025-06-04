package com.teach.javafx.controller;

import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.request.OptionItem;
import com.teach.javafx.util.CommonMethod;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import com.teach.javafx.request.HttpRequestUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.FlowPane;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import javafx.scene.layout.VBox;

import java.util.*;

/**
 * CourseController 登录交互控制类 对应 course-panel.fxml
 *  @FXML  属性 对应fxml文件中的
 *  @FXML 方法 对应于fxml文件中的 on***Click的属性
 */
public class CourseController {
    @FXML
    private TableView<Map<String, Object>> dataTableView;
    @FXML
    private TableColumn<Map,String> numColumn;
    @FXML
    private TableColumn<Map,String> nameColumn;
    @FXML
    private TableColumn<Map,String> creditColumn;
    @FXML
    private TableColumn<Map,String> preCourseColumn;
    @FXML
    private TableColumn<Map,String> selectNumColumn;
    @FXML
    private TableColumn<Map,String> attendenceNumColumn;
    @FXML
    private TableColumn<Map,String> textbooksColumn;

    @FXML
    private TextField numField;
    @FXML
    private TextField nameField;
/*    @FXML
    private TextField preCourseField;
    @FXML
    private TextField creditField;
    @FXML
    private TextField selectNumField;
    @FXML
    private TextField attdenceNumField;
    @FXML
    private TextField textbooksField;
*/
    @FXML
    private TextField numNameTextField;
    @FXML
    private VBox showVBox;
    @FXML
    private ComboBox<OptionItem> courseComboBox;

    private Integer courseId=null;
    private List<OptionItem> courseName=new ArrayList<>();
    private List<Map<String,Object>> courseList = new ArrayList<>();  // 学生信息列表数据
    private final ObservableList<Map<String,Object>> observableList= FXCollections.observableArrayList();  // TableView渲染列表

    @FXML
    private void onQueryButtonClick(){
        String numName = numNameTextField.getText();
        courseComboBox.setValue(null);
        OptionItem op;
        op=courseComboBox.getSelectionModel().getSelectedItem();
        DataResponse res;
        DataRequest req =new DataRequest();
        req.add("numName", numName);
        res = HttpRequestUtil.request("/api/course/getCourseList",req); //从后台获取所有学生信息列表集合
        if(res != null && res.getCode()== 0) {
            courseList = (List<Map<String, Object>>) res.getData();
        }
        setTableViewData();
    }

    private void setTableViewData() {
        observableList.clear();
        for (int j = 0; j < courseList.size(); j++) {
            observableList.addAll(FXCollections.observableArrayList(courseList.get(j)));
        }
        dataTableView.setItems(observableList);
    }
    @FXML
    protected void onSaveButtonClick(){
        if(numField.getText().isEmpty()){
            MessageDialog.showDialog(("wrong(课序号为空)"));
            return;
        }
        if(nameField.getText().isEmpty()){
            MessageDialog.showDialog(("wrong(课程名为空)"));
            return;
        }
        Map<String,Object> form=new HashMap<>();
        OptionItem op=courseComboBox.getSelectionModel().getSelectedItem();
        if(op!=null){
            form.put("preCourseId", op.getValue());
        }
        form.put("num",numField.getText());
        form.put("name",nameField.getText());
        DataRequest req =new DataRequest();
        req.add("form",form);
        DataResponse res = HttpRequestUtil.request("/api/course/courseSave",req);
        if(res.getCode()== 0) {
            MessageDialog.showDialog(("提交成功"));
            onQueryButtonClick();
        }else{
            MessageDialog.showDialog((res.getMsg()));
        }
        showVBox.setVisible(false);
        showVBox.setManaged(false);
    }
    public void editSave(Map<String, Object> map){
        Map<String,Object> form=new HashMap<>();
        form.put("num",map.get("num"));
        form.put("credit",map.get("credit"));
        form.put("selectNum",map.get("selectNum"));
        form.put("attendenceNum",map.get("attendenceNum"));
        form.put("textbooks",map.get("textbooks"));
        DataRequest req =new DataRequest();
        req.add("form",form);
        DataResponse res = HttpRequestUtil.request("/api/course/courseEdit",req);
        if(res.getCode()== 0) {
            MessageDialog.showDialog(("修改成功"));
            onQueryButtonClick();
        }else{
            MessageDialog.showDialog((res.getMsg()));
        }
    }
    public void clearPanel() {
        courseId = null;
        numField.setText("");
        nameField.setText("");
    }
    @FXML
    protected void onAddButtonClick() {
        showVBox.setVisible(true);
        showVBox.setManaged(true);
        clearPanel();
    }
    protected void changeCourseInfo(){
        Map<String,Object> form = dataTableView.getSelectionModel().getSelectedItem();
        if(form == null){
            clearPanel();
            return;
        }
        courseId= CommonMethod.getInteger(form,"courseId");
        DataRequest req =new DataRequest();
        req.add("courseId",courseId);
        DataResponse res = HttpRequestUtil.request("/api/course/getCourseInfo",req);
        if(res.getCode()!= 0){
            MessageDialog.showDialog(res.getMsg());
            return;
        }
        form=(Map<String,Object>)res.getData();
        numField.setText(CommonMethod.getString(form,"num"));
        nameField.setText(CommonMethod.getString(form,"name"));
        /*creditField.setText(CommonMethod.getString(form,"credit"));
        preCourseField.setText(CommonMethod.getString(form,"preCourse"));
        selectNumField.setText(CommonMethod.getString(form,"selectNum"));
        attdenceNumField.setText(CommonMethod.getString(form,"attdenceNum"));
        textbooksField.setText(CommonMethod.getString(form,"textbooks"));
    */}
    @FXML
    protected void onDeleteButtonClick() {
        Map form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            MessageDialog.showDialog("没有选择，不能删除");
            return;
        }
        int ret = MessageDialog.choiceDialog("确认要删除吗?");
        if (ret != MessageDialog.CHOICE_YES) {
            return;
        }
        courseId = CommonMethod.getInteger(form, "courseId");
        DataRequest req = new DataRequest();
        req.add("courseId", courseId);
        DataResponse res = HttpRequestUtil.request("/api/course/courseDelete", req);
        if(res!= null) {
            if (res.getCode() == 0) {
                MessageDialog.showDialog("删除成功！");
                onQueryButtonClick();
            } else {
                MessageDialog.showDialog(res.getMsg());
            }
        }
        showVBox.setVisible(false);
        showVBox.setManaged(false);
    }
    @FXML
    public void initialize() {
        numColumn.setCellValueFactory(new MapValueFactory<>("num"));
        nameColumn.setCellValueFactory(new MapValueFactory<>("name"));
        creditColumn.setCellValueFactory(new MapValueFactory<>("credit"));
        creditColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        creditColumn.setOnEditCommit(event -> {
            Map<String, Object> map = event.getRowValue();
            map.put("credit", event.getNewValue());
            editSave(map);
        });
        preCourseColumn.setCellValueFactory(new MapValueFactory<>("preCourse"));
        selectNumColumn.setCellValueFactory(new MapValueFactory<>("selectNum"));
        selectNumColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        selectNumColumn.setOnEditCommit(event -> {
            Map<String, Object> map = event.getRowValue();
            map.put("selectNum", event.getNewValue());
            editSave(map);
        });
        attendenceNumColumn.setCellValueFactory(new MapValueFactory<>("attendenceNum"));
        attendenceNumColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        attendenceNumColumn.setOnEditCommit(event ->{
            Map<String, Object> map = event.getRowValue();
            map.put("attendenceNum", event.getNewValue());
            editSave(map);
        });
        textbooksColumn.setCellValueFactory(cellData -> {
            List<String> textbooks = (List<String>) cellData.getValue().get("textbooks");
            // 将 List 转为逗号分隔的字符串显示
            String textbooksStr = String.join(", ", textbooks);
            return new SimpleStringProperty(textbooksStr);
        });

        textbooksColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        textbooksColumn.setOnEditCommit(event -> {
            Map<String, Object> map = event.getRowValue();
            // 将用户输入的新字符串按逗号分割，存回 List
            String newValue = event.getNewValue();
            List<String> newTextbooks = Arrays.asList(newValue.split(",\\s*"));
            map.put("textbooks", newTextbooks);
            editSave(map);
        });
        dataTableView.setEditable(true);
        TableView.TableViewSelectionModel<Map<String,Object>> tsm = dataTableView.getSelectionModel();
        ObservableList<Integer> list = tsm.getSelectedIndices();
        DataRequest req =new DataRequest();
        courseName = HttpRequestUtil.requestOptionItemList("/api/course/getCourseItemOptionList",req);
        OptionItem item=new OptionItem(null,"0","请选择");
        courseComboBox.getItems().add(item);
        courseComboBox.getItems().addAll(courseName);
        onQueryButtonClick();
        showVBox.setVisible(false);
        showVBox.setManaged(false);
    }


}
