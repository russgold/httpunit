package com.meterware.xml;
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

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public interface WriteableXMLElement {

    /**
     * Returns the names of the attributes of this element in the order in which they should be displayed.
     */
    String[] getAttributeNames();


    /**
     * Returns the names of the nested elements of this element in the order in which they should be displayed.
     */
    String[] getNestedElementNames();


    /**
     * Returns all nested elements with the specified name.
     */
    WriteableXMLElement[] getNestedElements( String elementName );


    /**
     * Returns any text contents of this element.
     */
    String getContents();


    /**
     * Returns true if the specified attribute should be written. Some attributes may be left implicit if they have
     * their default values.
     */
    boolean isExplicitAttribute( String attributeName );


    /**
     * Resets the specified attribute to its default value. Implementations may use this rather than an explicit
     * setter to decide whether to make an attribute explicit.
     */
    void resetAttribute( String attributeName );
}
