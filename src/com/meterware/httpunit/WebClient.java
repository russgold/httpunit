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
import java.io.IOException;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLConnection;

import java.util.*;

import org.xml.sax.SAXException;


/**
 * The context for a series of web requests. This class manages cookies used to maintain
 * session context, computes relative URLs, and generally emulates the browser behavior
 * needed to build an automated test of a web site.
 *
 * @author Russell Gold
 * @author Jan Ohrstrom
 * @author Seth Ladd
 * @author Oliver Imbusch
 **/
abstract
public class WebClient {

    /**
     * Submits a GET method request and returns a response.
     * @exception SAXException thrown if there is an error parsing the retrieved page
     **/
    public WebResponse getResponse( String urlString ) throws MalformedURLException, IOException, SAXException {
        return getResponse( new GetMethodWebRequest( urlString ) );
    }


    /**
     * Submits a web request and returns a response. This is an alternate name for the getResponse method.
     */
    public WebResponse sendRequest( WebRequest request ) throws MalformedURLException, IOException, SAXException {
        return getResponse( request );
    }


    /**
     * Returns the response representing the current main page.
     */
    public WebResponse getCurrentPage() {
        return getFrameContents( WebRequest.TOP_FRAME );
    }


    /**
     * Submits a web request and returns a response, using all state developed so far as stored in
     * cookies as requested by the server.
     * @exception SAXException thrown if there is an error parsing the retrieved page
     **/
    public WebResponse getResponse( WebRequest request ) throws MalformedURLException, IOException, SAXException {
        tellListeners( request );

        WebResponse response = getResource( request );
        if (response != null) {
            tellListeners( response );
            updateClient( response );
        }

        return getFrameContents( request.getTarget() );
    }


    /**
     * Returns the resource specified by the request. Does not update the client or load included framesets.
     * May return null if the resource is a JavaScript URL which would normally leave the client unchanged.
     */
    public WebResponse getResource( WebRequest request ) throws IOException {
        String urlString = request.getURLString().trim();
        if (urlString.startsWith( "about:" )) {
            return WebResponse.BLANK_RESPONSE;
        } else if (!urlString.startsWith( "javascript:" )) {
            return newResponse( request );
        } else {
            WebRequestSource wrs = request.getWebRequestSource();
            String result = (wrs == null) ? getCurrentPage().getScriptableObject().evaluateURL( urlString )
                                          : wrs.getScriptableDelegate().evaluateURL( urlString );
            if (result == null) return null;

            URL url = new URL( "javascript", null, -1, urlString.substring( "javascript:".length() ), JAVASCRIPT_STREAM_HANDLER );
            return new DefaultWebResponse( this, request.getTarget(), url, result );
        }
    }


    private void tellListeners( WebRequest request ) {
        List listeners;

        synchronized (_clientListeners) {
            listeners = new ArrayList( _clientListeners );
        }

        for (Iterator i = listeners.iterator(); i.hasNext();) {
            ((WebClientListener) i.next()).requestSent( this, request );
        }
    }


    private void tellListeners( WebResponse response ) {
        List listeners;

        synchronized (_clientListeners) {
            listeners = new ArrayList( _clientListeners );
        }

        for (Iterator i = listeners.iterator(); i.hasNext();) {
            ((WebClientListener) i.next()).responseReceived( this, response );
        }
    }


    /**
     * Resets the state of this client, removing all cookies, frames, and per-client headers. This does not affect
     * any listeners or preferences which may have been set.
     **/
    public void clearContents() {
        _frameContents = new FrameHolder( this, WebRequest.TOP_FRAME );
        _cookies = new Hashtable();
        _headers = new HeaderDictionary();
    }


    /**
     * Returns the name of the currently active frames.
     **/
    public String[] getFrameNames() {
        final List names = _frameContents.getActiveFrameNames();
        return (String[]) names.toArray( new String[ names.size() ] );
    }


    /**
     * Returns the response associated with the specified frame name.
     * Throws a runtime exception if no matching frame is defined.
     **/
    public WebResponse getFrameContents( String frameName ) {
        WebResponse response = (WebResponse) _frameContents.get( frameName );
        if (response == null) throw new NoSuchFrameException( frameName );
        return response;
    }


    /**
     * Defines a cookie to be sent to the server on every request.
     **/
    public void addCookie( String name, String value ) {
        _cookies.put( name, value );
    }


    /**
     * Returns the name of all the active cookies which will be sent to the server.
     **/
    public String[] getCookieNames() {
        String[] names = new String[ _cookies.size() ];
        int i = 0;
        for (Enumeration e = _cookies.keys(); e.hasMoreElements();) {
            names[i++] = (String) e.nextElement();
        }
        return names;
    }


    /**
     * Returns the value of the specified cookie.
     **/
    public String getCookieValue( String name ) {
        return (String) _cookies.get( name );
    }


