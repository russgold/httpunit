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
import javax.servlet.*;
import javax.servlet.http.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.meterware.httpunit.*;

/**
 * Tests support for the servlet configuration.
 **/
public class ConfigTest extends TestCase {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }
    
    
    public static Test suite() {
        return new TestSuite( ConfigTest.class );
    }


    public ConfigTest( String name ) {
        super( name );
    }


    public void testConfigObject() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet( resourceName, ConfigServlet.class.getName() );
        WebClient wc = sr.newClient();
        WebResponse response = wc.getResponse( "http://localhost/" + resourceName );
        assertNotNull( "No response received", response );
        assertEquals( "content type", "text/plain", response.getContentType() );
        assertEquals( "servlet name is " + ConfigServlet.class.getName(), response.getText() );
    }


    static class ConfigServlet extends HttpServlet {
        static String RESPONSE_TEXT = "the desired content\r\n";

        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException,IOException {
            resp.setContentType( "text/plain" );
            PrintWriter pw = resp.getWriter();
            ServletConfig config = getServletConfig();

            if (config == null) {
                pw.print( "config object is null" );
            } else {
                pw.print( "servlet name is " + config.getServletName() );
            }
            pw.close();
        }

    }
}


