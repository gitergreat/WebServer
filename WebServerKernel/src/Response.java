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
    //���ر��Ĵ�С��״̬�룬������Ϣ��Ҫ
    private int bytesNum;
    private int stateCode;
    private String error = "-";	//û�з�������

    //��ʽ�������ǰʱ�䣬��ӽ��뷵�ر�����
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
     * sendStaticResource model ����
     * 0:���������ļ� ״̬��200
     * 1:��Ҫ�����û���֤ ״̬��401
     * -1:�û���֤ʧ�� ״̬��403
     * 2:������ļ�δ��ͨ��У�� ״̬��500
     * 3:��֧�ֵ����󷽷� ״̬��400
     * 4:δ�޸� ״̬��304
     * */
    public void sendStaticResource(int model) throws IOException {
        byte[] bytes = new byte[BUFFER_SIZE];
        FileInputStream fis = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        String dateString =  simpleDateFormat.format(new Date()).toString();
        try {
            //��web�ļ�д�뵽OutputStream�ֽ�����
            lock.lock();//�����ͬ���̷߳���ͬһ���ļ����򽫸��ٽ���Դ����
            Thread thread = Thread.currentThread();
            System.out.println("thread:"+thread.getId());
            File file = new File(HttpServer.WEB_ROOT,  request.getUri());//���������У��
            //System.out.println(HttpServer.WEB_ROOT+request.getUri());
            if (file.exists()) {
                System.out.println("������ļ�:"+request.getUri());
                if(model == 0) {
                    stateCode = 200;
                    fis = new FileInputStream(file);
                    //���ڷָ��ַ���
                    StringTokenizer strTake = new StringTokenizer(request.getUri(), ".");
                    String tempTake;//���ڱ����ļ���׺��
                    String tempMessage = null;//���ڱ����׺���ĸ�ʽ
                    //�и��õ�һ���ַ���
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
                    }//û�к�׺�������
                    else {
                        tempMessage = "text/html";
                    }


                    //��ʽ����ȡ�ļ��޸ĵ����ʱ��
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
                    //��֤ʧ��ʱ���صı���
                    stateCode = 403;
                    error = "client denied by server configuration";
                    byte[] buff = forbidenMessage.getBytes();
                    output.write(buff,0,forbidenMessage.length());
                    bytesNum = buff.length;
                }

                if(model == 2){
                    //������ļ��޷�ͨ����֤
                    stateCode = 500;
                    error = "inside error";
                    System.out.println("Verify file failed! Stop transfering!");
                    byte[] buff = insideErrorMessage.getBytes();
                    output.write(buff,0,insideErrorMessage.length());
                    bytesNum = buff.length;
                }

                if(model == 3){
                    //��֧�ֵ�HTTP����������HTTP����
                    stateCode = 400;
                    error = "bad request";
                    System.out.println("Bad request or not surpport method!!");
                    byte[] buff = (badRequestMessage +"date:" + dateString + "\r\n" + "\r\n"+  "<h1>400 Bad Request</h1>").getBytes();
                    output.write(buff,0,buff.length);
                    bytesNum = buff.length;
                }

                if(model == 4){
                    //�ѱ�������ļ�
                    stateCode = 304;
                    System.out.println("File not modified!");
                    byte[] buff = NotModifiedMessage.getBytes();
                    output.write(buff,0,NotModifiedMessage.length());
                    bytesNum = buff.length;
                }

            } else {
                // file not found
                System.out.println("������ļ������ڣ�");
                error = "File does not exist:";
                stateCode = 404;
                byte[] buff = errorMessage.getBytes();
                output.write(buff);
                bytesNum = buff.length;
            }

        } catch (Exception e) {
            // ������ܴ����ļ����׳��쳣
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
//        lock.lock();//�����ͬ���̷߳���ͬһ���ļ����򽫸��ٽ���Դ����
//        try {
//            //��web�ļ�д�뵽OutputStream�ֽ�����
//            Thread thread = Thread.currentThread();
//            System.out.println("��ǰ�߳�"+thread.getId());
//            File file = new File(HttpServer.WEB_ROOT, request.getUri());//���������У��
//            if (file.exists()) {
//                stateCode = 200;//״̬��
//                MD5 md5 = new MD5(file);
//                String checkMessage = md5.getMD5();//��ȡ�ļ���md5ֵ����׼�����������е��ļ�md5ֵ����У��
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
//            // ������ܴ����ļ����׳��쳣
//            System.out.println(e.toString());
//        } finally {
//            if (fis != null)
//                fis.close();
//            lock.unlock();
//        }
//    }