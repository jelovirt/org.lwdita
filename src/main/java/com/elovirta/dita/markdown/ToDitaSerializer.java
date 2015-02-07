/**
 * Based on ToHtmlSerializer (C) 2010-2011 Mathias Doenitz
 */
package com.elovirta.dita.markdown;

import org.apache.commons.io.FilenameUtils;
import org.dita.dost.util.DitaClass;
import org.parboiled.common.StringUtils;
import org.pegdown.DefaultVerbatimSerializer;
import org.pegdown.LinkRenderer;
import org.pegdown.Printer;
import org.pegdown.VerbatimSerializer;
import org.pegdown.ast.*;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;

import static javax.xml.XMLConstants.*;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.toURI;
import static org.dita.dost.util.XMLUtils.AttributesBuilder;
import static org.parboiled.common.Preconditions.checkArgNotNull;

public class ToDitaSerializer implements Visitor {

    protected final ContentHandler contentHandler;
    protected Printer printer = new Printer();
    protected final Map<String, ReferenceNode> references = new HashMap<String, ReferenceNode>();
    protected final Map<String, String> abbreviations = new HashMap<String, String>();
//    protected final LinkRenderer linkRenderer;
    protected final List<ToDitaSerializerPlugin> plugins;

    protected TableNode currentTableNode;
    protected int currentTableColumn;
    protected boolean inTableHeader;

    protected final Map<String, VerbatimSerializer> verbatimSerializers;

    private final Deque<DitaClass> tagStack = new ArrayDeque<DitaClass>();
    private int elementCounter = 0;

    public ToDitaSerializer(final ContentHandler contentHandler, LinkRenderer linkRenderer) {
        this(contentHandler, linkRenderer, Collections.<ToDitaSerializerPlugin>emptyList());
    }

    private ToDitaSerializer(final ContentHandler contentHandler, LinkRenderer linkRenderer, List<ToDitaSerializerPlugin> plugins) {
        this.contentHandler = contentHandler;
//        this.linkRenderer = linkRenderer;
        this.plugins = plugins;
        this.verbatimSerializers = Collections.<String, VerbatimSerializer>singletonMap(VerbatimSerializer.DEFAULT, DefaultVerbatimSerializer.INSTANCE);
    }

    /*
    public ToDitaSerializer(final LinkRenderer linkRenderer, final Map<String, VerbatimSerializer> verbatimSerializers) {
        this(linkRenderer, verbatimSerializers, Collections.<ToDitaSerializerPlugin>emptyList());
    }

    public ToDitaSerializer(final LinkRenderer linkRenderer, final Map<String, VerbatimSerializer> verbatimSerializers, final List<ToDitaSerializerPlugin> plugins) {
        this.linkRenderer = linkRenderer;
        this.verbatimSerializers = new HashMap<String, VerbatimSerializer>(verbatimSerializers);
        if(!this.verbatimSerializers.containsKey(VerbatimSerializer.DEFAULT)) {
            this.verbatimSerializers.put(VerbatimSerializer.DEFAULT, DefaultVerbatimSerializer.INSTANCE);
        }
        this.plugins = plugins;
    }
    */

