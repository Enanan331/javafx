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

public class HonorEditController {
    @FXML
    private ComboBox<OptionItem> studentComboBox;
    private List<OptionItem> studentList;
    @FXML
    private TextField honorNameField;
    private HonorController honorController = null;
    private Integer honorId = null;

    @FXML
    private void initialize() {
    }

    @FXML
    public void okButtonClick() {
        Map<String, Object> data = new HashMap<>();
        OptionItem op;
        op = studentComboBox.getSelectionModel().getSelectedItem();
        if (op != null) {
            data.put("personId", Integer.parseInt(op.getValue()));
        }
        data.put("honorId", honorId);
        data.put("honorName", honorNameField.getText());
        honorController.doClose("ok", data);
    }

    public void init() {
        DataRequest req = new DataRequest(); OptionItem item=new OptionItem(null,"0","请选择");
        studentComboBox.setOnMouseClicked(e->{
            studentList= HttpRequestUtil.requestOptionItemList("/api/honor/getStudentItemOptionList",req);
            studentComboBox.getItems().clear();
            studentComboBox.getItems().add(item);
            studentComboBox.getItems().addAll(studentList);
        });
    }

    @FXML
    public void cancelButtonClick() {
        honorController.doClose("cancel", null);
    }

    public void setHonorController(HonorController honorController) {
        this.honorController = honorController;
    }

   /* public void showDialog(Map data) {
        System.out.println(data);
        if (data == null) {
            honorId = null;
            studentComboBox.getSelectionModel().select(-1);
            studentComboBox.setDisable(false);
            markField.setText("");
        } else {
            honorId = CommonMethod.getInteger(data, "honorId");
            System.out.println(studentList);
            studentComboBox.getSelectionModel().select(CommonMethod.getOptionItemIndexByValue(studentList, CommonMethod.getString(data, "personId")));
            studentComboBox.setDisable(true);
            markField.setText(CommonMethod.getString(data, "mark"));
        }
    }

    */
   public void showDialog(Map<String, Object> data) {
       System.out.println("传入的数据: " + data);

       if (data == null) {
           honorId = null;
           studentComboBox.getSelectionModel().select(-1);
           studentComboBox.setDisable(false);
           honorNameField.setText("");
       } else {
           honorId = CommonMethod.getInteger(data, "honorId");
           String studentNum = CommonMethod.getString(data, "studentNum"); // 获取学号

           // 遍历 studentList，匹配学号
           int selectedIndex = -1;
           for (int i = 0; i < studentList.size(); i++) {
               String option = studentList.get(i).toString();
               if (option.startsWith(studentNum + "-")) {
                   selectedIndex = i;
                   break;
               }
           }

           studentComboBox.getSelectionModel().select(selectedIndex);
           studentComboBox.setDisable(true);
           honorNameField.setText(CommonMethod.getString(data, "honorName"));
       }
   }
}


