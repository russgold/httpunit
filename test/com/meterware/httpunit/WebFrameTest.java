package com.meterware.httpunit;

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


    private WebConversation _wc;
}
