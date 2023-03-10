package com.elovirta.dita.markdown;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ext.anchorlink.AnchorLink;
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterBlock;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class BaseMarkdownParser implements MarkdownParser {

    private final MutableDataSet options;
    private ContentHandler contentHandler;

    BaseMarkdownParser(MutableDataSet options) {
        this.options = options;
    }

    @Override
    public void convert(BasedSequence sequence, URI input) throws ParseException {
        final Parser parser = Parser.builder(options).build();
        final Document root = parser.parse(sequence);
        final Document cleaned = generateMissingHeader(root, input);
        validate(cleaned);
        parseAST(cleaned);
    }

    @Override
    public void setContentHandler(final ContentHandler handler) {
        this.contentHandler = handler;
    }

    private void parseAST(final Document root) {
        ContentHandler res = contentHandler;
        if (DitaRenderer.SPECIALIZATION.get(options)) {
            final XMLFilterImpl specialize = new SpecializeFilter();
            specialize.setContentHandler(res);
            res = specialize;
        }
        final DitaRenderer s = new DitaRenderer(options);
        s.render(root, res);
    }

    private void validate(Document root) {
        final boolean lwdita = DitaRenderer.LW_DITA.getFrom(options);

        int level = 0;
        Node node = root.getFirstChild();
        while (node != null) {
            if (node instanceof Heading) {
                Heading heading = (Heading) node;
                if (lwdita && heading.getLevel() > 2) {
                    throw new ParseException(String.format("LwDITA does not support level %d header: %s", heading.getLevel(), heading.getText()));
                }
                if (heading.getLevel() > level + 1) {
                    throw new ParseException(String.format("Header level raised from %d to %d without intermediate header level", level, heading.getLevel()));
                }
                level = heading.getLevel();
            }
            node = node.getNext();
        }
    }

    /**
     * If document doesn't start with H1, generate H1 from YAML metadata or file name.
     */
    private Document generateMissingHeader(Document root, URI input) {
        if (DitaRenderer.WIKI.get(options) && isWiki(root)) {
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
        return root;
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
