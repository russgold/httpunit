package com.meterware.servletunit;
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
import java.net.HttpURLConnection;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;

import com.meterware.httpunit.HttpUnitUtils;
import com.meterware.httpunit.WebRequest;


class ServletUnitHttpResponse implements HttpServletResponse {


    /**
     * @deprecated Use encodeURL(String url)
     */
    public String encodeUrl( String url ) {
        return encodeURL( url );
    }


    /**
     * Adds the specified cookie to the response.  It can be called
     * multiple times to set more than one cookie.
     */
    public void addCookie( Cookie cookie ) {
        _cookies.addElement( cookie );
    }


    /**
     * Checks whether the response message header has a field with
     * the specified name.
     */
    public boolean containsHeader(String name) {
        throw new RuntimeException( "containsHeader not implemented" );
    }


    /**
     * @deprecated Use encodeRedirectURL(String url)
     **/
    public String encodeRedirectUrl( String url ) {
        return encodeRedirectURL( url );
    }


    /**
     * Encodes the specified URL by including the session ID in it,
     * or, if encoding is not needed, returns the URL unchanged.
     * The implementation of this method should include the logic to
     * determine whether the session ID needs to be encoded in the URL.
     * For example, if the browser supports cookies, or session
     * tracking is turned off, URL encoding is unnecessary.
     **/
    public String encodeURL( String url ) {
        return url;
    }


    /**
     * Encodes the specified URL for use in the
     * <code>sendRedirect</code> method or, if encoding is not needed,
     * returns the URL unchanged.  The implementation of this method
     * should include the logic to determine whether the session ID
     * needs to be encoded in the URL.  Because the rules for making
     * this determination differ from those used to decide whether to
     * encode a normal link, this method is seperate from the
     * <code>encodeUrl</code> method.
     **/
    public String encodeRedirectURL( String url ) {
        return url;
    }


    /**
     * Sends a temporary redirect response to the client using the
     * specified redirect location URL.  The URL must be absolute (for
     * example, <code><em>https://hostname/path/file.html</em></code>).
     * Relative URLs are not permitted here.
     */
    public void sendRedirect( String location ) throws IOException {
        setHeader( "Location", location );
    }


    /**
     * Sends an error response to the client using the specified status
     * code and descriptive message.  If setStatus has previously been
     * called, it is reset to the error status code.  The message is
     * sent as the body of an HTML page, which is returned to the user
     * to describe the problem.  The page is sent with a default HTML
     * header; the message is enclosed in simple body tags
     * (&lt;body&gt;&lt;/body&gt;).
     **/
    public void sendError( int sc ) throws IOException {
        throw new RuntimeException( "sendError not implemented" );
    }


    /**
     * Sends an error response to the client using the specified status
     * code and descriptive message.  If setStatus has previously been
     * called, it is reset to the error status code.  The message is
     * sent as the body of an HTML page, which is returned to the user
     * to describe the problem.  The page is sent with a default HTML
     * header; the message is enclosed in simple body tags
     * (&lt;body&gt;&lt;/body&gt;).
     **/
    public void sendError(int sc, String msg) throws IOException {
        throw new RuntimeException( "sendError not implemented" );
    }


    /**
     * Sets the status code for this response.  This method is used to
     * set the return status code when there is no error (for example,
     * for the status codes SC_OK or SC_MOVED_TEMPORARILY).  If there
     * is an error, the <code>sendError</code> method should be used
     * instead.
     **/
    public void setStatus(int sc) {
        throw new RuntimeException( "setStatus not implemented" );
    }


    /**
     * @deprecated As of version 2.1, due to ambiguous meaning of the message parameter. 
     * To set a status code use setStatus(int), to send an error with a description 
     * use sendError(int, String). Sets the status code and message for this response.
     **/
    public void setStatus( int sc, String msg ) {
        throw new RuntimeException( "setStatus not implemented" );
    }


    /**
     * Adds a field to the response header with the given name and value.
     * If the field had already been set, the new value overwrites the
     * previous one.  The <code>containsHeader</code> method can be
     * used to test for the presence of a header before setting its
     * value.
     **/
    public void setHeader( String name, String value ) {
        _headers.put( name.toUpperCase(), value );
    }


