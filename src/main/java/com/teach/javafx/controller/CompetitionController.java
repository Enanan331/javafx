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
        showVBox.setVisible(false);
        showVBox.setManaged(false);
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
        // 验证表单
        if (studentNumField.getText().isEmpty()) {
            MessageDialog.showDialog("学号不能为空");
            return;
        }
        if (studentNameField.getText().isEmpty()) {
            MessageDialog.showDialog("学生姓名不能为空");
            return;
        }
        if (subjectField.getText().isEmpty()) {
            MessageDialog.showDialog("竞赛科目不能为空");
            return;
        }
        
        // 准备表单数据
        Map<String, Object> form = new HashMap<>();
        form.put("studentNum", studentNumField.getText());
        form.put("studentName", studentNameField.getText());
        form.put("subject", subjectField.getText());
        form.put("result", resultField.getText());
        form.put("competitionTime", competitionTimeField.getText());
        
        try {
            if (competitionId == null) {
                // 新增操作
                System.out.println("执行新增操作");
                DataRequest req = new DataRequest();
                req.add("form", form);
                DataResponse res = HttpRequestUtil.request("/api/competition/competitionSave", req);
                
                handleResponse(res);
            } else {
                // 修改操作 - 先尝试保存新数据，成功后再删除旧数据
                System.out.println("执行修改操作，competitionId: " + competitionId);
                
                // 1. 先尝试保存新数据（但不删除旧数据）
                DataRequest saveReq = new DataRequest();
                saveReq.add("form", form);
                DataResponse saveRes = HttpRequestUtil.request("/api/competition/competitionSave", saveReq);
                
                // 2. 如果保存成功，获取新记录的ID并删除旧记录
                if (saveRes != null && saveRes.getCode() == 0) {
                    // 获取新记录的ID
                    Integer newCompetitionId = null;
                    
                    // 打印响应数据，帮助调试
                    System.out.println("保存成功，响应数据类型: " + 
                        (saveRes.getData() == null ? "null" : saveRes.getData().getClass().getName()));
                    System.out.println("保存成功，响应数据内容: " + saveRes.getData());
                    
                    if (saveRes.getData() instanceof Integer) {
                        newCompetitionId = (Integer) saveRes.getData();
                        System.out.println("从Integer类型获取ID: " + newCompetitionId);
                    } else if (saveRes.getData() instanceof String) {
                        try {
                            newCompetitionId = Integer.parseInt((String) saveRes.getData());
                            System.out.println("从String类型获取ID: " + newCompetitionId);
                        } catch (NumberFormatException e) {
                            System.out.println("无法将String转换为Integer: " + saveRes.getData());
                        }
                    } else if (saveRes.getData() instanceof Map) {
                        Map<String, Object> data = (Map<String, Object>) saveRes.getData();
                        System.out.println("Map内容: " + data);
                        
                        // 尝试多种可能的键名
                        String[] possibleKeys = {"competitionId", "id", "competition_id", "competitionid"};
                        for (String key : possibleKeys) {
                            if (data.containsKey(key)) {
                                Object value = data.get(key);
                                System.out.println("找到键 '" + key + "' 值为: " + value);
                                
                                if (value instanceof Integer) {
                                    newCompetitionId = (Integer) value;
                                    break;
                                } else if (value instanceof String) {
                                    try {
                                        newCompetitionId = Integer.parseInt((String) value);
                                        break;
                                    } catch (NumberFormatException e) {
                                        System.out.println("无法将String值转换为Integer: " + value);
                                    }
                                }
                            }
                        }
                    } else if (saveRes.getData() instanceof List) {
                        List<?> dataList = (List<?>) saveRes.getData();
                        System.out.println("List内容: " + dataList);
                        if (!dataList.isEmpty() && dataList.get(0) instanceof Map) {
                            Map<String, Object> firstItem = (Map<String, Object>) dataList.get(0);
                            newCompetitionId = CommonMethod.getInteger(firstItem, "competitionId");
                            System.out.println("从List的第一个Map中获取ID: " + newCompetitionId);
                        }
                    }
                    
                    // 如果仍然无法获取ID，尝试通过查询最新记录获取
                    if (newCompetitionId == null) {
                        System.out.println("无法从响应中获取ID，尝试查询最新记录");
                        
                        // 重新加载数据
                        loadAllCompetitionData();
                        
                        // 查找最新添加的记录（假设ID是自增的，最大的ID就是最新添加的）
                        int maxId = -1;
                        for (Map<String, Object> item : competitionList) {
                            int itemId = CommonMethod.getInteger(item, "competitionId");
                            if (itemId > maxId) {
                                maxId = itemId;
                            }
                        }
                        
                        if (maxId > 0) {
                            newCompetitionId = maxId;
                            System.out.println("通过查询找到最新记录ID: " + newCompetitionId);
                        }
                    }
                    
                    // 删除旧记录
                    if (newCompetitionId != null) {
                        DataRequest deleteReq = new DataRequest();
                        deleteReq.add("competitionId", competitionId);
                        DataResponse deleteRes = HttpRequestUtil.request("/api/competition/competitionDelete", deleteReq);
                        
                        if (deleteRes == null || deleteRes.getCode() != 0) {
                            System.out.println("删除原记录失败，但新记录已创建，ID: " + newCompetitionId);
                            MessageDialog.showDialog("警告：原记录删除失败，但修改已保存为新记录");
                        } else {
                            System.out.println("修改成功：创建了新记录(ID: " + newCompetitionId + ")并删除了旧记录(ID: " + competitionId + ")");
                            MessageDialog.showDialog("修改成功");
                        }
                    } else {
                        System.out.println("无法获取新记录ID，但新记录已创建");
                        MessageDialog.showDialog("警告：无法获取新记录ID，但修改已保存");
                    }
                    
                    // 重新加载数据
                    loadAllCompetitionData();
                    
                    // 隐藏编辑面板并清空表单
                    showVBox.setVisible(false);
                    showVBox.setManaged(false);
                    clearPanel();
                } else {
                    // 保存失败，显示错误信息
                    if (saveRes != null) {
                        MessageDialog.showDialog("修改失败: " + saveRes.getMsg());
                    } else {
                        MessageDialog.showDialog("修改失败: 网络请求失败");
                    }
                }
            }
        } catch (Exception e) {
            // 捕获并显示任何异常
            System.out.println("发生异常: " + e.getMessage());
            e.printStackTrace();
            MessageDialog.showDialog("发生错误: " + e.getMessage());
        }
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
            showVBox.setVisible(false);
            showVBox.setManaged(false);
            clearPanel();
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

    // 处理响应的辅助方法
    private void handleResponse(DataResponse res) {
        // 检查响应是否为null
        if (res == null) {
            System.out.println("请求返回null，请求失败");
            MessageDialog.showDialog("网络请求失败，请检查网络连接或服务器状态");
            return;
        }
        
        // 处理响应
        System.out.println("请求成功，响应码: " + res.getCode() + ", 消息: " + res.getMsg());
        if (res.getCode() == 0) {
            MessageDialog.showDialog("保存成功");
            // 完全重新加载数据
            loadAllCompetitionData();
            
            // 隐藏编辑面板并清空表单
            showVBox.setVisible(false);
            showVBox.setManaged(false);
            clearPanel();
        } else {
            MessageDialog.showDialog(res.getMsg());
        }
    }
}
