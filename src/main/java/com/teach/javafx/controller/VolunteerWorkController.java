package com.teach.javafx.controller;

import com.teach.javafx.controller.base.LocalDateStringConverter;
import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.util.CommonMethod;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VolunteerWorkController extends ToolController {
    @FXML
    private TableView<Map> dataTableView;  // 志愿活动信息表

    @FXML
    private TableColumn<Map, String> nameColumn; // 活动名称列
    @FXML
    private TableColumn<Map, String> dateColumn; // 日期列
    @FXML
    private TableColumn<Map, String> startTimeColumn; // 开始时间列
    @FXML
    private TableColumn<Map, String> endTimeColumn; // 结束时间列
    @FXML
    private TableColumn<Map, Double> serviceHoursColumn; // 服务时长列
    @FXML
    private TableColumn<Map, String> locationColumn; // 地点列
    @FXML
    private TableColumn<Map, String> organizerColumn; // 组织者列

    @FXML
    private TextField nameField;  // 活动名称输入域
    @FXML
    private DatePicker datePick;
    @FXML
    private ComboBox<String> startTimeComboBox;   // 开始时间选择框
    @FXML
    private ComboBox<String> endTimeComboBox;   // 结束时间选择框
    @FXML
    private TextField locationField;   // 地点输入域
    @FXML
    private TextField organizerField;   // 组织者输入域

    @FXML
    private TextField nameTextField;  // 查询活动名称输入域

    @FXML
    private VBox showVBox;

    private Integer volunteerWorkId = null;  // 当前编辑修改的志愿活动的主键
    private ArrayList<Map> volunteerWorkList = new ArrayList();  // 志愿活动信息列表数据
    private ObservableList<Map> observableList = FXCollections.observableArrayList();  // TableView渲染列表
    private boolean isAdd = false;  // 是否为添加活动

    @FXML
    public void initialize() {
        isAdd = false;
        showVBox.setVisible(false);
        showVBox.setManaged(false);
        datePick.setConverter(new LocalDateStringConverter("yyyy-MM-dd"));

        // 初始化表格列
        nameColumn.setCellValueFactory(new MapValueFactory<>("name"));
        dateColumn.setCellValueFactory(new MapValueFactory<>("date"));
        startTimeColumn.setCellValueFactory(new MapValueFactory<>("startTime"));
        endTimeColumn.setCellValueFactory(new MapValueFactory<>("endTime"));
        serviceHoursColumn.setCellValueFactory(new MapValueFactory<>("serviceHours"));
        locationColumn.setCellValueFactory(new MapValueFactory<>("location"));
        organizerColumn.setCellValueFactory(new MapValueFactory<>("organizer"));

        // 设置表格选择监听
        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        ObservableList<Integer> list = tsm.getSelectedIndices();
        list.addListener(this::onTableRowSelect);

        // 加载数据
        refreshTableViewData();

        // 初始化时间选择框
        initializeTimeComboBoxes();
    }

    private void refreshTableViewData() {
        DataResponse res;
        DataRequest req = new DataRequest();
        String name = nameTextField.getText();
        if (name != null && !name.isEmpty()) {
            req.add("name", name);
        }
        res = HttpRequestUtil.request("/api/volunteerWork/getVolunteerWorkList", req);
        if (res != null && res.getCode() == 0) {
            volunteerWorkList = (ArrayList<Map>) res.getData();
        }

        // 更新表格数据
        observableList.clear();
        for (int j = 0; j < volunteerWorkList.size(); j++) {
            observableList.addAll(FXCollections.observableArrayList(volunteerWorkList.get(j)));
        }
        dataTableView.setItems(observableList);
    }

    public void onTableRowSelect(ListChangeListener.Change<? extends Integer> change) {
        changeVolunteerWorkInfo();
        showVBox.setVisible(true);
        showVBox.setManaged(true);
        isAdd = false;
    }

    protected void changeVolunteerWorkInfo() {
        Map<String,Object> form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            clearPanel();
            return;
        }
        volunteerWorkId = CommonMethod.getInteger(form, "volunteerWorkId");
        nameField.setText(CommonMethod.getString(form, "name"));
        datePick.getEditor().setText(CommonMethod.getString(form, "date"));
        startTimeComboBox.setValue(CommonMethod.getString(form, "startTime"));
        endTimeComboBox.setValue(CommonMethod.getString(form, "endTime"));
        locationField.setText(CommonMethod.getString(form, "location"));
        organizerField.setText(CommonMethod.getString(form, "organizer"));
    }

    public void clearPanel() {
        volunteerWorkId = null;
        nameField.setText("");
        datePick.getEditor().setText("");
        startTimeComboBox.setValue(null);
        endTimeComboBox.setValue(null);
        locationField.setText("");
        organizerField.setText("");
    }

    @FXML
    protected void onQueryButtonClick() {
        refreshTableViewData();
        isAdd = false;
        showVBox.setVisible(false);
        showVBox.setManaged(false);
    }

    @FXML
    protected void onAddButtonClick() {
        clearPanel();
        isAdd = true;
        showVBox.setVisible(true);
        showVBox.setManaged(true);
        nameTextField.setText("");
        refreshTableViewData();
        dataTableView.getSelectionModel().clearSelection();
    }

    @FXML
    protected void onDeleteButtonClick() {
        Map form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            MessageDialog.showDialog("没有选择，不能删除");
            return;
        }
        int ret = MessageDialog.choiceDialog("确认要删除该志愿活动吗?");
        if (ret != MessageDialog.CHOICE_YES) {
            return;
        }
        volunteerWorkId = CommonMethod.getInteger(form, "volunteerWorkId");
        DataRequest req = new DataRequest();
        req.add("volunteerWorkId", volunteerWorkId);
        DataResponse res = HttpRequestUtil.request("/api/volunteerWork/deleteVolunteerWork", req);
        if(res != null) {
            if (res.getCode() == 0) {
                MessageDialog.showDialog("删除成功！");
            } else {
                MessageDialog.showDialog(res.getMsg());
            }
        }
        showVBox.setVisible(false);
        showVBox.setManaged(false);
        refreshTableViewData();
    }

    private void calculateServiceHours() {
        String startTime = startTimeComboBox.getValue();
        String endTime = endTimeComboBox.getValue();

        if (startTime == null || endTime == null) {
            return;
        }

        int startHour = Integer.parseInt(startTime.split(":")[0]);
        int endHour = Integer.parseInt(endTime.split(":")[0]);

        int serviceHours = endHour - startHour;
        System.out.println("Service Hours: " + serviceHours); // 可以添加到 UI 显示逻辑中
    }

    @FXML
    protected void onSaveButtonClick() {
        if (nameField.getText().isEmpty()) {
            MessageDialog.showDialog("活动名称不能为空！");
            return;
        }

        if (datePick.getEditor().getText().isEmpty()) {
            MessageDialog.showDialog("日期不能为空！");
            return;
        }

        if (startTimeComboBox.getValue() == null || endTimeComboBox.getValue() == null) {
            MessageDialog.showDialog("开始时间和结束时间不能为空！");
            return;
        }

        String startTime = startTimeComboBox.getValue();
        String endTime = endTimeComboBox.getValue();

        int startHour = Integer.parseInt(startTime.split(":")[0]);
        int endHour = Integer.parseInt(endTime.split(":")[0]);
        if (startHour >= endHour) {
            MessageDialog.showDialog("开始时间必须早于结束时间！");
            return;
        }

        calculateServiceHours();

        Map<String,Object> form = new HashMap<>();
        form.put("name", nameField.getText());
        form.put("date", datePick.getEditor().getText());
        form.put("startTime", startTime);
        form.put("endTime", endTime);
        form.put("serviceHours", endHour - startHour);
        form.put("location", locationField.getText());
        form.put("organizer", organizerField.getText());

        DataRequest req = new DataRequest();
        DataResponse res;
        if (isAdd) {
            req.add("form", form);
            res = HttpRequestUtil.request("/api/volunteerWork/addVolunteerWork", req);
            isAdd = false;
        } else {
            req.add("volunteerWorkId", volunteerWorkId);
            req.add("form", form);
            res = HttpRequestUtil.request("/api/volunteerWork/editVolunteerWork", req);
        }

        showVBox.setVisible(false);
        showVBox.setManaged(false);

        if (res != null && res.getCode() == 0) {
            MessageDialog.showDialog("保存成功！");
            nameTextField.setText("");
            refreshTableViewData();
        } else {
            MessageDialog.showDialog(res != null ? res.getMsg() : "保存失败");
            refreshTableViewData();
        }
    }

    @FXML
    protected void onManageStudentsClick() throws IOException {
        if (volunteerWorkId == null) {
            MessageDialog.showDialog("请先选择一个志愿活动");
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/teach/javafx/volunteer-work-students.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setWidth(1500);
        stage.setHeight(1000);
        stage.setResizable(false);
        stage.setTitle("管理学生志愿者");
        Scene scene = new Scene(root,1800,1200);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        VolunteerWorkStudentsController volunteerWorkStudentsController = loader.getController();
        volunteerWorkStudentsController.initialize(volunteerWorkId);
        stage.showAndWait();
    }

    private void initializeTimeComboBoxes() {
        // 生成从 0:00 到 23:00 的时间列表
        for (int i = 0; i < 24; i++) {
            String time = String.format("%02d:00", i);
            startTimeComboBox.getItems().add(time);
            endTimeComboBox.getItems().add(time);
        }
    }
}