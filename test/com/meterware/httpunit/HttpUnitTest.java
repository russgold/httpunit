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
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * a base class for HttpUnit regression tests.
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
abstract
class HttpUnitTest extends TestCase {

    public HttpUnitTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        _server = new PseudoServer();
        HttpUnitOptions.reset();
        _hostPath = "http://localhost:" + _server.getConnectedPort();
    }


    public void tearDown() throws Exception {
        if (_server != null) _server.shutDown();
    }


//--------------------------------- protected members ----------------------------------------------


    protected void defineResource( String resourceName, PseudoServlet servlet ) {
        _server.setResource( resourceName, servlet );
    }


    protected void defineResource( String resourceName, String value ) {
        _server.setResource( resourceName, value );
    }


    protected void defineResource( String resourceName, String value, String contentType ) {
        _server.setResource( resourceName, value, contentType );
    }
    
    
    protected void addResourceHeader( String resourceName, String header ) {
        _server.addResourceHeader( resourceName, header );
    }


    protected void setResourceCharSet( String resourceName, String setName, boolean reportCharSet ) {
        _server.setCharacterSet( resourceName, setName );
        _server.setSendCharacterSet( resourceName, reportCharSet );
    }


    protected void defineWebPage( String pageName, String body ) {
        defineResource( pageName + ".html", "<html><head><title>" + pageName + "</title></head>\n" + 
                                            "<body>" + body + "</body></html>" );
    }


    protected String getHostPath() {
        return _hostPath;
    }

    public static void assertTrue( String comment, boolean expression ) {
        assert( comment, expression );
    }


    protected void assertEquals( String comment, Object[] expected, Object[] found ) {
        if (!equals( expected, found )) {
            fail( comment + " expected: " + asText( expected ) + " but found " + asText( found ) );
        }
    }


    private boolean equals( Object[] first, Object[] second )
    {
        if (first.length != second.length) return false;
        for (int i = 0; i < first.length; i++)
        {
            if (!first[i].equals( second[i] )) return false;
        }
        return true;
    }




    protected void assertMatchingSet( String comment, Object[] expected, Object[] found ) {
        Vector expectedItems = new Vector();
        Vector foundItems    = new Vector();

        for (int i = 0; i < expected.length; i++) expectedItems.addElement( expected[i] );
        for (int i = 0; i < found.length; i++) foundItems.addElement( found[i] );

        for (int i = 0; i < expected.length; i++) {
            if (!foundItems.contains( expected[i] )) {
                fail( comment + ": expected " + asText( expected ) + " but found " + asText( found ) );
            } else {
                foundItems.removeElement( expected[i] );
            }
        }

        for (int i = 0; i < found.length; i++) {
            if (!expectedItems.contains( found[i] )) {
                fail( comment + ": expected " + asText( expected ) + " but found " + asText( found ) );
            } else {
                expectedItems.removeElement( found[i] );
            }
        }

        if (!foundItems.isEmpty()) fail( comment + ": expected " + asText( expected ) + " but found " + asText( found ) );
    }


    protected String asText( Object[] args ) {
        StringBuffer sb = new StringBuffer( "{" );
        for (int i = 0; i < args.length; i++) {
            if (i != 0) sb.append( "," );
            sb.append( '"' ).append( args[i] ).append( '"' );
        }
        sb.append( "}" );
        return sb.toString();
    }




    protected void assertEquals( String comment, byte[] expected, byte[] actual )
    {
        if (!equals( expected, actual )) 
        {
            fail( comment + " expected:\n" + toString( expected ) + ", but was:\n" + toString( actual ) );
        }
    }


    private boolean equals( byte[] first, byte[] second )
    {
        if (first.length != second.length) return false;
        for (int i = 0; i < first.length; i++)
        {
            if (first[i] != second[i]) return false;
        }
        return true;
    }


    private String toString( byte[] message )
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < message.length; i++) 
        {
            if (i != 0 && (i % 4) == 0) sb.append( ' ' );
            if (message[i] >= 0 && message[i] < 16) sb.append( '0' );
            sb.append( Integer.toHexString( 0xff & (int) message[i] ) );
        }
        return sb.toString();
    }


//---------------------------------------- private members -----------------------------------------

    private String _hostPath;

    private PseudoServer _server;


}
