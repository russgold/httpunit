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

import com.meterware.httpunit.scripting.ScriptableDelegate;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 * @author <a href="mailto:bw@xmlizer.biz">Bernhard Wagner</a>
 **/
class NekoHTMLParser implements HTMLParser {


    public void parse( URL pageURL, String pageText, DocumentAdapter adapter ) throws IOException, SAXException {
        try {
            DOMParser parser = DOMParser.newParser( adapter );
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


    final private static char NBSP = (char) 160;   // non-breaking space, defined by nekoHTML
}


class DOMParser extends org.apache.xerces.parsers.DOMParser {

    private static final String HTML_DOCUMENT_CLASS_NAME = "org.apache.html.dom.HTMLDocumentImpl";

    /** Augmentations feature identifier. */
    protected static final String AUGMENTATIONS = "http://cyberneko.org/html/features/augmentations";

    /** Filters property identifier. */
    protected static final String FILTERS = "http://cyberneko.org/html/properties/filters";

    /** Element case settings. possible values: "upper", "lower", "match" */
    protected static final String ELEMS = "http://cyberneko.org/html/properties/names/elems";

    /** Attribute case settings. possible values: "upper", "lower", "no-change" */
    protected static final String ATTRS = "http://cyberneko.org/html/properties/names/attrs";


    private DocumentAdapter _documentAdapter;
    private ScriptableDelegate _scriptableObject;


    static DOMParser newParser( DocumentAdapter adapter ) {
        final HTMLConfiguration configuration = new HTMLConfiguration();
        configuration.setFeature( AUGMENTATIONS, true );
        final ScriptFilter javaScriptFilter = new ScriptFilter( configuration );
        configuration.setProperty( FILTERS, new XMLDocumentFilter[] { javaScriptFilter } );
        if (HTMLParserFactory.isPreserveTagCase()) {
            configuration.setProperty( ELEMS, "match" );
            configuration.setProperty( ATTRS, "no-change" );
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
            _scriptableObject = _documentAdapter.getScriptableObject().getParent();
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
