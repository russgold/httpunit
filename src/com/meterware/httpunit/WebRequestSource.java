/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2001, Russell Gold
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
package com.meterware.httpunit;

import org.w3c.dom.Node;

import java.net.URL;


abstract
public class WebRequestSource {

    /**
     * Returns the ID associated with this request source.
     **/
    public String getID() {
        return NodeUtils.getNodeAttribute( _node, "id" );
    }


    /**
     * Returns the target for this request source.
     **/
    public String getTarget() {
        if (getSpecifiedTarget().length() == 0) {
            return _parentTarget;
        } else if (getSpecifiedTarget().equalsIgnoreCase( "_self" )) {
            return _parentTarget;
        } else {
            return getSpecifiedTarget();
        }
    }

    /**
     * Returns a copy of the domain object model subtree associated with this entity.
     **/
    public Node getDOMSubtree() {
        return _node.cloneNode( /* deep */ true );
    }


    /**
     * Creates and returns a web request from this request source.
     **/
    abstract
    public WebRequest getRequest();


 //----------------------------- protected members ---------------------------------------------

    /**
     * Contructs a web form given the URL of its source page and the DOM extracted
     * from that page.
     **/
    WebRequestSource( Node node, URL baseURL, String parentTarget ) {
        if (node == null) throw new IllegalArgumentException( "node must not be null" );
        _node         = node;
        _baseURL      = baseURL;
        _parentTarget = parentTarget;
    }


    protected URL getBaseURL() {
        return _baseURL;
    }


    /**
     * Returns the actual DOM for this request source, not a copy.
     **/
    protected Node getNode() {
        return _node;
    }

 //----------------------------- private members -----------------------------------------------


    /** The target in which the parent response is to be rendered. **/
    private String         _parentTarget;

    /** The URL of the page containing this entity. **/
    private URL            _baseURL;

    /** The DOM node representing this entity. **/
    private Node           _node;

    private String getSpecifiedTarget() {
        return NodeUtils.getNodeAttribute( _node, "target" );
    }


}
