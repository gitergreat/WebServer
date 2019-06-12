package check;


public class Check {
    public static boolean checkreturn(String account,String password){
        boolean checkbool = false;
        if("account".equals(account)&&"password".equals(password)){
            checkbool = true;
        }
        return checkbool;
    }
}
