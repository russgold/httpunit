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
import com.meterware.httpunit.WebImage;
import com.meterware.httpunit.WebRequest;

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


    public void testFunctionCallOnLoad() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><script language='JavaScript'>" +
                                         //   "<!-- " +
                                            "function sayCheese() { alert( \"Cheese!\" ); }" +
                                         //   "// -->" +
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


    public void testSetFormTextValue() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body onLoad=\"document.realform.color.value='green'\">" +
                                            "<form name='realform'><input name='color' value='blue'></form>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebForm form = response.getFormWithName( "realform" );
        assertEquals( "color parameter value", "green", form.getParameterValue( "color" ) );
    }


    public void testSetFormTextOnChangeEvent() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<form name='the_form'>" +
                                            "  <input name='color' value='blue' " +
                                            "         onChange='alert( \"color is now \" + document.the_form.color.value );'>" +
                                            "</form>" +
                                            "<a href='#' onClick='document.the_form.color.value=\"green\";'>green</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebForm form = response.getFormWithName( "the_form" );
        assertEquals( "Initial state", "blue", form.getParameterValue( "color" ) );

        assertEquals( "Alert message before change", null, response.getNextAlert() );
        form.setParameter( "color", "red" );
        assertEquals( "Alert after change", "color is now red", response.popNextAlert() );

        assertEquals( "Changed state", "red", form.getParameterValue( "color" ) );
        response.getLinks()[ 0 ].click();
        assertEquals( "Final state", "green", form.getParameterValue( "color" ) );
        assertEquals( "Alert message after JavaScript change", null, response.getNextAlert() );
    }


    public void testCheckboxSetChecked() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<form name='realform'><input type='checkbox' name='ready'></form>" +
                                            "<a href='#' name='clear' onMouseOver='document.realform.ready.checked=false;'>clear</a>" +
                                            "<a href='#' name='set' onMouseOver='document.realform.ready.checked=true;'>set</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebForm form = response.getFormWithName( "realform" );
        assertEquals( "initial parameter value", null, form.getParameterValue( "ready" ) );
        response.getLinkWithName( "set" ).mouseOver();
        assertEquals( "changed parameter value", "on", form.getParameterValue( "ready" ) );
        response.getLinkWithName( "clear" ).mouseOver();
        assertEquals( "final parameter value", null, form.getParameterValue( "ready" ) );
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


    public void testFormActionProperty() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage( "Default", "<form method=GET name='the_form' action = 'ask'>" +
                                  "<Input type=text name=age>" +
                                  "<Input type=submit value=Go>" +
                                  "</form>" +
                                  "<a href='#' name='doTell' onClick='document.the_form.action=\"tell\";'>tell</a>" +
                                  "<a href='#' name='doShow' onClick='alert( document.the_form.action );'>show</a>" );
        WebResponse page = wc.getResponse( getHostPath() + "/Default.html" );
        page.getLinkWithName( "doShow" ).click();
        assertEquals( "Current action", "ask", page.popNextAlert() );
        page.getLinkWithName( "doTell" ).click();

        WebRequest request = page.getForms()[0].getRequest();
        request.setParameter( "age", "23" );
        assertEquals( getHostPath() + "/tell?age=23", request.getURL().toExternalForm() );
    }


    public void testFormValidationOnSubmit() throws Exception {
        defineResource( "doIt?color=pink", "You got it!", "text/plain" );
        defineResource( "OnCommand.html",  "<html><head><script language='JavaScript'>" +
                                           "function verifyForm() { " +
                                           "  if (document.realform.color.value == 'pink') {" +
                                           "    return true;" +
                                           "  } else {" +
                                           "    alert( 'wrong color' );" +
                                           "    return false;" +
                                           "  }" +
                                           "}" +
                                           "</script></head>" +
                                           "<body>" +
                                           "<form name='realform' action='doIt' onSubmit='return verifyForm();'>" +
                                           "  <input name='color' value='blue'>" +
                                           "</form>" +
                                           "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebForm form = response.getFormWithName( "realform" );
        form.submit();
        assertEquals( "Alert message", "wrong color", response.popNextAlert() );
        assertSame( "Current response", response, wc.getCurrentPage() );
        form.setParameter( "color", "pink" );
        WebResponse newResponse = form.submit();
        assertEquals( "Result of submit", "You got it!", newResponse.getText() );
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


    public void testFormSelectReadableProperties() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><script language='JavaScript'>" +
                                            "function viewSelect( choices ) { \n" +
                                            "  alert( 'select has ' + choices.options.length + ' options' )\n;" +
                                            "  alert( 'select still has ' + choices.length + ' options' )\n;" +
                                            "  alert( 'select option ' + choices.options[0].index + ' is ' + choices.options[0].text )\n;" +
                                            "  alert( 'select 2nd option value is ' + choices.options[1].value )\n;" +
                                            "  if (choices.options[0].selected) alert( 'red selected' );\n" +
                                            "  if (choices.options[1].selected) alert( 'blue selected' );\n" +
                                            "}\n" +
                                            "</script></head>" +
                                            "<body onLoad='viewSelect( document.the_form.choices )'>" +
                                            "<form name='the_form'>" +
                                            "  <select name='choices'>" +
                                            "    <option value='1'>red" +
                                            "    <option value='3' selected>blue" +
                                            "  </select>" +
                                            "</form>" +
                                            "<a href='#' onMouseOver=\"alert( 'selected #' + document.the_form.choices.selectedIndex );\">which</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        assertEquals( "1st message", "select has 2 options", response.popNextAlert() );
        assertEquals( "2nd message", "select still has 2 options", response.popNextAlert() );
        assertEquals( "3rd message", "select option 0 is red", response.popNextAlert() );
        assertEquals( "4th message", "select 2nd option value is 3", response.popNextAlert() );
        assertEquals( "5th message", "blue selected", response.popNextAlert() );

        response.getLinks()[0].mouseOver();
        assertEquals( "before change message", "selected #1", response.popNextAlert() );
        response.getFormWithName( "the_form" ).setParameter( "choices", "1" );
        response.getLinks()[0].mouseOver();
        assertEquals( "after change message", "selected #0", response.popNextAlert() );
    }


    public void testFormSelectOnChangeEvent() throws Exception {
        defineResource( "OnCommand.html", "<html><head><script language='JavaScript'>" +
                                            "function selectOptionNum( the_select, index ) { \n" +
                                            "  for (var i = 0; i < the_select.length; i++) {\n" +
                                            "      the_select.options[i].selected = (i == index);\n" +
                                            "  }\n" +
                                            "}\n" +
                                            "</script></head>" +
                                          "<body>" +
                                          "<form name='the_form'>" +
                                          "  <select name='choices' onChange='alert( \"Selected index is \" + document.the_form.choices.selectedIndex );'>" +
                                          "    <option>red" +
                                          "    <option selected>blue" +
                                          "  </select>" +
                                          "</form>" +
                                          "<a href='#' onClick='selectOptionNum( document.the_form.choices, 0 )'>reset</a>" +
                                          "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        final WebForm form = response.getFormWithName( "the_form" );
        assertEquals( "Initial state", "blue", form.getParameterValue( "choices" ) );

        assertEquals( "Alert message before change", null, response.getNextAlert() );
        form.setParameter( "choices", "red" );
        assertEquals( "Alert after change", "Selected index is 0", response.popNextAlert() );
        form.setParameter( "choices", "blue" );
        assertEquals( "Alert after change", "Selected index is 1", response.popNextAlert() );

        assertEquals( "Initial state", "blue", form.getParameterValue( "choices" ) );
        response.getLinks()[ 0 ].click();
        assertEquals( "Final state", "red", form.getParameterValue( "choices" ) );
        assertEquals( "Alert message after JavaScript change", null, response.getNextAlert() );
    }


    public void testFormSelectWriteableProperties() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><script language='JavaScript'>" +
                                            "function selectOptionNum( the_select, index ) { \n" +
                                            "  for (var i = 0; i < the_select.length; i++) {\n" +
                                            "      the_select.options[i].selected = (i == index);\n" +
                                            "  }\n" +
                                            "}\n" +
                                            "</script></head>" +
                                            "<body>" +
                                            "<form name='the_form'>" +
                                            "  <select name='choices'>" +
                                            "    <option value='1'>red" +
                                            "    <option value='3' selected>blue" +
                                            "    <option value='5'>green" +
                                            "    <option value='7'>azure" +
                                            "  </select>" +
                                            "</form>" +
                                            "<a href='#' onClick='selectOptionNum( document.the_form.choices, 2 )'>green</a>" +
                                            "<a href='#' onClick='selectOptionNum( document.the_form.choices, 0 )'>red</a>" +
                                            "<a href='#' onClick='document.the_form.choices.options[0].value=\"9\"'>red</a>" +
                                            "<a href='#' onClick='document.the_form.choices.options[0].text=\"orange\"'>orange</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebForm form = response.getFormWithName( "the_form" );
        assertEquals( "initial selection", "3", form.getParameterValue( "choices" ) );

        response.getLinks()[0].click();
        assertEquals( "2nd selection", "5", form.getParameterValue( "choices" ) );
        response.getLinks()[1].click();
        assertEquals( "3rd selection", "1", form.getParameterValue( "choices" ) );
        response.getLinks()[2].click();
        assertEquals( "4th selection", "9", form.getParameterValue( "choices" ) );

        assertMatchingSet( "Displayed options", new String[] { "red", "blue", "green", "azure" }, form.getOptions( "choices" ) );
        response.getLinks()[3].click();
        assertMatchingSet( "Modified options", new String[] { "orange", "blue", "green", "azure" }, form.getOptions( "choices" ) );
    }


    public void testFormSelectOverwriteOptions() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><script language='JavaScript'>" +
                                            "function rewriteSelect( the_select ) { \n" +
                                            "  the_select.options[0] = new Option( 'apache', 'a' );\n" +
                                            "  the_select.options[1] = new Option( 'comanche', 'c' );\n" +
                                            "  the_select.options[2] = new Option( 'sioux', 'x' );\n" +
                                            "  the_select.options[3] = new Option( 'iriquois', 'q' );\n" +
                                            "}\n" +
                                            "</script></head>" +
                                            "<body>" +
                                            "<form name='the_form'>" +
                                            "  <select name='choices'>" +
                                            "    <option value='1'>red" +
                                            "    <option value='2'>yellow" +
                                            "    <option value='3' selected>blue" +
                                            "    <option value='5'>green" +
                                            "  </select>" +
                                            "</form>" +
                                            "<a href='#' onMouseOver='document.the_form.choices.options.length=3;'>shorter</a>" +
                                            "<a href='#' onMouseOver='document.the_form.choices.options[1]=null;'>weed</a>" +
                                            "<a href='#' onMouseOver='rewriteSelect( document.the_form.choices );'>replace</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebForm form = response.getFormWithName( "the_form" );
        assertMatchingSet( "initial values", new String[] { "1", "2", "3", "5" }, form.getOptionValues( "choices" ) );
        assertMatchingSet( "initial text", new String[] { "red", "yellow", "blue", "green" }, form.getOptions( "choices" ) );

        response.getLinks()[0].mouseOver();
        assertMatchingSet( "modified values", new String[] { "1", "2", "3" }, form.getOptionValues( "choices" ) );
        assertMatchingSet( "modified text", new String[] { "red", "yellow", "blue" }, form.getOptions( "choices" ) );

        response.getLinks()[1].mouseOver();
        assertMatchingSet( "weeded values", new String[] { "1", "3" }, form.getOptionValues( "choices" ) );
        assertMatchingSet( "weeded text", new String[] { "red", "blue" }, form.getOptions( "choices" ) );

        response.getLinks()[2].mouseOver();
        assertMatchingSet( "replaced values", new String[] { "a", "c", "x", "q" }, form.getOptionValues( "choices" ) );
        assertMatchingSet( "replaced text", new String[] { "apache", "comanche", "sioux", "iriquois" }, form.getOptions( "choices" ) );
    }

}
