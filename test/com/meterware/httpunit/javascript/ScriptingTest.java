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
import com.meterware.httpunit.*;

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
        assertNotNull( "No alert detected", response.getNextAlert() );
        assertEquals( "Alert message", "Ouch!", response.popNextAlert() );
        assertNull( "Alert should have been removed", response.getNextAlert() );
    }


    public void testConfirmationDialog() throws Exception {
        defineWebPage( "OnCommand", "<a href='NextPage' id='go' onClick='return confirm( \"go on?\" );'>" );
        defineResource( "NextPage", "Got the next page!" );

        WebConversation wc = new WebConversation();
        WebResponse wr = wc.getResponse( getHostPath() + "/OnCommand.html" );
        wc.setDialogResponder( new DialogAdapter() {
            public boolean getConfirmation( String confirmationPrompt ) {
                assertEquals( "Confirmation prompt", "go on?", confirmationPrompt );
                return false;
            }
        } );
        wr.getLinkWithID( "go" ).click();
        assertEquals( "Current page", wr, wc.getCurrentPage() );
        wc.setDialogResponder( new DialogAdapter() );
        wr.getLinkWithID( "go" ).click();
        assertEquals( "Page after confirmation", "Got the next page!", wc.getCurrentPage().getText() );
    }


    public void testPromptDialog() throws Exception {
        defineWebPage( "OnCommand", "<a href='NextPage' id='go' onClick='return \"yes\" == prompt( \"go on?\", \"no\" );'>" );
        defineResource( "NextPage", "Got the next page!" );

        WebConversation wc = new WebConversation();
        WebResponse wr = wc.getResponse( getHostPath() + "/OnCommand.html" );
        wr.getLinkWithID( "go" ).click();
        assertEquals( "Current page", wr, wc.getCurrentPage() );

        wc.setDialogResponder( new DialogAdapter() {
            public String getUserResponse( String prompt, String defaultResponse ) {
                assertEquals( "Confirmation prompt", "go on?", prompt );
                assertEquals( "Default response", "no", defaultResponse );
                return "yes";
            }
        } );
        wr.getLinkWithID( "go" ).click();
        assertEquals( "Page after confirmation", "Got the next page!", wc.getCurrentPage().getText() );
    }


    public void testFunctionCallOnLoad() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><script language='JavaScript'>" +
                                            "<!-- " +
                                            "function sayCheese() { alert( \"Cheese!\" ); }" +
                                            "// -->" +
                                            "</script></head>" +
                                            "<body onLoad='sayCheese()'></body>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        assertEquals( "Alert message", "Cheese!", response.popNextAlert() );
    }


    public void testDocumentTitle() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><title>Amazing!</title></head>" +
                                            "<body onLoad='alert(\"Window title is \" + document.title)'></body>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
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
        assertEquals( "Alert message", "found 2 link(s)", response.popNextAlert() );
        assertEquals( "Alert message", "found link 'reallink'", response.popNextAlert() );
        assertEquals( "Alert message", "did not find link 'nolink'", response.popNextAlert() );
        assertNull( "Alert should have been removed", response.getNextAlert() );
    }


    public void testLinkMouseOverEvent() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<form name='realform'><input name='color' value='blue'></form>" +
                                            "<a href='#' onMouseOver=\"document.realform.color.value='green';return false;\">green</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebForm form = response.getFormWithName( "realform" );
        WebLink link = response.getLinks()[0];
        assertEquals( "initial parameter value", "blue", form.getParameterValue( "color" ) );
        link.mouseOver();
        assertEquals( "changed parameter value", "green", form.getParameterValue( "color" ) );
    }


    public void testLinkClickEvent() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<form name='realform'><input name='color' value='blue'></form>" +
                                            "<a href='nothing.html' onClick=\"document.realform.color.value='green';return false;\">green</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebForm form = response.getFormWithName( "realform" );
        WebLink link = response.getLinks()[0];
        assertEquals( "initial parameter value", "blue", form.getParameterValue( "color" ) );
        link.click();
        assertEquals( "changed parameter value", "green", form.getParameterValue( "color" ) );
    }


    public void testScriptDisabled() throws Exception {
        HttpUnitOptions.setScriptingEnabled( false );
        defineResource( "nothing.html", "Should get here" );
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<form name='realform'><input name='color' value='blue'></form>" +
                                            "<a href='nothing.html' onClick=\"document.realform.color.value='green';return false;\">green</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebForm form = response.getFormWithName( "realform" );
        WebLink link = response.getLinks()[0];
        assertEquals( "initial parameter value", "blue", form.getParameterValue( "color" ) );
        link.click();
        assertEquals( "unchanged parameter value", "blue", form.getParameterValue( "color" ) );
        assertEquals( "Expected result", "Should get here", wc.getCurrentPage().getText() );
    }


    public void testHashDestinationOnEvent() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<form name='realform'><input name='color' value='blue'></form>" +
                                            "<a href='#' onClick=\"document.realform.color.value='green';\">green</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebForm form = response.getFormWithName( "realform" );
        WebLink link = response.getLinks()[0];
        assertEquals( "initial parameter value", "blue", form.getParameterValue( "color" ) );
        response = link.click();
        assertEquals( "changed parameter value", "green", response.getFormWithName( "realform" ).getParameterValue( "color" ) );
    }


    public void testLinkIndexes() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><script language='JavaScript'>" +
                                            "function alertLinks() { " +
                                            "  for (var i=0; i < document.links.length; i++) {" +
                                            "    alert( document.links[i].href );" +
                                            "  }" +
                                            "}" +
                                            "</script></head>" +
                                            "<body onLoad='alertLinks()'>" +
                                            "<a href='demo.html'>green</a>" +
                                            "<map name='map1'>" +
                                            "  <area href='guide.html' alt='Guide' shape='rect' coords='0,0,118,28'>" +
                                            "  <area href='search.html' alt='Search' shape='circle' coords='184,200,60'>" +
                                            "</map>" +
                                            "<a href='sample.html'>green</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        assertEquals( "Alert message", getHostPath() + "/demo.html", response.popNextAlert() );
        assertEquals( "Alert message", getHostPath() + "/guide.html", response.popNextAlert() );
        assertEquals( "Alert message", getHostPath() + "/search.html", response.popNextAlert() );
        assertEquals( "Alert message", getHostPath() + "/sample.html", response.popNextAlert() );
        assertNull( "Alert should have been removed", response.getNextAlert() );
    }


    public void testDocumentFindImages() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><script language='JavaScript'>" +
                                            "function getFound( object ) {\n" +
                                            "  return (object == null) ? \"did not find \" : \"found \";\n" +
                                            "  }\n" +
                                            "function viewImages() { \n" +
                                            "  alert( \"found \" + document.images.length + \" images(s)\" );\n" +
                                            "  alert( getFound( document.realimage ) + \"image 'realimage'\" )\n;" +
                                            "  alert( getFound( document.noimage ) + \"image 'noimage'\" );\n" +
                                            "  alert( '2nd image is ' + document.images[1].src ); }\n" +
                                            "</script></head>\n" +
                                            "<body onLoad='viewImages()'>\n" +
                                            "<img name='realimage' src='pict1.gif'>\n" +
                                            "<img name='2ndimage' src='pict2.gif'>\n" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        assertEquals( "Alert message", "found 2 images(s)", response.popNextAlert() );
        assertEquals( "Alert message", "found image 'realimage'", response.popNextAlert() );
        assertEquals( "Alert message", "did not find image 'noimage'", response.popNextAlert() );
        assertEquals( "Alert message", "2nd image is pict2.gif", response.popNextAlert() );
        assertNull( "Alert should have been removed", response.getNextAlert() );
    }


    public void testImageSwap() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<img name='theImage' src='initial.gif'>" +
                                            "<a href='#' onMouseOver=\"document.theImage.src='new.jpg';\">green</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebImage image = response.getImageWithName( "theImage" );
        WebLink link = response.getLinks()[0];
        assertEquals( "initial image source", "initial.gif", image.getSource() );
        link.mouseOver();
        assertEquals( "changed image source", "new.jpg", image.getSource() );
    }


}
