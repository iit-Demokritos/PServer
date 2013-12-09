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

package pserver.restPServlets;

import pserver.data.DBAccess;
import pserver.data.VectorMap;
import pserver.pservlets.PService;
import pserver.utilities.PageConverter;
import pserver.utilities.ResponseConverter;

/**
 *
 * @author Panagiotis Giotis <giotis.p@gmail.com>
 */
public class PgetAttributes implements pserver.pservlets.PService {

    private String responseType = pserver.pservlets.PService.xml;

    /**
     *
     * @return
     */
    @Override
    public String getMimeType() {
        return responseType;
    }

    /**
     *
     * @param params
     * @throws Exception
     */
    @Override
    public void init(String[] params) throws Exception {
        if (params.length < 1) {
            return;
        }
        if (params[0].endsWith("xml")) {
            responseType = pserver.pservlets.PService.xml;
        } else {
            responseType = pserver.pservlets.PService.json;
        }
    }

    /**
     *
     * @param parameters
     * @param response
     * @param dbAccess
     * @return
     */
    @Override
    public int service(VectorMap parameters, StringBuffer response, DBAccess dbAccess) {
        PService servlet = new pserver.pservlets.Personal();
        VectorMap PSparameters = new VectorMap(parameters.size() + 1);
        VectorMap tempMap = null;
        ResponseConverter converter = new ResponseConverter();

        int PageIndex;
        if (parameters.qpIndexOfKeyNoCase("pageindex") == -1) {
            PageIndex = 0;
        } else {
            PageIndex = Integer.parseInt(parameters.getVal(parameters.indexOfKey("pageindex", 0)).toString());
        }

        // fix the VectorMap
        PSparameters.add("clnt", parameters.getVal(parameters.indexOfKey("clientcredentials", 0)));

        PSparameters.add("com", "getattrdef");

        PSparameters.add("attr", parameters.getVal(parameters.indexOfKey("attributesPattern", 0)));

////        DebugLines
//        for(int i=0; i<PSparameters.size();i++){
//            System.out.println("===>  "+PSparameters.getKey(i)+" == "+PSparameters.getVal(i));
//            
//        }
        //call the right service
        int ResponseCode = servlet.service(PSparameters, response, dbAccess);

        if (PageIndex != 0) {
            PageConverter Pconverter = new PageConverter();
            StringBuffer tempBuffer = Pconverter.PConverter(response.toString(), PageIndex);
            response.delete(0, response.length());
            response.append(tempBuffer);
        }

        StringBuffer tempBuffer2 = converter.RConverter(response.toString(), responseType);
        response.delete(0, response.length());
        response.append(tempBuffer2);

        //DebugLine
        //        System.out.println("=====> " +response.toString() );
        return ResponseCode;
    }
}
