package com.meterware.httpunit.parsing;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2002, Russell Gold
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 *******************************************************************************************************************/
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;
import org.apache.xerces.xni.XNIException;

import org.cyberneko.html.HTMLConfiguration;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

import java.net.URL;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;

import com.meterware.httpunit.scripting.ScriptableDelegate;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 * @author <a href="mailto:bw@xmlizer.biz">Bernhard Wagner</a>
 * @author <a href="mailto:Artashes.Aghajanyan@lycos-europe.com">Artashes Aghajanyan</a>
 **/
class NekoHTMLParser implements HTMLParser {


    public void parse( URL pageURL, String pageText, DocumentAdapter adapter ) throws IOException, SAXException {
        try {
            DOMParser parser = DOMParser.newParser( adapter, pageURL );
            parser.parse( new InputSource( new StringReader( pageText ) ) );
            adapter.setRootNode( parser.getDocument() );
        } catch (DOMParser.ScriptException e) {
             throw e.getException();
        }
    }


    public String getCleanedText( String string ) {
        return (string == null) ? "" : string.replace( NBSP, ' ' );
    }


    public boolean supportsPreserveTagCase() {
        return true;
    }


    public boolean supportsReturnHTMLDocument() {
        return true;
    }


    public boolean supportsParserWarnings() {
        return true;
    }


    final private static char NBSP = (char) 160;   // non-breaking space, defined by nekoHTML
}


class DOMParser extends org.apache.xerces.parsers.DOMParser {

    private static final String HTML_DOCUMENT_CLASS_NAME = "org.apache.html.dom.HTMLDocumentImpl";

    /** Error reporting feature identifier. */
    private static final String REPORT_ERRORS = "http://cyberneko.org/html/features/report-errors";

    /** Augmentations feature identifier. */
    private static final String AUGMENTATIONS = "http://cyberneko.org/html/features/augmentations";

    /** Filters property identifier. */
    private static final String FILTERS = "http://cyberneko.org/html/properties/filters";

    /** Element case settings. possible values: "upper", "lower", "match" */
    private static final String TAG_NAME_CASE = "http://cyberneko.org/html/properties/names/elems";

    /** Attribute case settings. possible values: "upper", "lower", "no-change" */
    private static final String ATTRIBUTE_NAME_CASE = "http://cyberneko.org/html/properties/names/attrs";

    private DocumentAdapter _documentAdapter;
    private ScriptableDelegate _scriptableObject;


    static DOMParser newParser( DocumentAdapter adapter, URL url ) {
        final HTMLConfiguration configuration = new HTMLConfiguration();
        if (!HTMLParserFactory.getHTMLParserListeners().isEmpty() || HTMLParserFactory.isParserWarningsEnabled()) {
            configuration.setErrorHandler( new ErrorHandler( url ) );
            configuration.setFeature( REPORT_ERRORS, true);
        }
        configuration.setFeature( AUGMENTATIONS, true );
        final ScriptFilter javaScriptFilter = new ScriptFilter( configuration );
        configuration.setProperty( FILTERS, new XMLDocumentFilter[] { javaScriptFilter } );
        if (HTMLParserFactory.isPreserveTagCase()) {
            configuration.setProperty( TAG_NAME_CASE, "match" );
            configuration.setProperty( ATTRIBUTE_NAME_CASE, "no-change" );
        }

        try {
            final DOMParser domParser = new DOMParser( configuration, adapter );
            domParser.setFeature( DEFER_NODE_EXPANSION, false );
            if (HTMLParserFactory.isReturnHTMLDocument()) domParser.setProperty( DOCUMENT_CLASS_NAME, HTML_DOCUMENT_CLASS_NAME );
            javaScriptFilter.setParser( domParser );
            return domParser;
        } catch (SAXNotRecognizedException e) {
            throw new RuntimeException( e.toString() );
        } catch (SAXNotSupportedException e) {
            throw new RuntimeException( e.toString() );
        }

    }


    ScriptableDelegate getScriptableDelegate() {
        if (_scriptableObject == null) {
            Node node = getCurrentElementNode();
            while (!(node instanceof Document)) node = node.getParentNode();
            _documentAdapter.setRootNode( node );
            _scriptableObject = _documentAdapter.getScriptableObject();
        }
        return _scriptableObject;
    }


    private Node getCurrentElementNode() {
        try {
            final Node node = (Node) getProperty( CURRENT_ELEMENT_NODE );
            return node;
        } catch (SAXNotRecognizedException e) {
            throw new RuntimeException( CURRENT_ELEMENT_NODE + " property not recognized" );
        } catch (SAXNotSupportedException e) {
            e.printStackTrace();
            throw new RuntimeException( CURRENT_ELEMENT_NODE + " property not supported" );
        }
    }


    String getIncludedScript( String srcAttribute ) {
        try {
            return _documentAdapter.getIncludedScript( srcAttribute );
        } catch (IOException e) {
            throw new ScriptException( e );
        }
    }


    DOMParser( HTMLConfiguration configuration, DocumentAdapter adapter ) {
        super( configuration );
        _documentAdapter = adapter;
    }


    static class ScriptException extends RuntimeException {
        private IOException _cause;

        public ScriptException( IOException cause ) {
            _cause = cause;
        }

        public IOException getException() {
            return _cause;
        }
    }
}


class ErrorHandler implements XMLErrorHandler {

    private URL _url = null;

    ErrorHandler( URL url ) {
        _url = url;
    }

    public void warning( String domain, String key, XMLParseException warningException ) throws XNIException {
        if (HTMLParserFactory.isParserWarningsEnabled()) {
            System.out.println( "At line " + warningException.getLineNumber() + ", column " + warningException.getColumnNumber() + ": " + warningException.getMessage() );
        }

        Enumeration enum = HTMLParserFactory.getHTMLParserListeners().elements();
        while (enum.hasMoreElements()) {
            ((HTMLParserListener) enum.nextElement()).warning( _url, warningException.getMessage(), warningException.getLineNumber(), warningException.getColumnNumber() );
        }
    }


    public void error( String domain, String key, XMLParseException errorException ) throws XNIException {
        Enumeration enum = HTMLParserFactory.getHTMLParserListeners().elements();
        while (enum.hasMoreElements()) {
            ((HTMLParserListener) enum.nextElement()).error( _url, errorException.getMessage(), errorException.getLineNumber(), errorException.getColumnNumber() );
        }
    }


    public void fatalError( String domain, String key, XMLParseException fatalError ) throws XNIException {
        error( domain, key, fatalError );
        throw fatalError;
    }
}
