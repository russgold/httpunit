package com.meterware.pseudoserver;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2000-2003, Russell Gold
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
import junit.framework.TestSuite;

import java.net.HttpURLConnection;
import java.net.Socket;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedInputStream;


public class PseudoServerTest extends HttpUserAgentTest {

    public static void main( String args[] ) {
        junit.textui.TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( PseudoServerTest.class );
    }


    public PseudoServerTest( String name ) {
        super( name );
    }


    public void testNotFoundStatus() throws Exception {
        SocketConnection conn = new SocketConnection( "localhost", getHostPort() );
        SocketConnection.SocketResponse response = conn.getResponse( "GET", "/nothing.htm" );
        assertEquals( "Response code", HttpURLConnection.HTTP_NOT_FOUND, response.getResponseCode() );
    }


    public void testStatusSpecification() throws Exception {
        defineResource( "error.htm", "Not Modified", 304 );

        SocketConnection conn = new SocketConnection( "localhost", getHostPort() );
        SocketConnection.SocketResponse response = conn.getResponse( "GET", "/error.htm" );
        assertEquals( "Response code", 304, response.getResponseCode() );
    }


    /**
     * This tests simple access to the server without using any client classes.
     */
    public void testGetViaSocket() throws Exception {
        defineResource( "sample", "Get this", "text/plain" );
        Socket socket = new Socket( "localhost", getHostPort() );
        OutputStream os = socket.getOutputStream();
        InputStream is = new BufferedInputStream( socket.getInputStream() );

        sendHTTPLine( os, "GET /sample HTTP/1.0" );
        sendHTTPLine( os, "Host: meterware.com" );
        sendHTTPLine( os, "" );

        StringBuffer sb = new StringBuffer();
        int b;
        while (-1 != (b = is.read())) sb.append( (char) b );
        String result = sb.toString();
        assertTrue( "Did not find matching protocol", result.startsWith( "HTTP/1.0" ) );
        assertTrue( "Did not find expected text", result.indexOf( "Get this" ) > 0 );
    }


    private void sendHTTPLine( OutputStream os, final String line ) throws IOException {
        os.write( line.getBytes() );
        os.write( 13 );
        os.write( 10 );
    }


    /**
     * This verifies that the PseudoServer detects and echoes its protocol.
     */
    public void testProtocolMatching() throws Exception {
        defineResource( "sample", "Get this", "text/plain" );
        Socket socket = new Socket( "localhost", getHostPort() );
        OutputStream os = socket.getOutputStream();
        InputStream is = new BufferedInputStream( socket.getInputStream() );

        sendHTTPLine( os, "GET /sample HTTP/1.1" );
        sendHTTPLine( os, "Host: meterware.com" );
        sendHTTPLine( os, "" );

        StringBuffer sb = new StringBuffer();
        int b;
        while (-1 != (b = is.read())) sb.append( (char) b );
        String result = sb.toString();
        assertTrue( "Did not find matching protocol", result.startsWith( "HTTP/1.1" ) );
        assertTrue( "Did not find expected text", result.indexOf( "Get this" ) > 0 );
    }


    /**
     * This verifies that the PseudoServer can be restricted to a HTTP/1.0.
     */
    public void testProtocolThrottling() throws Exception {
        getServer().setMaxProtocolLevel( 1, 0 );
        defineResource( "sample", "Get this", "text/plain" );
        Socket socket = new Socket( "localhost", getHostPort() );
        OutputStream os = socket.getOutputStream();
        InputStream is = new BufferedInputStream( socket.getInputStream() );

        sendHTTPLine( os, "GET /sample HTTP/1.1" );
        sendHTTPLine( os, "Host: meterware.com" );
        sendHTTPLine( os, "Connection: close" );
        sendHTTPLine( os, "" );

        StringBuffer sb = new StringBuffer();
        int b;
        while (-1 != (b = is.read())) sb.append( (char) b );
        String result = sb.toString();
        assertTrue( "Did not find matching protocol", result.startsWith( "HTTP/1.0" ) );
        assertTrue( "Did not find expected text", result.indexOf( "Get this" ) > 0 );
    }


    public void testPseudoServlet() throws Exception {
        String resourceName = "tellMe";
        String name = "Charlie";
        final String prefix = "Hello there, ";
        String expectedResponse = prefix + name;

        defineResource( resourceName, new PseudoServlet() {
            public WebResource getPostResponse() {
                return new WebResource( prefix + getParameter( "name" )[0], "text/plain" );
            }
        } );

        SocketConnection conn = new SocketConnection( "localhost", getHostPort() );
        SocketConnection.SocketResponse response = conn.getResponse( "POST", '/' + resourceName, "name=" + name );
        assertEquals( "Content type", "text/plain", response.getHeader( "Content-Type" ) );
        assertEquals( "Response", expectedResponse, new String( response.getBody() ) );
    }


