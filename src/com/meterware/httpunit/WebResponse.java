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
import java.util.StringTokenizer;
import org.xml.sax.*;
import org.w3c.dom.*;

/**
 * A response from a web server to an Http request.
 **/
public class WebResponse {


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
     * @exception SAXException thrown if there is an error parsing the response.
     **/
    public Document getDOM() throws SAXException {
        return (Document) getReceivedPage().getDOM();
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


    /**
     * Constructs a response object from an input stream.
     * @param conversation the web conversation which received the response
     * @param url the url from which the response was received
     * @param inputStream the input stream from which the response can be read
     **/
    WebResponse( WebConversation conversation, URL url, URLConnection connection ) {
        _conversation = conversation;
        _url = url;
        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader input = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );

            String str;
            while (null != ((str = input.readLine()))) {
                sb.append( str ).append( endOfLine );
            }
            input.close ();
            _responseText = sb.toString();
            readHeaders( connection );
        } catch (IOException e) {
            throw new RuntimeException( "Unable to retrieve data from URL: " + _url.toExternalForm() );
        }
    }


    /**
     * Returns the web conversation of which this response is a part, and which contains the session
     * context information required for further requests.
     **/
    WebConversation getWebConversation() {
        return _conversation;
    }


//--------------------------------- private members --------------------------------------


    final private static String endOfLine = System.getProperty( "line.separator" );

    private ReceivedPage _page;

    private String _responseText;

    private URL    _url;

    private WebConversation _conversation;

    private String _contentType = "text/plain";

    private String _characterSet = "us-ascii";


    private ReceivedPage getReceivedPage() throws SAXException {
        if (_page == null) {
            if (!_contentType.equals( "text/html" )) throw new RuntimeException( "Response is not HTML" );
            _page = new ReceivedPage( _url, _responseText );
        }
        return _page;
    }


    private void readHeaders( URLConnection connection ) {
        readContentTypeHeader( connection );
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

