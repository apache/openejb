<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id$ -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="html" indent="no"/>

  <xsl:variable name="project" select="document('../project.xml')/project"/>

  <xsl:include href="util.xsl"/>
  <xsl:include href="topNav.xsl"/>
  <xsl:include href="leftNav.xsl"/>
  <xsl:include href="keywords.xsl"/>
  <xsl:include href="searchForm.xsl"/>
  <xsl:include href="news.xsl"/>


  <!-- Template for document -->

  <xsl:template match="/">
  <html>

  <head>
    <xsl:apply-templates select="keywords"/>
    
    
    <xsl:choose>
      <xsl:when test="/document/properties/title">
        <title>
          <xsl:value-of select="/document/properties/title"/>
          <xsl:if test="/document/properties/sub-title"> -- 
            <xsl:value-of select="/document/properties/sub-title"/>
          </xsl:if>
        </title>
      </xsl:when>
      <xsl:when test="/document/body/title"><title><xsl:value-of select="/document/body/title"/></title></xsl:when>
      <xsl:otherwise><title><xsl:value-of select="$project/title"/></title></xsl:otherwise>
    </xsl:choose>
    
    
    <xsl:choose>
      <xsl:when test="/document/properties/style">
        <xsl:element name="link">
        <xsl:attribute name="rel">stylesheet</xsl:attribute>
        <xsl:attribute name="href"><xsl:value-of select="/document/properties/style"/></xsl:attribute>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <link rel="stylesheet" href="default.css"/>
      </xsl:otherwise>
    </xsl:choose>
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
              <td valign="top" align="left"><br/><img border="0" height="55" hspace="0"
                  src="{$project/logo}" vspace="0" width="200"/><br/>
                   <xsl:if test="not(document[@page-title='none'])">
                       <img border="0" height="7" hspace="0" src="images/dotTrans.gif"/><br/>
                        <xsl:choose>
                          <xsl:when test="/document/body/title">
                            <span class="pageTitle"><xsl:value-of select="/document/body/title"/></span><br/>
                          </xsl:when>
                          <xsl:when test="/document/properties/title">
                            <span class="pageTitle"><xsl:value-of select="/document/properties/title"/></span><br/>
                              <xsl:if test="/document/properties/sub-title">
                                <span class="pageSubTitle"><xsl:value-of select="/document/properties/sub-title"/></span><br/>
                              </xsl:if>
                          </xsl:when>
                        </xsl:choose>
                      <xsl:if test="/document/properties/author">
                        <p>
                        <span class="author">by <xsl:value-of select="/document/properties/author"/></span><br/>
                        </p>
                      </xsl:if>
                      <img border="0" height="1" hspace="0" src="images/dotTrans.gif"/>
                   </xsl:if>
              </td>
            </tr>
          </table>
          <p/>

          <xsl:if test="document/body/header">
            <br/>          
            <xsl:apply-templates select="document/body/header"/>
          </xsl:if>
          <xsl:apply-templates select="document/body/mailing-list"/>

          <!-- build the page navigation first, section by section -->
          <xsl:choose>      
          <xsl:when test="document[@toc='none']"></xsl:when>
          <xsl:when test="document[@toc='numeric']">
                    
            <p/><br/>
            <xsl:for-each select="//section">
              <span class="toc">
                <xsl:choose>
                 <xsl:when test="@ref-id">
                  <xsl:variable name="level" select="count(ancestor::*)"/>
                  <xsl:choose>
                    <xsl:when test='$level=2'>
                      <a href="#{@ref-id}">
                      <xsl:number count="//section" format="1.1" /> 
                      <img src="images/dotTrans.gif" width="5" height="1" border="0"/>
                      <xsl:value-of select="@title"/></a><br/>
                    </xsl:when>
                    <xsl:when test='$level=3'>
                      <img src="images/dotTrans.gif" width="15" height="1" border="0"/>
                      <a href="#{@ref-id}">
                      <xsl:number count="//section" level="multiple" format="1.1" /> 
                      <img src="images/dotTrans.gif" width="5" height="1" border="0"/>
                      <xsl:value-of select="@title"/></a><br/>
                    </xsl:when>
                    <xsl:when test='$level=4'>
                      <img src="images/dotTrans.gif" width="30" height="1" border="0"/>
                      <a href="#{@ref-id}">
                      <xsl:number count="//section" level="multiple" format="1.1" /> 
                      <img src="images/dotTrans.gif" width="5" height="1" border="0"/>
                      <xsl:value-of select="@title"/></a><br/>
                    </xsl:when>
                    <xsl:otherwise>
                      <img src="images/dotTrans.gif" width="45" height="1" border="0"/>
                      <a href="#{@ref-id}">
                      <xsl:number count="//section" level="multiple" format="1.1" /> 
                      <img src="images/dotTrans.gif" width="5" height="1" border="0"/>
                      <xsl:value-of select="@title"/></a><br/>
                    </xsl:otherwise>
                  </xsl:choose>
                 </xsl:when>
                 <xsl:otherwise>
                  <xsl:variable name="level" select="count(ancestor::*)"/>
                  <xsl:choose>
                    <xsl:when test='$level=2'>
                      <a href="#{@title}">
                      <xsl:number count="//section" format="1" /> 
                      <img src="images/dotTrans.gif" width="5" height="1" border="0"/>
                      <xsl:value-of select="@title"/></a><br/>
                    </xsl:when>
                    <xsl:when test='$level=3'>
                      <img src="images/dotTrans.gif" width="15" height="1" border="0"/>
                      <a href="#{@title}">
                      <xsl:number count="//section" format="1" /> 
                      <img src="images/dotTrans.gif" width="5" height="1" border="0"/>
                      <xsl:value-of select="@title"/></a><br/>
                    </xsl:when>
                    <xsl:when test='$level=4'>
                      <img src="images/dotTrans.gif" width="30" height="1" border="0"/>
                      <a href="#{@title}">
                      <xsl:number count="//section" format="1" /> 
                      <img src="images/dotTrans.gif" width="5" height="1" border="0"/>
                      <xsl:value-of select="@title"/></a><br/>
                    </xsl:when>
                    <xsl:otherwise>
                      <img src="images/dotTrans.gif" width="45" height="1" border="0"/>
                      <a href="#{@title}">
                      <xsl:number count="//section" format="1" /> 
                      <img src="images/dotTrans.gif" width="5" height="1" border="0"/>
                      <xsl:value-of select="@title"/></a><br/>
                    </xsl:otherwise>
                  </xsl:choose>
                 </xsl:otherwise>
                </xsl:choose>
              </span>
            </xsl:for-each>
          
          </xsl:when>
          <xsl:otherwise>
            <p/><br/>
            <xsl:for-each select=".//section">
              <span class="toc">
                <xsl:choose>
                 <xsl:when test="@ref-id">
                  <xsl:variable name="level" select="count(ancestor::*)"/>
                  <xsl:choose>
                    <xsl:when test='$level=2'>
                      <a href="#{@ref-id}"><xsl:value-of select="@title"/></a><br/>
                    </xsl:when>
                    <xsl:when test='$level=3'>
                      <img src="images/dotTrans.gif" width="15" height="1" border="0"/>
                      <a href="#{@ref-id}"><xsl:value-of select="@title"/></a><br/>
                    </xsl:when>
                    <xsl:when test='$level=4'>
                      <img src="images/dotTrans.gif" width="30" height="1" border="0"/>
                      <a href="#{@ref-id}"><xsl:value-of select="@title"/></a><br/>
                    </xsl:when>
                    <xsl:otherwise>
                      <img src="images/dotTrans.gif" width="45" height="1" border="0"/>
                      <a href="#{@ref-id}"><xsl:value-of select="@title"/></a><br/>
                    </xsl:otherwise>
                  </xsl:choose>
                 </xsl:when>
                 <xsl:otherwise>
                  <xsl:variable name="level" select="count(ancestor::*)"/>
                  <xsl:choose>
                    <xsl:when test='$level=2'>
                      <a href="#{@title}"><xsl:value-of select="@title"/></a><br/>
                    </xsl:when>
                    <xsl:when test='$level=3'>
                      <img src="images/dotTrans.gif" width="15" height="1" border="0"/>
                      <a href="#{@title}"><xsl:value-of select="@title"/></a><br/>
                    </xsl:when>
                    <xsl:when test='$level=4'>
                      <img src="images/dotTrans.gif" width="30" height="1" border="0"/>
                      <a href="#{@title}"><xsl:value-of select="@title"/></a><br/>
                    </xsl:when>
                    <xsl:otherwise>
                      <img src="images/dotTrans.gif" width="45" height="1" border="0"/>
                      <a href="#{@title}"><xsl:value-of select="@title"/></a><br/>
                    </xsl:otherwise>
                  </xsl:choose>
                 </xsl:otherwise>
                </xsl:choose>
              </span>
            </xsl:for-each>
          </xsl:otherwise>
          </xsl:choose>      
          

        <br/>

        <!-- now show the sections themselves -->
        <xsl:apply-templates select="document/body/section"/>
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

  <xsl:template match="document//section">
    <xsl:variable name="level" select="count(ancestor::*)"/>

    
    <xsl:choose>
      <xsl:when test='$level=2'>
        <xsl:choose>
          <xsl:when test="@ref-id">
            <a name="{@ref-id}">
            <h2><xsl:value-of select="@title"/></h2></a>
          </xsl:when>
          <xsl:otherwise>
            <a name="{@title}">
            <h2><xsl:value-of select="@title"/></h2></a>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test='$level=3'>
        <xsl:choose>
          <xsl:when test="@ref-id">
            <a name="{@ref-id}">
            <h3><xsl:value-of select="@title"/></h3></a>
          </xsl:when>
          <xsl:otherwise>
            <a name="{@title}">
            <h3><xsl:value-of select="@title"/></h3></a>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test='$level=4'>
        <xsl:choose>
          <xsl:when test="@ref-id">
            <a name="{@ref-id}">
            <h4><xsl:value-of select="@title"/></h4></a>
          </xsl:when>
          <xsl:otherwise>
            <a name="{@title}">
            <h4><xsl:value-of select="@title"/></h4></a>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test='$level=5'>
        <xsl:choose>
          <xsl:when test="@ref-id">
            <a name="{@ref-id}">
            <h5><xsl:value-of select="@title"/></h5></a>
          </xsl:when>
          <xsl:otherwise>
            <a name="{@title}">
            <h5><xsl:value-of select="@title"/></h5></a>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="@ref-id">
            <a name="{@ref-id}">
            <h6><xsl:value-of select="@title"/></h6></a>
          </xsl:when>
          <xsl:otherwise>
            <a name="{@title}">
            <h6><xsl:value-of select="@title"/></h6></a>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="header">
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="sideimg">
     <xsl:element name="img">
        <xsl:attribute name="src"><xsl:value-of select="@src"/></xsl:attribute>
        <xsl:attribute name="width"><xsl:value-of select="@width"/></xsl:attribute>
        <xsl:attribute name="height"><xsl:value-of select="@height"/></xsl:attribute>
        <xsl:attribute name="border">0</xsl:attribute>
      </xsl:element><br/>
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
    <table border="0" cellpadding="2" cellspacing="2">
      <tr><td colspan="2" height="5"></td></tr>
      <span class="bodyGrey"><xsl:apply-templates/></span>
    </table>
  </xsl:template>

  <xsl:template match="ul/li">
    <tr>
      <td align="left" valign="top" width="10"><img src="images/grayDot.gif"/></td>
      <td align="left" valign="top"><span class="bodyBlack"><xsl:apply-templates/></span></td>
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
  
  <xsl:template match="note">
   <table border="0" cellpadding="0" cellspacing="0" width="440" bgcolor="#7270c2">
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
  
  <xsl:template match="file">
    <table border="0" cellpadding="0" cellspacing="0" width="440">
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

  <xsl:template match="code">
    <table border="0" cellpadding="0" cellspacing="0" width="440">
    <xsl:if test="@name">
      <tr>
      <td bgcolor="#c0c0c0"><i><span class="code-title"><xsl:value-of select="@name"/></span></i></td>
      </tr>
    </xsl:if>
    <tr>
    <td bgcolor="#e0e0e0">
    <span class="code-block">
      <pre><xsl:apply-templates/></pre>
    </span>
    </td>
    </tr>
    </table>
  </xsl:template>

  <xsl:template match="options">
    <table border="0" cellpadding="6" cellspacing="0" width="440">
      <xsl:apply-templates/>
    </table>
  </xsl:template>
  
  <xsl:template match="options/option">
    <tr>
    <td width="10">&#160;</td>
    <td valign="top">
      <span class="option-flag"><xsl:value-of select="@flag"/></span>
    </td>

    <xsl:choose>
     <xsl:when test="@param">
     <td valign="top">
       <span class="option-param"><xsl:value-of select="@param"/></span>
     </td>
     </xsl:when>
     <xsl:otherwise><td valign="top">&#160;</td></xsl:otherwise>
    </xsl:choose>
    
    <td valign="top">
      <span class="option"><xsl:apply-templates/></span>
    </td>
    
    </tr>            
  </xsl:template>

  <xsl:template match="code/comment">
    <span class="code-comment"><xsl:apply-templates/></span>
  </xsl:template>
  
  <xsl:template match="code-old">
    <span class="bodyCode">
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
  
  <xsl:template match="command">
    <span class="command"><xsl:apply-templates/></span>
  </xsl:template>

  <xsl:template match="commands">
    <table border="0" cellpadding="0" cellspacing="0" width="440">
    <tr>
    <td bgcolor="#e0e0e0">
    <span class="code-block">
      <pre><span class="command"><xsl:apply-templates/></span></pre>
    </span>
    </td>
    </tr>
    </table>
  </xsl:template>
  
  <xsl:template match="mailing-lists">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="mailing-list">
    <div class="bodyGrey">
      [
      <a href="mailto:{@manager}@{@server}?subject=subscribe {@name}">Subscribe</a> |
      <a href="mailto:{@manager}@{@server}?subject=unsubscribe {@name}">Unsubscribe</a> |
      <a href="mailto:{@name}@{@server}">Post Message</a> |
      <a href="{@archive}">Archive</a>
      ]
    </div>
  </xsl:template>

  <xsl:template match="mailing-list/title">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="mailing-list/description">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="contributors">
    <xsl:for-each select="type">
      <xsl:variable name="type" select="@name"/>
      <xsl:variable name="color-epsilon" select="'#DDDDDD'"/>
      

      <table cellpadding="4" cellspacing="2" width="90%">
        <tr>
          <td bgcolor="{$color-epsilon}">
            <span class="bodyBlack">
            <b><xsl:value-of select="@name"/></b>
            </span>
          </td>
        </tr>
        <tr>
          <td>
          <p><span class="bodyGrey"><xsl:value-of select="."/></span></p>
          </td>
        </tr>
        <xsl:for-each select="../contributor[@type=$type]">
          <tr>
            <td>
            <center>
            <img src="images/line_light.gif"  width="340" height="3" border="0"/>
            </center>
            </td>
          </tr>
          <tr>
            <td>
              <p>
                <span class="teamMember">
                <a href="mailto:{email}"><xsl:value-of select="name/@given"/>&#xA0;<xsl:value-of select="name/@surname"/></a>
                </span>
                
                <xsl:if test="role">
                  <span class="teamMemberRole"> - <xsl:value-of select="role"/></span>
                </xsl:if> 
                
                <xsl:if test="company/@id">
                  <br/>
                  <span class="bodyGrey">
                  <xsl:variable name="company-id" select="company/@id"/>
                  <xsl:variable name="company" select="../company[@id=$company-id]"/>
                  <xsl:choose>
                    <xsl:when test="$company/url">
                      <a href="http://{$company/url}"><xsl:value-of select="$company/name"/></a>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="$company/name"/>
                    </xsl:otherwise>
                  </xsl:choose>
                  </span>
                </xsl:if> 
              </p>
              
              <p>
                <span class="bodyBlack">
                  <xsl:value-of select="description"/>      
                </span>
              </p>
            </td>
          </tr>
        </xsl:for-each>
      </table>
      <br/><br/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="contributors0ld">
    <xsl:for-each select="type">
      <xsl:variable name="type" select="@name"/>
      <xsl:variable name="color-epsilon" select="'#ffffff'"/>
      <p><span class="bodyGrey"><b><xsl:value-of select="@name"/></b></span></p>
      <p><span class="bodyGrey"><xsl:value-of select="."/></span></p>
      <table cellpadding="4" cellspacing="2" width="90%">
        <tr>
          <td bgcolor="{$color-epsilon}"><span class="bodyGrey">
            <b>Name</b></span>
          </td>
          <td bgcolor="{$color-epsilon}"><span class="bodyGrey">
            <b>Contribution</b></span>
          </td>
          <td bgcolor="{$color-epsilon}"><span class="bodyGrey">
            <b>Company</b></span>
          </td>
        </tr>
        <xsl:for-each select="../contributor[@type=$type]">
          <tr>
            <td><span class="bodyGrey">
              <a href="mailto:{email}"><xsl:value-of select="name/@given"/>&#xA0;<xsl:value-of select="name/@surname"/></a></span>
            </td>
            <td><span class="bodyGrey">
               <xsl:value-of select="description"/></span>
            </td>
            <td><span class="bodyGrey">
               <xsl:variable name="company-id" select="company/@id"/>
               <xsl:variable name="company" select="../company[@id=$company-id]"/>
               <a href="http://{$company/url}"><xsl:value-of select="$company/name"/></a>
               &#xA0;</span>
            </td>
          </tr>
        </xsl:for-each>
      </table>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>

