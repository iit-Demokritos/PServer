/*
 * Copyright 2013 IIT , NCSR Demokritos - http://www.iit.demokritos.gr,
 *                            SciFY NPO - http://www.scify.org
 *
 * This product is part of the PServer Free Software.
 * For more information about PServer visit http://www.pserver-project.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *                 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * If this code or its output is used, extended, re-engineered, integrated,
 * or embedded to any extent in another software or hardware, there MUST be
 * an explicit attribution to this work in the resulting source code,
 * the packaging (where such packaging exists), or user interface
 * (where such an interface exists).
 *
 * The attribution must be of the form
 * "Powered by PServer, IIT NCSR Demokritos , SciFY"
 */

package pserver.data;
import java.security.*;
import java.math.*;

/**
 * A simple wrapper class for the sha-256 encryption
 *
 * @author scify
 * @author Nick Zorbas <nickzorb@gmail.con>
 * @since 1.1
 */

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
