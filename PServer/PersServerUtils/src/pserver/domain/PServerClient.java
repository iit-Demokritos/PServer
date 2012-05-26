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
import pserver.data.MD5;

/**
 *
 * @author alexm
 */
public class PServerClient {
    
    private String name; //Pserver client has a name
    private String pass; // a password
    private String md5pass = null; //and the password is stored as an MD5 hash value
    
    public PServerClient() {
        this.name = null;
        this.pass = null;
        this.md5pass = null;
    }
    
    public PServerClient( String name, String pass ) {
        this.name = name;
        this.pass = pass;
        try {
            this.md5pass = MD5.encrypt( pass );
        } catch (NoSuchAlgorithmException ex) {            
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
        try {
            this.pass = pass;
            this.md5pass = MD5.encrypt(pass);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
    }

    public String getMd5pass() {
        return md5pass;
    }

    public void setMd5pass(String md5pass) {
        this.md5pass = md5pass;
    }

}
