package com.meterware.httpunit;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2002, Russell Gold
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

import java.util.List;
import java.util.ArrayList;
import java.net.MalformedURLException;
import java.io.IOException;

import junit.framework.TestSuite;


/**
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
public class WebClientTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( WebClientTest.class );
    }


    public WebClientTest( String name ) {
        super( name );
    }


    public void testClientListener() throws Exception {
        defineWebPage( "Target",  "This is another page with <a href=Form.html target='_top'>one link</a>" );
        defineWebPage( "Form",    "This is a page with a simple form: " +
                                  "<form action=submit><input name=name><input type=submit></form>" +
                                  "<a href=Target.html target=red>a link</a>");
        defineResource( "Frames.html",
                        "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                        "<FRAMESET cols='20%,80%'>" +
                        "    <FRAME src='Target.html' name='red'>" +
                        "    <FRAME src=Form.html name=blue>" +
                        "</FRAMESET></HTML>" );

        WebConversation wc = new WebConversation();
        ArrayList messageLog = new ArrayList();
        wc.addClientListener( new ListenerExample( messageLog ) );

        WebResponse response = wc.getResponse( getHostPath() + "/Frames.html" );
        assertEquals( "Num logged items", 6, messageLog.size() );
        for (int i=0; i <3; i++) {
            verifyRequestResponsePair( messageLog, 2*i );
        }
    }


    private void verifyRequestResponsePair( ArrayList messageLog, int i ) throws MalformedURLException {
        assertTrue( "Logged item " + i + " is not a web request, but " + messageLog.get(i).getClass(),
                    messageLog.get(i) instanceof WebRequest );
        assertTrue( "Logged item " + (i+1) + " is not a web response, but " + messageLog.get(i+1).getClass(),
                    messageLog.get(i+1) instanceof WebResponse );
        assertEquals( "Response target", ((WebRequest) messageLog.get(i)).getTarget(), ((WebResponse) messageLog.get(i+1)).getTarget() );
        assertEquals( "Response URL", ((WebRequest) messageLog.get(i)).getURL(), ((WebResponse) messageLog.get(i+1)).getURL() );
    }


    private static class ListenerExample implements WebClientListener {

        private List _messageLog;


        public ListenerExample( List messageLog ) {
            _messageLog = messageLog;
        }


        public void requestSent( WebClient src, WebRequest req ) {
            _messageLog.add( req );
        }


        public void responseReceived( WebClient src, WebResponse resp ) {
            _messageLog.add( resp );
       }
    }


}
