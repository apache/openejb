<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id$ -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="html" indent="no"/>

  <xsl:variable name="project" select="document('../project.xml')/project"/>
  <xsl:variable name="group_id" select="$project/group_id"/>
  <xsl:variable name="bug_id" select="$project/bug_id"/>
  <xsl:variable name="task_id" select="$project/task_id"/>
  <xsl:variable name="current_release" select="./ChangeLog/Version[attribute::release_id][position()=last()]"/>

  <xsl:include href="util.xsl"/>
  <xsl:include href="topNav.xsl"/>
  <xsl:include href="leftNav.xsl"/>
  <xsl:include href="keywords.xsl"/>
  <xsl:include href="searchForm.xsl"/>


  <!-- Template for document -->

  <xsl:template match="/">
  <html>

  <head>
    <title>Release Plan</title>    
    
    <link rel="stylesheet" href="default.css"/>
  </head>

  <body bgcolor="#ffffff" link="#6763a9" vlink="#6763a9"
        topmargin="0" bottommargin="0" leftmargin="0" marginheight="0" marginwidth="0">

  <a name="top"/>

    <table border="0" cellpadding="0" cellspacing="0" width="712" height="400">

      <tr><td width="20" valign="top" align="left" bgcolor="#7270c2"><img
        src="images/dotTrans.gif" width="1" height="1" border="0"/></td>
        <td width="95" valign="top" align="left" bgcolor="#7270c2"><img
          src="images/dotTrans.gif" width="1" height="1" border="0"/></td>
        <td width="7" valign="top" align="left"><img src="images/dotTrans.gif" border="0"
          width="1" height="1"/></td>
        <td width="40" valign="top" align="left"><img
          src="images/dotTrans.gif" width="40" height="6" border="0"/></td>
        <td width="430" valign="top" align="left" bgcolor="#5A5CB8"><img
          src="images/top_2.gif"  width="430" height="6" border="0"/></td>
        <td width="120" valign="top" align="left" bgcolor="#E24717"><xsl:element
          name="img"><xsl:attribute name="src">images/top_3.gif</xsl:attribute>
        <xsl:attribute name="width">120</xsl:attribute>
        <xsl:attribute name="height">6</xsl:attribute>
        <xsl:attribute name="border">0</xsl:attribute>
      </xsl:element></td></tr>
      <tr>
        <td width="20" bgcolor="#7270c2" valign="top" align="left"><img
          src="images/dotTrans.gif" border="0" width="1" height="1"/></td>
        <td width="95" bgcolor="#7270c2" valign="top" align="left"><img
          src="images/dotTrans.gif" border="0" width="1" height="1"/></td>
        <td width="7" bgcolor="#ffffff" valign="top" align="left"></td>
        <td width="40" valign="top" align="left"><img
          src="images/dotTrans.gif" width="1" height="1" border="0"/></td>
        <td width="430" valign="middle" align="left">
        <xsl:apply-templates select="$project/topNav"/><br/>
        <img src="images/dotTrans.gif" width="1" height="2" border="0"/></td>
        <td width="120" height="20" valign="top" align="left">&#160;</td>
      </tr>
      <tr>
        <td width="20" bgcolor="#7270c2" valign="top" align="left"><img
          src="images/dotTrans.gif" width="20" height="3" border="0"/></td>
        <td width="95" bgcolor="#7270c2" valign="top" align="left"><img
          src="images/line_sm.gif" width="105" height="3" border="0"/></td>
        <td width="7" bgcolor="#a9a5de" valign="top" align="left"><img
          src="images/line_sm.gif" width="7" height="3" border="0"/></td>
        <td width="40" valign="top" align="left"><img
          src="images/line_light.gif" width="40" height="3" border="0"/></td>
        <td width="430" valign="top" align="left"><img
          src="images/line_light.gif" width="430" height="3" border="0"/></td>
        <td width="120" valign="top" align="left"><img
          src="images/dotTrans.gif" border="0" width="1" height="1"/></td>
      </tr>

      <tr>
        <td bgcolor="#7270c2" valign="top" align="left"><img
          src="images/dotTrans.gif" width="20" height="10" border="0"/></td>

        <td width="95" bgcolor="#7270c2" valign="top" align="left"><img src="images/dotTrans.gif" width="1" height="2" border="0"/><br/>
        <xsl:apply-templates select="$project/navSections"/></td>

        <td width="7" bgcolor="#a9a5de" valign="top" align="left">&#160;</td>
        <td width="40" valign="top" align="left">&#160;</td>
        <td rowspan="4" width="430" valign="top">
          <table cols="1" rows="2" border="0" cellpadding="0" cellspacing="0" width="430">
            <tr>
              <td valign="top" align="left"><br/><img border="0" height="50" hspace="0"
                  src="{$project/logo}" vspace="0" width="200"/><br/>
                  <img border="0" height="7" hspace="0" src="images/dotTrans.gif"/><br/>
                        <span class="pageTitle">Release Plan</span><br/>
                  <img border="0" height="1" hspace="0" src="images/dotTrans.gif"/>
              </td>
            </tr>
          </table>
          <p/>

            <xsl:for-each select="./ChangeLog/Version">
              <xsl:if test="@released = 'false'">
                <span class="toc">
                  <a href="#{@id}"><xsl:value-of select="@id"/> - Target release date: <xsl:value-of select="@releaseDate"/></a><br/>
                </span>
              </xsl:if>
            </xsl:for-each>
          

        <br/>

        <!-- now show the sections themselves -->
        <xsl:apply-templates select="ChangeLog/Version"/>
      </td>
      <td width="120" height="5" valign="top"
        align="left">
        <xsl:apply-templates select="document/body/sideimg"/>
        
        <!--
        <a href="http://www.exolab.org"><img
        src="images/logo_exolab.gif" hspace="0" vspace="10" width="77" height="20" border="0"/></a>
        -->
        &#160;        
        </td>
      </tr>

      <!-- line row -->

      <tr height="5">
        <td width="20" height="5" bgcolor="#7270c2" valign="top" align="left">&#160;</td>
        <td width="95" height="5" bgcolor="#7270c2" valign="top">&#160;</td>
        <td width="7" height="5" bgcolor="#a9a5de" valign="top" align="left">&#160;</td>
        <td width="40" height="5" valign="top" align="left">&#160;</td>
        <td width="120" height="5" valign="top" align="left">&#160;</td>
      </tr>

      <!-- content row -->

      <tr>
        <td width="20" height="5" bgcolor="#7270c2" valign="top" align="left">&#160;</td>
        <td width="95" bgcolor="#7270c2" valign="top" align="left">&#160;</td>
        <td width="7" bgcolor="#a9a5de" valign="top" align="left">
          <img src="images/dotTrans.gif" width="1" height="25" border="0"/>
        </td>
        <td width="40" valign="top" align="left">
          <img src="images/dotTrans.gif" width="1" height="25" border="0"/>
        </td>
        <td width="120" valign="top" align="left">&#160;</td>
      </tr>

      <!-- final row -->

      <tr height="5">
        <td width="20" rowspan="2" height="100%" bgcolor="#7270c2" valign="bottom"
          align="left"><img src="images/stripes1.gif" width="20" height="125" border="0"/></td>
        <td width="95" rowspan="2" height="100%" bgcolor="#7270c2" valign="bottom"
          align="left"><img src="images/stripe105.gif" width="105" height="125" border="0"/></td>
        <td width="7" rowspan="2" height="100%" bgcolor="#a9a5de" valign="top"
          align="left">&#160;</td>
        <td width="40" height="100%" valign="top" align="left">&#160;</td>
        <td width="120" height="100%" valign="top" align="left">&#160;</td>
      </tr>

      <!-- extra  row -->

      <tr height="5">
        <!--td width="20" height="25" valign="top" align="left">&#160;</td>
        <td width="95" height="25" valign="top" align="left">&#160;</td>
        <td width="7" height="25" valign="top" align="left">&#160;</td-->
        <td width="40" height="25" valign="top" align="left">&#160;</td>
        <td width="430" height="25" valign="bottom" align="left">
          <br/><br/>
          <img src="images/line_light.gif"  border="0" width="430" height="3"  /><br/>
          <p/>
          <span class="bodyGrey">
            <xsl:for-each select="$project/notice">
              <small><xsl:copy-of select="."/><br/>&#xA0;<br/></small>
            </xsl:for-each>
          </span>
          <p/>
          &#160;
        </td>
        <td width="120" height="25" valign="top" align="left">&#160;</td>
      </tr>

    </table>

  </body>

  </html>
  </xsl:template>


  <!-- Templates for sections and headers -->

  <xsl:template match="ChangeLog/Version">
    <xsl:if test="@released = 'false'">
      <xsl:variable name="release_id" select="@release_id"/>
      <a name="{@id}">
      <h2><xsl:value-of select="@id"/> - Target release date: <xsl:value-of select="@releaseDate"/></h2></a>
      <table border="0" cellpadding="2" cellspacing="2">
        <tr><td colspan="2" height="5"></td></tr>
        <xsl:if test="Entry[attribute::bug]">
            <tr>
                <td colspan="2" height="5"><span class="bodyBlack">Bugs:</span></td>
            </tr>
            <xsl:apply-templates select="Entry[attribute::bug]"/>
        </xsl:if>
        <xsl:if test="Entry[attribute::task]">
            <tr>
                <td colspan="2" height="5"><span class="bodyBlack">Enhancements:</span></td>
            </tr>
            <xsl:apply-templates select="Entry[attribute::task]"/>
        </xsl:if>
        <xsl:apply-templates select="Entry[not (@bug or @task)]"/>
      </table>
    </xsl:if>
  </xsl:template>

  <xsl:template match="Entry">
    <tr>
      <td align="left" valign="top" width="10"><img src="images/grayDot.gif"/></td>
      <td align="left" valign="top"><span class="bodyBlack">
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
      </span></td>
    </tr>
  </xsl:template>

</xsl:stylesheet>

