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
    }


    public void testFrameNames() throws Exception {
        defineResource( "Initial.html",
                        "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                        "<FRAMESET cols=\"20%,80%\">" +
                        "  <FRAMESET rows=\"30%,70%\">" +
                        "    <FRAME src=\"overview-frame.html\" name=\"packageListFrame\">" +
                        "    <FRAME src=allclasses-frame.html name=packageFrame>" +
                        "  </FRAMESET>" +
                        "  <FRAME src=\"overview-summary.html\" name=\"classFrame\">" +
                        "</FRAMESET></HTML>" );
        WebResponse response = _wc.getResponse( getHostPath() + "/Initial.html" );

        assertMatchingSet( "frame set names", 
                           new String[] { "packageListFrame", "packageFrame", "classFrame" },
                           response.getFrameNames() );
    }
	
	
	
    public void testFrameRetrieval() throws Exception {
        defineResource( "Initial.html",
                        "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                        "<FRAMESET cols=\"20%,80%\">" +
                        "  <FRAMESET rows=\"30%,70%\">" +
                        "    <FRAME src=\"overview-frame.html\" name=\"packageListFrame\">" +
                        "    <FRAME src=allclasses-frame.html name=packageFrame>" +
                        "  </FRAMESET>" +
                        "  <FRAME src=\"overview-summary.html\" name=\"classFrame\">" +
                        "</FRAMESET></HTML>" );
        WebResponse response = _wc.getResponse( getHostPath() + "/Initial.html" );

        assertEquals( "number of frames found", 3, response.getFrames().length );
    }
	
	
	
    private WebConversation _wc;
}
