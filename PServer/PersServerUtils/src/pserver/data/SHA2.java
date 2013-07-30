/* 
 * Copyright 2011 NCSR "Demokritos"
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");   
 * you may not use this file except in compliance with the License.   
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/

package pserver.data;
import java.security.*;
import java.math.*;

public class SHA2 {
    
    public SHA2() {
    }
    
    static public String encrypt(String input) throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(input.getBytes());//transforms password into SHA2 hash
        StringBuilder sb = new StringBuilder();
        BigInteger hash=new BigInteger(1,md.digest());
        sb.append(hash.toString(16));//transform bytes into hexadeciman integer string
        return sb.toString();
    }
    
}
