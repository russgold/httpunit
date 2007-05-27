package com.meterware.httpunit.dom;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2006-2007, Russell Gold
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
import org.w3c.dom.Text;
import org.w3c.dom.html.*;
import junit.framework.TestSuite;

/**
 * Test for HTMLDocumentImpl.
 */
public class HTMLDocumentTest extends AbstractHTMLElementTest {

    private Element _headElement;
    private Element _htmlElement;


    public static TestSuite suite() {
        return new TestSuite( HTMLDocumentTest.class );
    }


    protected void setUp() throws Exception {
        super.setUp();

        _htmlDocument.appendChild( _htmlElement = createElement( "html" ) );
        _htmlElement.appendChild( _headElement = createElement( "head" ) );
    }


    /**
     * Verifies that we can detect the lack of a document title.
     */
    public void testEmptyDocumentTitle() throws Exception {
        assertEquals( "title seen by document", "", _htmlDocument.getTitle() );
    }


    /**
     * Verifies that we can find the document title.
     */
    public void testReadDocumentTitle() throws Exception {
        Element title = createElement( "title" );
        Text text = _htmlDocument.createTextNode( "something here" );
        title.appendChild( text );

        _headElement.appendChild( title );

        assertEquals( "title seen by document", "something here", _htmlDocument.getTitle() );
    }


    /**
     * Verifies that we can modify an existing document title.
     */
    public void testModifyDocumentTitle() throws Exception {
        Element title = createElement( "title" );
        Text text = _htmlDocument.createTextNode( "something here" );
        title.appendChild( text );

        _headElement.appendChild( title );

        _htmlDocument.setTitle( "new value" );
        assertEquals( "title seen by document", "new value", _htmlDocument.getTitle() );
    }


    /**
     * Verifies that we can set the document title if none exists.
     */
    public void testCreateDocumentTitle() throws Exception {
        _htmlDocument.setTitle( "initial value" );
        assertEquals( "title seen by document", "initial value", _htmlDocument.getTitle() );
    }


    /**
     * Verifies retrieval of the body element.
     */
    public void testGetBody() throws Exception {
        Element body = createElement( "body" );
        _htmlElement.appendChild( body );

        assertSame( "Body element", body, _htmlDocument.getBody() );
    }


    /**
     * Verifies setting the body element.
     */
    public void testSetBody() throws Exception {
        HTMLElement body = (HTMLElement) createElement( "body" );
        _htmlDocument.setBody( body );

        assertSame( "Body element", body, _htmlDocument.getBody() );
    }


    /**
     * Verifies retrieval of the collection of links ('img' tags).
     */
    public void testGetImages() throws Exception {
        HTMLElement body = (HTMLElement) createElement( "body" );
        _htmlDocument.setBody( body );

        HTMLAnchorElement anchor1 = (HTMLAnchorElement) createElement( "a" );
        anchor1.setHref( "first" );
        body.appendChild( anchor1 );

        HTMLImageElement image1 = (HTMLImageElement) createElement( "img" );
        body.appendChild( image1 );

        HTMLImageElement image2 = (HTMLImageElement) createElement( "img" );
        image2.setName( "ship" );
        body.appendChild( image2 );

        HTMLAreaElement area1 = (HTMLAreaElement) createElement( "area" );
        body.appendChild( area1 );

        HTMLCollection images = _htmlDocument.getImages();
        assertNotNull( "Did not get the image collection", images );
        assertEquals( "Number of images", 2, images.getLength() );
        assertSame( "image 1", image1, images.item( 0 ) );
        assertSame( "image 2", image2, images.item( 1 ) );
    }


    /**
     * Verifies retrieval of the collection of links ('area' tags and 'a' tags with 'href' attributes).
     */
    public void testGetLinks() throws Exception {
        HTMLElement body = (HTMLElement) createElement( "body" );
        _htmlDocument.setBody( body );

        HTMLAnchorElement anchor1 = (HTMLAnchorElement) createElement( "a" );
        anchor1.setHref( "first" );
        body.appendChild( anchor1 );

        HTMLAnchorElement anchor2 = (HTMLAnchorElement) createElement( "a" );
        anchor2.setHref( "tent" );
        body.appendChild( anchor2 );

        HTMLAnchorElement anchor3 = (HTMLAnchorElement) createElement( "a" );
        anchor3.setName( "ship" );
        body.appendChild( anchor3 );

        HTMLAreaElement area1 = (HTMLAreaElement) createElement( "area" );
        body.appendChild( area1 );

        HTMLCollection links = _htmlDocument.getLinks();
        assertNotNull( "Did not get the links collection", links );
        assertEquals( "Number of links", 3, links.getLength() );
        assertSame( "link 1", area1,   links.item( 0 ) );
        assertSame( "link 2", anchor1, links.item( 1 ) );
        assertSame( "link 3", anchor2, links.item( 2 ) );
    }


