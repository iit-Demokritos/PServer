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
public class PdeleteUsers implements pserver.pservlets.PService {

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


        PSparameters.add("com", "remusr");

        if (parameters.qpIndexOfKeyNoCase("users") != -1) {
            String users = (String) parameters.getVal(parameters.indexOfKey("users", 0));

            //        {"john","kostas"}
            users = users.replace("{", "");
            users = users.replace("}", "");
            users.trim();
            String[] UsersTable = users.split(",");
            for (String tempusr : UsersTable) {
                tempusr = tempusr.replace("\"", "");
                PSparameters.add("usr", tempusr.trim());
            }

        } else {
            PSparameters.add("usr", "*");
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
