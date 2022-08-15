<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:ditaarch="http://dita.oasis-open.org/architecture/2005/"
                xmlns:x="https://github.com/jelovirt/dita-ot-markdown"
                exclude-result-prefixes="xs x"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                version="2.0">

  <xsl:import href="classpath:///hdita2dita-common.xsl"/>

  <xsl:output indent="yes"/>

  <xsl:template match="/">
    <xsl:apply-templates select="html/body"/>
  </xsl:template>

</xsl:stylesheet>
