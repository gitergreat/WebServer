package serverprog;

import java.io.*;
import java.security.MessageDigest;
import java.math.BigInteger;

//md5值计算的类
public class md5 {
    private File file;
    private MessageDigest messageDigest;

    public md5(File file) { this.file = file; }

    public String getMD5() {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            messageDigest = MessageDigest.getInstance("MD5");
            int length = 0;
            while ((length = fileInputStream.read(buffer)) != -1) {
                messageDigest.update(buffer, 0, length);
            }
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInteger = new BigInteger(1, messageDigest.digest());
        return bigInteger.toString(16);
    }

}