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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
                      

/**
 * A response from a web server to an Http request.
 **/
class HttpWebResponse extends WebResponse {


    /**
     * Constructs a response object from an input stream.
     * @param url the url from which the response was received
     * @param inputStream the input stream from which the response can be read
     **/
    HttpWebResponse( String target, URL url, URLConnection connection ) throws IOException {
        super( target, url );
        readHeaders( connection );
        if (((HttpURLConnection) connection).getResponseCode() == HttpURLConnection.HTTP_OK) loadResponseText( url, connection );
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
    
    
    /**
     * Returns the text of the response (excluding headers) as a string. Use this method in preference to 'toString'
     * which may be used to represent internal state of this object.
     **/
    public String getText() {
        return _responseText;
    }
    
    
//-------------------------------------------- private members ------------------------------------------------


    final private static String endOfLine     = System.getProperty( "line.separator" );
    final private static String _fileEncoding = System.getProperty( "file.encoding" );


    private int    _responseCode = HttpURLConnection.HTTP_OK;

    private String _responseText;

    private Hashtable _headers = new Hashtable();

    private void loadResponseText( URL url, URLConnection connection ) throws FileNotFoundException {
        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader input = new BufferedReader( new InputStreamReader( connection.getInputStream(), getCharacterSet() ) );

            String str;
            while (null != ((str = input.readLine()))) {
                sb.append( str ).append( endOfLine );
            }
            input.close();
            _responseText = sb.toString();
        } catch (FileNotFoundException e) {
            if (connection instanceof HttpURLConnection) {
                _responseCode = HttpURLConnection.HTTP_NOT_FOUND;
                _responseText = "";
            } else {
                e.fillInStackTrace();
                throw e;
            }
        } catch (IOException e) {
            throw new RuntimeException( "Unable to retrieve data from URL: " + url.toExternalForm() + " (" + e + ")" );
        }
    }


    private void readHeaders( URLConnection connection ) {
        loadHeaders( connection );
        try {
            if (connection instanceof HttpURLConnection) {
                _responseCode = ((HttpURLConnection) connection).getResponseCode();
            } else {
                _responseCode = HttpURLConnection.HTTP_OK;
                if (getContentType().startsWith( "text" )) {
                    _headers.put( "Content-type".toUpperCase(), getContentType() + "; charset=" + _fileEncoding );
                }
            }
        } catch (IOException e) {
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
            _headers.put( "Content-type".toUpperCase(), connection.getContentType() );
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

