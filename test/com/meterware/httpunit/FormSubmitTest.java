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
                                  "<Input type=submit value=Go>" +
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
                                  "<Input type=submit></form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebRequest request = page.getForms()[0].getRequest();
        request.setParameter( "age", "23" );
        assertEquals( getHostPath() + "/ask?age=23", request.getURL().toExternalForm() );
    }


    public void testSubmitButtonDetection() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=submit name=update>" +
                                  "<Input type=submit name=recalculate>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        SubmitButton[] buttons = form.getSubmitButtons();
        assertEquals( "num detected submit buttons", 2, buttons.length );
    }

                              
    public void testImageButtonDetection() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=image name=update src=\"\">" +
                                  "<Input type=image name=recalculate src=\"\">" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        SubmitButton[] buttons = form.getSubmitButtons();
        assertEquals( "num detected submit buttons", 2, buttons.length );
    }

                              
    public void testImageButtonDefaultSubmit() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=image name=update value=name src=\"\">" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        WebRequest request = form.getRequest();
        assertEquals( getHostPath() + "/ask?update=name&update.x=0&update.y=0&age=12", request.getURL().toExternalForm() );
    }

                              
    public void testImageButtonPositionalSubmit() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=image name=update value=name src=\"\">" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        WebRequest request = form.getRequest( form.getSubmitButton( "update" ), 10, 15 );
        assertEquals( getHostPath() + "/ask?update=name&update.x=10&update.y=15&age=12", request.getURL().toExternalForm() );
    }

                              
    public void testSubmitButtonAttributes() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=submit name=update value=age>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        SubmitButton[] buttons = form.getSubmitButtons();
        assertEquals( "num detected submit buttons", 1, buttons.length );
        assertEquals( "submit button name", "update", buttons[0].getName() );
        assertEquals( "submit button value", "age", buttons[0].getValue() );
    }

                              
    public void testSubmitButtonSelectionByName() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=submit name=update value=age>" +
                                  "<Input type=submit name=recompute value=age>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        SubmitButton button = form.getSubmitButton( "zork" );
        assertNull( "Found a non-existent button", button );
        button = form.getSubmitButton( "update" );
        assertNotNull( "Didn't find the desired button", button );
        assertEquals( "submit button name", "update", button.getName() );
        assertEquals( "submit button value", "age", button.getValue() );
    }

                              
    public void testSubmitButtonSelectionByNameAndValue() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=submit name=update value=age>" +
                                  "<Input type=submit name=update value=name>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        SubmitButton button = form.getSubmitButton( "update" );
        assertNotNull( "Didn't find the desired button", button );
        assertEquals( "submit button name", "update", button.getName() );
        assertEquals( "submit button value", "age", button.getValue() );
        button = form.getSubmitButton( "update", "name" );
        assertNotNull( "Didn't find the desired button", button );
        assertEquals( "submit button name", "update", button.getName() );
        assertEquals( "submit button value", "name", button.getValue() );
    }

                              
    public void testNamedButtonSubmitString() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=submit name=update value=age>" +
                                  "<Input type=submit name=update value=name>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        WebRequest request = form.getRequest( form.getSubmitButton( "update", "name" ) );
        assertEquals( getHostPath() + "/ask?update=name&age=12", request.getURL().toExternalForm() );
    }

                              
    public void testUnnamedButtonSubmit() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=submit name=update value=age>" +
                                  "<Input type=submit name=update value=name>" +
                                  "</form>" );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebForm form = page.getForms()[0];
        try {
            WebRequest request = form.getRequest();
            fail( "Should not allow submit with unnamed button" );
        } catch (IllegalRequestParameterException e) {
        } 
    }

                              
    public void testForeignSubmitButtonDetection() throws Exception {
        defineWebPage( "Default", "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=submit name=update value=age>" +
                                  "<Input type=submit name=update value=name>" +
                                  "</form>" );
        defineWebPage( "Dupl",    "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=submit name=update value=age>" +
                                  "<Input type=submit name=update value=name>" +
                                  "</form>" );
        defineWebPage( "Wrong",   "<form method=GET action = \"/ask\">" +
                                  "<Input type=text name=age value=12>" +
                                  "<Input type=submit name=save value=age>" +
                                  "</form>" );
        WebResponse other  = _wc.getResponse( getHostPath() + "/Dupl.html" );
        WebResponse page   = _wc.getResponse( getHostPath() + "/Default.html" );
        WebResponse wrong  = _wc.getResponse( getHostPath() + "/Wrong.html" );

        WebForm form = page.getForms()[0];
        WebForm otherForm = other.getForms()[0];
        WebForm wrongForm = wrong.getForms()[0];

        HttpUnitOptions.setParameterValuesValidated( true );
        WebRequest request = form.getRequest( otherForm.getSubmitButtons()[0] );

        HttpUnitOptions.setParameterValuesValidated( false );
        request = form.getRequest( wrongForm.getSubmitButtons()[0] );

        HttpUnitOptions.setParameterValuesValidated( true );
        try {
            request = form.getRequest( wrongForm.getSubmitButtons()[0] );
            fail( "Failed to reject illegal button" );
        } catch (IllegalRequestParameterException e) {
        }
    }

                              
//---------------------------------------------- private members ------------------------------------------------


    private WebConversation _wc;
}

