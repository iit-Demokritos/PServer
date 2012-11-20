<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
    <html>
        <head>
            <title>View from Table: user_attributes</title>
        </head>
        <body>
            <br></br>
            <h2>profile values for a user</h2>
            <p></p>
            <b>Tables:</b> user_attributes
            <br></br>
            <b>Description:</b> A selection of (attribute, value) pairs from the attributes of a specific user.
            <p></p>
            <table border="1" cellpadding="4">
                <xsl:for-each select="result/row">
                    <tr>
                        <th>
                            <xsl:value-of select="attr"/>
                        </th>
                        <td>
                            <xsl:value-of select="val"/>
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
