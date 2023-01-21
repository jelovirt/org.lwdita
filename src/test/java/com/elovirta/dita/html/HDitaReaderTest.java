package com.elovirta.dita.html;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xml.sax.XMLReader;

public class HDitaReaderTest extends HtmlReaderTest {

    @Override
    public XMLReader getReader() {
        return new HDitaReader();
    }

    @Override
    public String getSrc() {
        return "hdita/";
    }

    @Override
    public String getExp() {
        return "xdita/";
    }

    @Test
    public void testHdita() throws Exception {
        run("hdita.html");
    }

    @Test
    public void testMultipleTopLevel() throws Exception {
        run("multiple_top_level.html");
    }

    @Disabled
    @Test
    public void testheader_attributes() throws Exception {
        run("header_attributes.html");
    }

    @Disabled
    @Test
    public void testbody_attributes() throws Exception {
        run("body_attributes.html");
    }

    @Disabled("specialization not supported by LwDITA")
    @Test
    public void testMultipleTopLevelSpecialized() throws Exception {
        run("multiple_top_level_specialized.html");
    }

    @Disabled
    @Test
    public void testheader() throws Exception {
        run("header.html");
    }

    @Disabled("specialization not supported by LwDITA")
    @Test
    public void testreference() throws Exception {
        run("reference.html");
    }

    @Disabled("specialization not supported by LwDITA")
    @Test
    public void testconcept() throws Exception {
        run("concept.html");
    }

    @Disabled("specialization not supported by LwDITA")
    @Test
    public void testTask() throws Exception {
        run("task.html");
    }

    @Disabled("specialization not supported by LwDITA")
    @Test
    public void testTaskOneStep() throws Exception {
        run("taskOneStep.html");
    }
}