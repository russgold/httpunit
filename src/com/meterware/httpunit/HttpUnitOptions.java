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


/**
 * A collection of global options to control testing.
 *
 * @author Russell Gold
 **/
abstract
public class HttpUnitOptions {


    /**
     * Returns true if parser warnings are enabled.
     **/
    public static boolean getParserWarningsEnabled() {
        return _parserWarningsEnabled;
    }


    /**
     * If true, tells the parser to display warning messages. The default is false (warnings are not shown).
     **/
    public static void setParserWarningsEnabled( boolean enabled ) {
        _parserWarningsEnabled = enabled;
    }


    /**
     * Returns true if form parameter settings are checked.
     **/
    public static boolean getParameterValuesValidated() {
        return _parameterValuesValidated;
    }


    /**
     * If true, tells HttpUnit to throw an exception on any attempt to set a form parameter to a value
     * which could not be set via the browser. The default is true (parameters are validated).
     **/
    public static void setParameterValuesValidated( boolean validated ) {
        _parameterValuesValidated = validated;
    }


//--------------------------------- private members --------------------------------------


    private static boolean _parserWarningsEnabled;

    private static boolean _parameterValuesValidated = true;

}

