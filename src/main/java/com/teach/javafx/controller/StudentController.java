package com.teach.javafx.controller;

import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.LocalDateStringConverter;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.DataResponse;
import com.teach.javafx.util.CommonMethod;
import com.teach.javafx.controller.base.MessageDialog;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StudentController 登录交互控制类 对应 student_panel.fxml  对应于学生管理的后台业务处理的控制器，主要获取数据和保存数据的方法不同
 *
 * @FXML 属性 对应fxml文件中的
 * @FXML 方法 对应于fxml文件中的 on***Click的属性
 */
public class StudentController extends ToolController {
    private ImageView photoImageView;
    @FXML
    private TableView<Map> dataTableView;  //学生信息表
    @FXML
    private TableColumn<Map, String> numColumn;   //学生信息表 编号列
    @FXML
    private TableColumn<Map, String> nameColumn; //学生信息表 名称列
    @FXML
    private TableColumn<Map, String> deptColumn;  //学生信息表 院系列
    @FXML
    private TableColumn<Map, String> majorColumn; //学生信息表 专业列
    @FXML
    private TableColumn<Map, String> classNameColumn; //学生信息表 班级列
    @FXML
    private TableColumn<Map, String> cardColumn; //学生信息表 证件号码列
    @FXML
    private TableColumn<Map, String> genderColumn; //学生信息表 性别列
    @FXML
    private TableColumn<Map, String> birthdayColumn; //学生信息表 出生日期列
    @FXML
    private TableColumn<Map, String> emailColumn; //学生信息表 邮箱列
    @FXML
    private TableColumn<Map, String> phoneColumn; //学生信息表 电话列
    @FXML
    private TableColumn<Map, String> addressColumn;//学生信息表 地址列
    @FXML
    private Button photoButton;  //照片显示和上传按钮

    @FXML
    private TextField numField; //学生信息  学号输入域
    @FXML
    private TextField nameField;  //学生信息  名称输入域
    @FXML
    private TextField deptField; //学生信息  院系输入域
    @FXML
    private TextField majorField; //学生信息  专业输入域
    @FXML
    private TextField classNameField; //学生信息  班级输入域
    @FXML
    private TextField cardField; //学生信息  证件号码输入域
    @FXML
    private ComboBox<OptionItem> genderComboBox;  //学生信息  性别输入域
    @FXML
    private DatePicker birthdayPick;  //学生信息  出生日期选择域
    @FXML
    private TextField emailField;  //学生信息  邮箱输入域
    @FXML
    private TextField phoneField;   //学生信息  电话输入域
    @FXML
    private TextField addressField;  //学生信息  地址输入域

    @FXML
    private TextField numNameTextField;  //查询 姓名学号输入域
    @FXML
    private VBox showVBox;

    private Integer personId = null;  //当前编辑修改的学生的主键

    private ArrayList<Map> studentList = new ArrayList();  // 学生信息列表数据
    private List<OptionItem> genderList;   //性别选择列表数据
    private ObservableList<Map> observableList = FXCollections.observableArrayList();  // TableView渲染列表


    /**
     * 将学生数据集合设置到面板上显示
     */
    private void setTableViewData() {
        observableList.clear();
        for (int j = 0; j < studentList.size(); j++) {
            observableList.addAll(FXCollections.observableArrayList(studentList.get(j)));
        }
        dataTableView.setItems(observableList);
    }

    /**
     * 页面加载对象创建完成初始化方法，页面中控件属性的设置，初始数据显示等初始操作都在这里完成，其他代码都事件处理方法里
     */

