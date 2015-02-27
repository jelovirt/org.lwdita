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

  <xsl:template match="link" mode="ast">
    <xsl:text>[</xsl:text>
    <xsl:apply-templates mode="ast"/>
    <xsl:text>]</xsl:text>
    <xsl:text>(</xsl:text>
    <xsl:value-of select="@href"/>
    <xsl:text>)</xsl:text>
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
  
</xsl:stylesheet>