    /**
     * Specifies the user agent identification. Used to trigger browser-specific server behavior.
     **/
    public void setUserAgent( String userAgent ) {
	    setHeaderField( "User-Agent", userAgent );
    }


    /**
     * Returns the current user agent setting.
     **/
    public String getUserAgent() {
	    return getHeaderField( "User-Agent" );
    }


    /**
     * Sets a username and password for a basic authentication scheme.
     **/
    public void setAuthorization( String userName, String password ) {
        setHeaderField( "Authorization", "Basic " + Base64.encode( userName + ':' + password ) );
    }


    /**
     * Sets the value for a header field to be sent with all requests. If the value set is null,
     * removes the header from those to be sent.
     **/
    public void setHeaderField( String fieldName, String fieldValue ) {
        _headers.put( fieldName, fieldValue );
    }


    /**
     * Returns the value for the header field with the specified name. This method will ignore the case of the field name.
     */
    public String getHeaderField( String fieldName ) {
        return (String) _headers.get( fieldName );
    }


    /**
     * Specifies whether an exception will be thrown when an error status (4xx or 5xx) is detected on a response.
     * Defaults to the value returned by HttpUnitOptions.getExceptionsThrownOnErrorStatus.
     **/
    public void setExceptionsThrownOnErrorStatus( boolean throwExceptions ) {
        _exceptionsThrownOnErrorStatus = throwExceptions;
    }


    /**
     * Returns true if an exception will be thrown when an error status (4xx or 5xx) is detected on a response.
     **/
    public boolean getExceptionsThrownOnErrorStatus() {
        return _exceptionsThrownOnErrorStatus;
    }


    /**
     * Adds a listener to watch for requests and responses.
     */
    public void addClientListener( WebClientListener listener ) {
        synchronized (_clientListeners) {
            if (listener != null && !_clientListeners.contains( listener )) _clientListeners.add( listener );
        }
    }


    /**
     * Removes a listener to watch for requests and responses.
     */
    public void removeClientListener( WebClientListener listener ) {
        synchronized (_clientListeners) {
            _clientListeners.remove( listener );
        }
    }


    /**
     * Returns the next javascript alert without removing it from the queue.
     */
    public String getNextAlert() {
        return _alerts.isEmpty() ? null : (String) _alerts.getFirst();
    }


    /**
     * Returns the next javascript alert and removes it from the queue.
     */
    public String popNextAlert() {
        if (_alerts.isEmpty()) throw new IllegalStateException( "Tried to pop a non-existent alert" );
        return (String) _alerts.removeFirst();
    }


    /**
     * Specifies the object which will respond to all dialogs.
     **/
    public void setDialogResponder( DialogResponder responder ) {
        _dialogResponder = responder;
    }


//------------------------------------------ protected members -----------------------------------


    protected WebClient() {
        if (HttpUnitOptions.isAcceptGzip()) {
            setHeaderField( "Accept-Encoding", "gzip" );
        }

        try {
            _frameContents.updateFrames( new DefaultWebResponse( this, null, WebResponse.BLANK_HTML ) );
        } catch (IOException e) {
        } catch (SAXException e) {
        }
    }


    /**
     * Creates a web response object which represents the response to the specified web request.
     **/
    abstract
    protected WebResponse newResponse( WebRequest request ) throws MalformedURLException, IOException;


    /**
     * Writes the message body for the request.
     **/
    final protected void writeMessageBody( WebRequest request, OutputStream stream ) throws IOException {
        request.writeMessageBody( stream );
    }


    /**
     * Returns the value of the cookie header, or null if none is defined.
     **/
    protected String getCookieHeaderField() {
        String names[] = getCookieNames();
        if (names.length == 0) return null;

        StringBuffer sb = new StringBuffer(HttpUnitUtils.DEFAULT_BUFFER_SIZE);
        for (int i = 0; i < names.length; i++) {
            if (i != 0) sb.append( "; " );
            String name = names[i];
            sb.append( name ).append( '=' ).append( getCookieValue( name ) );
        }
        return sb.toString();
    }


    /**
     * Returns the value of all current header fields.
     **/
    protected Dictionary getHeaderFields() {
        Hashtable result = (Hashtable) _headers.clone();
        if (getCookieHeaderField() != null) result.put( "Cookie", getCookieHeaderField() );
        return result;
    }


    /**
     * Updates this web client based on a received response. This includes updating
     * cookies and frames.
     **/
    final
    protected void updateClient( WebResponse response ) throws MalformedURLException, IOException, SAXException {
        updateCookies( response );
        validateHeaders( response );
        if (HttpUnitOptions.getAutoRefresh() && response.getRefreshRequest() != null) {
            getResponse( response.getRefreshRequest() );
        } else if (shouldFollowRedirect( response )) {
            delay( HttpUnitOptions.getRedirectDelay() );
            getResponse( new RedirectWebRequest( response ) );
        } else {
            _frameContents.updateFrames( response );
        }
    }


