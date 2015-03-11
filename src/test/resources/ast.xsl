<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="xs"
                version="2.0">
  
  <xsl:import href="../main/ast2markdown.xsl"/>
  
  <xsl:strip-space elements="*"/>
  <xsl:preserve-space elements="codeblock"/>
  
  <xsl:output method="text"/>
  
  <xsl:template match="/">
    <xsl:variable name="ast-flat" as="node()">
      <xsl:apply-templates select="pandoc" mode="flatten"/>
    </xsl:variable>
    <xsl:variable name="ast-clean" as="node()">
      <xsl:apply-templates select="$ast-flat" mode="ast-clean"/>
    </xsl:variable>
    <xsl:apply-templates select="$ast-clean" mode="ast"/>
  </xsl:template>
  
</xsl:stylesheet>