<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- ========================================================== -->
<!-- Release status page                                        -->
<!-- ========================================================== -->

  <xsl:include href="../chunker.xsl"/>
  <xsl:include href="../website.xsl"/>

  <xsl:template match="trackers">
    <xsl:apply-templates select="." mode="ungrouped.items"/>
  </xsl:template>
  
  <xsl:template match="trackers" mode="ungrouped.items">
    <xsl:variable name="title">Ungrouped items</xsl:variable>
    <xsl:call-template name="write.chunk">
      <xsl:with-param name="filename">ungrouped-items.html</xsl:with-param>
      <xsl:with-param name="content">
        <xsl:call-template name="make.page">
          <xsl:with-param name="title" select="$title"/>
          <xsl:with-param name="main-content">
            <xsl:call-template name="make.header">
              <xsl:with-param name="title" select="$title"/>
            </xsl:call-template>
            <xsl:call-template name="list.trackers" />
          </xsl:with-param>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>

    <xsl:apply-templates select="tracker"/>
  </xsl:template>

  <xsl:template match="tracker">
    <xsl:variable name="title">Ungrouped <xsl:value-of select="@name"/></xsl:variable>
    <xsl:call-template name="write.chunk">
      <xsl:with-param name="filename"><xsl:value-of select="@id"/>-ungrouped.html</xsl:with-param>
      <xsl:with-param name="content">
        <xsl:call-template name="make.page">
          <xsl:with-param name="title" select="$title"/>
          <xsl:with-param name="main-content">
            <xsl:call-template name="make.header">
              <xsl:with-param name="title" select="$title"/>
            </xsl:call-template>
            <xsl:call-template name="tracker.items">
              <xsl:with-param name="status" select="'Open'"/>
            </xsl:call-template>
            <xsl:call-template name="tracker.items">
              <xsl:with-param name="status" select="'Closed'"/>
            </xsl:call-template>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="list.trackers">
    
    <span class="bodyBlack"><B>Trackers:</B></span>
    <xsl:for-each select="tracker">
      <a href="{@id}-ungrouped.html"><xsl:value-of select="@name"/></a><br/>
    </xsl:for-each>
  </xsl:template>


  <xsl:template name="tracker.items">
    <xsl:param name="status"/>
    
    <xsl:variable name="status.items" select="item[status=$status]"/>
    <xsl:variable name="current.items" select="$status.items[version='None']"/>

    <h3><xsl:value-of select="$status"/> items</h3>

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
    <xsl:if test="$current.items">
<!--      <th ><span class="bodyBlack"><B><xsl:value-of select="$tracker"/></B></span></th>-->
    <xsl:apply-templates select="$current.items"/>
    </xsl:if>
    </TABLE>
  </xsl:template>



  <xsl:template match="item">

    <TR BGCOLOR="#dababa">
      <TD NOWRAP=""><span class="bodyBlack"><xsl:value-of select="@id"/></span></TD>
      <TD><A HREF="http://jira.codehaus.org/{url}"><xsl:apply-templates select="summary"/></A></TD>
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
      <A HREF="http://jira.codehaus.org/secure/BrowseProject.jspa?id=10401{../url}"><xsl:apply-templates select="text()"/></A>
    </span>
  </xsl:template>

</xsl:stylesheet>
