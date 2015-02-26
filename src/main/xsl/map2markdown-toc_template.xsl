<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:import href="map2markdown-tocImpl.xsl"/>
  <xsl:import href="map2markdownImpl.xsl"/>

  <dita:extension id="dita.xsl.htmltoc" behavior="org.dita.dost.platform.ImportXSLAction" xmlns:dita="http://dita-ot.sourceforge.net"/>

  <xsl:output method="xml"
              encoding="UTF-8"
              indent="no"/>

</xsl:stylesheet>
