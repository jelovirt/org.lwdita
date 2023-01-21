package com.elovirta.dita.markdown;

import org.dita.dost.util.Constants;
import org.dita.dost.util.DitaClass;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import java.util.*;

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

    private enum ReferenceState {
        BODY,
        SECTION
    }

    private enum TaskState {
        BODY,
        CONTEXT,
        STEPS,
        RESULT
    }

    /**
     * Topic type stack. Default to topic in case of compound type
     */
    private Deque<Type> typeStack = new ArrayDeque<>(Arrays.asList(Type.TOPIC));
    private boolean inStep = false;
    private int paragraphCountInStep = 0;
    private int depth = 0;
    private boolean wrapOpen = false;
    private TaskState taskState = null;
    private ReferenceState referenceState = null;
    private boolean infoWrapOpen = false;

    private Deque<String> elementStack = new ArrayDeque<>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
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
                        taskState = TaskState.BODY;
                        renameStartElement(TASK_TASKBODY, atts);
                        break;
                    case "ol":
                        if (depth == DEPTH_IN_BODY) {
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
                        if (depth == DEPTH_IN_BODY) {
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
                        if (depth == 4) {
                            renameStartElement(TASK_STEP, atts);
                            inStep = true;
                        } else {
                            doStartElement(uri, localName, qName, atts);
                        }
                        break;
                    default:
                        if (depth == DEPTH_IN_BODY) {
                            if (taskState == TaskState.BODY) {
                                AttributesImpl sectionAtts = new AttributesImpl();
                                sectionAtts.addAttribute(NULL_NS_URI, "class", "class", "CDATA", TASK_CONTEXT.toString());
                                doStartElement(uri, TASK_CONTEXT.localName, TASK_CONTEXT.localName, sectionAtts);
                                taskState = TaskState.CONTEXT;
                            }
                            doStartElement(uri, localName, qName, atts);
                        } else if (inStep && depth == 5) {
                            switch (localName) {
                                case "p":
                                    paragraphCountInStep++;
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
                        referenceState = null;
                        renameStartElement(REFERENCE_REFERENCE, atts);
                        break;
                    case "body":
                        renameStartElement(REFERENCE_REFBODY, atts);
                        referenceState = ReferenceState.BODY;
                        break;
                    default:
                        if (depth == DEPTH_IN_BODY) {
                            switch (localName) {
                                case "table":
                                case "section":
                                    if (referenceState == ReferenceState.SECTION) {
                                        referenceState = ReferenceState.BODY;
                                        doEndElement(uri, "section", "section");
                                    }
                                    break;
                                default:
                                    if (referenceState == ReferenceState.BODY) {
                                        AttributesImpl sectionAtts = new AttributesImpl();
                                        sectionAtts.addAttribute(NULL_NS_URI, "class", "class", "CDATA", "- topic/section ");
                                        doStartElement(uri, "section", "section", sectionAtts);
                                        referenceState = ReferenceState.SECTION;
                                    }
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
        switch (typeStack.peek()) {
            case TASK:
                switch (localName) {
                    case "body":
                        if (taskState == TaskState.CONTEXT) {
                            taskState = null;
                            doEndElement(uri, TASK_CONTEXT.localName, TASK_CONTEXT.localName);
                        }
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
                        if (referenceState == ReferenceState.SECTION) {
                            referenceState = null;
                            doEndElement(uri, TOPIC_SECTION.localName, TOPIC_SECTION.localName);
                        }
                        doEndElement(uri, localName, qName);
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
//        System.out.printf("<%s>%n", localName);
        super.startElement(uri, localName, qName, atts);
        elementStack.push(localName);
    }

    public void doEndElement(String uri, String localName, String qName) throws SAXException {
        final String l = elementStack.pop();
//        System.out.printf("</%s = %s>%n", l, localName);
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

    private Collection<String> getOutputclass(Attributes atts) {
        final String outputclass = atts.getValue("outputclass");
        if (outputclass == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(outputclass.trim().split("\\s+"));
    }
}
