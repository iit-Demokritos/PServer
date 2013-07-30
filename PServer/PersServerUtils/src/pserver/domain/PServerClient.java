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
package pserver.domain;

import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pserver.data.SHA2;

/**
 *
 * @author alexm
 */
public class PServerClient {
    
    private String name; //Pserver client has a name
    private String pass; // a password
    private String sha2pass; //and the password is stored as an SHA2 hash value
    private String salt;    //with some salt
    
    public PServerClient() {
        name = null;
        pass = null;
        sha2pass = null;
        salt = null;
    }
    
    public PServerClient( String name, String pass, String salt ) {
        this.name = name;
        this.pass = pass;
        this.salt = salt;
        try {
            this.sha2pass = SHA2.encrypt( salt + pass );
        } catch (NoSuchAlgorithmException ex) {   
            ex.printStackTrace();         
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
        try {
            this.sha2pass = SHA2.encrypt( salt + pass );
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
    }

    public String getSHA2pass() {
        return sha2pass;
    }

    public void setSHA2pass(String sha2pass) {
        this.sha2pass = sha2pass;
    }
    
    public String getSalt() {
        return salt;
    }
    
    public void setSalt(String salt) {
        this.salt = salt;
        try {
            this.sha2pass = SHA2.encrypt( salt + pass );
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
    }

}
