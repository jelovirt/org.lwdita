<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                xmlns:ditaarch="http://dita.oasis-open.org/architecture/2005/"
                xmlns:x="https://github.com/jelovirt/dita-ot-markdown"
                xmlns:m="http://www.w3.org/1998/Math/MathML"
                xmlns:svg="http://www.w3.org/2000/svg"
                exclude-result-prefixes="xs x map m svg"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                version="2.0">

  <xsl:param name="formats"/>
  <xsl:param name="raw-dita" select="false()"/>

  <xsl:variable name="supported-formats" as="xs:string*"
                select="if (exists($formats))
                        then tokenize($formats, ',')
                        else ('md', 'markdown')"/>

  <!-- Topic -->

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

  <xsl:template match="head"/>

  <xsl:template match="body">
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="article">
    <topic>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="." mode="topic"/>
      <xsl:apply-templates select="ancestor::*/@xml:lang"/>
      <xsl:apply-templates select="@*"/>
      <xsl:variable name="h" select="(h1, h2, h3, h4, h5, h6)[1]" as="element()?"/>
      <xsl:choose>
        <xsl:when test="@id">
          <xsl:apply-templates select="@id"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:sequence select="x:get-id(.)"/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="$h"/>
      <xsl:variable name="contents" select="* except ($h, article)" as="element()*"/>
      <xsl:variable name="shortdesc" select="$contents[1]/self::p" as="element()?"/>
      <xsl:if test="$shortdesc">
        <shortdesc class="- topic/shortdesc ">
          <xsl:apply-templates select="$shortdesc/node()"/>
        </shortdesc>
      </xsl:if>
      <!--xsl:if test="$contents except $shortdesc"-->
        <body class="- topic/body ">
          <xsl:apply-templates select="$contents except $shortdesc"/>
        </body>
      <!--/xsl:if-->
      <xsl:apply-templates select="article"/>
    </topic>
  </xsl:template>
  <xsl:template match="article" mode="class">
    <xsl:attribute name="class">- topic/topic </xsl:attribute>
  </xsl:template>

  <xsl:function name="x:get-id" as="attribute()?">
    <xsl:param name="topic" as="element()"/>
    <xsl:variable name="title" select="($topic/h1 | $topic/h2 | $topic/h3 | $topic/h4 | $topic/h5 | $topic/h6)[1]" as="element()?"/>
    <xsl:if test="$title">
      <xsl:attribute name="id" select="translate(normalize-space(lower-case($title)), ' .', '-')"/>
    </xsl:if>
  </xsl:function>

  <xsl:template match="*" mode="topic">
    <xsl:attribute name="ditaarch:DITAArchVersion">2.0</xsl:attribute>
    <xsl:attribute name="specializations">@props/audience @props/deliveryTarget @props/otherprops @props/platform @props/product</xsl:attribute>
  </xsl:template>
  
  <xsl:template match="section">
    <section>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@*"/>
      <xsl:if test="empty(@id)">
        <xsl:sequence select="x:get-id(.)"/>
      </xsl:if>
      <xsl:apply-templates select="*"/>
    </section>
  </xsl:template>
  <xsl:template match="section" mode="class">
    <xsl:attribute name="class">- topic/section </xsl:attribute>
  </xsl:template>

  <xsl:template match="h1 | h2">
    <title>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* except @id | node()"/>
    </title>
  </xsl:template>
  <xsl:template match="h1 | h2" mode="class">
    <xsl:attribute name="class">- topic/title </xsl:attribute>
  </xsl:template>

  <xsl:template match="dl">
    <xsl:element name="{name()}">
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@*"/>
      <xsl:for-each-group select="*" group-starting-with="dt[empty(preceding-sibling::*[1]/self::dt)]">
        <dlentry class="- topic/dlentry ">
          <xsl:apply-templates select="current-group()"/>
        </dlentry>
      </xsl:for-each-group>
    </xsl:element>
  </xsl:template>
  <xsl:template match="dt">
    <dt>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </dt>
  </xsl:template>
  <xsl:template match="dd">
    <dd>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@*"/>
      <xsl:variable name="first-block" select="(*[x:is-html-block(.)])[1]" as="element()?"/>
      <xsl:choose>
        <xsl:when test="empty($first-block)">
          <p class="- topic/p ">
            <xsl:apply-templates select="node()"/>
          </p>
        </xsl:when>
        <xsl:when test="exists($first-block) and $first-block/preceding-sibling::node()[not(self::text()[not(normalize-space(.))])]">
          <p class="- topic/p ">
            <xsl:apply-templates select="$first-block/preceding-sibling::node()"/>
          </p>
          <xsl:apply-templates select="$first-block | $first-block/following-sibling::node()"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="node()"/>    
        </xsl:otherwise>
      </xsl:choose>
    </dd>
  </xsl:template>
  
  <xsl:function name="x:is-html-block" as="xs:boolean">
    <xsl:param name="node" as="node()?"/>
    <xsl:sequence select="exists(
      $node/self::address |
      $node/self::article |
      $node/self::aside |
      $node/self::blockquote |
      $node/self::canvas |
      $node/self::dd |
      $node/self::div |
      $node/self::dl |
      $node/self::dt |
      $node/self::fieldset |
      $node/self::figcaption |
      $node/self::figure |
      $node/self::figcaption |
      $node/self::footer |
      $node/self::form |
      $node/self::h1 |
      $node/self::h2 |
      $node/self::h3 |
      $node/self::h4 |
      $node/self::h5 |
      $node/self::h6 |
      $node/self::header |
      $node/self::hgroup |
      $node/self::hr |
      $node/self::li |
      $node/self::main |
      $node/self::nav |
      $node/self::noscript |
      $node/self::ol |
      $node/self::output |
      $node/self::p |
      $node/self::pre |
      $node/self::section |
      $node/self::table |
      $node/self::tfoot |
      $node/self::ul |
      $node/self::video
      )"/>
  </xsl:function>

  <xsl:template match="@xml:lang">
    <xsl:attribute name="lang" select="."/>
  </xsl:template>

  <xsl:template match="figure">
    <fig>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="figcaption"/>
      <xsl:apply-templates select="node() except figcaption"/>
    </fig>
  </xsl:template>
  <xsl:template match="figure" mode="class">
    <xsl:attribute name="class">- topic/fig </xsl:attribute>
  </xsl:template>

  <xsl:template match="div[@data-class = 'note']">
    <note>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* except @data-class | node()"/>
    </note>
  </xsl:template>
  <xsl:template match="div[@data-class = 'note']" mode="class">
    <xsl:attribute name="class">- topic/note </xsl:attribute>
  </xsl:template>
  <xsl:template match="div[@data-class = 'note']/@data-type">
    <xsl:attribute name="type" select="."/>
  </xsl:template>

  <xsl:template match="pre">  
    <pre>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:attribute name="xml:space">preserve</xsl:attribute>
      <xsl:apply-templates select="@* | node()"/>
    </pre>
  </xsl:template>
  <xsl:template match="pre" mode="class">
    <xsl:attribute name="class">- topic/pre </xsl:attribute>
  </xsl:template>

  <xsl:template match="img">
    <image>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* except @alt"/>
      <xsl:apply-templates select="." mode="image-placement"/>
      <xsl:if test="@alt">
        <alt class="- topic/alt ">
          <xsl:value-of select="@alt"/>
        </alt>
      </xsl:if>
    </image>
  </xsl:template>
  <xsl:template match="article/img | section/img" mode="image-placement">
    <xsl:attribute name="placement">break</xsl:attribute>
  </xsl:template>
  <xsl:template match="node()" mode="image-placement" priority="-10"/>
  <xsl:template match="img" mode="class">
    <xsl:attribute name="class">- topic/image </xsl:attribute>
  </xsl:template>
  <xsl:template match="img/@src">
    <xsl:attribute name="href" select="."/>
  </xsl:template>

  <xsl:template match="table">
    <xsl:variable name="table" select="." as="element()"/>
    <xsl:variable name="cols" as="xs:integer" select="max((descendant::tr/count(*), count(colgroup/col)))"/>
    <table>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="caption"/>
      <tgroup class="- topic/tgroup " cols="{$cols}">
        <xsl:for-each select="1 to $cols">
          <xsl:variable name="width" as="xs:string?">
            <xsl:variable name="col" select="$table/colgroup/col[current()]" as="element()?"/>
            <xsl:choose>
              <xsl:when test="normalize-space($col/@width)">
                <xsl:value-of select="normalize-space($col/@width)"/>
              </xsl:when>
              <xsl:when test="normalize-space($col/@style)">
                <xsl:variable name="tokens" select="tokenize($col/@style, ';')" as="xs:string*"/>
                <xsl:variable name="ws" select="$tokens[starts-with(normalize-space(.), 'width')]" as="xs:string?"/>
                <xsl:if test="exists($ws)">
                  <xsl:value-of select="substring-after(normalize-space($ws[1]), ':')"/>
                </xsl:if>
              </xsl:when>
            </xsl:choose>
          </xsl:variable>
          <colspec class="- topic/colspec " colname="col{.}">
            <xsl:if test="exists($width)">
              <xsl:attribute name="colwidth" select="$width"/>
            </xsl:if>
          </colspec>
        </xsl:for-each>
        <xsl:choose>
          <xsl:when test="tr[1][th and empty(td)]">
            <thead class="- topic/thead ">
              <xsl:apply-templates select="tr[1]"/>
            </thead>
            <tbody class="- topic/tbody ">
              <xsl:apply-templates select="tr[position() ne 1]"/>
            </tbody>
          </xsl:when>
          <xsl:when test="tr">
            <tbody class="- topic/tbody ">
              <xsl:apply-templates select="tr"/>
            </tbody>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="thead | tbody"/>
          </xsl:otherwise>
        </xsl:choose>
      </tgroup>
    </table>
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
    <xsl:element name="{name()}">
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:element>
  </xsl:template>
  <xsl:template match="tr">
    <row>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </row>
  </xsl:template>
  <xsl:template match="tr" mode="class">
    <xsl:attribute name="class">- topic/row </xsl:attribute>
  </xsl:template>
  <xsl:template match="td | th">
    <entry>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:call-template name="processColspan"/>
      <xsl:apply-templates select="@* | node()"/>
    </entry>
  </xsl:template>
  <xsl:template match="td | th" mode="class">
    <xsl:attribute name="class">- topic/entry </xsl:attribute>
  </xsl:template>
  <xsl:template match="@rowspan">
    <xsl:attribute name="morerows" select="xs:integer(.) - 1"/>
  </xsl:template>

  <xsl:template name="processColspan">
    <xsl:variable name="position" select="count(preceding-sibling::*) + 1"/>
    <xsl:if test="(@colspan castable as xs:integer) and (@colspan > 1)">
      <!-- Current row and column index -->
      <xsl:variable name="currentRowIndex" select="x:getRowIndex(.)"/>
      <xsl:variable name="currentColIndex" select="x:getColIndex(.)"/>
      <!-- Set of preceding rows -->
      <xsl:variable name="precedingRows" select="parent::tr/preceding-sibling::tr[position() &lt; $currentRowIndex]"/>
      <!-- Preceding cells in column which have row spans over the current row. -->
      <xsl:variable name="previousCellsWithRowSpans" select="
       ancestor::table//(th | td)[@rowspan castable as xs:integer][@rowspan][x:getRowIndex(.) &lt; $currentRowIndex][x:getColIndex(.) &lt;= $currentColIndex][number(@rowspan) + number(x:getRowIndex(.)) - number($currentRowIndex) &gt; 0]"/>
      <!-- Namestart and name end must be shifted with this shift offset. -->
      <xsl:variable name="shiftColNumber" as="xs:integer" select="count($previousCellsWithRowSpans)"/>
      <!-- The current cell might be pushed to the right by previous cells that span over multiple columns.  -->
      <xsl:variable name="previousCellsWithColSpan" select="preceding-sibling::*[(@colspan castable as xs:integer) and (@colspan > 1)]"/>
      <!-- Compute how many additional columns are occupied by the cells located to the left of the current cell. -->
      <xsl:variable name="colspanShift" select="sum(($previousCellsWithRowSpans, $previousCellsWithColSpan)/(@colspan - 1))"/>

      <xsl:attribute name="namest">
        <xsl:value-of select="concat('col', $position + $shiftColNumber + $colspanShift)"/>
      </xsl:attribute>
      <xsl:attribute name="nameend">
        <xsl:value-of select="concat('col', $position + number(@colspan) - 1 + $shiftColNumber + $colspanShift)"/>
      </xsl:attribute>
    </xsl:if>
    <xsl:if test="@rowspan castable as xs:integer and @rowspan > 1">
      <xsl:attribute name="morerows">
        <xsl:value-of select="number(@rowspan) - 1"/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>

  <xsl:function name="x:getRowIndex" as="xs:integer">
    <xsl:param name="cell" as="node()"/>
    <xsl:variable name="precedingRows" select="$cell/parent::tr/preceding-sibling::tr"/>
    <xsl:variable name="currentRowIndex" select="count($precedingRows) + 1"/>
    <xsl:value-of select="$currentRowIndex"/>
  </xsl:function>

  <xsl:function name="x:getColIndex" as="xs:integer">
    <xsl:param name="cell" as="node()"/>
    <xsl:sequence select="count($cell/preceding-sibling::td) + count($cell/preceding-sibling::th)"/>
  </xsl:function>

  <xsl:template match="td/@colspan | th/@colspan">
    <!-- Ignore this attribute -->
  </xsl:template>

  <xsl:template match="br">
    <xsl:processing-instruction name="linebreak"/>
  </xsl:template>

  <xsl:template match="span | code | s">
    <ph>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </ph>
  </xsl:template>
  <xsl:template match="span | code | s" mode="class">
    <xsl:attribute name="class">- topic/ph </xsl:attribute>
  </xsl:template>

  <xsl:template match="tt">
    <tt>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </tt>
  </xsl:template>
  <xsl:template match="tt" mode="class">
    <xsl:attribute name="class">+ topic/ph hi-d/tt </xsl:attribute>
  </xsl:template>

  <xsl:template match="b | strong">
    <b>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </b>
  </xsl:template>
  <xsl:template match="b | strong" mode="class">
    <xsl:attribute name="class">+ topic/ph hi-d/b </xsl:attribute>
  </xsl:template>

  <xsl:template match="em | i">
    <i>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </i>
  </xsl:template>
  <xsl:template match="i | em" mode="class">
    <xsl:attribute name="class">+ topic/ph hi-d/i </xsl:attribute>
  </xsl:template>
  <xsl:template match="sup">
    <sup>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </sup>
  </xsl:template>
  <xsl:template match="sup" mode="class">
    <xsl:attribute name="class">+ topic/ph hi-d/sup </xsl:attribute>
  </xsl:template>

  <xsl:template match="sub">
    <sub>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </sub>
  </xsl:template>
  <xsl:template match="sub" mode="class">
    <xsl:attribute name="class">+ topic/ph hi-d/sub </xsl:attribute>
  </xsl:template>

  <xsl:template match="u">
    <u>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </u>
  </xsl:template>
  <xsl:template match="u" mode="class">
    <xsl:attribute name="class">+ topic/ph hi-d/u </xsl:attribute>
  </xsl:template>

  <xsl:template match="a">
    <xsl:variable name="href" select="lower-case(if (contains(@href, '#')) then substring-before(@href, '#') else @href)"/>
    <xref>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:if test="@data-keyref">
        <xsl:attribute name="keyref" select="@data-keyref"/>
      </xsl:if>
      <xsl:variable name="extension" select="x:get-extension($href)"/>
      <xsl:choose>
        <xsl:when test="starts-with(@href, 'mailto')">
          <xsl:attribute name="format">email</xsl:attribute>
          <xsl:attribute name="scope">external</xsl:attribute>
        </xsl:when>
        <xsl:when test="@type">
          <xsl:attribute name="format" select="@type"/>
        </xsl:when>
        <xsl:when test="$extension = $supported-formats">
          <xsl:attribute name="format" select="$extension"/>
        </xsl:when>
        <xsl:when test="$extension = ('dita', 'xml')"/>
        <xsl:when test="@href">
          <xsl:attribute name="format" select="if (exists($extension))
                                               then $extension
                                               else 'html'"/>
        </xsl:when>
      </xsl:choose>
      <xsl:if test="matches(@href, '^https?://', 'i') or starts-with(@href, '/')">
        <xsl:attribute name="scope">external</xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="@* | node()"/>
    </xref>
  </xsl:template>
  <xsl:template match="a" mode="class">
    <xsl:attribute name="class">- topic/xref </xsl:attribute>
  </xsl:template>
  <xsl:template match="a/@target"/>
  <xsl:template match="a/@data-processing-role">
    <xsl:attribute name="processing-role" select="."/>
  </xsl:template>
  <xsl:template match="a/@rel">
    <xsl:attribute name="scope" select="."/>
  </xsl:template>

  <xsl:function name="x:get-extension" as="xs:string?">
    <xsl:param name="href"/>
    <xsl:variable name="path" select="if (contains($href, '://'))
                                      then tokenize(substring-after($href, '://'), '/')[position() gt 1]
                                      else tokenize($href, '/')"/>
    <xsl:variable name="file" select="$path[position() eq last()]"/>
    <xsl:sequence select="if (matches($file, '^.+\.(\w+?)$'))
                          then replace($file, '^.+\.(\w+?)$', '$1')
                          else ()"/>
  </xsl:function>

  <!-- HDITA -->

  <xsl:template match="div">
    <div>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </div>
  </xsl:template>

  <xsl:template match="ol">
    <ol>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </ol>
  </xsl:template>

  <xsl:template match="ul">
    <ul>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </ul>
  </xsl:template>
  <xsl:template match="ul" mode="class">
    <xsl:attribute name="class">- topic/ul </xsl:attribute>
  </xsl:template>

  <xsl:template match="li">
    <li>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </li>
  </xsl:template>

  <xsl:template match="p">
    <p>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </p>
  </xsl:template>

  <xsl:template match="audio">
    <audio>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </audio>
  </xsl:template>
  <xsl:template match="audio" mode="class">
    <xsl:attribute name="class">- topic/audio </xsl:attribute>
  </xsl:template>

  <xsl:template match="video">
    <video>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </video>
  </xsl:template>
  <xsl:template match="video" mode="class">
    <xsl:attribute name="class">- topic/video </xsl:attribute>
  </xsl:template>

  <xsl:template match="fallback" mode="class">
    <xsl:attribute name="class">- topic/fallback </xsl:attribute>
  </xsl:template>

