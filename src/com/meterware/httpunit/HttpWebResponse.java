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

import java.io.BufferedInputStream;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import java.util.Hashtable;
import java.util.StringTokenizer;
                      

/**
 * A response from a web server to an Http request.
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
class HttpWebResponse extends WebResponse {


    /**
     * Constructs a response object from an input stream.
     * @param target the target window or frame to which the request should be directed
     * @param url the url from which the response was received
     * @param connection the URL connection from which the response can be read
     **/
    HttpWebResponse( String target, URL url, URLConnection connection ) throws IOException {
        super( target, url );
        readHeaders( connection );

        /** make sure that any IO exception for HTML received page happens here, not later. **/
        if (_responseCode == HttpURLConnection.HTTP_OK) {
            defineRawInputStream( new BufferedInputStream( connection.getInputStream() ) );
            if (getContentType().startsWith( "text" )) loadResponseText();
        }
    }


    /**
     * Returns the response code associated with this response.
     **/
    public int getResponseCode() {
        return _responseCode;
    }


    /**
     * Returns the value for the specified header field. If no such field is defined, will return null.
     **/
    public String getHeaderField( String fieldName ) {
        return (String) _headers.get( fieldName.toUpperCase() );
    }
    
    
    public String toString() {
        return "[headers=" + _headers + "]";
    }


//------------------------------------- private members -------------------------------------


    final private static String END_OF_LINE   = System.getProperty( "line.separator" );
    final private static String FILE_ENCODING = System.getProperty( "file.encoding" );


    private int       _responseCode = HttpURLConnection.HTTP_OK;

    private Hashtable _headers = new Hashtable();

    
    private int getResponseCode( URLConnection connection, String statusHeader ) {
        if (statusHeader == null) throw new HttpNotFoundException( connection.getURL().toExternalForm() );

        StringTokenizer st = new StringTokenizer( statusHeader );
    	st.nextToken();
    	if (!st.hasMoreTokens()) {
    	    return HttpURLConnection.HTTP_OK;
    	} else try {
    	    return Integer.parseInt( st.nextToken() );
    	} catch (NumberFormatException e) {
    	    return HttpURLConnection.HTTP_INTERNAL_ERROR;
    	}
    }


    private void readHeaders( URLConnection connection ) {
        loadHeaders( connection );
        if (connection instanceof HttpURLConnection) {
            _responseCode = getResponseCode( connection, connection.getHeaderField(0) );
        } else {
            _responseCode = HttpURLConnection.HTTP_OK;
            if (connection.getContentType().startsWith( "text" )) {
                setContentTypeHeader( connection.getContentType() + "; charset=" + FILE_ENCODING );
            }
        }
    }


    private void loadHeaders( URLConnection connection ) {
        if (HttpUnitOptions.isLoggingHttpHeaders()) {
            System.out.println( "Header:: " + connection.getHeaderField(0) );
        }
        for (int i = 1; true; i++) {
            String key = connection.getHeaderFieldKey( i );
            if (key == null) break;
            if (HttpUnitOptions.isLoggingHttpHeaders()) {
                System.out.println( "Header:: " + connection.getHeaderFieldKey( i ) + ": " + connection.getHeaderField(i) );
            }
            addHeader( connection.getHeaderFieldKey( i ).toUpperCase(), connection.getHeaderField( i ) );
        }

        if (connection.getContentType() != null) {
            setContentTypeHeader( connection.getContentType() );
        } 
    }



    private void addHeader( String key, String field ) {
        if (_headers.get( key ) == null) {
            _headers.put( key, field );
        } else {
            _headers.put( key, _headers.get( key ) + ", " + field );
        }
    }

}

