package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2000-2001, Russell Gold
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
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.util.Dictionary;
import java.util.Enumeration;


/**
 * The context for a series of HTTP requests. This class manages cookies used to maintain
 * session context, computes relative URLs, and generally emulates the browser behavior
 * needed to build an automated test of a web site.
 *
 * @author Russell Gold
 **/
public class WebConversation extends WebClient {

    
    /**
     * Creates a new web conversation.
     **/
    public WebConversation() {
    }


//---------------------------------- protected members --------------------------------


    /**
     * Creates a web response object which represents the response to the specified web request.
     **/
    protected WebResponse newResponse( WebRequest request ) throws MalformedURLException, IOException {
        URLConnection connection = openConnection( request.getURL() );
	sendHeaders( connection, request.getHeaders() );
        if (HttpUnitOptions.isLoggingHttpHeaders()) {
            for (Enumeration e = getHeaderFields().keys(); e.hasMoreElements(); ) {
                String key = (String) e.nextElement();
                System.out.println( "Sending:: " + key + ": " + connection.getRequestProperty( key ) );
            }
        }
        request.completeRequest( connection );
        return new HttpWebResponse( request.getTarget(), request.getURL(), connection );
    }


//---------------------------------- private members --------------------------------

    static {
        HttpURLConnection.setFollowRedirects( false );
    }


    private URLConnection openConnection( URL url ) throws MalformedURLException, IOException {
        URLConnection connection = url.openConnection();
        connection.setUseCaches( false );
        sendHeaders( connection );
        return connection;
    }


    private void sendHeaders( URLConnection connection ) {
        sendHeaders( connection, getHeaderFields() );
    }


    private void sendHeaders( URLConnection connection, Dictionary headers ) {
        for (Enumeration e = headers.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            connection.setRequestProperty( key, (String) headers.get( key ) );
        }
    }
}
