<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:x="https://github.com/jelovirt/dita-ot-markdown"
                exclude-result-prefixes="xs x"
                version="2.0">

  <xsl:import href="classpath:///utils.xsl"/>

  <xsl:template match="/">
    <xsl:apply-templates mode="dispatch"/>
  </xsl:template>
  
  <xsl:template match="dita" mode="dispatch">
    <dita>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates mode="dispatch"/>
    </dita>
  </xsl:template>
  <xsl:template match="topic[tokenize(@outputclass, '\s+')[. = 'concept']]" mode="dispatch">
    <xsl:apply-templates select="." mode="concept"/>
  </xsl:template>
  <xsl:template match="topic[tokenize(@outputclass, '\s+')[. = 'task']]" mode="dispatch">
    <xsl:apply-templates select="." mode="task"/>
  </xsl:template>
  <xsl:template match="topic[tokenize(@outputclass, '\s+')[. = 'reference']]" mode="dispatch">
    <xsl:apply-templates select="." mode="reference"/>
  </xsl:template>
  <xsl:template match="*" mode="dispatch">
    <xsl:apply-templates select="." mode="copy"/>
  </xsl:template>

  <!-- reference -->

  <xsl:template match="topic" mode="reference">
    <reference class="- topic/topic reference/reference "
               domains="(topic reference) (topic hi-d) (topic ut-d) (topic indexing-d) (topic hazard-d) (topic abbrev-d) (topic pr-d) (topic sw-d) (topic ui-d)">
      <xsl:apply-templates select="@* except (@class | @domains) | node()" mode="#current"/>
    </reference>
  </xsl:template>

  <xsl:template match="body" mode="reference">
    <refbody class="- topic/body reference/refbody ">
      <xsl:apply-templates select="@* except @class" mode="#current"/>
      <xsl:for-each-group select="*" group-adjacent="contains(@class, ' topic/table ') or contains(@class, ' topic/section ')">
        <xsl:choose>
          <xsl:when test="current-grouping-key()">
            <xsl:apply-templates select="current-group()" mode="#current"/>
          </xsl:when>
          <xsl:otherwise>
            <section class="- topic/section ">
              <xsl:apply-templates select="current-group()" mode="#current"/>
            </section>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each-group>
    </refbody>
  </xsl:template>

  <!-- task -->

  <xsl:template match="topic" mode="task">
    <task class="- topic/topic task/task "
          domains="(topic task) (topic hi-d) (topic ut-d) (topic indexing-d) (topic hazard-d) (topic abbrev-d) (topic pr-d) (topic sw-d) (topic ui-d) (topic task strictTaskbody-c)">
      <xsl:apply-templates select="@* except (@class | @domains) | node()" mode="#current"/>
    </task>
  </xsl:template>

  <xsl:template match="body" mode="task">
    <taskbody class="- topic/body task/taskbody ">
      <xsl:apply-templates select="@* except @class" mode="#current"/>
      <xsl:for-each-group select="*" group-adjacent="contains(@class, ' topic/ol ') or contains(@class, ' topic/ul ') or contains(@class, ' topic/section ')">
        <xsl:choose>
          <xsl:when test="current-grouping-key() and empty(preceding-sibling::*)">
            <context class="- topic/section task/context ">
              <xsl:apply-templates select="current-group()/*" mode="#current"/>
            </context>
            <!--
            <xsl:apply-templates select="current-group()" mode="#current"/>
            -->
          </xsl:when>
          <xsl:when test="current-grouping-key()">
            <xsl:apply-templates select="current-group()" mode="#current"/>
          </xsl:when>
          <xsl:when test="current-group()[1]/preceding-sibling::*[contains(@class, ' topic/ol ') or contains(@class, ' topic/ul ')]">
            <result class="- topic/section task/result ">
              <xsl:apply-templates select="current-group()" mode="#current"/>
            </result>
          </xsl:when>
          <xsl:otherwise>
            <context class="- topic/section task/context ">
              <xsl:apply-templates select="current-group()" mode="#current"/>
            </context>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each-group>
    </taskbody>
  </xsl:template>

  <xsl:template match="body/ol" mode="task">
    <steps class="- topic/ol task/steps ">
      <xsl:apply-templates select="@* except @class | node()" mode="#current"/>
    </steps>
  </xsl:template>

  <xsl:template match="body/ul" mode="task">
    <steps-unordered class="- topic/ul task/steps-unordered ">
      <xsl:apply-templates select="@* except @class | node()" mode="#current"/>
    </steps-unordered>
  </xsl:template>

  <xsl:template match="body/ol/li | body/ul/li" mode="task">
    <step class="- topic/li task/step ">
      <xsl:apply-templates select="@* except @class" mode="#current"/>

      <xsl:variable name="first-block" select="*[x:is-block(.)][1]" as="element()?"/>
      <xsl:variable name="head" select="if (exists($first-block)) then node()[. &lt;&lt; $first-block] else node()" as="node()*"/>
      <xsl:variable name="tail" select="if (exists($first-block)) then ($first-block | node()[. &gt;&gt; $first-block]) else ()" as="node()*"/>
      <xsl:choose>
        <xsl:when test="$head[self::* or normalize-space()]">
          <cmd class="- topic/ph task/cmd ">
            <xsl:copy-of select="$head"/>
          </cmd>
          <xsl:if test="*">
            <info class="- topic/itemgroup task/info ">
              <xsl:apply-templates select="$tail" mode="#current"/>
            </info>  
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="$tail[1]">
            <cmd class="- topic/ph task/cmd ">
              <xsl:apply-templates select="@* except @class | node()" mode="#current"/>
            </cmd>
          </xsl:for-each>
          <xsl:if test="$tail[position() ge 2][self::* or normalize-space()]">
            <info class="- topic/itemgroup task/info ">
              <xsl:apply-templates select="$tail[position() gt 1]" mode="#current"/>
            </info>    
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
    </step>
  </xsl:template>

  <!-- concept -->

  <xsl:template match="topic" mode="concept">
    <concept class="- topic/topic concept/concept "
             domains="(topic concept) (topic hi-d) (topic ut-d) (topic indexing-d) (topic hazard-d) (topic abbrev-d) (topic pr-d) (topic sw-d) (topic ui-d)">
      <xsl:apply-templates select="@* except (@class | @domains) | node()" mode="#current"/>
    </concept>
  </xsl:template>

  <xsl:template match="body" mode="concept">
    <conbody class="- topic/body concept/conbody ">
      <xsl:apply-templates select="@* except @class | node()" mode="#current"/>
    </conbody>
  </xsl:template>

  <!-- common -->
  
  <xsl:template match="topic/@outputclass" mode="concept task reference">
    <xsl:variable name="type" as="xs:string?" select="tokenize(., '\s+')[. = ('concept', 'task', 'reference')]"/>
    <xsl:choose>
      <xsl:when test="exists($type)">
        <xsl:variable name="classes" select="tokenize(., '\s+')[. ne $type]" as="xs:string*"/>
        <xsl:if test="exists($classes)">
          <xsl:attribute name="{name()}">
            <xsl:value-of select="$classes" separator=" "/>
          </xsl:attribute>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <xsl:next-match/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@* | node()" priority="-10" mode="concept task reference copy">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:variable name="x:is-block-classes" as="xs:string*"
    select="
      (
      ' topic/body ',
      ' topic/bodydiv ',
      ' topic/shortdesc ',
      ' topic/abstract ',
      ' topic/title ',
      ' task/info ',
      ' topic/p ',
      ' topic/pre ',
      ' topic/note ',
      ' topic/fig ',
      ' topic/figgroup ',
      ' topic/dl ',
      ' topic/sl ',
      ' topic/ol ',
      ' topic/ul ',
      ' topic/li ',
      ' topic/sli ',
      ' topic/lines ',
      ' topic/itemgroup ',
      ' topic/section ',
      ' topic/sectiondiv ',
      ' topic/div ',
      ' topic/lq ',
      ' topic/table ',
      ' topic/entry ',
      ' topic/simpletable ',
      ' topic/stentry ',
      ' topic/example ',
      ' task/cmd ')"/>

  <xsl:function name="x:is-block" as="xs:boolean">
    <xsl:param name="element" as="node()"/>
    <xsl:variable name="class" select="string($element/@class)" as="xs:string"/>
    <xsl:sequence
      select="
        some $c in $x:is-block-classes
          satisfies contains($class, $c) or
          (contains($class, ' topic/image ') and $element/@placement = 'break')"
    />
  </xsl:function>

</xsl:stylesheet>
