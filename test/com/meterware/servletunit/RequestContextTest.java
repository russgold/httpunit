package com.meterware.servletunit;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2003, Russell Gold
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
import com.meterware.httpunit.HttpUnitTest;

import java.net.URL;

import junit.framework.TestSuite;


/**
 * 
 * @author <a href="russgold@httpunit.org">Russell Gold</a>
 **/

public class RequestContextTest extends HttpUnitTest {

    public static void main( String args[] ) {
        junit.textui.TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( RequestContextTest.class );
    }


    public RequestContextTest( String testName ) {
        super( testName );
    }


    /**
     * Verify parsing of a query string.
     */
    public void testQueryStringParsing() throws Exception {
        RequestContext rc = new RequestContext( new URL( "http://localhost/basic?param=red&param1=old&param=blue" ));
        assertMatchingSet( "parameter names", new String[] { "param", "param1" }, rc.getParameterNames() );
        assertMatchingSet( "param values", new String[] { "red", "blue" }, rc.getParameterValues( "param" ) );
        assertEquals( "param1 value", "old", ((String[]) rc.getParameterMap().get( "param1"))[0] );
    }


    /**
     * Verify parsing of a query string.
     */
    public void testParameterOverride() throws Exception {
        RequestContext rc1 = new RequestContext( new URL( "http://localhost/basic?param=red&param1=old&param=blue" ) );
        RequestContext rc2 = new RequestContext( new URL( "http://localhost/second?param=yellow&param2=fast" ));
        rc2.setParentContext( rc1 );
        assertMatchingSet( "parameter names", new String[] { "param", "param1", "param2" }, rc2.getParameterNames() );
        assertMatchingSet( "param values", new String[] { "yellow" }, rc2.getParameterValues( "param" ) );
        assertEquals( "param1 value", "old", ((String[]) rc2.getParameterMap().get( "param1"))[0] );
    }


}
