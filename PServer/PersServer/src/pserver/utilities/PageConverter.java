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

/**
 *
 * @author Panagiotis Giotis <giotis.p@gmail.com>
 */
public class PageConverter {

    private StringBuffer ConvertedBuffer;

    /**
     *
     * @param RString
     * @param PageIndex
     * @return
     */
    public StringBuffer PConverter(String RString, int PageIndex) {
        ConvertedBuffer = new StringBuffer();
        int totalPage = 2;
        int MaxResults = 20;
        String[] contentTable = RString.split("\\n");
        if (contentTable.length <= 2) {
            return null;
        }
        totalPage = (((contentTable.length - 4) / MaxResults) + 1);
        ConvertedBuffer.append(contentTable[0] + "\n");
        ConvertedBuffer.append(contentTable[1] + "\n");
        ConvertedBuffer.append(contentTable[2] + "\n");

        if (PageIndex > totalPage || PageIndex <= 0) {
            PageIndex = totalPage;
        }
        for (int i = 3; i < contentTable.length - 1; i++) {

            if (totalPage == 1) {
                ConvertedBuffer.append(contentTable[i] + "\n");
            } else {

                if ((((i - 3) / MaxResults) + 1) == PageIndex) {

                    ConvertedBuffer.append(contentTable[i] + "\n");
                }

            }
        }


        ConvertedBuffer.append("<page>" + PageIndex + "/" + totalPage + "</page>\n");
        ConvertedBuffer.append(contentTable[contentTable.length - 1] + "\n");

//        ConvertedBuffer.append(RString);

        return ConvertedBuffer;
    }
}
