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


/**
 * The context for a series of HTTP requests. This class manages cookies used to maintain
 * session context, computes relative URLs, and generally emulates the browser behavior
 * needed to build an automated test of a web site.
 **/
public class WebConversation {

    
    /**
     * Creates a new web conversation.
     **/
    public WebConversation() {
    }


    /**
     * Submits a web request and returns a response, using all state developed so far as stored in
     * cookies as requested by the server.
     **/
    public WebResponse getResponse( WebRequest request ) throws MalformedURLException, IOException {
        URLConnection connection = openConnection( request.getURL() );
        request.completeRequest( connection );
        updateCookies( connection );
        
        if (connection.getHeaderField( "Location" ) != null) {
            return getResponse( new RedirectWebRequest( request, connection.getHeaderField( "Location" ) ) );
        } else {
            WebResponse result = new WebResponse( this, request.getURL(), connection.getInputStream() );
            return result;
        }
    }


//---------------------------------- private members --------------------------------


    private Hashtable _cookies = new Hashtable();
    
    static {
        HttpURLConnection.setFollowRedirects( false );
    }

    private URLConnection openConnection( URL url ) throws MalformedURLException, IOException {
        URLConnection connection = url.openConnection();
        connection.setUseCaches( false );
        sendCookies( connection );
        return connection;
    }

    
    private void sendCookies( URLConnection connection ) {
    	if (_cookies.size() == 0) return;
    	
    	StringBuffer sb = new StringBuffer();
    	for (Enumeration e = _cookies.keys(); e.hasMoreElements();) {
    		String name = (String) e.nextElement();
    		sb.append( name ).append( '=' ).append( _cookies.get( name ) );
    		if (e.hasMoreElements()) sb.append( ';' );
    	}
    	connection.setRequestProperty( "Cookie", sb.toString() );
    }
    
    
    private void updateCookies( URLConnection connection ) {
        for (int i = 1; true; i++) {
            String key = connection.getHeaderFieldKey( i );
            if (key == null) break;
            if (!key.equalsIgnoreCase( "Set-Cookie" )) continue;
            StringTokenizer st = new StringTokenizer( connection.getHeaderField( i ), "=;" );
            String name = st.nextToken();
            String value = st.nextToken();
            _cookies.put( name, value );
        };
    }
}



class RedirectWebRequest extends WebRequest {

    RedirectWebRequest( WebRequest baseRequest, String relativeURL ) throws MalformedURLException {
        super( baseRequest, relativeURL );
    }


    public URL getURL() throws MalformedURLException {
        return new URL( getURLBase(), getURLString() );
    }
    
    
}