    /**
     * Adds a field to the response header with the given name and
     * integer value.  If the field had already been set, the new value
     * overwrites the previous one.  The <code>containsHeader</code>
     * method can be used to test for the presence of a header before
     * setting its value.
     **/
    public void setIntHeader( String name, int value ) {
        throw new RuntimeException( "setIntHeader not implemented" );
    }


    /**
     *
     * Adds a field to the response header with the given name and
     * date-valued field.  The date is specified in terms of
     * milliseconds since the epoch.  If the date field had already
     * been set, the new value overwrites the previous one.  The
     * <code>containsHeader</code> method can be used to test for the
     * presence of a header before setting its value.
     **/
    public void setDateHeader( String name, long date ) {
        throw new RuntimeException( "setDateHeader not implemented" );
    }


    /**
     * Returns the name of the character set encoding used for
     * the MIME body sent by this response.
     **/
    public String getCharacterEncoding() {
        return _encoding;
    }


    /**
     * Sets the content type of the response the server sends to
     * the client. The content type may include the type of character
     * encoding used, for example, <code>text/html; charset=ISO-8859-4</code>.
     *
     * <p>You can only use this method once, and you should call it
     * before you obtain a <code>PrintWriter</code> or
     * {@link ServletOutputStream} object to return a response.
     **/
    public void setContentType( String type ) {
        String[] typeAndEncoding = HttpUnitUtils.parseContentTypeHeader( type );

        _contentType = typeAndEncoding[0];
        if (typeAndEncoding[1] != null) _encoding = typeAndEncoding[1];

        setHeader( "Content-type", _contentType + "; charset=" + _encoding );
    }


    /**
     * Returns a {@link ServletOutputStream} suitable for writing binary
     * data in the response. The servlet engine does not encode the
     * binary data.
     *
     * @exception IllegalStateException if you have already called the <code>getWriter</code> method
     **/
    public ServletOutputStream getOutputStream() throws IOException {
        if (_writer != null) throw new IllegalStateException( "Tried to create output stream; writer already exists" );
        if (_servletStream == null) {
            _outputStream = new ByteArrayOutputStream();
            _servletStream = new ServletUnitOutputStream( _outputStream );
        }
        return _servletStream;
    }


    /**
     * Returns a <code>PrintWriter</code> object that you
     * can use to send character text to the client.
     * The character encoding used is the one specified
     * in the <code>charset=</code> property of the
     * {@link #setContentType} method, which you must call
     * <i>before</i> you call this method.
     *
     * <p>If necessary, the MIME type of the response is
     * modified to reflect the character encoding used.
     *
     * <p> You cannot use this method if you have already
     * called {@link #getOutputStream} for this
     * <code>ServletResponse</code> object.
     *
     * @exception UnsupportedEncodingException  if the character encoding specified in
     *						<code>setContentType</code> cannot be
     *						used
     *
     * @exception IllegalStateException    	if the <code>getOutputStream</code>
     * 						method has already been called for this
     *						response object; in that case, you can't
     *						use this method
     *
     **/
    public PrintWriter getWriter() throws UnsupportedEncodingException {
        if (_servletStream != null) throw new IllegalStateException( "Tried to create writer; output stream already exists" );
        if (_writer == null) {
            _outputStream = new ByteArrayOutputStream();
            _writer = new PrintWriter( new OutputStreamWriter( _outputStream, getCharacterEncoding() ) );
        }
        return _writer;
    }


    /**
     * Sets the length of the content the server returns
     * to the client. In HTTP servlets, this method sets the
     * HTTP Content-Length header.
     **/
    public void setContentLength( int len ) {
        throw new RuntimeException( "setContentLength not implemented" );
    }


//------------------------------- the following methods are new in JSDK 2.2 ----------------------


    /**
     * Adds a response header with the given name and value. This method allows response headers to have multiple values.
     **/
    public void addHeader( String name, String value ) {
        throw new RuntimeException( "addHeader not implemented" );
    }


