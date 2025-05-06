package com.teach.javafx.controller;

import com.teach.javafx.controller.base.MessageDialog;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.OptionItem;
import com.teach.javafx.util.CommonMethod;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FamilyMemberController extends ToolController {
    @FXML
    private TableView<Map> dataTableView;  // 用户信息表

    @FXML
    private TableColumn<Map, String> nameColumn; // 用户信息表 名称列
    @FXML
    private TableColumn<Map, String> studentNameColumn; // 用户信息表 年龄列
    @FXML
    private TableColumn<Map, String> relationColumn;
    @FXML
    private TableColumn<Map, String> ageColumn;
    @FXML
    private TableColumn<Map, String> unitColumn;
    @FXML
    private TableColumn<Map, String> genderColumn;

    @FXML
    private TextField nameField;  //查询 姓名输入域
    @FXML
    private TextField relationField;  //查询 姓名输入域
    @FXML
    private TextField ageField;   // 查询 年龄输入域
    @FXML
    private TextField unitField;   // 查询 单位输入域
    @FXML
    private ComboBox<OptionItem> genderComboBox;  //学生信息  性别输入域

    @FXML
    private TextField nameTextField;  //查询 姓名输入域

    @FXML
    private VBox showVBox;

    private Integer memberId = null;  //当前编辑修改的家庭成员的主键
    private ArrayList<Map> familyMemberList = new ArrayList();  // 家庭成员信息列表数据
    private ObservableList<Map> observableList = FXCollections.observableArrayList();  // TableView渲染列表
    private List<OptionItem> genderList;   //性别选择列表数据
    private boolean isAdd = false;  // 是否为添加用户
    private Integer personId = null;//学生id
//    private String name = "";

    @FXML
    public void initialize(Integer personId) {
        isAdd = false;
        this.personId = personId;
        showVBox.setVisible(false);
        showVBox.setManaged(false);
        DataResponse res;
        DataRequest req = new DataRequest();
        req.add("personId", personId);
//        req.add("name", name);
        res = HttpRequestUtil.request("/api/family_member/getFamilyMemberList", req); //从后台获取所有用户信息列表集合
        if (res != null && res.getCode() == 0) {
            familyMemberList = (ArrayList<Map>) res.getData();
        }
        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        ObservableList<Integer> list = tsm.getSelectedIndices();
        list.addListener(this::onTableRowSelect);
        nameColumn.setCellValueFactory(new MapValueFactory<>("name"));  //设置列值工厂属性
        studentNameColumn.setCellValueFactory(new MapValueFactory<>("studentName"));
        relationColumn.setCellValueFactory(new MapValueFactory<>("relation"));
        ageColumn.setCellValueFactory(new MapValueFactory<>("age"));
        unitColumn.setCellValueFactory(new MapValueFactory<>("unit"));
        genderColumn.setCellValueFactory(new MapValueFactory<>("genderName"));
        setTableViewData();
        genderList = HttpRequestUtil.getDictionaryOptionItemList("XBM");
        genderComboBox.getItems().addAll(genderList);
    }

    private void setTableViewData() {
        observableList.clear();
        for (int j = 0; j < familyMemberList.size(); j++) {
            observableList.addAll(FXCollections.observableArrayList(familyMemberList.get(j)));
        }
        dataTableView.setItems(observableList);
    }

    private void refreshTableViewData() {
        DataResponse res;
        DataRequest req = new DataRequest();
        req.add("personId", personId);
//        req.add("name", name);
        res = HttpRequestUtil.request("/api/family_member/getFamilyMemberList", req); //从后台获取所有用户信息列表集合
        if (res != null && res.getCode() == 0) {
            familyMemberList = (ArrayList<Map>) res.getData();
        }
        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        ObservableList<Integer> list = tsm.getSelectedIndices();
        list.addListener(this::onTableRowSelect);
        nameColumn.setCellValueFactory(new MapValueFactory<>("name"));  //设置列值工厂属性
        studentNameColumn.setCellValueFactory(new MapValueFactory<>("studentName"));
        relationColumn.setCellValueFactory(new MapValueFactory<>("relation"));
        ageColumn.setCellValueFactory(new MapValueFactory<>("age"));
        unitColumn.setCellValueFactory(new MapValueFactory<>("unit"));
        genderColumn.setCellValueFactory(new MapValueFactory<>("genderName"));
        setTableViewData();
    }

    public void onTableRowSelect(ListChangeListener.Change<? extends Integer> change) {
        changeFamilyMemberInfo();
        showVBox.setVisible(true);
        showVBox.setManaged(true);
        isAdd = false;
    }

    protected void changeFamilyMemberInfo() {
        Map<String,Object> form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            clearPanel();
            return;
        }
        memberId = CommonMethod.getInteger(form, "memberId");
        nameField.setText(CommonMethod.getString(form, "name"));
        ageField.setText(CommonMethod.getString(form, "age"));
        relationField.setText(CommonMethod.getString(form, "relation"));
        unitField.setText(CommonMethod.getString(form, "unit"));
        genderComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(genderList, CommonMethod.getString(form, "gender")));
    }

    public void clearPanel() {
        memberId = null;
        nameField.setText("");
        ageField.setText("");
        relationField.setText("");
        unitField.setText("");
        genderComboBox.getSelectionModel().select(-1);
    }

    @FXML
    protected void onQueryButtonClick() {
        String name = nameTextField.getText();
        if(name.isEmpty())
            refreshTableViewData();
        else {
            DataRequest req = new DataRequest();
            req.add("name", name);
            DataResponse res = HttpRequestUtil.request("/api/family_member/getFamilyMemberList", req);
            if (res != null && res.getCode() == 0) {
                familyMemberList = (ArrayList<Map>) res.getData();
                setTableViewData();
            }
        }
    }

    @FXML
    protected void onAddButtonClick() {
        clearPanel();
        isAdd = true;
        showVBox.setVisible(true);
        showVBox.setManaged(true);
    }

    @FXML
    protected void onDeleteButtonClick() {
        Map form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            MessageDialog.showDialog("没有选择，不能删除");
            return;
        }
        int ret = MessageDialog.choiceDialog("确认要删除吗?");
        if (ret!= MessageDialog.CHOICE_YES) {
            return;
        }
        memberId = CommonMethod.getInteger(form,"memberId");
        DataRequest req = new DataRequest();
        req.add("memberId", memberId);
        DataResponse res = HttpRequestUtil.request("/api/family_member/deleteFamilyMember", req);
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
        refreshTableViewData();
    }

    @FXML
    protected void onSaveButtonClick() {
        if (nameField.getText().isEmpty()) {
            MessageDialog.showDialog("家庭成员姓名不能为空！");
            return;
        }
        if (relationField.getText().isEmpty()) {
            MessageDialog.showDialog("关系不能为空！");
            return;
        }
        if(genderComboBox.getSelectionModel().getSelectedIndex() == -1) {
            MessageDialog.showDialog("性别不能为空！");
            return;
        }
        Map<String,Object> form = new HashMap<>();
        form.put("name", nameField.getText());
        form.put("personId", personId);
        form.put("relation", relationField.getText());
        form.put("age", ageField.getText());
        form.put("unit", unitField.getText());
        if (genderComboBox.getSelectionModel() != null && genderComboBox.getSelectionModel().getSelectedItem() != null)
            form.put("gender", genderComboBox.getSelectionModel().getSelectedItem().getValue());
        DataRequest req = new DataRequest();
        DataResponse res;
        if (isAdd) {
            req.add("form", form);
            res = HttpRequestUtil.request("/api/family_member/addFamilyMember", req);
            isAdd = false;
        }
        else {
            form.put("memberId", memberId);
            req.add("form", form);
            res = HttpRequestUtil.request("/api/family_member/editFamilyMember", req);
        }
        if (res.getCode() == 0) {
            MessageDialog.showDialog("保存成功！");
//            onQueryButtonClick();

        }
        else {
            MessageDialog.showDialog(res.getMsg());
        }
        showVBox.setVisible(false);
        showVBox.setManaged(false);
        refreshTableViewData();
    }
}
