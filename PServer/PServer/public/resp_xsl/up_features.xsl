<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
    <html>
        <head>
            <title>View from Table: up_features</title>
        </head>
        <body>
            <br></br>
            <h2>features and default values</h2>
            <p></p>
            <b>Tables:</b> up_features
            <br></br>
            <b>Description:</b> A selection of (feature, default value) pairs.
            <p></p>
            <table border="1" cellpadding="4">
                <xsl:for-each select="result/row">
                    <tr>
                        <th>
                            <xsl:value-of select="ftr"/>
                        </th>
                        <td>
                            <xsl:value-of select="defval"/>
                        </td>
                    </tr>
                </xsl:for-each>
            </table>
            <br></br>
            <a href="/">Back to home</a>
            <p></p>
        </body>
    </html>
</xsl:template>
</xsl:stylesheet>
