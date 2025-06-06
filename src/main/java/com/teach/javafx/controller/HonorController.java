package com.teach.javafx.controller;

import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.OptionItem;
import com.teach.javafx.util.CommonMethod;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HonorController {
    @FXML
    private TableView<Map> dataTableView;
    @FXML
    private TableColumn<Map,String> studentNumColumn;
    @FXML
    private TableColumn<Map,String> studentNameColumn;
    @FXML
    private TableColumn<Map,String> classNameColumn;
    @FXML
    private TableColumn<Map,String> honorNameColumn;
   // @FXML
    //private TableColumn<Map,String> markColumn;
    @FXML
    private TableColumn<Map, Button> editColumn;

    private ArrayList<Map> honorList = new ArrayList<>();
    private ObservableList<Map> observableList= FXCollections.observableArrayList();

    @FXML
    private ComboBox<OptionItem> studentComboBox;


    private List<OptionItem> studentList;
    private HonorEditController honorEditController = null;
    private Stage stage = null;
    public List<OptionItem> getStudentList() {
        return studentList;
    }

    @FXML
    private void onQueryButtonClick(){
        Integer personId = 0;
        OptionItem op;
        op = studentComboBox.getSelectionModel().getSelectedItem();
        if(op != null)
            personId = Integer.parseInt(op.getValue());
        DataResponse res;
        DataRequest req =new DataRequest();
        req.add("personId",personId);
        res = HttpRequestUtil.request("/api/honor/getHonorList",req); //从后台获取所有学生信息列表集合
        if(res != null && res.getCode()== 0) {
            honorList = (ArrayList<Map>)res.getData();
        }
        setTableViewData();
    }
    private void setTableViewData() {
        observableList.clear();
        Map map;
        Button editButton;
        for (int j = 0; j < honorList.size(); j++) {
            map = honorList.get(j);
            editButton = new Button("编辑");
            editButton.setId("edit" + j);
            editButton.setOnAction(e -> {
                editItem(((Button) e.getSource()).getId());
            });
            map.put("edit", editButton);
            observableList.addAll(FXCollections.observableArrayList(map));
        }
        dataTableView.setItems(observableList);
    }
    public void editItem(String name){
        if(name == null)
            return;
        int j = Integer.parseInt(name.substring(4,name.length()));
        Map data = honorList.get(j);
        initDialog();
        honorEditController.showDialog(data);
        MainApplication.setCanClose(false);
        stage.showAndWait();

    }
    private void initDialog() {
        if(stage!= null)
            return;
        FXMLLoader fxmlLoader ;
        Scene scene = null;
        try {
            fxmlLoader = new FXMLLoader(MainApplication.class.getResource("honor-edit-panel.fxml"));
            scene = new Scene(fxmlLoader.load(), 260, 140);
            stage = new Stage();
            stage.initOwner(MainApplication.getMainStage());
            stage.initModality(Modality.NONE);
            stage.setAlwaysOnTop(true);
            stage.setScene(scene);
            stage.setTitle("成绩录入对话框！");
            stage.setOnCloseRequest(event ->{
                MainApplication.setCanClose(true);
            });
            honorEditController = (HonorEditController) fxmlLoader.getController();
            honorEditController.setHonorController(this);
            honorEditController.init();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void initialize() {
        studentNumColumn.setCellValueFactory(new MapValueFactory<>("studentNum"));  //设置列值工程属性
        studentNameColumn.setCellValueFactory(new MapValueFactory<>("studentName"));
       // markColumn.setCellValueFactory(new MapValueFactory<>("mark"));
        classNameColumn.setCellValueFactory(new MapValueFactory<>("className"));
        editColumn.setCellValueFactory(new MapValueFactory<>("edit"));
        honorNameColumn.setCellValueFactory(new MapValueFactory<>("honorName"));

        DataRequest req = new DataRequest();
        studentList=HttpRequestUtil.requestOptionItemList("/api/honor/getStudentItemOptionList",req);
        OptionItem item=new OptionItem(null,"0","请选择");
        studentComboBox.getItems().add(item);
        studentComboBox.getItems().addAll(studentList);
        dataTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        studentComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.getValue().equals("0")) { // 排除"请选择"选项
                  onQueryButtonClick();
            }
        });
        studentComboBox.setOnMouseClicked(e->{
            studentList=HttpRequestUtil.requestOptionItemList("/api/honor/getStudentItemOptionList",req);
            studentComboBox.getItems().clear();
            studentComboBox.getItems().add(item);
            studentComboBox.getItems().addAll(studentList);
        });
        onQueryButtonClick();
    }
    public void doClose(String cmd,Map<String,Object> data){
        MainApplication.setCanClose(true);
        stage.close();
        if(!"ok".equals(cmd))
            return;
        DataResponse res;
        Integer personId= CommonMethod.getInteger(data,"personId");
        if(personId==null){
            MessageDialog.showDialog("没有选中学生不能添加保存！");
            return;
        }
        DataRequest req = new DataRequest();
        req.add("personId",personId);
        req.add("honorId",CommonMethod.getInteger(data,"honorId"));
        //req.add("mark",CommonMethod.getInteger(data,"mark"));
        req.add("honorName",CommonMethod.getString(data,"honorName"));
        req.add("className",CommonMethod.getString(data,"className"));
        res=HttpRequestUtil.request("/api/honor/honorSave",req);
        if(res != null && res.getCode()== 0) {
            onQueryButtonClick();
        }
    }
    @FXML
    private void onAddButtonClick() {
        initDialog();
        honorEditController.showDialog(null);
        MainApplication.setCanClose(false);
        stage.showAndWait();
    }
    @FXML
    private void onEditButtonClick() {
//        dataTableView.getSelectionModel().getSelectedItems();
        Map data = dataTableView.getSelectionModel().getSelectedItem();
        if(data == null) {
            MessageDialog.showDialog("没有选中，不能修改！");
            return;
        }
        initDialog();
        honorEditController.showDialog(data);
        MainApplication.setCanClose(false);
        stage.showAndWait();
    }

    @FXML
    private void onDeleteButtonClick() {
        Map<String,Object> form = dataTableView.getSelectionModel().getSelectedItem();
        if(form == null) {
            MessageDialog.showDialog("没有选择，不能删除");
            return;
        }
        int ret = MessageDialog.choiceDialog("确认要删除吗?");
        if(ret != MessageDialog.CHOICE_YES) {
            return;
        }
        Integer honorId = CommonMethod.getInteger(form,"honorId");
        DataRequest req = new DataRequest();
        req.add("honorId", honorId);
        DataResponse res = HttpRequestUtil.request("/api/honor/honorDelete",req);
        if(res.getCode() == 0) {
            onQueryButtonClick();
        }
        else {
            MessageDialog.showDialog(res.getMsg());
        }
    }
}
