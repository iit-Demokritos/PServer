<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
    <html>
        <head>
            <title>View from Table: decay_groups</title>
        </head>
        <body>
            <br></br>
            <h2>feature groups and decay rates</h2>
            <p></p>
            <b>Tables:</b> decay_groups
            <br></br>
            <b>Description:</b> A selection of (feature group, decay rate) pairs, ordered by decay rate descenting.
            <p></p>
            <table border="1" cellpadding="4">
                <xsl:for-each select="result/row">
                    <tr>
                        <th>
                            <xsl:value-of select="grp"/>
                        </th>
                        <td>
                            <xsl:value-of select="rate"/>
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
