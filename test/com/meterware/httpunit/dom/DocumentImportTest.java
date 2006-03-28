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
import junit.textui.TestRunner;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import org.w3c.dom.*;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class DocumentImportTest extends TestCase {

    private DocumentImpl _document;

    public static void main( String[] args ) {
        TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( DocumentImportTest.class );
    }


    protected void setUp() throws Exception {
        super.setUp();
        _document = DocumentImpl.createDocument();
    }


    /**
     * Verifies the importing of an attribute node with no children.
     */
    public void testImportAttribute() throws Exception {
        Element element = _document.createElement( "rainbow" );
        Attr original = _document.createAttribute( "color" );
        element.setAttributeNode( original );
        original.setValue( "red" );

        Attr copy = (Attr) _document.importNode( original, false );
        assertEquals( "Node type", Node.ATTRIBUTE_NODE, copy.getNodeType() );
        assertEquals( "Node name", "color", copy.getNodeName() );
        assertNull( "Should have removed the original element", copy.getOwnerElement() );
        assertEquals( "Node value", "red", copy.getNodeValue() );
        assertTrue( "Node value should be specified", copy.getSpecified() );
    }


    /**
     * Verifies the importing of a text node.
     */
    public void testImportText() throws Exception {
        String textValue = "something to say";
        Text original = _document.createTextNode( textValue );

        Text copy = (Text) _document.importNode( original, false );
        assertEquals( "Node type", Node.TEXT_NODE, copy.getNodeType() );
        assertEquals( "Node name", "#text", copy.getNodeName() );
        assertEquals( "length", textValue.length(), copy.getLength() );
    }


    /**
     * Verifies the importing of a simple element with attributes.
     */
    public void testImportElementWithAttributes() throws Exception {
        Element original = _document.createElement( "zork" );
        Attr size = _document.createAttribute( "interactive" );
        original.setAttribute( "version", "2.0" );
        original.setAttributeNode( size );

        Element copy = (Element) _document.importNode( original, /* deep */ false );
        assertEquals( "Node type", Node.ELEMENT_NODE, copy.getNodeType() );
        assertEquals( "Node name", "zork", copy.getNodeName() );
        assertEquals( "version attribute", "2.0", copy.getAttribute( "version" ) );
        assertTrue( "copy does not have interactive attribute", copy.hasAttribute( "interactive") );
    }


    /**
     * Verifies the shallow importing of an element with children.
     */
    public void testShallowImportElementWithChildren() throws Exception {
        Element original = _document.createElement( "zork" );
        original.appendChild( _document.createElement( "foo" ) );
        original.appendChild( _document.createElement( "bar" ) );

        Element copy = (Element) _document.importNode( original, /* deep */ false );
        assertEquals( "Node type", Node.ELEMENT_NODE, copy.getNodeType() );
        assertEquals( "Node name", "zork", copy.getNodeName() );
        assertFalse( "copy should have no children", copy.hasChildNodes() );
    }


    /**
     * Verifies the deep importing of an element with children.
     */
    public void testDeepImportElementWithChildren() throws Exception {
        Element original = _document.createElement( "zork" );
        original.appendChild( _document.createElement( "foo" ) );
        original.appendChild( _document.createTextNode( "in the middle" ) );
        original.appendChild( _document.createElement( "bar" ) );

        Element copy = (Element) _document.importNode( original, /* deep */ true );
        assertEquals( "Node type", Node.ELEMENT_NODE, copy.getNodeType() );
        assertEquals( "Node name", "zork", copy.getNodeName() );
        assertTrue( "copy should have children", copy.hasChildNodes() );

        NodeList children = copy.getChildNodes();
        assertEquals( "Number of child nodes", 3, children.getLength() );

        Node child = copy.getFirstChild();
        verifyNode( "1st", child, Node.ELEMENT_NODE, "foo", null );
        child = child.getNextSibling();
        verifyNode( "2nd", child,  Node.TEXT_NODE, "#text", "in the middle" );
        child = child.getNextSibling();
        verifyNode( "3rd", child,  Node.ELEMENT_NODE, "bar", null );
    }


    private void verifyNode( String comment, Node node, short type, String name, String value ) {
        assertEquals( comment + " node type", type, node.getNodeType() );
        assertEquals( comment + " node name", name, node.getNodeName() );
        assertEquals( comment + " node value", value, node.getNodeValue() );
    }

}
