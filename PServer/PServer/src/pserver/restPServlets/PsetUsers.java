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
public class PsetUsers implements pserver.pservlets.PService {

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
            responseType = pserver.pservlets.PService.json;
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
