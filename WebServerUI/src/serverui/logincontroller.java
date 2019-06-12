package serverui;


import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import serverprog.serverconfig;

public class logincontroller implements Initializable,setApp {

    private progentrance application;
    private Alert alert = new Alert(Alert.AlertType.ERROR);
    private serverconfig serverconfig;

    @FXML
    private PasswordField password;

    @FXML
    private TextField ipaddr;

    @FXML
    private TextField username;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @Override
    public void setApp(progentrance application) {
        this.application = application;
    }

    //点击登录按钮跳转至主页面
    @FXML
    void sign_gotomainpage(ActionEvent event) throws Exception{
        application.authority.inputInfo(username.getText(),password.getText(),ipaddr.getText(),22);
        if(!(ipaddr == null ||password == null || username == null)) {
            if (application.createSFTPChannel.getSession(application.authority, 5000) == null) {
                alert.setTitle("登录失败.");
                alert.setHeaderText("无法连接至" + ipaddr.getText() + ".");
                alert.setContentText("请检查ip地址、用户名及密码.");
                alert.showAndWait();
            } else {
                store();
                application.mainpage();
            }
        }
    }
    @FXML
    void relogin(ActionEvent event) throws FileNotFoundException {
        Readfile readfile = new Readfile();
        String[] stringArr = readfile.readfile(System.getProperty("user.dir") + "\\configuser.ini");
        ipaddr.setText(stringArr[0]);
        username.setText(stringArr[1]);
        password.setText(stringArr[2]);
    }
    void store()throws FileNotFoundException{
        serverconfig = new serverconfig();
        serverconfig.remember(ipaddr.getText(),username.getText(),password.getText());
    }
}
