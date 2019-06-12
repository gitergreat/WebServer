import java.io.InputStream;
import java.io.IOException;
import java.util.regex.*;

class Request {

    private InputStream input;
    private String uri;//���ڱ���Web�ͻ���������ļ�Ŀ¼
    private static String HTTP_MESSAGE = null;
    private static String bs64 = null;
    public Request(InputStream input) {
        this.input = input;
    }

    //��InputStream�ж�ȡrequest��Ϣ������request�л�ȡuriֵ
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
        //.out.println("���յ���HTTP����:"+HTTP_MESSAGE);
        uri = parseUri(request.toString());
        method = getHttpMethod(request.toString());
        return method;
        //System.out.println("HTTP ������"+method);
    }

    public String getHttpMessage(){
        return HTTP_MESSAGE;
    }

    public String getFileName(){//��ȡ������ļ���
        if(uri.contains("/")) {
            String[] fileName = uri.split("/");
            return fileName[fileName.length - 1];
        }
        else return uri;
    }

    public boolean isAuthorization(){
        String[] divideMessage = HTTP_MESSAGE.split("\r\n");//��http���İ��зָ�
        String patterString  = "Authorization";

        int index = 0;
        boolean flag = false;
        for(int i=0;i<divideMessage.length;i++){
            String[] tempString = divideMessage[i].split(":");
            //�ж��Ƿ����Authorization�ؼ��֣������ڣ�����ؼ�����������
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
     * requestString��ʽ���£�
     * GET /index.html HTTP/1.1
     * Host: localhost:8080
     * Connection: keep-alive
     * Cache-Control: max-age=0
     * ...
     * �ú���Ŀ�ľ���Ϊ�˻�ȡ/index.html�ַ���
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

    private String parseUri(String requestString) {//��ȡ������ļ�·��
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