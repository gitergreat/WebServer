package serverprog;

import java.io.*;
import java.util.Scanner;
import java.util.Base64;

//此类用于对服务器运行的的参数进行配置
//涉及写入相关文件
public class serverconfig {
    public void getinfo(String port,String dir,String def)throws FileNotFoundException {
        Scanner in = new Scanner(System.in);
        File file = new File(System.getProperty("user.dir")+"\\config.ini");
        PrintStream p = new PrintStream(new FileOutputStream(file));
        String line = port;
        p.println("port = "+ line);
        line = dir;
        p.println("WEBROOT = "+ line);
        p.println("deafaultPage = " + def);
        p.println("countPath = ipinfo");
        p.println("logPath = log");
    }
    public void remember(String ipaddr,String username,String password)throws FileNotFoundException{
        File file = new File(System.getProperty("user.dir")+"\\configuser.ini");
        PrintStream p = new PrintStream(new FileOutputStream(file));
        p.println(ipaddr);
        p.println(username);
        p.println(password);
    }
    //网页防篡改计算md5值
    public String md5(String filename, String ss)throws FileNotFoundException{
        try {
            int index = filename.indexOf('.');
            filename = filename.substring(0,index)+'_'+filename.substring(index+1)+".md5";
            File file = new File(System.getProperty("usr.dir"),  filename);
            PrintStream p = new PrintStream(new FileOutputStream(file));
            String base64String = Base64.getEncoder().encodeToString(ss.getBytes("utf-8"));
            p.println(base64String);

            return file.getAbsolutePath();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
