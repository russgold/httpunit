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
import java.net.URL;

import org.w3c.dom.Node;


/**
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
public class WebImage {

    private URL        _baseURL;
    private Node       _node;
    private ParsedHTML _parsedHTML;


    WebImage( ParsedHTML parsedHTML, URL baseURL, Node node ) {
        _baseURL = baseURL;
        _node = node;
        _parsedHTML = parsedHTML;
    }


    public String getSource() {
        return NodeUtils.getNodeAttribute( _node, "src" );
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
}
