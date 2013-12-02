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
import pserver.utilities.ResponseConverter;

/**
 *
 * @author Panagiotis Giotis <giotis.p@gmail.com>
 */
public class PsetUsers implements pserver.pservlets.PService {

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

        // fix the VectorMap

        PSparameters.add("clnt", parameters.getVal(parameters.indexOfKey("clientcredentials", 0)));


        PSparameters.add("com", "setusr");
        PSparameters.add("usr", parameters.getVal(parameters.indexOfKey("username", 0)));

        if (parameters.qpIndexOfKeyNoCase("attr") != -1) {
            String attributes = (String) parameters.getVal(parameters.indexOfKey("attr", 0));

            //        {"john","kostas"}
            attributes = attributes.replace("{", "");
            attributes = attributes.replace("}", "");
            attributes.trim();
            String[] AttributesTable = attributes.split(",");

            for (String tempatr : AttributesTable) {
                tempatr = tempatr.replace("\"", "");
                String[] AttributesValues = tempatr.trim().split(":");

                PSparameters.add("attr_" + AttributesValues[0], AttributesValues[1]);

            }

        }

        if (parameters.qpIndexOfKeyNoCase("ftr") != -1) {
            String features = (String) parameters.getVal(parameters.indexOfKey("ftr", 0));

            //        {"john","kostas"}
            features = features.replace("{", "");
            features = features.replace("}", "");
            features.trim();
            String[] FeaturesTable = features.split(",");

            for (String tempftr : FeaturesTable) {
                tempftr = tempftr.replace("\"", "");
                String[] FeaturesValues = tempftr.trim().split(":");

                PSparameters.add("ftr_" + FeaturesValues[0], FeaturesValues[1]);

            }

        }


        //call the right service
        int ResponseCode = servlet.service(PSparameters, response, dbAccess);

        StringBuffer tempBuffer = converter.RConverter(response.toString(), responseType);
        response.delete(0, response.length());
        response.append(tempBuffer);

        //DebugLine
        //        System.out.println("=====> " +response.toString() );


        return ResponseCode;
    }
}
