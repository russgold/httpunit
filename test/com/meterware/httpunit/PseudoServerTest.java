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
import java.net.HttpURLConnection;

import java.util.Dictionary;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.meterware.httpunit.*;

/**
 * Tests the basic authentication.
 **/
public class PseudoServerTest extends TestCase {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }
    
    
    public static Test suite() {
        return new TestSuite( PseudoServerTest.class );
    }


    public PseudoServerTest( String name ) {
        super( name );
    }


    public void testNotFound() throws Exception {
        PseudoServer ps = new PseudoServer();
        int port = ps.getConnectedPort();

        WebConversation wc   = new WebConversation();
        WebRequest request   = new GetMethodWebRequest( "http://localhost:" + port + "/nothing.htm" );
        try {
            WebResponse response = wc.getResponse( request );
            fail( "Should have rejected the request" );
        } catch (HttpNotFoundException e) {
            assertEquals( "Response code", HttpURLConnection.HTTP_NOT_FOUND, e.getResponseCode() );
        } finally {
            ps.shutDown();
        }
    }


    public void testInternalError() throws Exception {
        PseudoServer ps = new PseudoServer();
        ps.setErrorResource( "error.htt", 501, "Internal error" );
        int port = ps.getConnectedPort();

        WebConversation wc   = new WebConversation();
        WebRequest request   = new GetMethodWebRequest( "http://localhost:" + port + "/error.htt" );
        try {
            WebResponse response = wc.getResponse( request );
            fail( "Should have rejected the request" );
        } catch (HttpException e) {
            assertEquals( "Response code", 501, e.getResponseCode() );
        } finally {
            ps.shutDown();
        }
    }


    public void testSimpleGet() throws Exception {
        String resourceName = "something/interesting";
        String resourceValue = "the desired content";

        PseudoServer ps = new PseudoServer();
        ps.setResource( resourceName, resourceValue );
        int port = ps.getConnectedPort();

        try {
            WebConversation wc   = new WebConversation();
            WebRequest request   = new GetMethodWebRequest( "http://localhost:" + port + '/' + resourceName );
            WebResponse response = wc.getResponse( request );
            assertEquals( "requested resource", resourceValue, response.getText().trim() );
            assertEquals( "content type", "text/html", response.getContentType() );
        } finally {
            ps.shutDown();
        }
    }


    private String asBytes( String s ) {
        StringBuffer sb = new StringBuffer();
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
          sb.append( Integer.toHexString( chars[i] ) ).append( " " );
        }
        return sb.toString();
    }


    public void testRedirect() throws Exception {
        String resourceName = "something/redirected";
        String resourceValue = "the desired content";

        String redirectName = "anOldOne";

        PseudoServer ps = new PseudoServer();
        int port = ps.getConnectedPort();
        ps.setResource( resourceName, resourceValue );
        ps.setResource( redirectName, "" );
        ps.addResourceHeader( redirectName, "Location: http://localhost:" + port + '/' + resourceName );

        try {
            WebConversation wc   = new WebConversation();
            WebRequest request   = new GetMethodWebRequest( "http://localhost:" + port + '/' + redirectName );
            WebResponse response = wc.getResponse( request );
            assertEquals( "requested resource", resourceValue, response.getText().trim() );
            assertEquals( "content type", "text/html", response.getContentType() );
        } finally {
            ps.shutDown();
        }
    }



    public void testCookies() throws Exception {
        String resourceName = "something/baking";
        String resourceValue = "the desired content";

        PseudoServer ps = new PseudoServer();
        int port = ps.getConnectedPort();
        ps.setResource( resourceName, resourceValue );
        ps.addResourceHeader( resourceName, "Set-Cookie: age=12, name=george" );
        ps.addResourceHeader( resourceName, "Set-Cookie: type=short" );
        ps.addResourceHeader( resourceName, "Set-Cookie: funky=ab$==" );

        try {
            WebConversation wc   = new WebConversation();
            WebRequest request   = new GetMethodWebRequest( "http://localhost:" + port + '/' + resourceName );
            WebResponse response = wc.getResponse( request );
            assertEquals( "requested resource", resourceValue, response.getText().trim() );
            assertEquals( "content type", "text/html", response.getContentType() );
            assertEquals( "number of cookies", 4, wc.getCookieNames().length );
            assertEquals( "cookie 'age' value", "12", wc.getCookieValue( "age" ) );
            assertEquals( "cookie 'name' value", "george", wc.getCookieValue( "name" ) );
            assertEquals( "cookie 'type' value", "short", wc.getCookieValue( "type" ) );
            assertEquals( "cookie 'funky' value", "ab$==", wc.getCookieValue( "funky" ) );
        } finally {
            ps.shutDown();
        }
    }


    public void testOldCookies() throws Exception {
        String resourceName = "something/baking";
        String resourceValue = "the desired content";

        PseudoServer ps = new PseudoServer();
        int port = ps.getConnectedPort();
        ps.setResource( resourceName, resourceValue );
        ps.addResourceHeader( resourceName, "Set-Cookie: CUSTOMER=WILE_E_COYOTE; path=/; expires=Wednesday, 09-Nov-99 23:12:40 GMT" );

        try {
            WebConversation wc   = new WebConversation();
            WebRequest request   = new GetMethodWebRequest( "http://localhost:" + port + '/' + resourceName );
            WebResponse response = wc.getResponse( request );
            assertEquals( "requested resource", resourceValue, response.getText().trim() );
            assertEquals( "content type", "text/html", response.getContentType() );
            assertEquals( "number of cookies", 1, wc.getCookieNames().length );
            assertEquals( "cookie 'CUSTOMER' value", "WILE_E_COYOTE", wc.getCookieValue( "CUSTOMER" ) );
        } finally {
            ps.shutDown();
        }
    }


    public void testPseudoServlet() throws Exception {
        String resourceName = "tellMe";
        String name = "Charlie";
        final String prefix = "Hello there, ";
        String expectedResponse = prefix + name; 

        PseudoServer ps = new PseudoServer();
        int port = ps.getConnectedPort();

        try {
            ps.setResource( resourceName, new PseudoServlet() {
                public WebResource getPostResponse( Dictionary parameters, Dictionary headers ) {
                    return new WebResource( prefix + ((String[]) parameters.get( "name" ))[0], "text/plain" );
                }
            } );
 
            WebConversation wc   = new WebConversation();
            WebRequest request   = new PostMethodWebRequest( "http://localhost:" + port + '/' + resourceName );
            request.setParameter( "name", name );
            WebResponse response = wc.getResponse( request );
            assertEquals( "Content type", "text/plain", response.getContentType() );
            assertEquals( "Response", expectedResponse, response.getText().trim() );
        } finally {
            ps.shutDown();
        }
    }
}

