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

import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.meterware.httpunit.*;

/**
 * Tests the ServletUnitHttpResponse class.
 **/
public class HttpServletResponseTest extends ServletUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }
    
    
    public static Test suite() {
        return new TestSuite( HttpServletResponseTest.class );
    }


    public HttpServletResponseTest( String name ) {
        super( name );
    }


    public void testDefaultResponse() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        WebResponse response = new ServletUnitWebResponse( null, "_self", null, servletResponse );
        assertEquals( "Content type", "text/plain", response.getContentType() );
        assertEquals( "Contents", "", response.getText() );
    }


    public void testSimpleResponse() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.setContentType( "text/html" );
        PrintWriter pw = servletResponse.getWriter();
        pw.println( "<html><head><title>Sample Page</title></head><body></body></html>" );

        WebResponse response = new ServletUnitWebResponse( null, "_self", null, servletResponse );
        assertEquals( "Status code", HttpServletResponse.SC_OK, response.getResponseCode() );
        assertEquals( "Content type", "text/html", response.getContentType() );
        assertEquals( "Title", "Sample Page", response.getTitle() );
    }


    public void testEncoding() throws Exception {
        String hebrewTitle = "\u05d0\u05d1\u05d2\u05d3";
        String page = "<html><head><title>" + hebrewTitle + "</title></head>\n" +
                      "<body>This has no data\n" +
                      "</body></html>\n";
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.setContentType( "text/html; charset=iso-8859-8" );
        PrintWriter pw = servletResponse.getWriter();
        pw.print( page );
        pw.close();

        WebResponse response = new ServletUnitWebResponse( null, "_self", null, servletResponse );
        assertEquals( "Character set", "iso-8859-8", response.getCharacterSet() );
        assertEquals( "Title", hebrewTitle, response.getTitle() );
    }


    public void testStreamResponse() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.setContentType( "text/html" );
        ServletOutputStream sos = servletResponse.getOutputStream();
        sos.println( "<html><head><title>Sample Page</title></head><body></body></html>" );

        WebResponse response = new ServletUnitWebResponse( null, "_self", null, servletResponse );
        assertEquals( "Status code", HttpServletResponse.SC_OK, response.getResponseCode() );
        assertEquals( "Content type", "text/html", response.getContentType() );
        assertEquals( "Title", "Sample Page", response.getTitle() );
    }


    public void testStreamWriterAfterOutputStream() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.setContentType( "text/html" );
        ServletOutputStream sos = servletResponse.getOutputStream();
        try {
            servletResponse.getWriter();
            fail( "Should have thrown IllegalStateException" );
        } catch (IllegalStateException e) {
        }
    }


    public void testStreamOutputStreamAfterWriter() throws Exception {
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        servletResponse.getWriter();
        try {
            servletResponse.getOutputStream();
            fail( "Should have thrown IllegalStateException" );
        } catch (IllegalStateException e) {
        }
    }


}


