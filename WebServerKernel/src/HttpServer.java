import com.sun.xml.internal.ws.resources.HttpserverMessages;

import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class HttpServer implements Runnable {

    /**
     * WEB_ROOT是HTML和其它文件存放的目录. 这里的WEB_ROOT为工作目录下的webroot目录
     */
//    public static String WEB_ROOT = System.getProperty("user.dir") + File.separator + "webroot";
    public static String WEB_ROOT = null;
    // 关闭服务命令
    //private static final String SHUTDOWN_COMMAND = "/SHUTDOWN";
    //受限制文件目录
    public static String defaultFilePath;

    public String supportMethod = "GET";
    private String bs64;
    private String httpMessage;
    //接收返回的套接字用于处理
    private Socket socket;
//    private ServerSocket serverSocket;

    private boolean flag1 = false;//用于判断是否请求一个受限制文件
    private boolean flag2 = false;//判断是否已经进行了用户认证并且接收相应的bs64加密的字符串
    private boolean flag3 = false;//用于判断用户认证是否失败
    
    //访问时间
    private String date;
    //日志目录
    private String logPath;

    //构造函数
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
            httpMessage = request.getHttpMessage();//获取提取出的http报文
            //System.out.println("HttpMessage:"+httpMessage);
            //日志
        	LogFunction mylog = new LogFunction();
        	mylog.setLogPath(logPath);
        	mylog.setIPAddress(socket);
        	mylog.setTime(date);
            mylog.setRuquestInfo(request.getHttpMessage());
            //错误日志
            boolean errorExist = false;	//判断错误是否发生
            ELogFunction myElog = new ELogFunction();
            myElog.setLogPath(logPath);
            myElog.setTime(date);
            myElog.setIPAddress(socket);


            if (!method.equals(supportMethod)) {
                Response response = new Response(output);
                response.setRequest(request);
                response.sendStaticResource(3);//flag为false正常传输文件
                socket.close();
                return;
            }

            /* 将即将传输的文件与存储的md5值进行校验，校验通过允许传输文件，否则返回状态度码500
              为提高传输的速率，只对html文件进行加密验证*/
            int dividedIndex =fileName.lastIndexOf(".");
            String tpyeName = fileName.substring(dividedIndex+1,fileName.length());
            if(tpyeName.equals("html")||tpyeName.equals("")){
                Verify verify = new Verify(request);
                if (!verify.isVerified()) {
                    System.out.println("Can not transfer the file! The file may be tampered !");
                    Response response = new Response(output);
                    response.setRequest(request);
                    response.sendStaticResource(2);//flag为false正常传输文件
                    //获取日志信息
                    //获取日志信息
                    mylog.setStateCode(response.getStateCode());
                    mylog.settotalBytes(response.getBytesNum());
                    if (!response.getError().equals("-")) {
                        myElog.setErrorMessage(response.getError(), request.getHttpMessage());
                        errorExist = true;
                    }
                    lock1.lock();
                    try {
                        //有锁的才能写文件
                        mylog.writeTofile();
                    } finally {
                        // 释放锁
                        lock1.unlock();
                    }
                    if (errorExist) {
                        lock2.lock();
                        try {
                            //有锁的才能写文件
                            myElog.writeTofile();
                        } finally {
                            // 释放锁
                            lock2.unlock();
                        }
                    }
                    socket.close();
                    return;
                }
            }


            //如果没有请求具体的文件，则返回默认的文件
            if(fileName.equals("")){
                Response response = new Response(output);
                request.setUri(defaultFilePath);
                response.setRequest(request);
                response.sendStaticResource(0);
                //获取日志中所需信息
                mylog.setStateCode(response.getStateCode());
                mylog.settotalBytes(response.getBytesNum());
                if(!response.getError().equals("-")) {
                	myElog.setErrorMessage(response.getError(), request.getHttpMessage());
                	errorExist = true;
                }

                // 关闭 socket 对象
                socket.close();
                return;
            }

            /**
             * 判断请求中是否包含未修改未请求，并做出相应的修改
             * 此处判断应在是否需要进行认证之前，因为其已经缓存，所以不需要进行用户认证，直接返回304
             * 若已经发生了文件的改变，此时则需要重新进行用户认证再进行文件传输
             */
            IsModified isModified = new IsModified(request);
            if(isModified.needModified()){
                if(isModified.isModified()){
                    //请求的文件未经修改，返回304
                    Response response = new Response(output);
                    response.setRequest(request);
                    response.sendStaticResource(4);
                    //获取日志信息
                    mylog.setStateCode(response.getStateCode());
                    mylog.settotalBytes(response.getBytesNum());
                    if(!response.getError().equals("-")) {
                        myElog.setErrorMessage(response.getError(), request.getHttpMessage());
                        errorExist = true;
                    }
                    lock1.lock();
                    try {
                        //有锁的才能写文件
                        mylog.writeTofile();
                    } finally {
                        // 释放锁
                        lock1.unlock();
                    }
                    if(errorExist) {
                        lock2.lock();
                        try {
                            //有锁的才能写文件
                            myElog.writeTofile();
                        } finally {
                            // 释放锁
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



            //首先判断请求的文件是否是受限制文件
            Auth auth = new Auth(WEB_ROOT,"httpd.conf",request.getUri());//用于判断是否需要认证

            if(auth.ifLimitFile()){
                flag1 = true;
                System.out.println("It is a limited file!");
            }

            if(request.isAuthorization()){
                //flag2 = true;
                bs64 = request.getBs64();
               //System.out.println("bs64:"+bs64);
                auth.setUserString(bs64);
                
                //设置好用户名
                String temp = auth.getUserInput();
                
                //日志获得用户名
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


            if(flag1 == false){//如果请求一个非受限制文件，直接返回请求文件
                Response response = new Response(output);
                response.setRequest(request);
                response.sendStaticResource(0);//flag为false正常传输文件
                //获取日志信息
                mylog.setStateCode(response.getStateCode());
                mylog.settotalBytes(response.getBytesNum());
                if(!response.getError().equals("-")) {
                	myElog.setErrorMessage(response.getError(), request.getHttpMessage());
                	errorExist = true;
                }
                // 关闭 socket 对象
                socket.close();
                return;
            }

            if(flag1 == true && flag2 == false && flag3 == false){//如果请求一个受限制文件且未进行认证
                Response response = new Response(output);
                response.setRequest(request);
                response.sendStaticResource(1);//请求的是一个受限制文件，返回状态码401
                //获取日志信息
                mylog.setStateCode(response.getStateCode());
                mylog.settotalBytes(response.getBytesNum());
                if(!response.getError().equals("-")) {
                	myElog.setErrorMessage(response.getError(), request.getHttpMessage());
                	errorExist = true;
                }
                // 关闭 socket 对象
                socket.close();
                return;
            }

            if(flag2 == true){
                Response response = new Response(output);
                response.setRequest(request);
                response.sendStaticResource(0);//已经经过认证,正常从传输文件
                //获取日志信息
                mylog.setStateCode(response.getStateCode());
                mylog.settotalBytes(response.getBytesNum());
                if(!response.getError().equals("-")) {
                	myElog.setErrorMessage(response.getError(), request.getHttpMessage());
                	errorExist = true;
                }
                // 关闭 socket 对象
                socket.close();
                return;
            }

            if(flag3){
                Response response = new Response(output);
                response.setRequest(request);
                response.sendStaticResource(-1);//未经过认证,正常从传输文件
                //获取日志信息
                mylog.setStateCode(response.getStateCode());
                mylog.settotalBytes(response.getBytesNum());
                if(!response.getError().equals("-")) {
                	myElog.setErrorMessage(response.getError(), request.getHttpMessage());
                	errorExist = true;
                }
                // 关闭 socket 对象
                socket.close();
                return;
            }
            
            //日志写入

            lock1.lock();  
            try {
            	//有锁的才能写文件
                mylog.writeTofile();
            } finally {  
                // 释放锁  
                lock1.unlock();  
            }  
            //错误日志写入

            if(errorExist) {
            	lock2.lock();
            	try {
                	//有锁的才能写文件
                    myElog.writeTofile();
                } finally {  
                    // 释放锁  
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
//                String[] divideMessage = httpMessage.split("\r\n");//将http报文按行分割
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
//                    System.out.println("分割的字符串");
//                    System.out.println(divideMessage[i]);
//                }

// System.out.println(socket.getInetAddress());

// 创建 Response 对象

//                if (divideMessage[6].equals("Connection: keep-alive")) {
//                    socket = serverSocket.accept();
//                } else {
//                    socket.close();
//                    break;
//                }


//    ServerSocket serverSocket = null;
//        int port = 8080;
//        try {
//            //服务器套接字对象
//            serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.exit(1);
//        }


//        while (true) {
//                Socket socket = null;
//
//                try {
//                //等待连接，连接成功后，返回一个Socket对象
////                socket = serverSocket.accept();
////                System.out.println("连接成功！");
//                input = socket.getInputStream();
//                output = socket.getOutputStream();
//
//                // 创建Request对象并解析
//
//
//                // 检查是否是关闭服务命令
//
//                }
//                }