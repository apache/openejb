<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="html" indent="no"/>

  <xsl:variable name="project" select="document('../project.xml')/project"/>
  <xsl:variable name="group_id" select="$project/group_id"/>
  <xsl:variable name="bug_id" select="$project/bug_id"/>
  <xsl:variable name="task_id" select="$project/task_id"/>
  <xsl:variable name="current_release" select="./ChangeLog/Version[attribute::release_id][position()=last()]"/>

  <xsl:include href="topNav.xsl"/>
  <xsl:include href="leftNav.xsl"/>
  <xsl:include href="keywords.xsl"/>
  <xsl:include href="searchForm.xsl"/>


  <!-- Template for document -->

  <xsl:template match="/">
<style>
.openejb_pageTitle {
	font-size: 18px; 
	font-family: arial, "Helvetica", "Arial", "sans-serif"; 
	line-height: 28px; 
	font-weight: bold; 
	color: #666666;
}
.openejb_codeBlock { 
	font-size: 12px; 
	font-family: courier new, "Tahoma", "Helvetica", "Arial", "sans-serif";
	line-height: 16px; color: #757585;
}
</style>
<hr/>
<span class="openejb_pageTitle">Change Log</span>
<br/>
<h3>Version <xsl:value-of select="$current_release/attribute::id"/> - Release Date: <xsl:value-of select="$current_release/attribute::releaseDate"/></h3>
<hr/>
<p>CVS-TAG: <span class="openejb_codeBlock"><xsl:value-of select="$current_release/attribute::cvsTag"/></span></p>
<ul>
    <xsl:apply-templates select="$current_release/Entry"/>
</ul>
  </xsl:template>

  <xsl:template match="Entry">
      <li>
      <xsl:choose>
          <xsl:when test="@bug">
            <xsl:variable name="aid" select="@bug"/>
              [<xsl:element name="a">
                <xsl:attribute name="href"><xsl:value-of select="concat('http://sourceforge.net/tracker/index.php?func=detail&amp;aid=', $aid, '&amp;group_id=', $group_id, '&amp;atid=', $bug_id)"/></xsl:attribute>
                <xsl:value-of select="@bug"/>
              </xsl:element>] <xsl:value-of select="."/>
          </xsl:when>
          <xsl:when test="@task">
            <xsl:variable name="aid" select="@task"/>
              [<xsl:element name="a">
                <xsl:attribute name="href"><xsl:value-of select="concat('http://sourceforge.net/tracker/index.php?func=detail&amp;aid=', $aid, '&amp;group_id=', $group_id, '&amp;atid=', $task_id)"/></xsl:attribute>
                <xsl:value-of select="@task"/>
              </xsl:element>] <xsl:value-of select="."/>
          </xsl:when>
          <xsl:otherwise>
              <xsl:value-of select="."/>
          </xsl:otherwise>
      </xsl:choose>
      </li>
  </xsl:template>
</xsl:stylesheet>

