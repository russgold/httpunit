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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Stack;


/**
 * Some common utilities for manipulating DOM nodes.
 **/
class NodeUtils {


    public static NodeList getElementsByTagName( Node root, String tagName ) {
        if (root instanceof Document) {
            return ((Document) root).getElementsByTagName( tagName );
        } else if (root instanceof Element) {
            return ((Element) root).getElementsByTagName( tagName );
        } else {
            throw new RuntimeException( "root is neither an Element nor a Document" );
        }
    }


    public static int getAttributeValue( Node node, String attributeName, int defaultValue ) {
        NamedNodeMap nnm = node.getAttributes();
        Node attribute = nnm.getNamedItem( attributeName );
        if (attribute == null) {
            return defaultValue;
        } else try {
            return Integer.parseInt( attribute.getNodeValue() );
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }


    public static String getNodeAttribute( Node node, String attributeName ) {
        return getNodeAttribute( node, attributeName, "" );
    }


    public static String getNodeAttribute( Node node, String attributeName, String defaultValue ) {
        Node attribute = node.getAttributes().getNamedItem( attributeName );
        return (attribute == null) ? defaultValue : attribute.getNodeValue();
    }


    static boolean isNodeAttributePresent( Node node, final String attributeName ) {
        return node.getAttributes().getNamedItem( attributeName ) != null;
    }


    interface NodeAction {
        /**
         * Does appropriate processing on specified element. Will return false if the subtree below the element
         * should be skipped.
         */
        public boolean processElement( Element element );

        /**
         * Processes a text node.
         */
        public void processTextNodeValue( String value );
    }

    /**
     * Converts the DOM trees rooted at the specified nodes to text, ignoring
     * any HTML tags.
     **/
    public static String asText( NodeList rootNodes ) {
        final StringBuffer sb = new StringBuffer(HttpUnitUtils.DEFAULT_TEXT_BUFFER_SIZE);
        processNodes( rootNodes, new NodeAction() {
            public boolean processElement( Element node ) {
                if (node.getNodeName().equalsIgnoreCase( "p" )) {
                    sb.append( "\n" );
                } else if (node.getNodeName().equalsIgnoreCase( "tr" )) {
                    sb.append( "\n" );
                } else if (node.getNodeName().equalsIgnoreCase( "td" )) {
                    sb.append( " | " );
                } else if (node.getNodeName().equalsIgnoreCase( "th" )) {
                    sb.append( " | " );
                } else if (node.getNodeName().equalsIgnoreCase( "img" ) && HttpUnitOptions.getImagesTreatedAsAltText()) {
                    sb.append( getNodeAttribute( node, "alt" ) );
                }
                return true;
            }
            public void processTextNodeValue( String value ) {
                sb.append( convertNBSP( value ) );
            }
        } );
        return sb.toString();
    }


    /**
     * Converts the DOM trees rooted at the specified nodes to text, ignoring
     * any HTML tags.
     **/
    public static void processNodes( NodeList rootNodes, NodeAction action ) {
        Stack pendingNodes = new Stack();
        pushNodeList( rootNodes, pendingNodes );

        while (!pendingNodes.empty()) {
            Node node = (Node) pendingNodes.pop();

            if (node.getNodeType() == Node.TEXT_NODE) {
                action.processTextNodeValue( node.getNodeValue() );
            } else if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            } else
                action.processElement( (Element) node );

            pushNodeList( node.getChildNodes(), pendingNodes );
        }
    }


    final static private char NBSP = (char) 160;   // non-breaking space, defined by JTidy


    private static String convertNBSP( String text ) {
        if (text == null) return "";
        return text.replace( NBSP, ' ' );
    }


    private static void pushNodeList( NodeList nl, Stack stack ) {
        if (nl != null) {
            for (int i = nl.getLength()-1; i >= 0; i--) {
                stack.push( nl.item(i) );
            }
        }
    }


}