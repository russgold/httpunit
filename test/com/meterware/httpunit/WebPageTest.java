package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2000-2001, Russell Gold
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

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Dictionary;
import java.util.Vector;

import java.io.*;

import org.w3c.dom.Document;


/**
 * Unit tests for page structure, style, and headers.
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 * @author <a href="mailto:bx@bigfoot.com">Benoit Xhenseval</a>
 **/
public class WebPageTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }


    public static Test suite() {
        return new TestSuite( WebPageTest.class );
    }


    public WebPageTest( String name ) {
        super( name );
    }


    public void tearDown() throws Exception {
        super.tearDown();
        HttpUnitOptions.resetDefaultCharacterSet();
        HttpUnitOptions.setAutoRefresh( false );
    }


    public void testNoResponse() throws Exception {
        WebConversation wc = new WebConversation();
        try {
            WebRequest request = new GetMethodWebRequest( getHostPath() + "/SimplePage.html" );
            WebResponse simplePage = wc.getResponse( request );
            fail( "Did not complain about missing page" );
        } catch (HttpNotFoundException e) {
        }
    }


    public void testTitle() throws Exception {
        defineResource( "SimplePage.html",
                        "<html><head><title>A Sample Page</title></head>\n" +
                        "<body>This has no forms but it does\n" +
                        "have <a href=\"/other.html\">an <b>active</b> link</A>\n" +
                        " and <a name=here>an anchor</a>\n" +
                        "<a href=\"basic.html\"><IMG SRC=\"/images/arrow.gif\" ALT=\"Next -->\" WIDTH=1 HEIGHT=4></a>\n" +
                        "</body></html>\n" );
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/SimplePage.html" );
        WebResponse simplePage = wc.getResponse( request );
        assertEquals( "Title", "A Sample Page", simplePage.getTitle() );
        assertEquals( "Character set", "iso-8859-1", simplePage.getCharacterSet() );
        assertNull( "No refresh request should have been found", simplePage.getRefreshRequest() );
    }


    public void testLocalFile() throws Exception {
        File file = new File( "temp.html" );
        FileWriter fw = new FileWriter( file );
        PrintWriter pw = new PrintWriter( fw );
        pw.println( "<html><head><title>A Sample Page</title></head>" );
        pw.println( "<body>This is a very simple page<p>With not much text</body></html>" );
        pw.close();

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( "file:" + file.getAbsolutePath() );
        WebResponse simplePage = wc.getResponse( request );
        assertEquals( "Title", "A Sample Page", simplePage.getTitle() );
        assertEquals( "Character set", System.getProperty( "file.encoding" ), simplePage.getCharacterSet() );

        file.delete();
    }


    public void testNoLocalFile() throws Exception {
        File file = new File( "temp.html" );
        file.delete();

        try {
            WebConversation wc = new WebConversation();
            WebRequest request = new GetMethodWebRequest( "file:" + file.getAbsolutePath() );
            WebResponse simplePage = wc.getResponse( request );
            fail( "Should have complained about missing file" );
        } catch (java.io.FileNotFoundException e) {
        }

    }


    public void testSpecifiedEncoding() throws Exception {
        String hebrewTitle = "\u05d0\u05d1\u05d2\u05d3";
        String page = "<html><head><title>" + hebrewTitle + "</title></head>\n" +
                      "<body>This has no data\n" +
                      "</body></html>\n";
        defineResource( "SimplePage.html", page );
        setResourceCharSet( "SimplePage.html", "iso-8859-8", true );

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/SimplePage.html" );
        WebResponse simplePage = wc.getResponse( request );

        assertEquals( "Title", hebrewTitle, simplePage.getTitle() );
        assertEquals( "Character set", "iso-8859-8", simplePage.getCharacterSet() );
    }


    public void testUnspecifiedEncoding() throws Exception {
        String hebrewTitle = "\u05d0\u05d1\u05d2\u05d3";
        String page = "<html><head><title>" + hebrewTitle + "</title></head>\n" +
                      "<body>This has no data\n" +
                      "</body></html>\n";
        defineResource( "SimplePage.html", page );
        setResourceCharSet( "SimplePage.html", "iso-8859-8", false );

        HttpUnitOptions.setDefaultCharacterSet( "iso-8859-8" );
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/SimplePage.html" );
        WebResponse simplePage = wc.getResponse( request );

        assertEquals( "Character set", "iso-8859-8", simplePage.getCharacterSet() );
        assertEquals( "Title", hebrewTitle, simplePage.getTitle() );
    }


   public void testMetaEncoding() throws Exception {
       String hebrewTitle = "\u05d0\u05d1\u05d2\u05d3";
       String page = "<html><head><title>" + hebrewTitle + "</title>" +
                     "<meta Http_equiv=content-type content=\"text/html; charset=iso-8859-8\"></head>\n" +
                     "<body>This has no data\n" +
                     "</body></html>\n";
       defineResource( "SimplePage.html", page );
       setResourceCharSet( "SimplePage.html", "iso-8859-8", false );

       WebConversation wc = new WebConversation();
       WebRequest request = new GetMethodWebRequest( getHostPath() + "/SimplePage.html" );
       WebResponse simplePage = wc.getResponse( request );

       assertEquals( "Character set", "iso-8859-8", simplePage.getCharacterSet() );
       assertEquals( "Title", hebrewTitle, simplePage.getTitle() );
    }


    public void testHebrewForm() throws Exception {
        String hebrewName = "\u05d0\u05d1\u05d2\u05d3";
        defineResource( "HebrewForm.html",
                        "<html><head></head>" +
                        "<form method=POST action=\"SayHello\">" +
                        "<input type=text name=name><input type=submit></form></body></html>" );
        setResourceCharSet( "HebrewForm.html", "iso-8859-8", true );
        defineResource( "SayHello", new PseudoServlet() {
            public WebResource getPostResponse() {
                try {
                    String name = getParameter( "name" )[0];
                    WebResource result = new WebResource( "<html><body><table><tr><td>Hello, " +
                                                          new String( name.getBytes( "iso-8859-1" ), "iso-8859-8" ) +
                                                          "</td></tr></table></body></html>" );
                    result.setCharacterSet( "iso-8859-8" );
                    result.setSendCharacterSet( true );
                    return result;
                } catch (java.io.UnsupportedEncodingException e) {
                    return null;
                }
            }
        } );

        WebConversation wc = new WebConversation();
        WebResponse formPage = wc.getResponse( getHostPath() + "/HebrewForm.html" );
        WebForm form = formPage.getForms()[0];
        WebRequest request = form.getRequest();
        request.setParameter( "name", hebrewName );

        WebResponse answer = wc.getResponse( request );
        String[][] cells = answer.getTables()[0].asText();

        assertEquals( "Message", "Hello, " + hebrewName, cells[0][0] );
        assertEquals( "Character set", "iso-8859-8", answer.getCharacterSet() );
    }


    public void testMetaRefreshRequest() throws Exception {
        String refreshURL = getHostPath() + "/NextPage.html";
        String page = "<html><head><title>Sample</title>" +
                      "<meta Http_equiv=refresh content='2;" + refreshURL + "'></head>\n" +
                      "<body>This has no data\n" +
                      "</body></html>\n";
        defineResource( "SimplePage.html", page );

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/SimplePage.html" );
        WebResponse simplePage = wc.getResponse( request );

        assertEquals( "Refresh URL", refreshURL, simplePage.getRefreshRequest().getURL().toExternalForm() );
        assertEquals( "Refresh delay", 2, simplePage.getRefreshDelay() );
     }


    public void testMetaRefreshURLRequest() throws Exception {
        String refreshURL = getHostPath() + "/NextPage.html";
        String page = "<html><head><title>Sample</title>" +
                      "<meta Http-equiv=refresh content='2;URL=NextPage.html'></head>\n" +
                      "<body>This has no data\n" +
                      "</body></html>\n";
        defineResource( "SimplePage.html", page );

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/SimplePage.html" );
        WebResponse simplePage = wc.getResponse( request );

        assertEquals( "Refresh URL", refreshURL, simplePage.getRefreshRequest().getURL().toExternalForm() );
        assertEquals( "Refresh delay", 2, simplePage.getRefreshDelay() );
     }


    public void testAutoRefresh() throws Exception {
        String refreshURL = getHostPath() + "/NextPage.html";
        String page = "<html><head><title>Sample</title>" +
                      "<meta Http_equiv=refresh content='2;" + refreshURL + "'></head>\n" +
                      "<body>This has no data\n" +
                      "</body></html>\n";
        defineResource( "SimplePage.html", page );
        defineWebPage( "NextPage", "Not much here" );

        HttpUnitOptions.setAutoRefresh( true );
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/SimplePage.html" );
        WebResponse simplePage = wc.getResponse( request );

        assertNull( "No refresh request should have been found", simplePage.getRefreshRequest() );
     }

    /**
     * Test the meta tag content retrieval
     * @author <a href="mailto:bx@bigfoot.com">Benoit Xhenseval</a>
     **/
    public void testMetaTag() throws Exception {
        String refreshURL = getHostPath() + "/NextPage.html";
        String page = "<html><head><title>Sample</title>" +
                      "<meta Http-equiv=\"Expires\" content=\"now\"/>\n" +
                      "<meta name=\"robots\" content=\"index,follow\"/>" +
                      "<meta name=\"keywords\" content=\"test\"/>" +
                      "</head>\n" +
                      "<body>This has no data\n" +
                      "</body></html>\n";
        defineResource( "SimplePage.html", page );

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/SimplePage.html" );
        WebResponse simplePage = wc.getResponse( request );

        assertEquals( "robots meta tag","index,follow",simplePage.getMetaTagContent("name","robots"));
        assertEquals( "keywords meta tag","test",simplePage.getMetaTagContent("name","keywords"));
        assertEquals( "Expires meta tag","now",simplePage.getMetaTagContent("http-equiv","Expires"));
     }

    /**
     * test the stylesheet retrieval
     * @author <a href="mailto:bx@bigfoot.com">Benoit Xhenseval</a>
     **/
    public void testGetExternalStylesheet() throws Exception {
        String refreshURL = getHostPath() + "/NextPage.html";
        String page = "<html><head><title>Sample</title>" +
                      "<link type=\"text/css\" rel=\"stylesheet\" href=\"/style.css\"/>" +
                      "</head>\n" +
                      "<body>This has no data\n" +
                      "</body></html>\n";
        defineResource( "SimplePage.html", page );

        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/SimplePage.html" );
        WebResponse simplePage = wc.getResponse( request );

        assertEquals( "Stylesheet","/style.css",simplePage.getExternalStyleSheet());
     }

    private String toUnicode( String string ) {
        StringBuffer sb = new StringBuffer( );
        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            sb.append( "\\u" );
            sb.append( Integer.toHexString( chars[i] ) );
        }
        return sb.toString();
    }
}
