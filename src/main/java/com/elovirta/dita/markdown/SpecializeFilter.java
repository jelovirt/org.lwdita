package com.elovirta.dita.markdown;

import com.google.common.base.Strings;
import org.dita.dost.util.Constants;
import org.dita.dost.util.DitaClass;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.dita.dost.util.Constants.*;

public class SpecializeFilter extends XMLFilterImpl {

    private static int DEPTH_IN_BODY = 3;

    private enum Type {
        TOPIC,
        CONCEPT,
        TASK,
        REFERENCE
    }

    private enum TaskState {
        CONTEXT,
        STEPS,
        RESULT
    }

    /** Topic type stack. Default to topic in case of compound type */
    private Deque<Type> typeStack = new ArrayDeque<>(Arrays.asList(Type.TOPIC));
//    private Type type = Type.TOPIC;
    private boolean inBody = false;
    private boolean inStep = false;
    private int paragraphCountInStep = 0;
    private int depth = 0;
    private boolean wrapOpen = false;
    private TaskState taskState = null;
//    private boolean contextWrapOpen = false;
    private boolean infoWrapOpen = false;

    private Deque<String> elementStack = new ArrayDeque<>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
//        System.err.println("#" + localName + " infoWrapOpen="+infoWrapOpen);
        depth++;

        if (localName.equals(TOPIC_TOPIC.localName)) {
            depth = 1;
            final Collection<String> outputclasses = getOutputclass(atts);
            if (outputclasses.contains(CONCEPT_CONCEPT.localName)) {
                typeStack.push(Type.CONCEPT);
            } else if (outputclasses.contains(TASK_TASK.localName)) {
                typeStack.push(Type.TASK);
            } else if (outputclasses.contains(REFERENCE_REFERENCE.localName)) {
                typeStack.push(Type.REFERENCE);
            } else {
                typeStack.push(typeStack.peek());
            }
        }

        switch (typeStack.peek()) {
            case CONCEPT:
                switch (localName) {
                    case "topic":
                        renameStartElement(Constants.CONCEPT_CONCEPT, atts);
                        break;
                    case "body":
                        renameStartElement(Constants.CONCEPT_CONBODY, atts);
                        break;
                    default:
                        doStartElement(uri, localName, qName, atts);
                }
                break;
            case TASK:
                switch (localName) {
                    case "topic":
                        renameStartElement(TASK_TASK, atts);
                        taskState = null;
                        break;
                    case "body":
                        inBody = true;
                        renameStartElement(TASK_TASKBODY, atts);
                        break;
                    case "ol":
                        if (inBody && depth == DEPTH_IN_BODY) {
                            if (taskState == TaskState.CONTEXT) {
                                doEndElement(uri, TASK_CONTEXT.localName, TASK_CONTEXT.localName);
                            }
                            taskState = TaskState.STEPS;
                            renameStartElement(Constants.TASK_STEPS, atts);
                        } else {
                            doStartElement(uri, localName, qName, atts);
                        }
                        break;
                    case "ul":
                        if (inBody && depth == DEPTH_IN_BODY) {
                            if (taskState == TaskState.CONTEXT) {
                                doEndElement(uri, TASK_CONTEXT.localName, TASK_CONTEXT.localName);
                            }
                            taskState = TaskState.STEPS;
                            renameStartElement(TASK_STEPS_UNORDERED, atts);
                        } else {
                            doStartElement(uri, localName, qName, atts);
                        }
                        break;
                    case "li":
                        if (inBody && depth == 4) {
                            renameStartElement(TASK_STEP, atts);
                            inStep = true;
                        } else {
                            doStartElement(uri, localName, qName, atts);
                        }
                        break;
                    default:
                        if (inBody && depth == DEPTH_IN_BODY) {
//                            switch (localName) {
//                                case "ol":
//                                case "ul":
//                                    if (true) throw new RuntimeException();
//                                    // No need to wrap
//                                    if (taskState == TaskState.CONTEXT) {
//                                        taskState = TaskState.STEPS;
//                                        doEndElement(uri, TASK_CONTEXT.localName, TASK_CONTEXT.localName);
//                                    }
//                                    break;
//                                default:
                            if (taskState == null) {
                                AttributesImpl sectionAtts = new AttributesImpl();
                                sectionAtts.addAttribute(NULL_NS_URI, "class", "class", "CDATA", TASK_CONTEXT.toString());
                                doStartElement(uri, TASK_CONTEXT.localName, TASK_CONTEXT.localName, sectionAtts);
                                taskState = TaskState.CONTEXT;
                            }
//                                    break;
//                            }
                            doStartElement(uri, localName, qName, atts);
                        } else if (inStep && depth == 5) {
                            switch (localName) {
                                case "p":
                                    paragraphCountInStep++;
//                                    System.out.printf("p %d in step%n", paragraphCountInStep);
                                    if (paragraphCountInStep == 1) {
                                        renameStartElement(TASK_CMD, atts);
                                    } else if (paragraphCountInStep == 2 && !infoWrapOpen) {
                                        AttributesImpl res = new AttributesImpl(atts);
                                        res.addAttribute(NULL_NS_URI, "class", "class", "CDATA", TASK_INFO.toString());
                                        doStartElement(NULL_NS_URI, TASK_INFO.localName, TASK_INFO.localName, res);
                                        infoWrapOpen = true;
                                        doStartElement(uri, localName, qName, atts);
                                    } else {
                                        doStartElement(uri, localName, qName, atts);
                                    }
                                    break;
                                default:
                                    if (!infoWrapOpen) {
                                        AttributesImpl res = new AttributesImpl(atts);
                                        res.addAttribute(NULL_NS_URI, "class", "class", "CDATA", TASK_INFO.toString());
                                        doStartElement(NULL_NS_URI, TASK_INFO.localName, TASK_INFO.localName, res);
                                        infoWrapOpen = true;
//                                        doStartElement(uri, localName, qName, atts);
                                    }
                                    doStartElement(uri, localName, qName, atts);
                                    break;
                            }
                        } else {
                            doStartElement(uri, localName, qName, atts);
                        }
                }
                break;
            case REFERENCE:
                switch (localName) {
                    case "topic":
                        renameStartElement(REFERENCE_REFERENCE, atts);
                        break;
                    case "body":
                        inBody = true;
                        renameStartElement(REFERENCE_REFBODY, atts);
                        break;
                    default:
                        if (inBody && depth == DEPTH_IN_BODY) {
                            switch (localName) {
                                case "table":
                                case "section":
                                    if (wrapOpen) {
                                        wrapOpen = false;
                                        doEndElement(uri, "section", "section");
                                    }
                                    break;
                                default:
                                    wrapOpen = true;
                                    AttributesImpl sectionAtts = new AttributesImpl();
                                    sectionAtts.addAttribute(NULL_NS_URI, "class", "class", "CDATA", "- topic/section ");
                                    doStartElement(uri, "section", "section", sectionAtts);
                                    break;
                            }
                            doStartElement(uri, localName, qName, atts);
                        } else {
                            doStartElement(uri, localName, qName, atts);
                        }
                }
                break;
            default:
                doStartElement(uri, localName, qName, atts);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
//        System.err.println("#/" + localName);
        switch (typeStack.peek()) {
            case TASK:
                switch (localName) {
                    case "body":
                        if (taskState == TaskState.CONTEXT) {
                            taskState = null;
                            doEndElement(uri, TASK_CONTEXT.localName, TASK_CONTEXT.localName);
                        }
                        inBody = false;
                        doEndElement(uri, localName, qName);
                        break;
                    case "li":
                        if (inStep && depth == 4) {
                            inStep = false;
                            paragraphCountInStep = 0;
                            if (infoWrapOpen) {
                                doEndElement(NULL_NS_URI, TASK_INFO.localName, TASK_INFO.localName);
                                infoWrapOpen = false;
                            }
                        }
                        doEndElement(uri, localName, qName);
                        break;
                    default:
                        doEndElement(uri, localName, qName);
                }
                break;
            case REFERENCE:
                switch (localName) {
                    case "body":
                        if (wrapOpen) {
                            wrapOpen = false;
                            doEndElement(uri, TOPIC_SECTION.localName, TOPIC_SECTION.localName);
                        }
                        doEndElement(uri, localName, qName);
                        inBody = false;
                        break;
                    default:
                        doEndElement(uri, localName, qName);
                }
                break;
            case CONCEPT:
            default:
                doEndElement(uri, localName, qName);
        }

        if (localName.equals(TOPIC_TOPIC.localName)) {
            typeStack.pop();
        }

        depth--;
    }

