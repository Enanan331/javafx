package com.teach.javafx.controller;

import com.teach.javafx.MainApplication;
import com.teach.javafx.controller.base.LocalDateStringConverter;
import com.teach.javafx.controller.base.ToolController;
import com.teach.javafx.request.*;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TeacherController 登录交互控制类 对应 teacher_panel.fxml  对应于教师管理的后台业务处理的控制器，主要获取数据和保存数据的方法不同
 *
 * @FXML 属性 对应fxml文件中的
 * @FXML 方法 对应于fxml文件中的 on***Click的属性
 */
public class TeacherController extends ToolController {
    private ImageView photoImageView;
    @FXML
    private TableView<Map> dataTableView;  //教师信息表
    @FXML
    private TableColumn<Map, String> numColumn;   //教师信息表 编号列
    @FXML
    private TableColumn<Map, String> nameColumn; //教师信息表 名称列
    @FXML
    private TableColumn<Map, String> deptColumn;  //教师信息表 院系列
    @FXML
    private TableColumn<Map, String> titleColumn; //教师信息表 职称列
    @FXML
    private TableColumn<Map, String> degreeColumn; //教师信息表 学位列
    @FXML
    private TableColumn<Map, String> cardColumn; //教师信息表 证件号码列
    @FXML
    private TableColumn<Map, String> genderColumn; //教师信息表 性别列
    @FXML
    private TableColumn<Map, String> birthdayColumn; //教师信息表 出生日期列
    @FXML
    private TableColumn<Map, String> emailColumn; //教师信息表 邮箱列
    @FXML
    private TableColumn<Map, String> phoneColumn; //教师信息表 电话列
    @FXML
    private TableColumn<Map, String> addressColumn;//教师信息表 地址列
    @FXML
    private Button photoButton;  //照片显示和上传按钮

    @FXML
    private TextField numField; //教师信息  工号输入域
    @FXML
    private TextField nameField;  //教师信息  名称输入域
    @FXML
    private TextField deptField; //教师信息  院系输入域
    @FXML
    private TextField titleField; //教师信息  职称输入域
    @FXML
    private TextField degreeField; //教师信息  学位输入域
    @FXML
    private TextField cardField; //教师信息  证件号码输入域
    @FXML
    private ComboBox<OptionItem> genderComboBox;  //教师信息  性别输入域
    @FXML
    private DatePicker birthdayPick;  //教师信息  出生日期选择域
    @FXML
    private TextField emailField;  //教师信息  邮箱输入域
    @FXML
    private TextField phoneField;   //教师信息  电话输入域
    @FXML
    private TextField addressField;  //教师信息  地址输入域

    @FXML
    private TextField numNameTextField;  //查询 姓名工号输入域
    @FXML
    private VBox showVBox;

    private Integer personId = null;  //当前编辑修改的教师的主键

    private ArrayList<Map> teacherList = new ArrayList();  // 教师信息列表数据
    private List<OptionItem> genderList;   //性别选择列表数据
    private ObservableList<Map> observableList = FXCollections.observableArrayList();  // TableView渲染列表


    /**
     * 将教师数据集合设置到面板上显示
     */
    private void setTableViewData() {
        observableList.clear();
        for (int j = 0; j < teacherList.size(); j++) {
            observableList.addAll(FXCollections.observableArrayList(teacherList.get(j)));
        }
        dataTableView.setItems(observableList);
    }

    /**
     * 页面加载对象创建完成初始化方法，页面中控件属性的设置，初始数据显示等初始操作都在这里完成，其他代码都事件处理方法里
     */

    @FXML
    public void initialize() {
        photoImageView = new ImageView();
        photoImageView.setFitHeight(100);
        photoImageView.setFitWidth(100);
        photoButton.setGraphic(photoImageView);
        DataResponse res;
        DataRequest req = new DataRequest();
        req.add("numName", "");
        res = HttpRequestUtil.request("/api/teacher/getTeacherList", req); //从后台获取所有教师信息列表集合
        if (res != null && res.getCode() == 0) {
            teacherList = (ArrayList<Map>) res.getData();
        }
        numColumn.setCellValueFactory(new MapValueFactory<>("num"));  //设置列值工程属性
        nameColumn.setCellValueFactory(new MapValueFactory<>("name"));
        deptColumn.setCellValueFactory(new MapValueFactory<>("dept"));
        titleColumn.setCellValueFactory(new MapValueFactory<>("title"));
        degreeColumn.setCellValueFactory(new MapValueFactory<>("degree"));
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
     * 清除教师表单中输入信息
     */
    public void clearPanel() {
        personId = null;
        numField.setText("");
        nameField.setText("");
        deptField.setText("");
        titleField.setText("");
        degreeField.setText("");
        cardField.setText("");
        genderComboBox.getSelectionModel().select(-1);
        birthdayPick.getEditor().setText("");
        emailField.setText("");
        phoneField.setText("");
        addressField.setText("");
    }

    protected void changeTeacherInfo() {
        Map<String,Object> form = dataTableView.getSelectionModel().getSelectedItem();
        if (form == null) {
            clearPanel();
            return;
        }
        personId = CommonMethod.getInteger(form, "personId");
        DataRequest req = new DataRequest();
        req.add("personId", personId);
        DataResponse res = HttpRequestUtil.request("/api/teacher/getTeacherInfo", req);
        if (res.getCode() != 0) {
            MessageDialog.showDialog(res.getMsg());
            return;
        }
        form = (Map) res.getData();
        numField.setText(CommonMethod.getString(form, "num"));
        nameField.setText(CommonMethod.getString(form, "name"));
        deptField.setText(CommonMethod.getString(form, "dept"));
        titleField.setText(CommonMethod.getString(form, "title"));
        degreeField.setText(CommonMethod.getString(form, "degree"));
        cardField.setText(CommonMethod.getString(form, "card"));
        genderComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(genderList, CommonMethod.getString(form, "gender")));
        birthdayPick.getEditor().setText(CommonMethod.getString(form, "birthday"));
        emailField.setText(CommonMethod.getString(form, "email"));
        phoneField.setText(CommonMethod.getString(form, "phone"));
        addressField.setText(CommonMethod.getString(form, "address"));
        displayPhoto();
    }

