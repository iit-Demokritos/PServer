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

//===================================================================
// WServer
//
// The actual web server. Waits for requests at the specified port
// and creates a new ReqWorker class to handle each request at
// another port.
//===================================================================
package pserver.logic;

import java.net.*;
import java.io.*;
import javax.net.ssl.*;
//import com.sun.net.ssl.*;
import java.security.*;
import javax.net.*;

import pserver.*;

public class WServer extends Thread {
    private ServerSocket srvSock = null;
    
    public WServer(int port, int backlog,boolean sslOn) {
        super();
        String localHost = null;
        int localPort = -1;
        try {
            // create SSLServerSocket on specified port
            if(sslOn==true)
                srvSock =(ServerSocket)getSSLServerSocket(port, backlog);
            else
                srvSock = new ServerSocket(port, backlog);
            localPort = srvSock.getLocalPort();
            localHost = (InetAddress.getLocalHost()).toString();            
        } catch (Exception e) {            
            //e.printStackTrace();
            WebServer.flog.forceWriteln("Problem resolving localhost: " + e);
            //WebServer.terminate(false);  //quit application if cannot start            
            localHost = "";
        }
        //log msgs
        WebServer.win.log.markStart();
        WebServer.win.log.report("Server machine is: " + localHost);
        WebServer.win.log.report("Server local port is: " + localPort);
        WebServer.win.log.report("======== SERVER UP ========");
        WebServer.win.log.report("");  //empty line
        WebServer.flog.writeln("Server started at " + localHost +
                ", port " + localPort);
    }
    @Override
    public void run() {
        if (srvSock == null) return;
        //SSLSocket sock;
        Socket sock;
        ReqWorker worker;
        while (true) {
            try {
                //sock =(SSLSocket) srvSock.accept();  //waits until request arrives
                sock =srvSock.accept();  //waits until request arrives
                //in case the 'ReqWorker' class is overriden, the following
                //line must be adapted to create an object of the subclass
                worker = new PSReqWorker(sock);  //the subclass here!
                worker.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.gc();
        }
    }
    @Override
    protected void finalize(){
        if (srvSock != null){
            try {
                srvSock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            srvSock = null;
        }
    }
    ServerSocket getSSLServerSocket(int port,int backlog) throws Exception {
        // Make sure that JSSE is available
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        // A keystore is where keys and certificates are kept
        // Both the keystore and individual private keys should be password protected
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(new FileInputStream(KEYSTORE), KEYSTOREPW);
        // A KeyManagerFactory is used to create key managers
        javax.net.ssl.KeyManagerFactory kmf = javax.net.ssl.KeyManagerFactory.getInstance("SunX509");
        // Initialize the KeyManagerFactory to work with our keystore
        kmf.init(keystore, KEYPW);
        // An SSLContext is an environment for implementing JSSE
        // It is used to create a ServerSocketFactory
        javax.net.ssl.SSLContext sslc = javax.net.ssl.SSLContext.getInstance("SSLv3");
        // Initialize the SSLContext to work with our key managers
        sslc.init(kmf.getKeyManagers(), null, null);
        // Create a ServerSocketFactory from the SSLContext
        ServerSocketFactory ssf = sslc.getServerSocketFactory();
        // Socket to me
        SSLServerSocket serverSocket =
                (SSLServerSocket) ssf.createServerSocket(port, backlog);
        // Authenticate the client?
        serverSocket.setNeedClientAuth(requireClientAuthentication);
        // Return a ServerSocket on the desired port (443)
        return serverSocket;
    }
    
    String KEYSTORE = "certs";
    char[] KEYSTOREPW = "serverkspw".toCharArray();
    char[] KEYPW = "serverpw".toCharArray();
    boolean requireClientAuthentication=false;;
    
}
