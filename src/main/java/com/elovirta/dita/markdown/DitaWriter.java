package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.renderer.AttributablePart;
import com.elovirta.dita.markdown.renderer.NodeRendererContext;
import org.dita.dost.util.DitaClass;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.ArrayDeque;
import java.util.Deque;

import static javax.xml.XMLConstants.NULL_NS_URI;

public class DitaWriter {
    private NodeRendererContext context;
    private AttributablePart useAttributes;

    public DitaWriter(ContentHandler out) {
        this.contentHandler = out;
    }

    void setContext(NodeRendererContext context) {
        this.context = context;
    }

    public NodeRendererContext getContext() {
        return context;
    }
//
//    public DitaWriter srcPos() {
//        return srcPos(context.getCurrentNode().getChars());
//    }
//
//    public DitaWriter srcPosWithEOL() {
//        return srcPosWithEOL(context.getCurrentNode().getChars());
//    }
//
//    public DitaWriter srcPosWithTrailingEOL() {
//        return srcPosWithTrailingEOL(context.getCurrentNode().getChars());
//    }
//
//    public DitaWriter srcPos(BasedSequence sourceText) {
//        if (sourceText.isNotNull()) {
//            BasedSequence trimmed = sourceText.trimEOL();
//            return srcPos(trimmed.getStartOffset(), trimmed.getEndOffset());
//        }
//        return this;
//    }
//
//    @SuppressWarnings("WeakerAccess")
//    public DitaWriter srcPosWithEOL(BasedSequence sourceText) {
//        if (sourceText.isNotNull()) {
//            return srcPos(sourceText.getStartOffset(), sourceText.getEndOffset());
//        }
//        return this;
//    }
//
//    @SuppressWarnings("WeakerAccess")
//    public DitaWriter srcPosWithTrailingEOL(BasedSequence sourceText) {
//        if (sourceText.isNotNull()) {
//            int endOffset = sourceText.getEndOffset();
//            BasedSequence base = sourceText.getBaseSequence();
//
//            while (endOffset < base.length()) {
//                char c = base.charAt(endOffset);
//                if (c != ' ' && c != '\t') break;
//                endOffset++;
//            }
//
//            if (endOffset < base.length() && base.charAt(endOffset) == '\r') {
//                endOffset++;
//            }
//
//            if (endOffset < base.length() && base.charAt(endOffset) == '\n') {
//                endOffset++;
//            }
//            return srcPos(sourceText.getStartOffset(), endOffset);
//        }
//        return this;
//    }
//
//    public DitaWriter srcPos(int startOffset, int endOffset) {
//        if (startOffset <= endOffset && !context.getDitaOptions().sourcePositionAttribute.isEmpty()) {
//            super.attr(context.getDitaOptions().sourcePositionAttribute, startOffset + "-" + endOffset);
//        }
//        return this;
//    }
//
//    public DitaWriter withAttr() {
//        return withAttr(AttributablePart.NODE);
//    }
//
//    public DitaWriter withAttr(AttributablePart part) {
//        super.withAttr();
//        useAttributes = part;
//        return this;
//    }
//
//    public DitaWriter withAttr(LinkStatus status) {
//        attr(Attribute.LINK_STATUS_ATTR, status.getName());
//        return withAttr(AttributablePart.LINK);
//    }
//
//    public DitaWriter withAttr(ResolvedLink resolvedLink) {
//        return withAttr(resolvedLink.getStatus());
//    }
//
//    @Override
//    public DitaWriter tag(CharSequence tagName, boolean voidElement) {
//        if (useAttributes != null) {
//            final Attributes attributes = context.extendRenderingNodeAttributes(useAttributes, getAttributes());
//            String sourcePositionAttribute = context.getDitaOptions().sourcePositionAttribute;
//            String attributeValue = attributes.getValue(sourcePositionAttribute);
//
//            if (!attributeValue.isEmpty()) {
//                // add to tag ranges
//                int pos = attributeValue.indexOf('-');
//                int startOffset = -1;
//                int endOffset = -1;
//
//                if (pos != -1) {
//                    try {
//                        startOffset = Integer.valueOf(attributeValue.substring(0, pos));
//                    } catch (Throwable ignored) {
//
//                    }
//                    try {
//                        endOffset = Integer.valueOf(attributeValue.substring(pos + 1));
//                    } catch (Throwable ignored) {
//
//                    }
//                }
//
//                if (startOffset >= 0 && startOffset < endOffset) {
//                    ArrayList<TagRange> tagRanges = context.getDocument().get(DitaRenderer.TAG_RANGES);
//                    tagRanges.add(new TagRange(tagName, startOffset, endOffset));
//                }
//            }
//
//            setAttributes(attributes);
//            useAttributes = null;
//        }
//
//        super.tag(tagName, voidElement);
//        return this;
//    }

    final Deque<DitaClass> tagStack = new ArrayDeque<>();
    protected ContentHandler contentHandler;

    public void setContentHandler(final ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    // ContentHandler methods

    public void startElement(final DitaClass tag, final org.xml.sax.Attributes atts) {
        try {
            contentHandler.startElement(NULL_NS_URI, tag.localName, tag.localName, atts);
        } catch (final SAXException e) {
            throw new ParseException(e);
        }
        tagStack.addFirst(tag);
    }

    public void endElement() {
        if (!tagStack.isEmpty()) {
            endElement(tagStack.removeFirst());
        }
    }


    public void endElement(final DitaClass tag) {
        try {
            contentHandler.endElement(NULL_NS_URI, tag.localName, tag.localName);
        } catch (final SAXException e) {
            throw new ParseException(e);
        }
    }

    public void characters(final char c) {
        try {
            contentHandler.characters(new char[]{c}, 0, 1);
        } catch (final SAXException e) {
            throw new ParseException(e);
        }
    }

    public void characters(final String t) {
        final char[] cs = t.toCharArray();
        try {
            contentHandler.characters(cs, 0, cs.length);
        } catch (final SAXException e) {
            throw new ParseException(e);
        }
    }
}
