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

import java.io.*;

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
        if (_responseCode == HttpURLConnection.HTTP_OK) loadResponseText( url, connection );
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


    final private static String END_OF_LINE   = System.getProperty( "line.separator" );
    final private static String FILE_ENCODING = System.getProperty( "file.encoding" );


    private int    _responseCode = HttpURLConnection.HTTP_OK;

    private String _responseText;

    private Hashtable _headers = new Hashtable();

    private void loadResponseText( URL url, URLConnection connection ) throws IOException {
        StringBuffer sb = new StringBuffer();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BufferedInputStream inputStream = new BufferedInputStream( connection.getInputStream() );
            byte[] buffer = new byte[8 * 1024];
            int count = 0;
            do {
                outputStream.write( buffer, 0, count );
                count = inputStream.read( buffer, 0, buffer.length );
            } while (count != -1);

            inputStream.close();

            readMetaTags( outputStream.toByteArray() );
            _responseText = new String( outputStream.toByteArray(), getCharacterSet() );
        } catch (FileNotFoundException e) {
            if (connection instanceof HttpURLConnection) {
                _responseCode = HttpURLConnection.HTTP_NOT_FOUND;
                _responseText = "";
            } else {
                e.fillInStackTrace();
                throw e;
            }
        }
    }


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


    private void readMetaTags( byte[] rawMessage ) throws UnsupportedEncodingException {
        ByteTagParser parser = new ByteTagParser( rawMessage );
        ByteTag tag = parser.getNextTag();
        while (tag != null && !tag.getName().equalsIgnoreCase( "body" )) {
            if (tag.getName().equalsIgnoreCase( "meta" )) processMetaTag( tag );
            tag = parser.getNextTag();
        }
    }


    private void processMetaTag( ByteTag tag ) {
        if (tag.getAttribute( "http_equiv" ) != null &&
            tag.getAttribute( "http_equiv" ).equalsIgnoreCase( "content-type" )) {
            inferContentType( tag.getAttribute( "content" ) );
        }
    }


    private void inferContentType( String contentTypeHeader ) {
        String originalHeader = (String) _headers.get( "Content-type".toUpperCase() );
        if (originalHeader == null || originalHeader.indexOf( "charset" ) < 0) {
            _headers.put( "Content-type".toUpperCase(), contentTypeHeader );
        }
    }


    private void readHeaders( URLConnection connection ) {
        loadHeaders( connection );
        if (connection instanceof HttpURLConnection) {
            _responseCode = getResponseCode( connection, connection.getHeaderField(0) );
        } else {
            _responseCode = HttpURLConnection.HTTP_OK;
            if (getContentType().startsWith( "text" )) {
                _headers.put( "Content-type".toUpperCase(), getContentType() + "; charset=" + FILE_ENCODING );
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

//=======================================================================================

class ByteTag {

    ByteTag( byte[] buffer, int start, int length ) throws UnsupportedEncodingException {
        _buffer = new String( buffer, start, length, "iso-8859-1" ).toCharArray();
        _name = nextToken();

        String attribute = "";
        String token = nextToken();
        while (token.length() != 0) {
            if (token.equals( "=" ) && attribute.length() != 0) {
                _attributes.put( attribute.toLowerCase(), nextToken() );
                attribute = "";
            } else {
                if (attribute.length() > 0) _attributes.put( attribute.toLowerCase(), "" );
                attribute = token;
            }
            token = nextToken();
        }
    }


    public String getName() {
        return _name;
    }

    public String getAttribute( String attributeName ) {
        return (String) _attributes.get( attributeName );
    }

    public String toString() {
        return "ByteTag[ name=" + _name + ";attributes = " + _attributes + ']';
    }

    private String _name = "";
    private Hashtable _attributes = new Hashtable();


    private char[] _buffer;
    private int    _start;
    private int    _end = -1;


    private String nextToken() {
        _start = _end+1;
        while (_start < _buffer.length && Character.isWhitespace( _buffer[ _start ] )) _start++;
        if (_start >= _buffer.length) {
            return "";
        } else if (_buffer[ _start ] == '"') {
            for (_end = _start+1; _end < _buffer.length && _buffer[ _end ] != '"'; _end++);
            return new String( _buffer, _start+1, _end-_start-1 );
        } else if (_buffer[ _start ] == '=') {
            _end = _start;
            return "=";
        } else {
            for (_end = _start+1; _end < _buffer.length && _buffer[ _end ] != '=' && !Character.isWhitespace( _buffer[ _end ] ); _end++);
            return new String( _buffer, _start, (_end--)-_start );
        }
    }
}

//=======================================================================================


class ByteTagParser {

    ByteTagParser( byte[] buffer ) {
        _buffer = buffer;
    }


    ByteTag getNextTag() throws UnsupportedEncodingException {
        _start = _end+1;
        while (_start < _buffer.length && _buffer[ _start ] != '<') _start++;
        for (_end =_start+1; _end < _buffer.length && _buffer[ _end ] != '>'; _end++);
        if (_end >= _buffer.length || _end < _start) return null;
        return new ByteTag( _buffer, _start+1, _end-_start-1 );
    }


    private int _start = 0;
    private int _end   = -1;

    private byte[] _buffer;
}

