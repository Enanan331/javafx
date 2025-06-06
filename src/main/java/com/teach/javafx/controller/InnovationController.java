package com.teach.javafx.controller;

import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.OptionItem;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.controller.base.ToolController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import java.util.Optional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;

public class InnovationController extends ToolController {
    @FXML
    private TableView<Map<String, Object>> dataTableView;
    @FXML
    private TableColumn<Map, String> studentNumColumn;
    @FXML
    private TableColumn<Map, String> studentNameColumn;
    @FXML
    private TableColumn<Map, String> achievementColumn;
    @FXML
    private TableColumn<Map, String> advisorNameColumn;

    @FXML
    private TextField studentNumField;
    @FXML
    private TextField studentNameField;
    @FXML
    private TextField achievementField;
    @FXML
    private ComboBox<OptionItem> teacherComboBox;

    @FXML
    private TextField numNameTextField;
    @FXML
    private VBox showVBox;

    private Integer innovationId = null;
    private List<Map<String, Object>> innovationList = new ArrayList<>();
    private final ObservableList<Map<String, Object>> observableList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        studentNumColumn.setCellValueFactory(new MapValueFactory<>("studentNum"));
        studentNameColumn.setCellValueFactory(new MapValueFactory<>("studentName"));
        achievementColumn.setCellValueFactory(new MapValueFactory<>("achievement"));
        advisorNameColumn.setCellValueFactory(new MapValueFactory<>("advisorName"));
        
        dataTableView.setItems(observableList);
        
        // 先配置教师下拉框显示格式
        configureTeacherComboBox();
        
        // 然后加载教师选项
        loadTeacherOptions();
        
        // 设置表格选择监听器
        dataTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showVBox.setVisible(true);
                showVBox.setManaged(true);
                
                // 修复类型转换问题
                Object innovationIdObj = newSelection.get("innovationId");
                if (innovationIdObj instanceof Integer) {
                    innovationId = (Integer) innovationIdObj;
                } else if (innovationIdObj instanceof Double) {
                    innovationId = ((Double) innovationIdObj).intValue();
                } else if (innovationIdObj != null) {
                    try {
                        innovationId = Integer.parseInt(innovationIdObj.toString());
                    } catch (NumberFormatException e) {
                        System.err.println("无法将 " + innovationIdObj + " 转换为整数: " + e.getMessage());
                        return;
                    }
                }
                
                DataRequest req = new DataRequest();
                req.add("innovationId", innovationId);
                DataResponse res = HttpRequestUtil.request("/api/innovation/getInnovationInfo", req);
                
