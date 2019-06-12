package serverprog;

//登录服务器需要的参数
public class authority {
    public String username;
    public String password;
    public int port;
    public String IPaddr;
    public void inputInfo(String username, String password, String IPaddr,int port){
        this.username = username;
        this.password = password;
        this.port =port;
        this.IPaddr = IPaddr;
    }
}