    /**
     * Verifies retrieval of the collection of forms.
     */
    public void testGetForms() throws Exception {
        HTMLElement body = (HTMLElement) createElement( "body" );
        _htmlDocument.setBody( body );

        HTMLFormElement form1 = (HTMLFormElement) createElement( "form" );
        form1.setId( "left" );
        body.appendChild( form1 );

        HTMLFormElement form2 = (HTMLFormElement) createElement( "form" );
        form2.setName( "right" );
        body.appendChild( form2 );

        HTMLCollection forms = _htmlDocument.getForms();
        assertNotNull( "Did not get the forms collection", forms );
        assertEquals( "Number of forms", 2, forms.getLength() );
        assertSame( "form 1", form1, forms.item( 0 ) );
        assertSame( "form 2", form2, forms.item( 1 ) );

        assertSame( "form 1 by id", form1, forms.namedItem( "left" ) );
        assertSame( "form 2 by name", form2, forms.namedItem( "right" ) );
    }


    /**
     * Verifies retrieval of the collection of anchors.
     */
    public void testGetAnchors() throws Exception {
        HTMLElement body = (HTMLElement) createElement( "body" );
        _htmlDocument.setBody( body );

        HTMLAnchorElement anchor1 = (HTMLAnchorElement) createElement( "a" );
        anchor1.setName( "boat" );
        body.appendChild( anchor1 );

        HTMLAnchorElement anchor2 = (HTMLAnchorElement) createElement( "a" );
        anchor2.setId( "tent" );
        body.appendChild( anchor2 );

        HTMLAnchorElement anchor3 = (HTMLAnchorElement) createElement( "a" );
        anchor3.setName( "ship" );
        anchor3.setHref( ".." );
        body.appendChild( anchor3 );

        HTMLCollection anchors = _htmlDocument.getAnchors();
        assertNotNull( "Did not get the anchor collection", anchors );
        assertEquals( "Number of anchors", 2, anchors.getLength() );
        assertSame( "anchor 1", anchor1, anchors.item( 0 ) );
        assertSame( "anchor 3", anchor3, anchors.item( 1 ) );
    }


    /**
     * Verifies that the document has an empty write buffer by default.
     */
    public void testInitialWriteBuffer() throws Exception {
        assertNotNull( "No write buffer was defined for the document", _htmlDocument.getWriteBuffer() );
        assertEquals( "Default buffer size", 0, _htmlDocument.getWriteBuffer().length() );
    }


    /**
     * Verifies that writing to the document updates the write buffer.
     */
    public void testWriteBufferUpdate() throws Exception {
        _htmlDocument.write( "This is a test" );
        assertNotNull( "No write buffer was defined for the document", _htmlDocument.getWriteBuffer() );
        assertEquals( "Result of write buffer", "This is a test", _htmlDocument.getWriteBuffer().toString() );
    }


    /**
     * Verifies that writing to the document updates the write buffer.
     */
    public void testWritelnBufferUpdate() throws Exception {
        _htmlDocument.writeln( "This is a test" );
        _htmlDocument.writeln( "And another." );
        assertNotNull( "No write buffer was defined for the document", _htmlDocument.getWriteBuffer() );
        assertEquals( "Result of write buffer", "This is a test\r\nAnd another.\r\n", _htmlDocument.getWriteBuffer().toString() );
    }


    /**
     * Verifies that clearing the write buffer leaves it ready for new writes.
     */
    public void testBufferClear() throws Exception {
        _htmlDocument.write( "This is a test" );
        _htmlDocument.clearWriteBuffer();
        assertEquals( "Cleared buffer length", 0, _htmlDocument.getWriteBuffer().length() );
        _htmlDocument.write( "And another." );
        assertNotNull( "No write buffer was defined for the document", _htmlDocument.getWriteBuffer() );
        assertEquals( "Result of write buffer", "And another.", _htmlDocument.getWriteBuffer().toString() );
    }
}
