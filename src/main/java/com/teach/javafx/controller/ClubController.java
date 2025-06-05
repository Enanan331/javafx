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
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class ClubController extends ToolController {
    @FXML
    private TableView<Map> dataTableView;  // 社团信息表

    @FXML
    private TableColumn<Map, String> nameColumn; // 社团名称列
    @FXML
    private TableColumn<Map, String> presidentNameColumn; // 社长名列
    @FXML
    private TableColumn<Map, String> advisorNameColumn; // 指导教师名列
    @FXML
    private TableColumn<Map, String> locationColumn; // 地点列
    @FXML
    private TableColumn<Map, String> descriptionColumn; // 社团简介列

    @FXML
    private TextField nameField;  // 社团名称输入域
    @FXML
    private TextField locationField;   // 地点输入域
    @FXML
    private TextField descriptionField;   // 描述输入域
    @FXML
    private ComboBox<Map> presidentComboBox;
    @FXML
    private ComboBox<Map> advisorComboBox;

    @FXML
    private GridPane showGrid;
    @FXML
    private FlowPane showFlow;
    @FXML
    private Label nameLabel;
    @FXML
    private Label locationLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label advisorLabel;
    @FXML
    private Label presidentLabel;
    @FXML
    private Button saveButton;
    @FXML
    private Button manageMembersButton;


    @FXML
    private TextField nameTextField;  // 查询社团名称输入域

    @FXML
    private VBox showVBox;

    private Integer clubId = null;  // 当前编辑修改的社团的主键
    private ArrayList<Map> clubList = new ArrayList();  // 社团信息列表数据
    private ObservableList<Map> observableList = FXCollections.observableArrayList();  // TableView渲染列表
    private boolean isAdd = false;  // 是否为添加社团
    private List<Map> teacherList = new ArrayList<>();
    private List<Map> memberList = new ArrayList<>();
    private ObservableList<Map> allStudents = FXCollections.observableArrayList(); // 所有学生
    private ObservableList<Map> currentClubMembers = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        isAdd = false;
     setVisible(false);

        // 初始化表格列（按新顺序）
        nameColumn.setCellValueFactory(new MapValueFactory<>("name"));
        presidentNameColumn.setCellValueFactory(new MapValueFactory<>("presidentName"));
        advisorNameColumn.setCellValueFactory(new MapValueFactory<>("advisorName"));
        locationColumn.setCellValueFactory(new MapValueFactory<>("location"));
        descriptionColumn.setCellValueFactory(new MapValueFactory<>("description"));

        // 设置表格选择监听
        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        ObservableList<Integer> list = tsm.getSelectedIndices();
        list.addListener(this::onTableRowSelect);

        initComboBoxes();

        loadAllTeachers();
        initPresidentComboBox();
        loadAllStudents();

        // 加载数据
        refreshTableViewData();
    }

    private void loadAllTeachers() {
        DataResponse res = HttpRequestUtil.request("/api/club/getTeacherListAll", new DataRequest());
        if (res != null && res.getCode() == 0) {
            teacherList = (List<Map>) res.getData();
            advisorComboBox.setItems(FXCollections.observableArrayList(teacherList));
        }
    }

    private void initPresidentComboBox() {
        presidentComboBox.setCellFactory(param -> new ListCell<Map>() {
            @Override
            protected void updateItem(Map item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(CommonMethod.getString(item, "num") + " - " +
                            CommonMethod.getString(item, "name"));
                }
            }
        });

        presidentComboBox.setConverter(new StringConverter<Map>() {
            @Override
            public String toString(Map item) {
                if (item == null) return null;
                return CommonMethod.getString(item, "num") + " - " +
                        CommonMethod.getString(item, "name");
            }

            @Override
            public Map fromString(String string) {
                return null;
            }
        });
    }

    private void initComboBoxes() {
        // 配置社长下拉框显示格式
        presidentComboBox.setCellFactory(param -> new ListCell<Map>() {
            @Override
            protected void updateItem(Map item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(CommonMethod.getString(item, "num") + " - " +
                            CommonMethod.getString(item, "name"));
                }
            }
        });

        // 配置社长下拉框显示值格式
        presidentComboBox.setConverter(new StringConverter<Map>() {
            @Override
            public String toString(Map item) {
                if (item == null) return null;
                return CommonMethod.getString(item, "num") + " - " +
                        CommonMethod.getString(item, "name");
            }

            @Override
            public Map fromString(String string) {
                return null; // 不需要反向转换
            }
        });

        // 配置指导教师下拉框显示格式
        advisorComboBox.setCellFactory(param -> new ListCell<Map>() {
            @Override
            protected void updateItem(Map item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(CommonMethod.getString(item, "num") + " - " +
                            CommonMethod.getString(item, "name"));
                }
            }
        });

        // 配置指导教师下拉框显示值格式
        advisorComboBox.setConverter(new StringConverter<Map>() {
            @Override
            public String toString(Map item) {
                if (item == null) return null;
                return CommonMethod.getString(item, "num") + " - " +
                        CommonMethod.getString(item, "name");
            }

            @Override
            public Map fromString(String string) {
                return null; // 不需要反向转换
            }
        });

        // 确保教师下拉框使用最新数据
        advisorComboBox.getItems().setAll(FXCollections.observableArrayList(teacherList));

        // 首次加载所有学生数据
        loadAllStudents();
    }

    private void loadAllStudents() {
        DataResponse res = HttpRequestUtil.request("/api/student/getStudentListAll", new DataRequest());
        if (res != null && res.getCode() == 0) {
            allStudents.setAll((List<Map>) res.getData());
        }
    }

    private void loadCurrentClubMembers(Integer clubId) {
        DataRequest req = new DataRequest();
        req.add("clubId", clubId);
        DataResponse res = HttpRequestUtil.request("/api/club/getClubMemberList", req);
        if (res != null && res.getCode() == 0) {
            currentClubMembers.setAll((List<Map>) res.getData());
        }
    }

    private void loadClubMembers(Integer clubId) {
        DataRequest req = new DataRequest();
        req.add("clubId", clubId);
        DataResponse res = HttpRequestUtil.request("/api/club/getClubMemberList", req);
        if (res != null && res.getCode() == 0) {
            List<Map> currentMembers = (List<Map>) res.getData();

            // 仅加载当前社团成员到社长下拉框
            presidentComboBox.getItems().setAll(FXCollections.observableArrayList(currentMembers));

            // 保留当前选择值
            Map selected = presidentComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                for (Map item : presidentComboBox.getItems()) {
                    if (CommonMethod.getString(selected, "personId")
                            .equals(CommonMethod.getString(item, "personId"))) {
                        presidentComboBox.getSelectionModel().select(item);
                        break;
                    }
                }
            }
        }
    }

    private void loadTeacherData() {
        DataResponse res = HttpRequestUtil.request("/api/teacher/getTeacherList", new DataRequest());
        if (res != null && res.getCode() == 0) {
            teacherList = (ArrayList<Map>) res.getData();
            advisorComboBox.getItems().clear();
            advisorComboBox.getItems().addAll(FXCollections.observableArrayList(teacherList));
        }
    }



    protected void changeClubInfo() {
        Map<String, Object> form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            clearPanel();
            return;
        }
        clubId = CommonMethod.getInteger(form, "clubId");

        // 加载当前社团成员
        loadCurrentClubMembers(clubId);

        // 设置社长选择框数据源为当前社团成员
        presidentComboBox.setItems(currentClubMembers);
        advisorComboBox.getItems().setAll(FXCollections.observableArrayList(teacherList));

        // 设置表单字段值
        nameField.setText(CommonMethod.getString(form, "name"));
        locationField.setText(CommonMethod.getString(form, "location"));
        descriptionField.setText(CommonMethod.getString(form, "description"));

        // 设置社长选中项
        String presidentId = CommonMethod.getString(form, "presidentId");
        selectPresidentById(presidentId);

        // 设置指导教师
        String advisorId = CommonMethod.getString(form, "advisorId");
        selectAdvisorById(advisorId);
    }

    private void selectPresidentById(String presidentId) {
        if (presidentId != null && !presidentId.isEmpty()) {
            for (Map member : currentClubMembers) {
                if (presidentId.equals(CommonMethod.getString(member, "personId"))) {
                    presidentComboBox.getSelectionModel().select(member);
                    return;
                }
            }
        }
        presidentComboBox.getSelectionModel().clearSelection();
    }

    // 根据ID选择指导教师
    private void selectAdvisorById(String advisorId) {
        if (advisorId != null && !advisorId.isEmpty()) {
            for (Map teacher : teacherList) {
                if (advisorId.equals(CommonMethod.getString(teacher, "personId"))) {
                    advisorComboBox.getSelectionModel().select(teacher);
                    return;
                }
            }
        }
        advisorComboBox.getSelectionModel().clearSelection();
    }

    private void refreshTableViewData() {
        DataResponse res;
        DataRequest req = new DataRequest();
        String name = nameTextField.getText();
        if (name != null && !name.isEmpty()) {
            req.add("name", name);
        }
        res = HttpRequestUtil.request("/api/club/getClubList", req);
        if (res != null && res.getCode() == 0) {
            clubList = (ArrayList<Map>) res.getData();
        }

        // 更新表格数据
        observableList.clear();
        for (int j = 0; j < clubList.size(); j++) {
            observableList.addAll(FXCollections.observableArrayList(clubList.get(j)));
        }
        dataTableView.setItems(observableList);
    }

    public void onTableRowSelect(ListChangeListener.Change<? extends Integer> change) {
        if (change == null || change.getList().isEmpty()) {
            clearPanel();
            if (!isAdd) setVisible(false); // 无选中行且非添加模式时隐藏
            return;
        }
        changeClubInfo();
       setVisible(true);
        isAdd = false;
    }

    public void clearPanel() {
        clubId = null;
        nameField.setText("");
        locationField.setText("");
        descriptionField.setText("");
        presidentComboBox.setValue(null);
        advisorComboBox.setValue(null);
    }

    @FXML
    protected void onQueryButtonClick() {
        refreshTableViewData();
        isAdd = false;
      setVisible(false);
    }

    @FXML
    protected void onAddButtonClick() {
        clearPanel();
        isAdd = true;
        setVisible(true);
        nameTextField.setText("");

        // 添加模式：社长选择框使用所有学生
        presidentComboBox.setItems(allStudents);
        advisorComboBox.setItems(FXCollections.observableArrayList(teacherList));

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
        int ret = MessageDialog.choiceDialog("确认要删除该社团吗?");
        if (ret != MessageDialog.CHOICE_YES) {
            return;
        }
        clubId = CommonMethod.getInteger(form, "clubId");
        DataRequest req = new DataRequest();
        req.add("clubId", clubId);
        DataResponse res = HttpRequestUtil.request("/api/club/deleteClub", req);
        setVisible(false);
        if(res != null) {
            if (res.getCode() == 0) {
                setVisible(false);
                MessageDialog.showDialog("删除成功！");
            } else {
                setVisible(false);
                MessageDialog.showDialog(res.getMsg());
            }
        }
        // 无论成功或失败都隐藏VBox
        setVisible(false);
        refreshTableViewData();
    }


    @FXML
    protected void onSaveButtonClick() {
        if (nameField.getText().isEmpty()) {
            MessageDialog.showDialog("社团名称不能为空！");
            return;
        }

        Map<String,Object> form = new HashMap<>();
        form.put("name", nameField.getText());
        form.put("location", locationField.getText());
        form.put("description", descriptionField.getText());

        // 获取选中的社长
        Map selectedPresident = presidentComboBox.getSelectionModel().getSelectedItem();
        String presidentId = selectedPresident != null ?
                CommonMethod.getString(selectedPresident, "personId") : "";

        // 获取选中的指导老师
        Map selectedAdvisor = advisorComboBox.getSelectionModel().getSelectedItem();
        String advisorId = selectedAdvisor != null ?
                CommonMethod.getString(selectedAdvisor, "personId") : "";

        form.put("presidentId", presidentId);
        form.put("advisorId", advisorId);

        DataRequest req = new DataRequest();
        DataResponse res;
        if (isAdd) {
            req.add("form", form);
            res = HttpRequestUtil.request("/api/club/addClub", req);
            isAdd = false;
        } else {
            req.add("clubId", clubId);
            req.add("form", form);
            res = HttpRequestUtil.request("/api/club/editClub", req);
            setVisible(false);
        }

        // 无论成功或失败都隐藏VBox
       setVisible(false);

        if (res != null && res.getCode() == 0) {
            setVisible(false);
            MessageDialog.showDialog("保存成功！");
            nameTextField.setText("");
            refreshTableViewData();
        } else {
            setVisible(false);
            MessageDialog.showDialog("保存失败！"+(res!= null?res.getMsg():""));
            refreshTableViewData();
        }
    }

    @FXML
    protected void onManageMembersClick() throws IOException {
        if (clubId == null) {
            MessageDialog.showDialog("请先选择一个社团");
            return;
        }

        // 保存当前社团ID
        final Integer currentClubId = clubId;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/teach/javafx/club-members.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("管理社团成员");
        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);

        ClubMembersController clubMembersController = loader.getController();
        clubMembersController.initialize(currentClubId);

        // 设置关闭监听器
        stage.setOnHidden(e -> {
            // 重新加载当前社团成员
            loadCurrentClubMembers(currentClubId);

            // 刷新社长选择框
            presidentComboBox.setItems(currentClubMembers);

            // 重新选择当前社长
            Map<String, Object> selectedClub = dataTableView.getSelectionModel().getSelectedItem();
            if (selectedClub != null) {
                String presidentId = CommonMethod.getString(selectedClub, "presidentId");
                selectPresidentById(presidentId);
            }
        });

        stage.show();
    }

    private void setVisible(boolean visible) {
        showVBox.setVisible(visible);
        showVBox.setManaged(visible);
        showGrid.setVisible(visible);
        showGrid.setManaged(visible);
        showFlow.setVisible(visible);
        showFlow.setManaged(visible);
        nameLabel.setVisible(visible);
        nameLabel.setManaged(visible);
        nameField.setVisible(visible);
        nameField.setManaged(visible);
        locationLabel.setVisible(visible);
        locationLabel.setManaged(visible);
        locationField.setVisible(visible);
        locationField.setManaged(visible);
        descriptionLabel.setVisible(visible);
        descriptionLabel.setManaged(visible);
        descriptionField.setVisible(visible);
        descriptionField.setManaged(visible);
        advisorLabel.setVisible(visible);
        advisorLabel.setManaged(visible);
        advisorComboBox.setVisible(visible);
        advisorComboBox.setManaged(visible);
        presidentLabel.setVisible(visible);
        presidentLabel.setManaged(visible);
        presidentComboBox.setVisible(visible);
        presidentComboBox.setManaged(visible);
        saveButton.setVisible(visible);
        saveButton.setManaged(visible);
        manageMembersButton.setVisible(visible);
        manageMembersButton.setManaged(visible);
    }
}