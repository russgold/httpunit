//-----------------------------------------------------------------------------
// Copyright (c) 2001 by Hewlett-Packard Company. All rights reserved.
//-----------------------------------------------------------------------------
package com.meterware.servletunit;

import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.ByteArrayInputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import junit.framework.TestSuite;
import junit.framework.TestCase;


public class WebXMLTest extends TestCase {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( WebXMLTest.class );
    }


    public WebXMLTest( String name ) {
        super( name );
    }


    public void testBasicAccess() throws Exception {
        String config = "<?xml version='1.0' encoding='ISO-8859-1'?>" +
                        "<web-app>" +
                        "  <servlet><servlet-name>Simple</servlet-name>" +
                        "           <servlet-class>" + SimpleGetServlet.class.getName() + "</servlet-class></servlet>" +
                        "  <servlet-mapping><servlet-name>Simple</servlet-name>" +
                        "                   <url-pattern>/SimpleServlet</url-pattern></servlet-mapping>" +
                        "</web-app>";

        ServletRunner sr = new ServletRunner( new ByteArrayInputStream( config.getBytes() ) );
        WebRequest request   = new GetMethodWebRequest( "http://localhost/SimpleServlet" );
        WebResponse response = sr.getResponse( request );
        assertNotNull( "No response received", response );
        assertEquals( "content type", "text/html", response.getContentType() );
        assertEquals( "requested resource", SimpleGetServlet.RESPONSE_TEXT, response.getText() );
    }


    private final static String DOCTYPE = "<!DOCTYPE web-app PUBLIC " +
                                          "   \"-//Sun Microsystems, Inc.//DTD WebApplication 2.2//EN\" " +
                                          "   \"http://java.sun/com/j2ee/dtds/web-app_2_2.dtd\">";

//===============================================================================================================


    static class SimpleGetServlet extends HttpServlet {
        static String RESPONSE_TEXT = "the desired content\r\n";

        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
            resp.setContentType( "text/html" );
            PrintWriter pw = resp.getWriter();
            pw.print( RESPONSE_TEXT );
            pw.close();
        }
    }

}






