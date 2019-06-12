import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.io.*;

class Counting {
	private int num;				//一共有多少次访问
	private int IPnum;				//一共有多少个不同的IP访问
	private String tempstr;			//临时字符串
	private String allinfo;			//所有信息
	private String differentIPs;	//不同的IP地址
	private String nowIP;			//目前这次访问的IP地址
	private String differentIPInfo;	//不同IP地址的访问信息
	private String dirPath;			//设置文件夹路径
	
	public void setDirPath(String dirPath) {
		this.dirPath = dirPath;
	}
	
	
	public void writeIPToFile(Socket socket, String date) throws IOException {
		createNewLog();
		readFile();
		countDifferentIPs(socket);
		writeFile(date);
		WriteStatics();
	}
	
	//判断文件是否存在
	public void createNewLog() {
		File file = new File(dirPath+"/ip_address.log");
		if(!file.exists()) {
			try {
				file.createNewFile();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	//读取文件中的信息到readFile中
	public void readFile() throws IOException {
		allinfo = "";
		differentIPs = "";
		String pathname = dirPath + "/ip_address.log"; 
		num = 1;
		FileReader reader = new FileReader(pathname); 
		BufferedReader br = new BufferedReader(reader);
		if(br.readLine() == null) {
			br.close();
			return;
		}
		while ((tempstr = br.readLine()) != null) {
			
			StringTokenizer strTake = new StringTokenizer(tempstr);
			differentIPs += strTake.nextToken();//录入IP信息
			differentIPs += " ";
			
			if(tempstr.substring(0, 5).equals("Total")){//当"Total"时，停止录入
				break;
			}
			
			allinfo += tempstr;
			allinfo += "\r\n";
			num++;
		}
		br.close();
	 }


	public void writeFile(String date) throws IOException {
		File writeName = new File(dirPath + "/ip_address.log");
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(writeName), "gbk"));
		out.write("IP_Address\t\tTime\r\n");
		out.write(allinfo);
		out.write(nowIP);
		out.write("\t\t");
		out.write(date);
		out.write("\r\n");
		out.write("Total: "+ num + "\r\n\r\n");
		out.close();
	}
	
	public void countDifferentIPs(Socket socket) {
		int count;
		IPnum = 0;
		int i = 0;
		int j = 0;
		differentIPInfo = "";
		nowIP = socket.getInetAddress().getHostAddress();
		differentIPs += nowIP;
		StringTokenizer strTake = new StringTokenizer(differentIPs);
		String[] allIPs = new String[strTake.countTokens()];
		while(strTake.hasMoreTokens()) {
			allIPs[i] = strTake.nextToken();
			i++;
		}
		
		Set<String> set = new HashSet<String>();
		for (i = 0; i < allIPs.length; i++) {
            set.add(allIPs[i]);
        }
		String[] norepetitionIPs = (String[]) set.toArray(new String[0]);
		//当之前有数据时，减去一个“Total:”
		if(norepetitionIPs.length == 1) {
			IPnum = 1;
		}
		else {
			IPnum = norepetitionIPs.length - 1;
		}
		//计算不同的IP地址分别访问了多少次服务器
		for(i = 0; i < norepetitionIPs.length; i++) {
			count = 0;
			for(j = 0; j < allIPs.length; j++) {
				if(norepetitionIPs[i].equals(allIPs[j])) {
					count++;
				}
			}
			//不等于Total:时，才使用
			if(!norepetitionIPs[i].equals("Total:")) {
				differentIPInfo += "\t";
				differentIPInfo += norepetitionIPs[i];
				differentIPInfo += "\t\t";
				differentIPInfo += Integer.toString(count);
				differentIPInfo += "\r\n";
			}
		}
	}
	
	public void WriteStatics() throws IOException {
		File writeName = new File(dirPath + "/statics.log");
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(writeName), "gbk"));
		out.write("Statistics\tIP_Address\t\tTimes\r\n");
		out.write(differentIPInfo);
		out.write("There are "+ IPnum + " different IP address who reached the server!\r\n\r\n");
		out.flush(); 
		out.close();
	}
	
	//布尔函数，用于判断一个字符串是不是数字
	public boolean isNumeric(String str){
		try{
			Integer.parseInt(str);
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}
}