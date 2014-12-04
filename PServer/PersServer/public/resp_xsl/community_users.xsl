<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <html>
            <head>
                <title>Users in community view</title>
            </head>
            <body>
                <br></br>
                <h2>Users in community: 
                    <xsl:for-each select="result">
                        <xsl:value-of select="community"/>
                    </xsl:for-each>
                </h2>
                <p></p>
                <table border="1" cellpadding="4">
                    <th>
                        Users
                    </th>
                    <xsl:for-each select="result/row">
                        <tr>
                            <td>
                                <xsl:value-of select="user"/>
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

