package serverprog;

import com.jcraft.jsch.SftpProgressMonitor;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;

public class transferingMonitor extends TimerTask implements  SftpProgressMonitor {
    //设置默认时间为3秒
    private long progressInterval = 3*1000;
    // 记录传输是否结束
    private boolean isEnd = false;
    //记录已传输数据的大小
    private long transfered;
    //记录文件总大小
    private long filesize;
    //定时器对象
    private Timer timer;
    //记录是否已启动timer
    private boolean isScheduled = false;

    transferingMonitor(long filesize){
        this.filesize = filesize;
    }

    public void run(){
        setTransfered(0);
        if(!isEnd){
            System.out.println("Transfering...");
            long transfered = getTransfered();
            if(transfered != filesize){
                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                System.out.println("Transfered: " + decimalFormat.format((double)transfered/1024) + " KBytes");
                sendProgressMessage(transfered);
            }else {
                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                //System.out.println("Transfered: " + decimalFormat.format((double)filesize/1024) + " KBytes");
                System.out.println("Sending progress message: " + decimalFormat.format((double) transfered * 100 / (double) filesize) + "%");
                System.out.println("Finished transfering.");
                setEnd(true);
            }
        }else {
            stop();
        }
    }
    //关闭计时器
    public void stop(){
        if(timer != null){
            timer.cancel();
            timer.purge();
            timer = null;
            isScheduled = false;
        }
    }
    public void start(){
        if(timer == null){
            timer = new Timer();
        }
        timer.schedule(this,1000,progressInterval);
        isScheduled = true;
    }
    private void sendProgressMessage(long transfered){
        if(filesize != 0) {
            double d = ((double) transfered * 100) / (double) filesize;
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            System.out.println("Sending progress message: " + decimalFormat.format(d) + "%");
        }else {
            System.out.println("Progress message: " + transfered);
        }
    }
    public boolean count(long count){
        if(isEnd)return false;
        if(!isScheduled){
            start();
        }
        add(count);
        return true;
    }
    public void end(){
        setEnd(true);
    }
    public void setFilesize(long filesize){this.filesize = filesize;}
    private synchronized void add(long count){
        transfered = transfered + count;
    }
    public synchronized long getTransfered(){
        return  transfered;
    }
    private synchronized void setTransfered(long transfered){
        this.transfered = transfered;
    }
    private synchronized void setEnd(boolean isEnd){
        this.isEnd = isEnd;
    }
    public void init(int op, String src, String dest, long max){
        // Not used for putting InputStream
    }
}
