package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2000-2008, Russell Gold
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
import java.util.Iterator;

import junit.framework.TestSuite;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A test for the XML handling functionality.
 **/
public class XMLPageTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( XMLPageTest.class );
    }


    public XMLPageTest( String name ) {
        super( name );
    }


    public void testXML() throws Exception {
        defineResource( "SimplePage.xml",
                        "<?xml version=\"1.0\" ?><main><title>See me now</title></main>",
                        "text/xml" );

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/SimplePage.xml" );
        WebResponse simplePage = wc.getResponse( request );
        simplePage.getDOM();
    }
    
    /**
     * test case for BR [2373755] by Frank Waldheim
     * deactivated since it is the opposite of 1281655
     * @throws Exception
     */
    public void xtestXMLisHTML() throws Exception {
    	String originalXml = "<?xml version=\"1.0\" ?><main><title>See me now</title></main>";
    	defineResource("SimplePage.xml", originalXml, "text/xml");
    	WebConversation wc = new WebConversation();
    	WebRequest request = new GetMethodWebRequest(getHostPath()+"/SimplePage.xml");
    	WebResponse simplePage = wc.getResponse(request);
    	// we do not have an html result
    	assertFalse("xml result is not HTML",simplePage.isHTML());
    	// get the main element as root
    	assertNotNull("we do have an root-element",simplePage.getDOM().getDocumentElement()); 
    	assertEquals("the actual root must be the root of our test-xml",simplePage.getDOM().getDocumentElement().getTagName(),"main");
    }


    public void testTraversal() throws Exception {
        defineResource( "SimplePage.xml",
                        "<?xml version='1.0' ?><zero><main><first><second/></first><main><normal/><simple/></main><after/></main><end/></zero>",
                        "text/xml" );
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/SimplePage.xml" );
        WebResponse simplePage = wc.getResponse( request );
        NodeUtils.PreOrderTraversal pot = new NodeUtils.PreOrderTraversal( simplePage.getDOM() );
        final StringBuffer sb = new StringBuffer();
        pot.perform( new NodeUtils.NodeAction() {
            public boolean processElement( NodeUtils.PreOrderTraversal traversal, Element element ) {
                if (element.getNodeName().toLowerCase().equals( "main" )) {
                    traversal.pushContext( "x" );
                } else {
                    for (Iterator i = traversal.getContexts(); i.hasNext();) 
                    	sb.append( i.next() );
                    sb.append( element.getNodeName() ).append( "|" );
                }
                return true;
            }
            public void processTextNode( NodeUtils.PreOrderTraversal traversal, Node textNode ) {
            }
        } );
        // pre [ 1281655 ] [patch] result
        String expected="zero|xfirst|xsecond|xxnormal|xxsimple|xafter|end|";
        // new result
        // expected="HTML|HEAD|ZERO|xFIRST|xSECOND|xxNORMAL|xxSIMPLE|xAFTER|END|";
        String got=sb.toString().toLowerCase();
        assertTrue( "Traversal result", got.endsWith(expected) );
    }
}
