<?xml version="1.0"?>
<!-- $Id$ -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="news">
        <br/>
        <table width="100%" border="0" cellspacing="1" cellpadding="2" bgcolor="#a9a5de"><tr><td>
        <table width="100%" border="0" cellspacing="1" cellpadding="8" bgcolor="#EDEDED"><tr><td>
                <span class="newsTitle"><xsl:value-of select="title"/></span><br/><br/>
                <span class="newsSummary"><xsl:apply-templates select="summary"/><br/><br/>
                <div align="center"><a href="{url}">[Read More/Comment]</a></div><br/>
                Submitted by <a href="http://sourceforge.net/users/{author}"><xsl:value-of select="author"/></a>, 
                on <xsl:value-of select="date"/> @ <xsl:value-of select="time"/>
                </span>
        </td></tr></table></td></tr></table>

    </xsl:template>
    
</xsl:stylesheet>
