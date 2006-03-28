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
import org.w3c.dom.*;

import java.util.Hashtable;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class ElementImpl extends NodeImpl implements Element {

    private String _tagName;
    private Hashtable _attributes = new Hashtable();


    static ElementImpl createElement( DocumentImpl owner, String tagName ) {
        ElementImpl element = new ElementImpl();
        element.initialize( owner, tagName );
        return element;
    }


    protected void initialize( DocumentImpl owner, String tagName ) {
        super.initialize( owner );
        _tagName = tagName;
    }


    public String getNodeName() {
        return getTagName();
    }


    public short getNodeType() {
        return ELEMENT_NODE;
    }


    public String getNodeValue() throws DOMException {
        return null;
    }


    public void setNodeValue( String nodeValue ) throws DOMException {
    }


    public String getTagName() {
        return _tagName;
    }


    public boolean hasAttributes() {
        return !_attributes.isEmpty();
    }


    public NamedNodeMap getAttributes() {
        return new NamedNodeMapImpl( _attributes );
    }


    public String getAttribute( String name ) {
        Attr attr = getAttributeNode( name );
        return attr == null ? "" : attr.getValue();
    }


    public void setAttribute( String name, String value ) throws DOMException {
        Attr attribute = getOwnerDocument().createAttribute( name );
        attribute.setValue( value );
        setAttributeNode( attribute );
    }


    public void removeAttribute( String name ) throws DOMException {
        _attributes.remove( name );
    }


    public Attr getAttributeNode( String name ) {
        return (Attr) _attributes.get( name );
    }


    public Attr setAttributeNode( Attr newAttr ) throws DOMException {
        if (newAttr.getOwnerDocument() != getOwnerDocument()) throw new DOMException( DOMException.WRONG_DOCUMENT_ERR, "attribute must be from the same document as the element" );

        ((AttrImpl) newAttr).setOwnerElement( this );
        AttrImpl oldAttr = (AttrImpl) _attributes.put( newAttr.getName(), newAttr );
        if (oldAttr != null) oldAttr.setOwnerElement( null );
        return oldAttr;
    }


    public Attr removeAttributeNode( Attr oldAttr ) throws DOMException {
        if (!_attributes.containsValue( oldAttr)) throw new DOMException( DOMException.NOT_FOUND_ERR, "Specified attribute is not defined for this element" );

        AttrImpl removedAttr = (AttrImpl) _attributes.remove( oldAttr.getName() );
        if (removedAttr != null) removedAttr.setOwnerElement( null );
        return removedAttr;
    }


    public boolean hasAttribute( String name ) {
        return _attributes.containsKey( name );
    }


    // ----------------------- namespaces are not supported at present --------------------------------


    public String getAttributeNS( String namespaceURI, String localName ) {
        return null;
    }


    public void setAttributeNS( String namespaceURI, String qualifiedName, String value ) throws DOMException {
    }


    public void removeAttributeNS( String namespaceURI, String localName ) throws DOMException {
    }


    public Attr getAttributeNodeNS( String namespaceURI, String localName ) {
        return null;
    }


    public Attr setAttributeNodeNS( Attr newAttr ) throws DOMException {
        return null;
    }


    public NodeList getElementsByTagNameNS( String namespaceURI, String localName ) {
        return null;
    }


    public boolean hasAttributeNS( String namespaceURI, String localName ) {
        return false;
    }


    public static Element importNode( DocumentImpl document, Element original, boolean deep ) {
        Element copy = document.createElement( original.getTagName() );
        NamedNodeMap attributes = original.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            copy.setAttributeNode( (Attr) document.importNode( attributes.item(i), false ) );
        }
        if (deep) document.importChildren( original, copy );
        return copy;
    }


}
