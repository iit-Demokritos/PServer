/*
 * MD5.java
 *
 */

package pserver.data;
import java.util.*;
import java.security.*;
import java.math.*;

public class MD5 {
    
    public MD5() {
    }
    
    static public String encrypt(String input) throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(input.getBytes());//transforms password into md5 hash
        StringBuffer sb = new StringBuffer();
        BigInteger hash=new BigInteger(1,md.digest());
        sb.append(hash.toString(16));//transform bytes into hexadeciman integer string
        return sb.toString();
    }
    
}
