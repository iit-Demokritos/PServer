/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pserver.utilities;

/**
 * This class converts the xml response to any given type
 *
 * @author Panagiotis Giotis <giotis.p@gmail.com>
 */
public class ResponseConverter {

    private String ConvertedResponse;
    private StringBuffer ConvertedBuffer;

    public StringBuffer RConverter(String RString, String RType) {
        ConvertedBuffer = new StringBuffer();
        ConvertedResponse = RString;

        if (RType.equals("xml")) {
            ConvertXML();
        } else if (RType.equals("json")) {
            ConvertJson();
        }

        return ConvertedBuffer;
    }

    private void ConvertXML() {
        ConvertedBuffer.append(ConvertedResponse);
    }

    private void ConvertJson() {

        //TODO: convert xml to json
        
        
        
        
        ConvertedBuffer.append(ConvertedResponse);

    }
}