    /**
     * 点击教师列表的某一行，根据personId ,从后台查询教师的基本信息，切换教师的编辑信息
     */

    public void onTableRowSelect(ListChangeListener.Change<? extends Integer> change) {
        changeTeacherInfo();
        showVBox.setVisible(true);
        showVBox.setManaged(true);
    }

    /**
     * 点击查询按钮，从从后台根据输入的串，查询匹配的教师在教师列表中显示
     */
    @FXML
    protected void onQueryButtonClick() {
        String numName = numNameTextField.getText();
        DataRequest req = new DataRequest();
        req.add("numName", numName);
        DataResponse res = HttpRequestUtil.request("/api/teacher/getTeacherList", req);
        if (res != null && res.getCode() == 0) {
            teacherList = (ArrayList<Map>) res.getData();
            setTableViewData();
        }
    }

    /**
     * 添加新教师， 清空输入信息， 输入相关信息，点击保存即可添加新的教师
     */
    @FXML
    protected void onAddButtonClick() {
        clearPanel();
        showVBox.setVisible(true);
        showVBox.setManaged(true);
    }

    /**
     * 点击删除按钮 删除当前编辑的教师的数据
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
        DataResponse res = HttpRequestUtil.request("/api/teacher/teacherDelete", req);
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
    }

    /**
     * 点击保存按钮，保存当前编辑的教师信息，如果是新添加的教师，后台添加教师
     */
    @FXML
    protected void onSaveButtonClick() {
        if (numField.getText().isEmpty()) {
            MessageDialog.showDialog("工号为空，不能修改");
            return;
        }
        Map<String,Object> form = new HashMap<>();
        form.put("num", numField.getText());
        form.put("name", nameField.getText());
        form.put("dept", deptField.getText());
        form.put("title", titleField.getText());
        form.put("degree", degreeField.getText());
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
        DataResponse res = HttpRequestUtil.request("/api/teacher/teacherEditSave", req);
        if (res.getCode() == 0) {
            personId = CommonMethod.getIntegerFromObject(res.getData());
            MessageDialog.showDialog("提交成功！");
            onQueryButtonClick();
        } else {
            MessageDialog.showDialog(res.getMsg());
        }
        showVBox.setVisible(false);
        showVBox.setManaged(false);
    }

    /**
     * doNew() doSave() doDelete() 重写 ToolController 中的方法， 实现选择 新建，保存，删除 对教师的增，删，改操作
     */
    public void doNew() {
        clearPanel();
    }

    public void doSave() {
        onSaveButtonClick();
    }

    public void doDelete() {
        onDeleteButtonClick();
    }

