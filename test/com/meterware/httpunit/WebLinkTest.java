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
        super.setUp();
        defineResource( "SimplePage.html",
                        "<html><head><title>A Sample Page</title></head>\n" +
                        "<body>This has no forms but it does\n" +
                        "have <a href=\"/other.html\">an <b>active</b> link</A>\n" +
                        " and <a name=here>an anchor</a>\n" +
                        "<a href=\"basic.html\"><IMG SRC=\"/images/arrow.gif\" ALT=\"Next -->\" WIDTH=1 HEIGHT=4></a>\n" +
                        "</body></html>\n" );

        WebConversation wc = new WebConversation();
        _simplePage = wc.getResponse( getHostPath() + "/SimplePage.html" );
    }
	
	
    public void testFindNoLinks() throws Exception {
        defineResource( "NoLinks.html", "<html><head><title>NoLinks</title></head><body>No links at all</body></html>" );
        WebConversation wc = new WebConversation();

        WebLink[] links = wc.getResponse( getHostPath() + "/NoLinks.html" ).getLinks();
        assertNotNull( links );
        assertEquals( 0, links.length );
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
        assertEquals( getHostPath() + "/other.html", request.getURL().toExternalForm() );
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
        assertEquals( "active link URL", getHostPath() + "/other.html", link.getRequest().getURL().toExternalForm() );

        link = _simplePage.getLinkWithImageText( "Next -->" );
        assertNotNull( "the image link was not found", link );
        assertEquals( "image link URL", getHostPath() + "/basic.html", link.getRequest().getURL().toExternalForm() );

        HttpUnitOptions.setImagesTreatedAsAltText( true );
        link = _simplePage.getLinkWith( "Next -->" );
        assertNotNull( "the image link was not found", link );
        assertEquals( "image link URL", getHostPath() + "/basic.html", link.getRequest().getURL().toExternalForm() );

        HttpUnitOptions.setImagesTreatedAsAltText( false );
        link = _simplePage.getLinkWith( "Next -->" );
        assertNull( "the image link was found based on its hidden alt attribute", link );
    }


    public void testLinkText() throws Exception {
        WebLink link = _simplePage.getLinks()[0];
        assertEquals( "Link text", "an active link", link.asText() );
    }


    public void testLinkFollowing() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage( "Initial", "Go to <a href=\"Next.html\">the next page.</a> <a name=\"bottom\">Bottom</a>" );
        defineWebPage( "Next", "And go back to <a href=\"Initial.html#Bottom\">the first page.</a>" );
        
        WebResponse initialPage = wc.getResponse( getHostPath() + "/Initial.html" );
        assertEquals( "Num links in initial page", 1, initialPage.getLinks().length );
        WebLink link = initialPage.getLinks()[0];

        WebResponse nextPage = wc.getResponse( link.getRequest() );
        assertEquals( "Title of next page", "Next", nextPage.getTitle() );
        assertEquals( "Num links in next page", 1, nextPage.getLinks().length );
        link = nextPage.getLinks()[0];

        WebResponse thirdPage = wc.getResponse( link.getRequest() );
        assertEquals( "Title of next page", "Initial", thirdPage.getTitle() );
    }

                              
    private WebResponse _simplePage;


}
