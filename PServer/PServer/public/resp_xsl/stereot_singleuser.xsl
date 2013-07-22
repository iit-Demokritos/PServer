<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
    <html>
        <head>
            <title>View from Table: stereotype_users</title>
        </head>
        <body>
            <br></br>
            <h2>stereotype relevence degrees for a user</h2>
            <p></p>
            <b>Tables:</b> stereotype_users
            <br></br>
            <b>Description:</b> A selection of (stereotype, degree) pairs for a specific user. The degree shows how relevent the stereotype is to the user.
            <p></p>
            <table border="1" cellpadding="4">
                <xsl:for-each select="result/row">
                    <tr>
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
