package com.teach.javafx.controller;

import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.util.CommonMethod;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;

import java.util.*;

public class CompetitionController {
    @FXML
    private TableView<Map<String, Object>> dataTableView;
    @FXML
    private TableColumn<Map, String> studentNumColumn;
    @FXML
    private TableColumn<Map, String> studentNameColumn;
    @FXML
    private TableColumn<Map, String> subjectColumn;
    @FXML
    private TableColumn<Map, String> resultColumn;
    @FXML
    private TableColumn<Map, String> competitionTimeColumn;

    @FXML
    private TextField studentNumField;
    @FXML
    private TextField studentNameField;
    @FXML
    private TextField subjectField;
    @FXML
    private TextField resultField;
    @FXML
    private TextField competitionTimeField;

    @FXML
    private TextField numNameTextField;
    @FXML
    private VBox showVBox;

    private Integer competitionId = null;
    private List<Map<String, Object>> competitionList = new ArrayList<>();
    private final ObservableList<Map<String, Object>> observableList = FXCollections.observableArrayList();

    @FXML
    private void onQueryButtonClick() {
        String numName = numNameTextField.getText();
        DataRequest req = new DataRequest();
        req.add("numName", numName);
        DataResponse res = HttpRequestUtil.request("/api/competition/getCompetitionList", req);
        if (res != null && res.getCode() == 0) {
            competitionList = (List<Map<String, Object>>) res.getData();
        }
        setTableViewData();
    }

    private void setTableViewData() {
        observableList.clear();
        for (int j = 0; j < competitionList.size(); j++) {
            observableList.addAll(FXCollections.observableArrayList(competitionList.get(j)));
        }
        dataTableView.setItems(observableList);
    }

    @FXML
    protected void onSaveButtonClick() {
        if (studentNumField.getText().isEmpty()) {
            MessageDialog.showDialog("错误(学号为空)");
            return;
        }
        if (studentNameField.getText().isEmpty()) {
            MessageDialog.showDialog("错误(学生姓名为空)");
            return;
        }

        Map<String, Object> form = new HashMap<>();
        form.put("studentNum", studentNumField.getText());
        form.put("studentName", studentNameField.getText());
        form.put("subject", subjectField.getText());
        form.put("result", resultField.getText());
        form.put("competitionTime", competitionTimeField.getText());

        DataRequest req = new DataRequest();
        req.add("form", form);
        DataResponse res = HttpRequestUtil.request("/api/competition/competitionSave", req);
        if (res.getCode() == 0) {
            MessageDialog.showDialog("提交成功");
            onQueryButtonClick();
        } else {
            MessageDialog.showDialog(res.getMsg());
        }
        showVBox.setVisible(false);
        showVBox.setManaged(false);
    }

    public void clearPanel() {
        competitionId = null;
        studentNumField.setText("");
        studentNameField.setText("");
        subjectField.setText("");
        resultField.setText("");
        competitionTimeField.setText("");
    }

    @FXML
    protected void onAddButtonClick() {
        showVBox.setVisible(true);
        showVBox.setManaged(true);
        clearPanel();
    }

    protected void changeCompetitionInfo() {
        Map<String, Object> form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            clearPanel();
            return;
        }
        competitionId = CommonMethod.getInteger(form, "competitionId");
        DataRequest req = new DataRequest();
        req.add("competitionId", competitionId);
        DataResponse res = HttpRequestUtil.request("/api/competition/getCompetitionInfo", req);
        if (res.getCode() != 0) {
            MessageDialog.showDialog(res.getMsg());
            return;
        }
        form = (Map<String, Object>) res.getData();
        studentNumField.setText(CommonMethod.getString(form, "studentNum"));
        studentNameField.setText(CommonMethod.getString(form, "studentName"));
        subjectField.setText(CommonMethod.getString(form, "subject"));
        resultField.setText(CommonMethod.getString(form, "result"));
        competitionTimeField.setText(CommonMethod.getString(form, "competitionTime"));
    }

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
        Integer competitionId = CommonMethod.getInteger(form, "competitionId");
        DataRequest req = new DataRequest();
        req.add("competitionId", competitionId);
        DataResponse res = HttpRequestUtil.request("/api/competition/competitionDelete", req);
        if (res.getCode() == 0) {
            onQueryButtonClick();
        } else {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    @FXML
    public void initialize() {
        // 设置表格列的值工厂
        studentNumColumn.setCellValueFactory(new MapValueFactory<>("studentNum"));
        studentNameColumn.setCellValueFactory(new MapValueFactory<>("studentName"));
        subjectColumn.setCellValueFactory(new MapValueFactory<>("subject"));
        resultColumn.setCellValueFactory(new MapValueFactory<>("result"));
        competitionTimeColumn.setCellValueFactory(new MapValueFactory<>("competitionTime"));

        // 设置表格行选择监听器
        dataTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                changeCompetitionInfo();
                showVBox.setVisible(true);
                showVBox.setManaged(true);
            }
        });
        
        // 添加场景变化监听器
        dataTableView.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                // 当场景变为可见时刷新数据
                newScene.windowProperty().addListener((prop, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        Platform.runLater(this::loadAllCompetitionData);
                    }
                });
            }
        });
        
        // 初始化时加载所有数据
        loadAllCompetitionData();
        
        // 初始时隐藏编辑面板
        showVBox.setVisible(false);
        showVBox.setManaged(false);
    }

    /**
     * 加载所有竞赛数据
     */
    private void loadAllCompetitionData() {
        // 清空搜索框
        numNameTextField.setText("");
        
        // 发送请求获取所有竞赛数据
        DataRequest req = new DataRequest();
        req.add("numName", ""); // 空字符串表示获取所有数据
        DataResponse res = HttpRequestUtil.request("/api/competition/getCompetitionList", req);
        if (res != null && res.getCode() == 0) {
            competitionList = (List<Map<String, Object>>) res.getData();
        }
        setTableViewData();
    }

    // 添加一个公共方法，可以从外部调用来刷新数据
    public void refreshData() {
        Platform.runLater(this::loadAllCompetitionData);
    }
}
