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
     * Resets the default character set to the platform default encoding.
     **/
    public static void resetDefaultCharacterSet() {
        _characterSet = DEFAULT_CHARACTER_SET;
    }


    /**
     * Sets the default character set for pages which do not specify one. By default, HttpUnit uses the platform
     * default encoding.
     **/
    public static void setDefaultCharacterSet( String characterSet ) {
        _characterSet = characterSet;
    }


    /**
     * Returns the character set to be used for pages which do not specify one.
     **/
    public static String getDefaultCharacterSet() {
        return _characterSet;
    }


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


    /**
     * Returns true if images are treated as text, using their alt attributes.
     **/
    public static boolean getImagesTreatedAsAltText() {
        return _imagesTreatedAsAltText;
    }


    /**
     * If true, tells HttpUnit to treat images with alt attributes as though they were the text
     * value of that attribute in all searches and displays. The default is false (image text is generally ignored).
     **/
    public static void setImagesTreatedAsAltText( boolean asText ) {
        _imagesTreatedAsAltText = asText;
    }


    /**
     * Returns true if HTTP headers are to be dumped to system output.
     **/
    public static boolean isLoggingHttpHeaders() {
        return _loggingHttpHeaders;
    }


    /**
     * If true, tells HttpUnit to log HTTP headers to system output. The default is false.
     **/
    public static void setLoggingHttpHeaders( boolean enabled ) {
        _loggingHttpHeaders = enabled;
    }


    /**
     * Returns the delay, in milliseconds, before a redirect request is issues.
     **/
    public static int getRedirectDelay() {
        return _redirectDelay;
    }


    /**
     * Sets the delay, in milliseconds, before a redirect request is issued. This may be necessary if the server
     * under some cases where the server performs asynchronous processing which must be completed before the
     * new request can be handled properly, and is taking advantage of slower processing by most user agents. It
     * almost always indicates an error in the server design, and therefore the default delay is zero.
     **/
    public static void setRedirectDelay( int delayInMilliseconds ) {
        _redirectDelay = delayInMilliseconds;
    }


//--------------------------------- private members --------------------------------------


    private static String DEFAULT_CHARACTER_SET = "iso-8859-1";

    private static boolean _parserWarningsEnabled;

    private static boolean _parameterValuesValidated = true;

    private static boolean _imagesTreatedAsAltText;

    private static boolean _loggingHttpHeaders;

    private static int _redirectDelay;

    private static String _characterSet = DEFAULT_CHARACTER_SET;

}

