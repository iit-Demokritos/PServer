/*
 * This class describes the characteristics of an application client for PServer
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
    private String md5pass; //and the password is stored as an MD5 hash value
    
    public PServerClient() {
        this.name = null;
        this.pass = null;
        this.md5pass = null;
    }
    
    public PServerClient( String name, String pass ) throws NoSuchAlgorithmException{
        this.name = name;
        this.pass = pass;
        this.md5pass = MD5.encrypt( pass );
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