                if (res != null && res.getCode() == 0) {
                    Map<String, Object> data = (Map<String, Object>) res.getData();
                    studentNumField.setText((String) data.get("studentNum"));
                    studentNameField.setText((String) data.get("studentName"));
                    achievementField.setText((String) data.get("achievement"));
                    
                    // 修复类型转换问题
                    Object teacherIdObj = data.get("teacherId");
                    Integer teacherId = null;
                    if (teacherIdObj instanceof Integer) {
                        teacherId = (Integer) teacherIdObj;
                    } else if (teacherIdObj instanceof Double) {
                        teacherId = ((Double) teacherIdObj).intValue();
                    } else if (teacherIdObj != null) {
                        try {
                            teacherId = Integer.parseInt(teacherIdObj.toString());
                        } catch (NumberFormatException e) {
                            System.err.println("无法将 " + teacherIdObj + " 转换为整数: " + e.getMessage());
                        }
                    }
                    
                    if (teacherId != null) {
                        boolean found = false;
                        System.out.println("尝试选择教师ID: " + teacherId);
                        
                        for (OptionItem item : teacherComboBox.getItems()) {
                            try {
                                int itemValue = Integer.parseInt(item.getValue());
                                System.out.println("比较教师: Value=" + itemValue + ", Label=" + item.getLabel());
                                
                                if (itemValue == teacherId) {
                                    System.out.println("找到匹配的教师: " + item.getLabel());
                                    teacherComboBox.getSelectionModel().select(item);
                                    found = true;
                                    break;
                                }
                            } catch (NumberFormatException e) {
                                System.err.println("无法将 " + item.getValue() + " 转换为整数: " + e.getMessage());
                            }
                        }
                        
                        if (!found) {
                            System.err.println("未找到ID为 " + teacherId + " 的教师");
                            teacherComboBox.getSelectionModel().clearSelection();
                        }
                    } else {
                        System.out.println("教师ID为空，清除选择");
                        teacherComboBox.getSelectionModel().clearSelection();
                    }
                }
            }
        });
        
        // 添加场景变化监听器
        dataTableView.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                // 当场景变为可见时刷新数据
                newScene.windowProperty().addListener((prop, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        Platform.runLater(this::loadAllInnovationData);
                    }
                });
            }
        });
        
        // 初始化时加载所有数据
        loadAllInnovationData();
        
        // 初始时隐藏编辑面板
        showVBox.setVisible(false);
        showVBox.setManaged(false);
    }
    
    private void loadTeacherOptions() {
        try {
            DataRequest req = new DataRequest();
            List<OptionItem> teacherList = HttpRequestUtil.requestOptionItemList("/api/innovation/getTeacherOptionList", req);
            
            if (teacherList != null && !teacherList.isEmpty()) {
                teacherComboBox.getItems().clear();
                
                // 打印教师列表，用于调试
                System.out.println("加载的教师列表:");
                for (OptionItem item : teacherList) {
                    System.out.println("ID: " + item.getId() + ", Value: " + item.getValue() + ", Label: " + item.getLabel() + ", Title: " + item.getTitle() + ", ToString: " + item.toString());
                }
                
                teacherComboBox.getItems().addAll(teacherList);
            } else {
                System.out.println("未加载到教师列表或列表为空");
                // 如果API调用失败，添加一个默认选项
                teacherComboBox.getItems().clear();
                teacherComboBox.getItems().add(new OptionItem(0, "0", "无"));
                teacherComboBox.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.showDialog("加载教师列表失败: " + e.getMessage());
            
            // 添加一个默认选项
            teacherComboBox.getItems().clear();
            teacherComboBox.getItems().add(new OptionItem(0, "0", "无"));
            teacherComboBox.getSelectionModel().selectFirst();
        }
    }

    /**
     * 加载所有创新成果数据
     */
    public void loadAllInnovationData() {
        // 清空搜索框
        numNameTextField.setText("");
        
        // 发送请求获取所有创新成果数据
        DataRequest req = new DataRequest();
        req.add("numName", ""); // 空字符串表示获取所有数据
        DataResponse res = HttpRequestUtil.request("/api/innovation/getInnovationList", req);
        if (res != null && res.getCode() == 0) {
            innovationList = (List<Map<String, Object>>) res.getData();
            
            // 直接更新表格数据
            observableList.clear();
            observableList.addAll(innovationList);
            dataTableView.setItems(observableList);
        }
    }

    @FXML
    protected void onQueryButtonClick() {
        DataRequest req = new DataRequest();
        req.add("numName", numNameTextField.getText());
        DataResponse res = HttpRequestUtil.request("/api/innovation/getInnovationList", req);
        
        if (res != null && res.getCode() == 0) {
            innovationList = (List<Map<String, Object>>) res.getData();
            observableList.clear();
            observableList.addAll(innovationList);
            dataTableView.setItems(observableList);
        }
    }

    @FXML
    protected void onSaveButtonClick() {
        if (studentNumField.getText().isEmpty()) {
            MessageDialog.showDialog("学号不能为空");
            return;
        }
        if (studentNameField.getText().isEmpty()) {
            MessageDialog.showDialog("姓名不能为空");
            return;
        }
        if (achievementField.getText().isEmpty()) {
            MessageDialog.showDialog("创新成果不能为空");
            return;
        }
        
        Map<String, Object> form = new HashMap<>();
        form.put("studentNum", studentNumField.getText());
        form.put("studentName", studentNameField.getText());
        form.put("achievement", achievementField.getText());
        
        OptionItem selectedTeacher = teacherComboBox.getSelectionModel().getSelectedItem();
        if (selectedTeacher != null) {
            form.put("teacherId", Integer.parseInt(selectedTeacher.getValue()));
        } else {
            form.put("teacherId", 0);
        }

        DataRequest req = new DataRequest();
        req.add("form", form);
        DataResponse res = HttpRequestUtil.request("/api/innovation/innovationSave", req);
        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("提交成功");
            onQueryButtonClick();
        } else if (res != null) {
            MessageDialog.showDialog(res.getMsg());
        }
        showVBox.setVisible(false);
        showVBox.setManaged(false);
    }

    public void clearPanel() {
        innovationId = null;
        studentNumField.setText("");
        studentNameField.setText("");
        achievementField.setText("");
        teacherComboBox.getSelectionModel().clearSelection();
    }

    @FXML
    protected void onAddButtonClick() {
        showVBox.setVisible(true);
        showVBox.setManaged(true);
        clearPanel();
    }
    
    @FXML
    protected void onCancelButtonClick() {
        showVBox.setVisible(false);
        showVBox.setManaged(false);
    }
    
    @FXML
    protected void onDeleteButtonClick() {
        Map<String, Object> form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            MessageDialog.showDialog("没有选择，不能删除");
            return;
        }
        
        // 创建自定义的确认对话框，只有"是"和"否"按钮
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确认要删除该创新成果吗?");
        
        // 移除默认的"取消"按钮
        ButtonType yesButton = new ButtonType("是", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("否", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            // 用户点击了"是"按钮
            Object innovationIdObj = form.get("innovationId");
            Integer innovationId = null;
            if (innovationIdObj instanceof Integer) {
                innovationId = (Integer) innovationIdObj;
            } else if (innovationIdObj instanceof String) {
                innovationId = Integer.parseInt((String) innovationIdObj);
            }
            
            DataRequest req = new DataRequest();
            req.add("innovationId", innovationId);
            DataResponse res = HttpRequestUtil.request("/api/innovation/innovationDelete", req);
            if (res != null && res.getCode() == 0) {
                MessageDialog.showDialog("删除成功！");
                onQueryButtonClick();
            } else if (res != null) {
                MessageDialog.showDialog(res.getMsg());
            }
            
            showVBox.setVisible(false);
            showVBox.setManaged(false);
        }
    }
    
    // 实现ToolController的方法
    @Override
    public void doRefresh() {
        onQueryButtonClick();
    }

    /**
     * 配置教师下拉框显示格式
     */
    private void configureTeacherComboBox() {
        // 设置单元格工厂，控制下拉列表中每个选项的显示方式
        teacherComboBox.setCellFactory(param -> new ListCell<OptionItem>() {
            @Override
            protected void updateItem(OptionItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // 使用toString方法，这会返回title或label
                    setText(item.toString());
                    // 调试输出
                    System.out.println("显示教师选项: " + item.toString());
                }
            }
        });
        
        // 设置转换器，控制选中后在ComboBox中显示的文本
        teacherComboBox.setConverter(new StringConverter<OptionItem>() {
            @Override
            public String toString(OptionItem item) {
                if (item == null) return null;
                // 使用toString方法，这会返回title或label
                String text = item.toString();
                // 调试输出
                System.out.println("转换教师选项: " + text);
                return text;
            }
            
            @Override
            public OptionItem fromString(String string) {
                // 不需要从字符串转换回OptionItem，但为了完整性，我们可以实现它
                if (string == null || string.isEmpty()) return null;
                
                for (OptionItem item : teacherComboBox.getItems()) {
                    if (string.equals(item.toString())) {
                        return item;
                    }
                }
                return null;
            }
        });
        
        // 添加监听器，在选择变化时输出调试信息
        teacherComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("选中教师: ID=" + newVal.getId() + ", Value=" + newVal.getValue() + ", Title=" + newVal.getTitle());
            } else {
                System.out.println("未选中教师");
            }
        });
    }

    /**
     * 设置表格数据
     */
    private void setTableViewData() {
        observableList.clear();
        for (int j = 0; j < innovationList.size(); j++) {
            observableList.addAll(FXCollections.observableArrayList(innovationList.get(j)));
        }
        dataTableView.setItems(observableList);
    }
}
