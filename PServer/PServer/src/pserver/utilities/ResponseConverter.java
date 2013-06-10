/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pserver.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.wink.json4j.utils.XML;
import org.xml.sax.SAXException;

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
        CreateXml(RString);

        if (RType.equals("text/xml")) {
            ConvertXML();
        } else {
            ConvertJson();
        }

        return ConvertedBuffer;
    }

    private void ConvertXML() {
        ConvertedBuffer.append(ConvertedResponse);
    }

    private void ConvertJson() {

        //TODO: convert xml to json

        String jsonObj = null;

        try {
            jsonObj = XML.toJson(new File("./convert.xml"));

        } catch (SAXException ex) {
            Logger.getLogger(ResponseConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ResponseConverter.class.getName()).log(Level.SEVERE, null, ex);
        }

        String json = jsonObj.toString();
        CreateJson(json);

        ConvertedBuffer.append(json);

    }

    private void CreateXml(String xmlInput) {

        PrintWriter pw = null;

        try {
            pw = new PrintWriter(new FileWriter("./convert.xml"));  //If the file already exists, start writing at the end of it.

            pw.println(xmlInput);                                  // write to convert.xml
            pw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //Close the PrintWriter
            if (pw != null) {
                pw.close();
            }

        }

    }
    private void CreateJson(String jsonInput) {

        PrintWriter pw = null;

        try {
            pw = new PrintWriter(new FileWriter("./convert.json"));  //If the file already exists, start writing at the end of it.

            pw.println(jsonInput);                                  // write to convert.xml
            pw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //Close the PrintWriter
            if (pw != null) {
                pw.close();
            }

        }

    }
}
