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
import java.io.IOException;

import java.util.Hashtable;
import java.util.Enumeration;
import java.net.URLEncoder;


/**
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
class UncheckedParameterHolder implements ParameterHolder {

    private Hashtable    _parameters = new Hashtable();
    private static final String[] NO_VALUES = new String[ 0 ];
    private final String _characterSet;


    UncheckedParameterHolder() {
        _characterSet = "iso-8859-1";
    }


    UncheckedParameterHolder( WebRequestSource source ) {
        _characterSet = source.getCharacterSet();
        String[] names = source.getParameterNames();
        for (int i = 0; i < names.length; i++) {
            if (!source.isFileParameter( names[i] )) {
                _parameters.put( names[i], source.getParameterValues( names[i] ) );
            }
        }
    }


    /**
     * Specifies the position at which an image button (if any) was clicked.
     **/
    public void selectImageButtonPosition( SubmitButton imageButton, int x, int y ) {
        setParameter( imageButton.getName() + ".x", Integer.toString( x ) );
        setParameter( imageButton.getName() + ".y", Integer.toString( y ) );
    }


    /**
     * Iterates through the parameters in this holder, recording them in the supplied parameter processor.
     **/
    public void recordParameters( ParameterProcessor processor ) throws IOException {
        Enumeration e = _parameters.keys();

        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            Object value = _parameters.get( name );
            if (value instanceof String) {
                processor.addParameter( name, (String) value, _characterSet );
            } else if (value instanceof String[]) {
                String[] values = (String[]) value;
                for (int i = 0; i < values.length; i++) processor.addParameter( name, values[i], _characterSet );
            } else if (value instanceof UploadFileSpec[]) {
                UploadFileSpec[] files = (UploadFileSpec[]) value;
                for (int i = 0; i < files.length; i++) processor.addFile( name, files[i] );
            }
        }
    }


    public String[] getParameterNames() {
        return (String[]) _parameters.keySet().toArray( new String[ _parameters.size() ] );
    }


    public String getParameterValue( String name ) {
        String[] values = getParameterValues( name );
        return values.length == 0 ? null : values[0];
    }


    public String[] getParameterValues( String name ) {
        Object result = _parameters.get( name );
        if (result instanceof String) return new String[] { (String) result };
        if (result instanceof String[]) return (String[]) result;
        if (result instanceof UploadFileSpec) return new String[] { result.toString() };
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


    public void setParameter( String name, UploadFileSpec[] files ) {
        _parameters.put( name, files );
    }


    public boolean isFileParameter( String name ) {
        return false;
    }
}
