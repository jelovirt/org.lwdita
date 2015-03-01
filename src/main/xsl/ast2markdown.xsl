<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="xs"
                version="2.0">

  <xsl:variable name="linefeed" as="xs:string" select="'&#xA;'"/>

  <xsl:template match="div" mode="ast">
    <xsl:apply-templates mode="ast"/>
  </xsl:template>

  <xsl:template match="para" mode="ast">
    <xsl:param name="indent" tunnel="yes" as="xs:string" select="''"/>
    <xsl:value-of select="$indent"/>
    <xsl:apply-templates mode="ast"/>
    <xsl:value-of select="$linefeed"/>
    <xsl:value-of select="$linefeed"/>
  </xsl:template>
  
  <xsl:template match="header" mode="ast">
    <xsl:for-each select="1 to xs:integer(@level)">#</xsl:for-each>
    <xsl:text> </xsl:text>
    <xsl:apply-templates mode="ast"/>
    <xsl:call-template name="ast-attibutes"/>
    <xsl:value-of select="$linefeed"/>
    <xsl:value-of select="$linefeed"/>
  </xsl:template>
  
  <xsl:template name="ast-attibutes">
    <xsl:if test="@id or @class">
      <xsl:text> {</xsl:text>
      <xsl:if test="@id">
        <xsl:text>#</xsl:text>
        <xsl:value-of select="@id"/>
      </xsl:if>
      <xsl:for-each select="tokenize(@class, '\s+')">
        <xsl:text> .</xsl:text>
        <xsl:value-of select="."/>
      </xsl:for-each>
      <xsl:text>}</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="bulletlist | orderedlist" mode="ast">
    <xsl:param name="indent" tunnel="yes" as="xs:string" select="''"/>
    <xsl:variable name="nested" select="ancestor::bulletlist or ancestor::orderedlist"/>
    <xsl:if test="$nested">
      <xsl:value-of select="$linefeed"/>
    </xsl:if>
    <xsl:variable name="lis" select="li"/>
    <xsl:apply-templates select="$lis" mode="ast"/>
    <xsl:if test="not($nested)">
      <xsl:value-of select="$linefeed"/><!-- because last li will not write one -->
      <xsl:value-of select="$linefeed"/>
    </xsl:if>  
  </xsl:template>

  <xsl:template match="li" mode="ast">
    <xsl:param name="indent" tunnel="yes" as="xs:string" select="''"/>
    <xsl:value-of select="$indent"/>
    <xsl:choose>
      <xsl:when test="parent::bulletlist">
        <xsl:text>-   </xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="label" select="concat(position(), '.')" as="xs:string"/>
        <xsl:value-of select="$label"/>
        <xsl:value-of select="substring('    ', string-length($label) + 1)"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates mode="ast">
      <xsl:with-param name="indent" tunnel="yes" select="concat($indent, '    ')"/>
    </xsl:apply-templates>
    <xsl:if test="following-sibling::li">
      <xsl:value-of select="$linefeed"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="codeblock" mode="ast">
    <xsl:text>```</xsl:text>
    <xsl:choose>
      <xsl:when test="empty(@id) and @class and not(contains(@class, ' '))">
        <xsl:value-of select="@class"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="ast-attibutes"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:value-of select="$linefeed"/>
    <xsl:apply-templates mode="ast"/>
    <xsl:value-of select="$linefeed"/>
    <xsl:text>```</xsl:text>
    <xsl:value-of select="$linefeed"/>
    <xsl:value-of select="$linefeed"/>
  </xsl:template>
  
  <xsl:template match="table" mode="ast">
    <xsl:for-each select="thead">
      <xsl:for-each select="tr">
        <xsl:text>|</xsl:text>
        <xsl:for-each select="tablecell">
          <xsl:apply-templates mode="ast"/>
          <xsl:text>|</xsl:text>
        </xsl:for-each>
        <xsl:value-of select="$linefeed"/>
      </xsl:for-each>
      <xsl:for-each select="tr">
        <xsl:text>|</xsl:text>
        <xsl:for-each select="tablecell">
          <xsl:variable name="content" as="xs:string*">
            <xsl:apply-templates mode="ast"/>
          </xsl:variable>
          <xsl:value-of select="if (@align = ('left', 'center')) then ':' else '-'"/>
          <xsl:for-each select="3 to string-length($content)">-</xsl:for-each>
          <xsl:value-of select="if (@align = ('right', 'center')) then ':' else '-'"/>
          <xsl:text>|</xsl:text>
        </xsl:for-each>
        <xsl:value-of select="$linefeed"/>
      </xsl:for-each>
    </xsl:for-each>
    <xsl:for-each select="tbody">
      <xsl:for-each select="tr">
        <xsl:text>|</xsl:text>
        <xsl:for-each select="tablecell">
          <xsl:apply-templates mode="ast"/>
          <xsl:text>|</xsl:text>
        </xsl:for-each>
        <xsl:value-of select="$linefeed"/>
      </xsl:for-each>
    </xsl:for-each>
    <xsl:value-of select="$linefeed"/>
  </xsl:template>
  
  <xsl:template match="strong" mode="ast">
    <xsl:text>**</xsl:text>
    <xsl:apply-templates mode="ast"/>
    <xsl:text>**</xsl:text>
  </xsl:template>

  <xsl:template match="emph" mode="ast">
    <xsl:text>*</xsl:text>
    <xsl:apply-templates mode="ast"/>
    <xsl:text>*</xsl:text>
  </xsl:template>

  <xsl:template match="code" mode="ast">
    <xsl:text>`</xsl:text>
    <xsl:apply-templates mode="ast"/>
    <xsl:text>`</xsl:text>
  </xsl:template>

  <xsl:template match="link[@href]" mode="ast">
    <xsl:text>[</xsl:text>
    <xsl:apply-templates mode="ast"/>
    <xsl:text>]</xsl:text>
    <xsl:text>(</xsl:text>
    <xsl:value-of select="@href"/>
    <xsl:text>)</xsl:text>
  </xsl:template>
  
  <xsl:template match="link[empty(@href) and @keyref]" mode="ast">
    <xsl:text>[</xsl:text>
    <xsl:value-of select="@keyref"/>
    <xsl:text>]</xsl:text>
  </xsl:template>
  
  <xsl:template match="image" mode="ast">
    <xsl:text>![</xsl:text>
    <xsl:value-of select="@alt"/>
    <xsl:apply-templates mode="ast"/>
    <xsl:text>]</xsl:text>
    <xsl:text>(</xsl:text>
    <xsl:value-of select="@href"/>
    <xsl:if test="@title">
      <xsl:text> "</xsl:text>
      <xsl:value-of select="@title"/>
      <xsl:text>"</xsl:text>
    </xsl:if>
    <xsl:text>)</xsl:text>
    <xsl:if test="@placement = 'break'">
      <xsl:value-of select="$linefeed"/>
      <xsl:value-of select="$linefeed"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="span" mode="ast">
    <xsl:apply-templates mode="ast"/>
  </xsl:template>
  
  <xsl:template match="text()" mode="ast">
    <xsl:value-of select="."/>
  </xsl:template>
  
  <xsl:template match="node()" mode="ast" priority="-10">
    <xsl:message>ERROR: Unsupported AST node <xsl:value-of select="name()"/></xsl:message>
    <xsl:apply-templates mode="ast"/>
  </xsl:template>
  
  <!-- Whitespace cleanup -->
  
  <xsl:template match="text()"
                mode="ast-clean">
    <xsl:if test="preceding-sibling::node() and starts-with(., ' ')">
      <xsl:text> </xsl:text>
    </xsl:if>
    <xsl:value-of select="normalize-space(.)"/>
    <xsl:if test="following-sibling::node() and ends-with(., ' ')">
      <xsl:text> </xsl:text>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="pandoc/text() |
                       div/text() |
                       bulletlist/text() |
                       orderedlist/text() |
                       table/text() |
                       thead/text() |
                       tbody/text() |
                       tr/text()"
                mode="ast-clean" priority="10">
    <!--xsl:value-of select="normalize-space(.)"/-->
  </xsl:template>
  
  <xsl:template match="codeblock//text()"
                mode="ast-clean" priority="20">
    <xsl:value-of select="."/>
  </xsl:template>
  
  <xsl:template match="@* | node()"
                mode="ast-clean" priority="-10">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" mode="ast-clean"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>