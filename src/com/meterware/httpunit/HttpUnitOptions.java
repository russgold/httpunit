package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2000-2002, Russell Gold
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

import com.meterware.httpunit.scripting.ScriptingEngineFactory;

import java.util.Vector;

/**
 * A collection of global options to control HttpUnit's behavior.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 * @author <a href="mailto:dglo@ssec.wisc.edu">Dave Glowacki</a>
 * @author <a href="mailto:bx@bigfoot.com">Benoit Xhenseval</a>
 **/
public abstract class HttpUnitOptions {

    final static public String DEFAULT_SCRIPT_ENGINE_FACTORY = "com.meterware.httpunit.javascript.JavaScriptEngineFactory";


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
        _autoRedirect = true;
        _checkContentLength = false;
        _redirectDelay = 0;
        _characterSet = HttpUnitUtils.DEFAULT_CHARACTER_SET;
        _contentType = DEFAULT_CONTENT_TYPE;
        _postIncludesCharset = false;
        _acceptGzip = true;
        _acceptCookies = true;
        setScriptEngineClassName( DEFAULT_SCRIPT_ENGINE_FACTORY );
        setScriptingEnabled( true );
        setExceptionsThrownOnScriptError( true );
    }


    public static HTMLParser getHTMLParser() {
        if (_nekoParser != null) {
            return _nekoParser;
        } else if (_jtidyParser != null) {
            return _jtidyParser;
        } else {
            throw new RuntimeException( "No HTML parser found. Make sure that either nekoHTML.jar or Tidy.jar is in the in classpath" );
        }
    }


    /**
     * Returns true if HttpUnit is accepting and saving cookies. The default is to accept them.
     */
    public static boolean isAcceptCookies() {
        return _acceptCookies;
    }


    /**
     * Specifies whether HttpUnit should accept and send cookies.
     */
    public static void setAcceptCookies( boolean acceptCookies ) {
        _acceptCookies = acceptCookies;
    }


    /**
     * Returns true if any WebClient created will accept GZIP encoding of responses. The default is to accept GZIP encoding.
     **/
    public static boolean isAcceptGzip() {
        return _acceptGzip;
    }


    /**
     * Specifies whether a WebClient will be initialized to accept GZIP encoded responses. The default is true.
     */
    public static void setAcceptGzip( boolean acceptGzip ) {
        _acceptGzip = acceptGzip;
    }


    /**
     * Resets the default character set to the HTTP default encoding.
     **/
    public static void resetDefaultCharacterSet() {
        _characterSet = HttpUnitUtils.DEFAULT_CHARACTER_SET;
    }


    /**
     * Resets the default content type to plain text.
     **/
    public static void resetDefaultContentType() {
        _contentType = DEFAULT_CONTENT_TYPE;
    }


    /**
     * Sets the default character set for pages which do not specify one and for requests created without HTML sources.
     * By default, HttpUnit uses the HTTP default encoding, iso-8859-1.
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
     * Returns true if HttpUnit will throw an exception when a message is only partially received. The default is
     * to avoid such checks.
     */
    public static boolean isCheckContentLength() {
        return _checkContentLength;
    }


    /**
     * Specifies whether HttpUnit should throw an exception when the content length of a message does not match its
     * actual received length. Defaults to false.
     */
    public static void setCheckContentLength( boolean checkContentLength ) {
        _checkContentLength = checkContentLength;
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
     * which could not be set via the browser. The default is true (parameters are validated).<br>
     * <b>Note:</b> this only applies to a WebRequest created after this setting is changed. A request created
     * with this option disabled will not only not be checked for correctness, its parameter submission
     * order will not be guaranteed, and changing parameters will not trigger Javascript onChange / onClick events.
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
     * If true, text matches in methods such as {@link HTMLSegment#getLinkWith} are
     * case insensitive. The default is true (matches ignore case).
     **/
    public static boolean getMatchesIgnoreCase() {
        return _matchesIgnoreCase;
    }


    /**
     * If true, text matches in methods such as {@link HTMLSegment#getLinkWith} are
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
     * Returns true if HttpUnit should automatically follow page redirect requests (status 3xx).
     * By default, this is true.
     **/
    public static boolean getAutoRedirect() {
        return _autoRedirect;
    }


    /**
     * Determines whether HttpUnit should automatically follow page redirect requests (status 3xx).
     * By default, this is true in order to simulate normal browser operation.
     **/
    public static void setAutoRedirect( boolean autoRedirect ) {
        _autoRedirect = autoRedirect;
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

    /**
     * Remove an Html error listener.
     **/
    public static void removeHtmlErrorListener(HtmlErrorListener el) {
        _listeners.removeElement(el);
    }

    /**
     * Add an Html error listener.
     **/
    public static void addHtmlErrorListener(HtmlErrorListener el) {
        _listeners.addElement(el);
    }

    /**
     * Get the list of Html Error Listeners
     **/
    public static Vector getHtmlErrorListeners() {
        return _listeners;
    }


    public static String getScriptEngineClassName() {
        return _scriptEngineClassName;
    }


    public static void setScriptEngineClassName( String scriptEngineClassName ) {
        if (_scriptEngineClassName == null || !_scriptEngineClassName.equals( scriptEngineClassName )) {
            _scriptingEngine = null;
        }
        _scriptEngineClassName = scriptEngineClassName;
    }


    public static ScriptingEngineFactory getScriptingEngine() {
        if (_scriptingEngine == null) {
            try {
                Class factoryClass = Class.forName( _scriptEngineClassName );
                final ScriptingEngineFactory factory = (ScriptingEngineFactory) factoryClass.newInstance();
                _scriptingEngine = factory.isEnabled() ? factory : NULL_SCRIPTING_ENGINE_FACTORY;
                _scriptingEngine.setThrowExceptionsOnError( _exceptionsThrownOnScriptError );
            } catch (ClassNotFoundException e) {
                disableScripting( e, "Unable to find scripting engine factory class " );
            } catch (InstantiationException e) {
                disableScripting( e, "Unable to instantiate scripting engine factory class " );
            } catch (IllegalAccessException e) {
                disableScripting( e, "Unable to create scripting engine factory class " );
            }
        }
        return _scriptingEngine;
    }


    public static void setScriptingEnabled( boolean scriptingEnabled ) {
        if (scriptingEnabled != _scriptingEnabled) {
            _scriptingEngine = scriptingEnabled ? null : NULL_SCRIPTING_ENGINE_FACTORY;
        }
        _scriptingEnabled = scriptingEnabled;
    }


    public static boolean isScriptingEnabled() {
        return _scriptingEnabled;
    }



    /**
     * Determines whether script errors result in exceptions or warning messages.
     */
    public static void setExceptionsThrownOnScriptError( boolean throwExceptions ) {
        _exceptionsThrownOnScriptError = throwExceptions;
        getScriptingEngine().setThrowExceptionsOnError( throwExceptions );
    }


    /**
     * Returns true if script errors cause exceptions to be thrown.
     */
    public static boolean getExceptionsThrownOnScriptError() {
        return _exceptionsThrownOnScriptError;
    }


    /**
     * Returns the accumulated script error messages encountered. Error messages are accumulated only
     * if 'throwExceptionsOnError' is disabled.
     */
    public static String[] getScriptErrorMessages() {
        return getScriptingEngine().getErrorMessages();
    }


    /**
     * Clears the accumulated script error messages.
     */
    public static void clearScriptErrorMessages() {
        getScriptingEngine().clearErrorMessages();
    }


    private static void disableScripting( Exception e, String errorMessage ) {
        System.err.println( errorMessage + _scriptEngineClassName );
        System.err.println( "" + e );
        System.err.println( "JavaScript execution disabled");
        _scriptingEngine = NULL_SCRIPTING_ENGINE_FACTORY;
    }


//--------------------------------- private members --------------------------------------


    private static final String DEFAULT_CONTENT_TYPE   = "text/html";

    private static final ScriptingEngineFactory NULL_SCRIPTING_ENGINE_FACTORY = new ScriptingEngineFactory() {
        public boolean isEnabled() { return false; }
        public void associate( WebResponse response ) {}
        public void setThrowExceptionsOnError( boolean throwExceptions ) {}
        public boolean isThrowExceptionsOnError() { return false; }
        public String[] getErrorMessages() { return new String[ 0 ]; }
        public void clearErrorMessages() {}
    };


    private static boolean _acceptGzip = true;

    private static boolean _acceptCookies = true;

    private static boolean _parserWarningsEnabled;

    private static boolean _exceptionsOnErrorStatus = true;

    private static boolean _parameterValuesValidated = true;

    private static boolean _imagesTreatedAsAltText;

    private static boolean _loggingHttpHeaders;

    private static boolean _matchesIgnoreCase = true;

    private static boolean _autoRefresh;

    private static boolean _autoRedirect = true;

    private static boolean _postIncludesCharset = false;

    private static boolean _checkContentLength = false;

    private static int _redirectDelay;

    private static String _characterSet = HttpUnitUtils.DEFAULT_CHARACTER_SET;

    private static String _contentType = DEFAULT_CONTENT_TYPE;

    private static Vector _listeners;

    private static String _scriptEngineClassName;

    private static ScriptingEngineFactory _scriptingEngine;

    private static boolean _scriptingEnabled = true;

    private static boolean _exceptionsThrownOnScriptError = true;

    private static HTMLParser _jtidyParser;

    private static HTMLParser _nekoParser;

    static {
        _listeners = new Vector();
        reset();

        try {
            Class.forName( "org.w3c.tidy.Parser" );
            _jtidyParser = (HTMLParser) Class.forName( "com.meterware.httpunit.JTidyHTMLParser" ).newInstance();
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (ClassNotFoundException e) {
        }

        try {
            Class.forName( "org.cyberneko.html.HTMLConfiguration" );
            _nekoParser = (HTMLParser) Class.forName( "com.meterware.httpunit.NekoHTMLParser" ).newInstance();
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (ClassNotFoundException e) {
        }
    }
}
