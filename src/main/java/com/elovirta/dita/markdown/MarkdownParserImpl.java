package com.elovirta.dita.markdown;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ext.anchorlink.AnchorLink;
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterBlock;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataSet;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.XMLFilterImpl;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Base class for parsing Markdown to DITA.
 */
public class MarkdownParserImpl implements MarkdownParser {

    private final DataSet options;
    private ContentHandler contentHandler;
    private ErrorHandler errorHandler;

    public MarkdownParserImpl(DataSet options) {
        this.options = options;
    }

    @Override
    public void convert(BasedSequence sequence, URI input) throws ParseException {
        final Parser parser = Parser.builder(options).build();
        final Document root = parser.parse(sequence);
        try {
            final Document cleaned = preprocess(root, input);
            validate(cleaned);
            render(cleaned);
        } catch (SAXException e) {
            throw new ParseException(e);
        }
    }

    @Override
    public void setContentHandler(final ContentHandler handler) {
        this.contentHandler = handler;
    }

    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * Render AST to DITA events.
     *
     * @param root document AST
     */
    protected void render(final Document root) {
        ContentHandler res = contentHandler;
        if (DitaRenderer.SPECIALIZATION.get(options)) {
            final XMLFilterImpl specialize = new SpecializeFilter();
            specialize.setContentHandler(res);
            res = specialize;
        } else if (DitaRenderer.SPECIALIZATION_CONCEPT.get(options)) {
            final XMLFilterImpl specialize = new SpecializeFilter(SpecializeFilter.Type.CONCEPT);
            specialize.setContentHandler(res);
            res = specialize;
        } else if (DitaRenderer.SPECIALIZATION_TASK.get(options)) {
            final XMLFilterImpl specialize = new SpecializeFilter(SpecializeFilter.Type.TASK);
            specialize.setContentHandler(res);
            res = specialize;
        } else if (DitaRenderer.SPECIALIZATION_REFERENCE.get(options)) {
            final XMLFilterImpl specialize = new SpecializeFilter(SpecializeFilter.Type.REFERENCE);
            specialize.setContentHandler(res);
            res = specialize;
        }
        final DitaRenderer s = new DitaRenderer(options);
        s.render(root, res);
    }

    /**
     * Validate AST.
     *
     * @param root document AST
     * @throws ParseException if document is not valid
     */
    protected void validate(Document root) {
        final boolean mditaCoreProfile = DitaRenderer.MDITA_CORE_PROFILE.getFrom(options)
                || DitaRenderer.LW_DITA.getFrom(options);
        final boolean mditaExtendedProfile = DitaRenderer.MDITA_EXTENDED_PROFILE.getFrom(options);

        int level = 0;
        Node node = root.getFirstChild();
        while (node != null) {
            if (node instanceof Heading) {
                Heading heading = (Heading) node;
                if ((mditaCoreProfile || mditaExtendedProfile) && heading.getLevel() > 2) {
                    throw new ParseException(String.format("LwDITA does not support level %d heading: %s", heading.getLevel(), heading.getText()));
                }
                if (heading.getLevel() > level + 1) {
                    throw new ParseException(String.format("Heading level raised from %d to %d without intermediate heading level", level, heading.getLevel()));
                }
                level = heading.getLevel();
            }
            node = node.getNext();
        }
    }

    /**
     * Preprocess AST before validation and rendition.
     * <p>
     * If document doesn't start with H1, generate H1 from YAML metadata or file name.
     */
    protected Document preprocess(Document root, URI input) throws SAXException {
        if (DitaRenderer.WIKI.get(options) && isWiki(root)) {
            generateRootHeading(root, input);
        }
        if (DitaRenderer.FIX_ROOT_HEADING.get(options) && isWiki(root)) {
            if (errorHandler != null) {
                errorHandler.warning(new SAXParseException("Document content doesn't start with heading", null, input.toString(), 1, 1));
            }
            generateRootHeading(root, input);
        }
        return root;
    }

    private void generateRootHeading(Document root, URI input) {
        final YamlFrontMatterBlock yaml = root.getFirstChild() instanceof YamlFrontMatterBlock
                ? (YamlFrontMatterBlock) root.getFirstChild()
                : null;
        String title = getTextFromFile(input);
        final Heading heading = new Heading();
        if (yaml != null) {
            final AbstractYamlFrontMatterVisitor v = new AbstractYamlFrontMatterVisitor();
            v.visit(root);
            final Map<String, List<String>> metadata = v.getData();
            final List<String> ids = metadata.get("id");
            if (ids != null && !ids.isEmpty()) {
                heading.setAnchorRefId(ids.get(0));
            }
            final List<String> titles = metadata.get("title");
            if (titles != null && !titles.isEmpty()) {
                title = titles.get(0);
                if ((title.charAt(0) == '\'' && title.charAt(title.length() - 1) == '\'') ||
                        (title.charAt(0) == '"' && title.charAt(title.length() - 1) == '"')) {
                    title = title.substring(1, title.length() - 1);
                }
            }
        }
        heading.setLevel(1);
        final AnchorLink anchorLink = new AnchorLink();
        anchorLink.appendChild(new Text(title));
        heading.appendChild(anchorLink);
        root.prependChild(heading);
    }

    /**
     * Check if document doesn't start with H1, excluding optional YAML header.
     */
    private static boolean isWiki(Document root) {
        Node firstChild = root.getFirstChild();
        if (firstChild == null) {
            return false;
        }
        if (firstChild instanceof YamlFrontMatterBlock) {
            firstChild = firstChild.getNext();
        }
        return !(firstChild instanceof Heading && ((Heading) firstChild).getLevel() == 1);
    }

    /**
     * Filename to title.
     */
    private String getTextFromFile(URI file) {
        final String path = file.getPath();
        final String name = path.substring(path.lastIndexOf("/") + 1);
        final String title = name.lastIndexOf(".") != -1
                ? name.substring(0, name.lastIndexOf("."))
                : name;
        return title.replace('_', ' ').replace('-', ' ');
    }
}
