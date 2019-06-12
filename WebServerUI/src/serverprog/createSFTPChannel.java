package serverprog;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelExec;
import java.util.Properties;


//本类用于创建一个SFTP通道
//一个工具类，根据ip，用户名及密码得到一个SFTP channel对象，即ChannelSftp的实例对象，在应用程序中就可以使用该对象来调用SFTP的各种操作方法
public class createSFTPChannel {

    public Session session = null;
    public Channel channel = null;

    public Session getSession(authority authority, int timeout){
        JSch jSch = new JSch();
        try {
            //根据用户名、密码、端口、地址建立一个会话
            session = jSch.getSession(authority.username, authority.IPaddr, authority.port);
            session.setPassword(authority.password);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);//为session对象设置properties
            session.setTimeout(timeout);//设置timeout时间
            session.connect();//通过session建立连接
            return session;
        }catch (JSchException e){
            return null;
        }
    }

    public ChannelSftp getChannel(){
        try{
            //打开SFTP通道
            System.out.println("Opening channel.");
            channel = session.openChannel("sftp");
            channel.connect();//建立通道连接
            return (ChannelSftp) channel;
        }
        catch (JSchException e){
            return null;
        }
    }

    //关闭通道和会话
    public void closechannel()throws Exception{
        if(channel != null){
            channel.disconnect();
        }
        if(session != null){
            session.disconnect();
        }
    }
}
