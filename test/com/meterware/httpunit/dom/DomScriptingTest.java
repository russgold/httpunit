package com.meterware.httpunit.dom;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLBodyElement;
import org.w3c.dom.html.HTMLAnchorElement;
import org.mozilla.javascript.Scriptable;

/**
 * Tests basic scripting via the DOM.
 */
public class DomScriptingTest extends AbstractHTMLElementTest {


    public void testGetDocument() throws Exception {
        Element element = createElement( "body" );
        assertEquals( "Returned document", _htmlDocument, ((Scriptable) element).get( "document", null ) );
    }


    public void testDocumentGetTitle() throws Exception {
        _htmlDocument.setTitle( "something" );
        assertEquals( "title", "something", _htmlDocument.get( "title", null ) );

        Node body = createElement( "body" );
        assertEquals( "title", "something", ScriptingSupport.evaluateExpression( body, "document.title" ) );
    }


    public void testDocumentPutTitle() throws Exception {
        _htmlDocument.put( "title", _htmlDocument, "right here" );
        assertEquals( "title after put", "right here", _htmlDocument.getTitle() );

        Node body = createElement( "body" );
        ScriptingSupport.evaluateExpression( body, "document.title='new value'" );
        assertEquals( "title after script", "new value", _htmlDocument.getTitle() );
    }


    public void testElementPutTitle() throws Exception {
        HTMLBodyElement body = (HTMLBodyElement) createElement( "body" );
        Scriptable scriptableBody = (Scriptable) body;

        scriptableBody.put( "title", scriptableBody, "right here" );
        assertEquals( "title after put", "right here", body.getTitle() );

        ScriptingSupport.evaluateExpression( body, "title='new value'" );
        assertEquals( "title after script", "new value", body.getTitle() );
    }


    public void testBodyAttributes() throws Exception {
        HTMLBodyElement body = addBodyElement();
        body.setBgColor( "red" );

        assertEquals( "initial background color", "red", ScriptingSupport.evaluateExpression( _htmlDocument, "body.bgcolor" ) );

        ScriptingSupport.evaluateExpression( _htmlDocument, "body.id='blue'" );
        assertEquals( "revised foreground color", "blue", body.getId() );
    }


    public void testNumericAttributes() throws Exception {
        HTMLBodyElement body = addBodyElement();
        HTMLAnchorElementImpl anchor = (HTMLAnchorElementImpl) createElement( "a" );
        body.appendChild( anchor );
        anchor.setTabIndex( 4 );

        assertEquals( "initial tab index", new Integer(4), ScriptingSupport.evaluateExpression( anchor, "tabindex" ) );

        ScriptingSupport.evaluateExpression( anchor, "tabindex=6" );
        assertEquals( "revised tab index", 6, anchor.getTabIndex() );
    }

    private HTMLBodyElement addBodyElement() {
        HTMLBodyElement body = (HTMLBodyElement) createElement( "body" );
        _htmlDocument.setBody( body );
        return body;
    }


    public void testCreateElement() throws Exception {
        Object node = ScriptingSupport.evaluateExpression( _htmlDocument, "createElement( 'a' )" );
        assertNotNull( "No node returned", node );
        assertTrue( "Node is not an anchor element", node instanceof HTMLAnchorElement );
    }


    public void testDocumentLinksCollection() throws Exception {
        HTMLBodyElement body = addBodyElement();
        appendLink( body, "red", "red.html" );
        appendLink( body, "blue", "blue.html" );

        assertEquals( "number of links", new Integer(2), ScriptingSupport.evaluateExpression( _htmlDocument, "links.length" ) );
        Object second = ScriptingSupport.evaluateExpression( _htmlDocument, "links[1]" );
        assertNotNull( "Did not obtain any link object", second );
        assertTrue( "Object is not a link element", second instanceof HTMLAnchorElement );
        assertEquals( "Link ID", "blue", ((HTMLAnchorElement) second).getId() );

        assertEquals( "red link href", "red.html", ScriptingSupport.evaluateExpression( _htmlDocument, "links.red.href" ) );
    }


    private void appendLink( HTMLBodyElement body, String id, String href ) {
        HTMLAnchorElement anchor1 = (HTMLAnchorElement) createElement( "a" );
        anchor1.setId( id );
        anchor1.setHref( href );
        body.appendChild( anchor1 );
    }


    public void testConvertable() throws Exception {
        assertConvertable( String.class, String.class );
        assertConvertable( Integer.class, String.class );
        assertConvertable( String.class, Integer.class );
        assertConvertable( Short.class, Integer.class );
        assertConvertable( String.class, Boolean.class );
        assertConvertable( Byte.class, int.class );
    }

    private void assertConvertable( Class valueType, Class parameterType ) {
        assertTrue( valueType.getName() + " should be convertable to " + parameterType.getName(), ScriptingSupport.isConvertableTo( valueType, parameterType ) );
    }

}
