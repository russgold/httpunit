package com.meterware.servletunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2008 by Russell Gold
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
import com.meterware.httpunit.*;
import com.meterware.pseudoserver.HttpUserAgentTest;
import com.meterware.servletunit.*;

import java.io.*;
import java.net.HttpURLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;


/**
 * test for bug report [ 1043368 ] WebTable has wrong number of columns
 * by AutoTest
 */
public class FormTableTest extends HttpUnitTest {

    public static void main( String args[] ) {
        junit.textui.TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( FormTableTest.class );
    }


    public FormTableTest( String name ) {
        super( name );
    }

    public void testFormTable() throws Exception {
        final String resourceName = "something/interesting";

        ServletRunner sr = new ServletRunner();
        sr.registerServlet( resourceName, FormTableServlet.class.getName() );

        WebRequest request = new GetMethodWebRequest( "http://localhost/" + resourceName );
        WebResponse response = sr.getResponse( request );

        assertNotNull( "No response received", response );

        WebTable table = response.getTableStartingWithPrefix( "Test Form" );
  			// table.purgeEmptyCells();

        assertNotNull( "didn't find table", table );
        
        boolean bug1043368Pending=true;
        if (bug1043368Pending) {
        	this.warnDisabled("testFormTable", "for pending bug 1043368");
        } else {
        	System.out.println( table.toString() );        
        	assertFalse( "wrong table", 
                    table.getCellAsText( 1, 0 ).indexOf("Contact Name") == -1 );
        	assertEquals( "wrong column count", 4, table.getColumnCount());
        }	
        
    }

    public static class FormTableServlet extends HttpServlet {
        protected void doGet( HttpServletRequest req, 
                              HttpServletResponse resp ) 
            throws ServletException, IOException 
        {
            resp.setContentType( "text/html" );

            PrintWriter pw = resp.getWriter();
            pw.println(HtmlTablesTest.htmlForBug1043368);
            pw.flush();
            pw.close();

        }
    }
}


