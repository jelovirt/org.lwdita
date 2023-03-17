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

    public enum Type {
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
        STEP,
        INFO,
        RESULT
    }

    private final Type forceType;

    /**
     * Topic type stack. Default to topic in case of compound type
     */
    private Deque<Type> typeStack = new ArrayDeque<>(Arrays.asList(Type.TOPIC));
    private int paragraphCountInStep = 0;
    private int depth = 0;
    private TaskState taskState = null;
    private ReferenceState referenceState = null;

    private Deque<String> elementStack = new ArrayDeque<>();

    public SpecializeFilter() {
        this(null);
    }

    public SpecializeFilter(Type forceType) {
        super();
        this.forceType = forceType;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        depth++;

        if (localName.equals(TOPIC_TOPIC.localName)) {
            depth = 1;
            if (forceType != null) {
                typeStack.push(forceType);
            } else {
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
        }

        switch (typeStack.peek()) {
            case CONCEPT:
                startElementConcept(uri, localName, qName, atts);
                break;
            case TASK:
                startElementTask(uri, localName, qName, atts);
                break;
            case REFERENCE:
                startElementReference(uri, localName, qName, atts);
                break;
            default:
                doStartElement(uri, localName, qName, atts);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (typeStack.peek()) {
            case TASK:
                endElementTask(uri, localName, qName);
                break;
            case REFERENCE:
                endElementReference(uri, localName, qName);
                break;
            case CONCEPT:
                endElementConcept(uri, localName, qName);
                break;
            default:
                doEndElement(uri, localName, qName);
        }

        if (localName.equals(TOPIC_TOPIC.localName)) {
            typeStack.pop();
        }

        depth--;
    }

    private void startElementConcept(String uri, String localName, String qName, Attributes atts) throws SAXException {
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
    }

    private void endElementConcept(String uri, String localName, String qName) throws SAXException {
        doEndElement(uri, localName, qName);
    }

    private void startElementTask(String uri, String localName, String qName, Attributes atts) throws SAXException {
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
                if (taskState == TaskState.STEPS && depth == 4) {
                    renameStartElement(TASK_STEP, atts);
                    taskState = TaskState.STEP;
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
                } else if ((taskState == TaskState.STEP || taskState == TaskState.INFO) && depth == 5) {
                    switch (localName) {
                        case "p":
                            paragraphCountInStep++;
                            if (paragraphCountInStep == 1) {
                                renameStartElement(TASK_CMD, atts);
                            } else if (paragraphCountInStep == 2 && taskState != TaskState.INFO) {
                                AttributesImpl res = new AttributesImpl(atts);
                                res.addAttribute(NULL_NS_URI, "class", "class", "CDATA", TASK_INFO.toString());
                                doStartElement(NULL_NS_URI, TASK_INFO.localName, TASK_INFO.localName, res);
                                taskState = TaskState.INFO;
                                doStartElement(uri, localName, qName, atts);
                            } else {
                                doStartElement(uri, localName, qName, atts);
                            }
                            break;
                        default:
                            if (taskState != TaskState.INFO) {
                                AttributesImpl res = new AttributesImpl(atts);
                                res.addAttribute(NULL_NS_URI, "class", "class", "CDATA", TASK_INFO.toString());
                                doStartElement(NULL_NS_URI, TASK_INFO.localName, TASK_INFO.localName, res);
                                taskState = TaskState.INFO;
                            }
                            doStartElement(uri, localName, qName, atts);
                            break;
                    }
                } else {
                    doStartElement(uri, localName, qName, atts);
                }
        }
    }

    private void endElementTask(String uri, String localName, String qName) throws SAXException {
        switch (localName) {
            case "body":
                if (taskState == TaskState.CONTEXT) {
                    taskState = null;
                    doEndElement(uri, TASK_CONTEXT.localName, TASK_CONTEXT.localName);
                }
                doEndElement(uri, localName, qName);
                break;
            case "li":
                if (taskState == TaskState.INFO && depth == 4) {
                    doEndElement(NULL_NS_URI, TASK_INFO.localName, TASK_INFO.localName);
                    taskState = TaskState.STEP;
                }
                if (taskState == TaskState.STEP && depth == 4) {
                    paragraphCountInStep = 0;
                    taskState = TaskState.STEPS;
                }
                doEndElement(uri, localName, qName);
                break;
            default:
                doEndElement(uri, localName, qName);
        }
    }

    private void startElementReference(String uri, String localName, String qName, Attributes atts) throws SAXException {
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
    }

    private void endElementReference(String uri, String localName, String qName) throws SAXException {
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
