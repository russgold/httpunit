package com.meterware.httpunit;
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
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.List;

import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
public class WebWindow {

    /** The client which created this window. **/
    private WebClient _client;

    /** A map of frame names to current contents. **/
    private FrameHolder _frameContents;

    /** The name of the window, set via JavaScript. **/
    private String _name = "";

    /** The web response containing the reference that opened this window **/
    private WebResponse _opener;


    public WebClient getClient() {
        return _client;
    }


    /**
     * Returns the name of this window. Windows created through normal HTML or browser commands have empty names,
     * but JavaScript can set the name. A name may be used as a target for a request.
     */
    public String getName() {
        return _name;
    }


    /**
     * Returns the web response that contained the script which opened this window.
     */
    public WebResponse getOpener() {
        return _opener;
    }


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
     * Submits a web request and returns a response, using all state developed so far as stored in
     * cookies as requested by the server.
     * @exception SAXException thrown if there is an error parsing the retrieved page
     **/
    public WebResponse getResponse( WebRequest request ) throws MalformedURLException, IOException, SAXException {
        WebResponse response = _client.getResourceForWindow( request, this );

        return response == null ? null : updateWindow( request.getTarget(), response );
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
     * Returns the response representing the main page in this window.
     */
    public WebResponse getCurrentPage() {
        return getFrameContents( WebRequest.TOP_FRAME );
    }


    WebWindow( WebClient client ) {
        _client = client;
        _frameContents = new FrameHolder( _client, WebRequest.TOP_FRAME );
    }


    WebWindow( WebClient client, WebResponse opener ) {
        this( client );
        _opener = opener;
    }


    /**
     * Updates this web client based on a received response. This includes updating
     * cookies and frames.
     **/
    WebResponse updateWindow( String requestTarget, WebResponse response ) throws MalformedURLException, IOException, SAXException {
        _client.updateClient( response );
        if (HttpUnitOptions.getAutoRefresh() && response.getRefreshRequest() != null) {
            return getResponse( response.getRefreshRequest() );
        } else if (shouldFollowRedirect( response )) {
            delay( HttpUnitOptions.getRedirectDelay() );
            return getResponse( new RedirectWebRequest( response ) );
        } else {
            _client.updateFrameContents( this, requestTarget, response );
            return response;
        }
    }


    /**
     * Returns the resource specified by the request. Does not update the client or load included framesets.
     * May return null if the resource is a JavaScript URL which would normally leave the client unchanged.
     */
    WebResponse getResource( WebRequest request ) throws IOException {
        return _client.getResourceForWindow( request, this );
    }


    void updateFrameContents( WebResponse response ) throws IOException, SAXException {
        response.setWindow( this );
        _frameContents.updateFrames( response, response.getFrameName() );
    }


    void setName( String name ) {
        _name = name;
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


    private boolean shouldFollowRedirect( WebResponse response ) {
        return HttpUnitOptions.getAutoRedirect()
            && response.getResponseCode() >= HttpURLConnection.HTTP_MOVED_PERM
            && response.getResponseCode() <= HttpURLConnection.HTTP_MOVED_TEMP
            && response.getHeaderField( "Location" ) != null;
    }



}
