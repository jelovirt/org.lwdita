package com.elovirta.dita.markdown;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class SpecializeFilterTest {

    private final SAXParserFactory parserFactory = SAXParserFactory.newInstance();
    private final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    @BeforeEach
    public void setUp() {
        documentBuilderFactory.setNamespaceAware(true);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "concept",
            "reference",
            "task",
            "task_cmd_with_info",
            "task_context",
            "task_context_with_two_p",
            "task_two_p_in_info",
            "task_inline_in_cmd"
    })
    public void test(String name) throws Exception {
        final SpecializeFilter filter = new SpecializeFilter();
        filter.setParent(parserFactory.newSAXParser().getXMLReader());
        final Transformer transformer = transformerFactory.newTransformer();
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document act = documentBuilder.newDocument();
        try (InputStream srcIn = getClass().getResourceAsStream("/specialize/src/" + name + ".dita");
             InputStream expIn = getClass().getResourceAsStream("/specialize/exp/" + name + ".dita")) {
            transformer.transform(new SAXSource(filter, new InputSource(srcIn)), new DOMResult(act));
            final Document exp = documentBuilder.parse(expIn);
            final Diff diff = DiffBuilder
                    .compare(act)
                    .withTest(exp)
                    .normalizeWhitespace()
                    .ignoreWhitespace()
                    .checkForIdentical()
                    .build();
            if (diff.hasDifferences()) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.transform(new DOMSource(act), new StreamResult(System.out));
            }
            assertFalse(diff.hasDifferences());
        }
    }
}