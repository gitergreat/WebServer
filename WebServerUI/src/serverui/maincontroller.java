package serverui;


import java.util.regex.*;
import com.jcraft.jsch.ChannelExec;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.*;
import serverprog.serverconfig;
import com.jcraft.jsch.JSchException;
import serverprog.md5;

public class maincontroller implements Initializable,setApp {

    private progentrance application;
    private Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
    private Alert alert2 = new Alert(Alert.AlertType.ERROR);
    private String path;
    public File file_uploading;
    private File file_log;
    private File file_ipaddress;
    private File file_error;
    private File file_statics;
    private Readfile readfile;

    @FXML
    private TextField filepath;

    @FXML
    private TextField serverpath;

    @FXML
    private TextField defaultpage;

    @FXML
    private TextField port;

    @FXML
    private TextField rootdir;

    @FXML
    private TextArea textarea_statics;

    @FXML
    private TextArea textarea_error;

    @FXML
    private TextArea textarea_log = new TextArea();

    @FXML
    private TextArea textarea_ipaddress;

    @FXML
    void choosefile(ActionEvent event)throws Exception {

            FileChooser fileChooser = new FileChooser();

            fileChooser.setTitle("选择文件");

            file_uploading = fileChooser.showOpenDialog(application.stage);

            //文件路径
            path = file_uploading.getAbsolutePath();
            filepath.setText(path);

    }

    void downloadfile() {
        try {
            ChannelSftp channelSftp = application.createSFTPChannel.getChannel();
            channelSftp.get("WebServer/log/access.log",(System.getProperty("user.dir")+"\\access.log"));
            file_log = new File(System.getProperty("user.dir")+"\\access.log");
            channelSftp.get("WebServer/log/error.log",(System.getProperty("user.dir")+"\\error.log"));
            file_error = new File(System.getProperty("user.dir")+"\\error.log");
            channelSftp.get("WebServer/ipinfo/ip_address.log",(System.getProperty("user.dir")+"\\ip_address.log"));
            file_ipaddress = new File(System.getProperty("user.dir")+"\\ip_address.log");
            channelSftp.get("WebServer/ipinfo/statics.log",(System.getProperty("user.dir")+"\\statics.log"));
            file_statics = new File(System.getProperty("user.dir")+"\\statics.log");
        }catch (SftpException e ){
            alert2.setTitle("错误");
            alert2.setHeaderText("无法获取服务器日志文件");
            alert2.setContentText("日志文件不存在或传输发生错误");
            alert2.showAndWait();
        }
    }

    void displaylog(){
        String[] stringArr = readfile.readfile(file_log.getAbsolutePath());
        for(int i = 0 ; i < stringArr.length ; i ++)
        {
            textarea_log.appendText(stringArr[i]+"\n");
        }
        textarea_log.setEditable(false);
        textarea_log.setWrapText(true);
        stringArr = readfile.readfile(file_error.getAbsolutePath());
        for(int i = 0 ; i < stringArr.length ; i ++)
        {
            textarea_error.appendText(stringArr[i]+"\n");
        }
        textarea_error.setEditable(false);
        textarea_error.setWrapText(true);
        stringArr = readfile.readfile(file_ipaddress.getAbsolutePath());
        for(int i = 0 ; i < stringArr.length ; i ++)
        {
            textarea_ipaddress.appendText(stringArr[i]+"\n");
        }
        textarea_ipaddress.setEditable(false);
        textarea_ipaddress.setWrapText(true);
        stringArr = readfile.readfile(file_statics.getAbsolutePath());
        for(int i = 0 ; i < stringArr.length ; i ++)
        {
            textarea_statics.appendText(stringArr[i]+"\n");
        }
        textarea_statics.setEditable(false);
        textarea_statics.setWrapText(true);
    }

    @FXML
    void uploading(ActionEvent event) {
        try {
            ChannelSftp channelSftp = application.createSFTPChannel.getChannel();
            channelSftp.put(path,
                    serverpath.getText(), channelSftp.OVERWRITE);
            int index = path.lastIndexOf('/');
            String filename = path.substring(index+1);
            String cmd = "chmod 444 " + serverpath + "/" + filename;
            index = filename.lastIndexOf('.');
            String suffix = filename.substring(index);
            if(suffix.equals(".html")) {
                md5 md5 = new md5(file_uploading);
                String calmd5str = md5.getMD5();
                serverconfig serverconfig = new serverconfig();
                channelSftp.put(serverconfig.md5(filename, calmd5str), serverpath.getText(), channelSftp.OVERWRITE);
            }
            ChannelExec channelExec = (ChannelExec) application.createSFTPChannel.session.openChannel("exec");
            channelExec.setCommand(cmd);
            channelExec.connect();
            channelExec.disconnect();
            channelSftp.disconnect();
            alert1.setContentText("上传成功");
            alert1.setHeaderText("");
            alert1.setTitle("上传...");
            alert1.showAndWait();
        }catch (Exception e){
            alert2.setTitle("上传...");
            alert2.setHeaderText("上传发生错误");
            alert2.setContentText("上传失败");
            alert2.showAndWait();
        }
    }

