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

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class DocumentImpl extends NodeImpl implements Document {

    private Element _documentElement;


    static DocumentImpl createDocument() {
        DocumentImpl document = new DocumentImpl();
        document.initialize();
        return document;
    }


    protected void initialize() {};


    public String getNodeName() {
        return "#document";
    }


    public String getNodeValue() throws DOMException {
        return null;
    }


    public void setNodeValue( String nodeValue ) throws DOMException {
    }


    public short getNodeType() {
        return DOCUMENT_NODE;
    }


    public Document getOwnerDocument() {
        return this;
    }


    public DocumentType getDoctype() {
        return null;
    }


    public DOMImplementation getImplementation() {
        return null;
    }


    public Element getDocumentElement() {
        return _documentElement;
    }


    void setDocumentElement( Element documentElement ) {
        if (_documentElement != null) throw new IllegalStateException( "A document may have only one root" );
        _documentElement = documentElement;
        appendChild( documentElement );
    }


    public Element createElement( String tagName ) throws DOMException {
        return ElementImpl.createElement( this, tagName );
    }


    public DocumentFragment createDocumentFragment() {
        throw new UnsupportedOperationException( "DocumentFragment creation not supported ");
    }


    public Text createTextNode( String data ) {
        return TextImpl.createText( this, data );
    }


    public Comment createComment( String data ) {
        throw new UnsupportedOperationException( "Comment creation not supported ");
    }


    public CDATASection createCDATASection( String data ) throws DOMException {
        throw new UnsupportedOperationException( "CDATASection creation not supported ");
    }


    public ProcessingInstruction createProcessingInstruction( String target, String data ) throws DOMException {
        throw new UnsupportedOperationException( "ProcessingInstruction creation not supported ");
    }


    public Attr createAttribute( String name ) throws DOMException {
        return AttrImpl.createAttribute( this, name );
    }


    public EntityReference createEntityReference( String name ) throws DOMException {
        throw new UnsupportedOperationException( "EntityReference creation not supported ");
    }


    public Node importNode( Node importedNode, boolean deep ) throws DOMException {
        switch (importedNode.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                return AttrImpl.importNode( this, (Attr) importedNode );
            case Node.TEXT_NODE:
                return TextImpl.importNode( this, (Text) importedNode );
            case Node.ELEMENT_NODE:
                return ElementImpl.importNode( this, (Element) importedNode, deep );
            default:
                throw new DOMException( DOMException.NOT_SUPPORTED_ERR, "Cannot import node type " + importedNode.getNodeType() );
        }
    }


    public Element getElementById( String elementId ) {
        return null;
    }


    public Element createElementNS( String namespaceURI, String qualifiedName ) throws DOMException {
        if (namespaceURI != null) throw new UnsupportedOperationException( "Namespaces are not supported" );
        return createElement( qualifiedName );
    }


    public Attr createAttributeNS( String namespaceURI, String qualifiedName ) throws DOMException {
        if (namespaceURI != null) throw new UnsupportedOperationException( "Namespaces are not supported" );
        return createAttribute( qualifiedName );
    }


    public NodeList getElementsByTagNameNS( String namespaceURI, String localName ) {
        if (namespaceURI != null) throw new UnsupportedOperationException( "Namespaces are not supported" );
        return getElementsByTagName( localName );
    }


    void importChildren( Node original, Node copy ) {
        NodeList children = original.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node childCopy = importNode( children.item(i), /* deep */ true );
            copy.appendChild( childCopy );
        }
    }


}
