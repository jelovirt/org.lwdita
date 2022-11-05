/*
 * Based on ToHtmlSerializer (C) 2010-2011 Mathias Doenitz
 */
package com.elovirta.dita.markdown.renderer;

import com.elovirta.dita.markdown.DitaRenderer;
import com.elovirta.dita.markdown.MetadataSerializerImpl;
import com.elovirta.dita.markdown.ParseException;
import com.elovirta.dita.markdown.SaxWriter;
import com.elovirta.dita.utils.ClasspathURIResolver;
import com.elovirta.dita.utils.FragmentContentHandler;
import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ext.abbreviation.Abbreviation;
import com.vladsch.flexmark.ext.abbreviation.AbbreviationBlock;
import com.vladsch.flexmark.ext.anchorlink.AnchorLink;
import com.vladsch.flexmark.ext.attributes.AttributesNode;
import com.vladsch.flexmark.ext.definition.DefinitionItem;
import com.vladsch.flexmark.ext.definition.DefinitionList;
import com.vladsch.flexmark.ext.definition.DefinitionTerm;
import com.vladsch.flexmark.ext.footnotes.Footnote;
import com.vladsch.flexmark.ext.footnotes.FootnoteBlock;
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough;
import com.vladsch.flexmark.ext.gfm.strikethrough.Subscript;
import com.vladsch.flexmark.ext.jekyll.tag.JekyllTag;
import com.vladsch.flexmark.ext.jekyll.tag.JekyllTagBlock;
import com.vladsch.flexmark.ext.superscript.Superscript;
import com.vladsch.flexmark.ext.tables.*;
import com.vladsch.flexmark.ext.typographic.TypographicQuotes;
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterBlock;
import com.vladsch.flexmark.util.ast.ContentNode;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.ReferenceNode;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import org.apache.commons.io.FilenameUtils;
import org.dita.dost.util.DitaClass;
import org.dita.dost.util.SaxCache.EndElementEvent;
import org.dita.dost.util.SaxCache.SaxEvent;
import org.dita.dost.util.SaxCache.StartElementEvent;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.elovirta.dita.markdown.MetadataSerializerImpl.buildAtts;
import static javax.xml.XMLConstants.XML_NS_URI;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.toURI;
import static org.dita.dost.util.XMLUtils.AttributesBuilder;

/**
 * A renderer for a set of node types.
 */
public class CoreNodeRenderer {

    private static final String COLUMN_NAME_COL = "col";
    private static final String ATTRIBUTE_NAME_COLSPAN = "colspan";

