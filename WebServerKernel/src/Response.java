import java.io.OutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantLock;
import java.text.SimpleDateFormat;
import java.util.Locale;

/*
  HTTP Response = Status-Line
    *(( general-header | response-header | entity-header ) CRLF)
    CRLF
    [ message-body ]
    Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
*/
class Response {

    private static final String OKMessage_0 =" HTTP/1.1 200 OK\r\n" + "Cache-Control: no-cache\r\n" + "Connection: Keep-Alive\r\n" +
            "Content-Type:" ;
    private static final String OKMessage_1 = ";charset=utf-8\r\n" + "Server:Bpache\r\n";
    private static final String NotModifiedMessage = "HTTP/1.1 304 Not Modified\r\n" + "Cache-Control: no-cache\r\n"
            + "Content-Type: text/html;charset=utf-8\r\n" + "Server:Bpache\r\n" + "\r\n";
    private static final String badRequestMessage = "HTTP/1.1 400 Bad Request\r\n" + "Cache-Control: no-cache\r\n"
            + "Content-Type: text/html;charset=utf-8\r\n" + "Server:Bpache\r\n";

    private static final String unauthorizedMessage = "HTTP/1.1 401 UNAUTHORIZED\r\n"+"WWW-Authenticate: Basic realm=protected_docs\r\n" +
            "Cache-Control: no-cache\r\n" + "Connection: Keep-Alive\r\n" + "Content-Type: text/html;charset=utf-8\r\n"+ "Server:Bpache\r\n"
            +"Content-Length: ";

    private static final String forbidenMessage = " HTTP/1.1 403 Forbidden\r\n"+ "Cache-Control: no-cache\r\n" + "Connection: Keep-Alive\r\n"
            + "Content-Type: text/html;charset=utf-8\r\n"+ "Server:Bpache\r\n" + "\r\n" +  "<h1>403 Forbidden</h1>";

    private static final String errorMessage = " HTTP/1.1 404 File Not Found\r\n" + "Cache-Control: no-cache\r\n " +
            "Content-Type: text/html;charset=utf-8\r\n" + "Server:Bpache\r\n" +
            "Content-Length: 23\r\n" + "\r\n" + "<h1>404 File Not Found</h1>";

    private static final String insideErrorMessage = "HTTP/1.1 500 Internal Server Error\r\n" + "Cache-Control: no-cache\r\n"
            + "Content-Type: text/html;charset=utf-8\r\n" + "Server:Bpache\r\n" + "\r\n" +  "<h1>500 Internal Server Error</h1>";


    private static final int BUFFER_SIZE = 1024000;
    //    private ServerSocket serverSocket;
    private static ReentrantLock lock = new ReentrantLock();
    //public static final String HTTP_MESSSAGE = null;
    private Request request;
    private OutputStream output;
    //返回报文大小，状态码，错误信息概要
    private int bytesNum;
    private int stateCode;
    private String error = "-";	//没有发生错误

    //格式化输出当前时间，添加进入返回报文中
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

    public int getBytesNum() {
        return bytesNum;
    }

    public int getStateCode() {
        return stateCode;
    }

    public String getError() {
        return error;
    }

    public Response(OutputStream output) {
//        this.serverSocket = serverSocket;
        this.output = output;
    }

    public void setRequest(Request request) {
        this.request = request;
    }


