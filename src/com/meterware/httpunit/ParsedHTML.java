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

import java.util.Vector;
import java.util.ArrayList;

import org.w3c.dom.*;

/**
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 * @author <a href="mailto:bx@bigfoot.com">Benoit Xhenseval</a>
 **/
class ParsedHTML {

    private WebForm[]    _forms;
    private WebImage[]   _images;
    private WebResponse  _response;


    ParsedHTML( WebResponse response, URL baseURL, String baseTarget, Node rootNode, String characterSet ) {
        _response     = response;
        _baseURL      = baseURL;
        _baseTarget   = baseTarget;
        _rootNode     = rootNode;
        _characterSet = characterSet;
    }


    /**
     * Returns the forms found in the page in the order in which they appear.
     **/
    public WebForm[] getForms() {
        if (_forms == null) {
            NodeList forms = NodeUtils.getElementsByTagName( _rootNode, "form" );

            _forms = new WebForm[ forms.getLength() ];
            for (int i = 0; i < _forms.length; i++) {
                _forms[i] = new WebForm( _response, _baseURL, _baseTarget, forms.item( i ), _characterSet );
            }
        }
        return _forms;
    }


    /**
     * Returns the form found in the page with the specified ID.
     * @exception SAXException thrown if there is an error parsing the response.
     **/
    public WebForm getFormWithID( String ID ) {
        WebForm[] forms = getForms();
        for (int i = 0; i < forms.length; i++) {
            if (forms[i].getID().equals( ID )) return forms[i];
            else if (HttpUnitOptions.getMatchesIgnoreCase() && forms[i].getID().equalsIgnoreCase( ID )) return forms[i];
        }
        return null;
    }


    /**
     * Returns the form found in the page with the specified name.
     * @exception SAXException thrown if there is an error parsing the response.
     **/
    public WebForm getFormWithName( String name ) {
        WebForm[] forms = getForms();
        for (int i = 0; i < forms.length; i++) {
            if (forms[i].getName().equals( name )) return forms[i];
            else if (HttpUnitOptions.getMatchesIgnoreCase() && forms[i].getName().equalsIgnoreCase( name )) return forms[i];
        }
        return null;
    }


    /**
     * Returns the links found in the page in the order in which they appear.
     **/
    public WebLink[] getLinks() {
        if (_links == null) {
            final ArrayList list = new ArrayList();
            NodeUtils.processNodes( _rootNode.getChildNodes(), new NodeUtils.NodeAction() {
                public boolean processElement( Element element ) {
                    if (element.getNodeName().equalsIgnoreCase( "a" )) addLinkAnchor( list, element );
                    else if (element.getNodeName().equalsIgnoreCase( "area" )) addLinkAnchor( list, element );
                    return true;
                }
                public void processTextNodeValue( String value ) {
                }
            } );
            _links = (WebLink[]) list.toArray( new WebLink[ list.size() ] );
        }

        return _links;
    }


    private void addLinkAnchor( ArrayList list, Node child ) {
        if (isLinkAnchor( child )) {
            list.add( new WebLink( _response, _baseURL, _baseTarget, child ) );
        }
    }


    /**
     * Returns the first link which contains the specified text.
     **/
    public WebLink getLinkWith( String text ) {
        WebLink[] links = getLinks();
        for (int i = 0; i < links.length; i++) {
            if (contains( links[i].asText(), text )) return links[i];
        }
        return null;
    }


    /**
     * Returns the first link which contains an image with the specified text as its 'alt' attribute.
     **/
    public WebLink getLinkWithImageText( String text ) {
        WebImage image = getImageWithAltText( text );
        return image == null ? null : image.getLink();
    }


    /**
     * Returns the link found in the page with the specified ID.
     * @exception SAXException thrown if there is an error parsing the response.
     **/
    public WebLink getLinkWithID( String ID ) {
        WebLink[] links = getLinks();
        for (int i = 0; i < links.length; i++) {
            if (links[i].getID().equals( ID )) return links[i];
            else if (HttpUnitOptions.getMatchesIgnoreCase() && links[i].getID().equalsIgnoreCase( ID )) return links[i];
        }
        return null;
    }


