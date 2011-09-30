<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="/">
            <xsl:for-each select="//doc">
                <doc>
                    <xsl:for-each select="field">
                        <xsl:copy>
                            <xsl:attribute name="name">
                                <xsl:value-of select="@name"/>
                            </xsl:attribute><xsl:value-of select="translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')"/>
                        </xsl:copy>
                    </xsl:for-each>
                </doc>
            </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>