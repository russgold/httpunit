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

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Vector;


/**
 * A unit test of the httpunit parsing classes.
 **/
public class WebLinkTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }
	
	
    public static Test suite() {
        return new TestSuite( WebLinkTest.class );
    }


    public WebLinkTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        _simplePage = new ReceivedPage( _baseURL, HEADER + "<body>This has no forms but it does" +
                                         "have <a href=\"/other.html\">an <b>active</b> link</A>" +
                                         " and <a name=here>an anchor</a>" +
                                         "<a href=\"basic.html\"><IMG SRC=\"/images/arrow.gif\" ALT=\"Next -->\" WIDTH=1 HEIGHT=4></a>" +
                                         "</body></html>" );
    }
	
	
    public void testFindNoLinks() {
        WebForm[] forms = _simplePage.getForms();
        assertNotNull( forms );
        assertEquals( 0, forms.length );
    }


    public void testLinks() throws Exception {
        WebLink[] links = _simplePage.getLinks();
        assertNotNull( links );
        assertEquals( 2, links.length );
    }
    

    public void testLinkRequest() throws Exception {
        WebLink link = _simplePage.getLinks()[0];
        WebRequest request = link.getRequest();
        assert( "Should be a get request", request instanceof GetMethodWebRequest );
        assertEquals( "http://www.meterware.com/other.html", request.getURL().toExternalForm() );
    }


    public void testLinkReference() throws Exception {
        WebLink link = _simplePage.getLinks()[0];
        assertEquals( "URLString", "/other.html", link.getURLString() );
    }


    public void testGetLinkByText() throws Exception {
        WebLink link = _simplePage.getLinkWith( "no link" );
        assertNull( "Non-existent link should not have been found", link );
        link = _simplePage.getLinkWith( "an active link" );
        assertNotNull( "an active link was not found", link );
        assertEquals( "active link URL", "http://www.meterware.com/other.html", link.getRequest().getURL().toExternalForm() );

        link = _simplePage.getLinkWithImageText( "Next -->" );
        assertNotNull( "the image link was not found", link );
        assertEquals( "image link URL", "http://www.meterware.com/basic.html", link.getRequest().getURL().toExternalForm() );
    }


    public void testLinkText() throws Exception {
        WebLink link = _simplePage.getLinks()[0];
        assertEquals( "Link text", "an active link", link.asText() );
    }

                              
    private static URL _baseURL;
     
    static {
        try {
            _baseURL = new URL( "http://www.meterware.com" );
        } catch (java.net.MalformedURLException e ) {}  // ignore
    }

    private final static String HEADER = "<html><head><title>A Sample Page</title></head>";
    private ReceivedPage _simplePage;


}
