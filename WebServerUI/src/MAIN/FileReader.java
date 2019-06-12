package MAIN;

import java.io.*;


public class FileReader {

    private String filename;//filename为所读取文件的名称
    private String fileString;

    public FileReader(){
        this.filename = null;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public FileReader(String filename){
        this.filename = filename;
    }

    public String getFileString(){
        File file = new File(filename);
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
