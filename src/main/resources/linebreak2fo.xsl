<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <xsl:template match="processing-instruction('linebreak')">
    <fo:block line-height="0pt"/>
  </xsl:template>

</xsl:stylesheet>
