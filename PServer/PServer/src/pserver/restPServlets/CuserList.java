/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class CuserList implements pserver.pservlets.PService {

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


        int PageIndex;
        if (parameters.qpIndexOfKeyNoCase("pageindex") == -1) {
            PageIndex = 1;
        } else {
            PageIndex = Integer.parseInt(parameters.getVal(parameters.indexOfKey("pageindex", 0)).toString());
        }


        // fix the VectorMap

        PSparameters.add("clnt", parameters.getVal(parameters.indexOfKey("clientcredentials", 0)));


        PSparameters.add("com", "getusrcomp");
        PSparameters.add("ucom", parameters.getVal(parameters.indexOfKey("communityname", 0)));
        PSparameters.add("ftr", parameters.getVal(parameters.indexOfKey("pattern", 0)));


        //call the right service
        int ResponseCode = servlet.service(PSparameters, response, dbAccess);


        PageConverter Pconverter = new PageConverter();
        StringBuffer tempBuffer = Pconverter.PConverter(response.toString(), PageIndex);
        response.delete(0, response.length());
        response.append(tempBuffer);


        StringBuffer tempBuffer2 = converter.RConverter(response.toString(), responseType);
        response.delete(0, response.length());
        response.append(tempBuffer2);

        //DebugLine
        //        System.out.println("=====> " +response.toString() );

        return ResponseCode;

    }
}
