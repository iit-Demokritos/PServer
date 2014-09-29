<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <html>
            <head>
                <title>Community profile view</title>
            </head>
            <body>
                <br></br>
                <h2>Community profile: 
                    <xsl:for-each select="result">
                        <xsl:value-of select="community"/>
                    </xsl:for-each>
                </h2>
                <p></p>
                <table border="1" cellpadding="4">
                    
                    <xsl:for-each select="result/row">
                        <tr>
                            <td>
                                <xsl:value-of select="ftr_name"/>
                            </td>
                            <td>
                                <xsl:value-of select="ftr_value"/>
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
