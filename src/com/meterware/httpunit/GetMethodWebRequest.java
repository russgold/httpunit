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
import java.net.URL;

/**
 * An HTTP request using the GET method.
 **/
public class GetMethodWebRequest extends WebRequest {

    
    /**
     * Constructs a web request using a specific absolute url string.
     **/
    public GetMethodWebRequest( String urlString ) {
        super( urlString );
    }


    /**
     * Constructs a web request using a base URL and a relative url string.
     **/
    public GetMethodWebRequest( URL urlBase, String urlString ) {
        super( urlBase, urlString );
    }


    /**
     * Constructs a web request with a specific target.
     **/
    public GetMethodWebRequest( URL urlBase, String urlString, String target ) {
        super( urlBase, urlString, target );
    }


    /**
     * Returns the HTTP method defined for this request.
     **/
    public String getMethod() {
        return "GET";
    }


//--------------------------------------- package members ---------------------------------------------


    /**
     * Constructs a web request for a form.
     **/
    GetMethodWebRequest( URL urlBase, String urlString, String target, WebForm sourceForm, SubmitButton button ) {
        super( urlBase, urlString, target, sourceForm, button );
    }


//------------------------------------- protected members ---------------------------------------------


    protected String getURLString() {
        if (hasNoParameters()) {
            return super.getURLString();
        } else {
            return super.getURLString() + "?" + getParameterString();
        }
    }


}




