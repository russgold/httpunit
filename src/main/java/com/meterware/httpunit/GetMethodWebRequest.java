package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2000-2002, 2004, Russell Gold
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
 * An HTTP request using the GET method. 
 * RFC 2616 http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html defines: 
 * 
 * 9.3 GET
 * 
 * The GET method means retrieve whatever information (in the form of an entity)
 * is identified by the Request-URI. If the Request-URI refers to a
 * data-producing process, it is the produced data which shall be returned as
 * the entity in the response and not the source text of the process, unless
 * that text happens to be the output of the process.
 * 
 * The semantics of the GET method change to a "conditional GET" if the request
 * message includes an If-Modified-Since, If-Unmodified-Since, If-Match,
 * If-None-Match, or If-Range header field. A conditional GET method requests
 * that the entity be transferred only under the circumstances described by the
 * conditional header field(s). The conditional GET method is intended to reduce
 * unnecessary network usage by allowing cached entities to be refreshed without
 * requiring multiple requests or transferring data already held by the client.
 * 
 * The semantics of the GET method change to a "partial GET" if the request
 * message includes a Range header field. A partial GET requests that only part
 * of the entity be transferred, as described in section 14.35. The partial GET
 * method is intended to reduce unnecessary network usage by allowing
 * partially-retrieved entities to be completed without transferring data
 * already held by the client.
 * 
 * The response to a GET request is cacheable if and only if it meets the
 * requirements for HTTP caching described in section 13.
 * 
 * See section 15.1.3 for security considerations when used for forms.
 **/
public class GetMethodWebRequest extends HeaderOnlyWebRequest {

	/**
	 * initialize me - set method to GET
	 */
	private void init() {
		super.setMethod("GET");
	}

    /**
     * Constructs a web request using a specific absolute url string.
     **/
    public GetMethodWebRequest( String urlString ) {
        super( urlString );
        init();
    }


    /**
     * Constructs a web request using a base URL and a relative url string.
     **/
    public GetMethodWebRequest( URL urlBase, String urlString ) {
        super( urlBase, urlString );
        init();
    }


    /**
     * Constructs a web request with a specific target.
     **/
    public GetMethodWebRequest( URL urlBase, String urlString, String target ) {
        super( urlBase, urlString, target );
        init();
    }


//--------------------------------------- package members ---------------------------------------------


    /**
     * Constructs a web request for a form submitted from JavaScript.
     **/
    GetMethodWebRequest( WebForm sourceForm ) {
        super( sourceForm );
        init();
    }


    /**
     * Constructs a web request for a link or image.
     **/
    GetMethodWebRequest( FixedURLWebRequestSource source ) {
        super( source );
        init();
    }


    /**
     * Constructs a web request with a specific target.
     **/
    GetMethodWebRequest( WebResponse referer, Element sourceElement, URL urlBase, String urlString, String target ) {
        super( referer, sourceElement, urlBase, urlString, target );
        init();
    }


    /**
     * Constructs an initial web request for a frame.
     **/
    GetMethodWebRequest( URL urlBase, String urlString, FrameSelector frame ) {
        super( urlBase, urlString, frame );
        init();
    }


    /**
     * Constructs a web request for a javascript open call.
     **/
    GetMethodWebRequest( URL urlBase, String urlString, FrameSelector frame, String target ) {
        super( urlBase, urlString, frame, target );
        init();
    }


    /**
     * Constructs a web request for a form.
     **/
    GetMethodWebRequest( WebForm sourceForm, ParameterHolder parameterHolder, SubmitButton button, int x, int y ) {
        super( sourceForm, parameterHolder, button, x, y );
        init();
    }


}