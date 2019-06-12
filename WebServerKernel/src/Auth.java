import java.util.Base64;
import java.util.StringTokenizer;
class Auth {
    private static String WEB_ROOT = null;  //WEB_ROOT��·��
    private String buffer;  //������
    private String userString;  //�û�����������ַ���
    private String path;  //�û�������ļ�·��
    private String confPath;  //�����ļ���·��
    
    private String accessName;//���ʵ��û�������
    
    public String getAccessName() {
    	return accessName;
    }

    //���췽�������δ����������
    public Auth(String WEB_ROOT, String confPath, String path){
        this.path = path;
        this.WEB_ROOT = WEB_ROOT;
        this.confPath = confPath;
    }

    //�ж��ļ��Ƿ�Ϊ�����Ʒ����ļ�
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

    //���û�����֤�������������Ϣ�õ����û������봮
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

    //�ж��û��Ƿ���з���Ȩ��
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
        //���ļ�����buffer��
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
���ȹ��캯���������WEB_ROOT confPath �� path
����ifLimitFile()�����ж��Ƿ�Ϊ�������ļ����Ƿ���true  ���Ƿ���false��
1��������������ļ������÷���setUserString()��base64������
   Ȼ�����matchUser()���ж��û���ݣ���ȷ����true  ���󷵻�false
2����������������ļ���ֱ����Ӧ�û��������Դ
 */