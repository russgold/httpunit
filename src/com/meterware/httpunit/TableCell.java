package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2000, Russell Gold
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

import java.util.Stack;
import java.util.Vector;

import org.w3c.dom.*;


/**
 * A single cell in an HTML table.
 **/
public class TableCell extends ParsedHTML {

    
    /**
     * Returns the number of columns spanned by this cell.
     **/
    public int getColSpan() {
        return _colSpan;
    }


    /**
     * Returns the number of rows spanned by this cell.
     **/
    public int getRowSpan() {
        return _rowSpan;
    }


    /**
     * Returns the text value of this cell.
     **/
    public String asText() {
        return getCellContentsAsText( _element );
    }

    
//---------------------------------------- package methods -----------------------------------------


    TableCell( Element cellNode, URL url, String parentTarget ) {
        super( url, parentTarget, cellNode );
        _element = cellNode;
        _colSpan = getAttributeValue( cellNode, "colspan", 1 );
        _rowSpan = getAttributeValue( cellNode, "rowspan", 1 );
    }


//----------------------------------- private fields and methods -----------------------------------


    private Element _element;
    private int     _colSpan;
    private int     _rowSpan;

    private String getCellContentsAsText( Node node ) {
        if (node == null) {
            return "";
        } else if (!node.hasChildNodes()) {
            return "";
        } else {
            return NodeUtils.asText( node.getChildNodes() );
        }
    }



    private int getAttributeValue( Node node, String attributeName, int defaultValue ) {
        return NodeUtils.getAttributeValue( node, attributeName, defaultValue );
    }


}