    private boolean shouldFollowRedirect( WebResponse response ) {
        return HttpUnitOptions.getAutoRedirect()
            && response.getResponseCode() >= HttpURLConnection.HTTP_MOVED_PERM
            && response.getResponseCode() <= HttpURLConnection.HTTP_MOVED_TEMP
            && response.getHeaderField( "Location" ) != null;
    }


//------------------------------------------ package members ------------------------------------


    boolean getConfirmationResponse( String message ) {
        return _dialogResponder.getConfirmation( message );
    }


    String getUserResponse( String message, String defaultResponse ) {
        return _dialogResponder.getUserResponse( message, defaultResponse );
    }


    void postAlert( String message ) {
        _alerts.addLast( message );
    }

//------------------------------------------ private members -------------------------------------

    /** The list of alerts generated by JavaScript. **/
    private LinkedList _alerts = new LinkedList();

    /** The currently defined cookies. **/
    private Hashtable _cookies = new Hashtable();


    /** A map of frame names to current contents. **/
    private FrameHolder _frameContents = new FrameHolder( this, WebRequest.TOP_FRAME );


    /** A map of header names to values. **/
    private HeaderDictionary _headers = new HeaderDictionary();

    private boolean _exceptionsThrownOnErrorStatus = HttpUnitOptions.getExceptionsThrownOnErrorStatus();

    private List _clientListeners = new ArrayList();

    private DialogResponder _dialogResponder = new DialogAdapter();

    private static URLStreamHandler JAVASCRIPT_STREAM_HANDLER = new JavascriptURLStreamHandler();


    /**
     * Examines the headers in the response and throws an exception if appropriate.
     **/
    private void validateHeaders( WebResponse response ) throws HttpException, IOException {
        if (response.getHeaderField( "WWW-Authenticate" ) != null) {
            throw new AuthorizationRequiredException( response.getHeaderField( "WWW-Authenticate" ) );
        } else if (getExceptionsThrownOnErrorStatus()) {
            if (response.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                throw new HttpInternalErrorException( response.getURL() );
            } else if (response.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new HttpNotFoundException( response.getURL() );
            } else if (response.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
                throw new HttpException( response.getResponseCode(), response.getResponseMessage(), response.getURL() );
            }
        }
    }


    /**
     * Updates the cookies maintained in this client based on new cookies requested by the server.
     **/
    private void updateCookies( WebResponse response ) {
        if (!HttpUnitOptions.isAcceptCookies()) return;

        String[] names = response.getNewCookieNames();
        for (int i = 0; i < names.length; i++) {
            addCookie( names[i], response.getNewCookieValue( names[i] ) );
        }
    }


    /**
     * Delays the specified amount of time.
     **/
    private void delay( int numMilliseconds ) {
        if (numMilliseconds == 0) return;
        try {
            Thread.sleep( numMilliseconds );
        } catch (InterruptedException e) {
            // ignore the exception
        }
    }


//==================================================================================================


    static public class HeaderDictionary extends Hashtable {

        public void addEntries( Dictionary source ) {
            for (Enumeration e = source.keys(); e.hasMoreElements(); ) {
                Object key = e.nextElement();
                put( key, source.get( key ) );
            }
        }

        public Object get( Object fieldName ) {
            return (String) super.get( matchPreviousFieldName( fieldName.toString() ) );
        }


        public Object put( Object fieldName, Object fieldValue ) {
            fieldName = matchPreviousFieldName( fieldName.toString() );
            Object oldValue = super.get( fieldName );
            if (fieldValue == null) {
                remove( fieldName );
            } else {
                super.put( fieldName, fieldValue );
            }
            return oldValue;
        }


        /**
         * If a matching field name with different case is already known, returns the older name.
         * Otherwise, returns the specified name.
         **/
        private String matchPreviousFieldName( String fieldName ) {
            for (Enumeration e = keys(); e.hasMoreElements(); ) {
                String key = (String) e.nextElement();
                if (key.equalsIgnoreCase( fieldName )) return key;
            }
            return fieldName;
        }


    }

}


//==================================================================================================


class RedirectWebRequest extends WebRequest {


    RedirectWebRequest( WebResponse response ) throws MalformedURLException {
        super( response.getURL(), response.getHeaderField( "Location" ), response.getTarget() );
    }


    /**
     * Returns the HTTP method defined for this request.
     **/
    public String getMethod() {
        return "GET";
    }
}




//==================================================================================================


class JavascriptURLStreamHandler extends URLStreamHandler {

    protected URLConnection openConnection( URL u ) throws IOException {
        return null;
    }
}