    @FXML
    public void initialize() {
        // 确保每次初始化时创建新的ImageView实例
        photoImageView = new ImageView();
        photoImageView.setFitHeight(100);
        photoImageView.setFitWidth(100);
        photoButton.setGraphic(photoImageView);
        DataResponse res;
        DataRequest req = new DataRequest();
        req.add("numName", "");
        res = HttpRequestUtil.request("/api/student/getStudentList", req); //从后台获取所有学生信息列表集合
        if (res != null && res.getCode() == 0) {
            studentList = (ArrayList<Map>) res.getData();
        }
        numColumn.setCellValueFactory(new MapValueFactory<>("num"));  //设置列值工程属性
        nameColumn.setCellValueFactory(new MapValueFactory<>("name"));
        deptColumn.setCellValueFactory(new MapValueFactory<>("dept"));
        majorColumn.setCellValueFactory(new MapValueFactory<>("major"));
        classNameColumn.setCellValueFactory(new MapValueFactory<>("className"));
        cardColumn.setCellValueFactory(new MapValueFactory<>("card"));
        genderColumn.setCellValueFactory(new MapValueFactory<>("genderName"));
        birthdayColumn.setCellValueFactory(new MapValueFactory<>("birthday"));
        emailColumn.setCellValueFactory(new MapValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new MapValueFactory<>("phone"));
        addressColumn.setCellValueFactory(new MapValueFactory<>("address"));
        TableView.TableViewSelectionModel<Map> tsm = dataTableView.getSelectionModel();
        ObservableList<Integer> list = tsm.getSelectedIndices();
        list.addListener(this::onTableRowSelect);
        setTableViewData();
        genderList = HttpRequestUtil.getDictionaryOptionItemList("XBM");

        genderComboBox.getItems().addAll(genderList);
        birthdayPick.setConverter(new LocalDateStringConverter("yyyy-MM-dd"));
        showVBox.setVisible(false);
        showVBox.setManaged(false);
    }

    /**
     * 清除学生表单中输入信息
     */
    public void clearPanel() {
        personId = null;
        numField.setText("");
        nameField.setText("");
        deptField.setText("");
        majorField.setText("");
        classNameField.setText("");
        cardField.setText("");
        genderComboBox.getSelectionModel().select(-1);
        birthdayPick.getEditor().setText("");
        emailField.setText("");
        phoneField.setText("");
        addressField.setText("");
        // 清除照片显示
        photoImageView.setImage(null);
    }

