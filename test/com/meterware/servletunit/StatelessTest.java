package com.meterware.servletunit;
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
import javax.servlet.ServletException;
import javax.servlet.http.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.meterware.httpunit.*;

/**
 * Tests support for stateless HttpServlets.
 **/
public class StatelessTest extends TestCase {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }


    public static Test suite() {
        return new TestSuite( StatelessTest.class );
    }


    public StatelessTest( String name ) {
        super( name );
    }


    public void testNotFound() throws Exception {
        ServletRunner sr = new ServletRunner();

        WebRequest request   = new GetMethodWebRequest( "http://localhost/nothing" );
        try {
            sr.getResponse( request );
            fail( "Should have rejected the request" );
        } catch (HttpNotFoundException e) {
            assertEquals( "Response code", HttpURLConnection.HTTP_NOT_FOUND, e.getResponseCode() );
        }
    }


    public void testServletCaching() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet( resourceName, AccessCountServlet.class.getName() );

        WebRequest request   = new GetMethodWebRequest( "http://localhost/" + resourceName );
        assertEquals( "First reply", "1", sr.getResponse( request ).getText().trim() );
        assertEquals( "Second reply", "2", sr.getResponse( request ).getText().trim() );
    }


    public void testServletAccessByClassName() throws Exception {
        ServletRunner sr = new ServletRunner();

        WebRequest request   = new GetMethodWebRequest( "http://localhost/servlet/" + SimpleGetServlet.class.getName() );
        WebResponse response = sr.getResponse( request );
        assertNotNull( "No response received", response );
        assertEquals( "content type", "text/html", response.getContentType() );
        assertEquals( "requested resource", SimpleGetServlet.RESPONSE_TEXT, response.getText() );
    }


    public void testSimpleGet() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet( resourceName, SimpleGetServlet.class.getName() );

        WebRequest request   = new GetMethodWebRequest( "http://localhost/" + resourceName );
        WebResponse response = sr.getResponse( request );
        assertNotNull( "No response received", response );
        assertEquals( "content type", "text/html", response.getContentType() );
        assertEquals( "requested resource", SimpleGetServlet.RESPONSE_TEXT, response.getText() );
    }


    public void testGetWithSetParams() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet( resourceName, ParameterServlet.class.getName() );

        WebRequest request   = new GetMethodWebRequest( "http://localhost/" + resourceName );
        request.setParameter( "color", "red" );
        WebResponse response = sr.getResponse( request );
        assertNotNull( "No response received", response );
        assertEquals( "content type", "text/plain", response.getContentType() );
        assertEquals( "requested resource", "You selected red", response.getText() );
    }


    public void testGetWithInlineParams() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet( resourceName, ParameterServlet.class.getName() );

        WebRequest request   = new GetMethodWebRequest( "http://localhost/" + resourceName + "?color=dark+red" );
        WebResponse response = sr.getResponse( request );
        assertNotNull( "No response received", response );
        assertEquals( "content type", "text/plain", response.getContentType() );
        assertEquals( "requested resource", "You selected dark red", response.getText() );
    }

    public void testHeaderRetrieval() throws Exception {
        ServletRunner sr = new ServletRunner();
        sr.registerServlet( "/Parameters", ParameterServlet.class.getName() );

        ServletUnitClient client = sr.newClient();
        client.setHeaderField( "Sample", "Value" );
        client.setHeaderField( "Request", "Client" );
        WebRequest request   = new GetMethodWebRequest( "http://localhost/Parameters?color=dark+red" );
        request.setHeaderField( "request", "Caller" );
        InvocationContext ic = client.newInvocation( request );
        assertEquals( "Sample header", "Value", ic.getRequest().getHeader( "sample" ) );
        assertEquals( "Request header", "Caller", ic.getRequest().getHeader( "Request" ) );
     }


    public void testSimplePost() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet( resourceName, ParameterServlet.class.getName() );

        WebRequest request   = new PostMethodWebRequest( "http://localhost/" + resourceName );
        request.setParameter( "color", "red" );
        WebResponse response = sr.getResponse( request );
        assertNotNull( "No response received", response );
        assertEquals( "content type", "text/plain", response.getContentType() );
        assertEquals( "requested resource", "You posted red", response.getText() );
    }


    public void testRequestInputStream() throws Exception {
        ServletRunner sr = new ServletRunner();
        WebRequest request = new PostMethodWebRequest( "http://localhost/servlet/" + ParameterServlet.class.getName() );
        request.setParameter( "color", "green" );
        final String expectedBody = "color=green";
        InvocationContext ic = sr.newClient().newInvocation( request );
        assertEquals( "Message body type", "application/x-www-form-urlencoded", ic.getRequest().getContentType() );
        InputStream is = ic.getRequest().getInputStream();
        byte[] buffer = new byte[ expectedBody.length() ];
        assertEquals( "Input stream length", buffer.length, is.read( buffer ) );
        assertEquals( "Message body", expectedBody, new String( buffer ) );
    }



    static class SimpleGetServlet extends HttpServlet {
        static String RESPONSE_TEXT = "the desired content\r\n";

        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException,IOException {
            resp.setContentType( "text/html" );
            PrintWriter pw = resp.getWriter();
            pw.print( RESPONSE_TEXT );
            pw.close();
        }
    }


    static class AccessCountServlet extends HttpServlet {
        private int _numAccesses;

        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException,IOException {
            resp.setContentType( "text/plain" );
            PrintWriter pw = resp.getWriter();
            pw.print( String.valueOf( ++_numAccesses ) );
            pw.close();
        }
    }


    static class ParameterServlet extends HttpServlet {
        static String RESPONSE_TEXT = "the desired content\r\n";

        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException,IOException {
            resp.setContentType( "text/plain" );
            PrintWriter pw = resp.getWriter();
            pw.print( "You selected " + req.getParameter( "color" ) );
            pw.close();
        }

        protected void doPost( HttpServletRequest req, HttpServletResponse resp ) throws ServletException,IOException {
            resp.setContentType( "text/plain" );
            PrintWriter pw = resp.getWriter();
            pw.print( "You posted " + req.getParameter( "color" ) );
            pw.close();
        }

    }
}


