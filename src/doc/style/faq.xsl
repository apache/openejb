<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id$ -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html" indent="no"/>

<xsl:variable name="project" select="document('../project.xml')/project"/>

<xsl:include href="util.xsl"/>
<xsl:include href="leftNav.xsl"/>
<xsl:include href="keywords.xsl"/>
<xsl:include href="searchForm.xsl"/>

       
<xsl:template match="/">
<html>

    <head>
        <xsl:apply-templates select="keywords"/>

        <xsl:if test="/faqs">
            <title><xsl:value-of select="/faqs/name"/></title>
        </xsl:if>
        
        <link rel="stylesheet" href="default.css"/>
    </head>

    <body bgcolor="#ffffff" link="#6763a9" vlink="#6763a9"
        topmargin="0" bottommargin="0" leftmargin="0" marginheight="0" marginwidth="0">

    <a name="top"/>

    <table border="0" cellpadding="0" cellspacing="0" width="712" height="400">

        <tr>
            <td width="20" valign="top" align="left" bgcolor="#7270c2">
                <img src="images/dotTrans.gif" width="1" height="1" border="0"/>
            </td>
        <td width="95" valign="top" align="left" bgcolor="#7270c2"><img
          src="images/dotTrans.gif" width="1" height="1" border="0"/></td>
        <td width="7" valign="top" align="left"><img src="images/dotTrans.gif" border="0"
          width="1" height="1"/></td>
        <td width="40" valign="top" align="left"><img
          src="images/dotTrans.gif" width="40" height="6" border="0"/></td>
        <td width="440" valign="top" align="left"><img
          src="images/top_2.gif"  width="440" height="6" border="0"/></td>
        <td width="240" valign="top" align="left"><xsl:element
          name="img"><xsl:attribute name="src">images/top_3.gif</xsl:attribute>
        <xsl:attribute name="width">240</xsl:attribute>
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
        <td width="440" valign="middle" align="left">
          
          <xsl:for-each select="/*/super">
            <span class="bodyBlack">
            <xsl:element name="a">
            <xsl:attribute name="href"><xsl:value-of select="concat(@id, '.html')"/></xsl:attribute>
            <xsl:value-of select="."/>
            </xsl:element></span>
             <xsl:if test="not(position()=last())"><img src="images/grayDot.gif" width="20" height="13" border="0"/></xsl:if>
          </xsl:for-each>
          
        <br/>
        <img src="images/dotTrans.gif" width="1" height="2" border="0"/></td>
        <td width="240" height="20" valign="top" align="left">&#160;</td>
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
        <td width="440" valign="top" align="left"><img
          src="images/line_light.gif" width="440" height="3" border="0"/></td>
        <td width="240" valign="top" align="left"><img
          src="images/dotTrans.gif" border="0" width="1" height="1"/></td>
      </tr>

      <tr>
        <td bgcolor="#7270c2" valign="top" align="left"><img
          src="images/dotTrans.gif" width="20" height="10" border="0"/></td>

        <td width="95" bgcolor="#7270c2" valign="top" align="left"><img src="images/dotTrans.gif" width="1" height="2" border="0"/><br/>
        <xsl:apply-templates select="$project/navSections"/></td>

        <td width="7" bgcolor="#a9a5de" valign="top" align="left">&#160;</td>
        <td width="40" valign="top" align="left">&#160;</td>
        <td rowspan="4" width="440" valign="top">
          <p/>

          <table cellpadding="4" cellspacing="2" width="100%">
            
              <tr>
                <td valign="top" align="left">
                    <br/>
                    <img border="0" height="55" hspace="0" src="{$project/logo}" vspace="0" width="200"/><br/>
                    <img border="0" height="7" hspace="0" src="images/dotTrans.gif"/><br/>
                </td>
              </tr>
              
            <tr>
              <td><span class="pageTitle"><xsl:apply-templates select="/faqs/name"/></span>
              </td>
            </tr>

            <xsl:if test="/faqs/introduction">
              <tr>
                <td><xsl:apply-templates select='/faqs/introduction/.'/></td>
              </tr>
            </xsl:if>
            
            <xsl:if test="/faqs/faq">
              <tr>
                <td><ul>
                    <xsl:for-each select='/faqs/faq'>
                        <li>
                            <span class="toc">
                               <xsl:element name="a">
                                   <xsl:attribute name="href">#<xsl:value-of select='answer/@id'/></xsl:attribute>
                                   <xsl:value-of select='question'/>
                               </xsl:element>
                            </span>
                        </li>
                    </xsl:for-each>
                </ul></td>
              </tr>

              <xsl:for-each select='/faqs/faq'>
                  <tr>
                    <td>
                        <xsl:element name="a">
                            <xsl:attribute name="name"><xsl:value-of select='answer/@id'/></xsl:attribute>
                            <h2><xsl:value-of select='question'/></h2>
                        </xsl:element><xsl:apply-templates select='answer/.'/>
                    </td>
                  </tr>
              </xsl:for-each>
            </xsl:if>
            
          </table>    
        <!-- now show the sections themselves -->
      
      </td>
      <td width="240" height="5" valign="top" align="left">
        &#160;        
        </td>
      </tr>

      <!-- line row -->

      <tr height="5">
        <td width="20" height="5" bgcolor="#7270c2" valign="top" align="left">&#160;</td>
        <td width="95" height="5" bgcolor="#7270c2" valign="top">&#160;</td>
        <td width="7" height="5" bgcolor="#a9a5de" valign="top" align="left">&#160;</td>
        <td width="40" height="5" valign="top" align="left">&#160;</td>
        <td width="240" height="5" valign="top" align="left">&#160;</td>
      </tr>

      <!-- content row -->

      <tr>
        <td width="20" height="5" bgcolor="#7270c2" valign="top" align="left">&#160;</td>
        <td width="95" bgcolor="#7270c2" valign="top" align="left">&#160;</td>
        <td width="7" bgcolor="#a9a5de" valign="top" align="left">&#160;</td>
        <td width="40" valign="top" align="left">&#160;</td>
        <td width="240" valign="top" align="left">&#160;</td>
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
        <td width="240" height="100%" valign="top" align="left">&#160;</td>
      </tr>

      <!-- extra  row -->

      <tr height="5">
        <!--td width="20" height="25" valign="top" align="left">&#160;</td>
        <td width="95" height="25" valign="top" align="left">&#160;</td>
        <td width="7" height="25" valign="top" align="left">&#160;</td-->
        <td width="40" height="25" valign="top" align="left">&#160;</td>
        <td width="440" height="25" valign="bottom" align="left">
          <br/><br/>
          <img src="images/line_light.gif"  border="0" width="440" height="3"  /><br/>
          <p/>
          <span class="bodyGrey">
            <xsl:for-each select="$project/notice">
              <small><xsl:copy-of select="."/><br/>&#xA0;<br/></small>
            </xsl:for-each>
          </span>
          <p/>
          &#160;
        </td>
        <td width="240" height="25" valign="top" align="left">&#160;</td>
      </tr>

    </table>

  </body>

  </html>
  </xsl:template>


  <!-- Templates for sections and headers -->
  <xsl:template match="/component">
    <xsl:variable name="level" select="count(ancestor::*)"/>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="header">
    <xsl:apply-templates select="*"/>
  </xsl:template>


  <!-- Templates for HTML correction -->

  <xsl:template match="*|@*">
    <xsl:copy>
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="small">
    <span class="bodyGrey">
      <xsl:copy-of select="*|text()"/>
    </span>
  </xsl:template>

  <xsl:template match="p">
    <p>
      <span class="bodyBlack">
        <xsl:apply-templates select="*|@*|text()"/>
      </span>
    </p>
  </xsl:template>

  <xsl:template match="td">
    <td>
      <xsl:copy-of select="@*"/>
      <span class="bodyGrey">
      <xsl:apply-templates select="*|@*|text()"/>
      </span>
    </td>
  </xsl:template>

  <xsl:template match="ul">
    <table border="0" cellpadding="2" cellspacing="2" width="100%">
      <tr><td colspan="2" height="5"></td></tr>
      <span class="bodyGrey"><xsl:apply-templates/></span>
    </table>
  </xsl:template>

  <xsl:template match="ul/li">
    <tr>
      <td align="left" valign="top" width="10">-</td>
      <td align="left" valign="top"><span class="bodyGrey"><xsl:apply-templates/></span></td>
    </tr>
  </xsl:template>

  <xsl:template match="pre">
    <span class="bodyGrey">
      <pre><xsl:apply-templates/></pre>
    </span>
  </xsl:template>


  <xsl:template match="api">
    <xsl:choose>
      <xsl:when test="@package">
        <a href="api/{translate(@package,'.','/')}/package-summary.html"><xsl:copy-of select="."/></a>
      </xsl:when>
      <xsl:when test="@class">
        <a href="api/{translate(@class,'.','/')}.html#{.}"><xsl:value-of select="."/></a>
      </xsl:when>
      <xsl:otherwise>
        <a href="api/{translate(.,'.','/')}.html"><xsl:copy-of select="."/></a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="url">
    <a href="{.}"><xsl:copy-of select="."/></a>
  </xsl:template>

  <xsl:template match="email">
    <a href="mailto:{.}"><xsl:copy-of select="."/></a>
  </xsl:template>


  <!-- Templates for special content -->

  <xsl:template match="body-note">
    <hr size="1" noshadow=""/><span class="bodyGrey"><xsl:apply-templates/><hr size="1" noshadow=""/></span>
  </xsl:template>

  <xsl:template match="note">
   <table border="0" cellpadding="0" cellspacing="0" width="420" bgcolor="#7270c2">
      <tr>
       <td bgcolor="#7270c2">
        <span class="note-caption">
         <xsl:choose>
          <xsl:when test="@caption">
           &#160;<xsl:value-of select="@caption"/>
          </xsl:when>
          <xsl:otherwise>&#160;NOTE</xsl:otherwise>
         </xsl:choose>
        </span>
       </td>
      </tr>
   <tr>
   <td bgcolor="#7270c2">
    <table border="0" cellpadding="7" cellspacing="2" width="100%" bgcolor="#7270c2">
    <tr>
    <td bgcolor="#FFFFFF">
    <span class="note">
      <xsl:apply-templates/>
    </span>
    </td>
    </tr>
    </table>
    </td>
    </tr>
    </table>
  </xsl:template>

  <xsl:template match="codeBlock">
    <span class="codeBlock">
      <pre><xsl:apply-templates/></pre>
    </span>
  </xsl:template>

  <xsl:template match="file">
    <table border="0" cellpadding="0" cellspacing="0" width="420">
    <tr>
    <td bgcolor="#c0c0c0"><i><span class="code-title"><xsl:value-of select="@name"/></span></i></td>
    </tr>
    <tr>
    <td bgcolor="#e0e0e0">
    <span class="code-block">
      <pre><xsl:apply-templates/></pre>
    </span>
    </td>
    </tr>
    </table>
  </xsl:template>

  <xsl:template match="code-block">
    <table border="0" cellpadding="0" cellspacing="0" width="440">
    <tr>
    <td bgcolor="#e0e0e0">
    <span class="code-block">
      <pre><xsl:apply-templates/></pre>
    </span>
    </td>
    </tr>
    </table>
  </xsl:template>
  
  <xsl:template match="code">
    <span class="code-block">
      <xsl:apply-templates/>
    </span>
  </xsl:template>

  <xsl:template match="tab">
     <img src="images/dotTrans.gif" width="10" height="1" border="0"/>
  </xsl:template>

  <xsl:template match="term">
    <span class="bodyTerm">
      <xsl:apply-templates/>
    </span>
  </xsl:template>
  
  <xsl:template match="code/comment">
    <span class="bodyGrey">
      <font color="red"><xsl:apply-templates/></font>
    </span>
  </xsl:template>
  
  <xsl:template match="command">
    <span class="command"><xsl:apply-templates/></span>
  </xsl:template>


</xsl:stylesheet>

