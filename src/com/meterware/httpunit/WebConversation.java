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
 *
 * @author Russell Gold
 * @author Jan Ohrstrom
 * @author Seth Ladd 
 **/
public class WebConversation {

    
    /**
     * Creates a new web conversation.
     **/
    public WebConversation() {
    }

    /**
     * Submits a GET method request and returns a response.
     **/
    public WebResponse getResponse( String urlString ) throws MalformedURLException, IOException {
        return getResponse( new GetMethodWebRequest( urlString ) );
    }


    /**
     * Submits a web request and returns a response, using all state developed so far as stored in
     * cookies as requested by the server.
     **/
    public WebResponse getResponse( WebRequest request ) throws MalformedURLException, IOException {
        HttpURLConnection connection = (HttpURLConnection) openConnection( request.getURL() );
        request.completeRequest( connection );
        updateCookies( connection );

        if (connection.getHeaderField( "Location" ) != null) {
            return getResponse( new RedirectWebRequest( request, connection.getHeaderField( "Location" ) ) );
        } else if (connection.getHeaderField( "WWW-Authenticate" ) != null) {
            throw new AuthorizationRequiredException( connection.getHeaderField( "WWW-Authenticate" ) );
        } else if (connection.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            throw new HttpInternalErrorException( request.getURLString() );
        } else if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new HttpNotFoundException( request.getURLString() );        
        } else {
            WebResponse result = new WebResponse( this, request.getURL(), connection );
            return result;
        }
    }


    /**
     * Defines a cookie to be sent to the server on every request.
     **/
    public void addCookie(String name, String value) {
	_cookies.put( name, value );
    }


    /**
     * Returns the name of all the active cookies which will be sent to the server.
     **/
    public String[] getCookieNames() {
        String[] names = new String[ _cookies.size() ];
        int i = 0;
        for (Enumeration e = _cookies.keys(); e.hasMoreElements();) {
            names[i++] = (String) e.nextElement();
        }
        return names;
    }


    /**
     * Returns the value of the specified cookie.
     **/
    public String getCookieValue( String name ) {
        return (String) _cookies.get( name );
    }

    
    /**
     * Specifies the user agent identification. Used to trigger browser-specific server behavior.
     **/    
    public void setUserAgent(String userAgent) {
	_userAgent = userAgent;
    }
    
        
    /**
     * Returns the current user agent setting.
     **/
    public String getUserAgent() {
	return _userAgent;
    }


    /**
     * Sets a username and password for a basic authentication scheme.
     **/
    public void setAuthorization( String userName, String password ) {
        _authorization = "Basic " + Base64.encode( userName + ':' + password );
    }


//---------------------------------- private members --------------------------------

    /** The currently defined cookies. **/
    private Hashtable _cookies = new Hashtable();


    /** The current user agent. **/
    private String _userAgent;


    /** The authorization header value. **/
    private String _authorization;

    
    static {
        HttpURLConnection.setFollowRedirects( false );
    }

    private HttpURLConnection openConnection( URL url ) throws MalformedURLException, IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setUseCaches( false );
        sendAuthorization( connection );
	sendUserAgent( connection );
        sendCookies( connection );
        return connection;
    }


    private void sendAuthorization( URLConnection connection ) {
        if (_authorization == null) return;
        connection.setRequestProperty( "Authorization", _authorization );
    }

    
    private void sendUserAgent ( URLConnection connection ) {
	if (getUserAgent() == null) return;
	connection.setRequestProperty( "User-Agent" , getUserAgent() );
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
    
}
