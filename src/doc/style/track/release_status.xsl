<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- ========================================================== -->
<!-- Release status page                                        -->
<!-- ========================================================== -->

  <xsl:include href="../chunker.xsl"/>
  <xsl:include href="../website.xsl"/>

  <xsl:template match="trackers">
    <xsl:apply-templates select="." mode="release.status"/>
  </xsl:template>

  <xsl:template match="trackers" mode="release.status">
    <xsl:variable name="title">0.9.2 Release Status</xsl:variable>
    <xsl:call-template name="write.chunk">
      <xsl:with-param name="filename">release-0.9.2-status.html</xsl:with-param>
      <xsl:with-param name="content">
        <xsl:call-template name="make.page">
          <xsl:with-param name="title" select="$title"/>
          <xsl:with-param name="main-content">
            <xsl:call-template name="make.header">
              <xsl:with-param name="title" select="$title"/>
            </xsl:call-template>
            <xsl:call-template name="tracker.items">
              <xsl:with-param name="current.version" select="'0.9.1'"/>
              <xsl:with-param name="next.version" select="'0.9.2'"/>
              <xsl:with-param name="status" select="'Open'"/>
            </xsl:call-template>
            <xsl:call-template name="tracker.items">
              <xsl:with-param name="current.version" select="'0.9.1'"/>
              <xsl:with-param name="next.version" select="'0.9.2'"/>
              <xsl:with-param name="status" select="'Closed'"/>
            </xsl:call-template>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="tracker.items">
    <xsl:param name="current.version"/>
    <xsl:param name="next.version"/>
    <xsl:param name="status"/>
    
    <h3><xsl:value-of select="$status"/> items</h3>

    <TABLE WIDTH="900" BORDER="0" CELLSPACING="1" CELLPADDING="2">
        <TR BGCOLOR="">
            <TD ALIGN="MIDDLE"><span class="bodyBlack"><B>Tracker</B></span></TD>
            <TD ALIGN="MIDDLE"><span class="bodyBlack"><B>Request ID</B></span></TD>
            <TD ALIGN="MIDDLE"><span class="bodyBlack"><B>Summary</B></span></TD>
            <TD ALIGN="MIDDLE"><span class="bodyBlack"><B>Date</B></span></TD>
            <TD ALIGN="MIDDLE"><span class="bodyBlack"><B>Assigned To</B></span></TD>
            <TD ALIGN="MIDDLE"><span class="bodyBlack"><B>Submitted By</B></span></TD>
            <TD ALIGN="MIDDLE"><span class="bodyBlack"><B>Version</B></span></TD>
            <TD ALIGN="MIDDLE"><span class="bodyBlack"><B>Status</B></span></TD>
        </TR>
        <xsl:call-template name="items">
          <xsl:with-param name="tracker" select="'Patches'"/>
          <xsl:with-param name="version" select="$current.version"/>
          <xsl:with-param name="status" select="$status"/>
        </xsl:call-template>
        <xsl:call-template name="items">
          <xsl:with-param name="tracker" select="'Support Requests'"/>
          <xsl:with-param name="version" select="$current.version"/>
          <xsl:with-param name="status" select="$status"/>
        </xsl:call-template>
        <xsl:call-template name="items">
          <xsl:with-param name="tracker" select="'Bugs'"/>
          <xsl:with-param name="version" select="$current.version"/>
          <xsl:with-param name="status" select="$status"/>
        </xsl:call-template>
        <xsl:call-template name="items">
          <xsl:with-param name="tracker" select="'Tasks'"/>
          <xsl:with-param name="version" select="$next.version"/>
          <xsl:with-param name="status" select="$status"/>
        </xsl:call-template>
    </TABLE>
  </xsl:template>


  <xsl:template name="items">
    <xsl:param name="version"/>
    <xsl:param name="tracker"/>
    <xsl:param name="status"/>
    

    <xsl:variable name="tracker.items" select="tracker[@name=$tracker]"/>
    <xsl:variable name="status.items" select="$tracker.items/item[status=$status]"/>
    <xsl:variable name="current.items" select="$status.items[version=$version]"/>

    <xsl:if test="$current.items">
<!--      <th ><span class="bodyBlack"><B><xsl:value-of select="$tracker"/></B></span></th>-->
    <xsl:apply-templates select="$current.items"/>
    </xsl:if>
  </xsl:template>



  <xsl:template match="item">

    <TR BGCOLOR="#dababa">
      <TD NOWRAP=""><span class="bodyBlack"><xsl:value-of select="../@name"/></span></TD>
      <TD><span class="bodyBlack"><xsl:value-of select="@id"/></span></TD>
      <TD><A HREF="http://www.sourceforge.net{url}"><xsl:apply-templates select="summary"/></A></TD>
      <TD><xsl:apply-templates select="date"/></TD>
      <TD><xsl:apply-templates select="assigned-to"/></TD>
      <TD><xsl:apply-templates select="submitted-by"/></TD>
      <TD><xsl:apply-templates select="version"/></TD>
      <TD><xsl:apply-templates select="status"/></TD>
    </TR>

  </xsl:template>
  
  <xsl:template match="date|assigned-to|version|submitted-by|status">
    <span class="bodyBlack">
      <xsl:apply-templates select="*|@*|text()"/>
    </span>
  </xsl:template>
  
  <xsl:template match="summary">
    <span class="bodyBlack">
      <A HREF="http://www.sourceforge.net{../url}"><xsl:apply-templates select="text()"/></A>
    </span>
  </xsl:template>

</xsl:stylesheet>
