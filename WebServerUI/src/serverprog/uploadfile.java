package serverprog;

import com.jcraft.jsch.ChannelSftp;

import java.io.File;

public class uploadfile {
    //源文件路径
    public String src;
    //上传路径
    public String dest;

    public void setSrc(String src) {
        this.src = src;
    }
    public void setDest(String dest) {
        this.dest = dest;
    }

    public void upload(authority authority)throws Exception{
        //建立SFTP通道
        createSFTPChannel channel = new createSFTPChannel();
        ChannelSftp channelSftp = channel.getChannel();

        //获取文件大小用于后续上传信息的打印
        File file = new File(this.src);
        long filesize = file.length();

        //上传文件
        channelSftp.put(src,dest);
        System.out.println("Succeessfully transfered.");
        channelSftp.quit();
        channel.closechannel();
    }

}
