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
import java.util.Vector;

import java.net.URL;

import java.util.Vector;

import org.w3c.dom.*;

class ParsedHTML {


    ParsedHTML( URL pageURL, Node rootNode ) {
        _url = pageURL;
        _rootNode = rootNode;
    }


    /**
     * Returns the forms found in the page in the order in which they appear.
     **/
    public WebForm[] getForms() {
        NodeList forms = NodeUtils.getElementsByTagName( _rootNode, "form" );
        WebForm[] result = new WebForm[ forms.getLength() ];
        for (int i = 0; i < result.length; i++) {
            result[i] = new WebForm( getURL(), forms.item( i ) );
        }

        return result;
    }


    /**
     * Returns the links found in the page in the order in which they appear.
     **/
    public WebLink[] getLinks() {
        if (_links == null) {
            NodeList nl = NodeUtils.getElementsByTagName( _rootNode, "a" );
            Vector list = new Vector();
            for (int i = 0; i < nl.getLength(); i++) {
                Node child = nl.item(i);
                if (isLinkAnchor( child )) {
                    list.addElement( new WebLink( _url, child ) );
                }
            }
            _links = new WebLink[ list.size() ];
            list.copyInto( _links );
        }

        return _links;
    }


    /**
     * Returns the first link which contains the specified text.
     **/
    public WebLink getLinkWith( String text ) {
        WebLink[] links = getLinks();
        for (int i = 0; i < links.length; i++) {
            String linkText = NodeUtils.asText( links[i].getDOMSubtree().getChildNodes() ).toUpperCase();
            if (linkText.indexOf( text.toUpperCase() ) >= 0) {
                return links[i];
            }
        }
        return null;
    }


    /**
     * Returns the first link which contains an image with the specified text as its 'alt' attribute.
     **/
    public WebLink getLinkWithImageText( String text ) {
        WebLink[] links = getLinks();
        for (int i = 0; i < links.length; i++) {
            NodeList nl = ((Element) links[i].getDOMSubtree()).getElementsByTagName( "img" );
            for (int j = 0; j < nl.getLength(); j++) {
                NamedNodeMap nnm = nl.item(j).getAttributes();
                if (text.equalsIgnoreCase( getValue( nnm.getNamedItem( "alt" ) ) )) {
                    return links[i];
                }
            }
        }
        return null;
    }


    /**
     * Returns the top-level tables found in this page in the order in which
     * they appear.
     **/
    public WebTable[] getTables() {
        return WebTable.getTables( getDOM(), _url );
    }


    /**
     * Returns the first table in the response which has the specified text as the full text of
     * its first non-blank row and non-blank column. Will recurse into any nested tables, as needed.
     * @return the selected table, or null if none is found
     **/
    public WebTable getTableStartingWith( String text ) {
        return getTableStartingWith( text, getTables() );
    }
    
    
    /**
     * Returns the first table in the response which has the specified text as a prefix of the text 
     * in its first non-blank row and non-blank column. Will recurse into any nested tables, as needed.
     * @return the selected table, or null if none is found
     **/
    public WebTable getTableStartingWithPrefix( String text ) {
        return getTableStartingWithPrefix( text.toUpperCase(), getTables() );
    }
    
    
    /**
     * Returns a copy of the domain object model associated with this page.
     **/
    public Node getDOM() {
        return _rootNode.cloneNode( /* deep */ true );
    }


    /**
     * Returns the associated URL.
     **/
    public URL getURL() {
        return _url;
    }



//---------------------------------- Object methods --------------------------------


    public String toString() {
        return _url.toExternalForm() + System.getProperty( "line.separator" ) +
               _rootNode;
    }


//---------------------------------- private members --------------------------------

    Node _rootNode;

    WebLink[] _links;

    URL _url;


    private String getValue( Node node ) {
        return (node == null) ? "" : node.getNodeValue();
    }


    /**
     * Returns true if the node is a link anchor node.
     **/
    private boolean isLinkAnchor( Node node ) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return false;
        } else if (!node.getNodeName().equals( "a" )) {
            return false;
        } else {
            return (node.getAttributes().getNamedItem( "href" ) != null);
        }
    }



    /**
     * Returns the table with the specified text in its first non-blank row and column.
     **/
    private WebTable getTableStartingWith( String text, WebTable[] tables ) {
        for (int i = 0; i < tables.length; i++) {
            tables[i].purgeEmptyCells();
            if (tables[i].getRowCount() == 0) continue;
            if (tables[i].getCellAsText(0,0).equalsIgnoreCase( text )) {
                return tables[i];
            } else {
                for (int j = 0; j < tables[i].getRowCount(); j++) {
                    for (int k = 0; k < tables[i].getColumnCount(); k++) {
                        WebTable[] innerTables = tables[i].getTableCell(j,k).getTables();
                        if (innerTables.length != 0) {
                            WebTable result = getTableStartingWith( text, innerTables );
                            if (result != null) return result;
                        }
                    }
                }
            }
        }
        return null;
    }



    /**
     * Returns the table with the specified text in its first non-blank row and column.
     **/
    private WebTable getTableStartingWithPrefix( String text, WebTable[] tables ) {
        for (int i = 0; i < tables.length; i++) {
            tables[i].purgeEmptyCells();
            if (tables[i].getRowCount() == 0) continue;
            if (tables[i].getCellAsText(0,0).toUpperCase().startsWith( text )) {
                return tables[i];
            } else {
                for (int j = 0; j < tables[i].getRowCount(); j++) {
                    for (int k = 0; k < tables[i].getColumnCount(); k++) {
                        WebTable[] innerTables = tables[i].getTableCell(j,k).getTables();
                        if (innerTables.length != 0) {
                            WebTable result = getTableStartingWithPrefix( text, innerTables );
                            if (result != null) return result;
                        }
                    }
                }
            }
        }
        return null;
    }

}
