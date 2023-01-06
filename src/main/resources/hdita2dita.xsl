<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:ditaarch="http://dita.oasis-open.org/architecture/2005/"
                xmlns:x="https://github.com/jelovirt/dita-ot-markdown"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="xs x">

  <!-- FIXME: rename to hdita2xdita.xsl -->

  <xsl:import href="classpath:///hdita2dita-common.xsl"/>

  <xsl:template match="/">
    <xsl:apply-templates select="html"/>
  </xsl:template>

  <xsl:template match="html">
    <xsl:choose>
      <xsl:when test="count(body/article) gt 1">
        <dita>
          <xsl:apply-templates select="." mode="topic"/>
          <xsl:apply-templates select="@* | node()"/>
        </dita>        
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="body"/>        
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*" mode="topic">
    <xsl:attribute name="ditaarch:DITAArchVersion">2.0</xsl:attribute>
    <xsl:attribute name="specializations">(topic hi-d)(topic em-d)</xsl:attribute>
  </xsl:template>

  <xsl:template match="blockquote">
<!--    <p>-->
<!--      <xsl:apply-templates select="." mode="class"/>-->
<!--      <xsl:apply-templates select="@* | node()"/>-->
<!--    </p>-->
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="blockquote" mode="class">
    <xsl:attribute name="class">- topic/p </xsl:attribute>
  </xsl:template>

  <!-- Table -->

  <xsl:template match="table">
    <xsl:variable name="cols" as="xs:integer" select="max((descendant::tr/count(*), count(colgroup/col)))"/>
    <simpletable>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="caption"/>
<!--      <tgroup class="- topic/tgroup " cols="{$cols}">-->
<!--        <xsl:for-each select="1 to $cols">-->
<!--          <colspec class="- topic/colspec " colname="col{.}"/>-->
<!--        </xsl:for-each>-->
        <xsl:choose>
          <xsl:when test="tr[1][th and empty(td)]">
            <sthead class="- topic/sthead ">
              <xsl:apply-templates select="tr[1]"/>
            </sthead>
<!--            <tbody class="- topic/tbody ">-->
              <xsl:apply-templates select="tr[position() ne 1]"/>
<!--            </tbody>-->
          </xsl:when>
          <xsl:when test="tr">
<!--            <tbody class="- topic/tbody ">-->
              <xsl:apply-templates select="tr"/>
<!--            </tbody>-->
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="thead"/>
            <xsl:apply-templates select="tbody"/>
          </xsl:otherwise>
        </xsl:choose>
<!--      </tgroup>-->
    </simpletable>
  </xsl:template>
  <xsl:template match="table" mode="class">
    <xsl:attribute name="class">- topic/simpletable </xsl:attribute>
  </xsl:template>

  <xsl:template match="table/caption | figcaption">
    <title>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </title>
  </xsl:template>
  <xsl:template match="table/caption | figcaption" mode="class">
    <xsl:attribute name="class">- topic/title </xsl:attribute>
  </xsl:template>

  <xsl:template match="thead | tbody">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="thead/tr" priority="10">
    <sthead>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </sthead>
  </xsl:template>
  <xsl:template match="thead/tr" mode="class" priority="10">
    <xsl:attribute name="class">- topic/sthead </xsl:attribute>
  </xsl:template>

  <xsl:template match="tr">
    <strow>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </strow>
  </xsl:template>
  <xsl:template match="tr" mode="class">
    <xsl:attribute name="class">- topic/strow </xsl:attribute>
  </xsl:template>

  <xsl:template match="td | th">
    <stentry>
      <xsl:apply-templates select="." mode="class"/>
<!--      <xsl:call-template name="processColspan"/>-->
      <xsl:apply-templates select="@*"/>
      <xsl:if test="exists(node())">
        <p class="- topic/p ">
          <xsl:apply-templates select="node()"/>
        </p>
      </xsl:if>
    </stentry>
  </xsl:template>
  <xsl:template match="td | th" mode="class">
    <xsl:attribute name="class">- topic/stentry </xsl:attribute>
  </xsl:template>

  <xsl:template match="td/@align | th/@align"/>

  <xsl:template match="@rowspan | @colspan">
    <xsl:copy/>
  </xsl:template>

<!--  <xsl:template name="processColspan">-->
<!--    <xsl:variable name="position" select="count(preceding-sibling::*) + 1"/>-->
<!--    <xsl:if test="(@colspan castable as xs:integer) and (@colspan > 1)">-->
<!--      &lt;!&ndash; Current row and column index &ndash;&gt;-->
<!--      <xsl:variable name="currentRowIndex" select="x:getRowIndex(.)"/>-->
<!--      <xsl:variable name="currentColIndex" select="x:getColIndex(.)"/>-->
<!--      &lt;!&ndash; Set of preceding rows &ndash;&gt;-->
<!--      <xsl:variable name="precedingRows" select="parent::tr/preceding-sibling::tr[position() &lt; $currentRowIndex]"/>-->
<!--      &lt;!&ndash; Preceding cells in column which have row spans over the current row. &ndash;&gt;-->
<!--      <xsl:variable name="previousCellsWithRowSpans" select="-->
<!--       ancestor::table//(th | td)[@rowspan castable as xs:integer][@rowspan][x:getRowIndex(.) &lt; $currentRowIndex][x:getColIndex(.) &lt;= $currentColIndex][number(@rowspan) + number(x:getRowIndex(.)) - number($currentRowIndex) &gt; 0]"/>-->
<!--      &lt;!&ndash; Namestart and name end must be shifted with this shift offset. &ndash;&gt;-->
<!--      <xsl:variable name="shiftColNumber" as="xs:integer" select="count($previousCellsWithRowSpans)"/>-->
<!--      &lt;!&ndash; The current cell might be pushed to the right by previous cells that span over multiple columns.  &ndash;&gt;-->
<!--      <xsl:variable name="previousCellsWithColSpan" select="preceding-sibling::*[(@colspan castable as xs:integer) and (@colspan > 1)]"/>-->
<!--      &lt;!&ndash; Compute how many additional columns are occupied by the cells located to the left of the current cell. &ndash;&gt;-->
<!--      <xsl:variable name="colspanShift" select="sum(($previousCellsWithRowSpans, $previousCellsWithColSpan)/(@colspan - 1))"/>-->

<!--      <xsl:attribute name="namest">-->
<!--        <xsl:value-of select="concat('col', $position + $shiftColNumber + $colspanShift)"/>-->
<!--      </xsl:attribute>-->
<!--      <xsl:attribute name="nameend">-->
<!--        <xsl:value-of select="concat('col', $position + number(@colspan) - 1 + $shiftColNumber + $colspanShift)"/>-->
<!--      </xsl:attribute>-->
<!--    </xsl:if>-->
<!--    <xsl:if test="@rowspan castable as xs:integer and @rowspan > 1">-->
<!--      <xsl:attribute name="morerows">-->
<!--        <xsl:value-of select="number(@rowspan) - 1"/>-->
<!--      </xsl:attribute>-->
<!--    </xsl:if>-->
<!--  </xsl:template>-->

<!--  <xsl:template match="td/@colspan | th/@colspan">-->
<!--    &lt;!&ndash; Ignore this attribute &ndash;&gt;-->
<!--  </xsl:template>-->

</xsl:stylesheet>