<!--  <xsl:template match="controls" mode="class">-->
<!--    <xsl:attribute name="class">- topic/controls </xsl:attribute>-->
<!--    <xsl:attribute name="name" select="local-name()"/>-->
<!--  </xsl:template>-->
<!--  <xsl:template match="poster" mode="class">-->
<!--    <xsl:attribute name="class">- topic/poster </xsl:attribute>-->
<!--    <xsl:attribute name="name" select="local-name()"/>-->
<!--  </xsl:template>-->

  <xsl:template match="source">
    <source>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="@* | node()"/>
    </source>
  </xsl:template>
  <xsl:template match="source" mode="class">
    <xsl:attribute name="class">- topic/source </xsl:attribute>
    <xsl:attribute name="name" select="local-name()"/>
  </xsl:template>

  <xsl:template match="track" mode="class">
    <xsl:attribute name="class">- topic/track </xsl:attribute>
    <xsl:attribute name="name" select="local-name()"/>
  </xsl:template>

  <!-- Map -->

  <xsl:template match="nav">
    <map>
      <xsl:apply-templates select="." mode="class"/>
      <xsl:apply-templates select="." mode="topic"/>
      <xsl:apply-templates select="ancestor::*/@xml:lang"/>
      <xsl:apply-templates select="@* | node()"/>
    </map>
  </xsl:template>
  <xsl:template match="nav" mode="class">
    <xsl:attribute name="class">- map/map </xsl:attribute>
  </xsl:template>

  <xsl:template match="nav/h1">
    <topicmeta class="- map/topicmeta ">
      <navtitle class="- map/navtitle ">
        <xsl:apply-templates select="@* | node()"/>
      </navtitle>
    </topicmeta>
  </xsl:template>

  <xsl:template match="nav//ul | nav//ol">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="nav//li">
    <topicref class="- map/topicref ">
      <topicmeta class="- map/topicmeta ">
        <navtitle class="- map/navtitle ">
          <xsl:apply-templates select="node() except (ol | ul)"/>
        </navtitle>
      </topicmeta>
      <xsl:apply-templates select="ol | ul"/>
    </topicref>
  </xsl:template>

  <!-- Common -->

  <xsl:template match="@class">
    <xsl:attribute name="outputclass" select="."/>
  </xsl:template>

  <xsl:template match="@data-props">
    <xsl:attribute name="props" select="."/>
  </xsl:template>

  <xsl:variable name="classes" as="map(*)">
    <xsl:map>
      <xsl:map-entry key="'ditavalmeta'" select="'+ map/topicmeta ditavalref-d/ditavalmeta '"/>
      <xsl:map-entry key="'ditavalref'" select="'+ map/topicref ditavalref-d/ditavalref '"/>
      <xsl:map-entry key="'glossref'" select="'+ map/topicref glossref-d/glossref '"/>
      <xsl:map-entry key="'keydef'" select="'+ map/topicref mapgroup-d/keydef '"/>
      <xsl:map-entry key="'mapref'" select="'+ map/topicref mapgroup-d/mapref '"/>
      <xsl:map-entry key="'mapresources'" select="'+ map/topicref mapgroup-d/mapresources '"/>
      <xsl:map-entry key="'topicgroup'" select="'+ map/topicref mapgroup-d/topicgroup '"/>
      <xsl:map-entry key="'topichead'" select="'+ map/topicref mapgroup-d/topichead '"/>
      <xsl:map-entry key="'dvrKeyscopePrefix'" select="'+ topic/data ditavalref-d/dvrKeyscopePrefix '"/>
      <xsl:map-entry key="'dvrKeyscopeSuffix'" select="'+ topic/data ditavalref-d/dvrKeyscopeSuffix '"/>
      <xsl:map-entry key="'dvrResourcePrefix'" select="'+ topic/data ditavalref-d/dvrResourcePrefix '"/>
      <xsl:map-entry key="'dvrResourceSuffix'" select="'+ topic/data ditavalref-d/dvrResourceSuffix '"/>
      <xsl:map-entry key="'change-completed'" select="'+ topic/data relmgmt-d/change-completed '"/>
      <xsl:map-entry key="'change-item'" select="'+ topic/data relmgmt-d/change-item '"/>
      <xsl:map-entry key="'change-organization'" select="'+ topic/data relmgmt-d/change-organization '"/>
      <xsl:map-entry key="'change-person'" select="'+ topic/data relmgmt-d/change-person '"/>
      <xsl:map-entry key="'change-request-id'" select="'+ topic/data relmgmt-d/change-request-id '"/>
      <xsl:map-entry key="'change-request-reference'" select="'+ topic/data relmgmt-d/change-request-reference '"/>
      <xsl:map-entry key="'change-request-system'" select="'+ topic/data relmgmt-d/change-request-system '"/>
      <xsl:map-entry key="'change-revisionid'" select="'+ topic/data relmgmt-d/change-revisionid '"/>
      <xsl:map-entry key="'change-started'" select="'+ topic/data relmgmt-d/change-started '"/>
      <xsl:map-entry key="'change-summary'" select="'+ topic/data relmgmt-d/change-summary '"/>
      <xsl:map-entry key="'sort-as'" select="'+ topic/data ut-d/sort-as '"/>
      <xsl:map-entry key="'pd'" select="'+ topic/dd pr-d/pd '"/>
      <xsl:map-entry key="'equation-block'" select="'+ topic/div equation-d/equation-block '"/>
      <xsl:map-entry key="'consequence'" select="'+ topic/li hazard-d/consequence '"/>
      <xsl:map-entry key="'howtoavoid'" select="'+ topic/li hazard-d/howtoavoid '"/>
      <xsl:map-entry key="'messagepanel'" select="'+ topic/ul hazard-d/messagepanel '"/>
      <xsl:map-entry key="'typeofhazard'" select="'+ topic/li hazard-d/typeofhazard '"/>
      <xsl:map-entry key="'area'" select="'+ topic/figgroup ut-d/area '"/>
      <xsl:map-entry key="'imagemap'" select="'+ topic/fig ut-d/imagemap '"/>
      <xsl:map-entry key="'parml'" select="'+ topic/dl pr-d/parml '"/>
      <xsl:map-entry key="'plentry'" select="'+ topic/dlentry pr-d/plentry '"/>
      <xsl:map-entry key="'pt'" select="'+ topic/dt pr-d/pt '"/>
      <xsl:map-entry key="'equation-figure'" select="'+ topic/fig equation-d/equation-figure '"/>
      <xsl:map-entry key="'syntaxdiagram'" select="'+ topic/fig pr-d/syntaxdiagram '"/>
      <xsl:map-entry key="'fragment'" select="'+ topic/figgroup pr-d/fragment '"/>
      <xsl:map-entry key="'groupchoice'" select="'+ topic/figgroup pr-d/groupchoice '"/>
      <xsl:map-entry key="'groupcomp'" select="'+ topic/figgroup pr-d/groupcomp '"/>
      <xsl:map-entry key="'groupseq'" select="'+ topic/figgroup pr-d/groupseq '"/>
      <xsl:map-entry key="'synblk'" select="'+ topic/figgroup pr-d/synblk '"/>
      <xsl:map-entry key="'synnote'" select="'+ topic/fn pr-d/synnote '"/>
      <xsl:map-entry key="'mathml'" select="'+ topic/foreign mathml-d/mathml '"/>
      <xsl:map-entry key="'svg-container'" select="'+ topic/foreign svg-d/svg-container '"/>
      <xsl:map-entry key="'hazardsymbol'" select="'+ topic/image hazard-d/hazardsymbol '"/>
      <xsl:map-entry key="'mathmlref'" select="'+ topic/xref mathml-d/mathmlref '"/>
      <xsl:map-entry key="'coderef'" select="'+ topic/xref pr-d/coderef '"/>
      <xsl:map-entry key="'svgref'" select="'+ topic/xref svg-d/svgref '"/>
      <xsl:map-entry key="'markupname'" select="'+ topic/keyword markup-d/markupname '"/>
      <xsl:map-entry key="'numcharref'" select="'+ topic/keyword markup-d/markupname xml-d/numcharref '"/>
      <xsl:map-entry key="'parameterentity'" select="'+ topic/keyword markup-d/markupname xml-d/parameterentity '"/>
      <xsl:map-entry key="'textentity'" select="'+ topic/keyword markup-d/markupname xml-d/textentity '"/>
      <xsl:map-entry key="'xmlatt'" select="'+ topic/keyword markup-d/markupname xml-d/xmlatt '"/>
      <xsl:map-entry key="'xmlelement'" select="'+ topic/keyword markup-d/markupname xml-d/xmlelement '"/>
      <xsl:map-entry key="'xmlnsname'" select="'+ topic/keyword markup-d/markupname xml-d/xmlnsname '"/>
      <xsl:map-entry key="'xmlpi'" select="'+ topic/keyword markup-d/markupname xml-d/xmlpi '"/>
      <xsl:map-entry key="'apiname'" select="'+ topic/keyword pr-d/apiname '"/>
      <xsl:map-entry key="'kwd'" select="'+ topic/keyword pr-d/kwd '"/>
      <xsl:map-entry key="'option'" select="'+ topic/keyword pr-d/option '"/>
      <xsl:map-entry key="'parmname'" select="'+ topic/keyword pr-d/parmname '"/>
      <xsl:map-entry key="'cmdname'" select="'+ topic/keyword sw-d/cmdname '"/>
      <xsl:map-entry key="'msgnum'" select="'+ topic/keyword sw-d/msgnum '"/>
      <xsl:map-entry key="'varname'" select="'+ topic/keyword sw-d/varname '"/>
      <xsl:map-entry key="'shortcut'" select="'+ topic/keyword ui-d/shortcut '"/>
      <xsl:map-entry key="'wintitle'" select="'+ topic/keyword ui-d/wintitle '"/>
      <xsl:map-entry key="'shape'" select="'+ topic/keyword ut-d/shape '"/>
      <xsl:map-entry key="'change-historylist'" select="'+ topic/metadata relmgmt-d/change-historylist '"/>
      <xsl:map-entry key="'hazardstatement'" select="'+ topic/note hazard-d/hazardstatement '"/>
      <xsl:map-entry key="'em'" select="'+ topic/ph emphasis-d/em '"/>
      <xsl:map-entry key="'strong'" select="'+ topic/ph emphasis-d/strong '"/>
      <xsl:map-entry key="'equation-inline'" select="'+ topic/ph equation-d/equation-inline '"/>
      <xsl:map-entry key="'equation-number'" select="'+ topic/ph equation-d/equation-number '"/>
      <xsl:map-entry key="'b'" select="'+ topic/ph hi-d/b '"/>
      <xsl:map-entry key="'i'" select="'+ topic/ph hi-d/i '"/>
      <xsl:map-entry key="'line-through'" select="'+ topic/ph hi-d/line-through '"/>
      <xsl:map-entry key="'overline'" select="'+ topic/ph hi-d/overline '"/>
      <xsl:map-entry key="'sub'" select="'+ topic/ph hi-d/sub '"/>
      <xsl:map-entry key="'sup'" select="'+ topic/ph hi-d/sup '"/>
      <xsl:map-entry key="'tt'" select="'+ topic/ph hi-d/tt '"/>
      <xsl:map-entry key="'u'" select="'+ topic/ph hi-d/u '"/>
      <xsl:map-entry key="'codeph'" select="'+ topic/ph pr-d/codeph '"/>
      <xsl:map-entry key="'delim'" select="'+ topic/ph pr-d/delim '"/>
      <xsl:map-entry key="'oper'" select="'+ topic/ph pr-d/oper '"/>
      <xsl:map-entry key="'repsep'" select="'+ topic/ph pr-d/repsep '"/>
      <xsl:map-entry key="'sep'" select="'+ topic/ph pr-d/sep '"/>
      <xsl:map-entry key="'synph'" select="'+ topic/ph pr-d/synph '"/>
      <xsl:map-entry key="'var'" select="'+ topic/ph pr-d/var '"/>
      <xsl:map-entry key="'filepath'" select="'+ topic/ph sw-d/filepath '"/>
      <xsl:map-entry key="'msgph'" select="'+ topic/ph sw-d/msgph '"/>
      <xsl:map-entry key="'systemoutput'" select="'+ topic/ph sw-d/systemoutput '"/>
      <xsl:map-entry key="'userinput'" select="'+ topic/ph sw-d/userinput '"/>
      <xsl:map-entry key="'menucascade'" select="'+ topic/ph ui-d/menucascade '"/>
      <xsl:map-entry key="'uicontrol'" select="'+ topic/ph ui-d/uicontrol '"/>
      <xsl:map-entry key="'coords'" select="'+ topic/ph ut-d/coords '"/>
      <xsl:map-entry key="'codeblock'" select="'+ topic/pre pr-d/codeblock '"/>
      <xsl:map-entry key="'msgblock'" select="'+ topic/pre sw-d/msgblock '"/>
      <xsl:map-entry key="'screen'" select="'+ topic/pre ui-d/screen '"/>
      <xsl:map-entry key="'abbreviated-form'" select="'+ topic/term abbrev-d/abbreviated-form '"/>
      <xsl:map-entry key="'linktitle'" select="'+ topic/titlealt alternativeTitles-d/linktitle '"/>
      <xsl:map-entry key="'navtitle'" select="'- topic/navtitle '"/>
      <xsl:map-entry key="'searchtitle'" select="'- map/searchtitle '"/>
      <xsl:map-entry key="'subtitle'" select="'+ topic/titlealt alternativeTitles-d/subtitle '"/>
      <xsl:map-entry key="'titlehint'" select="'+ topic/titlealt alternativeTitles-d/titlehint '"/>
      <xsl:map-entry key="'fragref'" select="'+ topic/xref pr-d/fragref '"/>
      <xsl:map-entry key="'synnoteref'" select="'+ topic/xref pr-d/synnoteref '"/>
      <xsl:map-entry key="'keytext'" select="'- map/keytext '"/>
      <!--xsl:map-entry key="'map'" select="'- map/map '"/-->
      <xsl:map-entry key="'bookmap'" select="'- map/map bookmap/bookmap '"/>
      <xsl:map-entry key="'subjectScheme'" select="'- map/map subjectScheme/subjectScheme '"/>
      <xsl:map-entry key="'navref'" select="'- map/navref '"/>
      <xsl:map-entry key="'relcell'" select="'- map/relcell '"/>
      <xsl:map-entry key="'relcolspec'" select="'- map/relcolspec '"/>
      <xsl:map-entry key="'relheader'" select="'- map/relheader '"/>
      <xsl:map-entry key="'relrow'" select="'- map/relrow '"/>
      <xsl:map-entry key="'reltable'" select="'- map/reltable '"/>
      <xsl:map-entry key="'topicmeta'" select="'- map/topicmeta '"/>
      <xsl:map-entry key="'bookmeta'" select="'- map/topicmeta bookmap/bookmeta '"/>
      <xsl:map-entry key="'subjectHeadMeta'" select="'- map/topicmeta subjectScheme/subjectHeadMeta '"/>
      <xsl:map-entry key="'topicref'" select="'- map/topicref '"/>
      <xsl:map-entry key="'abbrevlist'" select="'- map/topicref bookmap/abbrevlist '"/>
      <xsl:map-entry key="'amendments'" select="'- map/topicref bookmap/amendments '"/>
      <xsl:map-entry key="'appendix'" select="'- map/topicref bookmap/appendix '"/>
      <xsl:map-entry key="'backmatter'" select="'- map/topicref bookmap/backmatter '"/>
      <xsl:map-entry key="'bibliolist'" select="'- map/topicref bookmap/bibliolist '"/>
      <xsl:map-entry key="'bookabstract'" select="'- map/topicref bookmap/bookabstract '"/>
      <xsl:map-entry key="'booklist'" select="'- map/topicref bookmap/booklist '"/>
      <xsl:map-entry key="'booklists'" select="'- map/topicref bookmap/booklists '"/>
      <xsl:map-entry key="'chapter'" select="'- map/topicref bookmap/chapter '"/>
      <xsl:map-entry key="'colophon'" select="'- map/topicref bookmap/colophon '"/>
      <xsl:map-entry key="'dedication'" select="'- map/topicref bookmap/dedication '"/>
      <xsl:map-entry key="'draftintro'" select="'- map/topicref bookmap/draftintro '"/>
      <xsl:map-entry key="'figurelist'" select="'- map/topicref bookmap/figurelist '"/>
      <xsl:map-entry key="'frontmatter'" select="'- map/topicref bookmap/frontmatter '"/>
      <xsl:map-entry key="'glossarylist'" select="'- map/topicref bookmap/glossarylist '"/>
      <xsl:map-entry key="'indexlist'" select="'- map/topicref bookmap/indexlist '"/>
      <xsl:map-entry key="'notices'" select="'- map/topicref bookmap/notices '"/>
      <xsl:map-entry key="'part'" select="'- map/topicref bookmap/part '"/>
      <xsl:map-entry key="'preface'" select="'- map/topicref bookmap/preface '"/>
      <xsl:map-entry key="'tablelist'" select="'- map/topicref bookmap/tablelist '"/>
      <xsl:map-entry key="'toc'" select="'- map/topicref bookmap/toc '"/>
      <xsl:map-entry key="'trademarklist'" select="'- map/topicref bookmap/trademarklist '"/>
      <xsl:map-entry key="'defaultSubject'" select="'- map/topicref subjectScheme/defaultSubject '"/>
      <xsl:map-entry key="'enumerationdef'" select="'- map/topicref subjectScheme/enumerationdef '"/>
      <xsl:map-entry key="'schemeref'" select="'- map/topicref subjectScheme/schemeref '"/>
      <xsl:map-entry key="'subjectHead'" select="'- map/topicref subjectScheme/subjectHead '"/>
      <xsl:map-entry key="'subjectdef'" select="'- map/topicref subjectScheme/subjectdef '"/>
      <xsl:map-entry key="'ux-window'" select="'- map/ux-window '"/>
      <xsl:map-entry key="'abstract'" select="'- topic/abstract '"/>
      <xsl:map-entry key="'glossdef'" select="'- topic/abstract concept/abstract glossentry/glossdef '"/>
      <xsl:map-entry key="'alt'" select="'- topic/alt '"/>
      <xsl:map-entry key="'audience'" select="'- topic/audience '"/>
      <xsl:map-entry key="'audio'" select="'- topic/audio '"/>
      <xsl:map-entry key="'author'" select="'- topic/author '"/>
      <xsl:map-entry key="'body'" select="'- topic/body '"/>
      <xsl:map-entry key="'conbody'" select="'- topic/body concept/conbody '"/>
      <xsl:map-entry key="'refbody'" select="'- topic/body reference/refbody '"/>
      <xsl:map-entry key="'taskbody'" select="'- topic/body task/taskbody '"/>
      <xsl:map-entry key="'troublebody'" select="'- topic/body troubleshooting/troublebody '"/>
      <xsl:map-entry key="'bodydiv'" select="'- topic/bodydiv '"/>
      <xsl:map-entry key="'conbodydiv'" select="'- topic/bodydiv concept/conbodydiv '"/>
      <xsl:map-entry key="'refbodydiv'" select="'- topic/bodydiv reference/refbodydiv '"/>
      <xsl:map-entry key="'troubleSolution'" select="'- topic/bodydiv troubleshooting/troubleSolution '"/>
      <xsl:map-entry key="'brand'" select="'- topic/brand '"/>
      <xsl:map-entry key="'category'" select="'- topic/category '"/>
      <xsl:map-entry key="'cite'" select="'- topic/cite '"/>
      <xsl:map-entry key="'colspec'" select="'- topic/colspec '"/>
      <xsl:map-entry key="'component'" select="'- topic/component '"/>
      <xsl:map-entry key="'copyrholder'" select="'- topic/copyrholder '"/>
      <xsl:map-entry key="'copyright'" select="'- topic/copyright '"/>
      <xsl:map-entry key="'copyryear'" select="'- topic/copyryear '"/>
      <xsl:map-entry key="'created'" select="'- topic/created '"/>
      <xsl:map-entry key="'critdates'" select="'- topic/critdates '"/>
      <xsl:map-entry key="'data'" select="'- topic/data '"/>
      <xsl:map-entry key="'approved'" select="'- topic/data bookmap/approved '"/>
      <xsl:map-entry key="'bookchangehistory'" select="'- topic/data bookmap/bookchangehistory '"/>
      <xsl:map-entry key="'bookevent'" select="'- topic/data bookmap/bookevent '"/>
      <xsl:map-entry key="'bookeventtype'" select="'- topic/data bookmap/bookeventtype '"/>
      <xsl:map-entry key="'bookid'" select="'- topic/data bookmap/bookid '"/>
      <xsl:map-entry key="'booknumber'" select="'- topic/data bookmap/booknumber '"/>
      <xsl:map-entry key="'bookowner'" select="'- topic/data bookmap/bookowner '"/>
      <xsl:map-entry key="'bookpartno'" select="'- topic/data bookmap/bookpartno '"/>
      <xsl:map-entry key="'bookrestriction'" select="'- topic/data bookmap/bookrestriction '"/>
      <xsl:map-entry key="'bookrights'" select="'- topic/data bookmap/bookrights '"/>
      <xsl:map-entry key="'copyrfirst'" select="'- topic/data bookmap/copyrfirst '"/>
      <xsl:map-entry key="'copyrlast'" select="'- topic/data bookmap/copyrlast '"/>
      <xsl:map-entry key="'edited'" select="'- topic/data bookmap/edited '"/>
      <xsl:map-entry key="'edition'" select="'- topic/data bookmap/edition '"/>
      <xsl:map-entry key="'isbn'" select="'- topic/data bookmap/isbn '"/>
      <xsl:map-entry key="'maintainer'" select="'- topic/data bookmap/maintainer '"/>
      <xsl:map-entry key="'organization'" select="'- topic/data bookmap/organization '"/>
      <xsl:map-entry key="'person'" select="'- topic/data bookmap/person '"/>
      <xsl:map-entry key="'printlocation'" select="'- topic/data bookmap/printlocation '"/>
      <xsl:map-entry key="'published'" select="'- topic/data bookmap/published '"/>
      <xsl:map-entry key="'publishtype'" select="'- topic/data bookmap/publishtype '"/>
      <xsl:map-entry key="'reviewed'" select="'- topic/data bookmap/reviewed '"/>
      <xsl:map-entry key="'tested'" select="'- topic/data bookmap/tested '"/>
      <xsl:map-entry key="'volume'" select="'- topic/data bookmap/volume '"/>
      <xsl:map-entry key="'glossPartOfSpeech'" select="'- topic/data concept/data glossentry/glossPartOfSpeech '"/>
      <xsl:map-entry key="'glossProperty'" select="'- topic/data concept/data glossentry/glossProperty '"/>
      <xsl:map-entry key="'glossStatus'" select="'- topic/data concept/data glossentry/glossStatus '"/>
      <xsl:map-entry key="'attributedef'" select="'- topic/data subjectScheme/attributedef '"/>
      <xsl:map-entry key="'elementdef'" select="'- topic/data subjectScheme/elementdef '"/>
      <xsl:map-entry key="'dd'" select="'- topic/dd '"/>
      <xsl:map-entry key="'ddhd'" select="'- topic/ddhd '"/>
      <xsl:map-entry key="'desc'" select="'- topic/desc '"/>
      <xsl:map-entry key="'div'" select="'- topic/div '"/>
      <xsl:map-entry key="'info'" select="'- topic/itemgroup task/info '"/>
      <xsl:map-entry key="'stepresult'" select="'- topic/itemgroup task/stepresult '"/>
      <xsl:map-entry key="'steptroubleshooting'" select="'- topic/itemgroup task/steptroubleshooting '"/>
      <xsl:map-entry key="'stepxmp'" select="'- topic/itemgroup task/stepxmp '"/>
      <xsl:map-entry key="'tutorialinfo'" select="'- topic/itemgroup task/tutorialinfo '"/>
      <xsl:map-entry key="'dl'" select="'- topic/dl '"/>
      <xsl:map-entry key="'dlentry'" select="'- topic/dlentry '"/>
      <xsl:map-entry key="'dlhead'" select="'- topic/dlhead '"/>
      <xsl:map-entry key="'draft-comment'" select="'- topic/draft-comment '"/>
      <xsl:map-entry key="'dt'" select="'- topic/dt '"/>
      <xsl:map-entry key="'dthd'" select="'- topic/dthd '"/>
      <xsl:map-entry key="'entry'" select="'- topic/entry '"/>
      <xsl:map-entry key="'example'" select="'- topic/example '"/>
      <xsl:map-entry key="'fallback'" select="'- topic/fallback '"/>
      <xsl:map-entry key="'featnum'" select="'- topic/featnum '"/>
      <xsl:map-entry key="'fig'" select="'- topic/fig '"/>
      <xsl:map-entry key="'figgroup'" select="'- topic/figgroup '"/>
      <xsl:map-entry key="'fn'" select="'- topic/fn '"/>
      <xsl:map-entry key="'foreign'" select="'- topic/foreign '"/>
      <xsl:map-entry key="'image'" select="'- topic/image '"/>
      <xsl:map-entry key="'glossSymbol'" select="'- topic/image concept/image glossentry/glossSymbol '"/>
      <xsl:map-entry key="'include'" select="'- topic/include '"/>
      <xsl:map-entry key="'index-see'" select="'+ topic/index-base indexing-d/index-see '"/>
      <xsl:map-entry key="'index-see-also'" select="'+ topic/index-base indexing-d/index-see-also '"/>
      <xsl:map-entry key="'indexterm'" select="'- topic/indexterm '"/>
      <xsl:map-entry key="'keyword'" select="'- topic/keyword '"/>
      <xsl:map-entry key="'keywords'" select="'- topic/keywords '"/>
      <xsl:map-entry key="'li'" select="'- topic/li '"/>
      <xsl:map-entry key="'choice'" select="'- topic/li task/choice '"/>
      <xsl:map-entry key="'step'" select="'- topic/li task/step '"/>
      <xsl:map-entry key="'stepsection'" select="'- topic/li task/stepsection '"/>
      <xsl:map-entry key="'lines'" select="'- topic/lines '"/>
      <xsl:map-entry key="'link'" select="'- topic/link '"/>
      <xsl:map-entry key="'linkinfo'" select="'- topic/linkinfo '"/>
      <xsl:map-entry key="'linklist'" select="'- topic/linklist '"/>
      <xsl:map-entry key="'linkpool'" select="'- topic/linkpool '"/>
      <xsl:map-entry key="'linktext'" select="'- map/linktext '"/>
      <xsl:map-entry key="'lq'" select="'- topic/lq '"/>
      <xsl:map-entry key="'media-source'" select="'- topic/media-source '"/>
      <xsl:map-entry key="'media-track'" select="'- topic/media-track '"/>
      <xsl:map-entry key="'metadata'" select="'- topic/metadata '"/>
      <xsl:map-entry key="'no-topic-nesting'" select="'- topic/no-topic-nesting '"/>
      <xsl:map-entry key="'note'" select="'- topic/note '"/>
      <xsl:map-entry key="'glossScopeNote'" select="'- topic/note concept/note glossentry/glossScopeNote '"/>
      <xsl:map-entry key="'glossUsage'" select="'- topic/note concept/note glossentry/glossUsage '"/>
      <xsl:map-entry key="'object'" select="'- topic/object '"/>
      <xsl:map-entry key="'ol'" select="'- topic/ol '"/>
      <xsl:map-entry key="'steps'" select="'- topic/ol task/steps '"/>
      <xsl:map-entry key="'othermeta'" select="'- topic/othermeta '"/>
      <xsl:map-entry key="'p'" select="'- topic/p '"/>
      <xsl:map-entry key="'glossSurfaceForm'" select="'- topic/p concept/p glossentry/glossSurfaceForm '"/>
      <xsl:map-entry key="'responsibleParty'" select="'- topic/p troubleshooting/responsibleParty '"/>
      <xsl:map-entry key="'param'" select="'- topic/param '"/>
      <xsl:map-entry key="'permissions'" select="'- topic/permissions '"/>
      <xsl:map-entry key="'ph'" select="'- topic/ph '"/>
      <xsl:map-entry key="'booklibrary'" select="'- topic/ph bookmap/booklibrary '"/>
      <xsl:map-entry key="'booktitlealt'" select="'- topic/ph bookmap/booktitlealt '"/>
      <xsl:map-entry key="'completed'" select="'- topic/ph bookmap/completed '"/>
      <xsl:map-entry key="'day'" select="'- topic/ph bookmap/day '"/>
      <xsl:map-entry key="'mainbooktitle'" select="'- topic/ph bookmap/mainbooktitle '"/>
      <xsl:map-entry key="'month'" select="'- topic/ph bookmap/month '"/>
      <xsl:map-entry key="'revisionid'" select="'- topic/ph bookmap/revisionid '"/>
      <xsl:map-entry key="'started'" select="'- topic/ph bookmap/started '"/>
      <xsl:map-entry key="'summary'" select="'- topic/ph bookmap/summary '"/>
      <xsl:map-entry key="'year'" select="'- topic/ph bookmap/year '"/>
      <xsl:map-entry key="'cmd'" select="'- topic/ph task/cmd '"/>
      <xsl:map-entry key="'platform'" select="'- topic/platform '"/>
      <xsl:map-entry key="'pre'" select="'- topic/pre '"/>
      <xsl:map-entry key="'prodinfo'" select="'- topic/prodinfo '"/>
      <xsl:map-entry key="'prodname'" select="'- topic/prodname '"/>
      <xsl:map-entry key="'prognum'" select="'- topic/prognum '"/>
      <xsl:map-entry key="'prolog'" select="'- topic/prolog '"/>
      <xsl:map-entry key="'publisher'" select="'- topic/publisher '"/>
      <xsl:map-entry key="'publisherinformation'" select="'- topic/publisher bookmap/publisherinformation '"/>
      <xsl:map-entry key="'q'" select="'- topic/q '"/>
      <xsl:map-entry key="'related-links'" select="'- topic/related-links '"/>
      <xsl:map-entry key="'required-cleanup'" select="'- topic/required-cleanup '"/>
      <xsl:map-entry key="'resourceid'" select="'- topic/resourceid '"/>
      <xsl:map-entry key="'revised'" select="'- topic/revised '"/>
      <xsl:map-entry key="'row'" select="'- topic/row '"/>
      <xsl:map-entry key="'section'" select="'- topic/section '"/>
      <xsl:map-entry key="'glossAlt'" select="'- topic/section concept/section glossentry/glossAlt '"/>
      <xsl:map-entry key="'refsyn'" select="'- topic/section reference/refsyn '"/>
      <xsl:map-entry key="'context'" select="'- topic/section task/context '"/>
      <xsl:map-entry key="'postreq'" select="'- topic/section task/postreq '"/>
      <xsl:map-entry key="'prereq'" select="'- topic/section task/prereq '"/>
      <xsl:map-entry key="'result'" select="'- topic/section task/result '"/>
      <xsl:map-entry key="'steps-informal'" select="'- topic/section task/steps-informal '"/>
      <xsl:map-entry key="'tasktroubleshooting'" select="'- topic/section task/tasktroubleshooting '"/>
      <xsl:map-entry key="'cause'" select="'- topic/section troubleshooting/cause '"/>
      <xsl:map-entry key="'condition'" select="'- topic/section troubleshooting/condition '"/>
      <xsl:map-entry key="'remedy'" select="'- topic/section troubleshooting/remedy '"/>
      <xsl:map-entry key="'series'" select="'- topic/series '"/>
      <xsl:map-entry key="'shortdesc'" select="'- map/shortdesc '"/>
      <xsl:map-entry key="'simpletable'" select="'- topic/simpletable '"/>
      <xsl:map-entry key="'properties'" select="'- topic/simpletable reference/properties '"/>
      <xsl:map-entry key="'choicetable'" select="'- topic/simpletable task/choicetable '"/>
      <xsl:map-entry key="'sl'" select="'- topic/sl '"/>
      <xsl:map-entry key="'sli'" select="'- topic/sli '"/>
      <xsl:map-entry key="'source'" select="'- topic/source '"/>
      <xsl:map-entry key="'state'" select="'- topic/state '"/>
      <xsl:map-entry key="'stentry'" select="'- topic/stentry '"/>
      <xsl:map-entry key="'propdesc'" select="'- topic/stentry reference/propdesc '"/>
      <xsl:map-entry key="'propdeschd'" select="'- topic/stentry reference/propdeschd '"/>
      <xsl:map-entry key="'proptype'" select="'- topic/stentry reference/proptype '"/>
      <xsl:map-entry key="'proptypehd'" select="'- topic/stentry reference/proptypehd '"/>
      <xsl:map-entry key="'propvalue'" select="'- topic/stentry reference/propvalue '"/>
      <xsl:map-entry key="'propvaluehd'" select="'- topic/stentry reference/propvaluehd '"/>
      <xsl:map-entry key="'chdesc'" select="'- topic/stentry task/chdesc '"/>
      <xsl:map-entry key="'chdeschd'" select="'- topic/stentry task/chdeschd '"/>
      <xsl:map-entry key="'choption'" select="'- topic/stentry task/choption '"/>
      <xsl:map-entry key="'choptionhd'" select="'- topic/stentry task/choptionhd '"/>
      <xsl:map-entry key="'sthead'" select="'- topic/sthead '"/>
      <xsl:map-entry key="'prophead'" select="'- topic/sthead reference/prophead '"/>
      <xsl:map-entry key="'chhead'" select="'- topic/sthead task/chhead '"/>
      <xsl:map-entry key="'strow'" select="'- topic/strow '"/>
      <xsl:map-entry key="'property'" select="'- topic/strow reference/property '"/>
      <xsl:map-entry key="'chrow'" select="'- topic/strow task/chrow '"/>
      <xsl:map-entry key="'table'" select="'- topic/table '"/>
      <xsl:map-entry key="'tbody'" select="'- topic/tbody '"/>
      <xsl:map-entry key="'term'" select="'- topic/term '"/>
      <xsl:map-entry key="'text'" select="'- topic/text '"/>
      <xsl:map-entry key="'tgroup'" select="'- topic/tgroup '"/>
      <xsl:map-entry key="'thead'" select="'- topic/thead '"/>
      <xsl:map-entry key="'title'" select="'- topic/title '"/>
      <xsl:map-entry key="'booktitle'" select="'- topic/title bookmap/booktitle '"/>
      <xsl:map-entry key="'glossAbbreviation'" select="'- topic/title concept/title glossentry/glossAbbreviation '"/>
      <xsl:map-entry key="'glossAcronym'" select="'- topic/title concept/title glossentry/glossAcronym '"/>
      <xsl:map-entry key="'glossShortForm'" select="'- topic/title concept/title glossentry/glossShortForm '"/>
      <xsl:map-entry key="'glossSynonym'" select="'- topic/title concept/title glossentry/glossSynonym '"/>
      <xsl:map-entry key="'glossterm'" select="'- topic/title concept/title glossentry/glossterm '"/>
      <xsl:map-entry key="'titlealt'" select="'- topic/titlealt '"/>
      <xsl:map-entry key="'tm'" select="'- topic/tm '"/>
      <!--xsl:map-entry key="'topic'" select="'- topic/topic '"/-->
      <xsl:map-entry key="'concept'" select="'- topic/topic concept/concept '"/>
      <xsl:map-entry key="'glossentry'" select="'- topic/topic concept/concept glossentry/glossentry '"/>
      <xsl:map-entry key="'glossgroup'" select="'- topic/topic concept/concept glossgroup/glossgroup '"/>
      <xsl:map-entry key="'reference'" select="'- topic/topic reference/reference '"/>
      <xsl:map-entry key="'task'" select="'- topic/topic task/task '"/>
      <xsl:map-entry key="'troubleshooting'" select="'- topic/topic troubleshooting/troubleshooting '"/>
      <xsl:map-entry key="'ul'" select="'- topic/ul '"/>
      <xsl:map-entry key="'choices'" select="'- topic/ul task/choices '"/>
      <xsl:map-entry key="'steps-unordered'" select="'- topic/ul task/steps-unordered '"/>
      <xsl:map-entry key="'unknown'" select="'- topic/unknown '"/>
      <xsl:map-entry key="'video'" select="'- topic/video '"/>
      <xsl:map-entry key="'vrm'" select="'- topic/vrm '"/>
      <xsl:map-entry key="'vrmlist'" select="'- topic/vrmlist '"/>
      <xsl:map-entry key="'xref'" select="'- topic/xref '"/>
      <xsl:map-entry key="'glossAlternateFor'" select="'- topic/xref concept/xref glossentry/glossAlternateFor '"/>
      <xsl:map-entry key="'addressdetails'" select="'+ topic/ph xnal-d/addressdetails '"/>
      <xsl:map-entry key="'administrativearea'" select="'+ topic/ph xnal-d/administrativearea '"/>
      <xsl:map-entry key="'anchor'" select="'- map/anchor '"/>
      <xsl:map-entry key="'anchorid'" select="'+ topic/keyword delay-d/anchorid '"/>
      <xsl:map-entry key="'anchorkey'" select="'+ topic/keyword delay-d/anchorkey '"/>
      <xsl:map-entry key="'anchorref'" select="'+ map/topicref mapgroup-d/anchorref '"/>
      <xsl:map-entry key="'authorinformation'" select="'+ topic/author xnal-d/authorinformation '"/>
      <xsl:map-entry key="'boolean'" select="'- topic/boolean '"/>
      <xsl:map-entry key="'closereqs'" select="'+ topic/section task/postreq taskreq-d/closereqs '"/>
      <xsl:map-entry key="'contactnumber'" select="'+ topic/data xnal-d/contactnumber '"/>
      <xsl:map-entry key="'contactnumbers'" select="'+ topic/data xnal-d/contactnumbers '"/>
      <xsl:map-entry key="'country'" select="'+ topic/ph xnal-d/country '"/>
      <xsl:map-entry key="'data-about'" select="'- topic/data-about '"/>
      <xsl:map-entry key="'emailaddress'" select="'+ topic/data xnal-d/emailaddress '"/>
      <xsl:map-entry key="'emailaddresses'" select="'+ topic/data xnal-d/emailaddresses '"/>
      <xsl:map-entry key="'esttime'" select="'+ topic/li task/li taskreq-d/esttime '"/>
      <xsl:map-entry key="'exportanchors'" select="'+ topic/keywords delay-d/exportanchors '"/>
      <xsl:map-entry key="'firstname'" select="'+ topic/data xnal-d/firstname '"/>
      <xsl:map-entry key="'generationidentifier'" select="'+ topic/data xnal-d/generationidentifier '"/>
      <xsl:map-entry key="'hasInstance'" select="'- map/topicref subjectScheme/hasInstance '"/>
      <xsl:map-entry key="'hasKind'" select="'- map/topicref subjectScheme/hasKind '"/>
      <xsl:map-entry key="'hasNarrower'" select="'- map/topicref subjectScheme/hasNarrower '"/>
      <xsl:map-entry key="'hasPart'" select="'- map/topicref subjectScheme/hasPart '"/>
      <xsl:map-entry key="'hasRelated'" select="'- map/topicref subjectScheme/hasRelated '"/>
      <xsl:map-entry key="'honorific'" select="'+ topic/data xnal-d/honorific '"/>
      <xsl:map-entry key="'index-base'" select="'- topic/index-base '"/>
      <xsl:map-entry key="'index-sort-as'" select="'+ topic/index-base indexing-d/index-sort-as '"/>
      <xsl:map-entry key="'indextermref'" select="'- topic/indextermref '"/>
      <xsl:map-entry key="'itemgroup'" select="'- topic/itemgroup '"/>
      <xsl:map-entry key="'lastname'" select="'+ topic/data xnal-d/lastname '"/>
      <xsl:map-entry key="'lcAge'" select="'- topic/p learningBase/p learningPlan/lcAge '"/>
      <xsl:map-entry key="'lcAnswerContent'" select="'+ topic/p learningInteractionBase-d/p learning-d/lcAnswerContent '"/>
      <xsl:map-entry key="'lcAnswerContent2'" select="'+ topic/div learningInteractionBase2-d/div learning2-d/lcAnswerContent2 '"/>
      <xsl:map-entry key="'lcAnswerOption'" select="'+ topic/li learningInteractionBase-d/li learning-d/lcAnswerOption '"/>
      <xsl:map-entry key="'lcAnswerOption2'" select="'+ topic/li learningInteractionBase2-d/li learning2-d/lcAnswerOption2 '"/>
      <xsl:map-entry key="'lcAnswerOptionGroup'" select="'+ topic/ul learningInteractionBase-d/ul learning-d/lcAnswerOptionGroup '"/>
      <xsl:map-entry key="'lcAnswerOptionGroup2'" select="'+ topic/ul learningInteractionBase2-d/ul learning2-d/lcAnswerOptionGroup2 '"/>
      <xsl:map-entry key="'lcArea'" select="'+ topic/figgroup learningInteractionBase-d/figgroup learning-d/lcArea '"/>
      <xsl:map-entry key="'lcArea2'" select="'+ topic/figgroup learningInteractionBase2-d/figgroup learning2-d/lcArea2 '"/>
      <xsl:map-entry key="'lcAreaCoords'" select="'+ topic/ph learningInteractionBase-d/ph learning-d/lcAreaCoords '"/>
      <xsl:map-entry key="'lcAreaCoords2'" select="'+ topic/ph learningInteractionBase2-d/ph learning2-d/lcAreaCoords2 '"/>
      <xsl:map-entry key="'lcAreaShape'" select="'+ topic/keyword learningInteractionBase-d/keyword learning-d/lcAreaShape '"/>
      <xsl:map-entry key="'lcAreaShape2'" select="'+ topic/keyword learningInteractionBase2-d/keyword learning2-d/lcAreaShape2 '"/>
      <xsl:map-entry key="'lcAssessment'" select="'- topic/p learningBase/p learningPlan/lcAssessment '"/>
      <xsl:map-entry key="'lcAsset'" select="'+ topic/p learningInteractionBase-d/p learning-d/lcAsset '"/>
      <xsl:map-entry key="'lcAsset2'" select="'+ topic/div learningInteractionBase2-d/div learning2-d/lcAsset2 '"/>
      <xsl:map-entry key="'lcAttitude'" select="'- topic/p learningBase/p learningPlan/lcAttitude '"/>
      <xsl:map-entry key="'lcAudience'" select="'- topic/section learningBase/lcAudience '"/>
      <xsl:map-entry key="'lcBackground'" select="'- topic/p learningBase/p learningPlan/lcBackground '"/>
      <xsl:map-entry key="'lcCIN'" select="'- topic/fig learningBase/fig learningPlan/lcCIN '"/>
      <xsl:map-entry key="'lcChallenge'" select="'- topic/section learningBase/lcChallenge '"/>
      <xsl:map-entry key="'lcClassroom'" select="'- topic/fig learningBase/fig learningPlan/lcClassroom '"/>
      <xsl:map-entry key="'lcClient'" select="'- topic/fig learningBase/fig learningPlan/lcClient '"/>
      <xsl:map-entry key="'lcConstraints'" select="'- topic/fig learningBase/fig learningPlan/lcConstraints '"/>
      <xsl:map-entry key="'lcCorrectResponse'" select="'+ topic/data learningInteractionBase-d/data learning-d/lcCorrectResponse '"/>
      <xsl:map-entry key="'lcCorrectResponse2'" select="'+ topic/data learningInteractionBase2-d/data learning2-d/lcCorrectResponse2 '"/>
      <xsl:map-entry key="'lcDelivDate'" select="'- topic/fig learningBase/fig learningPlan/lcDelivDate '"/>
      <xsl:map-entry key="'lcDelivery'" select="'- topic/p learningBase/p learningPlan/lcDelivery '"/>
      <xsl:map-entry key="'lcDownloadTime'" select="'- topic/fig learningBase/fig learningPlan/lcDownloadTime '"/>
      <xsl:map-entry key="'lcDuration'" select="'- topic/section learningBase/lcDuration '"/>
      <xsl:map-entry key="'lcEdLevel'" select="'- topic/p learningBase/p learningPlan/lcEdLevel '"/>
      <xsl:map-entry key="'lcFeedback'" select="'+ topic/p learningInteractionBase-d/p learning-d/lcFeedback '"/>
      <xsl:map-entry key="'lcFeedback2'" select="'+ topic/div learningInteractionBase2-d/div learning2-d/lcFeedback2 '"/>
      <xsl:map-entry key="'lcFeedbackCorrect'" select="'+ topic/p learningInteractionBase-d/p learning-d/lcFeedbackCorrect '"/>
      <xsl:map-entry key="'lcFeedbackCorrect2'" select="'+ topic/div learningInteractionBase2-d/div learning2-d/lcFeedbackCorrect2 '"/>
      <xsl:map-entry key="'lcFeedbackIncorrect'" select="'+ topic/p learningInteractionBase-d/p learning-d/lcFeedbackIncorrect '"/>
      <xsl:map-entry key="'lcFeedbackIncorrect2'" select="'+ topic/div learningInteractionBase2-d/div learning2-d/lcFeedbackIncorrect2 '"/>
      <xsl:map-entry key="'lcFileSizeLimitations'" select="'- topic/fig learningBase/fig learningPlan/lcFileSizeLimitations '"/>
      <xsl:map-entry key="'lcGapAnalysis'" select="'- topic/section learningBase/section learningPlan/lcGapAnalysis '"/>
      <xsl:map-entry key="'lcGapItem'" select="'- topic/fig learningBase/fig learningPlan/lcGapItem '"/>
      <xsl:map-entry key="'lcGapItemDelta'" select="'- topic/p learningBase/p learningPlan/lcGapItemDelta '"/>
      <xsl:map-entry key="'lcGeneralDescription'" select="'- topic/p learningBase/p learningPlan/lcGeneralDescription '"/>
      <xsl:map-entry key="'lcGoals'" select="'- topic/p learningBase/p learningPlan/lcGoals '"/>
      <xsl:map-entry key="'lcGraphics'" select="'- topic/fig learningBase/fig learningPlan/lcGraphics '"/>
      <xsl:map-entry key="'lcHandouts'" select="'- topic/fig learningBase/fig learningPlan/lcHandouts '"/>
      <xsl:map-entry key="'lcHotspot'" select="'+ topic/fig learningInteractionBase-d/lcInteractionBase learning-d/lcHotspot '"/>
      <xsl:map-entry key="'lcHotspot2'" select="'+ topic/div learningInteractionBase2-d/lcInteractionBase2 learning2-d/lcHotspot2 '"/>
      <xsl:map-entry key="'lcHotspotMap'" select="'+ topic/fig learningInteractionBase-d/figgroup learning-d/lcHotspotMap '"/>
      <xsl:map-entry key="'lcHotspotMap2'" select="'+ topic/fig learningInteractionBase2-d/figgroup learning2-d/lcHotspotMap2 '"/>
      <xsl:map-entry key="'lcInstruction'" select="'- topic/section learningBase/lcInstruction '"/>
      <xsl:map-entry key="'lcInstructornote'" select="'+ topic/note learningInteractionBase-d/note learning-d/lcInstructornote '"/>
      <xsl:map-entry key="'lcInstructornote2'" select="'+ topic/note learningInteractionBase2-d/note learning2-d/lcInstructornote2 '"/>
      <xsl:map-entry key="'lcInteraction'" select="'- topic/section learningBase/lcInteraction '"/>
      <xsl:map-entry key="'lcInteractionBase'" select="'+ topic/fig learningInteractionBase-d/lcInteractionBase '"/>
      <xsl:map-entry key="'lcInteractionBase2'" select="'+ topic/div learningInteractionBase2-d/lcInteractionBase2 '"/>
      <xsl:map-entry key="'lcInteractionLabel2'" select="'+ topic/p learningInteractionBase2-d/lcInteractionLabel2 '"/>
      <xsl:map-entry key="'lcIntervention'" select="'- topic/section learningBase/section learningPlan/lcIntervention '"/>
      <xsl:map-entry key="'lcInterventionItem'" select="'- topic/fig learningBase/fig learningPlan/lcInterventionItem '"/>
      <xsl:map-entry key="'lcIntro'" select="'- topic/section learningBase/lcIntro '"/>
      <xsl:map-entry key="'lcItem'" select="'+ topic/stentry learningInteractionBase-d/stentry learning-d/lcItem '"/>
      <xsl:map-entry key="'lcItem2'" select="'+ topic/stentry learningInteractionBase2-d/stentry learning2-d/lcItem2 '"/>
      <xsl:map-entry key="'lcJtaItem'" select="'- topic/p learningBase/p learningPlan/lcJtaItem '"/>
      <xsl:map-entry key="'lcKnowledge'" select="'- topic/p learningBase/p learningPlan/lcKnowledge '"/>
      <xsl:map-entry key="'lcLMS'" select="'- topic/fig learningBase/fig learningPlan/lcLMS '"/>
      <xsl:map-entry key="'lcLearnStrat'" select="'- topic/p learningBase/p learningPlan/lcLearnStrat '"/>
      <xsl:map-entry key="'lcLom'" select="'+ topic/metadata learningmeta-d/lcLom '"/>
      <xsl:map-entry key="'lcMatchTable'" select="'+ topic/simpletable learningInteractionBase-d/simpletable learning-d/lcMatchTable '"/>
      <xsl:map-entry key="'lcMatchTable2'" select="'+ topic/simpletable learningInteractionBase2-d/simpletable learning2-d/lcMatchTable2 '"/>
      <xsl:map-entry key="'lcMatching'" select="'+ topic/fig learningInteractionBase-d/lcInteractionBase learning-d/lcMatching '"/>
      <xsl:map-entry key="'lcMatching2'" select="'+ topic/div learningInteractionBase2-d/lcInteractionBase2 learning2-d/lcMatching2 '"/>
      <xsl:map-entry key="'lcMatchingHeader'" select="'+ topic/sthead learningInteractionBase-d/sthead learning-d/lcMatchingHeader '"/>
      <xsl:map-entry key="'lcMatchingHeader2'" select="'+ topic/sthead learningInteractionBase2-d/sthead learning2-d/lcMatchingHeader2 '"/>
      <xsl:map-entry key="'lcMatchingItem'" select="'+ topic/stentry learningInteractionBase-d/stentry learning-d/lcMatchingItem '"/>
      <xsl:map-entry key="'lcMatchingItem2'" select="'+ topic/stentry learningInteractionBase2-d/stentry learning2-d/lcMatchingItem2 '"/>
      <xsl:map-entry key="'lcMatchingItemFeedback'" select="'+ topic/stentry learningInteractionBase-d/stentry learning-d/lcMatchingItemFeedback '"/>
      <xsl:map-entry key="'lcMatchingItemFeedback2'" select="'+ topic/stentry learningInteractionBase2-d/stentry learning2-d/lcMatchingItemFeedback2 '"/>
      <xsl:map-entry key="'lcMatchingPair'" select="'+ topic/strow learningInteractionBase-d/strow learning-d/lcMatchingPair '"/>
      <xsl:map-entry key="'lcMatchingPair2'" select="'+ topic/strow learningInteractionBase2-d/strow learning2-d/lcMatchingPair2 '"/>
      <xsl:map-entry key="'lcModDate'" select="'- topic/fig learningBase/fig learningPlan/lcModDate '"/>
      <xsl:map-entry key="'lcMotivation'" select="'- topic/p learningBase/p learningPlan/lcMotivation '"/>
      <xsl:map-entry key="'lcMultipleSelect'" select="'+ topic/fig learningInteractionBase-d/lcInteractionBase learning-d/lcMultipleSelect '"/>
      <xsl:map-entry key="'lcMultipleSelect2'" select="'+ topic/div learningInteractionBase2-d/lcInteractionBase2 learning2-d/lcMultipleSelect2 '"/>
      <xsl:map-entry key="'lcNeeds'" select="'- topic/p learningBase/p learningPlan/lcNeeds '"/>
      <xsl:map-entry key="'lcNeedsAnalysis'" select="'- topic/section learningBase/section learningPlan/lcNeedsAnalysis '"/>
      <xsl:map-entry key="'lcNextSteps'" select="'- topic/section learningBase/lcNextSteps '"/>
      <xsl:map-entry key="'lcNoLMS'" select="'- topic/fig learningBase/fig learningPlan/lcNoLMS '"/>
      <xsl:map-entry key="'lcOJT'" select="'- topic/fig learningBase/fig learningPlan/lcOJT '"/>
      <xsl:map-entry key="'lcObjective'" select="'- topic/li learningBase/lcObjective '"/>
      <xsl:map-entry key="'lcObjectives'" select="'- topic/section learningBase/lcObjectives '"/>
      <xsl:map-entry key="'lcObjectivesGroup'" select="'- topic/ul learningBase/lcObjectivesGroup '"/>
      <xsl:map-entry key="'lcObjectivesStem'" select="'- topic/ph learningBase/lcObjectivesStem '"/>
      <xsl:map-entry key="'lcOpenAnswer'" select="'+ topic/p learningInteractionBase-d/p learning-d/lcOpenAnswer '"/>
      <xsl:map-entry key="'lcOpenAnswer2'" select="'+ topic/div learningInteractionBase2-d/div learning2-d/lcOpenAnswer2 '"/>
      <xsl:map-entry key="'lcOpenQuestion'" select="'+ topic/fig learningInteractionBase-d/lcInteractionBase learning-d/lcOpenQuestion '"/>
      <xsl:map-entry key="'lcOpenQuestion2'" select="'+ topic/div learningInteractionBase2-d/lcInteractionBase2 learning2-d/lcOpenQuestion2 '"/>
      <xsl:map-entry key="'lcOrgConstraints'" select="'- topic/p learningBase/p learningPlan/lcOrgConstraints '"/>
      <xsl:map-entry key="'lcOrganizational'" select="'- topic/fig learningBase/fig learningPlan/lcOrganizational '"/>
      <xsl:map-entry key="'lcPlanAudience'" select="'- topic/fig learningBase/fig learningPlan/lcPlanAudience '"/>
      <xsl:map-entry key="'lcPlanDescrip'" select="'- topic/fig learningBase/fig learningPlan/lcPlanDescrip '"/>
      <xsl:map-entry key="'lcPlanObjective'" select="'- topic/p learningBase/p learningPlan/lcPlanObjective '"/>
      <xsl:map-entry key="'lcPlanPrereqs'" select="'- topic/fig learningBase/fig learningPlan/lcPlanPrereqs '"/>
      <xsl:map-entry key="'lcPlanResources'" select="'- topic/p learningBase/p learningPlan/lcPlanResources '"/>
      <xsl:map-entry key="'lcPlanSubject'" select="'- topic/fig learningBase/fig learningPlan/lcPlanSubject '"/>
      <xsl:map-entry key="'lcPlanTitle'" select="'- topic/fig learningBase/fig learningPlan/lcPlanTitle '"/>
      <xsl:map-entry key="'lcPlayers'" select="'- topic/fig learningBase/fig learningPlan/lcPlayers '"/>
      <xsl:map-entry key="'lcPrereqs'" select="'- topic/section learningBase/lcPrereqs '"/>
      <xsl:map-entry key="'lcProcesses'" select="'- topic/p learningBase/p learningPlan/lcProcesses '"/>
      <xsl:map-entry key="'lcProject'" select="'- topic/section learningBase/section learningPlan/lcProject '"/>
      <xsl:map-entry key="'lcQuestion'" select="'+ topic/p learningInteractionBase-d/lcQuestionBase learning-d/lcQuestion '"/>
      <xsl:map-entry key="'lcQuestion2'" select="'+ topic/div learningInteractionBase2-d/lcQuestionBase2 learning2-d/lcQuestion2 '"/>
      <xsl:map-entry key="'lcQuestionBase'" select="'+ topic/p learningInteractionBase-d/lcQuestionBase '"/>
      <xsl:map-entry key="'lcQuestionBase2'" select="'+ topic/div learningInteractionBase2-d/lcQuestionBase2 '"/>
      <xsl:map-entry key="'lcResolution'" select="'- topic/fig learningBase/fig learningPlan/lcResolution '"/>
      <xsl:map-entry key="'lcResources'" select="'- topic/section learningBase/lcResources '"/>
      <xsl:map-entry key="'lcReview'" select="'- topic/section learningBase/lcReview '"/>
      <xsl:map-entry key="'lcSecurity'" select="'- topic/fig learningBase/fig learningPlan/lcSecurity '"/>
      <xsl:map-entry key="'lcSequence'" select="'+ topic/data learningInteractionBase-d/data learning-d/lcSequence '"/>
      <xsl:map-entry key="'lcSequence2'" select="'+ topic/data learningInteractionBase2-d/data learning2-d/lcSequence2 '"/>
      <xsl:map-entry key="'lcSequenceOption'" select="'+ topic/li learningInteractionBase-d/li learning-d/lcSequenceOption '"/>
      <xsl:map-entry key="'lcSequenceOption2'" select="'+ topic/li learningInteractionBase2-d/li learning2-d/lcSequenceOption2 '"/>
      <xsl:map-entry key="'lcSequenceOptionGroup'" select="'+ topic/ol learningInteractionBase-d/ol learning-d/lcSequenceOptionGroup '"/>
      <xsl:map-entry key="'lcSequenceOptionGroup2'" select="'+ topic/ol learningInteractionBase2-d/ol learning2-d/lcSequenceOptionGroup2 '"/>
      <xsl:map-entry key="'lcSequencing'" select="'+ topic/fig learningInteractionBase-d/lcInteractionBase learning-d/lcSequencing '"/>
      <xsl:map-entry key="'lcSequencing2'" select="'+ topic/div learningInteractionBase2-d/lcInteractionBase2 learning2-d/lcSequencing2 '"/>
      <xsl:map-entry key="'lcSingleSelect'" select="'+ topic/fig learningInteractionBase-d/lcInteractionBase learning-d/lcSingleSelect '"/>
      <xsl:map-entry key="'lcSingleSelect2'" select="'+ topic/div learningInteractionBase2-d/lcInteractionBase2 learning2-d/lcSingleSelect2 '"/>
      <xsl:map-entry key="'lcSkills'" select="'- topic/p learningBase/p learningPlan/lcSkills '"/>
      <xsl:map-entry key="'lcSpecChars'" select="'- topic/p learningBase/p learningPlan/lcSpecChars '"/>
      <xsl:map-entry key="'lcSummary'" select="'- topic/section learningBase/lcSummary '"/>
      <xsl:map-entry key="'lcTask'" select="'- topic/fig learningBase/fig learningPlan/lcTask '"/>
      <xsl:map-entry key="'lcTaskItem'" select="'- topic/p learningBase/p learningPlan/lcTaskItem '"/>
      <xsl:map-entry key="'lcTechnical'" select="'- topic/section learningBase/section learningPlan/lcTechnical '"/>
      <xsl:map-entry key="'lcTime'" select="'- topic/data learningBase/lcTime '"/>
      <xsl:map-entry key="'lcTrueFalse'" select="'+ topic/fig learningInteractionBase-d/lcInteractionBase learning-d/lcTrueFalse '"/>
      <xsl:map-entry key="'lcTrueFalse2'" select="'+ topic/div learningInteractionBase2-d/lcInteractionBase2 learning2-d/lcTrueFalse2 '"/>
      <xsl:map-entry key="'lcValues'" select="'- topic/p learningBase/p learningPlan/lcValues '"/>
      <xsl:map-entry key="'lcViewers'" select="'- topic/fig learningBase/fig learningPlan/lcViewers '"/>
      <xsl:map-entry key="'lcW3C'" select="'- topic/fig learningBase/fig learningPlan/lcW3C '"/>
      <xsl:map-entry key="'lcWorkEnv'" select="'- topic/fig learningBase/fig learningPlan/lcWorkEnv '"/>
      <xsl:map-entry key="'lcWorkEnvDescription'" select="'- topic/p learningBase/p learningPlan/lcWorkEnvDescription '"/>
      <xsl:map-entry key="'learningAssessment'" select="'- topic/topic learningBase/learningBase learningAssessment/learningAssessment '"/>
      <xsl:map-entry key="'learningAssessmentbody'" select="'- topic/body learningBase/learningBasebody learningAssessment/learningAssessmentbody '"/>
      <xsl:map-entry key="'learningBase'" select="'- topic/topic learningBase/learningBase '"/>
      <xsl:map-entry key="'learningBasebody'" select="'- topic/body learningBase/learningBasebody '"/>
      <xsl:map-entry key="'learningContent'" select="'- topic/topic learningBase/learningBase learningContent/learningContent '"/>
      <xsl:map-entry key="'learningContentRef'" select="'+ map/topicref learningmap-d/learningContentRef '"/>
      <xsl:map-entry key="'learningContentbody'" select="'- topic/body learningBase/learningBasebody learningContent/learningContentbody '"/>
      <xsl:map-entry key="'learningGroup'" select="'+ map/topicref learningmap-d/learningGroup '"/>
      <xsl:map-entry key="'learningGroupMap'" select="'- map/map learningGroupMap/learningGroupMap '"/>
      <xsl:map-entry key="'learningGroupMapRef'" select="'+ map/topicref learningmap-d/learningGroupMapRef '"/>
      <xsl:map-entry key="'learningObject'" select="'+ map/topicref learningmap-d/learningObject '"/>
      <xsl:map-entry key="'learningObjectMap'" select="'- map/map learningObjectMap/learningObjectMap '"/>
      <xsl:map-entry key="'learningObjectMapRef'" select="'+ map/topicref learningmap-d/learningObjectMapRef '"/>
      <xsl:map-entry key="'learningOverview'" select="'- topic/topic learningBase/learningBase learningOverview/learningOverview '"/>
      <xsl:map-entry key="'learningOverviewRef'" select="'+ map/topicref learningmap-d/learningOverviewRef '"/>
      <xsl:map-entry key="'learningOverviewbody'" select="'- topic/body learningBase/learningBasebody learningOverview/learningOverviewbody '"/>
      <xsl:map-entry key="'learningPlan'" select="'- topic/topic learningBase/learningBase learningPlan/learningPlan '"/>
      <xsl:map-entry key="'learningPlanRef'" select="'+ map/topicref learningmap-d/learningPlanRef '"/>
      <xsl:map-entry key="'learningPlanbody'" select="'- topic/body learningBase/learningBasebody learningPlan/learningPlanbody '"/>
      <xsl:map-entry key="'learningPostAssessmentRef'" select="'+ map/topicref learningmap-d/learningPostAssessmentRef '"/>
      <xsl:map-entry key="'learningPreAssessmentRef'" select="'+ map/topicref learningmap-d/learningPreAssessmentRef '"/>
      <xsl:map-entry key="'learningSummary'" select="'- topic/topic learningBase/learningBase learningSummary/learningSummary '"/>
      <xsl:map-entry key="'learningSummaryRef'" select="'+ map/topicref learningmap-d/learningSummaryRef '"/>
      <xsl:map-entry key="'learningSummarybody'" select="'- topic/body learningBase/learningBasebody learningSummary/learningSummarybody '"/>
      <xsl:map-entry key="'locality'" select="'+ topic/ph xnal-d/locality '"/>
      <xsl:map-entry key="'localityname'" select="'+ topic/ph xnal-d/localityname '"/>
      <xsl:map-entry key="'lomAggregationLevel'" select="'+ topic/data learningmeta-d/lomAggregationLevel '"/>
      <xsl:map-entry key="'lomContext'" select="'+ topic/data learningmeta-d/lomContext '"/>
      <xsl:map-entry key="'lomCoverage'" select="'+ topic/data learningmeta-d/lomCoverage '"/>
      <xsl:map-entry key="'lomDifficulty'" select="'+ topic/data learningmeta-d/lomDifficulty '"/>
      <xsl:map-entry key="'lomInstallationRemarks'" select="'+ topic/data learningmeta-d/lomInstallationRemarks '"/>
      <xsl:map-entry key="'lomIntendedUserRole'" select="'+ topic/data learningmeta-d/lomIntendedUserRole '"/>
      <xsl:map-entry key="'lomInteractivityLevel'" select="'+ topic/data learningmeta-d/lomInteractivityLevel '"/>
      <xsl:map-entry key="'lomInteractivityType'" select="'+ topic/data learningmeta-d/lomInteractivityType '"/>
      <xsl:map-entry key="'lomLearningResourceType'" select="'+ topic/data learningmeta-d/lomLearningResourceType '"/>
      <xsl:map-entry key="'lomOtherPlatformRequirements'" select="'+ topic/data learningmeta-d/lomOtherPlatformRequirements '"/>
      <xsl:map-entry key="'lomSemanticDensity'" select="'+ topic/data learningmeta-d/lomSemanticDensity '"/>
      <xsl:map-entry key="'lomStructure'" select="'+ topic/data learningmeta-d/lomStructure '"/>
      <xsl:map-entry key="'lomTechRequirement'" select="'+ topic/data learningmeta-d/lomTechRequirement '"/>
      <xsl:map-entry key="'lomTypicalAgeRange'" select="'+ topic/data learningmeta-d/lomTypicalAgeRange '"/>
      <xsl:map-entry key="'lomTypicalLearningTime'" select="'+ topic/data learningmeta-d/lomTypicalLearningTime '"/>
      <xsl:map-entry key="'middlename'" select="'+ topic/data xnal-d/middlename '"/>
      <xsl:map-entry key="'namedetails'" select="'+ topic/data xnal-d/namedetails '"/>
      <xsl:map-entry key="'noconds'" select="'+ topic/li task/li taskreq-d/noconds '"/>
      <xsl:map-entry key="'nosafety'" select="'+ topic/li task/li taskreq-d/nosafety '"/>
      <xsl:map-entry key="'nospares'" select="'+ topic/data task/data taskreq-d/nospares '"/>
      <xsl:map-entry key="'nosupeq'" select="'+ topic/data task/data taskreq-d/nosupeq '"/>
      <xsl:map-entry key="'nosupply'" select="'+ topic/data task/data taskreq-d/nosupply '"/>
      <xsl:map-entry key="'organizationinfo'" select="'+ topic/data xnal-d/organizationinfo '"/>
      <xsl:map-entry key="'organizationname'" select="'+ topic/ph xnal-d/organizationname '"/>
      <xsl:map-entry key="'organizationnamedetails'" select="'+ topic/ph xnal-d/organizationnamedetails '"/>
      <xsl:map-entry key="'otherinfo'" select="'+ topic/data xnal-d/otherinfo '"/>
      <xsl:map-entry key="'perscat'" select="'+ topic/li task/li taskreq-d/perscat '"/>
      <xsl:map-entry key="'perskill'" select="'+ topic/li task/li taskreq-d/perskill '"/>
      <xsl:map-entry key="'personinfo'" select="'+ topic/data xnal-d/personinfo '"/>
      <xsl:map-entry key="'personname'" select="'+ topic/data xnal-d/personname '"/>
      <xsl:map-entry key="'personnel'" select="'+ topic/li task/li taskreq-d/personnel '"/>
      <xsl:map-entry key="'postalcode'" select="'+ topic/ph xnal-d/postalcode '"/>
      <xsl:map-entry key="'prelreqs'" select="'+ topic/section task/prereq taskreq-d/prelreqs '"/>
      <xsl:map-entry key="'relatedSubjects'" select="'- map/topicref subjectScheme/relatedSubjects '"/>
      <xsl:map-entry key="'reqcond'" select="'+ topic/li task/li taskreq-d/reqcond '"/>
      <xsl:map-entry key="'reqconds'" select="'+ topic/ul task/ul taskreq-d/reqconds '"/>
      <xsl:map-entry key="'reqcontp'" select="'+ topic/li task/li taskreq-d/reqcontp '"/>
      <xsl:map-entry key="'reqpers'" select="'+ topic/ul task/ul taskreq-d/reqpers '"/>
      <xsl:map-entry key="'safecond'" select="'+ topic/li task/li taskreq-d/safecond '"/>
      <xsl:map-entry key="'safety'" select="'+ topic/ol task/ol taskreq-d/safety '"/>
      <xsl:map-entry key="'sectiondiv'" select="'- topic/sectiondiv '"/>
      <xsl:map-entry key="'spare'" select="'+ topic/li task/li taskreq-d/spare '"/>
      <xsl:map-entry key="'spares'" select="'+ topic/p task/p taskreq-d/spares '"/>
      <xsl:map-entry key="'sparesli'" select="'+ topic/ul task/ul taskreq-d/sparesli '"/>
      <xsl:map-entry key="'subjectCell'" select="'+ map/relcell classify-d/subjectCell '"/>
      <xsl:map-entry key="'subjectRel'" select="'- map/relrow subjectScheme/subjectRel '"/>
      <xsl:map-entry key="'subjectRelHeader'" select="'- map/relrow subjectScheme/subjectRelHeader '"/>
      <xsl:map-entry key="'subjectRelTable'" select="'- map/reltable subjectScheme/subjectRelTable '"/>
      <xsl:map-entry key="'subjectRole'" select="'- map/relcell subjectScheme/subjectRole '"/>
      <xsl:map-entry key="'subjectref'" select="'+ map/topicref classify-d/subjectref '"/>
      <xsl:map-entry key="'substep'" select="'- topic/li task/substep '"/>
      <xsl:map-entry key="'substeps'" select="'- topic/ol task/substeps '"/>
      <xsl:map-entry key="'supeqli'" select="'+ topic/ul task/ul taskreq-d/supeqli '"/>
      <xsl:map-entry key="'supequi'" select="'+ topic/li task/li taskreq-d/supequi '"/>
      <xsl:map-entry key="'supequip'" select="'+ topic/p task/p taskreq-d/supequip '"/>
      <xsl:map-entry key="'supplies'" select="'+ topic/p task/p taskreq-d/supplies '"/>
      <xsl:map-entry key="'supply'" select="'+ topic/li task/li taskreq-d/supply '"/>
      <xsl:map-entry key="'supplyli'" select="'+ topic/ul task/ul taskreq-d/supplyli '"/>
      <xsl:map-entry key="'thoroughfare'" select="'+ topic/ph xnal-d/thoroughfare '"/>
      <xsl:map-entry key="'titlealts'" select="'- topic/titlealts '"/>
      <xsl:map-entry key="'topicCell'" select="'+ map/relcell classify-d/topicCell '"/>
      <xsl:map-entry key="'topicSubjectHeader'" select="'+ map/relrow classify-d/topicSubjectHeader '"/>
      <xsl:map-entry key="'topicSubjectRow'" select="'+ map/relrow classify-d/topicSubjectRow '"/>
      <xsl:map-entry key="'topicSubjectTable'" select="'+ map/reltable classify-d/topicSubjectTable '"/>
      <xsl:map-entry key="'topicapply'" select="'+ map/topicref classify-d/topicapply '"/>
      <xsl:map-entry key="'topicset'" select="'+ map/topicref mapgroup-d/topicset '"/>
      <xsl:map-entry key="'topicsetref'" select="'+ map/topicref mapgroup-d/topicsetref '"/>
      <xsl:map-entry key="'topicsubject'" select="'+ map/topicref classify-d/topicsubject '"/>
      <xsl:map-entry key="'url'" select="'+ topic/data xnal-d/url '"/>
      <xsl:map-entry key="'urls'" select="'+ topic/data xnal-d/urls '"/>
    </xsl:map>
  </xsl:variable>

  <xsl:template match="*" mode="class" priority="-10">
    <xsl:variable name="local-name" select="local-name()" as="xs:string"/>
    <xsl:attribute name="class">
      <xsl:choose>
        <xsl:when test="$raw-dita and map:contains($classes, $local-name)">
          <xsl:value-of select="$classes($local-name)"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>- topic/</xsl:text>
          <xsl:value-of select="$local-name"/>
          <xsl:text> </xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="@* | node()" mode="class" priority="-15"/>

  <xsl:template match="*" priority="-10">
    <xsl:if test="not($raw-dita and
                        (map:contains($classes, local-name()) or
                          starts-with(name(), 'm:') or
                          starts-with(name(), 'svg:')))">
      <xsl:message>WARN: Unsupported HTML5 element '<xsl:value-of select="name()"/>'</xsl:message>
    </xsl:if>
    <!--
    <xsl:if test="name() = (
    'a',
