package com.meterware.httpunit.parsing;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2001, Russell Gold.
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
import java.io.PrintWriter;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.meterware.httpunit.parsing.JTidyPrintWriter;
import com.meterware.httpunit.*;

/**
 * Unit tests for valid HTML test.  This is using the JTidy callback feature.
 *
 * @author <a href="mailto:bx@bigfoot.com">Benoit Xhenseval</a>
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
public class JTidyPrintWriterTest extends HttpUnitTest implements HtmlErrorListener {

    public static void main(String[] args) {
        junit.textui.TestRunner.run( suite() );
    }


    public static Test suite() {
        return new TestSuite( JTidyPrintWriterTest.class );
    }


    public JTidyPrintWriterTest( String name ) {
        super( name );
    }

    public void setUp() throws Exception {
        super.setUp();
        HttpUnitOptions.setParserWarningsEnabled(true);
        HttpUnitOptions.addHtmlErrorListener(this);
    }

    public void tearDown() throws Exception {
        HttpUnitOptions.resetDefaultCharacterSet();
        HttpUnitOptions.setAutoRefresh( false );
        HttpUnitOptions.removeHtmlErrorListener(this);
        super.tearDown();
    }

    /**
     * Tidy seems to be returning number in a funny format... test if it is handled ok
     **/
    public void testLongLine() throws Exception {
        URL url = new URL("http://localhost/blank.html");
        PrintWriter p = new JTidyPrintWriter(url);
        p.print("line 1234 column 1234");
        p.print("line 1,234 column 1,234");
        p.print("line 1,234,567 column 1,234,567");
        p.print("line 1,2,34 column 12,34");
        p.print("line 123,,4 column 12,,34");
    }

    public void testWrongXHTMLPage() throws Exception {
        defineResource( "SimplePage.html",
                        "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>"+
                        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"DTD/xhtml1-transitional.dtd\">"+
                        "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\">" +
                        "<head><title>A Sample Page</head>\n" +
                        "<body><p><b>Wrong embedded tags</p></b>\n" +
                        "have <a blef=\"other.html?a=1&b=2\">an invalid link</A>\n" +
                        "<IMG SRC=\"/images/arrow.gif\" WIDTH=1 HEIGHT=4>\n" +
                        "<unknownTag>bla</unknownTag>" +
                        "</body></html>\n" );
        _correctHTML = false;
        _foundErrors = false;
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/SimplePage.html" );
        wc.getResponse( request );
        assertTrue("Must have found errors",_foundErrors);
        assertEquals( "Expected URL", request.getURL(), _badURL );
    }

    public void testRightXHTMLPage() throws Exception {
        defineResource( "SimplePage.html",
                        "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>"+
                        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"DTD/xhtml1-transitional.dtd\">"+
                        "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\">" +
                        "<head><title>A Sample Page</title></head>\n" +
                        "<body><p><b>Wrong embedded tags</b></p>\n" +
                        "have <a blef=\"other.html?a=1&amp;b=2\">an invalid link</A>\n" +
                        "<IMG SRC=\"/images/arrow.gif\" alt=\"\" WIDTH=1 HEIGHT=4>\n" +
                        "</body></html>\n" );
        _correctHTML = true;
        _foundErrors = false;
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/SimplePage.html" );
        wc.getResponse( request );
        assertTrue("Must NOT have found errors",!_foundErrors);
    }

    public void warning( URL url, String msg, int line, int column )
    {
        _foundErrors = true;
        _badURL = url;

        if (!_correctHTML)
        {
            boolean found = false;

            found = msg.equals("Warning: missing </title> before </head>")
             || msg.equals("Warning: missing </b> before </p>")
             || msg.equals("Warning: discarding unexpected </b>")
             || msg.equals("Warning: <img> lacks \"alt\" attribute")
             || msg.equals("Warning: discarding unexpected <unknowntag>")
             || msg.equals("Warning: discarding unexpected </unknowntag>")
             || msg.equals("Warning: unescaped & or unknown entity \"&b\"");

            assertTrue("Found Expected Warnings " + msg,found);
        }
    }

    public void error(URL url, String msg, int line, int column)
    {
        _foundErrors = true;
        _badURL = url;

        if (!_correctHTML)
        {
            boolean found = false;

            found = msg.equals("Error: <unknowntag> is not recognized!");

            assertTrue("Found Expected Warnings " + msg,found);
        }
    }

    private boolean _correctHTML;
    private boolean _foundErrors;
    private URL     _badURL;

}