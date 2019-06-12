import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Main {
    public static void main(String[] args) throws IOException {

        //���ж˿ں�
        int port = 8080;

        //�ļ��洢Ŀ¼
        String WEB_ROOT = "webroot";

        //��վĬ���ļ�·��
        String defaultFilePath = "/test/a.html";
        
        //����IPͳ��·��
        String defaultCountPath = "ipinfo";

        //��־·��
        String defaultLogPath = "log";

        //TCP�˿�
        ServerSocket serverSocket = null;
        
        //����
    	SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z", Locale.US);

        File file = new File("config.ini");//��ѯ��ʼ�������ļ�λ��
        if (file.exists()) {
            //��������ļ����ڣ���ֱ�Ӷ�ȡ�����ļ��������ö˿ں���������Ŀ¼
            MyFileReader fileReader = new MyFileReader();
            fileReader.setFilename("config.ini");
            String fileStirng = fileReader.getFileString();
            String[] dividedFileString = fileStirng.split("\r\n");
            System.out.println("WebServer initializing...");
            //��ȡconfig.ini�ļ����ݣ��������������ʼ������
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
        //����Ҳ���Ĭ�������ļ����򽫶˿ں���Webroot����ΪĬ��ֵ��������Ĭ�ϵ������ļ�
        else {
            System.out.println("�����ļ���ʧ����������Ĭ�������ļ�");
            String writeString  = "port = 8080\r\n"
            					+ "WEBROOT = webroot\r\n"
            					+ "deafaultPage = /test/a.html\r\n"
            					+ "countPath = ipinfo\r\n"
            					+ "logPath = log\r\n";
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(writeString.getBytes());
            System.out.println("Ĭ�������ļ�������");
            fileOutputStream.close();
        }
        //����ܷ�����ʹ����־���ܺ�ͳ�ƹ���
        checkCountAndLog(defaultCountPath, defaultLogPath);
        File dir = new File(WEB_ROOT);
        if(!dir.exists()){
            System.out.println("���������г�ʼ������WEB_ROOTĿ¼�����ڣ�\n");
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
        	//ͳ��
        	Counting mycount = new Counting();
        	mycount.setDirPath(defaultCountPath);
        	
            Socket socket = null;
            try {
                //��������������һ�����ȴ��ɹ���������ʱ�������ͷ�
                socket = serverSocket.accept();
                String date = df.format(new Date());
                //д��ͳ��
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
    //�ж��Ƿ����ͳ�ƹ��ܺ���־���ܵ��ļ���
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
//            System.out.println("��ǰĿ¼��" + server.WEB_ROOT);


//            int i = 0;
//            FileInputStream fileInputStream = new FileInputStream(file);
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
//            System.out.println("WebServer initializing...");
//            while ((config[i++] = bufferedReader.readLine()) != null) { }
//            temp = config[1].split("=");//��config.ini��ȡWEB_ROOT·��
//            WEB_ROOT = temp[1].trim();
//            temp = config[0].split("=");//��ȡĬ�����õĶ˿ں�
//            port = Integer.parseInt(temp[1].trim());