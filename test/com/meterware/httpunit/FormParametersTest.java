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
public class FormParametersTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }
    
    
    public static Test suite() {
        return new TestSuite( FormParametersTest.class );
    }


    public FormParametersTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
    }
	
	
    public void testDisabledChoiceParameterValidation() throws Exception {
        ReceivedPage page = new ReceivedPage( _baseURL, HEADER + "<body><form method=GET action = \"/ask\">" +
                                       "<Select name=colors><Option>blue<Option>red</Select>" +
                                       "<Select name=fish><Option value=red>snapper<Option value=pink>salmon</select>" +
                                       "<Select name=media multiple size=2><Option>TV<Option>Radio</select>" +
                                       "</form></body></html>" );
        WebRequest request = page.getForms()[0].getRequest();
        HttpUnitOptions.setParameterValuesValidated( false );
        request.setParameter( "noSuchControl", "green" );
        request.setParameter( "colors", "green" );
        request.setParameter( "fish", "purple" );
        request.setParameter( "media", "CDRom" );
        request.setParameter( "colors", new String[] { "blue", "red" } );
        request.setParameter( "fish", new String[] { "red", "pink" } );
    }

                              
    public void testEnabledChoiceParameterValidation() throws Exception {
        ReceivedPage page = new ReceivedPage( _baseURL, HEADER + "<body><form method=GET action = \"/ask\">" +
                                       "<Select name=colors><Option>blue<Option>red</Select>" +
                                       "<Select name=fish><Option value=red>snapper<Option value=pink>salmon</select>" +
                                       "<Select name=media multiple size=2><Option>TV<Option>Radio</select>" +
                                       "</form></body></html>" );
        WebRequest request = page.getForms()[0].getRequest();
        HttpUnitOptions.setParameterValuesValidated( true );
        validateSetParameterRejected( request, "noSuchControl", "green", "setting of non-existent control" );
        validateSetParameterRejected( request, "colors", "green", "setting of undefined value" );
        validateSetParameterRejected( request, "fish", "snapper", "setting of display value" );
        validateSetParameterRejected( request, "media", "CDRom", "setting list to illegal value" );
        validateSetParameterRejected( request, "colors", new String[] { "blue", "red" }, "setting multiple values on choice" );
        validateSetParameterRejected( request, "media", new String[] { "TV", "CDRom" }, "setting one bad value in a group" );
    
        request.setParameter( "colors", "blue" );
        request.setParameter( "fish", "red" );
        request.setParameter( "media", "TV" );
        request.setParameter( "colors", new String[] { "blue" } );
        request.setParameter( "fish", new String[] { "red" } );
        request.setParameter( "media", new String[] { "TV", "Radio" } );
    }


    public void testDisabledTextParameterValidation() throws Exception {
        ReceivedPage page = new ReceivedPage( _baseURL, HEADER + "<body><form method=GET action = \"/ask\">" +
                                       "<Input type=text name=color>" +
                                       "<Input type=password name=password>" +
                                       "<Input type=hidden name=secret>" +
                                       "</form></body></html>" );
        WebRequest request = page.getForms()[0].getRequest();
        HttpUnitOptions.setParameterValuesValidated( false );
        request.setParameter( "color", "green" );
        request.setParameter( "password", "purple" );
        request.setParameter( "secret", "value" );
        request.setParameter( "colors", new String[] { "blue", "red" } );
        request.setParameter( "fish", new String[] { "red", "pink" } );
        request.setParameter( "secret", new String[] { "red", "pink" } );
    }


    public void testEnabledTextParameterValidation() throws Exception {
        ReceivedPage page = new ReceivedPage( _baseURL, HEADER + "<body><form method=GET action = \"/ask\">" +
                                       "<Input type=text name=color>" +
                                       "<Input type=password name=password>" +
                                       "<Input type=hidden name=secret>" +
                                       "</form></body></html>" );
        WebRequest request = page.getForms()[0].getRequest();
        HttpUnitOptions.setParameterValuesValidated( true );
        request.setParameter( "color", "green" );
        request.setParameter( "password", "purple" );
        request.setParameter( "secret", "value" );
        validateSetParameterRejected( request, "colors", new String[] { "blue", "red" }, "setting input to multiple values" );
        validateSetParameterRejected( request, "fish", new String[] { "red", "pink" }, "setting password to multiple values" );
        validateSetParameterRejected( request, "secret", new String[] { "red", "pink" }, "setting hidden field to multiple values" );
    }


    public void testDisabledRadioButtonValidation() throws Exception {
        ReceivedPage page = new ReceivedPage( _baseURL, HEADER + "<body><form method=GET action = \"/ask\">" +
                                       "<Input type=radio name=color value=red>" +
                                       "<Input type=radio name=color value=blue>" +
                                       "<Input type=radio name=color value=green>" +
                                       "</form></body></html>" );
        WebRequest request = page.getForms()[0].getRequest();
        HttpUnitOptions.setParameterValuesValidated( false );
        request.setParameter( "color", "black" );
        request.setParameter( "color", new String[] { "blue", "red" } );
    }


    public void testEnabledRadioButtonValidation() throws Exception {
        ReceivedPage page = new ReceivedPage( _baseURL, HEADER + "<body><form method=GET action = \"/ask\">" +
                                       "<Input type=radio name=color value=red>" +
                                       "<Input type=radio name=color value=blue>" +
                                       "<Input type=radio name=color value=green>" +
                                       "</form></body></html>" );
        WebRequest request = page.getForms()[0].getRequest();
        HttpUnitOptions.setParameterValuesValidated( true );
        request.setParameter( "color", "red" );
        request.setParameter( "color", "blue" );
        validateSetParameterRejected( request, "color", "black", "setting radio buttons to unknown value" );
        validateSetParameterRejected( request, "color", new String[] { "blue", "red" }, "setting radio buttons to multiple values" );
    }


    public void testCheckboxValidation() throws Exception {
        ReceivedPage page = new ReceivedPage( _baseURL, HEADER + "<body><form method=GET action = \"/ask\">" +
                                       "<Input type=checkbox name=use_color>" +
                                       "</form></body></html>" );
        WebRequest request = page.getForms()[0].getRequest();
        HttpUnitOptions.setParameterValuesValidated( false );
        request.setParameter( "use_color", "red" );

        HttpUnitOptions.setParameterValuesValidated( true );
        request.setParameter( "use_color", "on" );
        request.removeParameter( "use_color" );
        validateSetParameterRejected( request, "use_color", "red", "setting checkbox to a string value" );
    }


//---------------------------------------------- private members ------------------------------------------------


    private void validateSetParameterRejected( WebRequest request, String parameterName, String value, String comment ) throws Exception {
        try {
            request.setParameter( parameterName, value );
            fail( "Did not forbid " + comment );
        } catch (IllegalRequestParameterException e) {
        }
    }

                              
    private void validateSetParameterRejected( WebRequest request, String parameterName, String[] values, String comment ) throws Exception {
        try {
            request.setParameter( parameterName, values );
            fail( "Did not forbid " + comment );
        } catch (IllegalRequestParameterException e) {
        }
    }

                              
    private static URL _baseURL;
     
    static {
        try {
            _baseURL = new URL( "http://www.meterware.com" );
        } catch (java.net.MalformedURLException e ) {}  // ignore
    }

    private final static String HEADER = "<html><head><title>A Sample Page</title></head>";

}