    private static final Attributes TOPIC_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_TOPIC.toString())
            .add(DITA_NAMESPACE, ATTRIBUTE_NAME_DITAARCHVERSION, ATTRIBUTE_PREFIX_DITAARCHVERSION + ":" + ATTRIBUTE_NAME_DITAARCHVERSION, "CDATA", "1.2")
            .add(ATTRIBUTE_NAME_DOMAINS, "(topic hi-d) (topic ut-d) (topic indexing-d) (topic hazard-d) (topic abbrev-d) (topic pr-d) (topic sw-d) (topic ui-d)")
            .build();
    private static final Attributes BODY_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_BODY.toString()).build();
    private static final Attributes SECTION_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_SECTION.toString()).build();
    private static final Attributes LI_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_LI.toString()).build();
    private static final Attributes P_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_P.toString()).build();
    private static final Attributes I_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, HI_D_I.toString()).build();
    private static final Attributes B_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, HI_D_B.toString()).build();
    private static final Attributes DD_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_DD.toString()).build();
    private static final Attributes CODEPH_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, PR_D_CODEPH.toString()).build();
    private static final Attributes CODEBLOCK_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, PR_D_CODEBLOCK.toString()).build();
    private static final Attributes DT_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_DT.toString()).build();
    private static final Attributes DEL_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_PH.toString()).add("importance", "deleted").build();
    private static final Attributes TITLE_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_TITLE.toString()).build();
    private static final Attributes BLOCKQUOTE_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_PRE.toString()).build();
    private static final Attributes UL_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_UL.toString()).build();
    private static final Attributes DL_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_DL.toString()).build();
    private static final Attributes OL_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_OL.toString()).build();
    private static final Attributes TABLE_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_TABLE.toString()).build();
    private static final Attributes TBODY_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_TBODY.toString()).build();
    private static final Attributes THEAD_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_THEAD.toString()).build();
    private static final Attributes TR_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_ROW.toString()).build();
    private static final Attributes IMAGE_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_IMAGE.toString()).build();
    private static final Attributes XREF_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_XREF.toString()).build();
    private static final Attributes ALT_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_ALT.toString()).build();
    private static final Attributes PH_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_PH.toString()).build();
    private static final Attributes ENTRY_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_ENTRY.toString()).build();

    public void toHtml(final RootNode astRoot) throws SAXException {
        checkArgNotNull(astRoot, "astRoot");
        contentHandler.startDocument();
        contentHandler.startPrefixMapping(ATTRIBUTE_PREFIX_DITAARCHVERSION, DITA_NAMESPACE);
        startElement(TOPIC_TOPIC, new AttributesBuilder(TOPIC_ATTS).add("id", "x").build());
        try {
            astRoot.accept(this);
            while (!tagStack.isEmpty()) {
                endElement();
            }
        } catch (final ParseException e) {
            throw new SAXException("Failed to parse Markdown: " + e.getMessage(), e);
        }
        contentHandler.endPrefixMapping(ATTRIBUTE_PREFIX_DITAARCHVERSION);
        contentHandler.endDocument();
//        return printer.getString();
    }

    // Visitor methods

    private void toString(final Node node, final StringBuilder buf) {
        if (node instanceof SuperNode) {
            for (final Node n: node.getChildren()) {
                toString(n, buf);
            }
        } else if (node instanceof TextNode) {
            buf.append(((TextNode) node).getText());
            for (final Node n: node.getChildren()) {
                toString(n, buf);
            }
        } else if (node instanceof SimpleNode) {
            buf.append(toString((SimpleNode) node));
        } else {
            throw new ParseException("Trying to process " + node);
        }
    }

    private String toString(final Node node) {
        final StringBuilder buf = new StringBuilder();
        toString(node, buf);
        return buf.toString();
    }

    @Override
    public void visit(final RootNode node) {
        for (final ReferenceNode refNode : node.getReferences()) {
            references.put(normalize(toString(refNode)), refNode);
        }
//        for (final AbbreviationNode abbrNode : node.getAbbreviations()) {
//            visitChildren(abbrNode);
//            String abbr = printer.getString();
//            printer.clear();
//            abbrNode.getExpansion().accept(this);
//            String expansion = printer.getString();
//            abbreviations.put(abbr, expansion);
//            printer.clear();
//        }
        visitChildren(node);
    }

    @Override
    public void visit(final AbbreviationNode node) {
    }

    @Override
    public void visit(final AutoLinkNode node) {
        final AttributesBuilder atts = getLinkAttributes(node.getText());

        startElement(TOPIC_XREF, atts.build());
        visitChildren(node);
        endElement();
    }

    @Override
    public void visit(final BlockQuoteNode node) {
        printTag(node, TOPIC_PRE, BLOCKQUOTE_ATTS);
    }

    @Override
    public void visit(final BulletListNode node) {
        printTag(node, TOPIC_UL, UL_ATTS);
    }

    @Override
    public void visit(final CodeNode node) {
        printTag(node, PR_D_CODEPH, CODEPH_ATTS);
    }

    @Override
    public void visit(final DefinitionListNode node) {
        printTag(node, TOPIC_DL, DL_ATTS);
    }

    @Override
    public void visit(final DefinitionNode node) {
        printTag(node, TOPIC_DD, DD_ATTS);
    }

    @Override
    public void visit(final DefinitionTermNode node) {
        printTag(node, TOPIC_DT, DT_ATTS);
    }

    @Override
    public void visit(final ExpImageNode node) {
        final AttributesBuilder atts = getLinkAttributes(node.url);
        atts.add("title", node.title);

        startElement(TOPIC_IMAGE, atts.build());
        visitChildren(node);
        endElement();
    }

    @Override
    public void visit(final ExpLinkNode node) {
        final AttributesBuilder atts = getLinkAttributes(node.url);
        //atts.add("title", node.title);

        startElement(TOPIC_XREF, atts.build());
        visitChildren(node);
        endElement();
    }

    private boolean inSection = false;

    @Override
    public void visit(final HeaderNode node) {
        if (node.getLevel() == 1) {
            printTag(node, TOPIC_TITLE, TITLE_ATTS);
            startElement(TOPIC_BODY, BODY_ATTS);
        } else {
            if (inSection) {
                endElement();
                inSection = false;
            }
            startElement(TOPIC_SECTION, SECTION_ATTS);
            inSection = true;
            printTag(node, TOPIC_TITLE, TITLE_ATTS);
        }
    }

    @Override
    public void visit(final HtmlBlockNode node) {
        String text = node.getText();
//        if (text.length() > 0) {
//            printer.println();
//        }
        characters(text);
    }

    @Override
    public void visit(final InlineHtmlNode node) {
        characters(node.getText());
    }

    @Override
    public void visit(final ListItemNode node) {
        printTag(node, TOPIC_LI, LI_ATTS);
    }

    @Override
    public void visit(final MailLinkNode node) {
        final AttributesBuilder atts = getLinkAttributes(node.getText());

        startElement(TOPIC_XREF, atts.build());
        visitChildren(node);
        endElement();
    }

    @Override
    public void visit(final OrderedListNode node) {
        printTag(node, TOPIC_OL, OL_ATTS);
    }

    @Override
    public void visit(final ParaNode node) {
        printTag(node, TOPIC_P, P_ATTS);
    }

    @Override
    public void visit(final QuotedNode node) {
        switch (node.getType()) {
            case DoubleAngle:
                characters('\u00AB');//&laquo;
                visitChildren(node);
                characters('\u00AB');//&laquo;
                break;
            case Double:
                characters('\u201C');//"&ldquo;"
                visitChildren(node);
                characters('\u201C');//"&ldquo;"
                break;
            case Single:
                characters('\u2018');//"&lsquo;"
                visitChildren(node);
                characters('\u2018');//"&lsquo;"
                break;
        }
    }

    @Override
    public void visit(final ReferenceNode node) {
        // reference nodes are not printed
    }

    @Override
    public void visit(final RefImageNode node) {
        final String text = toString(node);
        final String key = node.referenceKey != null ? toString(node.referenceKey) : text;
        final ReferenceNode refNode = references.get(normalize(key));
        if (refNode == null) { // "fake" reference image link
            // TODO
//            throw new UnsupportedOperationException();
//            printer.print("![").print(text).print(']');
//            if (node.separatorSpace != null) {
//                printer.print(node.separatorSpace).print('[');
//                if (node.referenceKey != null) printer.print(key);
//                printer.print(']');
//            }
        } else {
//            LinkRenderer.Rendering rendering = linkRenderer.render(node, refNode.getUrl(), refNode.getTitle(), text);
            final AttributesBuilder atts = new AttributesBuilder(IMAGE_ATTS)
                    .add(ATTRIBUTE_NAME_HREF, refNode.getUrl());
//            for (LinkRenderer.Attribute attr : rendering.attributes) {
//                atts.add(attr.name, attr.value);
//            }
            startElement(TOPIC_IMAGE, atts.build());
            startElement(TOPIC_ALT, ALT_ATTS);
            //characters(refNode.getTitle());
            visitChildren(refNode);
            endElement();
            endElement();
        }
    }

    @Override
    public void visit(final RefLinkNode node) {
        final String text = toString(node);
        final String key = node.referenceKey != null ? toString(node.referenceKey) : text;
        final ReferenceNode refNode = references.get(normalize(key));
        if (refNode == null) { // "fake" reference link
            final AttributesBuilder atts = new AttributesBuilder(XREF_ATTS)
                    .add(ATTRIBUTE_NAME_KEYREF, key);
            startElement(TOPIC_XREF, atts.build());
            if (node.referenceKey != null) {
                visitChildren(node);
            }
            endElement();
        } else {
            final AttributesBuilder atts = getLinkAttributes(refNode.getUrl());
            startElement(TOPIC_XREF, atts.build());
            visitChildren(node);
            endElement();
        }
    }

    @Override
    public void visit(final SimpleNode node) {
        switch (node.getType()) {
            case Apostrophe:
                characters('\u2019');//"&rsquo;"
                break;
            case Ellipsis:
                characters('\u2026');//"&hellip;"
                break;
            case Emdash:
                characters('\u2014');//"&mdash;"
                break;
            case Endash:
                characters('\u2013');//"&ndash;"
                break;
            case HRule:
//                startElement(TOPIC_PH, new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, "+ html-d/hr ").build());
//                endElement();
                characters('\n');
                break;
            case Linebreak:
//                startElement(TOPIC_PH, new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, "+ html-d/br ").build());
//                endElement();
                characters('\n');
                break;
            case Nbsp:
                characters("\u00A0");//&nbsp;
                break;
            default:
                throw new ParseException("Unsupported simple node: " + node.getType());
        }
    }

    private String toString(final SimpleNode node) {
        switch (node.getType()) {
            case Apostrophe:
                return "\u2019";//"&rsquo;"
            case Ellipsis:
                return "\u2026";//"&hellip;"
            case Emdash:
                return "\u2014'";//"&mdash;"
            case Endash:
                return "\u2013";//"&ndash;"
            case Linebreak:
            case HRule:
                return "\n";
            case Nbsp:
                return "\u00A0";//&nbsp;
            default:
                throw new ParseException("Unsupported simple node: " + node.getType());
        }
    }


    @Override
    public void visit(final StrongEmphSuperNode node) {
        if (node.isClosed()) {
            if (node.isStrong()) {
                printTag(node, HI_D_B, B_ATTS);
            } else {
                printTag(node, HI_D_I, I_ATTS);
            }
        } else {
            //sequence was not closed, treat open chars as ordinary chars
            characters(node.getChars());
            visitChildren(node);
        }
    }

    @Override
    public void visit(final StrikeNode node) {
        printTag(node, TOPIC_PH, DEL_ATTS);
    }

    @Override
    public void visit(final TableBodyNode node) {
        printTag(node, TOPIC_TBODY, TBODY_ATTS);
    }

    @Override
    public void visit(final TableCaptionNode node) {
        startElement(TOPIC_TITLE, TITLE_ATTS);
        visitChildren(node);
        endElement();
    }

    @Override
    public void visit(final TableCellNode node) {
        final List<TableColumnNode> columns = currentTableNode.getColumns();
        final TableColumnNode column = columns.get(Math.min(currentTableColumn, columns.size()-1));

        final AttributesBuilder atts = new AttributesBuilder(ENTRY_ATTS);
        column.accept(this);
        if (tableColumnAlignment != null) {
            atts.add("align", tableColumnAlignment);
            tableColumnAlignment = null;
        }
        if (node.getColSpan() > 1) {
            atts.add("colspan", Integer.toString(node.getColSpan()));
        }
        startElement(TOPIC_ENTRY, atts.build());
        visitChildren(node);
        endElement();

        currentTableColumn += node.getColSpan();
    }

    private String tableColumnAlignment = null;

    @Override
    public void visit(final TableColumnNode node) {
        switch (node.getAlignment()) {
            case None:
                break;
            case Left:
                tableColumnAlignment = "left";
//                printer.print(" align=\"left\"");
                break;
            case Right:
                tableColumnAlignment = "right";
//                printer.print(" align=\"right\"");
                break;
            case Center:
                tableColumnAlignment = "center";
//                printer.print(" align=\"center\"");
                break;
            default:
                throw new ParseException("Unsupported table column alignment: " + node.getAlignment());
        }
    }

    @Override
    public void visit(final TableHeaderNode node) {
        inTableHeader = true;
        printTag(node, TOPIC_THEAD, THEAD_ATTS);
        inTableHeader = false;
    }

    @Override
    public void visit(final TableNode node) {
        currentTableNode = node;
        printTag(node, TOPIC_TABLE, TABLE_ATTS);
        currentTableNode = null;
    }

    @Override
    public void visit(final TableRowNode node) {
        currentTableColumn = 0;
        printTag(node, TOPIC_ROW, TR_ATTS);
    }

    @Override
    public void visit(final VerbatimNode node) {
//        final VerbatimSerializer serializer = lookupSerializer(node.getType());
//        serializer.serialize(node, printer);
        final AttributesBuilder atts = new AttributesBuilder(CODEBLOCK_ATTS);
        if (!StringUtils.isEmpty(node.getType())) {
            atts.add("outputclass", node.getType());
        }
        startElement(PR_D_CODEBLOCK, atts.build());
        characters(node.getText());
        endElement();
    }

