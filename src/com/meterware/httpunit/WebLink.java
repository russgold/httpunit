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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.w3c.dom.Node;

/**
 * This class represents a link in an HTML page. Users of this class may examine the
 * structure of the link (as a DOM), or create a {@tag WebRequest} to simulate clicking
 * on the link.
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 * @author <a href="mailto:benoit.xhenseval@avondi.com>Benoit Xhenseval</a>
 **/
public class WebLink extends WebRequestSource {

    /**
     * Creates and returns a web request which will simulate clicking on this link.
     **/
    public WebRequest getRequest() {
        WebRequest request = new GetMethodWebRequest( getBaseURL(), getBareURL(), getTarget() );
        addPresetParameters( request );
        request.setHeaderField( "Referer", getBaseURL().toExternalForm() );
        return request;
    }


    /**
     * Strips a URL from its parameters
     **/
    private String getBareURL() {
        final String url = getURLString();
        final int questionMarkIndex = url.indexOf("?");
        if (questionMarkIndex >= 1 && questionMarkIndex < url.length() - 1) {
            return url.substring(0, questionMarkIndex);
        }
        return url;
    }


    private void addPresetParameters(WebRequest request) {
        Hashtable params = getPresetParameters();
        Enumeration e = params.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            request.setParameter( key, (String[]) params.get( key ) );
        }
    }


    /**
     * Gets all parameters from a URL
     **/
    private String getParametersString() {
        final String url = getURLString();
        final int questionMarkIndex = url.indexOf("?");
        if (questionMarkIndex >= 1 && questionMarkIndex < url.length() - 1) {
            return url.substring(questionMarkIndex + 1);
        }
        return "";
    }


    /**
     * Builds list of parameters
     **/
    private Hashtable getPresetParameters() {
        Hashtable params = new Hashtable();
        StringTokenizer st = new StringTokenizer( getParametersString(), PARAM_DELIM );
        while (st.hasMoreTokens()) stripOneParameter( params, st.nextToken() );
        return params;
    }


    /**
     * add a pair key-value to the hashtable, creates an array of values if param already exists.
     **/
    private void stripOneParameter( Hashtable params, String param ) {
        final int index = param.indexOf( "=" );
        String value = ((index < 0) ? null
                           : ((index == param.length() - 1)
                               ? ""
                               : HttpUnitUtils.decode( param.substring( index + 1 ) )));
        String key = (index < 0) ? param : HttpUnitUtils.decode( param.substring( 0, index ) );
        params.put( key, withNewValue( (String[]) params.get( key ), value ) );
    }


    /**
     * Returns a string array created by appending a string to an existing array. The existing array may be null.
     **/
    private String[] withNewValue( String[] oldValue, String newValue ) {
        String[] result;
        if (oldValue == null) {
            result = new String[] { newValue };
        } else {
            result = new String[ oldValue.length+1 ];
            System.arraycopy( oldValue, 0, result, 0, oldValue.length );
            result[ oldValue.length ] = newValue;
        }
        return result;
    }


    /**
     * Returns the URL referenced by this link. This may be a relative URL.
     **/
    public String getURLString() {
        return NodeUtils.getNodeAttribute( getNode(), "href" );
    }


    /**
     * Returns the text value of this link.
     **/
    public String asText() {
        if (getNode().getNodeName().equals( "area" )) {
            return NodeUtils.getNodeAttribute( getNode(), "alt" );
        } else if (!getNode().hasChildNodes()) {
            return "";
        } else {
            return NodeUtils.asText( getNode().getChildNodes() );
        }
    }


//---------------------------------- package members --------------------------------


    /**
     * Contructs a web link given the URL of its source page and the DOM extracted
     * from that page.
     **/
    WebLink( URL baseURL, String parentTarget, Node node ) {
        super( node, baseURL, parentTarget );
    }

//---------------------------------- private members --------------------------------

    private static final Hashtable NO_PARAMS = new Hashtable();
    private static final String PARAM_DELIM = "&";

}
