package com.meterware.httpunit;
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
 **/
class NekoHTMLParser implements HTMLParser {


    public void parse( HTMLPage page, URL pageURL, String pageText ) throws IOException, SAXException {
        try {
            DOMParser parser = DOMParser.newParser(page);
            parser.parse( new InputSource( new StringReader( pageText ) ) );
            page.setRootNode( parser.getDocument() );
        } catch (DOMParser.ScriptException e) {
             throw e.getException();
        }
    }


    public String getCleanedText( String string ) {
        return (string == null) ? "" : string.replace( NBSP, ' ' );
    }

    final private static char NBSP = (char) 160;   // non-breaking space, defined by nekoHTML
}


class DOMParser extends org.apache.xerces.parsers.DOMParser {

    private static final String HTML_DOCUMENT_CLASS_NAME = "org.apache.html.dom.HTMLDocumentImpl";

    /** Augmentations feature identifier. */
    protected static final String AUGMENTATIONS = "http://cyberneko.org/html/features/augmentations";

    /** Filters property identifier. */
    protected static final String FILTERS = "http://cyberneko.org/html/properties/filters";


    private HTMLPage _htmlPage;
    private ScriptableDelegate _scriptableObject;


    static DOMParser newParser( HTMLPage page ) {
        final HTMLConfiguration configuration = new HTMLConfiguration();
        configuration.setFeature( AUGMENTATIONS, true );
        final ScriptFilter javaScriptFilter = new ScriptFilter( configuration );
        configuration.setProperty( FILTERS, new XMLDocumentFilter[] { javaScriptFilter } );
        final DOMParser domParser = new DOMParser( configuration, page );
        javaScriptFilter.setParser( domParser );
        return domParser;
    }


    ScriptableDelegate getScriptableDelegate() {
        if (_scriptableObject == null) {
            Node node = getCurrentElementNode();
            while (!(node instanceof Document)) node = node.getParentNode();
            _htmlPage.setRootNode( node );
            _scriptableObject = _htmlPage.getScriptableObject().getParent();
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
            return _htmlPage.getIncludedScript( srcAttribute );
        } catch (IOException e) {
            throw new ScriptException( e );
        }
    }


    DOMParser( HTMLConfiguration configuration, HTMLPage page ) {
        super( configuration );
        _htmlPage = page;

        try {
            setFeature( DEFER_NODE_EXPANSION, false );
            setProperty( DOCUMENT_CLASS_NAME, HTML_DOCUMENT_CLASS_NAME );
        } catch (SAXNotRecognizedException e) {
            throw new RuntimeException( e.toString() );
        } catch (SAXNotSupportedException e) {
            throw new RuntimeException( e.toString() );
        }
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
