package com.teach.javafx.controller;

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
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClubMembersController extends ToolController {
    @FXML
    private TableView<Map> dataTableView;  // 学生信息表

    @FXML
    private TableColumn<Map, String> numColumn; // 学生信息表 学号列
    @FXML
    private TableColumn<Map, String> nameColumn; // 学生信息表 姓名列
    @FXML
    private TableColumn<Map, String> deptColumn; // 学生信息表 院系列
    @FXML
    private TableColumn<Map, String> majorColumn; // 学生信息表 专业列
    @FXML
    private TableColumn<Map, String> classNameColumn; // 学生信息表 班级列

    @FXML
    private TextField numNameTextField;  // 查询 学号姓名输入域

    @FXML
    private VBox showVBox;

    @FXML
    private Label numLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Button saveButton;

    @FXML
    private GridPane showGrid;

    @FXML
    private FlowPane showFlow;

    @FXML
    private TextField numField;  // 学号输入域
    @FXML
    private TextField nameField;  // 姓名输入域

    private Integer personId = null;  // 当前编辑修改的学生的主键
    private ArrayList<Map> studentList = new ArrayList();  // 学生信息列表数据
    private ObservableList<Map> observableList = FXCollections.observableArrayList();  // TableView渲染列表
    private boolean isAdd = false;  // 是否为添加学生
    private Integer clubId = null; // 社团ID

    @FXML
    public void initialize(Integer clubId) {
        this.clubId = clubId;
        isAdd = false;
        setVisible(false);

        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        ObservableList<Integer> list = tsm.getSelectedIndices();
        list.addListener(this::onTableRowSelect);
        // 根据社团ID获取学生列表
        getStudentListByClubId();
        // 设置表格列值工厂
        setColumnValueFactories();
        // 设置表格数据
        setTableViewData();
    }

    private void getStudentListByClubId() {
        DataResponse res;
        DataRequest req = new DataRequest();
        req.add("clubId", clubId);
        res = HttpRequestUtil.request("/api/club/getClubMemberList", req);
        if (res != null && res.getCode() == 0) {
            studentList = (ArrayList<Map>) res.getData();
        }
    }

    private void setColumnValueFactories() {
        numColumn.setCellValueFactory(new MapValueFactory<>("num"));
        nameColumn.setCellValueFactory(new MapValueFactory<>("name"));
        deptColumn.setCellValueFactory(new MapValueFactory<>("dept"));
        majorColumn.setCellValueFactory(new MapValueFactory<>("major"));
        classNameColumn.setCellValueFactory(new MapValueFactory<>("className"));
    }

    private void setTableViewData() {
        observableList.clear();
        for (int j = 0; j < studentList.size(); j++) {
            observableList.addAll(FXCollections.observableArrayList(studentList.get(j)));
        }
        dataTableView.setItems(observableList);
    }

    private void refreshTableViewData() {
        getStudentListByClubId();
        setTableViewData();
    }

    public void onTableRowSelect(ListChangeListener.Change<? extends Integer> change) {
        changeStudentInfo();
        setVisible(true);
        isAdd = false;
    }

    protected void changeStudentInfo() {
        Map<String, Object> form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            clearPanel();
            return;
        }
        personId = CommonMethod.getInteger(form, "personId");
        numField.setText(CommonMethod.getString(form, "num"));
        nameField.setText(CommonMethod.getString(form, "name"));
    }

    public void clearPanel() {
        personId = null;
        numField.setText("");
        nameField.setText("");
    }

    @FXML
    protected void onStudentsQueryButtonClick() {
        String searchText = numNameTextField.getText();
        if (searchText.isEmpty()) {
            refreshTableViewData();
        } else {
            DataRequest req = new DataRequest();
            req.add("numName", searchText);
            req.add("clubId", clubId);
            DataResponse res = HttpRequestUtil.request("/api/club/getClubMember", req);
            if (res != null && res.getCode() == 0) {
                studentList = (ArrayList<Map>) res.getData();
                setTableViewData();
            }
        }
        isAdd = false;
        setVisible(false);
    }

    @FXML
    protected void onStudentsAddButtonClick() {
        clearPanel();
        isAdd = true;
        setVisible(true);
        numNameTextField.setText("");
        refreshTableViewData();
        dataTableView.getSelectionModel().clearSelection();
    }

    @FXML
    protected void onStudentsDeleteButtonClick() {
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
        req.add("clubId", clubId);
        DataResponse res = HttpRequestUtil.request("/api/club/deleteClubMember", req);
        if (res != null) {
            if (res.getCode() == 0) {
                MessageDialog.showDialog("删除成功！");
                numNameTextField.setText("");
                clearPanel();
                refreshTableViewData();
            } else {
                MessageDialog.showDialog(res.getMsg());
            }
        }
        setVisible(false);
        dataTableView.getSelectionModel().clearSelection();
    }

    @FXML
    protected void onStudentsSaveButtonClick() {
        if (numField.getText().isEmpty()) {
            MessageDialog.showDialog("学号不能为空！");
            return;
        }
        if (nameField.getText().isEmpty()) {
            MessageDialog.showDialog("姓名不能为空！");
            return;
        }

        DataRequest req = new DataRequest();
        req.add("num", numField.getText());
        req.add("name", nameField.getText());
        req.add("clubId", clubId);
        DataResponse res;
        if (isAdd) {
            res = HttpRequestUtil.request("/api/club/addClubMember", req);
        } else {
            req.add("personId", personId);
            res = HttpRequestUtil.request("/api/club/editClubMember", req);
        }

        if (res != null) {
            if (res.getCode() == 0) {
                numNameTextField.setText("");
                clearPanel();
                refreshTableViewData();
                MessageDialog.showDialog("保存成功！");
            } else {
                setVisible(false);
                dataTableView.getSelectionModel().clearSelection();
                numNameTextField.setText("");
                clearPanel();
                refreshTableViewData();
                MessageDialog.showDialog(res.getMsg());
            }
        } else {
            setVisible(false);
            dataTableView.getSelectionModel().clearSelection();
            MessageDialog.showDialog("保存失败，后端无响应！");
        }
        setVisible(false);
        dataTableView.getSelectionModel().clearSelection();
    }

    private void setVisible(boolean visible){
        showVBox.setVisible(visible);
        showVBox.setManaged(visible);
        showGrid.setVisible(visible);
        showGrid.setManaged(visible);
        showFlow.setVisible(visible);
        showFlow.setManaged(visible);
        numLabel.setVisible(visible);
        numLabel.setManaged(visible);
        numField.setVisible(visible);
        numField.setManaged(visible);
        nameLabel.setVisible(visible);
        nameLabel.setManaged(visible);
        nameField.setVisible(visible);
        nameField.setManaged(visible);
        saveButton.setVisible(visible);
        saveButton.setManaged(visible);
    }
}