    /**
     * Returns the link found in the page with the specified name.
     * @exception SAXException thrown if there is an error parsing the response.
     **/
    public WebLink getLinkWithName( String name ) {
        WebLink[] links = getLinks();
        for (int i = 0; i < links.length; i++) {
            if (links[i].getName().equals( name )) return links[i];
            else if (HttpUnitOptions.getMatchesIgnoreCase() && links[i].getName().equalsIgnoreCase( name )) return links[i];
        }
        return null;
    }


    /**
     * Returns the images found in the page in the order in which they appear.
     **/
    public WebImage[] getImages() {
        if (_images == null) {
            NodeList images = NodeUtils.getElementsByTagName( _rootNode, "img" );

            _images = new WebImage[ images.getLength() ];
            for (int i = 0; i < _images.length; i++) {
                _images[i] = new WebImage( _response, this, _baseURL, images.item( i ), _baseTarget );
            }
        }
        return _images;
    }


    /**
     * Returns the image found in the page with the specified name.
     **/
    public WebImage getImageWithName( String name ) {
        WebImage[] images = getImages();
        for (int i = 0; i < images.length; i++) {
            if (images[i].getName().equals( name )) return images[i];
            else if (HttpUnitOptions.getMatchesIgnoreCase() && images[i].getName().equalsIgnoreCase( name )) return images[i];
        }
        return null;
    }


    /**
     * Returns the first image found in the page with the specified src attribute.
     **/
    public WebImage getImageWithSource( String source ) {
        WebImage[] images = getImages();
        for (int i = 0; i < images.length; i++) {
            if (images[i].getSource().equals( source )) return images[i];
            else if (HttpUnitOptions.getMatchesIgnoreCase() && images[i].getSource().equalsIgnoreCase( source )) return images[i];
        }
        return null;
    }


    /**
     * Returns the first image found in the page with the specified alt attribute.
     **/
    public WebImage getImageWithAltText( String altText ) {
        WebImage[] images = getImages();
        for (int i = 0; i < images.length; i++) {
            if (images[i].getSource().equals( altText )) return images[i];
            else if (HttpUnitOptions.getMatchesIgnoreCase() && images[i].getAltText().equalsIgnoreCase( altText )) return images[i];
        }
        return null;
    }


    /**
     * Returns the top-level tables found in this page in the order in which
     * they appear.
     **/
    public WebTable[] getTables() {
        return WebTable.getTables( _response, getOriginalDOM(), _baseURL, _baseTarget, _characterSet );
    }


    /**
     * Returns the first table in the response which has the specified text as the full text of
     * its first non-blank row and non-blank column. Will recurse into any nested tables, as needed.
     * @return the selected table, or null if none is found
     **/
    public WebTable getTableStartingWith( final String text ) {
        return getTableSatisfyingPredicate( getTables(), new TablePredicate() {
            public boolean isTrue( WebTable table ) {
                table.purgeEmptyCells();
                return table.getRowCount() > 0 &&
                       matches( table.getCellAsText(0,0), text );
            }
        } );
    }


    /**
     * Returns the first table in the response which has the specified text as a prefix of the text
     * in its first non-blank row and non-blank column. Will recurse into any nested tables, as needed.
     * @return the selected table, or null if none is found
     **/
    public WebTable getTableStartingWithPrefix( final String text ) {
        return getTableSatisfyingPredicate( getTables(), new TablePredicate() {
            public boolean isTrue( WebTable table ) {
                table.purgeEmptyCells();
                return table.getRowCount() > 0 &&
                       hasPrefix( table.getCellAsText(0,0).toUpperCase(), text );
            }
        } );
    }


    /**
     * Returns the first table in the response which has the specified text as its summary attribute.
     * Will recurse into any nested tables, as needed.
     * @return the selected table, or null if none is found
     **/
    public WebTable getTableWithSummary( final String summary ) {
        return getTableSatisfyingPredicate( getTables(), new TablePredicate() {
            public boolean isTrue( WebTable table ) {
                return matches( table.getSummary(), summary );
            }
        } );
    }


