<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
  xmlns:xalanredirect="org.apache.xalan.xslt.extensions.Redirect"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:import href="track/release_status.xsl"/>
<!--  <xsl:import href="track/ungrouped-items.xsl"/> -->

  <xsl:template match="trackers">
    <xsl:apply-templates select="." mode="release.status"/>
<!--
    <xsl:apply-imports select="." mode="ungrouped.items"/>
-->    
    <xsl:apply-imports/>
  </xsl:template>


<!-- ========================================================== -->
<!-- Unused                                                     -->
<!-- ========================================================== -->

  <xsl:template match="trackers/tracker/foo">

    <xsl:variable name="filename">tracker-<xsl:value-of select="@id"/>.html</xsl:variable>

    <xsl:call-template name="write.chunk">
      <xsl:with-param name="filename" select="$filename"/>
      <xsl:with-param name="content">
        <p>
          <h2><xsl:value-of select="@name"/></h2>
        </p>
        <TABLE WIDTH="900" BORDER="0" CELLSPACING="1" CELLPADDING="2">
            <TR BGCOLOR="">
                <TD ALIGN="MIDDLE"><span class="bodyBlack"><B>Request ID</B></span></TD>
                <TD ALIGN="MIDDLE"><span class="bodyBlack"><B>Summary</B></span></TD>
                <TD ALIGN="MIDDLE"><span class="bodyBlack"><B>Date</B></span></TD>
                <TD ALIGN="MIDDLE"><span class="bodyBlack"><B>Assigned To</B></span></TD>
                <TD ALIGN="MIDDLE"><span class="bodyBlack"><B>Submitted By</B></span></TD>
                <TD ALIGN="MIDDLE"><span class="bodyBlack"><B>Version</B></span></TD>
                <TD ALIGN="MIDDLE"><span class="bodyBlack"><B>Status</B></span></TD>
            </TR>
            <xsl:apply-templates select="*"/>
        </TABLE>
      </xsl:with-param>
    </xsl:call-template>

  </xsl:template>


</xsl:stylesheet>