    /*
     * sendStaticResource model 参数
     * 0:正常返回文件 状态码200
     * 1:需要进行用户认证 状态码401
     * -1:用户认证失败 状态码403
     * 2:传输的文件未能通过校验 状态码500
     * 3:不支持的请求方法 状态码400
     * 4:未修改 状态码304
     * */
    public void sendStaticResource(int model) throws IOException {
        byte[] bytes = new byte[BUFFER_SIZE];
        FileInputStream fis = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        String dateString =  simpleDateFormat.format(new Date()).toString();
        try {
            //将web文件写入到OutputStream字节流中
            lock.lock();//如果不同的线程访问同一个文件，则将该临界资源上锁
            Thread thread = Thread.currentThread();
            System.out.println("thread:"+thread.getId());
            File file = new File(HttpServer.WEB_ROOT,  request.getUri());//在这里添加校验
            //System.out.println(HttpServer.WEB_ROOT+request.getUri());
            if (file.exists()) {
                System.out.println("请求的文件:"+request.getUri());
                if(model == 0) {
                    stateCode = 200;
                    fis = new FileInputStream(file);
                    //用于分割字符串
                    StringTokenizer strTake = new StringTokenizer(request.getUri(), ".");
                    String tempTake;//用于保存文件后缀名
                    String tempMessage = null;//用于保存后缀名的格式
                    //切割获得第一个字符串
                    if(strTake.hasMoreTokens()) {
                        strTake.nextToken();
                    }
                    if(strTake.hasMoreTokens()) {
                        tempTake = strTake.nextToken();
                        if(tempTake.equals("js")){
                            tempMessage = "application/javascript";
                        }
                        if(tempTake.equals("png") || tempTake.equals("jpg") || tempTake.equals("gif") || tempTake.equals("jpeg")){
                            tempMessage = "image/" + tempTake;
                        }
                        if(tempTake.equals("css") || tempTake.equals("html") || tempTake.equals("xml")){
                            tempMessage = "text/" + tempTake;
                        }
                    }//没有后缀名的情况
                    else {
                        tempMessage = "text/html";
                    }


                    //格式化获取文件修改的最后时间
                    Long lastLong = file.lastModified();
                    Date lastModifiedDate = new Date(lastLong);
                    String lastModifiedString = simpleDateFormat.format(lastModifiedDate);

                    int ch = fis.read(bytes, 0, BUFFER_SIZE);
                    //String lastModifiedString = new Date(file.lastModified()).toString();
                    while (ch != -1) {
                        //byte[] buff = (OKMessage  + "\r\n"+"date:"+ dateString +"\r\n"+ "last-modified:" + lastModifiedString +"\r\n" + Long.toString(file.length()) +"\r\n" + "\r\n").getBytes();

                        //byte[] buff = ((OKMessage_0+tempMessage+OKMessage1 + Long.toString(file.length())) + "\r\n"+"date:"+ dateString +"\r\n"  +"\r\n" + "\r\n").getBytes();
                        byte[] buff = (OKMessage_0+tempMessage+OKMessage_1 +"date:"+ dateString +"\r\n" +"Content-Length:"+ Long.toString(file.length()) +"\r\n" + "Last-Modified: " + lastModifiedString +"\r\n" + "\r\n").getBytes();

                        output.write(buff);


                        output.write(bytes, 0, ch);
                        ch = fis.read(bytes, 0, BUFFER_SIZE);
                        bytesNum = buff.length;
                    }
                }

                if(model == 1) {
                    stateCode = 401;
                    error = "authentication failure for:";
                    byte[] buff = unauthorizedMessage.getBytes();
                    byte[] buffer = (unauthorizedMessage + Long.toString(unauthorizedMessage.length())+ "\r\n" + "date:"+ dateString + "\r\n" + "\r\n").getBytes();
                    output.write(buffer, 0, buffer.length);
                    bytesNum = buff.length;
                }

                if(model == -1){
                    //认证失败时返回的报文
                    stateCode = 403;
                    error = "client denied by server configuration";
                    byte[] buff = forbidenMessage.getBytes();
                    output.write(buff,0,forbidenMessage.length());
                    bytesNum = buff.length;
                }

                if(model == 2){
                    //传输的文件无法通过验证
                    stateCode = 500;
                    error = "inside error";
                    System.out.println("Verify file failed! Stop transfering!");
                    byte[] buff = insideErrorMessage.getBytes();
                    output.write(buff,0,insideErrorMessage.length());
                    bytesNum = buff.length;
                }

                if(model == 3){
                    //不支持的HTTP方法或错误的HTTP请求
                    stateCode = 400;
                    error = "bad request";
                    System.out.println("Bad request or not surpport method!!");
                    byte[] buff = (badRequestMessage +"date:" + dateString + "\r\n" + "\r\n"+  "<h1>400 Bad Request</h1>").getBytes();
                    output.write(buff,0,buff.length);
                    bytesNum = buff.length;
                }

                if(model == 4){
                    //已被缓存的文件
                    stateCode = 304;
                    System.out.println("File not modified!");
                    byte[] buff = NotModifiedMessage.getBytes();
                    output.write(buff,0,NotModifiedMessage.length());
                    bytesNum = buff.length;
                }

            } else {
                // file not found
                System.out.println("请求的文件不存在！");
                error = "File does not exist:";
                stateCode = 404;
                byte[] buff = errorMessage.getBytes();
                output.write(buff);
                bytesNum = buff.length;
            }

        } catch (Exception e) {
            // 如果不能传输文件则抛出异常
            System.out.println(e.toString());
        } finally {
            if (fis != null)
                fis.close();
            lock.unlock();
        }
    }

}



