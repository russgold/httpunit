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
import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.*;

import java.util.Hashtable;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class HTMLDocumentImpl extends DocumentImpl implements HTMLDocument {

    private static Hashtable _exemplars = new Hashtable();

    public String getTitle() {
        return null;
    }


    public void setTitle( String title ) {
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
        return null;
    }


    public void setBody( HTMLElement body ) {
    }


    public HTMLCollection getImages() {
        return null;
    }


    public HTMLCollection getApplets() {
        return null;
    }


    public HTMLCollection getLinks() {
        return null;
    }


    public HTMLCollection getForms() {
        return null;
    }


    public HTMLCollection getAnchors() {
        return null;
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
        return getExemplar( tagName ).create( this, toNodeCase( tagName ) );
    }


    public NodeList getElementsByTagName( String name ) {
        return super.getElementsByTagName( toNodeCase( name ) );
    }


    public Node cloneNode( boolean deep ) {
        HTMLDocumentImpl copy = new HTMLDocumentImpl();
        if (deep) copy.importChildren( this, copy );
        return copy;
    }


    private HTMLElementImpl getExemplar( String tagName ) {
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
