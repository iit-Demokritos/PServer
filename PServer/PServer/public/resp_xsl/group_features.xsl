<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
    <html>
        <head>
            <title>View from Tables: ftr_groups,ftrgroup_features</title>
        </head>
        <body>
            <br></br>
            <h2>feature groups: group</h2>
            <p></p>
            <b>Tables:</b> Feature Groups
            <br></br>
            <b>Description:</b> A selection of features from a feature group
            <p></p>
            <table border="1" cellpadding="4">
                <xsl:for-each select="result/row">
                    <tr>
                        <td>
                            <xsl:value-of select="feature"/>
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
