package com.meterware.httpunit;

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Vector;


/**
 * Tests to ensure the proper handling of the target attribute.
 **/
public class RequestTargetTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }
    
    
    public static Test suite() {
        return new TestSuite( RequestTargetTest.class );
    }


    public RequestTargetTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
        _wc = new WebConversation();
    }


    public void testDefaultLinkTarget() throws Exception {
        defineWebPage( "Initial", "Here is a <a href=\"SimpleLink.html\">simple link</a>." );

        WebRequest request = new GetMethodWebRequest( getHostPath() + "/Initial.html" );
        assertEquals( "new link target", WebRequest.TOP_FRAME, request.getTarget() );
        
        WebResponse response = _wc.getResponse( request );
        assertEquals( "default response target", WebRequest.TOP_FRAME, response.getFrameName() );
        WebLink link = response.getLinks()[0];
        assertEquals( "default link target", WebRequest.TOP_FRAME, link.getTarget() );
        assertEquals( "default request target", WebRequest.TOP_FRAME, link.getRequest().getTarget() );
    }
	
	
    public void testExplicitLinkTarget() throws Exception {
        defineWebPage( "Initial", "Here is a <a href=\"SimpleLink.html\" target=\"subframe\">simple link</a>." );

        WebLink link = _wc.getResponse( getHostPath() + "/Initial.html" ).getLinks()[0];
        assertEquals( "explicit link target", "subframe", link.getTarget() );
        assertEquals( "request target", "subframe", link.getRequest().getTarget() );
    }
	
	
    public void testInheritedLinkTarget() throws Exception {
        defineWebPage( "Initial", "Here is a <a href=\"SimpleLink.html\" target=\"subframe\">simple link</a>." );
        defineWebPage( "SimpleLink", "Here is <a href=\"Initial.html\">another simple link</a>." );

        WebLink link = _wc.getResponse( getHostPath() + "/Initial.html" ).getLinks()[0];
        assertEquals( "explicit link target", "subframe", link.getTarget() );
        assertEquals( "request target", "subframe", link.getRequest().getTarget() );

        WebResponse response = _wc.getResponse( link.getRequest() );
        assertEquals( "response target", "subframe", response.getFrameName() );
        link = response.getLinks()[0];
        assertEquals( "inherited link target", "subframe", link.getTarget() );
    }
	
	
    public void testInheritedLinkTargetInTable() throws Exception {
        defineWebPage( "Initial", "Here is a <a href=\"SimpleLink.html\" target=\"subframe\">simple link</a>." );
        defineWebPage( "SimpleLink", "Here is <table><tr><td><a href=\"Initial.html\">another simple link</a>.</td></tr></table>" );

        WebLink link = _wc.getResponse( getHostPath() + "/Initial.html" ).getLinks()[0];
        assertEquals( "explicit link target", "subframe", link.getTarget() );
        assertEquals( "request target", "subframe", link.getRequest().getTarget() );

        WebResponse response = _wc.getResponse( link.getRequest() );
        assertEquals( "response target", "subframe", response.getFrameName() );
        WebTable table = response.getTables()[0];
        TableCell cell = table.getTableCell(0,0);
        link = cell.getLinks()[0];
        assertEquals( "inherited link target", "subframe", link.getTarget() );
    }
	
	
    public void testDefaultFormTarget() throws Exception {
        defineWebPage( "Initial", "Here is a simple form: " +
                                  "<form method=POST action = \"/servlet/Login\"><B>" +
                                  "<input type=\"checkbox\" name=first>Disabled" +
                                  "<br><Input type=submit value = \"Log in\">" +
                                  "</form>" );

        WebResponse response = _wc.getResponse( getHostPath() + "/Initial.html" );
        assertEquals( "Num forms in page", 1, response.getForms().length );
        WebForm form = response.getForms()[0];
        assertEquals( "default form target", WebRequest.TOP_FRAME, form.getTarget() );
        assertEquals( "default request target", WebRequest.TOP_FRAME, form.getRequest().getTarget() );
    }

	
    public void testExplicitPostFormTarget() throws Exception {
        defineWebPage( "Initial", "Here is a simple form: " +
                                  "<form method=POST action = \"/servlet/Login\" target=\"subframe\"><B>" +
                                  "<input type=\"checkbox\" name=first>Disabled" +
                                  "<br><Input type=submit value = \"Log in\">" +
                                  "</form>" );

        WebForm form = _wc.getResponse( getHostPath() + "/Initial.html" ).getForms()[0];
        assertEquals( "explicit form target", "subframe", form.getTarget() );
        assertEquals( "request target", "subframe", form.getRequest().getTarget() );
    }
	
	
    public void testExplicitGetFormTarget() throws Exception {
        defineWebPage( "Initial", "Here is a simple form: " +
                                  "<form method=GET action = \"/servlet/Login\" target=\"subframe\"><B>" +
                                  "<input type=\"checkbox\" name=first>Disabled" +
                                  "<br><Input type=submit value = \"Log in\">" +
                                  "</form>" );

        WebForm form = _wc.getResponse( getHostPath() + "/Initial.html" ).getForms()[0];
        assertEquals( "explicit form target", "subframe", form.getTarget() );
        assertEquals( "request target", "subframe", form.getRequest().getTarget() );
    }

	
    public void testInheritedFormTarget() throws Exception {
        defineWebPage( "Initial", "Here is a <a href=\"SimpleLink.html\" target=\"subframe\">simple link</a>." );
        defineWebPage( "SimpleLink", "Here is a simple form: " +
                                     "<form method=GET action = \"/servlet/Login\" target=\"subframe\"><B>" +
                                     "<input type=\"checkbox\" name=first>Disabled" +
                                     "<br><Input type=submit value = \"Log in\">" +
                                     "</form>" );

        WebLink link = _wc.getResponse( getHostPath() + "/Initial.html" ).getLinks()[0];
        assertEquals( "explicit link target", "subframe", link.getTarget() );
        assertEquals( "request target", "subframe", link.getRequest().getTarget() );

        WebResponse response = _wc.getResponse( link.getRequest() );
        assertEquals( "response target", "subframe", response.getFrameName() );
        WebForm form = response.getForms()[0];
        assertEquals( "inherited form target", "subframe", form.getTarget() );
    }
	
	
    private WebConversation _wc;
}
