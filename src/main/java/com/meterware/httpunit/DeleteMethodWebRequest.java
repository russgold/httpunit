package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2009, Matthew M. Boedicker
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
import org.w3c.dom.Element;

import java.net.URL;

/**
 * An HTTP request using the DELETE method. 
 * RFC 2616 http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html defines: 
 * 
 * 9.7 DELETE
 * 
 * The DELETE method requests that the origin server delete the resource identified
 * by the Request-URI. This method MAY be overridden by human intervention (or other
 * means) on the origin server. The client cannot be guaranteed that the operation
 * has been carried out, even if the status code returned from the origin server
 * indicates that the action has been completed successfully. However, the server
 * SHOULD NOT indicate success unless, at the time the response is given, it
 * intends to delete the resource or move it to an inaccessible location.
 * 
 * A successful response SHOULD be 200 (OK) if the response includes an entity
 * describing the status, 202 (Accepted) if the action has not yet been enacted, or
 * 204 (No Content) if the action has been enacted but the response does not include
 * an entity.
 * 
 * If the request passes through a cache and the Request-URI identifies one or more
 * currently cached entities, those entries SHOULD be treated as stale. Responses to
 * this method are not cacheable. 
**/
public class DeleteMethodWebRequest extends HeaderOnlyWebRequest {

	/**
	 * initialize me - set method to DELETE
	 */
	private void init() {
		super.setMethod("DELETE");
	}

    /**
     * Constructs a web request using a specific absolute url string.
     **/
    public DeleteMethodWebRequest( String urlString ) {
        super( urlString );
        init();
    }


    /**
     * Constructs a web request using a base URL and a relative url string.
     **/
    public DeleteMethodWebRequest( URL urlBase, String urlString ) {
        super( urlBase, urlString );
        init();
    }


    /**
     * Constructs a web request with a specific target.
     **/
    public DeleteMethodWebRequest( URL urlBase, String urlString, String target ) {
        super( urlBase, urlString, target );
        init();
    }


//--------------------------------------- package members ---------------------------------------------


    /**
     * Constructs a web request for a form submitted from JavaScript.
     **/
    DeleteMethodWebRequest( WebForm sourceForm ) {
        super( sourceForm );
        init();
    }


    /**
     * Constructs a web request for a link or image.
     **/
    DeleteMethodWebRequest( FixedURLWebRequestSource source ) {
        super( source );
        init();
    }


    /**
     * Constructs a web request with a specific target.
     **/
    DeleteMethodWebRequest( WebResponse referer, Element sourceElement, URL urlBase, String urlString, String target ) {
        super( referer, sourceElement, urlBase, urlString, target );
        init();
    }


    /**
     * Constructs an initial web request for a frame.
     **/
    DeleteMethodWebRequest( URL urlBase, String urlString, FrameSelector frame ) {
        super( urlBase, urlString, frame );
        init();
    }


    /**
     * Constructs a web request for a javascript open call.
     **/
    DeleteMethodWebRequest( URL urlBase, String urlString, FrameSelector frame, String target ) {
        super( urlBase, urlString, frame, target );
        init();
    }


    /**
     * Constructs a web request for a form.
     **/
    DeleteMethodWebRequest( WebForm sourceForm, ParameterHolder parameterHolder, SubmitButton button, int x, int y ) {
        super( sourceForm, parameterHolder, button, x, y );
        init();
    }


}
