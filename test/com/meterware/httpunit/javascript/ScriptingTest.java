package com.meterware.httpunit.javascript;
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
import com.meterware.httpunit.HttpUnitTest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;

import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
public class ScriptingTest extends HttpUnitTest {

    public static void main( String args[] ) {
        TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( ScriptingTest.class );
    }


    public ScriptingTest( String name ) {
        super( name );
    }


    public void testSingleCommandOnLoad() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body onLoad='alert(\"Ouch!\")'></body>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        JavaScript.run( response );
        assertNotNull( "No alert detected", response.getNextAlert() );
        assertEquals( "Alert message", "Ouch!", response.popNextAlert() );
        assertNull( "Alert should have been removed", response.getNextAlert() );
    }


    public void testFunctionCallOnLoad() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><script language='JavaScript'>" +
                                         //   "<!-- " +
                                            "function sayCheese() { alert( \"Cheese!\" ); }" +
                                         //   "// -->" +
                                            "</script></head>" +
                                            "<body onLoad='sayCheese()'></body>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        JavaScript.run( response );
        assertEquals( "Alert message", "Cheese!", response.popNextAlert() );
    }


    public void testDocumentTitle() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><title>Amazing!</title></head>" +
                                            "<body onLoad='alert(\"Window title is \" + document.title)'></body>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        JavaScript.run( response );
        assertEquals( "Alert message", "Window title is Amazing!", response.popNextAlert() );
    }


    public void testDocumentFindForms() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><script language='JavaScript'>" +
                                            "function getFound( object ) {" +
                                            "  return (object == null) ? \"did not find \" : \"found \";" +
                                            "  }" +
                                            "function viewForms() { " +
                                            "  alert( \"found \" + document.forms.length + \" form(s)\" );" +
                                            "  alert( getFound( document.realform ) + \"form 'realform'\" );" +
                                            "  alert( getFound( document.noform ) + \"form 'noform'\" ); }" +
                                            "</script></head>" +
                                            "<body onLoad='viewForms()'>" +
                                            "<form name='realform'></form>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        JavaScript.run( response );
        assertEquals( "Alert message", "found 1 form(s)", response.popNextAlert() );
        assertEquals( "Alert message", "found form 'realform'", response.popNextAlert() );
        assertEquals( "Alert message", "did not find form 'noform'", response.popNextAlert() );
        assertNull( "Alert should have been removed", response.getNextAlert() );
    }


    public void testDocumentFindLinks() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><script language='JavaScript'>" +
                                            "function getFound( object ) {" +
                                            "  return (object == null) ? \"did not find \" : \"found \";" +
                                            "  }" +
                                            "function viewLinks() { " +
                                            "  alert( \"found \" + document.links.length + \" link(s)\" );" +
                                            "  alert( getFound( document.reallink ) + \"link 'reallink'\" );" +
                                            "  alert( getFound( document.nolink ) + \"link 'nolink'\" );" +
                                            "}" +
                                            "</script></head>" +
                                            "<body onLoad='viewLinks()'>" +
                                            "<a href='something' name='reallink'>first</a>" +
                                            "<a href='else'>second</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        JavaScript.run( response );
        assertEquals( "Alert message", "found 2 link(s)", response.popNextAlert() );
        assertEquals( "Alert message", "found link 'reallink'", response.popNextAlert() );
        assertEquals( "Alert message", "did not find link 'nolink'", response.popNextAlert() );
        assertNull( "Alert should have been removed", response.getNextAlert() );
    }


    public void testSetFormFieldValue() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body onLoad=\"document.realform.color.value='green'\">" +
                                            "<form name='realform'><input name='color' value='blue'></form>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        JavaScript.run( response );
        WebForm form = response.getFormWithName( "realform" );
        assertEquals( "color parameter value", "green", form.getParameterValue( "color" ) );
    }


    public void testLinkMouseOverEvent() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<form name='realform'><input name='color' value='blue'></form>" +
                                            "<a href='#' onMouseOver=\"document.realform.color.value='green'\">green</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        JavaScript.run( response );
        WebForm form = response.getFormWithName( "realform" );
        WebLink link = response.getLinks()[0];
        assertEquals( "initial parameter value", "blue", form.getParameterValue( "color" ) );
        link.mouseOver();
        assertEquals( "changed parameter value", "green", form.getParameterValue( "color" ) );
    }


}
