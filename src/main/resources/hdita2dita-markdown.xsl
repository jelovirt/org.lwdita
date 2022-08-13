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
    <xsl:apply-templates select="html/body"/>
  </xsl:template>

  <xsl:template match="h3 | h4 | h5 | h6">
    <title>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* except @id | node()"/>
    </title>
  </xsl:template>
  <xsl:template match="h3 | h4 | h5 | h6" mode="class">
    <xsl:attribute name="class">- topic/title </xsl:attribute>
  </xsl:template>

  <xsl:template match="pre[code]">
    <codeblock>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:attribute name="xml:space">preserve</xsl:attribute>
      <xsl:apply-templates select="@* | code/node()"/>
    </codeblock>
  </xsl:template>
  <xsl:template match="pre[code]" mode="class">
    <xsl:attribute name="class">+ topic/pre pr-d/codeblock </xsl:attribute>
  </xsl:template>

  <xsl:template match="br">
    <xsl:processing-instruction name="linebreak"/>
  </xsl:template>


  <xsl:template match="del">
    <ph>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:attribute name="status">deleted</xsl:attribute>
      <xsl:apply-templates select="@* | node()"/>
    </ph>
  </xsl:template>
  <xsl:template match="del" mode="class">
    <xsl:attribute name="class">- topic/ph </xsl:attribute>
  </xsl:template>

  <xsl:template match="s">
    <line-through>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </line-through>
  </xsl:template>
  <xsl:template match="s" mode="class">
    <xsl:attribute name="class">+ topic/ph hi-d/line-through </xsl:attribute>
  </xsl:template>

  <xsl:template match="code">
    <codeph>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </codeph>
  </xsl:template>
  <xsl:template match="code" mode="class">
    <xsl:attribute name="class">+ topic/ph pr-d/codeph </xsl:attribute>
  </xsl:template>

  <xsl:template match="var">
    <var>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </var>
  </xsl:template>
  <xsl:template match="var" mode="class">
    <xsl:attribute name="class">+ topic/ph pr-d/var </xsl:attribute>
  </xsl:template>

  <xsl:template match="ul | menu">
    <ul>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </ul>
  </xsl:template>
  <xsl:template match="ul | menu" mode="class">
    <xsl:attribute name="class">- topic/ul </xsl:attribute>
  </xsl:template>

</xsl:stylesheet>
