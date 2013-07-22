<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
    <html>
        <head>
            <title>Decay values</title>
        </head>
        <body>
            <br></br>
            <h2>features and decay values</h2>
            <p></p>
            <b>Description:</b> The requested (feature, decay values) pairs for a user, feature group, and decay rate, ordered by decay value descenting.
            <p></p>
            <table border="1" cellpadding="4">
                <xsl:for-each select="result/row">
                    <tr>
                        <th>
                            <xsl:value-of select="ftr"/>
                        </th>
                        <td>
                            <xsl:value-of select="decay_val"/>
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
