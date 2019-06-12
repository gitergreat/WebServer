import java.util.Base64;
import java.util.StringTokenizer;
class Auth {
    private static String WEB_ROOT = null;  //WEB_ROOT的路径
    private String buffer;  //缓冲区
    private String userString;  //用户名和密码的字符串
    private String path;  //用户请求的文件路径
    private String confPath;  //配置文件的路径
    
    private String accessName;//访问的用户名名称
    
    public String getAccessName() {
    	return accessName;
    }

    //构造方法，依次传入所需参数
    public Auth(String WEB_ROOT, String confPath, String path){
        this.path = path;
        this.WEB_ROOT = WEB_ROOT;
        this.confPath = confPath;
    }

    //判断文件是否为受限制访问文件
    public boolean ifLimitFile(){
        MyFileReader reader1 = new MyFileReader(WEB_ROOT, confPath);
        buffer = reader1.getFileString();

        int find = buffer.indexOf(path+"\r\n");
        if(find!=-1){
            return true;
        }else{
            return false;
        }
    }
    public void setUserString(String userString){
        this.userString = userString;
    }

    //从用户在认证窗口中输入的信息得到其用户名密码串
    public String getUserInput() {
        try {
            //userString = userString + "=";
            //System.out.println(user);

            //byte[] base64decodedBytes = new BASE64Decoder().decodeBuffer(userString);
            byte[] base64decodedBytes = Base64.getDecoder().decode(userString.getBytes());
            String userInfo = new String(base64decodedBytes, "utf-8");
            
            System.out.println(userInfo);
            
            StringTokenizer strTake = new StringTokenizer(userInfo, ":");
            if(strTake.hasMoreTokens()){
            	accessName = strTake.nextToken();
    		}
            else {
            	accessName = "-";
            }
            
            return userInfo;
            //String[] info = userString.split("\\:");
            //return info;
        } catch (Exception e) {
            System.out.println("Error :" + e.getMessage());
            return null;
        }
    }

    //判断用户是否具有访问权限
    public boolean matchUser() {
        int begin = path.lastIndexOf("/");  // /
        int end = path.length()-1;  //\n
        String subPath = path.substring(begin+1, end+1);
        String userPath = path.substring(0, begin+1);

        if (subPath.indexOf(".")!=-1){
            userPath = userPath + subPath.replace(".","_") + ".txt";
        }else {
            userPath = userPath + subPath + ".txt";
        }

        MyFileReader reader2 = new MyFileReader(WEB_ROOT, userPath);
        buffer = reader2.getFileString();
        //把文件读到buffer中
        String userInfo = getUserInput()+"\r\n";
        int find = buffer.indexOf(userInfo);

        if(find!=-1){
            return true;
        }else{
            return false;
        }

    }

}

//readme:
/*
首先构造函数传入参数WEB_ROOT confPath 和 path
利用ifLimitFile()方法判断是否为受限制文件（是返回true  不是返回false）
1、如果是受限制文件，调用方法setUserString()将base64串输入
   然后调用matchUser()，判断用户身份，正确返回true  错误返回false
2、如果不是受限制文件，直接响应用户请求的资源
 */