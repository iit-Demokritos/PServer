<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
    <html>
        <head>
            <title>View from Table: user_profiles</title>
        </head>
        <body>
            <br></br>
            <h2>user profiles: users, features, values</h2>
            <p></p>
            <b>Tables:</b> user_profiles
            <br></br>
            <b>Description:</b> A selection of (user, feature, value) from the user profiles.
            <p></p>
            <table border="1" cellpadding="4">
                <xsl:for-each select="result/row">
                    <tr>
                        <th>
                            <xsl:value-of select="usr"/>
                        </th>
                        <th>
                            <xsl:value-of select="ftr"/>
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
