package MAIN;

public class Search {
    public String[] getString(){
        FileReader fileReader = new FileReader();
        fileReader.setFilename("contact.txt");
        String fileString = fileReader.getFileString();
        String[] dividedString = fileString.split("\r\n");
        return dividedString;
    }

    public String findName(String name){
        String[] dividedString = getString();
        for(int i=0;i<dividedString.length;i++){
            String[] temp = dividedString[i].split(":");
            if(name.equals(temp[0])){
                System.out.println(temp[1]);
                return temp[1];
            }
        }
        return null;
    }

    public String findPhoneNum(String phoneNum){
        String[] dividedString = getString();
        for(int i=0;i<dividedString.length;i++){
            String[] temp = dividedString[i].split(":");
            if(phoneNum.equals(temp[1])){
                System.out.println(temp[0]);
                return temp[0];
            }
        }
        return null;
    }
}