    /**
     * Returns the first table in the response which has the specified text as its ID attribute.
     * Will recurse into any nested tables, as needed.
     * @return the selected table, or null if none is found
     **/
    public WebTable getTableWithID( final String ID ) {
        return getTableSatisfyingPredicate( getTables(), new TablePredicate() {
            public boolean isTrue( WebTable table ) {
                return matches( table.getID(), ID );
            }
        } );
    }


    /**
     * Returns a copy of the domain object model associated with this page.
     **/
    public Node getDOM() {
        return _rootNode.cloneNode( /* deep */ true );
    }

    /**
     * Returns the domain object model associated with this page, to be used internally.
     * @author <a href="mailto:bx@bigfoot.com">Benoit Xhenseval</a>
     **/
    Node getOriginalDOM() {
        return _rootNode;
    }


//---------------------------------- Object methods --------------------------------


    public String toString() {
        return _baseURL.toExternalForm() + System.getProperty( "line.separator" ) +
               _rootNode;
    }


//---------------------------------- protected members ------------------------------


    /**
     * Overrides the base URL for this HTML segment.
     **/
    protected void setBaseURL( URL baseURL ) {
        _baseURL = baseURL;
    }


    /**
     * Overrides the base target for this HTML segment.
     **/
    protected void setBaseTarget( String baseTarget ) {
        _baseTarget = baseTarget;
    }


//---------------------------------- package members --------------------------------


    /**
     * Returns the base URL for this HTML segment.
     **/
    URL getBaseURL() {
        return _baseURL;
    }


    interface TablePredicate {
        public boolean isTrue( WebTable table );
    }


    interface LinkPredicate {
        public boolean isTrue( WebLink link );
    }


    WebLink getLinkSatisfyingPredicate( LinkPredicate predicate ) {
        WebLink[] links = getLinks();
        for (int i = 0; i < links.length; i++) {
            if (predicate.isTrue( links[ i ] )) return links[ i ];
        }
        return null;
    }


//---------------------------------- private members --------------------------------

    private Node _rootNode;

    private WebLink[] _links;

    private URL _baseURL;

    private String _baseTarget;

    private String _characterSet;


    private boolean contains( String string, String substring ) {
        if (HttpUnitOptions.getMatchesIgnoreCase()) {
            return string.toUpperCase().indexOf( substring.toUpperCase() ) >= 0;
        } else {
            return string.indexOf( substring ) >= 0;
        }
    }


    private boolean hasPrefix( String string, String prefix ) {
        if (HttpUnitOptions.getMatchesIgnoreCase()) {
            return string.toUpperCase().startsWith( prefix.toUpperCase() );
        } else {
            return string.startsWith( prefix );
        }
    }


    private boolean matches( String string1, String string2 ) {
        if (HttpUnitOptions.getMatchesIgnoreCase()) {
            return string1.equalsIgnoreCase( string2 );
        } else {
            return string1.equals( string2 );
        }
    }


    private String getValue( Node node ) {
        return (node == null) ? "" : node.getNodeValue();
    }


    /**
     * Returns true if the node is a link anchor node.
     **/
    private boolean isLinkAnchor( Node node ) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return false;
        } else if (!node.getNodeName().equals( "a" ) && !node.getNodeName().equals( "area" )) {
            return false;
        } else {
            return (node.getAttributes().getNamedItem( "href" ) != null);
        }
    }



    /**
     * Returns the table with the specified text in its summary attribute.
     **/
    private WebTable getTableSatisfyingPredicate( WebTable[] tables, TablePredicate predicate ) {
        for (int i = 0; i < tables.length; i++) {
            if (predicate.isTrue( tables[i] )) {
                return tables[i];
            } else {
                for (int j = 0; j < tables[i].getRowCount(); j++) {
                    for (int k = 0; k < tables[i].getColumnCount(); k++) {
                        TableCell cell = tables[i].getTableCell(j,k);
                        if (cell != null) {
                            WebTable[] innerTables = cell.getTables();
                            if (innerTables.length != 0) {
                                WebTable result = getTableSatisfyingPredicate( innerTables, predicate );
                                if (result != null) return result;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

}