<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
    <html>
        <head>
            <title>Average feature value from Table: num_data</title>
        </head>
        <body>
            <br></br>
            <h2>average feature value</h2>
            <p></p>
            <b>Tables:</b> num_data
            <br></br>
            <b>Description:</b> The average numeric value for the specified feature(s) and (maybe) user.
            <p></p>
            <table border="1" cellpadding="4">
                <xsl:for-each select="result/row">
                    <tr>
                        <th>
                            <xsl:value-of select="avg"/>
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
