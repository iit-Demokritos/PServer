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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.wink.json4j.utils.XML;
import org.xml.sax.SAXException;

/**
 * This class converts the xml response to any given type
 * current support just json conversion
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
        ConvertedBuffer.append(ConvertedResponse);

        if (RType.equals("text/xml")) {
            return ConvertedBuffer;
        } else {
            ConvertJson();
        }
        return ConvertedBuffer;
    }

    /**
     * Convert xml to json 
     */
    private void ConvertJson() {
        //convert xml to json
        String jsonObj = null;

        try {
            jsonObj = XML.toJson(new ByteArrayInputStream(this.ConvertedBuffer.toString().getBytes(Charset.forName("UTF-8"))));
            String json = jsonObj.toString();
            ConvertedBuffer.delete(0, ConvertedBuffer.length());
            ConvertedBuffer.append(json);
        } catch (SAXException ex) {
            Logger.getLogger(ResponseConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ResponseConverter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
