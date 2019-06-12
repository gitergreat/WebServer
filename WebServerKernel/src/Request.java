import java.io.InputStream;
import java.io.IOException;
import java.util.regex.*;

class Request {

    private InputStream input;
    private String uri;//用于保存Web客户端请求的文件目录
    private static String HTTP_MESSAGE = null;
    private static String bs64 = null;
    public Request(InputStream input) {
        this.input = input;
    }

    //从InputStream中读取request信息，并从request中获取uri值
    public String parse() {
        String method;
        StringBuffer request = new StringBuffer(2048);
        int messageLength;
        byte[] buffer = new byte[2048];
        try {
            messageLength = input.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            messageLength = -1;
        }
        for (int j = 0; j < messageLength; j++) {
            request.append((char) buffer[j]);
        }
     //   System.out.print(request.toString());
        HTTP_MESSAGE = request.toString();
        //.out.println("接收到的HTTP报文:"+HTTP_MESSAGE);
        uri = parseUri(request.toString());
        method = getHttpMethod(request.toString());
        return method;
        //System.out.println("HTTP 方法："+method);
    }

    public String getHttpMessage(){
        return HTTP_MESSAGE;
    }

    public String getFileName(){//获取请求的文件名
        if(uri.contains("/")) {
            String[] fileName = uri.split("/");
            return fileName[fileName.length - 1];
        }
        else return uri;
    }

    public boolean isAuthorization(){
        String[] divideMessage = HTTP_MESSAGE.split("\r\n");//将http报文按行分割
        String patterString  = "Authorization";

        int index = 0;
        boolean flag = false;
        for(int i=0;i<divideMessage.length;i++){
            String[] tempString = divideMessage[i].split(":");
            //判断是否存在Authorization关键字，若存在，保存关键字所在索引
            if(tempString[0].equals(patterString)){
                flag = true;
                index = i;
                break;
            }
        }
        if(flag) {
            String[] tempString1 = divideMessage[index].split(" ");
            bs64 = tempString1[2];
            return true;
        }
        else return false;
    }

    public String getBs64(){
        return bs64;
    }

    public void setUri(String uri){
        this.uri = uri;
    }

    /**
     *
     * requestString形式如下：
     * GET /index.html HTTP/1.1
     * Host: localhost:8080
     * Connection: keep-alive
     * Cache-Control: max-age=0
     * ...
     * 该函数目的就是为了获取/index.html字符串
     */
    public String getHttpMethod(String requestString){
        String[] dividedString = requestString.split("\r\n");
        if(dividedString[0] == null){
            return null;
        }
        else {
            String[] tempString = dividedString[0].split(" ");
            if(tempString[0] == null){
                return null;
            }
            else {
                System.out.println("method:"+tempString[0]);
                return tempString[0];
            }
        }
//        return "GET";
    }

    private String parseUri(String requestString) {//获取请求的文件路径
        int index1, index2;
        index1 = requestString.indexOf(' ');
        if (index1 != -1) {
            index2 = requestString.indexOf(' ', index1 + 1);
            if (index2 > index1) {
                String tempString = requestString.substring(index1 + 1, index2);
                return tempString.substring(1,tempString.length());
            }
                //return requestString.substring(index1 + 1, index2).substring(1,requestString.length());
        }
        return null;
    }

    public String getUri() {
        //System.out.println("uri:"+uri);
        return uri;
    }

}


//    private String parseUri(String requestString) {
//        int index1, index2;
//        index1 = requestString.indexOf(' ');
//        if (index1 != -1) {
//            index2 = requestString.indexOf(' ', index1 + 1);
//            if (index2 > index1)
//                return requestString.substring(index1 + 1, index2);
//        }
//        return null;
//    }


//    private String[] parseHttpMessage(String requestString){
//        String pattern = "\\r\\n";
//        Pattern ptn = new Pattern(pattern);
//        String[] store = ptn.split(requestString);
//        for(int i=0;i<store.length();i++){
//            System.out.println(store[i]);
//            return store;
//        }
//    }