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
 * Tests support for navigating among servlets.
 **/
public class NavigationTest extends TestCase {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }
    
    
    public static Test suite() {
        return new TestSuite( NavigationTest.class );
    }


    public NavigationTest( String name ) {
        super( name );
    }


    public void testRedirect() throws Exception {
        ServletRunner sr = new ServletRunner();
        sr.registerServlet( "target", TargetServlet.class.getName() );
        sr.registerServlet( "origin", OriginServlet.class.getName() );

        WebClient wc = sr.newClient();
        WebResponse response = wc.getResponse( "http://localhost/origin" );
        assertNotNull( "No response received", response );
        assertEquals( "requested resource", TargetServlet.RESPONSE_TEXT, response.getText() );
        assertEquals( "Returned cookie count", 0, response.getNewCookieNames().length );
    }




    static class OriginServlet extends HttpServlet {

        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException,IOException {
            resp.setContentType( "text/plain" );
            resp.sendRedirect( "http://localhost/target" );
        }

    }


    static class TargetServlet extends HttpServlet {
        static String RESPONSE_TEXT = "the desired content\r\n";

        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException,IOException {
            resp.setContentType( "text/plain" );
            PrintWriter pw = resp.getWriter();
            pw.print( RESPONSE_TEXT );
            pw.close();
        }

    }
}


