<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id$ -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- Templates for links -->

  <xsl:template match="a">
    <a>
      <xsl:if test="@href">
        <xsl:variable name="href">
          <xsl:call-template name="link-convertor">
            <xsl:with-param name="href" select="@href"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:attribute name="href">
          <xsl:value-of select="$href"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:for-each select="@*[not(name(.)='href')]">
        <xsl:copy-of select="."/>
      </xsl:for-each>
      <xsl:apply-templates/>
    </a>
  </xsl:template>

  <xsl:template name="link-convertor">
    <xsl:param name="href" select="empty"/>
    <xsl:choose>
      <xsl:when test="starts-with($href,'http:')">
        <xsl:value-of select="$href"/>
      </xsl:when>
      <xsl:when test="not(contains($href,'.xml'))">
        <xsl:value-of select="$href"/>
      </xsl:when>
      <xsl:otherwise><xsl:value-of select="substring-before($href, '.xml')"/>.html</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="javadoc">
    <xsl:choose>
      <xsl:when test="@type='package'">
        <a href="apidocs/{translate(.,'.','/')}/package-summary.html"><xsl:copy-of select="."/></a>
      </xsl:when>
      <xsl:otherwise>
        <a href="apidocs/{translate(.,'.','/')}.html"><xsl:copy-of select="."/></a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>