//for(int i=0;i<tempMessage.length;i++){
//        String[] divideTempMessage = tempMessage[i].split(":");
//        if(divideTempMessage[0].equals(request.getUri())){
//        if(divideTempMessage[1].equals(checkMessage)){
//        break;
//        }
//        else {
//        System.out.println("The file may be changed! Stop transfer");
//        byte[] buff = insideErrorMessage.getBytes();
//        output.write(buff,0,insideErrorMessage.length());
//        }
//        }
//        else {
//        if(tempMessage.length - 1 == i) {
//        System.out.println("The file check string not found!");
//        output.write(forbidenMessage.getBytes(), 0, forbidenMessage.length());
//        return;
//        }
//        }
//        }

//    MyFileReader myFileReader = new MyFileReader(HttpServer.WEB_ROOT,"md5.txt");
//    String MD5Message = myFileReader.getFileString();
//    String[] tempMes
//    \
//sage = MD5Message.split("\r\n");


//    public void sendStaticResource() throws IOException {
//        byte[] bytes = new byte[BUFFER_SIZE];
//        FileInputStream fis = null;
//        lock.lock();//如果不同的线程访问同一个文件，则将该临界资源上锁
//        try {
//            //将web文件写入到OutputStream字节流中
//            Thread thread = Thread.currentThread();
//            System.out.println("当前线程"+thread.getId());
//            File file = new File(HttpServer.WEB_ROOT, request.getUri());//在这里添加校验
//            if (file.exists()) {
//                stateCode = 200;//状态码
//                MD5 md5 = new MD5(file);
//                String checkMessage = md5.getMD5();//获取文件的md5值，并准备进行与已有的文件md5值进行校验
//                fis = new FileInputStream(file);
//                int ch = fis.read(bytes, 0, BUFFER_SIZE);
//                while (ch != -1) {
//                    byte[] buff = (OKMessage_0 + file.length() + "\r\n" + "\r\n").getBytes();
////                    String errorMessage = "HTTP/1.1 401 Unauthorized\r\n" + "WWW-Authenticate: Basic realm=index.html\r\n";
//                    output.write(buff);
////                   // output.write(errorMessage.getBytes());
//                    output.write(bytes, 0, ch);
//                    ch = fis.read(bytes, 0, BUFFER_SIZE);
//                    bytesNum = buff.length;
//                }
////                if(true) {
////                    String errorMessage ="HTTP/1.1 401 Unauthorized\r\n" +
////                            "WWW-Authenticate: Basic realm=protected_docs\r\n";
////                    output.write(errorMessage.getBytes(),0,errorMessage.length());
////                    Socket socket = serverSocket.accept();
////              }
//
//            } else {
//                // file not found
//                stateCode = 404;
//                error = "File does not exist:";
//                byte[] buff = errorMessage.getBytes();
//                output.write(buff);
//                bytesNum = buff.length;
//            }
//
//        } catch (Exception e) {
//            // 如果不能传输文件则抛出异常
//            System.out.println(e.toString());
//        } finally {
//            if (fis != null)
//                fis.close();
//            lock.unlock();
//        }
//    }