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

import java.util.Enumeration;


/**
 * This interface is implemented by classes which hold parameters for web requests.
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
interface ParameterHolder {


    /**
     * Returns an enumeration of all parameter names in this collection.
     **/
    Enumeration getParameterNames();


    /**
     * Returns the multiple default values of the named parameter.
     **/
    String[] getParameterValues( String name );


    /**
     * Removes a parameter name from this collection.
     **/
    void removeParameter( String name );


    /**
     * Sets the value of a parameter in a web request.
     **/
    void setParameter( String name, String value );


    /**
     * Sets the multiple values of a parameter in a web request.
     **/
    void setParameter( String name, String[] values );


    /**
     * Returns true if the specified parameter is a file field.
     **/
    boolean isFileParameter( String name );


    /**
     * Sets the file for a parameter upload in a web request.
     **/
    void selectFile( String parameterName, File file );


    /**
     * Sets the file for a parameter upload in a web request.
     **/
    void selectFile( String parameterName, File file, String contentType );


    /**
     * Sets the file for a parameter upload in a web request.
     **/
    void selectFile( String parameterName, String fileName, InputStream inputStream, String contentType );


}
