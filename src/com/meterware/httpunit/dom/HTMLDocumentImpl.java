package com.meterware.httpunit.dom;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2004, Russell Gold
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
import org.w3c.dom.html.*;
import org.w3c.dom.*;
import org.w3c.dom.Node;
import org.mozilla.javascript.*;

import java.util.Hashtable;
import java.util.ArrayList;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class HTMLDocumentImpl extends DocumentImpl implements HTMLDocument {

    private static Hashtable _exemplars = new Hashtable();

    public Object get( String propertyName, Scriptable scriptable ) {
        if (propertyName.equals( "document" )) return this;

        Object result = super.get( propertyName, scriptable );
        if (result != NOT_FOUND) return result;

        return ScriptingSupport.getNamedProperty( this, getJavaPropertyName( propertyName ), scriptable );
    }


    public void put( String propertyName, Scriptable initialObject, Object value ) {
        ScriptingSupport.setNamedProperty( this, getJavaPropertyName( propertyName ), value );
    }


    private String getJavaPropertyName( String propertyName ) {
        if (propertyName.equals( "document" )) {
            return "ownerDocument";
        } else {
            return propertyName;
        }
    }


    public String getTitle() {
        HTMLTitleElement result = getTitleElement();
        return result == null ? "" : result.getText();
    }


    private HTMLTitleElement getTitleElement() {
        HTMLTitleElement result = null;
        NodeList titleNodes = getElementsByTagName( "title" );
        for (int i = 0; i < titleNodes.getLength(); i++) {
            Node node = titleNodes.item( i );
            if (node instanceof HTMLTitleElement) {
                result = ((HTMLTitleElement) node);
            }
        }
        return result;
    }


    private HTMLHeadElement getHeadElement() {
        NodeList headNodes = getElementsByTagName( "head" );
        for (int i = 0; i < headNodes.getLength(); i++) {
            Node node = headNodes.item( i );
            if (node instanceof HTMLHeadElement) {
                return ((HTMLHeadElement) node);
            }
        }

        HTMLHeadElement head = (HTMLHeadElement) createElement( "head" );
        getHtmlElement().appendChild( head );
        return head;
    }


    private HTMLHtmlElement getHtmlElement() {
        NodeList htmlNodes = getElementsByTagName( "html" );
        for (int i = 0; i < htmlNodes.getLength(); i++) {
            Node node = htmlNodes.item( i );
            if (node instanceof HTMLHtmlElement) {
                return ((HTMLHtmlElement) node);
            }
        }

        HTMLHtmlElement html = (HTMLHtmlElement) createElement( "html" );
        appendChild( html );
        return html;
    }


    public void setTitle( String title ) {
        HTMLTitleElement titleElement = getTitleElement();
        if (titleElement != null) {
            titleElement.setText( title );
        } else {
            titleElement = (HTMLTitleElement) createElement( "title" );
            titleElement.setText( title );
            getHeadElement().appendChild( titleElement );
        }
    }


    public String getReferrer() {
        return null;
    }


    public String getDomain() {
        return null;
    }


    public String getURL() {
        return null;
    }


    public HTMLElement getBody() {
        NodeList bodyNodes = getElementsByTagName( "body" );
        for (int i = 0; i < bodyNodes.getLength(); i++) {
            Node node = bodyNodes.item( i );
            if (node instanceof HTMLBodyElement) {
                return ((HTMLBodyElement) node);
            }
        }
        return null;
    }


    public void setBody( HTMLElement body ) {
        getHtmlElement().appendChild( body );
    }


    public HTMLCollection getImages() {
        ArrayList elements = new ArrayList();
        appendElementsWithTags( new String[] {"img"}, elements );
        return HTMLCollectionImpl.createHTMLCollectionImpl( new NodeListImpl( elements ) );
    }


    public HTMLCollection getApplets() {
        return null;
    }


    public HTMLCollection getLinks() {
        ArrayList elements = new ArrayList();
        appendElementsWithTags( new String[] {"area"}, elements );

        NodeList nodeList = getElementsByTagName( "A" );
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item( i );
            if (node.getAttributes().getNamedItem( "href" ) != null) {
                elements.add( node );
            }
        }
        return HTMLCollectionImpl.createHTMLCollectionImpl( new NodeListImpl( elements ) );
    }


    public HTMLCollection getForms() {
        ArrayList elements = new ArrayList();
        appendElementsWithTags( new String[] {"form"}, elements );
        return HTMLCollectionImpl.createHTMLCollectionImpl( new NodeListImpl( elements ) );
    }


    public HTMLCollection getAnchors() {
        NodeList nodeList = getElementsByTagName( "A" );
        ArrayList elements = new ArrayList();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item( i );
            if (node.getAttributes().getNamedItem( "name" ) != null) {
                elements.add( node );
            }
        }
        return HTMLCollectionImpl.createHTMLCollectionImpl( new NodeListImpl( elements ) );
    }


    public String getCookie() {
        return null;
    }


    public void setCookie( String cookie ) {
    }


    public void open() {
    }


    public void close() {
    }


    public void write( String text ) {
    }


    public void writeln( String text ) {
    }


    public NodeList getElementsByName( String elementName ) {
        return null;
    }


    public Element createElement( String tagName ) throws DOMException {
        ElementImpl element = getExemplar( tagName ).create();
        element.initialize( this, toNodeCase( tagName ) );
        return element;
    }


    public Element createElementNS( String namespaceURI, String qualifiedName ) throws DOMException {
        ElementImpl element = getExemplar( qualifiedName ).create();
        element.initialize( this, namespaceURI, toNodeCase( qualifiedName ) );
        return element;
    }


