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
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
/**
 * Represents the parse tree for a segment of HTML.
 * @author Russell Gold
 **/
public interface HTMLSegment {


    /**
     * Returns the forms found in this HTML segment in the order in which they appear.
     * @exception SAXException thrown if there is an error parsing the segment.
     **/
    public WebForm[] getForms() throws SAXException;


    /**
     * Returns the form found in this HTML segment with the specified name.
     * @exception SAXException thrown if there is an error parsing the segment.
     **/
    public WebForm getFormWithName( String name ) throws SAXException;


    /**
     * Returns the links found in this HTML segment in the order in which they appear.
     * @exception SAXException thrown if there is an error parsing the segment.
     **/
    public WebLink[] getLinks() throws SAXException;


    /**
     * Returns the first link which contains the specified text.
     * @exception SAXException thrown if there is an error parsing the segment.
     **/
    public WebLink getLinkWith( String text ) throws SAXException;


    /**
     * Returns the first link which contains an image with the specified text as its 'alt' attribute.
     * @exception SAXException thrown if there is an error parsing the segment.
     **/
    public WebLink getLinkWithImageText( String text ) throws SAXException;


    /**
     * Returns the top-level tables found in this HTML segment in the order in which
     * they appear.
     * @exception SAXException thrown if there is an error parsing the segment.
     **/
    public WebTable[] getTables() throws SAXException;


    /**
     * Returns the first table in this HTML segment which has the specified text as the full text of
     * its first non-blank row and non-blank column. Will recurse into any nested tables, as needed.
     * @return the selected table, or null if none is found
     * @exception SAXException thrown if there is an error parsing the segment.
     **/
    public WebTable getTableStartingWith( final String text ) throws SAXException;
    
    
    /**
     * Returns the first table in this HTML segment which has the specified text as a prefix of the text 
     * in its first non-blank row and non-blank column. Will recurse into any nested tables, as needed.
     * @return the selected table, or null if none is found
     * @exception SAXException thrown if there is an error parsing the segment.
     **/
    public WebTable getTableStartingWithPrefix( String text ) throws SAXException;
    
    
    /**
     * Returns the first table in this HTML segment which has the specified text as its summary attribute. 
     * Will recurse into any nested tables, as needed.
     * @return the selected table, or null if none is found
     * @exception SAXException thrown if there is an error parsing the segment.
     **/
    public WebTable getTableWithSummary( String summary ) throws SAXException;
    
    
    /**
     * Returns the first table in this HTML segment which has the specified text as its ID attribute. 
     * Will recurse into any nested tables, as needed.
     * @return the selected table, or null if none is found
     * @exception SAXException thrown if there is an error parsing the segment.
     **/
    public WebTable getTableWithID( final String ID ) throws SAXException;
    
}
