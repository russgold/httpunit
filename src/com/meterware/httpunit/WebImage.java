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
import com.meterware.httpunit.scripting.ScriptableDelegate;

import java.net.URL;

import org.w3c.dom.Node;


/**
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
public class WebImage extends FixedURLWebRequestSource {

    private URL        _baseURL;
    private Node       _node;
    private ParsedHTML _parsedHTML;
    private Scriptable _scriptable;
    private String     _src;
    private String     _alt;


    WebImage( WebResponse response, ParsedHTML parsedHTML, URL baseURL, Node node, String parentTarget ) {
        super( response, node, baseURL, NodeUtils.getNodeAttribute( node, "src" ), parentTarget );
        _baseURL = baseURL;
        _node = node;
        _parsedHTML = parsedHTML;
        _src = NodeUtils.getNodeAttribute( _node, "src" );
        _alt = NodeUtils.getNodeAttribute( _node, "alt" );
    }


    public String getName() {
        return NodeUtils.getNodeAttribute( _node, "name" );
    }


    public String getSource() {
        return _src;
    }


    public String getAltText() {
        return _alt;
    }


    public WebLink getLink() {
        return _parsedHTML.getLinkSatisfyingPredicate( new ParsedHTML.LinkPredicate() {

            public boolean isTrue( WebLink link ) {
                for (Node parent = _node.getParentNode(); parent != null; parent = parent.getParentNode()) {
                    if (parent.equals( link.getNode() )) return true;
                }
                return false;
            }
        } );
    }


    /**
     * Returns an object which provides scripting access to this link.
     **/
    public Scriptable getScriptableObject() {
        if (_scriptable == null) _scriptable = new Scriptable();
        return _scriptable;
    }


    public class Scriptable extends ScriptableDelegate {

        public Object get( String propertyName ) {
            if (propertyName.equalsIgnoreCase( "src" )) {
                return getSource();
            } else {
               return super.get( propertyName );
            }
        }


        public void set( String propertyName, Object value ) {
            if (propertyName.equalsIgnoreCase( "src" )) {
                if (value != null) _src = value.toString();
            } else {
                super.set( propertyName, value );
            }
        }
    }


//---------------------------------- WebRequestSource methods ------------------------------------------


    /**
     * Returns the scriptable delegate.
     */

    ScriptableDelegate getScriptableDelegate() {
        return getScriptableObject();
    }
}
