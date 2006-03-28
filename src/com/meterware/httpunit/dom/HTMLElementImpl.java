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
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.NodeList;


/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class HTMLElementImpl extends ElementImpl implements HTMLElement {


    public final static String UNSPECIFIED_ATTRIBUTE = null;


    ElementImpl create( DocumentImpl owner, String tagName ) {
        HTMLElementImpl element = new HTMLElementImpl();
        element.initialize( owner, tagName );
        return element;
    }


    protected void initialize( DocumentImpl owner, String tagName ) {
        super.initialize( owner, tagName.toUpperCase() );
    }


    public String getId() {
        return getAttributeWithNoDefault( "id" );
    }


    public void setId( String id ) {
        setAttribute( "id", id );
    }


    public String getTitle() {
        return getAttributeWithNoDefault( "title" );
    }


    public void setTitle( String title ) {
        setAttribute( "title", title );
    }


    public String getLang() {
        return getAttributeWithNoDefault( "lang" );
    }


    public void setLang( String lang ) {
        setAttribute( "lang", lang );
    }


    public String getDir() {
        return getAttributeWithNoDefault( "dir" );
    }


    public void setDir( String dir ) {
        setAttribute( "dir", dir );
    }


    public String getClassName() {
        return getAttributeWithNoDefault( "class" );
    }


    public void setClassName( String className ) {
        setAttribute( "class", className );
    }


    public NodeList getElementsByTagName( String name ) {
        return super.getElementsByTagName( ((HTMLDocumentImpl) getOwnerDocument()).toNodeCase( name ) );
    }


    final protected String getAttributeWithDefault( String attributeName, String defaultValue ) {
        if (hasAttribute( attributeName )) return getAttribute( attributeName );
        return defaultValue;
    }


    final protected String getAttributeWithNoDefault( String attributeName ) {
        if (hasAttribute( attributeName )) return getAttribute( attributeName );
        return UNSPECIFIED_ATTRIBUTE;
    }


    protected boolean getBooleanAttribute( String name ) {
        String value = getAttribute( name );
        return (value.length() > 0 && value.equalsIgnoreCase( "true" ) );
    }


    protected int getIntegerAttribute( String name ) {
        String value = getAttribute( name );
        return value.length() == 0 ? 0 : Integer.parseInt( value );
    }


    protected void setAttribute( String name, boolean disabled ) {
        setAttribute( name, disabled ? "true" : "false" );
    }


    protected void setAttribute( String name, int value ) {
        setAttribute( name, Integer.toString( value ) );
    }


    HTMLDocumentImpl getHtmlDocument() {
        return (HTMLDocumentImpl) getOwnerDocument();
    }
}
