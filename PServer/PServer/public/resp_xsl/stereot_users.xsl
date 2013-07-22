<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
    <html>
        <head>
            <title>View from Table: stereotype_users</title>
        </head>
        <body>
            <br></br>
            <h2>stereotype users: users, stereotypes, degrees</h2>
            <p></p>
            <b>Tables:</b> stereotype_users
            <br></br>
            <b>Description:</b> A selection of (user, stereotype, degree) from the stereotypes assigned to users with a relevence degree.
            <p></p>
            <table border="1" cellpadding="4">
                <xsl:for-each select="result/row">
                    <tr>
                        <th>
                            <xsl:value-of select="usr"/>
                        </th>
                        <th>
                            <xsl:value-of select="str"/>
                        </th>
                        <td>
                            <xsl:value-of select="deg"/>
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
