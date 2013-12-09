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

package pserver.utilities;

import pserver.WebServer;
import pserver.data.DBAccess;
import pserver.data.VectorMap;

/**
 * This is a simple class that checks the client credentials of a request
 *
 * @author Nick Zorbas <nickzorb@gmail.com>
 *
 * @since 1.1
 */
public abstract class ClientCredentialsChecker {

    /**
     * Check whether there are valid clinet credentials in the query parameters
     *
     * @param dbAccess the pserver's database connection handler
     * @param queryParam the current request's query parameters
     * @return returnes whether the check was successful or not (true or false)
     */
    public static boolean check(DBAccess dbAccess, VectorMap queryParam) {
        int clntIdx = queryParam.qpIndexOfKeyNoCase("clnt");
        if (clntIdx == -1) {
            WebServer.win.log.error("-Missing client credentials");
            return false;
        }
        String clientCredentials = (String) queryParam.getVal(clntIdx);
        if (clientCredentials.indexOf('|') <= 0) {
            WebServer.win.log.error("-Malformed client credentials \"" + clientCredentials + "\"");
            return false;
        }
        String clientName = clientCredentials.substring(0, clientCredentials.indexOf('|'));
        String clientPass = clientCredentials.replace(clientName, "").replace("|", "");
        try {
            dbAccess.connect();
            boolean res = dbAccess.checkClientCredentials(clientName, clientPass);
            if (res == false) {
                WebServer.win.log.error("-Invalid client credentials \"" + clientCredentials + "\"");
            }
            return res;
        } catch (Exception e) {
            WebServer.win.log.error("-Database exception: " + e.getLocalizedMessage());
            return false;
        }
    }
}
