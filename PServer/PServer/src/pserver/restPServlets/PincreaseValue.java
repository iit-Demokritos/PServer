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


        PSparameters.add("com", "incval");
        PSparameters.add("usr", parameters.getVal(parameters.indexOfKey("username", 0)));


        if (parameters.qpIndexOfKeyNoCase("featurelist") != -1) {
            String features = (String) parameters.getVal(parameters.indexOfKey("featurelist", 0));

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


//        //        DebugLines
//        for (int i = 0; i < PSparameters.size(); i++) {
//            System.out.println("===>  " + PSparameters.getKey(i) + " == " + PSparameters.getVal(i));
//
//        }


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
