package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2000, Russell Gold
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

import java.io.*;
import java.net.*;
import java.util.*;
import org.xml.sax.*;
import org.w3c.dom.*;

import java.lang.reflect.*;

/**
 * A response from a web server to an Http request.
 **/
public class WebResponse {


    /**
     * Returns true if the response is HTML.
     **/
    public boolean isHTML() {
        return _contentType.equals( HTML_CONTENT );
    }


    /**
     * Returns the response code associated with this response.
     **/
    public int getResponseCode() {
        return _responseCode;
    }


    /**
     * Returns the URL which invoked this response.
     **/
    public URL getURL() {
        return _url;
    }


    /**
     * Returns the content type of this response.
     **/
    public String getContentType() {
        return _contentType;
    }


    /**
     * Returns the character set used in this response.
     **/
    public String getCharacterSet() {
        return _characterSet;
    }


    /**
     * Returns the title of the page.
     **/
    public String getTitle() throws SAXException {
        return getReceivedPage().getTitle();
    }


    /**
     * Returns the target of the page.
     **/
    public String getTarget() throws SAXException {
        return _target;
    }


    /**
     * Returns the names of the frames found in the page in the order in which they appear.
     **/
    public String[] getFrameNames() throws SAXException {
        WebFrame[] frames = getFrames();
        String[] result = new String[ frames.length ];
        for (int i = 0; i < result.length; i++) {
            result[i] = frames[i].getName();
        }

        return result;
    }


    /**
     * Returns the forms found in the page in the order in which they appear.
     * @exception SAXException thrown if there is an error parsing the response.
     **/
    public WebForm[] getForms() throws SAXException {
        return getReceivedPage().getForms();
    }


    /**
     * Returns the links found in the page in the order in which they appear.
     * @exception SAXException thrown if there is an error parsing the response.
     **/
    public WebLink[] getLinks() throws SAXException {
        return getReceivedPage().getLinks();
    }


    /**
     * Returns the first link which contains the specified text.
     **/
    public WebLink getLinkWith( String text ) throws SAXException {
        return getReceivedPage().getLinkWith( text );
    }


    /**
     * Returns the first link which contains an image with the specified text as its 'alt' attribute.
     **/
    public WebLink getLinkWithImageText( String text ) throws SAXException {
        return getReceivedPage().getLinkWithImageText( text );
    }


    /**
     * Returns the top-level tables found in this page in the order in which
     * they appear.
     * @exception SAXException thrown if there is an error parsing the response.
     **/
    public WebTable[] getTables() throws SAXException {
        return getReceivedPage().getTables();
    }


    /**
     * Returns a copy of the domain object model tree associated with this response.
     * If the response is HTML, it will use a special parser which can transform HTML into an XML DOM.
     * @exception SAXException thrown if there is an error parsing the response.
     **/
    public Document getDOM() throws SAXException {
        if (isHTML()) {
            return (Document) getReceivedPage().getDOM();
        } else {
            return getXMLDOM();
        }
    }


    /**
     * Returns the first table in the response which has the specified text as the full text of
     * its first non-blank row and non-blank column. Will recurse into any nested tables, as needed.
     * Case is ignored.
     * @exception SAXException thrown if there is an error parsing the response.
     * @return the selected table, or null if none is found
     **/
    public WebTable getTableStartingWith( String text ) throws SAXException {
        return getReceivedPage().getTableStartingWith( text );
    }
    
    
    /**
     * Returns the first table in the response which has the specified text as a prefix of the text of
     * its first non-blank row and non-blank column. Will recurse into any nested tables, as needed.
     * Case is ignored.
     * @exception SAXException thrown if there is an error parsing the response.
     * @return the selected table, or null if none is found
     **/
    public WebTable getTableStartingWithPrefix( String text ) throws SAXException {
        return getReceivedPage().getTableStartingWithPrefix( text );
    }


    /**
     * Returns the first table in the response which has the specified text as its summary attribute. 
     * Will recurse into any nested tables, as needed.
     * Case is ignored.
     * @exception SAXException thrown if there is an error parsing the response.
     * @return the selected table, or null if none is found
     **/
    public WebTable getTableWithSummary( String text ) throws SAXException {
        return getReceivedPage().getTableWithSummary( text );
    }


    /**
     * Returns the text of the response (excluding headers) as a string. Use this method in preference to 'toString'
     * which may be used to represent internal state of this object.
     **/
    public String getText() {
        return _responseText;
    }
    
    
    public String toString() {
        return _responseText;
    }


//---------------------------------- package members --------------------------------


    final static WebResponse BLANK_RESPONSE = new WebResponse( "<html><head></head><body></body></html>" );


    /**
     * Constructs a response object from an input stream.
     * @param conversation the web conversation which received the response
     * @param url the url from which the response was received
     * @param inputStream the input stream from which the response can be read
     **/
    WebResponse( WebConversation conversation, String target, URL url, URLConnection connection ) {
        this( conversation, target, url, getResponseText( url, connection ) );
        readHeaders( connection );
    }