    /**
     * 导出教师信息表的示例 重写ToolController 中的doExport 这里给出了一个导出教师基本信息到Excl表的示例， 后台生成Excl文件数据，传回前台，前台将文件保存到本地
     */
    public void doExport() {
        String numName = numNameTextField.getText();
        DataRequest req = new DataRequest();
        req.add("numName", numName);
        byte[] bytes = HttpRequestUtil.requestByteData("/api/teacher/getTeacherListExcl", req);
        if (bytes != null) {
            try {
                FileChooser fileDialog = new FileChooser();
                fileDialog.setTitle("选择保存的文件");
                fileDialog.setInitialDirectory(new File("C:/"));
                fileDialog.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("XLSX 文件", "*.xlsx"));
                File file = fileDialog.showSaveDialog(null);
                if (file != null) {
                    FileOutputStream out = new FileOutputStream(file);
                    out.write(bytes);
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void onImportButtonClick() {
        FileChooser fileDialog = new FileChooser();
        fileDialog.setTitle("选择教师数据表");
        fileDialog.setInitialDirectory(new File("D:/"));
        fileDialog.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XLSX 文件", "*.xlsx"));
        File file = fileDialog.showOpenDialog(null);
        String paras = "";
        DataResponse res = HttpRequestUtil.importData("/api/teacher/importTeacherData", file.getPath(), paras);
        if (res.getCode() == 0) {
            MessageDialog.showDialog("上传成功！");
        } else {
            MessageDialog.showDialog(res.getMsg());
        }
    }

    @FXML
    protected void onFamilyButtonClick() {
        DataRequest req = new DataRequest();
        req.add("personId", personId);
        DataResponse res = HttpRequestUtil.request("/api/teacher/getFamilyMemberList", req);
        if (res.getCode() != 0) {
            MessageDialog.showDialog(res.getMsg());
            return;
        }
        List<Map> familyList = (List<Map>) res.getData();
        ObservableList<Map> oList = FXCollections.observableArrayList(familyList);
        Scene scene = null, pScene = null;
        Stage stage;
        stage = new Stage();
        TableView<Map> table = new TableView<>(oList);
        table.setEditable(true);
        TableColumn<Map, String> relationColumn = new TableColumn<>("关系");
        relationColumn.setCellValueFactory(new MapValueFactory("relation"));
        relationColumn.setCellFactory(TextFieldTableCell.<Map>forTableColumn());
        relationColumn.setOnEditCommit(event -> {
            TableView tempTable = event.getTableView();
            Map tempEntity = (Map) tempTable.getItems().get(event.getTablePosition().getRow());
            tempEntity.put("relation",event.getNewValue());
        });
        table.getColumns().add(relationColumn);
        TableColumn<Map, String> nameColumn = new TableColumn<>("姓名");
        nameColumn.setCellValueFactory(new MapValueFactory<>("name"));
        nameColumn.setCellFactory(TextFieldTableCell.<Map>forTableColumn());
        table.getColumns().add(nameColumn);
        TableColumn<Map, String> genderColumn = new TableColumn<>("性别");
        genderColumn.setCellValueFactory(new MapValueFactory<>("gender"));
        genderColumn.setCellFactory(TextFieldTableCell.<Map>forTableColumn());
        table.getColumns().add(genderColumn);
        TableColumn<Map, String> ageColumn = new TableColumn<>("年龄");
        ageColumn.setCellValueFactory(new MapValueFactory<>("age"));
        ageColumn.setCellFactory(TextFieldTableCell.<Map>forTableColumn());
        table.getColumns().add(ageColumn);
        TableColumn<Map, String> unitColumn = new TableColumn<>("单位");
        unitColumn.setCellValueFactory(new MapValueFactory<>("unit"));
        unitColumn.setCellFactory(TextFieldTableCell.<Map>forTableColumn());
        table.getColumns().add(unitColumn);
        BorderPane root = new BorderPane();
        FlowPane flowPane = new FlowPane();
        Button obButton = new Button("确定");
        obButton.setOnAction(event -> {
            for(Map map: table.getItems()) {
                System.out.println("map:"+map);
            }
            stage.close();
        });
        flowPane.getChildren().add(obButton);
        root.setCenter(table);
        root.setBottom(flowPane);
        scene = new Scene(root, 260, 140);
        stage.initOwner(MainApplication.getMainStage());
        stage.initModality(Modality.NONE);
        stage.setAlwaysOnTop(true);
        stage.setScene(scene);
        stage.setTitle("家庭信息录入对话框！");
        stage.setOnCloseRequest(event -> {
            MainApplication.setCanClose(true);
        });
        stage.showAndWait();
    }
    public void displayPhoto(){
        DataRequest req = new DataRequest();
        req.add("fileName", "photo/" + personId + ".jpg");  //个人照片显示
        byte[] bytes = HttpRequestUtil.requestByteData("/api/base/getFileByteData", req);
        if (bytes != null) {
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            Image img = new Image(in);
            photoImageView.setImage(img);
        }
    }

    @FXML
    public void onPhotoButtonClick(){
        FileChooser fileDialog = new FileChooser();
        fileDialog.setTitle("图片上传");
        fileDialog.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG 文件", "*.jpg"));
        File file = fileDialog.showOpenDialog(null);
        if(file == null)
            return;
        DataResponse res =HttpRequestUtil.uploadFile("/api/base/uploadPhoto",file.getPath(),"photo/" + personId + ".jpg");
        if(res.getCode() == 0) {
            MessageDialog.showDialog("上传成功！");
            displayPhoto();
        }
        else {
            MessageDialog.showDialog(res.getMsg());
        }
    }
    @FXML
    public void onImportFeeButtonClick(){
        FileChooser fileDialog = new FileChooser();
        fileDialog.setTitle("选择消费数据表");
        fileDialog.setInitialDirectory(new File("C:/"));
        fileDialog.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XLSX 文件", "*.xlsx"));
        File file = fileDialog.showOpenDialog(null);
        String paras = "personId="+personId;
        DataResponse res =HttpRequestUtil.importData("/api/teacher/importFeeData",file.getPath(),paras);
        if(res.getCode() == 0) {
            MessageDialog.showDialog("上传成功！");
        }
        else {
            MessageDialog.showDialog(res.getMsg());
        }
    }
}