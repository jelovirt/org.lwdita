<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="xs"
                version="2.0">

  <xsl:variable name="type" as="xs:string?" select="tokenize(/topic/@outputclass, '\s+')[. = ('concept', 'task', 'reference')]"/>

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- reference -->

  <xsl:template match="topic[$type = 'reference']">
    <reference class="- topic/topic reference/reference "
               domains="(topic reference) (topic hi-d) (topic ut-d) (topic indexing-d) (topic hazard-d) (topic abbrev-d) (topic pr-d) (topic sw-d) (topic ui-d)">
      <xsl:apply-templates select="@* except (@class | @domains) | node()"/>
    </reference>
  </xsl:template>

  <xsl:template match="body[$type = 'reference']">
    <refbody class="- topic/body reference/refbody ">
      <xsl:apply-templates select="@* except @class"/>
      <xsl:for-each-group select="*" group-adjacent="contains(@class, ' topic/table ') or contains(@class, ' topic/section ')">
        <xsl:choose>
          <xsl:when test="current-grouping-key()">
            <xsl:apply-templates select="current-group()"/>
          </xsl:when>
          <xsl:otherwise>
            <section class="- topic/section ">
              <xsl:apply-templates select="current-group()"/>
            </section>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each-group>
    </refbody>
  </xsl:template>

  <!-- task -->

  <xsl:template match="topic[$type = 'task']">
    <task class="- topic/topic task/task "
          domains="(topic task) (topic hi-d) (topic ut-d) (topic indexing-d) (topic hazard-d) (topic abbrev-d) (topic pr-d) (topic sw-d) (topic ui-d) (topic task strictTaskbody-c)">
      <xsl:apply-templates select="@* except (@class | @domains) | node()"/>
    </task>
  </xsl:template>

  <xsl:template match="body[$type = 'task']">
    <taskbody class="- topic/body task/taskbody ">
      <xsl:apply-templates select="@* except @class"/>
      <xsl:for-each-group select="*" group-adjacent="contains(@class, ' topic/ol ') or contains(@class, ' topic/ul ') or contains(@class, ' topic/section ')">
        <xsl:choose>
          <xsl:when test="current-grouping-key()">
            <xsl:apply-templates select="current-group()"/>
          </xsl:when>
          <xsl:when test="current-group()[1]/preceding-sibling::*[contains(@class, ' topic/ol ') or contains(@class, ' topic/ul ')]">
            <result class="- topic/section task/result ">
              <xsl:apply-templates select="current-group()"/>
            </result>
          </xsl:when>
          <xsl:otherwise>
            <context class="- topic/section task/context ">
              <xsl:apply-templates select="current-group()"/>
            </context>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each-group>
    </taskbody>
  </xsl:template>

  <xsl:template match="body[$type = 'task']/ol">
    <steps class="- topic/ol task/steps ">
      <xsl:apply-templates select="@* except @class | node()"/>
    </steps>
  </xsl:template>

  <xsl:template match="body[$type = 'task']/ul">
    <steps-unordered class="- topic/ul task/steps-unordered ">
      <xsl:apply-templates select="@* except @class | node()"/>
    </steps-unordered>
  </xsl:template>

  <xsl:template match="body[$type = 'task']/ol/li | body[$type = 'task']/ul/li">
    <step class="- topic/li task/step ">
      <xsl:apply-templates select="@* except @class"/>
      <xsl:for-each select="*[1]">
        <cmd class="- topic/ph task/cmd ">
          <xsl:apply-templates select="@* except @class | node()"/>
        </cmd>
      </xsl:for-each>
      <info class="- topic/itemgroup task/info ">
        <xsl:apply-templates select="*[position() gt 1]"/>
      </info>
    </step>
  </xsl:template>

  <!-- concept -->

  <xsl:template match="topic[$type = 'concept']">
    <concept class="- topic/topic concept/concept "
             domains="(topic concept) (topic hi-d) (topic ut-d) (topic indexing-d) (topic hazard-d) (topic abbrev-d) (topic pr-d) (topic sw-d) (topic ui-d)">
      <xsl:apply-templates select="@* except (@class | @domains) | node()"/>
    </concept>
  </xsl:template>

  <xsl:template match="body[$type = 'concept']">
    <conbody class="- topic/body concept/conbody ">
      <xsl:apply-templates select="@* except @class | node()"/>
    </conbody>
  </xsl:template>

  <!-- common -->
  
  <xsl:template match="topic/@outputclass">
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

  <xsl:template match="@* | node()" priority="-10">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
