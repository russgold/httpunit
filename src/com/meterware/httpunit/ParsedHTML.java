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
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 * @author <a href="mailto:bx@bigfoot.com">Benoit Xhenseval</a>
 **/
class ParsedHTML {

    private Node _rootNode;

    private URL _baseURL;

    private String _baseTarget;

    private String _characterSet;

    private WebResponse  _response;

    private boolean      _updateElements = true;

    /** map of element IDs to elements. **/
    private HashMap      _elementsByID = new HashMap();

    /** map of DOM elements to HTML elements **/
    private HashMap      _elements = new HashMap();

    private ArrayList    _formsList = new ArrayList();
    private WebForm[]    _forms;

    private ArrayList    _imagesList = new ArrayList();
    private WebImage[]   _images;

    private ArrayList    _linkList = new ArrayList();
    private WebLink[]    _links;

    private ArrayList    _appletList = new ArrayList();
    private WebApplet[]  _applets;

    private ArrayList    _tableList = new ArrayList();
    private WebTable[]   _tables;


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
            loadElements();
            _forms = (WebForm[]) _formsList.toArray( new WebForm[ _formsList.size() ] );
        }
        return _forms;
    }


    /**
     * Returns the links found in the page in the order in which they appear.
     **/
    public WebLink[] getLinks() {
        if (_links == null) {
            loadElements();
            _links = (WebLink[]) _linkList.toArray( new WebLink[ _linkList.size() ] );
        }
        return _links;
    }


    /**
     * Returns a proxy for each applet found embedded in this page.
     */
    public WebApplet[] getApplets() {
        if (_applets == null) {
            loadElements();
            _applets = (WebApplet[]) _appletList.toArray( new WebApplet[ _appletList.size() ] );
        }
        return _applets;
    }


    /**
     * Returns the images found in the page in the order in which they appear.
     **/
    public WebImage[] getImages() {
        if (_images == null) {
            loadElements();
            _images = (WebImage[]) _imagesList.toArray( new WebImage[ _imagesList.size() ] );
        }
        return _images;
    }


    /**
     * Returns the top-level tables found in the page in the order in which they appear.
     **/
    public WebTable[] getTables() {
        if (_tables == null) {
            loadElements();
            _tables = (WebTable[]) _tableList.toArray( new WebTable[ _tableList.size() ] );
        }
        return _tables;
    }


    /**
     * Returns the HTMLElement with the specified ID.
     */
    public HTMLElement getElementWithID( String id ) {
        return (HTMLElement) getElementWithID( id, HTMLElement.class );
    }


    /**
     * Returns the form found in the page with the specified ID.
     **/
    public WebForm getFormWithID( String id ) {
        return (WebForm) getElementWithID( id, WebForm.class );
    }


    /**
     * Returns the link found in the page with the specified ID.
     **/
    public WebLink getLinkWithID( String id ) {
        return (WebLink) getElementWithID( id, WebLink.class );

    }


    private Object getElementWithID( String id, final Class klass ) {
        loadElements();
        return whenCast( _elementsByID.get( id ), klass );
    }


    private Object whenCast( Object o, Class klass ) {
        return klass.isInstance( o ) ? o : null;
    }


    /**
     * Returns the form found in the page with the specified name.
      **/
    public WebForm getFormWithName( String name ) {
        WebForm[] forms = getForms();
        for (int i = 0; i < forms.length; i++) {
            if (HttpUnitUtils.matches( name, forms[i].getName() )) return forms[i];
        }
        return null;
    }


    abstract static class HTMLElementFactory {
        abstract HTMLElement toHTMLElement( ParsedHTML parsedHTML, Element element );
        void recordElement( NodeUtils.PreOrderTraversal pot, Element element, HTMLElement htmlElement ) {
            if (htmlElement != null) {
                addToMaps( pot, element, htmlElement );
                addToLists( pot, htmlElement );
            }
        }
        protected void addToLists( NodeUtils.PreOrderTraversal pot, HTMLElement htmlElement ) {
            for (Iterator i = pot.getContexts(); i.hasNext();) {
                Object o = i.next();
                if (o instanceof ParsedHTML) ((ParsedHTML) o).addToList( htmlElement );
            }
        }
        protected void addToMaps( NodeUtils.PreOrderTraversal pot, Element element, HTMLElement htmlElement ) {
            for (Iterator i = pot.getContexts(); i.hasNext();) {
                Object o = i.next();
                if (o instanceof ParsedHTML) ((ParsedHTML) o).addToMaps( element, htmlElement );
            }
        }
    }


    static class WebFormFactory extends HTMLElementFactory {
        HTMLElement toHTMLElement( ParsedHTML parsedHTML, Element element ) {
            return parsedHTML.toWebForm( element );
        }
    }


    static class WebLinkFactory extends HTMLElementFactory {
        HTMLElement toHTMLElement( ParsedHTML parsedHTML, Element element ) {
            return parsedHTML.toLinkAnchor( element );
        }
    }


    static class WebImageFactory extends HTMLElementFactory {
        HTMLElement toHTMLElement( ParsedHTML parsedHTML, Element element ) {
            return parsedHTML.toWebImage( element );
        }
    }


    static class WebAppletFactory extends HTMLElementFactory {
        HTMLElement toHTMLElement( ParsedHTML parsedHTML, Element element ) {
            return parsedHTML.toWebApplet( element );
        }
    }


    static class WebTableFactory extends HTMLElementFactory {
        HTMLElement toHTMLElement( ParsedHTML parsedHTML, Element element ) {
            return parsedHTML.toWebTable( element );
        }


//        protected void addToLists( NodeUtils.PreOrderTraversal pot, HTMLElement htmlElement ) {
//            for (Iterator i = pot.getContexts(); i.hasNext();) {
//                Object o = i.next();
//                if (o instanceof ParsedHTML) {
//                    ((ParsedHTML) o).addToList( htmlElement );
//                    return;
//                }
//            }
//        }
    }


    private static HashMap _htmlFactoryClasses = new HashMap();

    static {
        _htmlFactoryClasses.put( "a", new WebLinkFactory() );
        _htmlFactoryClasses.put( "area", new WebLinkFactory() );
        _htmlFactoryClasses.put( "form", new WebFormFactory() );
        _htmlFactoryClasses.put( "img", new WebImageFactory() );
        _htmlFactoryClasses.put( "applet", new WebAppletFactory() );
        _htmlFactoryClasses.put( "table", new WebTableFactory() );
    }

    private static HTMLElementFactory getHTMLElementFactory( String tagName ) {
        return (HTMLElementFactory) _htmlFactoryClasses.get( tagName );
    }


    private void loadElements() {
        if (!_updateElements) return;

        NodeUtils.NodeAction action = new NodeUtils.NodeAction() {
            public boolean processElement( NodeUtils.PreOrderTraversal pot, Element element ) {
                if (_elements.containsKey( element )) return true;

                HTMLElementFactory factory = getHTMLElementFactory( element.getNodeName().toLowerCase() );
                if (factory != null) factory.recordElement( pot, element, factory.toHTMLElement( ParsedHTML.this, element ) );

                return true;
            }
            public void processTextNodeValue( String value ) {
            }
        };
        NodeUtils.PreOrderTraversal nt = new NodeUtils.PreOrderTraversal( getRootNode() );
        nt.pushBaseContext( this );
        nt.perform( action );

        _updateElements = false;
    }


    private WebForm toWebForm( Element element ) {
        return new WebForm( _response, _baseURL, _baseTarget, element, _characterSet );
    }


    private WebLink toLinkAnchor( Element child ) {
        return (!isLinkAnchor( child )) ? null : new WebLink( _response, _baseURL, _baseTarget, child );
    }


    private WebImage toWebImage( Element child ) {
        return new WebImage( _response, this, _baseURL, child, _baseTarget );
    }


    private WebApplet toWebApplet( Element element ) {
        return new WebApplet( _response, element, _baseTarget );
    }


    private WebTable toWebTable( Element element ) {
        if (!WebTable.isTopLevelTable( element, getRootNode() )) return null;
        return new WebTable( _response, element, _baseURL, _baseTarget, _characterSet );
    }


    private void addToMaps( Element element, HTMLElement htmlElement ) {
        _elements.put( element, htmlElement );
        if (htmlElement.getID() != null) _elementsByID.put( htmlElement.getID(), htmlElement );
    }


    private void addToList( HTMLElement htmlElement ) {
        ArrayList list = getListForElement( htmlElement );
        if (list != null) list.add( htmlElement );
    }


    private ArrayList getListForElement( HTMLElement element ) {
        if (element instanceof WebLink) return _linkList;
        if (element instanceof WebForm) return _formsList;
        if (element instanceof WebImage) return _imagesList;
        if (element instanceof WebApplet) return _appletList;
        if (element instanceof WebTable) return _tableList;
        return null;
    }


    /**
     * Returns the first link which contains the specified text.
     **/
    public WebLink getLinkWith( String text ) {
        return getFirstMatchingLink( WebLink.MATCH_CONTAINED_TEXT, text );
    }


    /**
     * Returns the link which contains the first image with the specified text as its 'alt' attribute.
     **/
    public WebLink getLinkWithImageText( String text ) {
        WebImage image = getImageWithAltText( text );
        return image == null ? null : image.getLink();
    }


    /**
     * Returns the link found in the page with the specified name.
     **/
    public WebLink getLinkWithName( String name ) {
        return getFirstMatchingLink( WebLink.MATCH_NAME, name );
    }


    /**
     * Returns the first link found in the page matching the specified criteria.
     **/
    public WebLink getFirstMatchingLink( HTMLElementPredicate predicate, Object criteria ) {
        WebLink[] links = getLinks();
        for (int i = 0; i < links.length; i++) {
            if (predicate.matchesCriteria( links[i], criteria )) return links[i];
        }
        return null;
    }


    /**
     * Returns all links found in the page matching the specified criteria.
     **/
    public WebLink[] getMatchingLinks( HTMLElementPredicate predicate, Object criteria ) {
        ArrayList matches = new ArrayList();
        WebLink[] links = getLinks();
        for (int i = 0; i < links.length; i++) {
            if (predicate.matchesCriteria( links[i], criteria )) matches.add( links[i] );
        }
        return (WebLink[]) matches.toArray( new WebLink[ matches.size() ] );
    }


    /**
     * Returns the image found in the page with the specified name.
     **/
    public WebImage getImageWithName( String name ) {
        WebImage[] images = getImages();
        for (int i = 0; i < images.length; i++) {
            if (HttpUnitUtils.matches( name, images[i].getName() )) return images[i];
        }
        return null;
    }


    /**
     * Returns the first image found in the page with the specified src attribute.
     **/
    public WebImage getImageWithSource( String source ) {
        WebImage[] images = getImages();
        for (int i = 0; i < images.length; i++) {
            if (HttpUnitUtils.matches( source, images[i].getSource() )) return images[i];
        }
        return null;
    }


    /**
     * Returns the first image found in the page with the specified alt attribute.
     **/
    public WebImage getImageWithAltText( String altText ) {
        WebImage[] images = getImages();
        for (int i = 0; i < images.length; i++) {
            if (HttpUnitUtils.matches( altText, images[i].getAltText() )) return images[i];
        }
        return null;
    }


    /**
     * Returns the first table in the response which matches the specified predicate and value.
     * Will recurse into any nested tables, as needed.
     * @return the selected table, or null if none is found
     **/
    public WebTable getFirstMatchingTable( HTMLElementPredicate predicate, Object criteria ) {
        return getTableSatisfyingPredicate( getTables(), predicate, criteria );
    }


    /**
     * Returns the first table in the response which has the specified text as the full text of
     * its first non-blank row and non-blank column. Will recurse into any nested tables, as needed.
     * @return the selected table, or null if none is found
     **/
    public WebTable getTableStartingWith( String text ) {
        return getFirstMatchingTable( WebTable.MATCH_FIRST_NONBLANK_CELL, text );
    }


    /**
     * Returns the first table in the response which has the specified text as a prefix of the text
     * in its first non-blank row and non-blank column. Will recurse into any nested tables, as needed.
     * @return the selected table, or null if none is found
     **/
    public WebTable getTableStartingWithPrefix( String text ) {
        return getFirstMatchingTable( WebTable.MATCH_FIRST_NONBLANK_CELL_PREFIX, text );
    }


    /**
     * Returns the first table in the response which has the specified text as its summary attribute.
     * Will recurse into any nested tables, as needed.
     * @return the selected table, or null if none is found
     **/
    public WebTable getTableWithSummary( String summary ) {
        return getFirstMatchingTable( WebTable.MATCH_SUMMARY, summary );
    }


    /**
     * Returns the first table in the response which has the specified text as its ID attribute.
     * Will recurse into any nested tables, as needed.
     * @return the selected table, or null if none is found
     **/
    public WebTable getTableWithID( String ID ) {
        return getFirstMatchingTable( WebTable.MATCH_ID, ID );
    }


    /**
     * Returns a copy of the domain object model associated with this page.
     **/
    public Node getDOM() {
        return getRootNode().cloneNode( /* deep */ true );
    }