/*
    Element createScriptableElement( String tagName ) throws DOMException {
        try {
            ElementImpl element = (ElementImpl) Context.getCurrentContext().newObject( this, getExemplar( tagName ).getClassName() );
            element.initialize( this, toNodeCase( tagName ) );
            return element;
        } catch (PropertyException e) {
            throw new RuntimeException( e );
        } catch (NotAFunctionException e) {
            throw new RuntimeException( e );
        } catch (JavaScriptException e) {
            throw new RuntimeException( e );
        }
    }

*/

    public NodeList getElementsByTagName( String name ) {
        return super.getElementsByTagName( toNodeCase( name ) );
    }


    public Node cloneNode( boolean deep ) {
        HTMLDocumentImpl copy = new HTMLDocumentImpl();
        if (deep) copy.importChildren( this, copy );
        return copy;
    }


    private static HTMLElementImpl getExemplar( String tagName ) {
        HTMLElementImpl impl = (HTMLElementImpl) _exemplars.get( tagName.toLowerCase() );
        if (impl == null) impl = new HTMLElementImpl();
        return impl;
    }


    String toNodeCase( String nodeName ) {
        return nodeName.toUpperCase();
    }


    static {
        _exemplars.put( "html",     new HTMLHtmlElementImpl() );
        _exemplars.put( "head",     new HTMLHeadElementImpl() );
        _exemplars.put( "link",     new HTMLLinkElementImpl() );
        _exemplars.put( "title",    new HTMLTitleElementImpl() );
        _exemplars.put( "meta",     new HTMLMetaElementImpl() );
        _exemplars.put( "base",     new HTMLBaseElementImpl() );
        _exemplars.put( "style",    new HTMLStyleElementImpl() );
        _exemplars.put( "body",     new HTMLBodyElementImpl() );
        _exemplars.put( "form",     new HTMLFormElementImpl() );
        _exemplars.put( "select",   new HTMLSelectElementImpl() );
        _exemplars.put( "option",   new HTMLOptionElementImpl() );
        _exemplars.put( "input",    new HTMLInputElementImpl() );
        _exemplars.put( "textarea", new HTMLTextAreaElementImpl() );
        _exemplars.put( "a",        new HTMLAnchorElementImpl() );
        _exemplars.put( "area",     new HTMLAreaElementImpl() );
        _exemplars.put( "img",      new HTMLImageElementImpl() );
    }
}
