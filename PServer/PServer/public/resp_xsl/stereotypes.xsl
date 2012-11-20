<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
    <html>
        <head>
            <title>View from Table: stereotypes</title>
        </head>
        <body>
            <br></br>
            <h2>stereotypes</h2>
            <p></p>
            <b>Tables:</b> stereotypes
            <br></br>
            <b>Description:</b> A selection from the existing stereotypes.
            <p></p>
            <table border="1" cellpadding="4">
                <xsl:for-each select="result/row">
                    <tr>
                        <th>
                            <xsl:value-of select="str"/>
                        </th>
                        <th>
                            <xsl:value-of select="rule"/>
                        </th>
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
