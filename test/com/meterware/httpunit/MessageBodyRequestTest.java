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

import java.util.*;

import java.io.*;

import org.w3c.dom.Document;

import javax.activation.DataSource;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;


/**
 * A unit test to verify miscellaneous requests with message bodies.
 **/
public class MessageBodyRequestTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }
	
	
    public static Test suite() {
        return new TestSuite( MessageBodyRequestTest.class );
    }


    public MessageBodyRequestTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
    }
	
        
    public void testGenericPostRequest() throws Exception {
        defineResource( "ReportData", new BodyEcho() );
        String sourceData = "This is an interesting test\nWith two lines";
        InputStream source = new ByteArrayInputStream( sourceData.getBytes( "iso-8859-1" ) );

        WebConversation wc = new WebConversation();
        WebRequest wr = new PostMethodWebRequest( getHostPath() + "/ReportData", source, "text/sample" );
        WebResponse response = wc.getResponse( wr );
        assertEquals( "Body response", "\nPOST\n" + sourceData, response.getText() );
        assertEquals( "Content-type", "text/sample", response.getContentType() ); 
    }


    public void testPutRequest() throws Exception {
        defineResource( "ReportData", new BodyEcho() );
        String sourceData = "This is an interesting test\nWith two lines";
        InputStream source = new ByteArrayInputStream( sourceData.getBytes( "iso-8859-1" ) );

        WebConversation wc = new WebConversation();
        WebRequest wr = new PutMethodWebRequest( getHostPath() + "/ReportData", source, "text/plain" );
        WebResponse response = wc.getResponse( wr );
        assertEquals( "Body response", "\nPUT\n" + sourceData, response.getText() );
    }


}


class BodyEcho extends PseudoServlet {
    public WebResource getResponse( String method, Dictionary parameters, Dictionary headers ) {
        return new WebResource( "\n" + method + "\n" + (String) headers.get( PseudoServlet.CONTENTS ), (String) headers.get( "CONTENT-TYPE" ) );
    }
}

