package com.meterware.servletunit;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2003, Russell Gold
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
import com.meterware.httpunit.HttpUnitUtils;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Map;
import java.util.StringTokenizer;
import java.net.URL;


/**
 *
 * @author <a href="russgold@httpunit.org">Russell Gold</a>
 **/
class RequestContext {

    private Hashtable _parameters = new Hashtable();
    private Hashtable _visibleParameters;
    private RequestContext _parentContext;
    private URL _url;


    RequestContext( URL url ) {
        _url = url;
        String file = _url.getFile();
        if (file.indexOf( '?' ) >= 0) loadParameters( file.substring( file.indexOf( '?' )+1 ) );
    }


    void setParentContext( RequestContext parentContext ) {
        _parentContext = parentContext;
        _visibleParameters = null;
    }


    RequestContext getParentContext() {
        return _parentContext;
    }


    String getRequestURI() {
        return _url.getPath();
    }


    Enumeration getParameterNames() {
        return getParameters().keys();
    }


    Map getParameterMap() {
        return (Map) getParameters().clone();
    }


    String[] getParameterValues( String name ) {
        return (String[]) getParameters().get( name );
    }

    final static private int STATE_INITIAL     = 0;
    final static private int STATE_HAVE_NAME   = 1;
    final static private int STATE_HAVE_EQUALS = 2;
    final static private int STATE_HAVE_VALUE  = 3;


    /**
     * This method employs a state machine to parse a parameter query string.
     * The transition rules are as follows:
     *    State  \          text         '='           '&'
     *    initial:         have_name      -           initial
     *    have_name:          -         have_equals   initial
     *    have_equals:     have_value     -           initial
     *    have_value:         -         initial       initial
     * actions occur on the following transitions:
     *    initial -> have_name:   save token as name
     *    have_equals -> initial: record parameter with null value
     *    have_value  -> initial: record parameter with value
     **/
    void loadParameters( String queryString ) {
        if (queryString.length() == 0) return;
        StringTokenizer st = new StringTokenizer( queryString, "&=", /* return tokens */ true );
        int state = STATE_INITIAL;
        String name  = null;
        String value = null;

        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals( "&" )) {
                state = STATE_INITIAL;
                if (name != null && value != null) addParameter( name, value );
                name  = value = null;
            } else if (token.equals( "=" )) {
                if (state == STATE_HAVE_NAME) {
                    state = STATE_HAVE_EQUALS;
                } else if (state == STATE_HAVE_VALUE) {
                    state = STATE_INITIAL;
                }
            } else if (state == STATE_INITIAL) {
                name = HttpUnitUtils.decode( token );
                value = "";
                state = STATE_HAVE_NAME;
            } else {
                value = HttpUnitUtils.decode( token );
                state = STATE_HAVE_VALUE;
            }
        }
        if (name != null && value != null) addParameter( name, value );
    }


    private void addParameter( String name, String encodedValue ) {
        String[] values = (String[]) _parameters.get( name );
        _visibleParameters = null;
        if (values == null) {
            _parameters.put( name, new String[] { encodedValue } );
        } else {
            _parameters.put( name, extendedArray( values, encodedValue ) );
        }
    }


    private static String[] extendedArray( String[] baseArray, String newValue ) {
        String[] result = new String[ baseArray.length+1 ];
        System.arraycopy( baseArray, 0, result, 0, baseArray.length );
        result[ baseArray.length ] = newValue;
        return result;
    }


    private Hashtable getParameters() {
        if (_visibleParameters == null) {
            if (_parentContext == null) {
                _visibleParameters = _parameters;
            } else {
                _visibleParameters = (Hashtable) _parentContext.getParameters().clone();
                for (Enumeration e = _parameters.keys(); e.hasMoreElements();) {
                    Object key = e.nextElement();
                    _visibleParameters.put( key, _parameters.get( key ) );
                }
            }
        }
        return _visibleParameters;
    }


}