//    private VerbatimSerializer lookupSerializer(final String type) {
//        if (type != null && verbatimSerializers.containsKey(type)) {
//            return verbatimSerializers.get(type);
//        } else {
//            return verbatimSerializers.get(VerbatimSerializer.DEFAULT);
//        }
//    }

    @Override
    public void visit(final WikiLinkNode node) {
        String url;
        try {
            url = "./" + URLEncoder.encode(node.getText().replace(' ', '-'), "UTF-8") + ".html";
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
        final AttributesBuilder atts = getLinkAttributes(url);

        startElement(TOPIC_XREF, atts.build());
        //characters(rendering.text);
        visitChildren(node);
        endElement();
    }

    @Override
    public void visit(final TextNode node) {
        if (abbreviations.isEmpty()) {
            characters(node.getText());
        } else {
            printWithAbbreviations(node.getText());
        }
    }

    @Override
    public void visit(final SpecialTextNode node) {
        characters(node.getText());
    }

    @Override
    public void visit(final SuperNode node) {
        visitChildren(node);
    }

    @Override
    public void visit(final Node node) {
//        for (final ToDitaSerializerPlugin plugin : plugins) {
//            if (plugin.visit(node, this, printer)) {
//                return;
//            }
//        }
        // override this method for processing custom Node implementations
        throw new ParseException("Unsupported node type " + node);
    }

    // helpers

    protected void visitChildren(final SuperNode node) {
        for (final Node child : node.getChildren()) {
            child.accept(this);
        }
    }

    protected void visitChildren(final TextNode node) {
        for (final Node child : node.getChildren()) {
            child.accept(this);
        }
    }

    protected void printTag(TextNode node, final DitaClass tag, final Attributes atts) {
        startElement(tag, atts);
        characters(node.getText());
        endElement();
    }

    protected void printTag(SuperNode node, final DitaClass tag, final Attributes atts) {
        startElement(tag, atts);
        visitChildren(node);
        endElement();
    }

    private AttributesBuilder getLinkAttributes(final String href) {
        final AttributesBuilder atts = new AttributesBuilder(XREF_ATTS)
                .add(ATTRIBUTE_NAME_HREF, href);

        final String ext = FilenameUtils.getExtension(href).toLowerCase();
        String format;
        if (ext.equals(ATTR_FORMAT_VALUE_DITA) || ext.equals("xml")) {
            format = null;
        } else if (ext.equals("md") || ext.equals("markdown")) {
            // Markdown is converted to DITA
            format = null;
        } else {
            format = ext;
        }
        if (format != null) {
            atts.add(ATTRIBUTE_NAME_FORMAT, ext);
        }

        final URI uri = toURI(href);
        if (uri != null && uri.isAbsolute()) {
            atts.add(ATTRIBUTE_NAME_SCOPE, ATTR_SCOPE_VALUE_EXTERNAL);
        }

        return atts;
    }

//    private void printAttribute(String name, String value) {
//        printer.print(' ').print(name).print('=').print('"').print(value).print('"');
//    }

    protected String printChildrenToString(final SuperNode node) {
        Printer priorPrinter = printer;
        printer = new Printer();
        visitChildren(node);
        String result = printer.getString();
        printer = priorPrinter;
        return result;
    }

    protected String normalize(final String string) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch(c) {
                case ' ':
                case '\n':
                case '\t':
                    continue;
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    protected void printWithAbbreviations(String string) {
        Map<Integer, Map.Entry<String, String>> expansions = null;

        for (Map.Entry<String, String> entry : abbreviations.entrySet()) {
            // first check, whether we have a legal match
            String abbr = entry.getKey();

            int ix = 0;
            while (true) {
                final int sx = string.indexOf(abbr, ix);
                if (sx == -1) break;

                // only allow whole word matches
                ix = sx + abbr.length();

                if (sx > 0 && Character.isLetterOrDigit(string.charAt(sx - 1))) continue;
                if (ix < string.length() && Character.isLetterOrDigit(string.charAt(ix))) {
                    continue;
                }

                // ok, legal match so save an expansions "task" for all matches
                if (expansions == null) {
                    expansions = new TreeMap<Integer, Map.Entry<String, String>>();
                }
                expansions.put(sx, entry);
            }
        }

        if (expansions != null) {
            int ix = 0;
            for (Map.Entry<Integer, Map.Entry<String, String>> entry : expansions.entrySet()) {
                int sx = entry.getKey();
                final String abbr = entry.getValue().getKey();
                final String expansion = entry.getValue().getValue();

                characters(string.substring(ix, sx));
                final AttributesBuilder atts = new AttributesBuilder(PH_ATTS);
                if (StringUtils.isNotEmpty(expansion)) {
                    atts.add(ATTRIBUTE_NAME_OTHERPROPS, expansion);
                }
                startElement(TOPIC_PH, atts.build());
                characters(abbr);
                endElement();
                ix = sx + abbr.length();
            }
            characters(string.substring(ix));
        } else {
            characters(string);
        }
    }

    // ContentHandler methods

    private void startElement(final DitaClass tag, final Attributes atts) {
        if (++elementCounter == 2 && !tag.equals(TOPIC_TITLE)) {
            throw new ParseException("Document must start with a title: " + tag.localName);
        }
        try {
            contentHandler.startElement(NULL_NS_URI, tag.localName, tag.localName, atts);
        } catch (final SAXException e) {
            e.printStackTrace();
        }
        tagStack.addFirst(tag);
    }

    private void endElement() {
        if (!tagStack.isEmpty()) {
            endElement(tagStack.removeFirst());
        }
    }


    private void endElement(final DitaClass tag) {
        try {
            contentHandler.endElement(NULL_NS_URI, tag.localName, tag.localName);
        } catch (final SAXException e) {
            e.printStackTrace();
        }
    }

    private void characters(final char c) {
        try {
            contentHandler.characters(new char[] { c }, 0, 1);
        } catch (final SAXException e) {
            e.printStackTrace();
        }
    }

    private void characters(final String t) {
        final char[] cs = t.toCharArray();
        try {
            contentHandler.characters(cs, 0, cs.length);
        } catch (final SAXException e) {
            e.printStackTrace();
        }
    }

}
