package com.meterware.httpunit;

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
     **/
    public WebForm[] getForms() throws SAXException {
        return getReceivedPage().getForms();
    }


    /**
     * Returns the links found in the page in the order in which they appear.
     **/
    public WebLink[] getLinks() throws SAXException {
        return getReceivedPage().getLinks();
    }


    /**
     * Returns a copy of the domain object model tree associated with this response.
     **/
    public Document getDOM() throws SAXException {
        return getReceivedPage().getDOM();
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
    WebResponse( WebConversation conversation, URL url, InputStream inputStream ) throws IOException {
        _conversation = conversation;
        _url = url;
        StringBuffer sb = new StringBuffer();
        BufferedReader input = new BufferedReader( new InputStreamReader( inputStream ) );

        String str;
        while (null != ((str = input.readLine()))) {
            sb.append( str ).append( endOfLine );
        }
        input.close ();
        _responseText = sb.toString();
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