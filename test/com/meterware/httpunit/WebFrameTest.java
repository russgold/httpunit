package com.meterware.httpunit;
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
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Vector;


/**
 * A test of the web frame functionality.
 **/
public class WebFrameTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }
    
    
    public static Test suite() {
        return new TestSuite( WebFrameTest.class );
    }


    public WebFrameTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
        _wc = new WebConversation();

        defineWebPage( "Linker",  "This is a trivial page with <a href=Target.html>one link</a>" );
        defineWebPage( "Target",  "This is another page with <a href=Form.html target=\"_top\">one link</a>" );
        defineWebPage( "Form",    "This is a page with a simple form: " + 
                                  "<form action=submit><input name=name><input type=submit></form>" +
                                  "<a href=Linker.html target=red>a link</a>");
        defineResource( "Frames.html",
                        "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                        "<FRAMESET cols=\"20%,80%\">" +
                        "    <FRAME src=\"Linker.html\" name=\"red\">" +
                        "    <FRAME src=Form.html name=blue>" +
                        "</FRAMESET></HTML>" );
    }


    public void testDefaultFrameNames() throws Exception {
        defineWebPage( "Initial", "This is a trivial page" );
        WebResponse response = _wc.getResponse( getHostPath() + "/Initial.html" );
        assertMatchingSet( "Frames defined for the conversation", new String[] { "_top" }, _wc.getFrameNames() );
    }


    public void testDefaultFrameContents() throws Exception {
        WebResponse response = _wc.getResponse( getHostPath() + "/Linker.html" );
        assert( "Default response not the same as default frame contents", response == _wc.getFrameContents( "_top" ) );
        response = _wc.getResponse( response.getLinks()[0].getRequest() );
        assert( "Second response not the same as default frame contents", response == _wc.getFrameContents( "_top" ) );
    }


    public void testFrameNames() throws Exception {
        WebResponse response = _wc.getResponse( getHostPath() + "/Frames.html" );
        assertMatchingSet( "frame set names", 
                           new String[] { "red", "blue" },
                           response.getFrameNames() );
    }


    public void testFrameRequests() throws Exception {
        WebResponse response = _wc.getResponse( getHostPath() + "/Frames.html" );
        WebRequest[] requests = response.getFrameRequests();
        assertEquals( "Number of frame requests", 2, requests.length );
        assertEquals( "Target for first request", "red", requests[0].getTarget() );
        assertEquals( "URL for second request", getHostPath() + "/Form.html", requests[1].getURL().toExternalForm() );
    }


    public void testFrameLoading() throws Exception {
        WebResponse response = _wc.getResponse( getHostPath() + "/Frames.html" );

        assertMatchingSet( "Frames defined for the conversation", new String[] { "_top", "red", "blue" }, _wc.getFrameNames() );
        assertEquals( "Number of links in first frame", 1, _wc.getFrameContents( "red" ).getLinks().length );
        assertEquals( "Number of forms in second frame", 1, _wc.getFrameContents( "blue" ).getForms().length );
    }


    public void testInFrameLinks() throws Exception {
        WebResponse response = _wc.getResponse( getHostPath() + "/Frames.html" );

        response = _wc.getResponse( _wc.getFrameContents( "red" ).getLinks()[0].getRequest() );
        assert( "Second response not the same as source frame contents", response == _wc.getFrameContents( "red" ) );
        assertMatchingSet( "Frames defined for the conversation", new String[] { "_top", "red", "blue" }, _wc.getFrameNames() );
        assertEquals( "URL for second request", getHostPath() + "/Target.html", response.getURL().toExternalForm() );
    }


    public void testFrameURLBase() throws Exception {
        defineWebPage( "Deeper/Linker",  "This is a trivial page with <a href=Target.html>one link</a>" );
        defineWebPage( "Deeper/Target",  "This is another page with <a href=Form.html target=\"_top\">one link</a>" );
        defineWebPage( "Deeper/Form",    "This is a page with a simple form: " + 
                                  "<form action=submit><input name=name><input type=submit></form>" +
                                  "<a href=Linker.html target=red>a link</a>");
        defineResource( "Frames.html",
                        "<HTML><HEAD><TITLE>Initial</TITLE>" +
                        "<base href=\"" + getHostPath() + "/Deeper/Frames.html\"></HEAD>" +
                        "<FRAMESET cols=\"20%,80%\">" +
                        "    <FRAME src=\"Linker.html\" name=\"red\">" +
                        "    <FRAME src=Form.html name=blue>" +
                        "</FRAMESET></HTML>" );

        WebResponse response = _wc.getResponse( getHostPath() + "/Frames.html" );

        response = _wc.getResponse( _wc.getFrameContents( "red" ).getLinks()[0].getRequest() );
        assert( "Second response not the same as source frame contents", response == _wc.getFrameContents( "red" ) );
        assertMatchingSet( "Frames defined for the conversation", new String[] { "_top", "red", "blue" }, _wc.getFrameNames() );
        assertEquals( "URL for second request", getHostPath() + "/Deeper/Target.html", response.getURL().toExternalForm() );
    }


    public void testUnnamedFrames() throws Exception {
        defineWebPage( "Linker",  "This is a trivial page with <a href=Target.html>one link</a>" );
        defineWebPage( "Target",  "This is another page with <a href=Form.html target=\"_top\">one link</a>" );
        defineWebPage( "Form",    "This is a page with a simple form: " + 
                                  "<form action=submit><input name=name><input type=submit></form>" +
                                  "<a href=Linker.html target=red>a link</a>");
        defineResource( "Frames.html",
                        "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                        "<FRAMESET cols=\"20%,80%\">" +
                        "    <FRAME src=\"Linker.html\">" +
                        "    <FRAME src=Form.html>" +
                        "</FRAMESET></HTML>" );

        WebResponse response = _wc.getResponse( getHostPath() + "/Frames.html" );
        WebResponse linker = getFrameWithURL( _wc, "Linker" );
        assertNotNull( "Linker not found", linker );

        response = _wc.getResponse( linker.getLinks()[0].getRequest() );
        WebResponse target = getFrameWithURL( _wc, "Target" );
        assert( "Second response not the same as source frame contents", response == target );
    }


    private String getNameOfFrameWithURL( WebConversation wc, String urlString ) {
        String[] names = wc.getFrameNames();
        for (int i = 0; i < names.length; i++) {
            WebResponse candidate = wc.getFrameContents( names[i] );
            if (candidate.getURL().toExternalForm().indexOf( urlString ) >= 0) {
                return names[i];
            }
        }
        return null;
    }


    private WebResponse getFrameWithURL( WebConversation wc, String urlString ) {
        String name = getNameOfFrameWithURL( wc, urlString );
        if (name == null) return null;
        return wc.getFrameContents( name );
    }


    public void testCrossFrameLinks() throws Exception {
        WebResponse response = _wc.getResponse( getHostPath() + "/Frames.html" );

        _wc.getResponse( _wc.getFrameContents( "red" ).getLinks()[0].getRequest() );
        response = _wc.getResponse( _wc.getFrameContents( "blue" ).getLinks()[0].getRequest() );
        assert( "Second response not the same as source frame contents", response == _wc.getFrameContents( "red" ) );
        assertMatchingSet( "Frames defined for the conversation", new String[] { "_top", "red", "blue" }, _wc.getFrameNames() );
        assertEquals( "URL for second request", getHostPath() + "/Linker.html", response.getURL().toExternalForm() );
    }


    public void testLinkToTopFrame() throws Exception {
        WebResponse response = _wc.getResponse( getHostPath() + "/Frames.html" );

        response = _wc.getResponse( _wc.getFrameContents( "red" ).getLinks()[0].getRequest() );
        response = _wc.getResponse( response.getLinks()[0].getRequest() );
        assert( "Second response not the same as source frame contents", response == _wc.getFrameContents( "_top" ) );
        assertEquals( "URL for second request", getHostPath() + "/Form.html", response.getURL().toExternalForm() );
        assertMatchingSet( "Frames defined for the conversation", new String[] { "_top" }, _wc.getFrameNames() );
    }


    public void testEmptyFrame() throws Exception {
        defineResource( "HalfFrames.html",
                        "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                        "<FRAMESET cols=\"20%,80%\">" +
                        "    <FRAME src=\"Linker.html\" name=\"red\">" +
                        "    <FRAME name=blue>" +
                        "</FRAMESET></HTML>" );
        _wc.getResponse( getHostPath() + "/HalfFrames.html" );
        WebResponse response = _wc.getFrameContents( "blue" );

        assertNotNull( "Loaded nothing for the empty frame", response );
        assertEquals( "Num links", 0, response.getLinks().length );
    }


    public void testSelfTarget() throws Exception {
        defineWebPage( "Linker",  "This is a trivial page with <a href=Target.html target=_self>one link</a>" );

        _wc.getResponse( getHostPath() + "/Frames.html" );
        WebResponse response = _wc.getResponse( _wc.getFrameContents( "red" ).getLinks()[0].getRequest() );
        assertMatchingSet( "Frames defined for the conversation", new String[] { "_top", "red", "blue" }, _wc.getFrameNames() );
        assert( "Second response not the same as source frame contents", response == _wc.getFrameContents( "red" ) );
        assertEquals( "URL for second request", getHostPath() + "/Target.html", response.getURL().toExternalForm() );
    }
    
    
    public void testSubFrameRedirect() throws Exception {
        defineResource( "Linker.html", "" );
        addResourceHeader( "Linker.html", "Location: " + getHostPath() + "/Target.html" );
                        
        _wc.getResponse( getHostPath() + "/Frames.html" );
        assertMatchingSet( "Frames defined for the conversation", new String[] { "_top", "red", "blue" }, _wc.getFrameNames() );
        assert( "Did not redirect", _wc.getFrameContents( "red" ).getURL().toExternalForm().endsWith( "Target.html" ) );
        
    }


    public void notestParentTarget() throws Exception {
        defineWebPage( "Linker",  "This is a trivial page with <a href=Target.html target=_parent>one link</a>" );

        _wc.getResponse( getHostPath() + "/Frames.html" );
        WebResponse response = _wc.getResponse( _wc.getFrameContents( "red" ).getLinks()[0].getRequest() );
        assertMatchingSet( "Frames defined for the conversation", new String[] { "_top" }, _wc.getFrameNames() );
        assert( "Second response not the same as source frame contents", response == _wc.getFrameContents( "_top" ) );
        assertEquals( "URL for second request", getHostPath() + "/Target.html", response.getURL().toExternalForm() );
    }


    private WebConversation _wc;
}
