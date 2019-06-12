import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.io.*;

class Counting {
	private int num;				//һ���ж��ٴη���
	private int IPnum;				//һ���ж��ٸ���ͬ��IP����
	private String tempstr;			//��ʱ�ַ���
	private String allinfo;			//������Ϣ
	private String differentIPs;	//��ͬ��IP��ַ
	private String nowIP;			//Ŀǰ��η��ʵ�IP��ַ
	private String differentIPInfo;	//��ͬIP��ַ�ķ�����Ϣ
	private String dirPath;			//�����ļ���·��
	
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
	
	//�ж��ļ��Ƿ����
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
	//��ȡ�ļ��е���Ϣ��readFile��
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
			differentIPs += strTake.nextToken();//¼��IP��Ϣ
			differentIPs += " ";
			
			if(tempstr.substring(0, 5).equals("Total")){//��"Total"ʱ��ֹͣ¼��
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
		//��֮ǰ������ʱ����ȥһ����Total:��
		if(norepetitionIPs.length == 1) {
			IPnum = 1;
		}
		else {
			IPnum = norepetitionIPs.length - 1;
		}
		//���㲻ͬ��IP��ַ�ֱ�����˶��ٴη�����
		for(i = 0; i < norepetitionIPs.length; i++) {
			count = 0;
			for(j = 0; j < allIPs.length; j++) {
				if(norepetitionIPs[i].equals(allIPs[j])) {
					count++;
				}
			}
			//������Total:ʱ����ʹ��
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
	
	//���������������ж�һ���ַ����ǲ�������
	public boolean isNumeric(String str){
		try{
			Integer.parseInt(str);
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}
}