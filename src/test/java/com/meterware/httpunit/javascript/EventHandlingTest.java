package com.meterware.httpunit.javascript;
/********************************************************************************************************************
 * $Id$
 * $URL$
 *
 * Copyright (c) 2009, Wolfgang Fahl
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

import com.meterware.httpunit.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;


/**
 * @author Wolfgang Fahl
 */
public class EventHandlingTest extends HttpUnitTest {

    private WebConversation _wc;

    @Before
    public void setUp() throws Exception {
        _wc = new WebConversation();
    }

    /**
     * add a resource with the given name and title defining the given javaScript and content
     *
     * @param name
     * @param title
     * @param javaScript
     * @param content
     */
    private void addResource(String name, String title, String onLoad, String javaScript, String content) {
        if (onLoad == null) {
            onLoad = "";
        }
        if (!onLoad.equals("")) {
            onLoad = " onload='" + onLoad + "'";
        }
        defineResource(name + ".html",
                "<html>\n\t<head>\n\t\t<title>" + title + "</title>\n" +
                        "\t\t<script type='text/javascript'>\n" +
                        javaScript +
                        "\t\t</script>\n\t</head>\n" +
                        "\t<body" + onLoad + ">\n\t\t" + content + "\n\t</body>\n</html>");
    }

    /**
     * get the response for the resource with the given name
     *
     * @param name
     * @return the response
     * @throws SAXException
     * @throws IOException
     */
    private WebResponse getResponse(String name) throws IOException, SAXException {
        WebResponse response = _wc.getResponse(getHostPath() + "/" + name + ".html");
        return response;
    }

    /**
     * test for [ 1163753 ] partial patch for bug 771335 (DOM2 Events support) by  Rafael Krzewski
     *
     * @throws SAXException
     * @throws IOException
     */
    @Test
    public void testSimpleEventHandler() throws IOException, SAXException {
        String javaScript =
                "			function testEventHandler() {\n" +
                        "				if (document.addEventListener) {\n" +
                        "					alert('found addEventListener');\n" +
                        "				}\n" +
                        "			}\n";
        String onLoad = "testEventHandler()";
        String content = "";
        String name = "simple1";
        addResource(name, "only check addEventListener function available", onLoad, javaScript, content);
        WebResponse response = getResponse(name);
        // System.out.println(response.getText());
        String alert = _wc.popNextAlert();
        assertEquals("found addEventListener", alert);
    }

    // FIXME need more tests for event handling

}