    protected void changeStudentInfo() {
        Map<String,Object> form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            clearPanel();
            return;
        }
        personId = CommonMethod.getInteger(form, "personId");
        DataRequest req = new DataRequest();
        req.add("personId", personId);
        DataResponse res = HttpRequestUtil.request("/api/student/getStudentInfo", req);
        if (res.getCode() != 0) {
            MessageDialog.showDialog(res.getMsg());
            return;
        }
        form = (Map) res.getData();
        numField.setText(CommonMethod.getString(form, "num"));
        nameField.setText(CommonMethod.getString(form, "name"));
        deptField.setText(CommonMethod.getString(form, "dept"));
        majorField.setText(CommonMethod.getString(form, "major"));
        classNameField.setText(CommonMethod.getString(form, "className"));
        cardField.setText(CommonMethod.getString(form, "card"));
        genderComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(genderList, CommonMethod.getString(form, "gender")));
        birthdayPick.getEditor().setText(CommonMethod.getString(form, "birthday"));
        emailField.setText(CommonMethod.getString(form, "email"));
        phoneField.setText(CommonMethod.getString(form, "phone"));
        addressField.setText(CommonMethod.getString(form, "address"));
        
        // 清除之前的照片
        photoImageView.setImage(null);
        // 然后显示新的照片
        displayPhoto();
    }

    /**
     * 点击学生列表的某一行，根据personId ,从后台查询学生的基本信息，切换学生的编辑信息
     */

    public void onTableRowSelect(ListChangeListener.Change<? extends Integer> change) {
        changeStudentInfo();
        showVBox.setVisible(true);
        showVBox.setManaged(true);
    }

    /**
     * 点击查询按钮，从从后台根据输入的串，查询匹配的学生在学生列表中显示
     */
    @FXML
    protected void onQueryButtonClick() {
        String numName = numNameTextField.getText();
        DataRequest req = new DataRequest();
        req.add("numName", numName);
        DataResponse res = HttpRequestUtil.request("/api/student/getStudentList", req);
        if (res != null && res.getCode() == 0) {
            studentList = (ArrayList<Map>) res.getData();
            setTableViewData();
        }
        showVBox.setVisible(false);
        showVBox.setManaged(false);
    }


    /**
     * 添加新学生， 清空输入信息， 输入相关信息，点击保存即可添加新的学生
     */
    @FXML
    protected void onAddButtonClick() {
        showVBox.setVisible(true);
        showVBox.setManaged(true);
        clearPanel();
        dataTableView.getSelectionModel().clearSelection();
    }

    /**
     * 点击删除按钮 删除当前编辑的学生的数据
     */
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
        personId = CommonMethod.getInteger(form, "personId");
        DataRequest req = new DataRequest();
        req.add("personId", personId);
        DataResponse res = HttpRequestUtil.request("/api/student/studentDelete", req);
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
        dataTableView.getSelectionModel().clearSelection();
    }

    /**
     * 点击保存按钮，保存当前编辑的学生信息，如果是新添加的学生，后台添加学生
     */
    @FXML
    protected void onSaveButtonClick() {
        if (numField.getText().isEmpty()) {
            MessageDialog.showDialog("学号为空，不能修改");
            return;
        }
        Map<String,Object> form = new HashMap<>();
        form.put("num", numField.getText());
        form.put("name", nameField.getText());
        form.put("dept", deptField.getText());
        form.put("major", majorField.getText());
        form.put("className", classNameField.getText());
        form.put("card", cardField.getText());
        if (genderComboBox.getSelectionModel() != null && genderComboBox.getSelectionModel().getSelectedItem() != null)
            form.put("gender", genderComboBox.getSelectionModel().getSelectedItem().getValue());
        form.put("birthday", birthdayPick.getEditor().getText());
        form.put("email", emailField.getText());
        form.put("phone", phoneField.getText());
        form.put("address", addressField.getText());
        DataRequest req = new DataRequest();
        req.add("personId", personId);
        req.add("form", form);
        DataResponse res = HttpRequestUtil.request("/api/student/studentEditSave", req);
        if (res!= null) {
            if (res.getCode() == 0) {
                personId = CommonMethod.getIntegerFromObject(res.getData());
                MessageDialog.showDialog("提交成功！");
                onQueryButtonClick();
            }
            else {
                MessageDialog.showDialog(res.getMsg());
            }
        }
        else {
            MessageDialog.showDialog("提交失败！后端无响应");
        }

        showVBox.setVisible(false);
        showVBox.setManaged(false);
        dataTableView.getSelectionModel().clearSelection();
    }


    @FXML
    protected void onFamilyButtonClick() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/teach/javafx/family-member-panel.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setWidth(1200);
        stage.setHeight(900);
        stage.setResizable(false);
        stage.setTitle("familyMember");
        Scene scene = new Scene(root,1800,1200);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        FamilyMemberController familyMemberController = loader.getController();
        familyMemberController.initialize(personId);
        stage.showAndWait();
    }

    public void displayPhoto(){
        if (personId == null) return;
        
        DataRequest req = new DataRequest();
        req.add("fileName", "photo/" + personId + ".jpg");  //个人照片显示
        byte[] bytes = HttpRequestUtil.requestByteData("/api/base/getFileByteData", req);
        if (bytes != null) {
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            Image img = new Image(in);
            photoImageView.setImage(img);
        } else {
            // 如果没有照片，显示默认图片
            photoImageView.setImage(null);
        }
    }

    @FXML
    public void onPhotoButtonClick() {
        if (personId == null) {
            MessageDialog.showDialog("请先选择一个学生！");
            return;
        }
        
        FileChooser fileDialog = new FileChooser();
        fileDialog.setTitle("图片上传");
        fileDialog.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG 文件", "*.jpg"));
        File file = fileDialog.showOpenDialog(null);
        if (file == null)
            return;
        
        if (!file.exists() || !file.canRead()) {
            MessageDialog.showDialog("无法读取选择的文件！");
            return;
        }
        
        if (file.length() > 5 * 1024 * 1024) { // 5MB限制
            MessageDialog.showDialog("文件太大，请选择小于5MB的图片！");
            return;
        }
        
        // 添加时间戳防止缓存问题
        String timestamp = String.valueOf(System.currentTimeMillis());
        String remoteFile = "photo/" + personId + ".jpg?t=" + timestamp;
        
        try {
            DataResponse res = HttpRequestUtil.uploadFile("/api/base/uploadPhoto", file.getPath(), remoteFile);
            
            if (res.getCode() == 0) {
                MessageDialog.showDialog("上传成功！");
                // 清除之前的照片
                photoImageView.setImage(null);
                // 显示新照片
                displayPhoto();
            } else {
                MessageDialog.showDialog("上传失败: " + res.getMsg());
            }
        } catch (Exception e) {
            MessageDialog.showDialog("上传过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    public void onImportFeeButtonClick(){
        FileChooser fileDialog = new FileChooser();
        fileDialog.setTitle("前选择消费数据表");
        fileDialog.setInitialDirectory(new File("C:/"));
        fileDialog.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XLSX 文件", "*.xlsx"));
        File file = fileDialog.showOpenDialog(null);
        String paras = "personId="+personId;
        DataResponse res =HttpRequestUtil.importData("/api/student/importFeeData",file.getPath(),paras);
        if(res.getCode() == 0) {
            MessageDialog.showDialog("上传成功！");
        }
        else {
            MessageDialog.showDialog(res.getMsg());
        }
    }

}
