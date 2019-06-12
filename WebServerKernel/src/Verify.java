import com.sun.xml.internal.ws.resources.HttpserverMessages;

import java.io.File;
import java.util.Base64;


class Verify {
    //private String uri;
    //private boolean flag = true;
    private String filename = "md5.txt";
    private String filePath = HttpServer.WEB_ROOT;
    private Request request;

    Verify(Request request){
        this.request = request;
    }

    public boolean isVerified() {
        //MyFileReader myFileReader = new MyFileReader(filePath,filename);
        //
        if (request.getUri().equals("")) {
            //如果请求的页面为默认页面，则按此处理
            File file = new File(HttpServer.WEB_ROOT, HttpServer.defaultFilePath);
            MD5 md5 = new MD5(file);
            String checkMessage = md5.getMD5();//获取文件的md5值，并准备进行与已有的文件md5值进行校验
            System.out.println(checkMessage);

            System.out.println(request.getUri() + "_md5.txt");
            String filename;
            if(request.getUri().equals("")){
                filename = HttpServer.defaultFilePath;
            }
            else filename = request.getUri();

            File checkFile = new File(filePath, filename + "_md5.txt");
            if (!checkFile.exists()) {
                System.out.println("检验文件不存在!");
                return false;
            }

            MyFileReader myFileReader = new MyFileReader(filePath, filename + "_md5.txt");

            String encryptMD5Message = myFileReader.getFileString();
            byte[] base64DecodedBytes = Base64.getDecoder().decode(encryptMD5Message.substring(0, encryptMD5Message.length() - 2).getBytes());
            String decryptMD5Message = new String(base64DecodedBytes);

            //String[] tempMessage = decryptMD5Message.split("\r\n");
            if (decryptMD5Message.equals(checkMessage)) {
                return true;
            } else return false;
        }
        else {
            File file = new File(HttpServer.WEB_ROOT, request.getUri());
            MD5 md5 = new MD5(file);
            String checkMessage = md5.getMD5();//获取文件的md5值，并准备进行与已有的文件md5值进行校验
            //System.out.println(checkMessage);

            //System.out.println(request.getUri() + "_md5.txt");
            String fileName = (request.getUri()).replace(".","_")+".md5";
            //MyFileReader myFileReader = new MyFileReader(filePath, request.getUri() + "_md5.txt");
            MyFileReader myFileReader = new MyFileReader(filePath, fileName);

            File checkFile = new File(filePath, fileName);
            if (!checkFile.exists()) {
                System.out.println("检验文件不存在!");
                return false;
            }

            String encryptMD5Message = myFileReader.getFileString();
            byte[] base64DecodedBytes = Base64.getDecoder().decode(encryptMD5Message.substring(0, encryptMD5Message.length() - 2).getBytes());
            String decryptMD5Message = new String(base64DecodedBytes);

            //String[] tempMessage = decryptMD5Message.split("\r\n");
            if (decryptMD5Message.equals(checkMessage)) {
                return true;
            } else return false;

        }
    }


}

//        for(int i=0;i<tempMessage.length;i++){
//        String[] divideTempMessage = tempMessage[i].split(":");
//        if(divideTempMessage[0].equals(request.getUri())){
//        if(divideTempMessage[1].equals(checkMessage)){
//        return true;//验证成功，返回结果true
//        }
//        else {
//        if(tempMessage.length - 1 == i) {
//        //若找不到对应的校验文件，返回false
//        System.out.println("The file check string not found!");
//        return false;
//        }
//        }
//        }
////            else {
////                if(tempMessage.length - 1 == i) {
////                    System.out.println("The file check string not found!");
////                    return false;
////                }
////            }
//        }