package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2000-2002, Russell Gold
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

import org.w3c.dom.Node;

/**
 * A frame in a web page.
 **/
class WebFrame {


//---------------------------------------- package methods -----------------------------------------


    WebFrame( URL baseURL, Node frameNode, String parentFrameName ) {
        _element = frameNode;
        _baseURL = baseURL;
        _name    = getFrameName( parentFrameName );
    }


    String getName() {
        return _name;
    }


    private String getFrameName( String parentFrameName ) {
        final String relativeName = NodeUtils.getNodeAttribute( _element, "name" );
        if (relativeName.length() == 0) return toString();
        else return getNestedFrameName( parentFrameName, relativeName );
    }


    /**
     * Given the qualified name of a frame and the name of a nested frame, returns the qualified name of the nested frame.
     */
    static String getNestedFrameName( String parentFrameName, final String relativeName ) {
        if (parentFrameName.equalsIgnoreCase( WebRequest.TOP_FRAME )) return relativeName;
        return parentFrameName + ':' + relativeName;
    }


    /**
     * Returns the qualified name of a target frame.
     *
     */
    static String getTargetFrameName( String sourceFrameName, final String relativeName ) {
        if (relativeName.equalsIgnoreCase( WebRequest.PARENT_FRAME )) return getParentFrameName( sourceFrameName );
        if (sourceFrameName.indexOf( ':' ) < 0) return relativeName;
        return getParentFrameName( sourceFrameName ) + ':' + relativeName;
    }


    static String getParentFrameName( String parentFrameName ) {
        if (parentFrameName.indexOf( ':' ) < 0) return WebRequest.TOP_FRAME;
        return parentFrameName.substring( 0, parentFrameName.lastIndexOf( ':' ) );
    }


    WebRequest getInitialRequest() {
        return new GetMethodWebRequest( _baseURL,
                                        NodeUtils.getNodeAttribute( _element, "src" ),
                                        getName() );
    }


    boolean hasInitialRequest() {
        return NodeUtils.getNodeAttribute( _element, "src" ).length() > 0;
    }


//----------------------------------- private fields and methods -----------------------------------


    private Node   _element;

    private URL    _baseURL;

    private String _name;
}

