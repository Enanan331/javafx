package com.teach.javafx.controller;

import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.util.CommonMethod;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeeController {


    @FXML
    private TableView<Map<String, Object>> dataTableView;  //学生信息表
    @FXML
    private TableColumn<Map, String> numColumn;   //学生信息表 编号列
    @FXML
    private TableColumn<Map, String> nameColumn; //学生信息表 名称列
    @FXML
    private TableColumn<Map, String> feeIdColumn; //学生信息表 证件号码列
    @FXML
    private TableColumn<Map, String> dayColumn; //学生信息表 消费日期列
    @FXML
    private TableColumn<Map, String> moneyColumn;//学生信息表 消费金额列
    @FXML
    private TextField numField; //学生信息  学号输入域
    @FXML
    private TextField numQ1;
    @FXML
    private TextField numQ2;
    @FXML
    private TextField numQ3;
    @FXML
    private TextField dayQ1;
    @FXML
    private TextField dayQ3;
    @FXML
    private TextField nameField;  //学生信息  名称输入域
    @FXML
    private TextField cardField; //学生信息  证件号码输入域
    @FXML
    private TextField  moneyField; //学生信息  消费金额输入域
    @FXML
    private DatePicker cardDatePicker;//学生信息  消费日期输入域

    @FXML
    private VBox showVBox;

    @FXML private StackPane contentContainer;
    @FXML private VBox vbox1;
    @FXML private VBox vbox11;
    @FXML private VBox vbox12;
    @FXML private VBox vbox13;
    @FXML private VBox vbox14;

    private Integer personId = null;  //当前编辑修改的学生的主键
    private List<Map<String,Object>> feeList = new ArrayList();  // 学生信息列表数据
    private final ObservableList<Map<String,Object>> observableList = FXCollections.observableArrayList();  // TableView渲染列表
    private void setTableViewData() {
        observableList.clear();
        for (int j = 0; j < feeList.size(); j++) {
            observableList.addAll(FXCollections.observableArrayList(feeList.get(j)));
        }
        dataTableView.setItems(observableList);
    }


    @FXML
    public ComboBox<String> myComboBox;

    @FXML
    public void initialize() {
        // 初始化ComboBox
        myComboBox.getItems().addAll("查单人单天", "查单人所有", "查某人总和", "查最新数据");
        myComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        switch(newValue) {
                            case "查单人单天": showVbox11();break;
                            case "查单人所有": showVbox12();break;
                            case "查某人总和": showVbox13();break;
                            case "查最新数据": showVbox14();break;
                        }
                    }
                });

        // 获取数据
        DataResponse res;
        DataRequest req = new DataRequest();
        req.add("personId", -1);
        res = HttpRequestUtil.request("/api/fee/getFeeList", req);

        if (res != null && res.getCode() == 0) {
            feeList = (List<Map<String, Object>>) res.getData();

            numColumn.setCellValueFactory(cellData -> {
                Map<String, Object> row = cellData.getValue();
                Object personId = row.get("personId");
                if (personId instanceof Double) {
                    return new SimpleStringProperty(String.valueOf(((Double) personId).intValue()));
                } else {
                    return new SimpleStringProperty(personId != null ? personId.toString() : "");
                }
            });
            numColumn.setCellValueFactory(cellData -> {
                Map<String, Object> row = cellData.getValue();
                Object feeId = row.get("feeId");
                if (feeId instanceof Double) {
                    return new SimpleStringProperty(String.valueOf(((Double) feeId).intValue()));
                } else {
                    return new SimpleStringProperty(feeId != null ? feeId.toString() : "");
                }
            });

            // 其他列保持不变
            nameColumn.setCellValueFactory(new MapValueFactory<>("name"));
            feeIdColumn.setCellValueFactory(new MapValueFactory<>("feeId"));
            dayColumn.setCellValueFactory(new MapValueFactory<>("day"));
            moneyColumn.setCellValueFactory(new MapValueFactory<>("money"));

            // 设置数据到TableView
            ObservableList<Map<String, Object>> items = FXCollections.observableArrayList(feeList);
            dataTableView.setItems(items);
        }

        // 其他初始化代码...
        showVBox.setVisible(false);
        showVBox.setManaged(false);
    }


    @FXML
    void onAddButtonClick(ActionEvent event) {
        showVBox.setVisible(true);
        showVBox.setManaged(true);
        vbox1.setVisible(true);
        vbox1.toFront();
        vbox11.setVisible(false);
        vbox12.setVisible(false);
        vbox13.setVisible(false);
        vbox14.setVisible(false);
    }
    @FXML
    void onShowAllButtonClick(ActionEvent event) {
        DataResponse res;
        DataRequest req = new DataRequest();
        req.add("personId", -1);
        res = HttpRequestUtil.request("/api/fee/getFeeList", req);
        if (res != null && res.getCode() == 0) {
            feeList = (ArrayList<Map<String,Object>>) res.getData();
            setTableViewData();
        }
    }
    @FXML
    void onQ1ButtonClick(ActionEvent event) {
        showVbox11();
        Integer num = Integer.valueOf(numQ1.getText());
        String day = dayQ1.getText();
        DataRequest req = new DataRequest();
        req.add("personId", num);
        req.add("day", day);
        DataResponse res = HttpRequestUtil.request("/api/fee/getFee", req);
        if (res != null && res.getCode() == 0) {
            feeList = (ArrayList<Map<String,Object>>) res.getData();
            setTableViewData();
        }
        showVBox.setVisible(false);
        showVBox.setManaged(false);
    }
    @FXML
    void onQ2ButtonClick(ActionEvent event) {
        showVbox12();
        Integer num = Integer.valueOf(numQ2.getText());
        DataRequest req = new DataRequest();
        req.add("personId", num);
        DataResponse res = HttpRequestUtil.request("/api/fee/getFee", req);
        if (res != null && res.getCode() == 0) {
            feeList = (ArrayList<Map<String,Object>>) res.getData();
            setTableViewData();
        }
        showVBox.setVisible(false);
        showVBox.setManaged(false);
    }
    @FXML
    void onQ3ButtonClick(ActionEvent event) {
        showVbox13();
        Integer num = Integer.valueOf(numQ3.getText());
        DataRequest req = new DataRequest();
        req.add("personId", num);
        req.add("day", dayQ3.getText());
        DataResponse res = HttpRequestUtil.request("/api/fee/getFee", req);
        if (res != null && res.getCode() == 0) {
            feeList = (ArrayList<Map<String,Object>>) res.getData();
            setTableViewData();
        }
        showVBox.setVisible(false);
        showVBox.setManaged(false);
    }
    @FXML
    void onQ4ButtonClick(ActionEvent event) {
        showVbox14();
        DataRequest req = new DataRequest();
        DataResponse res = HttpRequestUtil.request("/api/fee/getLatestFeeRecord", req);
        if(res != null && res.getCode()== 0) {
            feeList = (List<Map<String, Object>>) res.getData();
        }
        setTableViewData();
    }

    @FXML
    void onDeleteButtonClick(ActionEvent event) {
        Map form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            MessageDialog.showDialog("没有选择，不能删除");
            return;
        }
        int ret = MessageDialog.choiceDialog("确认要删除吗?");
        if (ret != MessageDialog.CHOICE_YES) {
            return;
        }
        personId = CommonMethod.getInteger(form, "personId");
        DataRequest req = new DataRequest();
        req.add("personId", personId);
        DataResponse res = HttpRequestUtil.request("/api/fee/deleteFee", req);
        if(res!= null) {
            if (res.getCode() == 0) {
                MessageDialog.showDialog("删除成功！");
//                onQueryButtonClick();
            } else {
                MessageDialog.showDialog(res.getMsg());
            }
        }
        showVBox.setVisible(false);
        showVBox.setManaged(false);
        dataTableView.getSelectionModel().clearSelection();
    }


    @FXML
    protected void onSaveButtonClick() {
        if (numField.getText().isEmpty()) {
            MessageDialog.showDialog("学工号为空，不能修改");
            return;
        }
        Map<String,Object> form = new HashMap<>();
        form.put("num", numField.getText());
        form.put("name", nameField.getText());
        form.put("card", cardField.getText());
        DataRequest req = new DataRequest();
        req.add("personId", personId);
        req.add("form", form);
        DataResponse res = HttpRequestUtil.request("/api/fee/addFee", req);
        if (res.getCode() == 0) {
            personId = CommonMethod.getIntegerFromObject(res.getData());
            MessageDialog.showDialog("提交成功！");
        } else {
            MessageDialog.showDialog(res.getMsg());
        }
        showVBox.setVisible(false);
        showVBox.setManaged(false);
        dataTableView.getSelectionModel().clearSelection();
    }
    @FXML
    private void showVbox11() {
        showVBox.setVisible(true);
        showVBox.setManaged(true);
        vbox1.setVisible(false);
        vbox11.setVisible(true);
        vbox12.setVisible(false);
        vbox13.setVisible(false);
        vbox14.setVisible(false);
        vbox11.toFront();
    }

    @FXML
    private void showVbox12() {
        showVBox.setVisible(true);
        showVBox.setManaged(true);
        vbox1.setVisible(false);
        vbox11.setVisible(false);
        vbox12.setVisible(true);
        vbox13.setVisible(false);
        vbox14.setVisible(false);
        vbox12.toFront();
    }

    @FXML
    private void showVbox13() {
        showVBox.setVisible(true);
        showVBox.setManaged(true);
        vbox1.setVisible(false);
        vbox11.setVisible(false);
        vbox12.setVisible(false);
        vbox13.setVisible(true);
        vbox14.setVisible(false);
        vbox13.toFront();
    }
    @FXML
    private void showVbox14() {
        showVBox.setVisible(true);
        showVBox.setManaged(true);
        vbox1.setVisible(false);
        vbox11.setVisible(false);
        vbox12.setVisible(false);
        vbox13.setVisible(false);
        vbox14.setVisible(true);
        vbox14.toFront();
    }
}
