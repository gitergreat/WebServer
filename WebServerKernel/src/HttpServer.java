import com.sun.xml.internal.ws.resources.HttpserverMessages;

import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class HttpServer implements Runnable {

    /**
     * WEB_ROOT��HTML�������ļ���ŵ�Ŀ¼. �����WEB_ROOTΪ����Ŀ¼�µ�webrootĿ¼
     */
//    public static String WEB_ROOT = System.getProperty("user.dir") + File.separator + "webroot";
    public static String WEB_ROOT = null;
    // �رշ�������
    //private static final String SHUTDOWN_COMMAND = "/SHUTDOWN";
    //�������ļ�Ŀ¼
    public static String defaultFilePath;

    public String supportMethod = "GET";
    private String bs64;
    private String httpMessage;
    //���շ��ص��׽������ڴ���
    private Socket socket;
//    private ServerSocket serverSocket;

    private boolean flag1 = false;//�����ж��Ƿ�����һ���������ļ�
    private boolean flag2 = false;//�ж��Ƿ��Ѿ��������û���֤���ҽ�����Ӧ��bs64���ܵ��ַ���
    private boolean flag3 = false;//�����ж��û���֤�Ƿ�ʧ��
    
    //����ʱ��
    private String date;
    //��־Ŀ¼
    private String logPath;

    //���캯��
    HttpServer(Socket socket,String WEB_ROOT,String defaultFilePath, String date, String logPath){
    	this.WEB_ROOT = System.getProperty("user.dir") + File.separator + WEB_ROOT;
    	//System.out.println(this.WEB_ROOT);
        this.socket = socket;
        this.defaultFilePath = defaultFilePath;
        this.date = date;
        this.logPath = logPath;
    }

    public void  run() {
        try {
            Lock lock1 = new ReentrantLock();
            Lock lock2 = new ReentrantLock();
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();
            Request request = new Request(input);
            String method = request.parse();
            String fileName = request.getFileName();
            httpMessage = request.getHttpMessage();//��ȡ��ȡ����http����
            //System.out.println("HttpMessage:"+httpMessage);
            //��־
        	LogFunction mylog = new LogFunction();
        	mylog.setLogPath(logPath);
        	mylog.setIPAddress(socket);
        	mylog.setTime(date);
            mylog.setRuquestInfo(request.getHttpMessage());
            //������־
            boolean errorExist = false;	//�жϴ����Ƿ���
            ELogFunction myElog = new ELogFunction();
            myElog.setLogPath(logPath);
            myElog.setTime(date);
            myElog.setIPAddress(socket);


            if (!method.equals(supportMethod)) {
                Response response = new Response(output);
                response.setRequest(request);
                response.sendStaticResource(3);//flagΪfalse���������ļ�
                socket.close();
                return;
            }

            /* ������������ļ���洢��md5ֵ����У�飬У��ͨ���������ļ������򷵻�״̬����500
              Ϊ��ߴ�������ʣ�ֻ��html�ļ����м�����֤*/
            int dividedIndex =fileName.lastIndexOf(".");
            String tpyeName = fileName.substring(dividedIndex+1,fileName.length());
            if(tpyeName.equals("html")||tpyeName.equals("")){
                Verify verify = new Verify(request);
                if (!verify.isVerified()) {
                    System.out.println("Can not transfer the file! The file may be tampered !");
                    Response response = new Response(output);
                    response.setRequest(request);
                    response.sendStaticResource(2);//flagΪfalse���������ļ�
                    //��ȡ��־��Ϣ
                    //��ȡ��־��Ϣ
                    mylog.setStateCode(response.getStateCode());
                    mylog.settotalBytes(response.getBytesNum());
                    if (!response.getError().equals("-")) {
                        myElog.setErrorMessage(response.getError(), request.getHttpMessage());
                        errorExist = true;
                    }
                    lock1.lock();
                    try {
                        //�����Ĳ���д�ļ�
                        mylog.writeTofile();
                    } finally {
                        // �ͷ���
                        lock1.unlock();
                    }
                    if (errorExist) {
                        lock2.lock();
                        try {
                            //�����Ĳ���д�ļ�
                            myElog.writeTofile();
                        } finally {
                            // �ͷ���
                            lock2.unlock();
                        }
                    }
                    socket.close();
                    return;
                }
            }


            //���û�����������ļ����򷵻�Ĭ�ϵ��ļ�
            if(fileName.equals("")){
                Response response = new Response(output);
                request.setUri(defaultFilePath);
                response.setRequest(request);
                response.sendStaticResource(0);
                //��ȡ��־��������Ϣ
                mylog.setStateCode(response.getStateCode());
                mylog.settotalBytes(response.getBytesNum());
                if(!response.getError().equals("-")) {
                	myElog.setErrorMessage(response.getError(), request.getHttpMessage());
                	errorExist = true;
                }

                // �ر� socket ����
                socket.close();
                return;
            }

            /**
             * �ж��������Ƿ����δ�޸�δ���󣬲�������Ӧ���޸�
             * �˴��ж�Ӧ���Ƿ���Ҫ������֤֮ǰ����Ϊ���Ѿ����棬���Բ���Ҫ�����û���֤��ֱ�ӷ���304
             * ���Ѿ��������ļ��ĸı䣬��ʱ����Ҫ���½����û���֤�ٽ����ļ�����
             */
            IsModified isModified = new IsModified(request);
            if(isModified.needModified()){
                if(isModified.isModified()){
                    //������ļ�δ���޸ģ�����304
                    Response response = new Response(output);
                    response.setRequest(request);
                    response.sendStaticResource(4);
                    //��ȡ��־��Ϣ
                    mylog.setStateCode(response.getStateCode());
                    mylog.settotalBytes(response.getBytesNum());
                    if(!response.getError().equals("-")) {
                        myElog.setErrorMessage(response.getError(), request.getHttpMessage());
                        errorExist = true;
                    }
                    lock1.lock();
                    try {
                        //�����Ĳ���д�ļ�
                        mylog.writeTofile();
                    } finally {
                        // �ͷ���
                        lock1.unlock();
                    }
                    if(errorExist) {
                        lock2.lock();
                        try {
                            //�����Ĳ���д�ļ�
                            myElog.writeTofile();
                        } finally {
                            // �ͷ���
                            lock2.unlock();
                        }
                    }
                    socket.close();
                    return;
                }
//                else {
//                    Response response = new Response(output);
//                    response.setRequest(request);
//                    response.sendStaticResource(0);
//                }
            }



            //�����ж�������ļ��Ƿ����������ļ�
            Auth auth = new Auth(WEB_ROOT,"httpd.conf",request.getUri());//�����ж��Ƿ���Ҫ��֤

            if(auth.ifLimitFile()){
                flag1 = true;
                System.out.println("It is a limited file!");
            }

            if(request.isAuthorization()){
                //flag2 = true;
                bs64 = request.getBs64();
               //System.out.println("bs64:"+bs64);
                auth.setUserString(bs64);
                
                //���ú��û���
                String temp = auth.getUserInput();
                
                //��־����û���
                mylog.setAuthName(auth.getAccessName());
                myElog.serUserName(auth.getAccessName());
                
                //auth.initUser();

                boolean flag = auth.matchUser();
                if(flag){
                    flag2 = true;
                }
                else{
                    flag3 = true;
                }
                //System.out.println(flag2);
            }


            if(flag1 == false){//�������һ�����������ļ���ֱ�ӷ��������ļ�
                Response response = new Response(output);
                response.setRequest(request);
                response.sendStaticResource(0);//flagΪfalse���������ļ�
                //��ȡ��־��Ϣ
                mylog.setStateCode(response.getStateCode());
                mylog.settotalBytes(response.getBytesNum());
                if(!response.getError().equals("-")) {
                	myElog.setErrorMessage(response.getError(), request.getHttpMessage());
                	errorExist = true;
                }
                // �ر� socket ����
                socket.close();
                return;
            }

            if(flag1 == true && flag2 == false && flag3 == false){//�������һ���������ļ���δ������֤
                Response response = new Response(output);
                response.setRequest(request);
                response.sendStaticResource(1);//�������һ���������ļ�������״̬��401
                //��ȡ��־��Ϣ
                mylog.setStateCode(response.getStateCode());
                mylog.settotalBytes(response.getBytesNum());
                if(!response.getError().equals("-")) {
                	myElog.setErrorMessage(response.getError(), request.getHttpMessage());
                	errorExist = true;
                }
                // �ر� socket ����
                socket.close();
                return;
            }

            if(flag2 == true){
                Response response = new Response(output);
                response.setRequest(request);
                response.sendStaticResource(0);//�Ѿ�������֤,�����Ӵ����ļ�
                //��ȡ��־��Ϣ
                mylog.setStateCode(response.getStateCode());
                mylog.settotalBytes(response.getBytesNum());
                if(!response.getError().equals("-")) {
                	myElog.setErrorMessage(response.getError(), request.getHttpMessage());
                	errorExist = true;
                }
                // �ر� socket ����
                socket.close();
                return;
            }

            if(flag3){
                Response response = new Response(output);
                response.setRequest(request);
                response.sendStaticResource(-1);//δ������֤,�����Ӵ����ļ�
                //��ȡ��־��Ϣ
                mylog.setStateCode(response.getStateCode());
                mylog.settotalBytes(response.getBytesNum());
                if(!response.getError().equals("-")) {
                	myElog.setErrorMessage(response.getError(), request.getHttpMessage());
                	errorExist = true;
                }
                // �ر� socket ����
                socket.close();
                return;
            }
            
            //��־д��

            lock1.lock();  
            try {
            	//�����Ĳ���д�ļ�
                mylog.writeTofile();
            } finally {  
                // �ͷ���  
                lock1.unlock();  
            }  
            //������־д��

            if(errorExist) {
            	lock2.lock();
            	try {
                	//�����Ĳ���д�ļ�
                    myElog.writeTofile();
                } finally {  
                    // �ͷ���  
                    lock2.unlock();  
                }  
            }


        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

}



//                String bs64;
//                String[] divideMessage = httpMessage.split("\r\n");//��http���İ��зָ�
//                for(int i=0;i<divideMessage.length;i++){
//                    String patterString  = "Authorization";
//                    String[] tempString = divideMessage[i].split(":");
//                    if(tempString[0].equals(patterString)){
//                        String[] tempString1 = divideMessage[i].split(" ");
//                        bs64 = tempString1[2];
//                        System.out.println(bs64);
//                    }
//                }
//                for(int i=0;i<divideMessage.length;i++){
//                    System.out.println("�ָ���ַ���");
//                    System.out.println(divideMessage[i]);
//                }

// System.out.println(socket.getInetAddress());

// ���� Response ����

//                if (divideMessage[6].equals("Connection: keep-alive")) {
//                    socket = serverSocket.accept();
//                } else {
//                    socket.close();
//                    break;
//                }


//    ServerSocket serverSocket = null;
//        int port = 8080;
//        try {
//            //�������׽��ֶ���
//            serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.exit(1);
//        }


//        while (true) {
//                Socket socket = null;
//
//                try {
//                //�ȴ����ӣ����ӳɹ��󣬷���һ��Socket����
////                socket = serverSocket.accept();
////                System.out.println("���ӳɹ���");
//                input = socket.getInputStream();
//                output = socket.getOutputStream();
//
//                // ����Request���󲢽���
//
//
//                // ����Ƿ��ǹرշ�������
//
//                }
//                }