package MAIN;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.collections.*;

public class controller {
    private TableView<Search> table = new TableView<Search>();
    private final ObservableList<Search> data
            = FXCollections.observableArrayList();

    private Search search;
    //ObservableList<TableColumn> observableList = search.getColumns();
    @FXML
    private TextField num;

    @FXML
    private TextField name;
    Alert information = new Alert(Alert.AlertType.INFORMATION);

    @FXML
    void searchbyname(ActionEvent event) {
        information.setContentText(search.findName(name.getText()));
        information.setTitle("information"); //设置标题，不设置默认标题为本地语言的information
        information.setHeaderText("search.findName(name.getText())"); //设置头标题，默认标题为本地语言的information
        //Button infor = new Button("show Information");
        //infor.setOnAction((ActionEvent)->{
            information.showAndWait(); //显示弹窗，同时后续代码等挂起
        };

    @FXML
    void searchbynum(ActionEvent event) {
        search.findPhoneNum(num.getText());

    }

}
