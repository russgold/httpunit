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
import java.io.File;
import java.io.InputStream;

import java.util.Hashtable;
import java.util.Enumeration;
import java.net.URLEncoder;


/**
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
class UncheckedParameterCollection implements ParameterHolder {

    Hashtable    _parameters = new Hashtable();
    private static final String[] NO_VALUES = new String[ 0 ];
    private final String _characterSet;


    UncheckedParameterCollection() {
        _characterSet = "iso-8859-1";
    }


    UncheckedParameterCollection( WebRequestSource source ) {
        _characterSet = source.getCharacterSet();
        String[] names = source.getParameterNames();
        for (int i = 0; i < names.length; i++) {
            if (names[i].length() > 0 && !source.isFileParameter( names[i] )) {
                _parameters.put( names[i], source.getParameterValues( names[i] ) );
            }
        }
    }


    String getParameterString() {
        StringBuffer sb = new StringBuffer(HttpUnitUtils.DEFAULT_BUFFER_SIZE);
        Enumeration e = _parameters.keys();

        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            Object value = _parameters.get( name );
            if (value instanceof String) {
                appendParameter( sb, name, (String) value, e.hasMoreElements() );
            } else {
                appendParameters( sb, name, (String[]) value, e.hasMoreElements() );
            }
        }
        return sb.toString();
    }


    private void appendParameters( StringBuffer sb, String name, String[] values, boolean moreToCome ) {
        for (int i = 0; i < values.length; i++) {
            appendParameter( sb, name, values[i], (i < values.length-1 || moreToCome ) );
        }
    }


    private void appendParameter( StringBuffer sb, String name, String value, boolean moreToCome ) {
        sb.append( encode( name ) );
        if (value != null) sb.append( '=' ).append( encode( value ) );
        if (moreToCome) sb.append( '&' );
    }


    /**
     * Returns a URL-encoded version of the string, including all eight bits, unlike URLEncoder, which strips the high bit.
     **/
    private String encode( String source ) {
        if (_characterSet.equalsIgnoreCase( "iso-8859-1" )) {
            return URLEncoder.encode( source );
        } else {
            try {
                byte[] rawBytes = source.getBytes( _characterSet );
                StringBuffer result = new StringBuffer(HttpUnitUtils.DEFAULT_BUFFER_SIZE);
                for (int i = 0; i < rawBytes.length; i++) {
                    int candidate = rawBytes[i] & 0xff;
                    if (candidate == ' ') {
                        result.append( '+' );
                    } else if ((candidate >= 'A' && candidate <= 'Z') ||
                               (candidate >= 'a' && candidate <= 'z') ||
                               (candidate == '.') ||
                               (candidate >= '0' && candidate <= '9')) {
                        result.append( (char) rawBytes[i] );
                    } else if (candidate < 16) {
                        result.append( "%0" ).append( Integer.toHexString( candidate ).toUpperCase() );
                    } else {
                        result.append( '%' ).append( Integer.toHexString( candidate ).toUpperCase() );
                    }
                }
                return result.toString();
            } catch (java.io.UnsupportedEncodingException e) {
                return "????";    // XXX should pass the exception through as IOException ultimately
            }
        }
    }


    public Enumeration getParameterNames() {
        return _parameters.keys();
    }


    public String getParameterValue( String name ) {
        String[] values = getParameterValues( name );
        return values.length == 0 ? null : values[0];
    }


    public String[] getParameterValues( String name ) {
        Object result = _parameters.get( name );
        if (result instanceof String) return new String[] { (String) result };
        if (result instanceof String[]) return (String[]) result;
        if (result instanceof WebRequest.UploadFileSpec) return new String[] { result.toString() };
        return NO_VALUES;
    }


    public void removeParameter( String name ) {
        _parameters.remove( name );
    }


    public void setParameter( String name, String value ) {
        _parameters.put( name, value );
    }


    public void setParameter( String name, String[] values ) {
        _parameters.put( name, values );
    }


    public boolean isFileParameter( String name ) {
        return false;
    }


    public void selectFile( String parameterName, File file ) {
    }


    public void selectFile( String parameterName, File file, String contentType ) {
    }


    public void selectFile( String parameterName, String fileName, InputStream inputStream, String contentType ) {
    }
}
