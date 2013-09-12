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

package pserver.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.wink.json4j.utils.XML;
import org.xml.sax.SAXException;

/**
 * This class converts the xml response to any given type
 *
 * @author Panagiotis Giotis <giotis.p@gmail.com>
 * @author Nick Zorbas <nickzorb@gmail.con>
 */
public class ResponseConverter {

    private String ConvertedResponse;
    private StringBuffer ConvertedBuffer;

    /**
     *
     * @param RString
     * @param RType
     * @return
     */
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
            String json = jsonObj.toString();
            CreateJson(json);
            ConvertedBuffer.append(json);
        } catch (SAXException ex) {
            Logger.getLogger(ResponseConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ResponseConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(ResponseConverter.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void CreateXml(String xmlInput) {

        BufferedWriter pw = null;

        try {
            pw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("./convert.xml"), Charset.forName("UTF-8")));  //If the file already exists, start writing at the end of it.
            // write to convert.xml
            if (xmlInput.equals("null")) {
                pw.append("<result></result>");
            } else {
                pw.append(xmlInput);
            }
            pw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //Close the PrintWriter
            if (pw != null) {
                try {
                    pw.close();
                } catch (IOException ex) {
                    Logger.getLogger(ResponseConverter.class.getName()).log(Level.SEVERE, null, ex);
                }
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
