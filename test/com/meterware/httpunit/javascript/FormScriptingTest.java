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
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.HttpUnitTest;
import com.meterware.httpunit.WebRequest;

import junit.textui.TestRunner;
import junit.framework.TestSuite;


/**
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
public class FormScriptingTest extends HttpUnitTest {

    public static void main( String args[] ) {
        TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( FormScriptingTest.class );
    }


    public FormScriptingTest( String name ) {
        super( name );
    }


    public void testElementsProperty() throws Exception {
        defineResource( "OnCommand.html", "<html><head><script language='JavaScript'>" +
                                           "function listElements( form ) {\n" +
                                           "  elements = form.elements;\n" +
                                           "  alert( 'form has ' + elements.length + ' elements' );\n" +
                                           "  alert( 'value is ' + elements[0].value );\n" +
                                           "  alert( 'index is ' + elements[1].selectedIndex );\n" +
                                           "  elements[2].checked=true;\n" +
                                           "}" +
                                           "</script></head>" +
                                          "<body>" +
                                          "<form name='the_form'>" +
                                          "  <input type=text name=first value='Initial Text'>" +
                                          "  <select name='choices'>" +
                                          "    <option>red" +
                                          "    <option selected>blue" +
                                          "  </select>" +
                                          "  <input type=checkbox name=ready>" +
                                          "</form>" +
                                          "<a href='#' onClick='listElements( document.the_form )'>elements</a>" +
                                          "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        final WebForm form = response.getFormWithName( "the_form" );
        assertEquals( "Initial state", null, form.getParameterValue( "ready" ) );

        response.getLinks()[ 0 ].click();
        assertEquals( "Message 1", "form has 3 elements", response.popNextAlert() );
        assertEquals( "Message 2", "value is Initial Text", response.popNextAlert() );
        assertEquals( "Message 3", "index is 1", response.popNextAlert() );

        assertEquals( "Changed state", "on", form.getParameterValue( "ready" ) );
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
