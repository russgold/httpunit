package com.meterware.httpunit.dom;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2007, Russell Gold
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
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Stack;

import com.meterware.httpunit.scripting.ScriptingHandler;
import junit.framework.TestSuite;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
public class DomWindowTest extends AbstractHTMLElementTest {

    private Stack  _proxyCalls = new Stack();
    private String _answer;
    private String _replacementText = null;


    public static TestSuite suite() {
        return new TestSuite( DomWindowTest.class );
    }

    /**
     * Verifies that we can obtain a window for a document and then retrieve document for that window.
     */
    public void testDocumentWindowAccess() throws Exception {
        DomWindow window = _htmlDocument.getWindow();
        assertSame( "The original document", _htmlDocument, window.getDocument() );
        assertSame( "The window upon subsequence request", window, _htmlDocument.getWindow() );
        assertSame( "The window accessed from itself", window, window.getWindow() );
        assertSame( "The window's 'self' object", window, window.getSelf() );
    }

    /**
     * Verifies that the open method returns a window with an appropriate document.
     */
    public void testWindowOpen() throws Exception {
        DomWindow window1 = createMainWindow();
        DomWindow window2 = window1.open( "next.html", "broken", "", false );
        HTMLDocument document = window2.getDocument();
        assertNotNull( "Window has no associated document", document );
        assertEquals( "Title of document in new window", "broken (next.html)", document.getTitle() );

        window2.close();
        assertLastProxyMethod( "close" );
    }


    /**
     * Writes to a document should appear in the window's document write buffer.
     */
    public void testDocumentWrite() throws Exception {
        DomWindow window1 = createMainWindow();
        window1.getDocument().write( "A simple string" );
        assertEquals( "Contents of write buffer", "A simple string", window1.getDocumentWriteBuffer() );
        window1.discardDocumentWriteBuffer();
        assertEquals( "Contents of cleared write buffer", "", window1.getDocumentWriteBuffer() );

    }


    private void assertLastProxyMethod( String method ) {
        assertEquals( "Last proxy method called", method, popProxyCall() );
    }


    private DomWindow createMainWindow() {
        DomWindow window = _htmlDocument.getWindow();
        window.setProxy( new TestWindowProxy( _htmlDocument ) );
        return window;
    }


    /**
     * Verifies that an alert request is sent to the proxy appropriately.
     */
    public void testAlert() throws Exception {
        DomWindow window = createMainWindow();
        window.alert( "A little message" );
        assertLastProxyMethod( "alert( A little message )" );
    }

    /**
     * Verifies that a confirmation request is sent to the proxy and the appropriate answer is returned.
     */
    public void testConfirm() throws Exception {
        DomWindow window = createMainWindow();
        _answer = "no";
        assertFalse( "Should have said no", window.confirm( "Time to quit?" ) );
        assertLastProxyMethod( "confirm( Time to quit? )" );
        _answer = "yes";
        assertTrue( "Should have said yes", window.confirm( "Want to stay?" ) );
        assertLastProxyMethod( "confirm( Want to stay? )" );
    }


    /**
     * Verifies that a prompt is sent to the proxy and the appropriate answer is returned.
     */
    public void testPrompt() throws Exception {
        DomWindow window = createMainWindow();
        _answer = null;
        assertEquals( "User default choice", "0", window.prompt( "How many choices?", "0" ) );
        assertLastProxyMethod( "prompt( How many choices? )" );
        _answer = "blue";
        assertEquals( "Explicit user choice", "blue", window.prompt( "What is your favorite color?", "yellow" ) );
        assertLastProxyMethod( "prompt( What is your favorite color? )" );
    }

    /**
     * Verifies that writing to and closing a document triggers a replaceText request.
     */
    public void testTextReplacement() throws Exception {
        DomWindow window = createMainWindow();
        window.getDocument().write( "A bit of text" );
        window.getDocument().close();
        assertNotNull( "No text replacement occurred", _replacementText );
        assertEquals( "Replacement text", "A bit of text", _replacementText );
    }

    // todo test getNavigator
    // todo test getScreen
    // todo test getLocation, setLocation
    // todo test getFrames
    // todo test clearCaches, getDocumentWriteBuffer, clearDocumentWriteBuffer


    /**
     * Verifies simply the existence of some methods not currently implemented. Todo make them do something useful.
     */
    public void testMethodExistences() throws Exception {
        DomWindow window =  _htmlDocument.getWindow();
        window.setTimeout( 40 );
        window.focus();
        window.moveTo( 10, 20 );
    }


    private String popProxyCall() {
        if (_proxyCalls.isEmpty()) return "";
        return (String) _proxyCalls.pop();
    }


    private void pushProxyCall( String call ) {
        _proxyCalls.push( call );
    }


    class TestWindowProxy implements DomWindowProxy {

        private HTMLDocumentImpl _document;


        public TestWindowProxy( HTMLDocumentImpl htmlDocument ) {
            _document = htmlDocument;
            _document.getWindow().setProxy( this );
        }


        public ScriptingHandler getScriptingHandler() {
            return _document.getWindow();
        }


        public DomWindowProxy openNewWindow( String name, String relativeUrl ) throws IOException, SAXException {
            HTMLDocumentImpl document = new HTMLDocumentImpl();
            document.setTitle( name + " (" + relativeUrl + ')' );
            return new TestWindowProxy( document );
        }


        public void close() {
            pushProxyCall( "close" );
        }


        public void alert( String message ) {
            pushProxyCall( "alert( " + message + " )" );
        }


        public boolean confirm( String message ) {
            pushProxyCall( "confirm( " + message + " )" );
            return _answer.equals( "yes" );
        }


        public String prompt( String prompt, String defaultResponse ) {
            pushProxyCall( "prompt( " + prompt + " )" );
            return _answer == null ? defaultResponse : _answer;
        }


        public boolean replaceText( String text, String contentType ) {
            _replacementText = text;
            return true;
        }
    }

}
