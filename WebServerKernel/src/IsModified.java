import java.io.File;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

class IsModified {
    private String HttpMessage;
    private String date;
    private Request request;
    private File file;
    private final String ifModified = "If-Modified-Since";

    IsModified(Request request){
        this.request = request;
        this.HttpMessage = request.getHttpMessage();
        this.file = new File(HttpServer.WEB_ROOT,request.getUri());
    }

    boolean needModified(){
        String[] divideHttpMessage = HttpMessage.split("\r\n");
        for(int i=0;i<divideHttpMessage.length;i++){
//            String[] tempDivideHttpMessage =  divideHttpMessage[i].split(":");
//            if(tempDivideHttpMessage[0].equals(ifModified)){
//                date = tempDivideHttpMessage[1];
//                return true;
//            }
            int index = divideHttpMessage[i].indexOf(":");
            if(index!=-1) {
                String subDividedHttpMessage = divideHttpMessage[i].substring(0, index - 1);
                if (subDividedHttpMessage.equals(ifModified)) {
                    date = divideHttpMessage[i].substring(index + 1, divideHttpMessage[i].length());
                    return true;
                }
            }
        }
        return false;
    }

    boolean isModified(){
        try {
            ///String[] divideString = date.split(":");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
           // Date HttpModifiedDate = simpleDateFormat.parse(divideString[1]);
            Date HttpModifiedDate = simpleDateFormat.parse(date);


            Long lastModified = file.lastModified();
            Date lastModifiedDate = new Date(lastModified);

            if (lastModifiedDate.before(HttpModifiedDate)) {
                return true;
            }
            else { return false; }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
