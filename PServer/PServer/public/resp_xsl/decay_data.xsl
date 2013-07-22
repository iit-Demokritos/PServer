<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
    <html>
        <head>
            <title>View from Table: decay_data</title>
        </head>
        <body>
            <br></br>
            <h2>decay data: users, features, timestamps</h2>
            <p></p>
            <b>Tables:</b> decay_data
            <br></br>
            <b>Description:</b> A selection of (user, feature, timestamp) from the decay data.
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
                        <th>
                            <xsl:value-of select="timestamp"/>
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
