import java.io.*;

class MyFileReader {

    private String filename;//filename为所读取文件的名称
    private String fileString;
    private static String WEB_ROOT;

    public MyFileReader(){
        //this.WEB_ROOT = null;
        this.fileString = null;
        this.filename = null;
    }

    public MyFileReader(String WEB_ROOT){
        this.WEB_ROOT = WEB_ROOT;
    }

    public MyFileReader(String WEB_ROOT, String filename){
        this.WEB_ROOT = WEB_ROOT;
        this.filename = filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileString(){
        File file = new File(WEB_ROOT,filename);
        if(!file.exists()){
            System.out.println("Can not open the file! file does not exit!");
            return null;
        }
        try {
            InputStream inputStream = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String temp = bufferedReader.readLine();
            StringBuffer stringBuffer = new StringBuffer();
            while(temp != null){
                stringBuffer.append(temp);
                stringBuffer.append("\r\n");
                temp = bufferedReader.readLine();
            }
            fileString = stringBuffer.toString();
            bufferedReader.close();
            inputStream.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return fileString;
    }

}
