/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
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
public class Cgetcommusers implements pserver.pservlets.PService {

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

        PService servlet = new pserver.pservlets.Communities();
        VectorMap PSparameters = new VectorMap(parameters.size() + 1);
        VectorMap tempMap = null;
        ResponseConverter converter = new ResponseConverter();

        // fix the VectorMap
        PSparameters.add("clnt", 
                parameters.getVal(parameters.indexOfKey("clientcredentials", 0)));


        PSparameters.add("com", "getcommusers");
        
        PSparameters.add("name", 
                parameters.getVal(parameters.indexOfKey("name", 0)));

        if (parameters.qpIndexOfKeyNoCase("usrpattern") != -1) {
            PSparameters.add("usrpattern", 
                    parameters.getVal(parameters.indexOfKey("usrpattern", 0)));
        }

////        DebugLines
//        for(int i=0; i<PSparameters.size();i++){
//            System.out.println("===>  "+PSparameters.getKey(i)+" == "+PSparameters.getVal(i));
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