    @FXML
    void finishconfig(ActionEvent event)throws Exception {
    try{
        if(port.getText().equals("") || rootdir.getText().equals("") || defaultpage.getText().equals("")){
            alert2.setTitle("上传...");
            alert2.setHeaderText("上传配置文件失败");
            alert2.setContentText("输入配置不可为空");
            alert2.showAndWait();
        }else {
            ChannelSftp channelSftp = application.createSFTPChannel.getChannel();
            serverconfig serverconfig = new serverconfig();
            serverconfig.getinfo(port.getText(), rootdir.getText(),defaultpage.getText());
            channelSftp.put((System.getProperty("user.dir") + "\\config.ini"), "WebServer/", channelSftp.OVERWRITE);

            //上传配置文件成功之后需要重启服务器
            //远程获取服务器运行的进程号，再将其关闭后重启
            ChannelExec channelExec = (ChannelExec) application.createSFTPChannel.session.openChannel("exec");
            channelExec.setCommand("ps -aux");
            channelExec.connect();
            InputStream in = channelExec.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
            String buff = null;
            int count = 0;
            String[] recieveString = new String[1024];
            while ((buff = reader.readLine()) != null) {
                recieveString[count++] = buff;
                //System.out.println(buff);
            }
            for (int i = 0; i < count; i++) {
                System.out.println(recieveString[i]);
            }
            int indexOfCommand = recieveString[0].lastIndexOf(" ");
            indexOfCommand++;
            //获取进程号
            int pid = 0;
            for (int i = 1; i < count; i++) {
                if (recieveString[i].substring(indexOfCommand, recieveString[i].length()).equals("java -jar WebServer.jar Main")) {
                    System.out.println("所求的：" + recieveString[i]);
                    Pattern pattern = Pattern.compile("[0-9]+");
                    Matcher matcher = pattern.matcher(recieveString[i]);
                    if(matcher.find()){
                        int gropu = matcher.groupCount();
                        pid = Integer.parseInt(matcher.group(0));
                        System.out.println("pid="+pid);
                    }
                    else System.out.println("error!");
                }
            }
            reader.close();
            //关闭进程并重启
            ChannelExec channelExec1 = (ChannelExec) application.createSFTPChannel.session.openChannel("exec");
            String s = "kill " + pid+"";
            channelExec1.setCommand(s);//+";"+"nohup java -jar WebServer.jar Main");
            channelExec1.connect();
            ChannelExec channelExec3 = (ChannelExec) application.createSFTPChannel.session.openChannel("exec");
            channelExec3.setCommand("cd ~/WebServer;nohup java -jar WebServer.jar Main");
            channelExec3.connect();

            channelExec3.disconnect();
            channelExec1.disconnect();
            channelExec.disconnect();
            channelSftp.disconnect();
            alert1.setTitle("上传...");
            alert1.setHeaderText("上传配置文件成功");
            alert1.setContentText("配置设置成功");
            alert1.showAndWait();
        }
    }catch (JSchException e){
        alert2.setTitle("上传...");
        alert2.setHeaderText("上传配置文件失败");
        alert2.setContentText("配置设置失败");
        alert2.showAndWait();
    }
    }

    @FXML
    void exittheserver(ActionEvent event) throws Exception{
        application.createSFTPChannel.closechannel();
        application.stage.close();
        return;
    }

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
    /**
     * 根据传进来的模块重新绘制主界面：
     * 拿到main_border清空子元素，然后根据数据挨个把模块添加进来并重新绘制
     */
    void loadLayout(String[] strs) {
        for (String str : strs) {
            URL layout = this.application.getClass().getResource(str);
            if (layout != null) {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    InputStream in = maincontroller.class.getResourceAsStream(str);
                    loader.setBuilderFactory(new JavaFXBuilderFactory());
                    loader.setLocation(maincontroller.class.getResource(str));
                    Node page;
                    try {
                        page = (Node) loader.load(in);
                    } finally {
                        in.close();
                    }
                   // main_border.getChildren().add(page);
                    setApp setapp = (setApp)loader.getController();
                    setapp.setApp(this.application);
                } catch (IOException ex) {
                    Logger.getLogger(maincontroller.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}