/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
        if (contentTable.length<=2){
            return null;
        }
        totalPage = (((contentTable.length - 4) / MaxResults) + 1);
        ConvertedBuffer.append(contentTable[0] + "\n");
        ConvertedBuffer.append(contentTable[1] + "\n");
        ConvertedBuffer.append(contentTable[2] + "\n");


        if (PageIndex > totalPage ||PageIndex<=0) {
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
