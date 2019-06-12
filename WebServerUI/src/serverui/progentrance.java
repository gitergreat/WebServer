package serverui;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import serverprog.authority;
import serverprog.createSFTPChannel;

//程序的入口

public class progentrance extends Application{

    public Stage stage;
    //主页面
    private maincontroller main;
    private logincontroller login;
    public createSFTPChannel createSFTPChannel= new createSFTPChannel();
    public authority authority = new authority();

    //启动时加载登录页面
    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.setResizable(false);
        loginpage();
        stage.show();
    }

    //显示登录页面
    public void loginpage(){
        try {
            login = (logincontroller) replaceSceneContent("fxmldir/LOGINPAGE.fxml","fxmldir/beautify.css");
            login.setApp(this);
            this.stage.setTitle("登录.Powehi服务器管理器");
        } catch (Exception ex) {
            Logger.getLogger(maincontroller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //显示主页面
    public void mainpage(){
        try {
            maincontroller main = (maincontroller) replaceSceneContent("fxmldir/MAINPAGE.fxml","fxmldir/beautify.css");
            main.setApp(this);
            this.stage.setTitle("主页.Powehi服务器管理器");
            main.downloadfile();
            main.displaylog();
        } catch (Exception ex) {
            Logger.getLogger(progentrance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //加载页面函数
    private Initializable replaceSceneContent(String fxml,String css) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        InputStream in = progentrance.class.getResourceAsStream(fxml);
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(progentrance.class.getResource(fxml));
        AnchorPane page;
        try {
            page = (AnchorPane) loader.load(in);
        } finally {
            in.close();
        }
        Scene scene = new Scene(page, 671, 422);
        scene.getStylesheets().add(
                getClass().getResource(css)
                        .toExternalForm());
        stage.setScene(scene);
        stage.sizeToScene();
        return (Initializable) loader.getController();
    }

    public static void main(String[] args) {
        launch(args);
    }
    /*public void reMain(String[] strs){
        main.loadLayout(strs);
    }*/
}