'article',
'audio',
'b',
'body',
'caption',
'dd',
'div',
'dl',
'dt',
'em',
'figcaption',
'figure',
'h1',
'h2',
'i',
'img',
'li',
'meta',
'nav',
'ol',
'p',
'pre',
'section',
'source',
'span',
'strong',
'sub',
'sup',
'table',
'td',
'th',
'title',
'tr',
'u',
'ul',
'video'
    )">
      <xsl:message terminate="yes">FATAL: Unsupported HDITA element '<xsl:value-of select="name()"/>': <xsl:for-each select="ancestor-or-self::*"><xsl:value-of select="name()"/>/</xsl:for-each>
      </xsl:message>
    </xsl:if>
    -->
    <xsl:element name="{name()}">
      <xsl:if test="not($raw-dita and contains(name(), ':'))">
        <xsl:apply-templates select="." mode="class"/>
      </xsl:if>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="@* | node()" priority="-15">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@data-keyref">
    <xsl:attribute name="keyref" select="."/>
  </xsl:template>

  <xsl:template match="@data-conref" priority="10">
    <xsl:attribute name="conref" select="."/>
  </xsl:template>

  <xsl:template match="@data-conkeyref" priority="10">
    <xsl:attribute name="conkeyref" select="."/>
  </xsl:template>

</xsl:stylesheet>