    /**
     * Returns the web conversation of which this response is a part, and which contains the session
     * context information required for further requests.
     **/
    WebConversation getWebConversation() {
        return _conversation;
    }


    /**
     * Returns the frames found in the page in the order in which they appear.
     **/
    WebRequest[] getFrameRequests() throws SAXException {
        WebFrame[] frames = getFrames();
        Vector requests = new Vector();
        for (int i = 0; i < frames.length; i++) {
            if (frames[i].hasInitialRequest()) {
                requests.addElement( frames[i].getInitialRequest() );
            }
        }

        WebRequest[] result = new WebRequest[ requests.size() ];
        requests.copyInto( result );
        return result;
    }


//--------------------------------- private members --------------------------------------



    final private static String endOfLine = System.getProperty( "line.separator" );


    final private static String HTML_CONTENT = "text/html";

    private WebFrame[] _frames;

    private ReceivedPage _page;

    private String _responseText;

    private String _contentType = "text/plain";

    private String _characterSet = "us-ascii";

    private int    _responseCode;


    // the following variables are essentially final; however, the JDK 1.1 compiler does not handle final variables properly with
    // multiple constructors that call each other, so the final qualifiers have been removed.

    private URL    _url;

    private WebConversation _conversation;

    private String _target;


    /**
     * Constructs a response object from a text response.
     **/
    private WebResponse( String responseText ) {
        this( null, "", null, responseText );
        _contentType = HTML_CONTENT;
    }


    /**
     * Constructs a response object.
     * @param conversation the web conversation which received the response
     * @param url the url from which the response was received
     * @param inputStream the input stream from which the response can be read
     **/
    private WebResponse( WebConversation conversation, String target, URL url, String responseText ) {
        _conversation = conversation;
        _url = url;
        _target = target;
        _responseText = responseText;
    }


    private static String getResponseText( URL url, URLConnection connection ) {
        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader input = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );

            String str;
            while (null != ((str = input.readLine()))) {
                sb.append( str ).append( endOfLine );
            }
            input.close ();
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException( "Unable to retrieve data from URL: " + url.toExternalForm() + " (" + e + ")" );
        }
    }


    private WebFrame[] getFrames() throws SAXException {
        if (_frames == null) {
            NodeList nl = NodeUtils.getElementsByTagName( getReceivedPage().getDOM(), "frame" );
            Vector list = new Vector();
            for (int i = 0; i < nl.getLength(); i++) {
                Node child = nl.item(i);
                list.addElement( new WebFrame( getReceivedPage().getBaseURL(), child ) );
            }
            _frames = new WebFrame[ list.size() ];
            list.copyInto( _frames );
        }

        return _frames;
    }


    private ReceivedPage getReceivedPage() throws SAXException {
        if (_page == null) {
            if (!isHTML()) throw new RuntimeException( "Response is not HTML" );
            _page = new ReceivedPage( _url, _target, _responseText );
        }
        return _page;
    }


    private Document getXMLDOM() throws SAXException {
        Document doc = null;

        try {
            Class parserClass = Class.forName("org.apache.xerces.parsers.DOMParser");
            Constructor constructor = parserClass.getConstructor( null );
            Object parser = constructor.newInstance( null );

            Class[] parseMethodArgTypes = { InputSource.class };
            Object[] parseMethodArgs = { new InputSource( new StringReader( _responseText ) ) };
            Method parseMethod = parserClass.getMethod( "parse", parseMethodArgTypes );
            parseMethod.invoke( parser, parseMethodArgs );

            Method getDocumentMethod = parserClass.getMethod( "getDocument", null );
            doc = (Document)getDocumentMethod.invoke( parser, null );
        } catch (InvocationTargetException ex) {
            Throwable tex = ex.getTargetException();
            if (tex  instanceof SAXException) {
                throw (SAXException)tex;
            } else if (tex instanceof IOException) {
                throw new RuntimeException( tex.toString() );
            } else {
                throw new IllegalStateException( "unexpected exception" );
            }
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException( "parse method not found" );
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException( "parse method not public" );
        } catch (InstantiationException ex) {
            throw new IllegalStateException( "error instantiating parser" );
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException( "parser class not found" );
        }

        return doc;
   }


    private void readHeaders( URLConnection connection ) {
        readContentTypeHeader( connection );
        try {
            if (connection instanceof HttpURLConnection) {
                _responseCode = ((HttpURLConnection) connection).getResponseCode();
            } else {
                _responseCode = HttpURLConnection.HTTP_OK;
            }
        } catch (IOException e) {
        }
    }


    private void readContentTypeHeader( URLConnection connection ) {
        String contentHeader = connection.getContentType();
        if (contentHeader != null) {
            StringTokenizer st = new StringTokenizer( contentHeader, ";=" );
            _contentType = st.nextToken();
            while (st.hasMoreTokens()) {
                String parameter = st.nextToken();
                if (st.hasMoreTokens()) {
                    String value = st.nextToken();
                    if (parameter.equalsIgnoreCase( "charset" )) _characterSet = value;
                }
            }
        }
    }

}

