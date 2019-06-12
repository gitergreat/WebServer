import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.StringTokenizer;

class ELogFunction {
	private String logPath;
	private String time;
	private String errorType = "[error]";//默认为[error]
	private String IPAddress;
	private String errorMessage = "";
	private String username = "-"; 			//如果存在访问信息，则设置用户名
	
	public void serUserName(String username) {
		this.username = username;
	}
	
	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}
	
	public void setTime(String date) {
		time = "["+date+"]";
	}
	
	public void setErrorType(String errorType) {
		this.errorType = "["+errorType+"]";
	}
	
	public void setIPAddress(Socket socket) {
		//[client 127.0.0.1]
		IPAddress = "[client "+socket.getInetAddress().getHostAddress()+"]";
	}
	
	public void setErrorMessage(String error, String request) {
		if(!username.equals("-")) {
			errorMessage = "user " + username + " : ";
		}
		errorMessage += error + " ";
		StringTokenizer strTake = new StringTokenizer(request, "\n");
		String temp = null;
		if(strTake.hasMoreTokens()){
			temp = strTake.nextToken();
		}
		strTake = new StringTokenizer(temp);
		strTake.nextToken();
		//获取访问地址
		if(strTake.hasMoreTokens()){
			errorMessage += strTake.nextToken();
		}
	}

	public void createNewLog() {
		File file = new File("log/error.log");
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
		File writeName = new File(logPath + "/error.log");
		createNewLog();
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(writeName, true), "gbk"));
		out.write(time+" ");
		out.write(errorType+" ");
		out.write(IPAddress+ " ");
		out.write(errorMessage+"\r\n");
		out.flush();
		out.close();
	}
	
}