    private static final Attributes TOPIC_ATTS = new AttributesBuilder()
            .add(ATTRIBUTE_NAME_CLASS, TOPIC_TOPIC.toString())
            .add(DITA_NAMESPACE, ATTRIBUTE_NAME_DITAARCHVERSION, ATTRIBUTE_PREFIX_DITAARCHVERSION + ":" + ATTRIBUTE_NAME_DITAARCHVERSION, "CDATA", "2.0")
//            .add(ATTRIBUTE_NAME_DOMAINS, "(topic hi-d) (topic ut-d) (topic indexing-d) (topic hazard-d) (topic abbrev-d) (topic pr-d) (topic sw-d) (topic ui-d)")
            .build();
    private static final Attributes BODY_ATTS = buildAtts(TOPIC_BODY);
    private static final Attributes SECTION_ATTS = buildAtts(TOPIC_SECTION);
    private static final Attributes EXAMPLE_ATTS = buildAtts(TOPIC_EXAMPLE);
    private static final Attributes FN_ATTS = buildAtts(TOPIC_FN);
    private static final Attributes LI_ATTS = buildAtts(TOPIC_LI);
    private static final Attributes P_ATTS = buildAtts(TOPIC_P);
    private static final Attributes I_ATTS = buildAtts(HI_D_I);
    private static final Attributes B_ATTS = buildAtts(HI_D_B);
    private static final Attributes DD_ATTS = buildAtts(TOPIC_DD);
    private static final Attributes CODEPH_ATTS = buildAtts(PR_D_CODEPH);
    private static final Attributes CODEBLOCK_ATTS = buildAtts(PR_D_CODEBLOCK);
    private static final Attributes PRE_ATTS = buildAtts(TOPIC_PRE);
    private static final Attributes DT_ATTS = buildAtts(TOPIC_DT);
    private static final Attributes DEL_ATTS = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_PH.toString()).add("status", "deleted").build();
    private static final Attributes LINE_THROUGH_ATTS = buildAtts(HI_D_LINE_THROUGH);
    private static final Attributes SUP_ATTS = buildAtts(HI_D_SUP);
    private static final Attributes SUB_ATTS = buildAtts(HI_D_SUB);
    private static final Attributes TITLE_ATTS = buildAtts(TOPIC_TITLE);
    private static final Attributes SHORTDESC_ATTS = buildAtts(TOPIC_SHORTDESC);
    private static final Attributes PROLOG_ATTS = buildAtts(TOPIC_PROLOG);
    private static final Attributes BLOCKQUOTE_ATTS = buildAtts(TOPIC_LQ);
    private static final Attributes UL_ATTS = buildAtts(TOPIC_UL);
    private static final Attributes DL_ATTS = buildAtts(TOPIC_DL);
    private static final Attributes DLENTRY_ATTS = buildAtts(TOPIC_DLENTRY);
    private static final Attributes OL_ATTS = buildAtts(TOPIC_OL);
    private static final Attributes TABLE_ATTS = buildAtts(TOPIC_TABLE);
    private static final Attributes TGROUP_ATTS = buildAtts(TOPIC_TGROUP);
    private static final Attributes COLSPEC_ATTS = buildAtts(TOPIC_COLSPEC);
    private static final Attributes TBODY_ATTS = buildAtts(TOPIC_TBODY);
    private static final Attributes THEAD_ATTS = buildAtts(TOPIC_THEAD);
    private static final Attributes TR_ATTS = buildAtts(TOPIC_ROW);

    private static final Attributes SIMPLETABLE_ATTS = buildAtts(TOPIC_SIMPLETABLE);
    private static final Attributes STHEAD_ATTS = buildAtts(TOPIC_STHEAD);
    private static final Attributes STROW_ATTS = buildAtts(TOPIC_STROW);
    private static final Attributes STENTRY_ATTS = buildAtts(TOPIC_STENTRY);

    private static final Attributes IMAGE_ATTS = buildAtts(TOPIC_IMAGE);
    private static final Attributes XREF_ATTS = buildAtts(TOPIC_XREF);
    private static final Attributes ALT_ATTS = buildAtts(TOPIC_ALT);
    private static final Attributes PH_ATTS = buildAtts(TOPIC_PH);
    private static final Attributes ENTRY_ATTS = buildAtts(TOPIC_ENTRY);
    private static final Attributes FIG_ATTS = buildAtts(TOPIC_FIG);
    private static final Attributes REQUIRED_CLEANUP_ATTS = buildAtts(TOPIC_REQUIRED_CLEANUP);
    private static final Attributes EMPTY_ATTS = new AttributesImpl();

    private static final Map<String, DitaClass> sections = new HashMap<>();

    static {
        sections.put(TOPIC_SECTION.localName, TOPIC_SECTION);
        sections.put(TOPIC_EXAMPLE.localName, TOPIC_EXAMPLE);
    }

    private final SAXTransformerFactory tf;
    private final Templates t;

    //    private final Map<String, Object> documentMetadata;
    private final Map<String, ReferenceNode> references = new HashMap<>();
    private final Map<String, String> abbreviations = new HashMap<>();
    private final MetadataSerializerImpl metadataSerializer;

    private final Boolean shortdescParagraph;
    private final Boolean idFromYaml;
    private final Boolean lwDita;

    private TableBlock currentTableNode;
    private int currentTableColumn;
    private boolean inSection = false;

    private final Set<String> footnotes = new HashSet<>();
    private String lastId;

    /**
     * Current header level.
     */
    private int headerLevel = 0;

    public CoreNodeRenderer(DataHolder options) {
        this.shortdescParagraph = DitaRenderer.SHORTDESC_PARAGRAPH.getFrom(options);
        this.idFromYaml = DitaRenderer.ID_FROM_YAML.getFrom(options);
        this.lwDita = DitaRenderer.LW_DITA.getFrom(options);
        this.metadataSerializer = new MetadataSerializerImpl(idFromYaml);

        final String stylesheet = lwDita
                ? "/hdita2dita.xsl"
                : "/hdita2dita-markdown.xsl";
        try (InputStream in = getClass().getResourceAsStream(stylesheet)) {
            tf = (SAXTransformerFactory) TransformerFactory.newInstance();
            tf.setURIResolver(new ClasspathURIResolver(tf.getURIResolver()));
            t = tf.newTemplates(new StreamSource(in, "classpath://" + stylesheet));
        } catch (IOException | TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the mapping of nodes this renderer handles to rendering function
     */
    public Map<Class<? extends Node>, NodeRenderingHandler<? extends Node>> getNodeRenderingHandlers() {
        return Stream.concat(
                lwDita
                    ? Stream.<NodeRenderingHandler>of(
                        new NodeRenderingHandler<>(TableBlock.class, (node, context, html) -> renderSimpleTableBlock(node, context, html)),
                        new NodeRenderingHandler<>(TableCaption.class, (node, context, html) -> renderSimpleTableCaption(node, context, html)),
                        new NodeRenderingHandler<>(TableBody.class, (node, context, html) -> renderSimpleTableBody(node, context, html)),
                        new NodeRenderingHandler<>(TableHead.class, (node, context, html) -> renderSimpleTableHead(node, context, html)),
                        new NodeRenderingHandler<>(TableRow.class, (node, context, html) -> renderSimpleTableRow(node, context, html)),
                        new NodeRenderingHandler<>(TableCell.class, (node, context, html) -> renderSimpleTableCell(node, context, html)),
                        new NodeRenderingHandler<>(TableSeparator.class, (node, context, html) -> renderSimpleTableSeparator(node, context, html))
                    )
                    : Stream.<NodeRenderingHandler>of(
                        new NodeRenderingHandler<>(TableBlock.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(TableCaption.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(TableBody.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(TableHead.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(TableRow.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(TableCell.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(TableSeparator.class, (node, context, html) -> render(node, context, html))
                    ),
                Stream.<NodeRenderingHandler>of(
                        new NodeRenderingHandler<>(Abbreviation.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(AbbreviationBlock.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(AttributesNode.class, (node, context, html) -> { /* Ignore */ }),
                        new NodeRenderingHandler<>(YamlFrontMatterBlock.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(Footnote.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(FootnoteBlock.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(AutoLink.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(BlockQuote.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(BulletList.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(Code.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(CodeBlock.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(DefinitionList.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(DefinitionTerm.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(DefinitionItem.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(Document.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(Emphasis.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(FencedCodeBlock.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(HardLineBreak.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(Heading.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(HtmlBlock.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(HtmlCommentBlock.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(HtmlInnerBlock.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(HtmlInnerBlockComment.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(HtmlEntity.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(HtmlInline.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(HtmlInlineComment.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(Image.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(ImageRef.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(IndentedCodeBlock.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(Link.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(Strikethrough.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(LinkRef.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(BulletListItem.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(OrderedListItem.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(MailLink.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(OrderedList.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(Paragraph.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(Reference.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(SoftLineBreak.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(StrongEmphasis.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(Text.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(TextBase.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(ThematicBreak.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(AnchorLink.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(JekyllTagBlock.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(JekyllTag.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(Superscript.class, (node, context, html) -> render(node, context, html)),
                        new NodeRenderingHandler<>(Subscript.class, (node, context, html) -> render(node, context, html))
                ))
                .collect(Collectors.toMap(
                        handler -> handler.getNodeType(),
                        handler -> handler
                ));
    }

//    void toHtml(final Document astRoot) throws SAXException {
////        checkArgNotNull(astRoot, "astRoot");
//        clean(astRoot);
//        final boolean isCompound = hasMultipleTopLevelHeaders(astRoot);
//        contentHandler.startDocument();
//        contentHandler.startPrefixMapping(ATTRIBUTE_PREFIX_DITAARCHVERSION, DITA_NAMESPACE);
//        if (isCompound) {
//            contentHandler.startElement(NULL_NS_URI, ELEMENT_NAME_DITA, ELEMENT_NAME_DITA, EMPTY_ATTS);
//        }
//        try {
//            astRoot.accept(this);
//            while (!tagStack.isEmpty()) {
//                html.endElement();
//            }
//        } catch (final ParseException e) {
//            //e.printStackTrace();
//            throw new SAXException("Failed to parse Markdown: " + e.getMessage(), e);
//        }
//        if (isCompound) {
//            contentHandler.endElement(NULL_NS_URI, ELEMENT_NAME_DITA, ELEMENT_NAME_DITA);
//        }
//        contentHandler.endPrefixMapping(ATTRIBUTE_PREFIX_DITAARCHVERSION);
//        contentHandler.endDocument();
//    }

    private boolean hasMultipleTopLevelHeaders(Document astRoot) {
        final long count = StreamSupport.stream(astRoot.getChildren().spliterator(), false)
                .filter(n -> (n instanceof Heading) && (((Heading) n).getLevel() == 1))
                .count();
        return count > 1;
    }

    /**
     * Replace metadata para with actual metadata element. Modifies AST <b>in-place</b>.
     */
    private void clean(final Document node) {
//        final Map<String, String> metadata = new HashMap<>();
//
//        // read pandoc_title_block
//        final Node first = node.getChildren().iterator().next();
//        if (first instanceof Paragraph && toString(first).startsWith("%")) {
//            final String[] fields = toString(first).split("\n");
//            if (fields.length >= 1) {
//                metadata.put("title", fields[0].substring(1));
//            }
////            if (fields.length >= 2) {
////                metadata.put("authors", fields[0].substring(1));
////            }
////            if (fields.length >= 3) {
////                metadata.put("date", fields[0].substring(1));
////            }
//        }
//
//        // insert header
//        if (metadata.containsKey("title")) {
//            boolean levelOneFound = false;
//            for (final Node c : node.getChildren()) {
//                if (c instanceof Heading) {
//                    final Heading h = (Heading) c;
//                    if (h.getLevel() == 1) {
//                        levelOneFound = true;
//                        break;
//                    }
//                }
//            }
//            final Node m;
//            if (levelOneFound) {
//                m = new MetadataNode(metadata.get("title"));
//            } else {
////                m = new Heading(1, new Text(metadata.get("title")));
//                m = new Heading(BasedSequenceImpl.of(metadata.get("title")));
//            }
//            m.insertBefore(node.getFirstChild());
////            node.getChildren().set(0, m);
//        }
    }

    // Visitor methods
//
//    private static void toString(final Node node, final StringBuilder buf) {
//        if (node instanceof SuperNode) {
//            for (final Node n : node.getChildren()) {
//                toString(n, buf);
//            }
//        } else if (node instanceof Text) {
//            buf.append(((Text) node).getChars());
//            for (final Node n : node.getChildren()) {
//                toString(n, buf);
//            }
//        } else if (node instanceof SimpleNode) {
//            buf.append(toString((SimpleNode) node));
//        } else {
//            throw new ParseException("Trying to process " + node);
//        }
//    }

//    private static String toString(final Node node) {
//        final StringBuilder buf = new StringBuilder();
//        toString(node, buf);
//        return buf.toString();
//    }

    private void render(final Document node, final NodeRendererContext context, final SaxWriter html) {
//        for (final ReferenceNode refNode : node.getReferences()) {
//            references.put(normalize(toString(refNode)), refNode);
//        }
//        for (final AbbreviationNode abbrNode : node.getAbbreviations()) {
//            visitChildren(abbrNode);
//            String abbr = printer.getString();
//            printer.clear();
//            abbrNode.getExpansion().accept(this);
//            String expansion = printer.getString();
//            abbreviations.put(abbr, expansion);
//            printer.clear();
//        }
        final boolean isCompound = hasMultipleTopLevelHeaders(node);
        if (isCompound) {
            final AttributesBuilder atts = new AttributesBuilder()
                    .add(DITA_NAMESPACE, ATTRIBUTE_NAME_DITAARCHVERSION, ATTRIBUTE_PREFIX_DITAARCHVERSION + ":" + ATTRIBUTE_NAME_DITAARCHVERSION, "CDATA", "2.0");
            if (lwDita) {
                atts.add(ATTRIBUTE_NAME_SPECIALIZATIONS, "(topic hi-d)(topic em-d)");
            } else {
                atts.add(ATTRIBUTE_NAME_SPECIALIZATIONS, "@props/audience @props/deliveryTarget @props/otherprops @props/platform @props/product");
            }
            html.startElement(ELEMENT_NAME_DITA, atts.build());
        }
        context.renderChildren(node);
        if (isCompound) {
            html.endElement();
        }
    }

    private void render(final Abbreviation node, final NodeRendererContext context, final SaxWriter html) {
        html.characters(node.getChars().toString());
    }

    private void render(final AbbreviationBlock node, final NodeRendererContext context, final SaxWriter html) {
        // Ignore
    }

    private void render(AnchorLink node, final NodeRendererContext context, final SaxWriter html) {
        context.renderChildren(node);
    }

    private void render(JekyllTagBlock node, final NodeRendererContext context, final SaxWriter html) {
        context.renderChildren(node);
    }

    private void render(JekyllTag node, final NodeRendererContext context, final SaxWriter html) {
        if (node.getTag().toString().equals("include")) {
            final AttributesBuilder atts = new AttributesBuilder(REQUIRED_CLEANUP_ATTS)
                    .add(ATTRIBUTE_NAME_CONREF, node.getParameters().toString());
            html.startElement(TOPIC_REQUIRED_CLEANUP, atts.build());
            html.endElement();
        }
    }

    private void render(final AutoLink node, final NodeRendererContext context, final SaxWriter html) {
        if (node.getChars().charAt(0) == '<') {
            final AttributesBuilder atts = getLinkAttributes(node.getText().toString());

            html.startElement(TOPIC_XREF, getInlineAttributes(node, atts.build()));
            html.characters(node.getText().toString());
            html.endElement();
        } else {
            context.renderChildren(node);
        }
    }

    private void render(final TextBase node, final NodeRendererContext context, final SaxWriter html) {
        context.renderChildren(node);
    }

    private void render(final Footnote node, final NodeRendererContext context, final SaxWriter html) {
        final String callout = node.getText().toString().trim();
        final String id = getId("fn_" + callout);
        if (footnotes.contains(id)) {
            final Attributes atts = new AttributesBuilder(XREF_ATTS)
                    .add(ATTRIBUTE_NAME_TYPE, "fn")
                    .add(ATTRIBUTE_NAME_HREF, String.format("#%s/%s", lastId, id))
                    .build();
            html.startElement(TOPIC_XREF, atts);
            html.endElement();
        } else {
            footnotes.add(id);
            final Attributes atts = new AttributesBuilder(FN_ATTS)
                    .add("callout", callout)
                    .add(ATTRIBUTE_NAME_ID, id)
                    .build();
            html.startElement(TOPIC_FN, atts);
            Node child = node.getFootnoteBlock().getFirstChild();
            while (child != null) {
                context.renderChildren(child);
                child = child.getNext();
            }
            html.endElement();
        }
    }

    private void render(final FootnoteBlock node, final NodeRendererContext context, final SaxWriter html) {
        // Ignore
    }

    private void render(final BlockQuote node, final NodeRendererContext context, final SaxWriter html) {
        if (lwDita) {
            context.renderChildren(node);
        } else {
            printTag(node, context, html, TOPIC_LQ, getAttributesFromAttributesNode(node, BLOCKQUOTE_ATTS));
        }
    }

    private Attributes getAttributesFromAttributesNode(Node node, Attributes base) {
        if (!lwDita && isAttributesParagraph(node.getNext())) {
            final Title header = Title.getFromChildren(node.getNext());
            final AttributesBuilder builder = new AttributesBuilder(base);
            return readAttributes(header, builder).build();
        } else {
            return base;
        }
    }

    private Attributes getInlineAttributes(Node node, Attributes base) {
        if (!lwDita) {
            if (node.getChildOfType(AttributesNode.class) != null) {
                final Title header = Title.getFromChildren(node);
                final AttributesBuilder builder = new AttributesBuilder(base);
                return readAttributes(header, builder).build();
            } else if (node.getNext() instanceof AttributesNode) {
                final Title header = Title.getFromNext(node);
                final AttributesBuilder builder = new AttributesBuilder(base);
                return readAttributes(header, builder).build();
            }
        }
        return base;
    }

    private void render(final BulletList node, final NodeRendererContext context, final SaxWriter html) {
        printTag(node, context, html, TOPIC_UL, getAttributesFromAttributesNode(node, UL_ATTS));
    }

    private void render(final Code node, final NodeRendererContext context, final SaxWriter html) {
        if (lwDita) {
            printTag(node, context, html, TOPIC_PH, PH_ATTS);
        } else {
            printTag(node, context, html, PR_D_CODEPH, getInlineAttributes(node, CODEPH_ATTS));
        }
    }

    private void render(final DefinitionList node, final NodeRendererContext context, final SaxWriter html) {
        html.startElement(TOPIC_DL, getAttributesFromAttributesNode(node, DL_ATTS));
        DitaClass previous = null;
//        for (final Node child : node.getChildren()) {
//            if (previous == null) {
//                html.startElement(TOPIC_DLENTRY, DLENTRY_ATTS);
//            }
//            if (child instanceof DefinitionTermNode) {
//                if (TOPIC_DD.equals(previous)) {
//                    html.endElement(); // dlentry
//                    html.startElement(TOPIC_DLENTRY, DLENTRY_ATTS);
//                }
//            }
        context.renderChildren(node);
//            previous = (child instanceof DefinitionTermNode) ? TOPIC_DT : TOPIC_DD;
//        }
//        html.endElement(); // dlentry
        html.endElement(); // dl
    }

    private void render(final DefinitionTerm node, final NodeRendererContext context, final SaxWriter html) {
        if (node.getPrevious() == null || !(node.getPrevious() instanceof DefinitionTerm)) {
            html.startElement(TOPIC_DLENTRY, DLENTRY_ATTS);
        }
//        printTag(node, context, html, TOPIC_DT, DT_ATTS);
        html.startElement(TOPIC_DT, DT_ATTS);
        Node child = node.getFirstChild();
        while (child != null) {
            Node next = child.getNext();
            context.renderChildren(child);
            child = next;
        }
        html.endElement();
    }

    private void render(final DefinitionItem node, final NodeRendererContext context, final SaxWriter html) {
        printTag(node, context, html, TOPIC_DD, DD_ATTS);
        if (node.getNext() == null || !(node.getNext() instanceof DefinitionItem)) {
            html.endElement();
        }
    }

    private void render(final Image node, final NodeRendererContext context, final SaxWriter html) {
        final AttributesBuilder atts = new AttributesBuilder(getInlineAttributes(node, IMAGE_ATTS))
                .add(ATTRIBUTE_NAME_HREF, node.getUrl().toString());
        writeImage(node, node.getTitle().toString(), null, atts, context, html);
    }

    private void render(final Superscript node, final NodeRendererContext context, final SaxWriter html) {
        printTag(node, context, html, HI_D_SUP, getInlineAttributes(node, SUP_ATTS));
    }

    private void render(final Subscript node, final NodeRendererContext context, final SaxWriter html) {
        printTag(node, context, html, HI_D_SUB, getInlineAttributes(node, SUB_ATTS));
    }

    private void writeImage(Image node, final String title, final String alt, final AttributesBuilder atts,
                            final NodeRendererContext context, SaxWriter html) {
        if (!title.isEmpty()) {
            html.startElement(TOPIC_FIG, FIG_ATTS);
            html.startElement(TOPIC_TITLE, TITLE_ATTS);
            html.characters(title);
            html.endElement();
            html.startElement(TOPIC_IMAGE, atts.build());
            if (hasChildren(node)) {
                html.startElement(TOPIC_ALT, ALT_ATTS);
                if (alt != null) {
                    html.characters(alt);
                } else {
                    context.renderChildren(node);
                }
                html.endElement();
            }
            html.endElement();
            html.endElement();
        } else {
            if (onlyImageChild) {
                atts.add("placement", "break");
            }
            html.startElement(TOPIC_IMAGE, atts.build());
            if (hasChildren(node)) {
                html.startElement(TOPIC_ALT, ALT_ATTS);
                if (alt != null) {
                    html.characters(alt);
                } else {
                    context.renderChildren(node);
                }
                html.endElement();
            }
            html.endElement();
        }
    }

    private void writeImage(ImageRef node, final String title, final String alt, final AttributesBuilder atts,
                            final NodeRendererContext context, SaxWriter html) {
        if (!title.isEmpty()) {
            html.startElement(TOPIC_FIG, FIG_ATTS);
            html.startElement(TOPIC_TITLE, TITLE_ATTS);
            html.characters(title);
            html.endElement();
            html.startElement(TOPIC_IMAGE, atts.build());
            if (hasChildren(node)) {
                html.startElement(TOPIC_ALT, ALT_ATTS);
                if (alt != null) {
                    html.characters(alt);
                } else {
                    context.renderChildren(node);
                }
                html.endElement();
            }
            html.endElement(); // image
            html.endElement(); // fig
        } else {
            if (onlyImageChild) {
                atts.add("placement", "break");
            }
            html.startElement(TOPIC_IMAGE, atts.build());
            if (hasChildren(node)) {
                html.startElement(TOPIC_ALT, ALT_ATTS);
                if (alt != null) {
                    html.characters(alt);
                } else {
                    context.renderChildren(node);
                }
                html.endElement();
            }
            html.endElement(); // image
        }
    }

//    @Override
//    private void render(final ExpLinkNode node, final NodeRendererContext context, final DitaWriter html) {
//        final AttributesBuilder atts = getLinkAttributes(node.url);
//        //atts.add("title", node.title);
//
//        html.startElement(TOPIC_XREF, atts.build());
//        context.renderChildren(node);
//        html.endElement();
//    }

    private static <E> E containsSome(final Collection<E> col, final Collection<E> find) {
        for (final E c : col) {
            if (find.contains(c)) {
                return c;
            }
        }
        return null;
    }

    private final static Pattern p = Pattern.compile("^(.+?)(?:\\{(.+?)\\})?$");

    private Metadata parseMetadata(Node node) {
        List<String> classes;
        String id;
        BasedSequence info = node.getChars();
        final Matcher m = p.matcher(info);
        if (m.matches()) {
            if (m.group(2) != null) {
                return Metadata.parse(m.group(2));
            } else {
//                    id = getId(title);
                id = null;
            }
        } else {
//                id = getId(contents);
            id = null;
        }
        return null;
    }

    private void render(final Heading node, final NodeRendererContext context, final SaxWriter html) {
        if (lwDita && node.getLevel() > 2) {
            throw new ParseException("LwDITA does not support level " + node.getLevel() + " header: " + node.getText());
        }
//        if (node.getLevel() > headerLevel + 1) {
//            throw new ParseException("Header level raised from " + headerLevel + " to " + node.getLevel() + " without intermediate header level");
//        }
        final StringBuilder buf = new StringBuilder();
        node.getAstExtra(buf);
        Title header = null;
        if (!lwDita) {
            if (node.getFirstChild() instanceof AnchorLink) {
                header = Title.getFromChildren(node.getFirstChild());
            } else {
                header = Title.getFromChildren(node);
            }
            header.id.ifPresent(node::setAnchorRefId);
        }

        if (inSection) {
            html.endElement(); // section or example
            inSection = false;
        }
        final DitaClass cls;
        final boolean isSection;
        if (lwDita && node.getLevel() == 2) {
            isSection = true;
            cls = TOPIC_SECTION;
        } else if (!lwDita) {
            final String sectionClassName = containsSome(header.classes, sections.keySet());
            if (sectionClassName != null) {
                isSection = true;
                cls = sections.get(sectionClassName);
            } else {
                isSection = false;
                cls = null;
            }
        } else {
            isSection = false;
            cls = null;
        }
        if (isSection) {
            if (node.getLevel() <= headerLevel) {
                throw new ParseException("Level " + node.getLevel() + " section title must be higher level than parent topic title " + headerLevel);
            }
            final AttributesBuilder atts = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, cls.toString());
            final String id = getSectionId(node, header);
            if (id != null) {
                atts.add(ATTRIBUTE_NAME_ID, id);
            }
            if (!lwDita) {
                final Collection<String> classes = new ArrayList<>(header.classes);
                classes.removeAll(sections.keySet());
                if (!classes.isEmpty()) {
                    atts.add("outputclass", String.join(" ", classes));
                }
            }
            html.startElement(cls, atts.build());
            inSection = true;
            html.startElement(TOPIC_TITLE, TITLE_ATTS);
            context.renderChildren(node);
            html.endElement(); // title
        } else {
            if (headerLevel > 0) {
                html.endElement(); // body
            }
            for (; node.getLevel() <= headerLevel; headerLevel--) {
                html.endElement(); // topic
            }
            headerLevel = node.getLevel();

            final AttributesBuilder atts = lwDita
                    ? new AttributesBuilder(TOPIC_ATTS)
                        .add(ATTRIBUTE_NAME_SPECIALIZATIONS, "(topic hi-d)(topic em-d)")
                    : new AttributesBuilder(TOPIC_ATTS)
                        .add(ATTRIBUTE_NAME_SPECIALIZATIONS, "@props/audience @props/deliveryTarget @props/otherprops @props/platform @props/product");

            final String id = getTopicId(node, header);
            if (id != null) {
                lastId = id;
                atts.add(ATTRIBUTE_NAME_ID, id);
            }
            if (!lwDita) {
                if (!header.classes.isEmpty()) {
                    atts.add(ATTRIBUTE_NAME_OUTPUTCLASS, String.join(" ", header.classes));
                }
                for (Map.Entry<String, String> attr : header.attributes.entrySet()) {
                    atts.add(attr.getKey(), attr.getValue());
                }
            }
            html.startElement(TOPIC_TOPIC, atts.build());
            html.startElement(TOPIC_TITLE, TITLE_ATTS);
            context.renderChildren(node);
            html.endElement(); // title
            if (shortdescParagraph && node.getNext() instanceof Paragraph) {
                html.startElement(TOPIC_SHORTDESC, SHORTDESC_ATTS);
                context.renderChildren(node.getNext());
                html.endElement(); // shortdesc
            }
            if (node.getLevel() == 1) {
                final Node firstChild = node.getDocument().getFirstChild();
                if (firstChild instanceof YamlFrontMatterBlock) {
                    html.startElement(TOPIC_PROLOG, PROLOG_ATTS);
                    metadataSerializer.render((YamlFrontMatterBlock) firstChild, context, html);
                    html.endElement();
                }
            }
            html.startElement(TOPIC_BODY, BODY_ATTS);
        }
    }

    private String getSectionId(Heading node, Title header) {
        if (header != null) {
            if (node.getAnchorRefId() != null) {
                return node.getAnchorRefId();
            } else {
                return null;
//                return getId(header.title);
            }
        } else {
            if (node.getAnchorRefId() != null) {
                return node.getAnchorRefId();
            } else {
                return getId(node.getText().toString());
            }
        }
    }

    private String getTopicId(final Heading node, final Title header) {
        if (idFromYaml && node.getLevel() == 1 && node.getDocument().getChildOfType(YamlFrontMatterBlock.class) != null) {
            final AbstractYamlFrontMatterVisitor v = new AbstractYamlFrontMatterVisitor();
            v.visit(node.getDocument());
            final Map<String, List<String>> metadata = v.getData();
            final List<String> ids = metadata.get("id");
            if (ids != null && !ids.isEmpty()) {
                return ids.get(0);
            }
        }
        return getSectionId(node, header);
    }

    private void outputMetadata(Map<String, Object> documentMetadata, SaxWriter html) {
        html.startElement(TOPIC_PROLOG, PROLOG_ATTS);
//        metadataSerializer.write(documentMetadata);
        html.endElement();
    }

    private void render(final YamlFrontMatterBlock node, final NodeRendererContext context, final SaxWriter html) {
//        final String text = node.getChars().toString();
//        html.characters(text);
        // YAML header is pulled by Heading renderer
    }

    //    private String getId(final Heading node) {
//        return getId(toString(node));
//    }

    private static String getId(final String contents) {
        return contents.toLowerCase()
                .replaceAll("[^\\w]", "")
                .trim()
                .replaceAll("\\s+", "_");
    }

    private void render(final HtmlBlock node, final NodeRendererContext context, final SaxWriter html) {
        final String text = node.getChars().toString();
        final FragmentContentHandler fragmentFilter = new FragmentContentHandler();
        fragmentFilter.setContentHandler(html.contentHandler);
        final TransformerHandler h;
        try {
            h = tf.newTransformerHandler(t);
        } catch (final TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
        h.setResult(new SAXResult(fragmentFilter));
        final HtmlParser parser = new HtmlParser();
        parser.setContentHandler(h);
        try {
            parser.parse(new InputSource(new StringReader(text)));
        } catch (IOException | SAXException e) {
            throw new ParseException("Failed to parse HTML: " + e.getMessage(), e);
        }
    }

    private void render(final HtmlInline node, final NodeRendererContext context, final SaxWriter html) {
        final String text = node.getChars().toString();
        final CacheContentHandler cache = new CacheContentHandler();
        final TransformerHandler h;
        try {
            h = tf.newTransformerHandler(t);
        } catch (final TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
        h.setResult(new SAXResult(cache));
        final HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALLOW);
        parser.setContentHandler(h);
        if (text.startsWith("</")) {
            final String data = text.replaceAll("/", "") + text;
            try (final StringReader in = new StringReader(data)) {
                parser.parse(new InputSource(in));
                for (SaxEvent event : cache.events) {
                    if (event instanceof EndElementEvent) {
                        event.write(html.contentHandler);
                    }
                }
            } catch (IOException | SAXException e) {
                throw new ParseException("Failed to parse HTML: " + e.getMessage(), e);
            }
        } else {
            try (final StringReader in = new StringReader(text)) {
                parser.parse(new InputSource(in));
                for (SaxEvent event : cache.events) {
                    if (event instanceof StartElementEvent) {
                        event.write(html.contentHandler);
                    }
                }
            } catch (IOException | SAXException e) {
                throw new ParseException("Failed to parse HTML: " + e.getMessage(), e);
            }
        }
    }

    private static class CacheContentHandler extends XMLFilterImpl {

        final List<SaxEvent> events = new ArrayList<>();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            events.add(new StartElementEvent(uri, localName, qName, atts));
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            events.add(new EndElementEvent(uri, localName, qName));
        }
    }

    private StartElementEvent parseHtmlInline(final HtmlInline node) {
        final List<StartElementEvent> events = new ArrayList<>();
        final HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALLOW);
        parser.setContentHandler(new XMLFilterImpl() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                events.add(new StartElementEvent(uri, localName, qName, atts));
            }
        });
        try (final StringReader in = new StringReader(node.getChars().toString())) {
            parser.parse(new InputSource(in));
        } catch (IOException | SAXException e) {
            throw new ParseException("Failed to parse HTML: " + e.getMessage(), e);
        }
        if (events.isEmpty()) {
            return null;
        }
        return events.get(0);
    }

    private void render(final ListItem node, final NodeRendererContext context, final SaxWriter html) {
        printTag(node, context, html, TOPIC_LI, LI_ATTS);
    }

    private void render(final MailLink node, final NodeRendererContext context, final SaxWriter html) {
        final AttributesBuilder atts = getLinkAttributes("mailto:" + node.getText());
        atts.add(ATTRIBUTE_NAME_FORMAT, "email");

        html.startElement(TOPIC_XREF, getInlineAttributes(node, atts.build()));
        context.renderChildren(node);
        html.endElement();
    }

    private void render(final OrderedList node, final NodeRendererContext context, final SaxWriter html) {
        printTag(node, context, html, TOPIC_OL, getAttributesFromAttributesNode(node, OL_ATTS));
    }

    private boolean onlyImageChild = false;

    private boolean isAttributesParagraph(final Node node) {
        if (node == null) {
            return false;
        }
        final Node firstChild = node.getFirstChild();
        return firstChild != null && firstChild instanceof AttributesNode &&  firstChild.getNext() == null;
    }

    private void render(final Paragraph node, final NodeRendererContext context, final SaxWriter html) {
        if (isAttributesParagraph(node)) {
            // Attributes for previous block
        } else if (shortdescParagraph && !inSection && node.getPrevious() instanceof Heading) {
            // Pulled by Heading
        } else if (containsImage(node)) {
            onlyImageChild = true;
            context.renderChildren(node);
            onlyImageChild = false;
        } else {
            final Attributes atts;
            if (!lwDita) {
                final Title header = Title.getFromChildren(node);
                final AttributesBuilder builder = new AttributesBuilder(P_ATTS);
                atts = readAttributes(header, builder).build();
            } else {
                atts = P_ATTS;
            }
            printTag(node, context, html, TOPIC_P, atts);
        }
    }

    private AttributesBuilder readAttributes(Title header, AttributesBuilder builder) {
        if (!header.classes.isEmpty()) {
            builder.add(ATTRIBUTE_NAME_OUTPUTCLASS, header.classes.stream().collect(Collectors.joining(" ")));
        }
        for (Map.Entry<String, String> attr : header.attributes.entrySet()) {
            builder.add(attr.getKey(), attr.getValue());
        }
        header.id.ifPresent(id -> builder.add(ATTRIBUTE_NAME_ID, id));
        return builder;
    }

    /**
     * Contains only single image
     */
    private boolean containsImage(final ContentNode node) {
        final Node first = node.getFirstChild();
        if (first != null && (first instanceof Image || first instanceof ImageRef)) {
            return first.getNextAnyNot(AttributesNode.class) == null;
        }
        return false;
    }

    private void render(final TypographicQuotes node, final NodeRendererContext context, final SaxWriter html) {
//        switch (node.getType()) {
//            case DoubleAngle:
//                html.characters('\u00AB');//&laquo;
//                context.renderChildren(node);
//                html.characters('\u00AB');//&laquo;
//                break;
//            case Double:
//                html.characters('\u201C');//"&ldquo;"
//                context.renderChildren(node);
//                html.characters('\u201C');//"&ldquo;"
//                break;
//            case Single:
        html.characters('\u2018');//"&lsquo;"
        context.renderChildren(node);
        html.characters('\u2018');//"&lsquo;"
//                break;
//        }
    }

    private void render(final ReferenceNode node, final NodeRendererContext context, final SaxWriter html) {
        throw new RuntimeException();
    }

    private void render(final Reference node, final NodeRendererContext context, final SaxWriter html) {
        // Ignore
    }

    private void render(final ImageRef node, final NodeRendererContext context, final SaxWriter html) {
        final String text = node.getText().toString();
        final String key = node.getReference() != null ? node.getReference().toString() : text;
        final Reference refNode = node.getReferenceNode(node.getDocument());
        if (refNode == null) { // "fake" reference image link
            final AttributesBuilder atts = new AttributesBuilder(IMAGE_ATTS)
                    .add(ATTRIBUTE_NAME_KEYREF, key);
            if (onlyImageChild) {
                atts.add("placement", "break");
            }
            html.startElement(TOPIC_IMAGE, getInlineAttributes(node, atts.build()));
//            if (node.getReference() != null) {
//                context.renderChildren(node);
//            }
            html.endElement();
        } else {
            final AttributesBuilder atts = new AttributesBuilder(getInlineAttributes(node, IMAGE_ATTS))
                    .add(ATTRIBUTE_NAME_HREF, refNode.getUrl().toString());
            if (key != null) {
                atts.add(ATTRIBUTE_NAME_KEYREF, key);
            }
            writeImage(node, refNode.getTitle().toString(), text, atts, context, html);
        }
    }

    private void render(final RefNode node, final NodeRendererContext context, final SaxWriter html) {
        final String text = node.getText().toString();
        final String key = node.getReference() != null ? node.getReference().toString() : text;
        final Reference refNode = node.getReferenceNode(node.getDocument());
        if (refNode == null) { // "fake" reference link
            final AttributesBuilder atts = new AttributesBuilder(XREF_ATTS)
                    .add(ATTRIBUTE_NAME_KEYREF, key);
            html.startElement(TOPIC_XREF, atts.build());
            if (!node.getText().toString().isEmpty()) {
                html.characters(node.getText().toString());
            }
            html.endElement();
        } else {
            final AttributesBuilder atts = getLinkAttributes(refNode.getUrl().toString());
            html.startElement(TOPIC_XREF, atts.build());
            if (!refNode.getTitle().toString().isEmpty()) {
                html.characters(refNode.getTitle().toString());
            } else {
                context.renderChildren(node);
            }
            html.endElement();
        }
    }

    private void render(final Link node, final NodeRendererContext context, final SaxWriter html) {
        final String text = node.getText().toString();
//        final String key = node.getReference() != null ? node.getReference().toString() : text;
//        final ReferenceNode refNode = node.getReferenceNode(node.getDocument());
//        if (refNode == null) { // "fake" reference link
//            final AttributesBuilder atts = new AttributesBuilder(XREF_ATTS)
//                    .add(ATTRIBUTE_NAME_KEYREF, key);
//            html.startElement(TOPIC_XREF, atts.build());
//            if (node.getReference() != null) {
//                context.renderChildren(node);
//            }
//            html.endElement();
//        } else {
        final AttributesBuilder atts = getLinkAttributes(node.getUrl().toString());
        html.startElement(TOPIC_XREF, getInlineAttributes(node, atts.build()));
//            if (!refNode.getTitle().toString().isEmpty()) {
//                html.characters(refNode.toString());
//            } else {
        context.renderChildren(node);
//            }
        html.endElement();
//        }
    }

//    @Override
//    private void render(final SimpleNode node, final NodeRendererContext context, final DitaWriter html) {
//        switch (node.getType()) {
//            case Apostrophe:
//                html.characters('\u2019');//"&rsquo;"
//                break;
//            case Ellipsis:
//                html.characters('\u2026');//"&hellip;"
//                break;
//            case Emdash:
//                html.characters('\u2014');//"&mdash;"
//                break;
//            case Endash:
//                html.characters('\u2013');//"&ndash;"
//                break;
//            case HRule:
////                html.startElement(TOPIC_PH, new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, "+ html-d/hr ").build());
////                html.endElement();
//                html.characters('\n');
//                break;
//            case Linebreak:
////                html.startElement(TOPIC_PH, new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, "+ html-d/br ").build());
////                html.endElement();
//                html.characters('\n');
//                break;
//            case Nbsp:
//                html.characters("\u00A0");//&nbsp;
//                break;
//            default:
//                throw new ParseException("Unsupported simple node: " + node.getType());
//        }
//    }

//    private static String toString(final SimpleNode node) {
//        switch (node.getType()) {
//            case Apostrophe:
//                return "\u2019";//"&rsquo;"
//            case Ellipsis:
//                return "\u2026";//"&hellip;"
//            case Emdash:
//                return "\u2014'";//"&mdash;"
//            case Endash:
//                return "\u2013";//"&ndash;"
//            case Linebreak:
//            case HRule:
//                return "\n";
//            case Nbsp:
//                return "\u00A0";//&nbsp;
//            default:
//                throw new ParseException("Unsupported simple node: " + node.getType());
//        }
//    }


//    @Override
//    private void render(final StrongEmphSuperNode node, final NodeRendererContext context, final DitaWriter html) {
//        if (node.isClosed()) {
//            if (node.isStrong()) {
//                printTag(node, HI_D_B, B_ATTS);
//            } else {
//                printTag(node, HI_D_I, I_ATTS);
//            }
//        } else {
//            //sequence was not closed, treat open chars as ordinary chars
//            html.characters(node.getChars());
//            context.renderChildren(node);
//        }
//    }

    private void render(final Strikethrough node, final NodeRendererContext context, final SaxWriter html) {
        if (lwDita) {
            printTag(node, context, html, TOPIC_PH, PH_ATTS);
        } else {
            printTag(node, context, html, HI_D_LINE_THROUGH, getInlineAttributes(node, LINE_THROUGH_ATTS));
        }
    }

    private void render(final Emphasis node, final NodeRendererContext context, final SaxWriter html) {
        printTag(node, context, html, HI_D_I, getInlineAttributes(node, I_ATTS));
    }

    private void render(final StrongEmphasis node, final NodeRendererContext context, final SaxWriter html) {
        printTag(node, context, html, HI_D_B, getInlineAttributes(node, B_ATTS));
    }

    // OASIS Table

    private void render(final TableBody node, final NodeRendererContext context, final SaxWriter html) {
        printTag(node, context, html, TOPIC_TBODY, TBODY_ATTS);
    }

    private void render(final TableCaption node, final NodeRendererContext context, final SaxWriter html) {
        // Pull processed by TableBlock
//        html.startElement(TOPIC_TITLE, TITLE_ATTS);
//        context.renderChildren(node);
//        html.endElement();
    }

    private void render(final TableCell node, final NodeRendererContext context, final SaxWriter html) {
//        final List<TableColumnNode> columns = currentTableNode.getColumns();
//        final TableColumnNode column = columns.get(Math.min(currentTableColumn, columns.size() - 1));
//
        final AttributesBuilder atts = new AttributesBuilder(ENTRY_ATTS);
//        column.accept(this);
        if (node.getAlignment() != null) {
            atts.add(ATTRIBUTE_NAME_ALIGN, node.getAlignment().cellAlignment().name().toLowerCase());
        }
        if (node.getSpan() > 1) {
            atts.add(ATTRIBUTE_NAME_NAMEST, COLUMN_NAME_COL + Integer.toString(currentTableColumn + 1));
            atts.add(ATTRIBUTE_NAME_NAMEEND, COLUMN_NAME_COL + Integer.toString(currentTableColumn + node.getSpan()));
        }
        html.startElement(TOPIC_ENTRY, atts.build());
        context.renderChildren(node);
        html.endElement();

        currentTableColumn += node.getSpan();
    }

    private String tableColumnAlignment = null;

//    @Override
//    private void render(final TableColumnNode node, final NodeRendererContext context, final DitaWriter html) {
//        switch (node.getAlignment()) {
//            case None:
//                break;
//            case Left:
//                tableColumnAlignment = "left";
//                break;
//            case Right:
//                tableColumnAlignment = "right";
//                break;
//            case Center:
//                tableColumnAlignment = "center";
//                break;
//            default:
//                throw new ParseException("Unsupported table column alignment: " + node.getAlignment());
//        }
//    }

    private void render(final TableHead node, final NodeRendererContext context, final SaxWriter html) {
        printTag(node, context, html, TOPIC_THEAD, THEAD_ATTS);
    }

    private void render(final TableBlock node, final NodeRendererContext context, final SaxWriter html) {
        currentTableNode = node;
        final Attributes tableAtts;
        if (!lwDita && isAttributesParagraph(node.getNext())) {
            final Title header = Title.getFromChildren(node.getNext());
            final AttributesBuilder builder = new AttributesBuilder(TABLE_ATTS);
            tableAtts = readAttributes(header, builder).build();
        } else {
            tableAtts = TABLE_ATTS;
        }
        html.startElement(TOPIC_TABLE, tableAtts);
        for (final Node child : node.getChildren()) {
            if (child instanceof TableCaption) {
                html.startElement(TOPIC_TITLE, TITLE_ATTS);
                context.renderChildren((TableCaption) child);
                html.endElement();
//                render((TableCaption) child, context, html);
            }
        }
        final int maxCols = findMaxCols(node);
        final Attributes atts = new AttributesBuilder(TGROUP_ATTS)
                .add(ATTRIBUTE_NAME_COLS, Integer.toString(maxCols))
                .build();
        html.startElement(TOPIC_TGROUP, atts);

        for (int i = 0; i < maxCols; i++) {
            final AttributesBuilder catts = new AttributesBuilder(COLSPEC_ATTS)
                    .add(ATTRIBUTE_NAME_COLNAME, COLUMN_NAME_COL + (i + 1));
//            switch (col.getAlignment()) {
//                case Center:
//                    catts.add(ATTRIBUTE_NAME_ALIGN, "center");
//                    break;
//                case Right:
//                    catts.add(ATTRIBUTE_NAME_ALIGN, "right");
//                    break;
//                case Left:
//                    catts.add(ATTRIBUTE_NAME_ALIGN, "left");
//                    break;
//            }
            html.startElement(TOPIC_COLSPEC, catts.build());
            html.endElement(); // colspec
        }

//            final AttributesBuilder catts = new AttributesBuilder(COLSPEC_ATTS)
//                    .add(ATTRIBUTE_NAME_COLNAME, COLUMN_NAME_COL + counter);
//            switch (col.getAlignment()) {
//                case Center:
//                    catts.add(ATTRIBUTE_NAME_ALIGN, "center");
//                    break;
//                case Right:
//                    catts.add(ATTRIBUTE_NAME_ALIGN, "right");
//                    break;
//                case Left:
//                    catts.add(ATTRIBUTE_NAME_ALIGN, "left");
//                    break;
//            }
//            html.startElement(TOPIC_COLSPEC, catts.build());
//            html.endElement(); // colspec
//        for (final Node child : node.getChildren()) {
//            if (!(child instanceof TableCaptionNode)) {
//                context.renderChildren(child);
//            }
//        }
        context.renderChildren(node);
        html.endElement(); // tgroup
        html.endElement(); // table
        currentTableNode = null;
    }

    private int findMaxCols(TableBlock table) {
        int max = 0;
        for (Node body = table.getFirstChild(); body != null; body = body.getNext()) {
            if (body instanceof TableHead || body instanceof TableBody) {
                for (Node row = body.getFirstChild(); row != null; row = row.getNext()) {
                    if (row instanceof TableRow) {
                        int colCount = 0;
                        for (Node col = row.getFirstChild(); col != null; col = col.getNext()) {
                            if (col instanceof TableCell) {
                                TableCell c = ((TableCell) col);
                                colCount = colCount + c.getSpan();
                            }
                        }
                        max = Math.max(max, colCount);
                    }
                }
            }
        }
        return max;
    }

    private void render(TableSeparator node, NodeRendererContext context, SaxWriter html) {
        // Ignore
    }

    private void render(final TableRow node, final NodeRendererContext context, final SaxWriter html) {
        currentTableColumn = 0;
        printTag(node, context, html, TOPIC_ROW, TR_ATTS);
    }

    // Simple table

    private void renderSimpleTableBlock(final TableBlock node, final NodeRendererContext context, final SaxWriter html) {
        currentTableNode = node;
        final Attributes tableAtts;
        if (!lwDita && isAttributesParagraph(node.getNext())) {
            final Title header = Title.getFromChildren(node.getNext());
            final AttributesBuilder builder = new AttributesBuilder(SIMPLETABLE_ATTS);
            tableAtts = readAttributes(header, builder).build();
        } else {
            tableAtts = SIMPLETABLE_ATTS;
        }
        html.startElement(TOPIC_SIMPLETABLE, tableAtts);
        for (final Node child : node.getChildren()) {
            if (child instanceof TableCaption) {
                html.startElement(TOPIC_TITLE, TITLE_ATTS);
                context.renderChildren((TableCaption) child);
                html.endElement();
//                render((TableCaption) child, context, html);
            }
        }
//        final int maxCols = findMaxCols(node);
//        final Attributes atts = new AttributesBuilder(TGROUP_ATTS)
//                .add(ATTRIBUTE_NAME_COLS, Integer.toString(maxCols))
//                .build();
//        html.startElement(TOPIC_TGROUP, atts);

//        for (int i = 0; i < maxCols; i++) {
//            final AttributesBuilder catts = new AttributesBuilder(COLSPEC_ATTS)
//                    .add(ATTRIBUTE_NAME_COLNAME, COLUMN_NAME_COL + (i + 1));
////            switch (col.getAlignment()) {
////                case Center:
////                    catts.add(ATTRIBUTE_NAME_ALIGN, "center");
////                    break;
////                case Right:
////                    catts.add(ATTRIBUTE_NAME_ALIGN, "right");
////                    break;
////                case Left:
////                    catts.add(ATTRIBUTE_NAME_ALIGN, "left");
////                    break;
////            }
//            html.startElement(TOPIC_COLSPEC, catts.build());
//            html.endElement(); // colspec
//        }

//            final AttributesBuilder catts = new AttributesBuilder(COLSPEC_ATTS)
//                    .add(ATTRIBUTE_NAME_COLNAME, COLUMN_NAME_COL + counter);
//            switch (col.getAlignment()) {
//                case Center:
//                    catts.add(ATTRIBUTE_NAME_ALIGN, "center");
//                    break;
//                case Right:
//                    catts.add(ATTRIBUTE_NAME_ALIGN, "right");
//                    break;
//                case Left:
//                    catts.add(ATTRIBUTE_NAME_ALIGN, "left");
//                    break;
//            }
//            html.startElement(TOPIC_COLSPEC, catts.build());
//            html.endElement(); // colspec
//        for (final Node child : node.getChildren()) {
//            if (!(child instanceof TableCaptionNode)) {
//                context.renderChildren(child);
//            }
//        }
        context.renderChildren(node);
//        html.endElement(); // tgroup
        html.endElement(); // table
        currentTableNode = null;
    }

    private void renderSimpleTableCaption(final TableCaption node, final NodeRendererContext context, final SaxWriter html) {
        // Pull processed by TableBlock
//        html.startElement(TOPIC_TITLE, TITLE_ATTS);
//        context.renderChildren(node);
//        html.endElement();
    }

//    @Override
//    private void render(final TableColumnNode node, final NodeRendererContext context, final DitaWriter html) {
//        switch (node.getAlignment()) {
//            case None:
//                break;
//            case Left:
//                tableColumnAlignment = "left";
//                break;
//            case Right:
//                tableColumnAlignment = "right";
//                break;
//            case Center:
//                tableColumnAlignment = "center";
//                break;
//            default:
//                throw new ParseException("Unsupported table column alignment: " + node.getAlignment());
//        }
//    }

    private void renderSimpleTableHead(final TableHead node, final NodeRendererContext context, final SaxWriter html) {
//        printTag(node, context, html, TOPIC_STHEAD, STHEAD_ATTS);
        context.renderChildren(node);
    }

    private void renderSimpleTableBody(final TableBody node, final NodeRendererContext context, final SaxWriter html) {
//        html.startElement(TOPIC_TBODY, TBODY_ATTS);
        context.renderChildren(node);
//        html.endElement();
    }

    private void renderSimpleTableSeparator(TableSeparator node, NodeRendererContext context, SaxWriter html) {
        // Ignore
    }

    private void renderSimpleTableRow(final TableRow node, final NodeRendererContext context, final SaxWriter html) {
        currentTableColumn = 0;
        if (node.getParent() instanceof TableHead) {
            printTag(node, context, html, TOPIC_STHEAD, STHEAD_ATTS);
        } else {
            printTag(node, context, html, TOPIC_STROW, STROW_ATTS);
        }
    }

    private void renderSimpleTableCell(final TableCell node, final NodeRendererContext context, final SaxWriter html) {
//        final List<TableColumnNode> columns = currentTableNode.getColumns();
//        final TableColumnNode column = columns.get(Math.min(currentTableColumn, columns.size() - 1));
//
        final AttributesBuilder atts = new AttributesBuilder(STENTRY_ATTS);
//        column.accept(this);
//        if (node.getAlignment() != null) {
//            atts.add(ATTRIBUTE_NAME_ALIGN, node.getAlignment().cellAlignment().name().toLowerCase());
//        }
        if (node.getSpan() > 1) {
//            atts.add(ATTRIBUTE_NAME_NAMEST, COLUMN_NAME_COL + Integer.toString(currentTableColumn + 1));
//            atts.add(ATTRIBUTE_NAME_NAMEEND, COLUMN_NAME_COL + Integer.toString(currentTableColumn + node.getSpan()));
            atts.add(ATTRIBUTE_NAME_COLSPAN, Integer.toString(node.getSpan()));
        }
        html.startElement(TOPIC_STENTRY, atts.build());
        if (isInline(node.getFirstChild())) {
            html.startElement(TOPIC_P, P_ATTS);
            context.renderChildren(node);
            html.endElement();
        } else {
            context.renderChildren(node);
        }
        html.endElement();

        currentTableColumn += node.getSpan();
    }

    private boolean isInline(Node node) {
        return node instanceof Text
                || node instanceof Emphasis
                || node instanceof StrongEmphasis;
    }

    // Code block

    private void render(final CodeBlock node, final NodeRendererContext context, final SaxWriter html) {
        final AttributesBuilder atts = new AttributesBuilder(lwDita ? PRE_ATTS : CODEBLOCK_ATTS)
                .add(XML_NS_URI, "space", "xml:space", "CDATA", "preserve");
//        if (node.getType() != null && !node.getType().isEmpty()) {
//            final String type = node.getType().trim();
//            final Metadata metadata;
//            if (type.startsWith("{")) {
//                metadata = Metadata.parse(type.substring(1, type.length() - 1));
//            } else {
//                metadata = new Metadata(null, Collections.singletonList(type));
//            }
//            if (metadata.id != null) {
//                atts.add(ATTRIBUTE_NAME_ID, metadata.id);
//            }
//            if (!metadata.classes.isEmpty()) {
//                atts.add("outputclass", String.join(" ", metadata.classes));
//            }
//        }
        html.startElement(lwDita ? TOPIC_PRE : PR_D_CODEBLOCK, atts.build());
        String text = node.getChars().toString();
        if (text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }
        html.characters(text);
        html.endElement();
    }

    private void render(final IndentedCodeBlock node, final NodeRendererContext context, final SaxWriter html) {
        final AttributesBuilder atts = new AttributesBuilder(lwDita ? PRE_ATTS : CODEBLOCK_ATTS)
                .add(XML_NS_URI, "space", "xml:space", "CDATA", "preserve");
//        if (node.getType() != null && !node.getType().isEmpty()) {
//            final String type = node.getType().trim();
//            final Metadata metadata;
//            if (type.startsWith("{")) {
//                metadata = Metadata.parse(type.substring(1, type.length() - 1));
//            } else {
//                metadata = new Metadata(null, Collections.singletonList(type));
//            }
//            if (metadata.id != null) {
//                atts.add(ATTRIBUTE_NAME_ID, metadata.id);
//            }
//            if (!metadata.classes.isEmpty()) {
//                atts.add("outputclass", String.join(" ", metadata.classes));
//            }
//        }
        html.startElement(lwDita ? TOPIC_PRE : PR_D_CODEBLOCK, atts.build());
        // FIXME: For compatibility with HTML pre/code, should be removed
        if (lwDita) {
            html.startElement(TOPIC_PH, PH_ATTS);
        }
        String text = node.getContentChars().toString();
        if (text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }
        html.characters(text);
        if (lwDita) {
            html.endElement();
        }
        html.endElement();
    }

    private void render(final FencedCodeBlock node, final NodeRendererContext context, final SaxWriter html) {
        final AttributesBuilder atts = new AttributesBuilder(lwDita ? PRE_ATTS : CODEBLOCK_ATTS)
                .add(XML_NS_URI, "space", "xml:space", "CDATA", "preserve");
//        if (node.getType() != null && !node.getType().isEmpty()) {
//            final String type = node.getType().trim();
//            final Metadata metadata;
//            if (type.startsWith("{")) {
//                metadata = Metadata.parse(type.substring(1, type.length() - 1));
//            } else {
//                metadata = new Metadata(null, Collections.singletonList(type));
//            }
//            if (metadata.id != null) {
//                atts.add(ATTRIBUTE_NAME_ID, metadata.id);
//            }
//            if (!metadata.classes.isEmpty()) {
//                atts.add("outputclass", String.join(" ", metadata.classes));
//            }
//        }


        BasedSequence info = node.getInfo();
        if (info.startsWith("{") && info.endsWith("}")) {
            final Metadata metadata = Metadata.parse(info.subSequence(1, info.length() - 1).toString());
            if (!metadata.classes.isEmpty()) {
                atts.add("outputclass", String.join(" ", metadata.classes));
            }
            if (metadata.id != null) {
                atts.add(ATTRIBUTE_NAME_ID, metadata.id);
            }
        } else if (info.isNotNull() && !info.isBlank()) {
            int space = info.indexOf(' ');
            BasedSequence language;
            if (space == -1) {
                language = info;
            } else {
                language = info.subSequence(0, space);
            }
            atts.add("outputclass", /*context.getDitaOptions().languageClassPrefix +*/ language.unescape());
        } else {
            String noLanguageClass = context.getDitaOptions().noLanguageClass.trim();
            if (!noLanguageClass.isEmpty()) {
                atts.add("outputclass", noLanguageClass);
            }
        }

        html.startElement(lwDita ? TOPIC_PRE : PR_D_CODEBLOCK, atts.build());
        // FIXME: For compatibility with HTML pre/code, should be removed
        if (lwDita) {
            html.startElement(TOPIC_PH, PH_ATTS);
        }
        String text = node.getContentChars().normalizeEOL();
        if (text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }
        html.characters(text);
        if (lwDita) {
            html.endElement();
        }
        html.endElement();
    }

//    @Override
//    private void render(final WikiLinkNode node, final NodeRendererContext context, final DitaWriter html) {
//        String url;
//        try {
//            url = "./" + URLEncoder.encode(node.getChars().toString().replace(' ', '-'), "UTF-8") + ".html";
//        } catch (final UnsupportedEncodingException e) {
//            throw new IllegalArgumentException(e);
//        }
//        final AttributesBuilder atts = getLinkAttributes(url);
//
//        html.startElement(TOPIC_XREF, atts.build());
//        //html.characters(rendering.text);
//        context.renderChildren(node);
//        html.endElement();
//    }

    private void render(final Text node, final NodeRendererContext context, final SaxWriter html) {
        if (abbreviations.isEmpty()) {
            if (node.getParent() instanceof Code) {
                html.characters(node.getChars().toString());
            } else {
                html.characters(node.getChars().unescapeNoEntities());
            }
        } else {
            printWithAbbreviations(node.getChars().toString(), html);
        }
    }

//    @Override
//    private void render(final SpecialText node, final NodeRendererContext context, final DitaWriter html) {
//        html.characters(node.getChars().toString());
//    }

    private void render(final ContentNode node, final NodeRendererContext context, final SaxWriter html) {
        context.renderChildren(node);
    }

    private void render(final SoftLineBreak node, final NodeRendererContext context, final SaxWriter html) {
        html.characters('\n');
    }

    private void render(final HardLineBreak node, final NodeRendererContext context, final SaxWriter html) {
        html.processingInstruction("linebreak", null);
    }

    private void render(final HtmlEntity node, final NodeRendererContext context, final SaxWriter html) {
        final BasedSequence chars = node.getChars();
        final String name = chars.subSequence(1, chars.length() - 1).toString().toLowerCase();
        final String val = Entities.ENTITIES.getProperty(name);
        if (val != null) {
            html.characters(val);
        }
    }

    private void render(final HtmlInlineComment node, final NodeRendererContext context, final SaxWriter html) {
        // Ignore
    }

    private void render(final Node node, final NodeRendererContext context, final SaxWriter html) {
        throw new RuntimeException("No renderer configured for " + node.getNodeName() + " = " + node.getClass().getCanonicalName());
//        if (node instanceof MetadataNode) {
//            final MetadataNode n = (MetadataNode) node;
//            final String id = getId(n.title);
//            html.startElement(TOPIC_TOPIC, new AttributesBuilder(TOPIC_ATTS).add(ATTRIBUTE_NAME_ID, id).build());
//            html.startElement(TOPIC_TITLE, TITLE_ATTS);
//            html.characters(n.title);
//            html.endElement();
//        } else {
//            context.renderChildren(node);
//        }
//        for (final ToDitaSerializerPlugin plugin : plugins) {
//            if (plugin.visit(node, this, printer)) {
//                return;
//            }
//        }
    }

    // helpers

    private boolean hasChildren(final Node node) {
        return node.hasChildren();
//        if (node instanceof SuperNode) {
//            return !node.hasChildren();
//        } else if (node instanceof Text) {
//            return !((Text) node).hasChildren();
//        } else {
//            throw new UnsupportedOperationException();
//        }
    }

//    private void visitChildren(final Node node) {
//        for (final Node child : node.getChildren()) {
//            child.accept(this);
//        }
//    }

    private void printTag(Text node, NodeRendererContext context, SaxWriter html, final DitaClass tag, final Attributes atts) {
        html.startElement(tag, atts);
        html.characters(node.getChars().toString());
        html.endElement();
    }

    private void printTag(Node node, NodeRendererContext context, SaxWriter html, final DitaClass tag, final Attributes atts) {
        html.startElement(tag, atts);
        context.renderChildren(node);
        html.endElement();
    }

    private AttributesBuilder getLinkAttributes(final String href) {
        final AttributesBuilder atts = new AttributesBuilder(XREF_ATTS)
                .add(ATTRIBUTE_NAME_HREF, href);
        if (href.startsWith("#")) {
            atts.add(ATTRIBUTE_NAME_FORMAT, "markdown");
        } else {
            final URI uri = toURI(href);
            String format = null;
            if (uri.getPath() != null) {
                final String ext = FilenameUtils.getExtension(uri.getPath()).toLowerCase();
                switch (ext) {
                    case ATTR_FORMAT_VALUE_DITA:
                    case "xml":
                        format = null;
                        break;
                    // Markdown is converted to DITA
                    case "md":
                    case "markdown":
                        format = "markdown";
                        break;
                    default:
                        format = !ext.isEmpty() ? ext : "html";
                        break;
                }
            }
            if (uri.getScheme() != null && uri.getScheme().equals("mailto")) {
                atts.add(ATTRIBUTE_NAME_FORMAT, "email");
            }
            if (format != null) {
                atts.add(ATTRIBUTE_NAME_FORMAT, format);
            }

            if (uri != null && (uri.isAbsolute() || !uri.isAbsolute() && uri.getPath() != null && uri.getPath().startsWith("/"))) {
                atts.add(ATTRIBUTE_NAME_SCOPE, ATTR_SCOPE_VALUE_EXTERNAL);
            }
        }
        return atts;
    }

    private String normalize(final String string) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch (c) {
                case ' ':
                case '\n':
                case '\t':
                    continue;
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    private void printWithAbbreviations(String string, final SaxWriter html) {
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
                    expansions = new TreeMap<>();
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

                html.characters(string.substring(ix, sx));
                final AttributesBuilder atts = new AttributesBuilder(PH_ATTS);
                if (expansion != null && !expansion.isEmpty()) {
                    atts.add(ATTRIBUTE_NAME_OTHERPROPS, expansion);
                }
                html.startElement(TOPIC_PH, atts.build());
                html.characters(abbr);
                html.endElement();
                ix = sx + abbr.length();
            }
            html.characters(string.substring(ix));
        } else {
            html.characters(string);
        }
    }
}
