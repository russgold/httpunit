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
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.meterware.httpunit.*;
import java.net.HttpURLConnection;

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


    public void testSimpleGet() throws Exception {
        String resourceName = "something/interesting";
        String resourceValue = "the desired content\r\n";

        PseudoServer ps = new PseudoServer();
        ps.setResource( resourceName, resourceValue );
        int port = ps.getConnectedPort();

        try {
            WebConversation wc   = new WebConversation();
            WebRequest request   = new GetMethodWebRequest( "http://localhost:" + port + '/' + resourceName );
            WebResponse response = wc.getResponse( request );
            assertEquals( "requested resource", resourceValue, response.toString() );
            assertEquals( "content type", "text/html", response.getContentType() );
        } finally {
            ps.shutDown();
        }
    }



    public void testRedirect() throws Exception {
        String resourceName = "something/interesting";
        String resourceValue = "the desired content\r\n";

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
            assertEquals( "requested resource", resourceValue, response.toString() );
            assertEquals( "content type", "text/html", response.getContentType() );
        } finally {
            ps.shutDown();
        }
    }



    public void testCookies() throws Exception {
        String resourceName = "something/interesting";
        String resourceValue = "the desired content\r\n";

        PseudoServer ps = new PseudoServer();
        int port = ps.getConnectedPort();
        ps.setResource( resourceName, resourceValue );
        ps.addResourceHeader( resourceName, "Set-Cookie: age=12, name=george" );
        ps.addResourceHeader( resourceName, "Set-Cookie: type=short" );

        try {
            WebConversation wc   = new WebConversation();
            WebRequest request   = new GetMethodWebRequest( "http://localhost:" + port + '/' + resourceName );
            WebResponse response = wc.getResponse( request );
            assertEquals( "requested resource", resourceValue, response.toString() );
            assertEquals( "content type", "text/html", response.getContentType() );
            assertEquals( "number of cookies", 3, wc.getCookieNames().length );
            assertEquals( "cookie 'age' value", "12", wc.getCookieValue( "age" ) );
            assertEquals( "cookie 'name' value", "george", wc.getCookieValue( "name" ) );
            assertEquals( "cookie 'type' value", "short", wc.getCookieValue( "type" ) );
        } finally {
            ps.shutDown();
        }
    }
}