//---------------------------------- Object methods --------------------------------


    public String toString() {
        return _baseURL.toExternalForm() + System.getProperty( "line.separator" ) +
               _rootNode;
    }


//---------------------------------- package members --------------------------------


    /**
     * Specifies the root node for this HTML fragment.
     */
    void setRootNode( Node rootNode ) {
        if (_rootNode != null && rootNode != _rootNode )
            throw new IllegalStateException( "The root node has already been defined as " + _rootNode + " and cannot be redefined as " + rootNode );
        _rootNode = rootNode;
        _links = null;
        _forms = null;
        _images = null;
        _applets = null;
        _tables = null;
        _updateElements = true;
    }


    /**
     * Returns the base URL for this HTML segment.
     **/
    URL getBaseURL() {
        return _baseURL;
    }


    WebResponse getResponse() {
        return _response;
    }


    /**
     * Returns the domain object model associated with this page, to be used internally.
     **/
    Node getOriginalDOM() {
        return getRootNode();
    }


//---------------------------------- private members --------------------------------


    private Node getRootNode() {
        if (_rootNode == null) throw new IllegalStateException( "The root node has not been specified" );
        return _rootNode;
    }



    /**
     * Returns true if the node is a link anchor node.
     **/
    private boolean isLinkAnchor( Node node ) {     // XXX do we really need all these tests any more?
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return false;
        } else if (!node.getNodeName().equalsIgnoreCase( "a" ) && !node.getNodeName().equalsIgnoreCase( "area" )) {
            return false;
        } else {
            return (node.getAttributes().getNamedItem( "href" ) != null);
        }
    }



    /**
     * Returns the table with the specified text in its summary attribute.
     **/
    private WebTable getTableSatisfyingPredicate( WebTable[] tables, HTMLElementPredicate predicate, Object value ) {
        for (int i = 0; i < tables.length; i++) {
            if (predicate.matchesCriteria( tables[i], value )) {
                return tables[i];
            } else {
                for (int j = 0; j < tables[i].getRowCount(); j++) {
                    for (int k = 0; k < tables[i].getColumnCount(); k++) {
                        TableCell cell = tables[i].getTableCell(j,k);
                        if (cell != null) {
                            WebTable[] innerTables = cell.getTables();
                            if (innerTables.length != 0) {
                                WebTable result = getTableSatisfyingPredicate( innerTables, predicate, value );
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