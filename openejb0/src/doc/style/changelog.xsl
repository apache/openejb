<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id$ -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="text"/>

    <xsl:param name="version" select="empty"/>

    <xsl:template match="/">
        <xsl:for-each select="ChangeLog/Version">
            <xsl:if test="@released = 'true'"><xsl:apply-templates select="."/></xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="Version">
Version <xsl:value-of select="@id"/><xsl:text>  </xsl:text><xsl:value-of select="@releaseDate"/>
-------------------------------
CVS-TAG: <xsl:value-of select="@cvsTag"/><xsl:text>

</xsl:text>
        <xsl:apply-templates select="Entry"/>
    </xsl:template>

    <xsl:template match="Entry">
        <xsl:choose>
            <xsl:when test="@task != ''">- [task: <xsl:value-of select="@task"/>] <xsl:value-of select="."/><xsl:text>
</xsl:text></xsl:when>
            <xsl:when test="@bug != ''">- [bug: <xsl:value-of select="@bug"/>] <xsl:value-of select="."/><xsl:text>
</xsl:text></xsl:when>
            <xsl:otherwise>- <xsl:value-of select="."/><xsl:text>
</xsl:text></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>

