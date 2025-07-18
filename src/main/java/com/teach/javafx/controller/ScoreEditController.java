package com.teach.javafx.controller;

import com.teach.javafx.request.DataRequest;
import com.teach.javafx.request.HttpRequestUtil;
import com.teach.javafx.request.OptionItem;
import com.teach.javafx.util.CommonMethod;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MessageController 登录交互控制类 对应 base/message-dialog.fxml
 *  @FXML  属性 对应fxml文件中的
 *  @FXML 方法 对应于fxml文件中的 on***Click的属性
 */

public class ScoreEditController {
    @FXML
    private ComboBox<OptionItem> studentComboBox;
    private List<OptionItem> studentList;
    @FXML
    private ComboBox<OptionItem> courseComboBox;
    private List<OptionItem> courseList;
    @FXML
    private TextField markField;
    private ScoreTableController scoreTableController= null;
    private Integer scoreId= null;
    @FXML
    public void initialize() {
    }

    @FXML
    public void okButtonClick(){
        Map<String,Object> data = new HashMap<>();
        OptionItem op;
        op = studentComboBox.getSelectionModel().getSelectedItem();
        if(op != null) {
            data.put("personId",Integer.parseInt(op.getValue()));
        }
        op = courseComboBox.getSelectionModel().getSelectedItem();
        if(op != null) {
            data.put("courseId", Integer.parseInt(op.getValue()));
        }
        data.put("scoreId",scoreId);
        data.put("mark",markField.getText());
        scoreTableController.doClose("ok",data);
    }
    @FXML
    public void cancelButtonClick(){
        scoreTableController.doClose("cancel",null);
    }

    public void setScoreTableController(ScoreTableController scoreTableController) {
        this.scoreTableController = scoreTableController;
    }
    public void init(){
        DataRequest req = new DataRequest(); OptionItem item=new OptionItem(null,"0","请选择");
        studentComboBox.setOnMouseClicked(e->{
            studentList= HttpRequestUtil.requestOptionItemList("/api/honor/getStudentItemOptionList",req);
            studentComboBox.getItems().clear();
            studentComboBox.getItems().add(item);
            studentComboBox.getItems().addAll(studentList);
        });
        courseComboBox.setOnMouseClicked(e->{
            courseList=HttpRequestUtil.requestOptionItemList("/api/score/getCourseItemOptionList",req);
            courseComboBox.getItems().clear();
            courseComboBox.getItems().add(item);
            courseComboBox.getItems().addAll(courseList);
        });
    }
    public void showDialog(Map data){
        if(data == null) {
            scoreId = null;
            studentComboBox.getSelectionModel().select(-1);
            courseComboBox.getSelectionModel().select(-1);
            studentComboBox.setDisable(false);
            courseComboBox.setDisable(false);
            markField.setText("");
        }else {
            scoreId = CommonMethod.getInteger(data,"scoreId");
            studentComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(studentList, CommonMethod.getString(data, "personId")));
            courseComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(courseList, CommonMethod.getString(data, "courseId")));
            studentComboBox.setDisable(true);
            courseComboBox.setDisable(true);
            markField.setText(CommonMethod.getString(data, "mark"));
        }
    }
}
