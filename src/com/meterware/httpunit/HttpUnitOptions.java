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


/**
 * A collection of global options to control HttpUnit's behavior.
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 * @author <a href="mailto:dglo@ssec.wisc.edu">Dave Glowacki</a>
 **/
abstract
public class HttpUnitOptions {

    /**
     *  Resets all options to their default values.
     */
    public static void reset() {
        _parserWarningsEnabled = false;
        _exceptionsOnErrorStatus = true;
        _parameterValuesValidated = true;
        _imagesTreatedAsAltText = false;
        _loggingHttpHeaders = false;
        _matchesIgnoreCase = true;
        _autoRefresh = false;
        _redirectDelay = 0;
        _characterSet = DEFAULT_CHARACTER_SET;
        _contentType = DEFAULT_CONTENT_TYPE;
        _postIncludesCharset = true;
    }


    /**
     * Resets the default character set to the HTTP default encoding.
     **/
    public static void resetDefaultCharacterSet() {
        _characterSet = DEFAULT_CHARACTER_SET;
    }


    /**
     * Resets the default content type to plain text.
     **/
    public static void resetDefaultContentType() {
        _contentType = DEFAULT_CONTENT_TYPE;
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
     * Determines whether a normal POST request will include the character set in the content-type header.
     * The default is to include it; however, some older servlet engines (most notably Tomcat 3.1) get confused
     * when they see it.
     **/
    public static void setPostIncludesCharset( boolean postIncludesCharset )
    {
        _postIncludesCharset = postIncludesCharset;
    }


    /**
     * Returns true if POST requests should include the character set in the content-type header.
     **/
    public static boolean isPostIncludesCharset()
    {
        return _postIncludesCharset;
    }


    /**
     * Sets the default content type for pages which do not specify one.
     **/
    public static void setDefaultContentType( String contentType ) {
        _contentType = contentType;
    }


    /**
     * Returns the content type to be used for pages which do not specify one.
     **/
    public static String getDefaultContentType() {
        return _contentType;
    }


    /**
     * Returns true if parser warnings are enabled.
     **/
    public static boolean getParserWarningsEnabled() {
        return _parserWarningsEnabled;
    }


    /**
     * If true, WebClient.getResponse throws an exception when it receives an error status.
     * Defaults to true.
     **/
    public static void setExceptionsThrownOnErrorStatus( boolean enabled ) {
        _exceptionsOnErrorStatus = enabled;
    }


    /**
     * Returns true if WebClient.getResponse throws exceptions when detected an error status.
     **/
    public static boolean getExceptionsThrownOnErrorStatus() {
        return _exceptionsOnErrorStatus;
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
     * If true, text matches in methods such as {@link HTMLFragment#getLinkWith} are
     * case insensitive. The default is true (matches ignore case).
     **/
    public static boolean getMatchesIgnoreCase() {
        return _matchesIgnoreCase;
    }


    /**
     * If true, text matches in methods such as {@link HTMLFragment#getLinkWith} are
     * case insensitive. The default is true (matches ignore case).
     **/
    public static void setMatchesIgnoreCase( boolean ignoreCase ) {
        _matchesIgnoreCase = ignoreCase;
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


    /**
     * Returns true if HttpUnit should automatically follow page refresh requests. 
     * By default, this is false, so that programs can verify the redirect page presented
     * to users before the browser switches to the new page.
     **/
    public static boolean getAutoRefresh() {
        return _autoRefresh;
    }


    /**
     * Specifies whether HttpUnit should automatically follow page refresh requests. 
     * By default, this is false, so that programs can verify the redirect page presented
     * to users before the browser switches to the new page. Setting this to true can
     * cause an infinite loop on pages that refresh themselves.
     **/
    public static void setAutoRefresh( boolean autoRefresh ) {
        _autoRefresh = autoRefresh;
    }


//--------------------------------- private members --------------------------------------


    private static String DEFAULT_CONTENT_TYPE   = "text/plain";
    private static String DEFAULT_CONTENT_HEADER = DEFAULT_CONTENT_TYPE;
    private static String DEFAULT_CHARACTER_SET  = "iso-8859-1";

    private static boolean _parserWarningsEnabled;

    private static boolean _exceptionsOnErrorStatus = true;

    private static boolean _parameterValuesValidated = true;

    private static boolean _imagesTreatedAsAltText;

    private static boolean _loggingHttpHeaders;

    private static boolean _matchesIgnoreCase = true;

    private static boolean _autoRefresh;

    private static boolean _postIncludesCharset = true;

    private static int _redirectDelay;

    private static String _characterSet = DEFAULT_CHARACTER_SET;

    private static String _contentType = DEFAULT_CONTENT_TYPE;
}

