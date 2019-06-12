import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.StringTokenizer;

class LogFunction {
	//日志每行的内容
	private String logPath;
	private String IPAddress;
	private final static String none = "-";
	private String authName = "-";
	private String time;
	private String requestInfo;
	private int stateCode;
	private long totalBytes;
	
	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}
	
	public void setIPAddress(Socket socket) {
		IPAddress = socket.getInetAddress().getHostAddress();
	}
	
	public void setAuthName(String authName) {
		this.authName = authName;
	}
	
	public void setTime(String date) {
		time = "["+date+"]";
	}
	
	public void setRuquestInfo(String info) {
		StringTokenizer strTake = new StringTokenizer(info, "\n");
		if(strTake.hasMoreTokens()){
			requestInfo = '"'+strTake.nextToken()+'"';
		}
	}
	
	public void setStateCode(int stateCode) {
		this.stateCode = stateCode;
	}
	
	public void settotalBytes(long totalBytes) {
		this.totalBytes = totalBytes;
	}
	
	public void createNewLog() {
		File file = new File(logPath + "/access.log");
		if(!file.exists()) {
			try {
				file.createNewFile();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void writeTofile() throws IOException, FileNotFoundException, UnsupportedEncodingException {
		File writeName = new File(logPath + "/access.log");
		createNewLog();
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(writeName, true), "gbk"));
		out.write(IPAddress+" ");
		out.write(none+" ");
		out.write(authName+" ");
		out.write(time+" ");
		out.write(requestInfo+" ");
		out.write(stateCode+ " ");
		out.write(totalBytes+"\r\n");
		out.flush();
		out.close();
	}
}
