<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id$ -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="project/navSections">
    <xsl:for-each select="section">
      <table border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td valign="top" align="left">
            <xsl:choose>
              <xsl:when test="@url">
                <xsl:variable name="url">
                  <xsl:call-template name="link-convertor">
                    <xsl:with-param name="href" select="@url"/>
                    </xsl:call-template>
                  </xsl:variable>
                  <a href="{$url}"><span class="subMenuOn"><xsl:value-of select="@name"/></span></a>
                </xsl:when>
                <xsl:otherwise>
                  <span class="subMenuOn"><xsl:value-of select="@name"/></span>
                </xsl:otherwise>
              </xsl:choose>
          </td>
        </tr>
        <xsl:for-each select="navLink">
          <xsl:variable name="url">
            <xsl:call-template name="link-convertor">
              <xsl:with-param name="href" select="url"/>
              </xsl:call-template>
            </xsl:variable>
            <tr>
              <td valign="top" align="left">
                <a href="{$url}"><span class="subMenuOff">&#160;&#160;&#160;
                <xsl:value-of select="display"/></span></a>
              </td>
            </tr>
          </xsl:for-each>
      </table>
    </xsl:for-each>
        <img src="images/dotTrans.gif" width="1" height="15" border="0"/><br/>
        <img src="images/line_sm.gif" width="105" height="3" border="0"/><br/>

          <A href="http://sourceforge.net"> 
            <IMG  src="http://sourceforge.net/sflogo.php?group_id=44351"
                  width="88" height="31" border="0" alt="SourceForge Logo"/>
          </A>
  </xsl:template>
  
</xsl:stylesheet>
