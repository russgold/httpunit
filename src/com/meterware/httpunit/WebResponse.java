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
import org.xml.sax.*;
import org.w3c.dom.*;

/**
 * A response from a web server to an Http request.
 **/
public class WebResponse {


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
     * Returns the first table in the response which has the specified text in its first non-blank row and
     * non-blank column. Will recurse into any nested tables, as needed.
     * @exception SAXException thrown if there is an error parsing the response.
     * @return the selected table, or null if none is found
     **/
    public WebTable getTableStartingWith( String text ) throws SAXException {
        return getReceivedPage().getTableStartingWith( text );
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
    WebResponse( WebConversation conversation, URL url, InputStream inputStream ) {
        _conversation = conversation;
        _url = url;
        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader input = new BufferedReader( new InputStreamReader( inputStream ) );

            String str;
            while (null != ((str = input.readLine()))) {
                sb.append( str ).append( endOfLine );
            }
            input.close ();
            _responseText = sb.toString();
        } catch (IOException e) {
            throw new RuntimeException( "Unable to retrieve URL: " + _url.toExternalForm() );
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


    private ReceivedPage getReceivedPage() throws SAXException {
        if (_page == null) {
            _page = new ReceivedPage( _url, _responseText );
        }
        return _page;
    }

}

