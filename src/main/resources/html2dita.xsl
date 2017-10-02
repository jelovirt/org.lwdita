<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:ditaarch="http://dita.oasis-open.org/architecture/2005/"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="xs">

  <xsl:import href="classpath:///hdita2dita-common.xsl"/>

  <xsl:output indent="yes"></xsl:output>

  <xsl:template match="/">
    <xsl:apply-templates select="html"/>
  </xsl:template>

  <xsl:template match="html">
    <xsl:choose>
      <xsl:when test="count(body/article) gt 1">
        <dita>
          <xsl:attribute name="ditaarch:DITAArchVersion">1.3</xsl:attribute>
          <xsl:apply-templates select="@* | node()"/>
        </dita>        
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="body"/>        
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="article">
    <xsl:variable name="name" select="'topic'"/>
    <xsl:element name="{$name}">
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="." mode="topic"/>
      <xsl:attribute name="ditaarch:DITAArchVersion">1.3</xsl:attribute>
      <xsl:apply-templates select="ancestor::*/@xml:lang"/>
      <xsl:apply-templates select="@*"/>
      <xsl:choose>
        <xsl:when test="@id">
          <xsl:apply-templates select="@id"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="id" select="translate(normalize-space(lower-case(h1)), ' ', '-')"/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="h1"/>
      <body class="- topic/body ">
        <xsl:apply-templates select="* except h1"/>
      </body>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
