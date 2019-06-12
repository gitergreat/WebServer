import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Main {
    public static void main(String[] args) throws IOException {

        //运行端口号
        int port = 8080;

        //文件存储目录
        String WEB_ROOT = "webroot";

        //网站默认文件路径
        String defaultFilePath = "/test/a.html";
        
        //访问IP统计路径
        String defaultCountPath = "ipinfo";

        //日志路径
        String defaultLogPath = "log";

        //TCP端口
        ServerSocket serverSocket = null;
        
        //日期
    	SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z", Locale.US);

        File file = new File("config.ini");//查询初始化配置文件位置
        if (file.exists()) {
            //如果配置文件存在，则直接读取配置文件内容设置端口号与运行主目录
            MyFileReader fileReader = new MyFileReader();
            fileReader.setFilename("config.ini");
            String fileStirng = fileReader.getFileString();
            String[] dividedFileString = fileStirng.split("\r\n");
            System.out.println("WebServer initializing...");
            //读取config.ini文件内容，将服务器程序初始化设置
            for(int i=0;i<dividedFileString.length;i++){
                String[] tempStirng = dividedFileString[i].split("=");
                for(int j=0;j<tempStirng.length;j++) {
                    tempStirng[j] = tempStirng[j].trim();
                }
                if(tempStirng[0].equals("port")){
                    port = Integer.parseInt(tempStirng[1]);
                    //System.out.println("port="+port);
                }
                if(tempStirng[0].equals("WEBROOT")){
                    WEB_ROOT = tempStirng[1];
                    //System.out.println("WEB_ROOT:"+WEB_ROOT);
                }
                if(tempStirng[0].equals("deafaultPage")){
                	defaultFilePath = tempStirng[1];
                    //System.out.println("defaultFilePath:"+defalutFilePath);
                }
                if(tempStirng[0].equals("countPath")){
                	defaultCountPath = tempStirng[1];
                }
                if(tempStirng[0].equals("logPath")){
                	defaultLogPath = tempStirng[1];  
                }

            }
        }
        //如果找不到默认配置文件，则将端口号与Webroot设置为默认值，并创建默认的配置文件
        else {
            System.out.println("配置文件丢失，即将生成默认配置文件");
            String writeString  = "port = 8080\r\n"
            					+ "WEBROOT = webroot\r\n"
            					+ "deafaultPage = /test/a.html\r\n"
            					+ "countPath = ipinfo\r\n"
            					+ "logPath = log\r\n";
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(writeString.getBytes());
            System.out.println("默认配置文件已生成");
            fileOutputStream.close();
        }
        //检查能否正常使用日志功能和统计功能
        checkCountAndLog(defaultCountPath, defaultLogPath);
        File dir = new File(WEB_ROOT);
        if(!dir.exists()){
            System.out.println("服务器运行初始化出错，WEB_ROOT目录不存在！\n");
            return;
        }
//        FileReader fileReader = new FileReader(WEB_ROOT);
//        fileReader.setFilename("/test/a.html");
//        String temp231 = fileReader.getFileString();
//        System.out.println(temp231);

        try {
            serverSocket = new ServerSocket(port, 1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        while (true) {
        	//统计
        	Counting mycount = new Counting();
        	mycount.setDirPath(defaultCountPath);
        	
            Socket socket = null;
            try {
                //将程序阻塞在这一步，等待成功建立连接时将程序释放
                socket = serverSocket.accept();
                String date = df.format(new Date());
                //写入统计
                mycount.writeIPToFile(socket, date);
                
                System.out.println("Connection established...");
                Thread thread = new Thread(new HttpServer(socket, WEB_ROOT, defaultFilePath, date, defaultLogPath));
                thread.start();
            } catch (Exception e) {
                e.printStackTrace();
               continue;
            }

        }
    }
    //判断是否存在统计功能和日志功能的文件夹
    public static void checkCountAndLog(String defaultCountPath, String defaultLogPath) {
    	File myPath1 = new File(defaultCountPath);
    	File myPath2 = new File(defaultLogPath);
    	if(!myPath1.exists()) {
    		myPath1.mkdir();
    	}
    	if(!myPath2.exists()) {
    		myPath2.mkdir();
    	}
    }
}




//    Thread thread = new Thread(server);
//            thread.start();
//    HttpServer server = new HttpServer();
//            System.out.println("当前目录：" + server.WEB_ROOT);


//            int i = 0;
//            FileInputStream fileInputStream = new FileInputStream(file);
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
//            System.out.println("WebServer initializing...");
//            while ((config[i++] = bufferedReader.readLine()) != null) { }
//            temp = config[1].split("=");//从config.ini获取WEB_ROOT路径
//            WEB_ROOT = temp[1].trim();
//            temp = config[0].split("=");//获取默认设置的端口号
//            port = Integer.parseInt(temp[1].trim());