    /**
     * Adds a response header with the given name and value. This method allows response headers to have multiple values.
     **/
    public void addIntHeader( String name, int value ) {
        throw new RuntimeException( "addIntHeader not implemented" );
    }


    /**
     * Adds a response header with the given name and value. This method allows response headers to have multiple values.
     **/
    public void addDateHeader( String name, long value ) {
        throw new RuntimeException( "addDateHeader not implemented" );
    }

    
    /**
     * Sets the preferred buffer size for the body of the response. The servlet container 
     * will use a buffer at least as large as the size requested. The actual buffer size 
     * used can be found using getBufferSize.
     **/
    public void setBufferSize( int size ) {   // XXX throw IllegalStateException if anything has been written
        throw new RuntimeException( "setBufferSize not implemented" );
    }


    /**
     * Returns the actual buffer size used for the response. If no buffering is used, this method returns 0.
     **/ 
    public int getBufferSize() {
        return 0;
    }


    /**
     * Returns a boolean indicating if the response has been committed. A commited response has 
     * already had its status code and headers written.
     **/
    public boolean isCommitted() {
        return false;    // XXX set true if flushBuffer has been called
    }
     
     
    /**
     * Forces any content in the buffer to be written to the client. A call to this method automatically 
     * commits the response, meaning the status code and headers will be written.
     **/
    public void flushBuffer() throws IOException {
        throw new RuntimeException( "flushBuffer not implemented" );
    }

    
    /**
     * Clears any data that exists in the buffer as well as the status code and headers. 
     * If the response has been committed, this method throws an IllegalStateException.
     **/
    public void reset() {
        throw new RuntimeException( "reset not implemented" );
    }


    /**
     * Sets the locale of the response, setting the headers (including the Content-Type's charset) 
     * as appropriate. This method should be called before a call to getWriter(). 
     * By default, the response locale is the default locale for the server.
     **/
    public void setLocale( Locale locale ) {
        throw new RuntimeException( "setLocale not implemented" );
    }

     
    /**
     * Returns the locale assigned to the response.
     **/
    public Locale getLocale() {
        throw new RuntimeException( "getLocale not implemented" );
    }

     
     
//---------------------------------------- package methods ---------------------------------------


    /**
     * Returns the content type defined for this response.
     **/
    String getContentType() {
        return _contentType;
    }


    /**
     * Returns the contents of this response.
     **/
    byte[] getContents() {
        if (_outputStream == null) {
            return new byte[0];
        } else {
            if (_writer != null) _writer.flush();
            return _outputStream.toByteArray();
        }
    }


    /**
     * Returns the status of this response.
     **/
    int getStatus() {
        return _status;
    }


    /**
     * Returns the headers defined for this response.
     **/
    String getHeaderField( String name ) {
        if (!_headersComplete) completeHeaders();
        return (String) _headers.get( name.toUpperCase() );
    }


//------------------------------------------- private members ------------------------------------


    private String _contentType = "text/plain";

    private String _encoding    = "us-ascii";

    private PrintWriter  _writer;

    private ServletOutputStream _servletStream;

    private ByteArrayOutputStream _outputStream;

    private int _status = SC_OK;

    private Hashtable _headers = new Hashtable();

    private boolean _headersComplete;

    private Vector  _cookies = new Vector();


    private void completeHeaders() {
        if (_headersComplete) return;
        addCookieHeader();
        _headersComplete = true;
    }


    private void addCookieHeader() {
        if (_cookies.isEmpty()) return;

        StringBuffer sb = new StringBuffer();
        for (Enumeration e = _cookies.elements(); e.hasMoreElements();) {
            Cookie cookie = (Cookie) e.nextElement();
            sb.append( cookie.getName() ).append( '=' ).append( cookie.getValue() );
            if (e.hasMoreElements()) sb.append( ',' );
        }
        setHeader( "Set-Cookie", sb.toString() );
    }
}



class ServletUnitOutputStream extends ServletOutputStream {

    ServletUnitOutputStream( ByteArrayOutputStream stream ) {
        _stream = stream;
    }


    public void write( int aByte ) throws IOException {
        _stream.write( aByte );
    }

    private ByteArrayOutputStream _stream;
}