    public void doStartElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
//        System.out.printf("%s<%s>%n", Strings.repeat(" ", depth), localName);
        super.startElement(uri, localName, qName, atts);
        elementStack.push(localName);
    }

    public void doEndElement(String uri, String localName, String qName) throws SAXException {
        final String l = elementStack.pop();
//        System.out.printf("%s</%s = %s>%n", Strings.repeat(" ", depth), l, localName);
        super.endElement(uri, l, l);
    }

    private void renameStartElement(DitaClass cls, Attributes atts) throws SAXException {
        AttributesImpl res = new AttributesImpl(atts);
        res.addAttribute(NULL_NS_URI, "class", "class", "CDATA", cls.toString());
        final int i = res.getIndex(NULL_NS_URI, "outputclass");
        if (i != -1) {
            res.removeAttribute(i);
        }
        doStartElement(NULL_NS_URI, cls.localName, cls.localName, res);
    }

    private void renameEndElement(DitaClass cls) throws SAXException {
        doEndElement(NULL_NS_URI, cls.localName, cls.localName);
    }

    private Collection<String> getOutputclass(Attributes atts) {
        final String outputclass = atts.getValue("outputclass");
        if (outputclass == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(outputclass.trim().split("\\s+"));
    }

/*
# reference
body/* -> wrap everything not table or section into section

# concept
topic -> concept
body -> conbody

# task
topic -> task
body -> taskbody
body/ol -> steps
body/ul -> steps-unordered

  <xsl:template match="body" mode="task">
    <taskbody class="- topic/body task/taskbody ">
      <xsl:apply-templates select="@* except @class" mode="#current"/>
      <xsl:for-each-group select="*" group-adjacent="contains(@class, ' topic/ol ') or contains(@class, ' topic/ul ') or contains(@class, ' topic/section ')">
        <xsl:choose>
          <xsl:when test="current-grouping-key() and empty(preceding-sibling::*)">
            <context class="- topic/section task/context ">
              <xsl:apply-templates select="current-group()/*" mode="#current"/>
            </context>
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
 */
}
