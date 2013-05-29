/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class PincreaseValue implements pserver.pservlets.PService {

    private String responseType = pserver.pservlets.PService.xml;

    @Override
    public String getMimeType() {
        return responseType;
    }

    @Override
    public void init(String[] params) throws Exception {
        if (params.length < 1) {
            return;
        }
        if (params[0].endsWith("xml")) {
            responseType = pserver.pservlets.PService.xml;
        } else {
            responseType = pserver.pservlets.PService.txt;
        }
    }

    @Override
    public int service(VectorMap parameters, StringBuffer response, DBAccess dbAccess) {
        PService servlet = new pserver.pservlets.Personal();
        VectorMap PSparameters = new VectorMap(parameters.size() + 1);
        VectorMap tempMap = null;
        ResponseConverter converter = new ResponseConverter();

        // fix the VectorMap

        PSparameters.add("clnt", parameters.getVal(parameters.indexOfKey("clientcredentials", 0)));


        PSparameters.add("com", "incval");
        PSparameters.add("usr", parameters.getVal(parameters.indexOfKey("username", 0)));


        if (parameters.qpIndexOfKeyNoCase("FeatureList") != -1) {
            String features = (String) parameters.getVal(parameters.indexOfKey("FeatureList", 0));

            //        {"john","kostas"}
            features = features.replace("{", "");
            features = features.replace("}", "");
            features.trim();
            String[] FeaturesTable = features.split(",");

            for (String tempftr : FeaturesTable) {
                tempftr = tempftr.replace("\"", "");
                String[] FeaturesValues = tempftr.trim().split(":");

                PSparameters.add(FeaturesValues[0], FeaturesValues[1]);

            }

        }


        //call the right service
        int ResponseCode = servlet.service(PSparameters, response, dbAccess);

        response = converter.RConverter(response.toString(), responseType);


        return ResponseCode;
    }
}
