<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/TR/WD-xsl">
<xsl:template match="/">
    <html>
        <head>
            <title>Table Rows Count</title>
        </head>
        <body>
            <br></br>
            Relevant rows: <b><xsl:value-of select="result/row/num_of_rows"/></b>
            <p></p>
            <a href="/">Back to home</a>
            <p></p>
        </body>
    </html>
</xsl:template>
</xsl:stylesheet>