<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id$ -->
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
<span class="openejb_pageTitle">OpenEJB -- EJB Container System and EJB Server</span>
<br/>
<h3>Release news, <xsl:value-of select="$current_release/attribute::releaseDate"/>.</h3>       
<br/>
<h3>OpenEJB <xsl:value-of select="$current_release/attribute::id"/> - <a href="http://openejb.sf.net">http://openejb.sf.net</a></h3>
<hr/>
<p>Release <xsl:value-of select="$current_release/attribute::id"/> available at: 
<xsl:variable name="current_release_id" select="$current_release/@release_id"/>
<xsl:element name="a">
  <xsl:attribute name="href"><xsl:value-of select="concat('http://sourceforge.net/project/showfiles.php?group_id=', $group_id, '&amp;release_id=', $current_release_id)"/></xsl:attribute>
  <xsl:value-of select="concat('http://sourceforge.net/project/showfiles.php?group_id=', $group_id, '&amp;release_id=', $current_release_id)"/>
</xsl:element></p>
<h3>Change summary</h3>
<p>Usability enhancements, documentation corrections, more options for 
the command line tools, and bug fixes.</p>
<h3>Unpack your distribution</h3>
<p>To install OpenEJB, simply unpack your zip or tar.gz into the
directory where you want OpenEJB to live.</p>
<p>Windows users can download the zip and unpack it with the
WinZip program.</p>
<p>Linux users can download the tar.gz and unpack it with the 
following command:</p>
<span class="openejb_codeBlock">
tar xzvf openejb-<xsl:value-of select="$current_release/attribute::cvsTag"/>.tar.gz
</span>
<p>If you've unpacked OpenEJB into the directory C:\openejb, for
example, then this directory is your OPENEJB_HOME 
directory.  The OPENEJB_HOME directory is referred to in 
various parts of the documentation, so it's good to remember 
where it is.</p>
<h3>Using OpenEJB</h3>
<p>Now all you need to do is move to the OPENEJB_HOME 
directory, the directory where OpenEJB was unpacked, and 
type:</p>
<span class="openejb_codeBlock">
openejb help
</span>
<p>For Windows users, that looks like this:</p>
<span class="openejb_codeBlock">
C:\openejb> openejb help
</span>
<p>For UNIX/Linux/Mac OS X users, that looks like this:</p>
<span class="openejb_codeBlock">
[user@host openejb]# ./openejb.sh help
</span>
<p>You really only need to know two commands to use 
OpenEJB, deploy and start.  Both are completely 
documented and have examples.</p>
<p>For help information and command options, try this:</p>
<span class="openejb_codeBlock">
openejb deploy -help<br/>
openejb start -help
</span>
<p>For examples on using the command and options, try this:</p>
<span class="openejb_codeBlock">
openejb deploy -examples<br/>
openejb start -examples
</span>
<p>That's it!</p>
  </xsl:template>

</xsl:stylesheet>