    public void testChunkedRequest() throws Exception {
        super.defineResource( "/chunkedServlet", new PseudoServlet() {
            public WebResource getPostResponse() {
                return new WebResource( super.getBody(), "text/plain" );
            }
        } );

        SocketConnection conn = new SocketConnection( "localhost", getHostPort() );
        conn.startChunkedResponse( "POST", "/chunkedServlet" );
        conn.sendChunk( "This " );
        conn.sendChunk( "is " );
        conn.sendChunk( "chunked.");
        SocketConnection.SocketResponse response = conn.getResponse();
        assertEquals( "retrieved body", "This is chunked.", new String( response.getBody() ) );
    }


    public void testChunkedResponse() throws Exception {
        defineResource( "/chunkedServlet", new PseudoServlet() {
            public WebResource getGetResponse() {
                WebResource webResource = new WebResource( "5\r\nSent \r\n3\r\nin \r\n07\r\nchunks.\r\n0\r\n", "text/plain" );
                webResource.addHeader( "Transfer-Encoding: chunked" );
                return webResource;
            }
        } );

        SocketConnection conn = new SocketConnection( "localhost", getHostPort() );
        SocketConnection.SocketResponse response = conn.getResponse( "GET", "/chunkedServlet" );
        assertEquals( "retrieved body", "Sent in chunks.", new String( response.getBody() ) );
        assertNull( "No Content-Length header should have been sent", response.getHeader( "Content-Length" ) );
    }


    public void testPersistentConnection() throws Exception {
        super.defineResource( "/testServlet", new TestMethodServlet() );

        SocketConnection conn = new SocketConnection( "localhost", getHostPort() );
        SocketConnection.SocketResponse resp1 = conn.getResponse( "HEAD", "/testServlet" );
        assertEquals( "test-header", "test-value1", resp1.getHeader( "test-header1") );

        SocketConnection.SocketResponse resp2 = conn.getResponse( "GET", "/testServlet" );
        assertEquals( "retrieved body", TestMethodServlet.GET_DATA, new String( resp2.getBody() ) );

        SocketConnection.SocketResponse resp3 = conn.getResponse( "OPTIONS", "/testServlet" );
        assertEquals( "allow header", "GET", resp3.getHeader( "Allow") );
    }


    private class TestMethodServlet extends PseudoServlet {

        private static final String GET_DATA = "This is from the TestMethodServlet - GET";

        public WebResource getResponse( String method ) throws IOException {
            if (method.equals( "GET" )) {
                return new WebResource( GET_DATA );
            } else if (method.equals( "HEAD" )) {
                WebResource headResource = new WebResource( "" );
                headResource.addHeader( "test-header1:test-value1" );
                return headResource;
            } else if (method.equals( "OPTIONS" )) {
                WebResource optionsResource = new WebResource( GET_DATA );
                optionsResource.addHeader( "Allow:GET" );
                optionsResource.addHeader( "test-header1:test-value1" );
                return optionsResource;
            } else {
                return super.getResponse( method );
            }
        }
    }


    public void testBadMethodUsingPseudoServlet() throws Exception {
        String resourceName = "tellMe";

        defineResource( resourceName, new PseudoServlet() {} );

        SocketConnection conn = new SocketConnection( "localhost", getHostPort() );
        SocketConnection.SocketResponse response = conn.getResponse( "HEAD", '/' + resourceName );
        assertEquals( "Status code returned", HttpURLConnection.HTTP_BAD_METHOD, response.getResponseCode() );
    }


    public void testClasspathDirectory() throws Exception {
        mapToClasspath( "/some/classes" );

        SocketConnection conn = new SocketConnection( "localhost", getHostPort() );
        conn.getResponse( "GET", "/some/classes/" + SocketConnection.SocketResponse.class.getName().replace('.','/') + ".class" );
    }


    public void testPseudoServletRequestAccess() throws Exception {
        defineResource( "/properties", new PseudoServlet() {
            public WebResource getGetResponse() {
                return new WebResource( super.getRequest().getURI(), "text/plain" );
            }
        } );

        SocketConnection conn = new SocketConnection( "localhost", getHostPort() );
        SocketConnection.SocketResponse response = conn.getResponse( "GET", "/properties" );
        assertEquals( "retrieved body", "/properties", new String( response.getBody() ) );
    }


}

