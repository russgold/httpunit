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
 * A test of the parameter validation functionality.
 **/
public class FormSubmitTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }
    
    
    public static Test suite() {
        return new TestSuite( FormSubmitTest.class );
    }


    public FormSubmitTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
        _wc = new WebConversation();
    }
	
	
    public void testSubmitString() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebRequest request = page.getForms()[0].getRequest();
        request.setParameter( "age", "23" );
        assertEquals( getHostPath() + "/ask?age=23", request.getURL().toExternalForm() );
    }

                              
    public void testNoNameSubmitString() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text value=dontSend>" +
                                  "<Input type=text name=age>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebRequest request = page.getForms()[0].getRequest();
        request.setParameter( "age", "23" );
        assertEquals( getHostPath() + "/ask?age=23", request.getURL().toExternalForm() );
    }

                              
//---------------------------------------------- private members ------------------------------------------------


    private WebConversation _wc;
}

