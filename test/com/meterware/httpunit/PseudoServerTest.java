package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2000-2002, Russell Gold
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
public class PseudoServerTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }
    
    
    public static Test suite() {
        return new TestSuite( PseudoServerTest.class );
    }


    public PseudoServerTest( String name ) {
        super( name );
    }


    public void testNoSuchServer() throws Exception {
        WebConversation wc = new WebConversation();

        try {
            WebResponse response = wc.getResponse( "http://no.such.host" );
        } catch (HttpNotFoundException e) {
        }
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


    public void testNotModifiedResponse() throws Exception {
        PseudoServer ps = new PseudoServer();
        ps.setErrorResource( "error.htm", 304, "Not Modified" );
        int port = ps.getConnectedPort();

        try {
            WebConversation wc   = new WebConversation();
            WebRequest request   = new GetMethodWebRequest( "http://localhost:" + port + "/error.htm" );
            WebResponse response = wc.getResponse( request );
            assertEquals( "Response code", 304, response.getResponseCode() );
            response.getText();
            response.getInputStream().read();
        } finally {
            ps.shutDown();
        }
    }


    public void testInternalErrorException() throws Exception {
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


    public void testInternalErrorDisplay() throws Exception {
        PseudoServer ps = new PseudoServer();
        ps.setErrorResource( "error.htm", 501, "Internal error" );
        int port = ps.getConnectedPort();

        try {
            WebConversation wc   = new WebConversation();
            wc.setExceptionsThrownOnErrorStatus( false );
            WebRequest request   = new GetMethodWebRequest( "http://localhost:" + port + "/error.htm" );
            WebResponse response = wc.getResponse( request );
            assertEquals( "Response code", 501, response.getResponseCode() );
            assertEquals( "Message contents", "Internal error", response.getText().trim() );
        } finally {
            ps.shutDown();
        }
    }


    public void testHeaderFields() throws Exception {
        WebConversation wc = new WebConversation();
        wc.setHeaderField( "user-agent", "Mozilla 6" );
        assertEquals( "Mozilla 6", wc.getUserAgent() );
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
        ps.setErrorResource( redirectName, HttpURLConnection.HTTP_MOVED_PERM, "" );
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


    public void testDisabledRedirect() throws Exception {
        String resourceName = "something/redirected";
        String resourceValue = "the desired content";

        String redirectName = "anOldOne";
        String redirectValue = "old content";

        PseudoServer ps = new PseudoServer();
        int port = ps.getConnectedPort();
        ps.setResource( resourceName, resourceValue );
        ps.setErrorResource( redirectName, HttpURLConnection.HTTP_MOVED_PERM, redirectValue );
        ps.addResourceHeader( redirectName, "Location: http://localhost:" + port + '/' + resourceName );

        try {
            HttpUnitOptions.setAutoRedirect( false );
            WebConversation wc   = new WebConversation();
            WebRequest request   = new GetMethodWebRequest( "http://localhost:" + port + '/' + redirectName );
            WebResponse response = wc.getResponse( request );
            assertEquals( "requested resource", redirectValue, response.getText().trim() );
            assertEquals( "content type", "text/html", response.getContentType() );
        } finally {
            HttpUnitOptions.setAutoRedirect( true );
            ps.shutDown();
        }
    }


    public void testCookies() throws Exception {
        String resourceName = "something/baking";
        String resourceValue = "the desired content";

        PseudoServer ps = new PseudoServer();
        int port = ps.getConnectedPort();
        ps.setResource( resourceName, resourceValue );
        ps.addResourceHeader( resourceName, "Set-Cookie: HSBCLoginFailReason=; path=/" );
        ps.addResourceHeader( resourceName, "Set-Cookie: age=12, name= george" );
        ps.addResourceHeader( resourceName, "Set-Cookie: type=short" );
        ps.addResourceHeader( resourceName, "Set-Cookie: funky=ab$==" );
        ps.addResourceHeader( resourceName, "Set-Cookie: p30waco_sso=3.0,en,us,AMERICA,Drew;path=/, PORTAL30_SSO_TEST=X" );
        ps.addResourceHeader( resourceName, "Set-Cookie: SESSION_ID=17585,Dzm5LzbRPnb95QkUyIX+7w5RDT7p6OLuOVZ91AMl4hsDATyZ1ej+FA==; path=/;" );

        try {
            WebConversation wc   = new WebConversation();
            WebRequest request   = new GetMethodWebRequest( "http://localhost:" + port + '/' + resourceName );
            WebResponse response = wc.getResponse( request );
            assertEquals( "requested resource", resourceValue, response.getText().trim() );
            assertEquals( "content type", "text/html", response.getContentType() );
            assertEquals( "number of cookies", 8, wc.getCookieNames().length );
            assertEquals( "cookie 'HSBCLoginFailReason' value", "", wc.getCookieValue( "HSBCLoginFailReason" ) );
            assertEquals( "cookie 'age' value", "12", wc.getCookieValue( "age" ) );
            assertEquals( "cookie 'name' value", "george", wc.getCookieValue( "name" ) );
            assertEquals( "cookie 'type' value", "short", wc.getCookieValue( "type" ) );
            assertEquals( "cookie 'funky' value", "ab$==", wc.getCookieValue( "funky" ) );
            assertEquals( "cookie 'p30waco_sso' value", "3.0,en,us,AMERICA,Drew", wc.getCookieValue( "p30waco_sso" ) );
            assertEquals( "cookie 'PORTAL30_SSO_TEST' value", "X", wc.getCookieValue( "PORTAL30_SSO_TEST" ) );
            assertEquals( "cookie 'SESSION_ID' value", "17585,Dzm5LzbRPnb95QkUyIX+7w5RDT7p6OLuOVZ91AMl4hsDATyZ1ej+FA==", wc.getCookieValue( "SESSION_ID" ) );
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
                public WebResource getPostResponse() {
                    return new WebResource( prefix + getParameter( "name" )[0], "text/plain" );
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


    public void testRefererHeader() throws Exception {
        String resourceName = "tellMe";
        String linkSource = "fromLink";
	    String formSource = "fromForm";
	
        PseudoServer ps = new PseudoServer();
        int port = ps.getConnectedPort();

        String page0 = "http://localhost:" + port + '/' + resourceName;
	    String page1 = "http://localhost:" + port + '/' + linkSource;
	    String page2 = "http://localhost:" + port + '/' + formSource;
	
        ps.setResource( linkSource, "<html><head></head><body><a href=\"tellMe\">Go</a></body></html>" );
	    ps.setResource( formSource, "<html><body><form action=\"tellMe\"><input type=submit></form></body></html>" );

        try {
            ps.setResource( resourceName, new PseudoServlet() {
                public WebResource getGetResponse() {
		            String referer = getHeader( "Referer" );
                    return new WebResource( referer == null ? "null" : referer, "text/plain" );
                }
            } );
 
            WebConversation wc   = new WebConversation();
            WebResponse response = wc.getResponse( page0 );
            assertEquals( "Content type", "text/plain", response.getContentType() );
            assertEquals( "Default Referer header", "null", response.getText().trim() );

    	    response = wc.getResponse( page1 );
    	    response = wc.getResponse( response.getLinks()[0].getRequest() );
    	    assertEquals( "Link Referer header", page1, response.getText().trim() );
    
    	    response = wc.getResponse( page2 );
    	    response = wc.getResponse( response.getForms()[0].getRequest() );
    	    assertEquals( "Form Referer header", page2, response.getText().trim() );
        } finally {
            ps.shutDown();
        }
    }
}

