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
public class PsetAttributes implements pserver.pservlets.PService {

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


        PSparameters.add("com", "setattrdef");
       

        if (parameters.qpIndexOfKeyNoCase("Attributes") != -1) {
            String attributes = (String) parameters.getVal(parameters.indexOfKey("Attributes", 0));

            //        {"john","kostas"}
            attributes = attributes.replace("{", "");
            attributes = attributes.replace("}", "");
            attributes.trim();
            String[] AttributesTable = attributes.split(",");

            for (String tempatr : AttributesTable) {
                tempatr = tempatr.replace("\"", "");
                String[] AttributesValues = tempatr.trim().split(":");

                PSparameters.add(AttributesValues[0], AttributesValues[1]);

            }

        }

        //call the right service
        int ResponseCode = servlet.service(PSparameters, response, dbAccess);

        response = converter.RConverter(response.toString(), responseType);


        return ResponseCode;

    }
}
