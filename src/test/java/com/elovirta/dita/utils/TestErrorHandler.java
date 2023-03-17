package com.elovirta.dita.utils;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.List;

public class TestErrorHandler implements ErrorHandler {
    public final List<SAXParseException> warnings = new ArrayList<>();
    public final List<SAXParseException> errors = new ArrayList<>();
    public final List<SAXParseException> fatalErrors = new ArrayList<>();

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        warnings.add(exception);
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        errors.add(exception);
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        fatalErrors.add(exception);
    }
}
