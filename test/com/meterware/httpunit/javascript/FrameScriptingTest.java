package com.meterware.httpunit.javascript;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2003-2004, Russell Gold
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

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * More complex tests of frame functionality.
 *
 * @author <a href="andrew.bickerton@hp.com">Andrew Bickerton</a>
 **/
public class FrameScriptingTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }


    public static Test suite() {
        return new TestSuite( FrameScriptingTest.class );
    }


    public FrameScriptingTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
        _wc = new WebConversation();

        defineWebPage( "Simple",  "This is a trivial page." );
        defineResource("image.gif", new byte[]{0, 1, 0, 1}, "image/gif");
        defineWebPage("Menu/Page/Target", "Target page");

        StringBuffer buff = new StringBuffer();
        buff.append("This frame contains JavaScript that re-writes the \"red\" frame if it exists.\n");
        buff.append("<script language=\"JavaScript\">\n");
        buff.append("if (window.parent.red) {\n");
        buff.append("  window.parent.red.document.open(\"text/html\")\n");
        buff.append("  with (window.parent.red.document) {\n");
        final String[] lines = getMenuHtmlLines(getHostPath() + "/Menu/Page/Main.html", "Target.html");
        for (int i = 0; i < lines.length; i++) {
            buff.append("    write('").append(lines[i]).append("\\n").append("');\n");
        }
        buff.append("    close();\n");
        buff.append("  }\n");
        buff.append("}\n");
        buff.append("</script>\n");

        defineWebPage("frameRewriter", buff.toString());
    }

    /**
     * Creates and returns the lines of HTML to represent a menu page with
     * a <code>&lt;base ...&gt;</code> tag and one link.
     * Used where the formulated HTML is to be embedded in JavaScript.
     * @param baseUrlString URL string to be used in the href of the 'base' tag.
     * @param linkUrlString URL string to be used in the href of the link.
     * @return String array representing the lines of HTML.
     */
    private String[] getMenuHtmlLines(String baseUrlString, String linkUrlString) {
        return new String[]{
                    "<html><head><title>Menu</title>",
                    "<base href=\"" + baseUrlString + "\" target=\"blue\"></base>",
                    "</head>",
                    "<body>",
                    "This is the menu.<br>",
                    "Link to <a href=\""+linkUrlString+"\">target page</a>.",
                    "</body></html>"};
    }

    /**
     * Creates and returns HTML to represent a menu page with a <code>&lt;base ...&gt;</code> tag and one link.
     * @param baseUrlString URL string to be used in the href of the 'base' tag.
     * @param linkUrlString URL string to be used in the href of the link.
     * @return String representing the HTML.
     */
    private String getMenuHtml(final String baseUrlString, final String linkUrlString) {
        final String[] menuLines = getMenuHtmlLines(baseUrlString, linkUrlString);
        StringBuffer pageBuffer = new StringBuffer();
        for (int i = 0; i < menuLines.length; i++) {
            String line = menuLines[i];
            pageBuffer.append(line).append("\n");
        }
        return pageBuffer.toString();
    }

    /**
     * (Re)defines the web page 'Frames.html', which is a frameset containing 'red' and 'blue' frames.
     * @param redSrc The source URL string for the 'red' frame.
     * @param blueSrc The source URL string for the 'blue' frame.
     */
    private void redefineFrames(String redSrc, String blueSrc) {
        defineResource("Frames.html",
                        "<HTML><HEAD><TITLE>Frameset</TITLE></HEAD>" +
                        "<FRAMESET cols=\"20%,80%\">" +
                        "    <FRAME src=\""+redSrc+"\" name=\"red\">" +
                        "    <FRAME src=\""+blueSrc+"\" name=\"blue\">" +
                        "</FRAMESET></HTML>");
    }

    /**
     * Test that an image can be used as the source for a frame within a frameset.
     */
    public void testImageInFrame() throws Exception {
        redefineFrames("/image.gif", "Simple.html");
        _wc.getResponse( getHostPath() + "/Frames.html" );
        WebResponse redFrame = _wc.getFrameContents("red");
        assertEquals("Red frame content-type", "image/gif", redFrame.getContentType());
    }

    /**
     * Test that a link within a frame that contains a <code>&lt;base ...&gt;</code> element is handled correctly.
     */
    public void testFrameContainingBaseElement() throws Exception {
        final String TARGET_TITLE = "Somewhere/Else/Target";
        defineWebPage( TARGET_TITLE,  "This is the target page." );
        defineWebPage( "Main",    "This is a simple page.");
        defineResource( "BaseRelLinker.html", getMenuHtml(getHostPath() + "/Somewhere/Near/", "../Else/Target.html") );
        redefineFrames("BaseRelLinker.html", "Main.html");

        _wc.getResponse( getHostPath() + "/Frames.html" );
        WebLink link = _wc.getFrameContents( "red" ).getLinks()[0];
        link.click();
        assertEquals("Content of blue frame after link click", TARGET_TITLE, _wc.getFrameContents("blue").getTitle());
    }

    /**
     * Test correct handling of a link within a frame that has been re-written by JavaScript,
     * and which contains a <code>&lt;base ...&gt;</code> element.
     */
    public void testFrameRewrittenToUseBaseElement() throws Exception {
        redefineFrames("/Simple.html", "frameRewriter.html");
        _wc.getResponse(getHostPath() + "/Frames.html");
        WebLink link = _wc.getFrameContents("red").getLinks()[0];
        link.click();
        assertEquals("Content of blue frame after clicking menu link", "Menu/Page/Target", _wc.getFrameContents("blue").getTitle());
    }


    /**
     * Test correct handling of a link within a frame that has been re-written by JavaScript over an image,
     * and which contains a <code>&lt;base ...&gt;</code> element.
     */
    public void testImageFrameRewrittenToUseBaseElement() throws Exception {
        redefineFrames("/image.gif", "frameRewriter.html");
        _wc.getResponse(getHostPath() + "/Frames.html");
        WebLink link = _wc.getFrameContents("red").getLinks()[0];
        link.click();
        assertEquals("Content of blue frame after clicking menu link", "Menu/Page/Target", _wc.getFrameContents("blue").getTitle());
    }


    /**
     * Verifies that a javascript URL which triggers refresh to the parent of a frame resolves with something sensible.
     */
    public void testJavaScriptURLToParentFrame() throws Exception {
        defineResource( "Frames.html",
                        "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                        "<FRAMESET cols=\"20%,80%\">" +
                        "    <FRAME src='Linker.html' name='red'>" +
                        "    <FRAME src=Form name=blue>" +
                        "</FRAMESET></HTML>" );
        defineWebPage( "Linker",  "This is a trivial page with <a href='javascript:window.parent.location.replace( \"Target\" );'>one link</a>" );
        defineResource( "Form",    "This is a page with nothing we care about");
        defineResource( "Target",  "You made it!", "text/plain" );

        _wc.getResponse( getHostPath() + "/Frames.html" );
        WebResponse result = _wc.getFrameContents( "red" ).getLinkWith( "one link" ).click();
        assertEquals( "Result of click", "You made it!", result.getText() );
    }

    /**
     * Verifies that when JavaScript overwrites an empty frame, other empty frames stay empty.
     */
    public void testJavaScriptOverwritingBlankFrame() throws Exception {
        defineResource("Frames.html",
                "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                "<FRAMESET>" +
                "    <FRAME src='Writer.html' name='red'>" +
                "    <FRAME name=green>" +
                "    <FRAME name=blue>" +
                "</FRAMESET></HTML>");
        defineWebPage("Writer", "This page overwrites an empty frame. <script language=\"JavaScript\">"
                + "  window.parent.green.document.open(\"text/html\")\n"
                + "  window.parent.green.document.write(\"Generated <a href=\\\"#\\\">link</a>\")\n"
                + "  window.parent.green.document.close()\n"
                + "</script>");

        _wc.getResponse(getHostPath() + "/Frames.html");
        assertEquals("Links in green frame", 1, _wc.getFrameContents("green").getLinks().length);
        assertEquals("Links in blue frame", 0, _wc.getFrameContents("blue").getLinks().length);
    }


    /**
     * Verifies that an open call from a subframe can specify another frame name.
     */
    public void testOpenIntoSubframe() throws Exception {
        defineResource( "Frames.html",
                        "<html><head><frameset>" +
                        "    <frame name='banner'>" +
                        "    <frame src='main.html' name='main'>" +
                        "</frameset></html>" );
        defineResource( "target.txt", "You made it!" );
        defineWebPage( "main", "<button id='button' onclick=\"window.open( 'target.txt', 'banner' )\">" );

        _wc.getResponse( getHostPath() + "/Frames.html" );
        ((Button) _wc.getFrameContents( "main" ).getElementWithID( "button" )).click();
        assertEquals( "Num open windows", 1, _wc.getOpenWindows().length );
        assertEquals( "New banner", "You made it!", _wc.getFrameContents( "banner" ).getText() );
        assertNotNull( "Original button no longer there",  _wc.getFrameContents( "main" ).getElementWithID( "button" ) );
    }



    /**
     * Verifies that an open call from a subframe can specify another frame name.
     */
    public void testSelfOpenFromSubframe() throws Exception {
        defineResource( "Frames.html",
                        "<html><head><frameset>" +
                        "    <frame name='banner' src='banner.html'>" +
                        "    <frame name='main'   src='main.html'>" +
                        "</frameset></html>" );
        defineResource( "target.txt", "You made it!" );
        defineWebPage( "main", "<button id='button2' onclick=\"window.open( 'target.txt', 'banner' )\">" );
        defineWebPage( "banner", "<button id='button1' onclick=\"window.open( 'target.txt', '_self' )\">" );

        _wc.getResponse( getHostPath() + "/Frames.html" );
        ((Button) _wc.getFrameContents( "banner" ).getElementWithID( "button1" )).click();
        assertEquals( "Num open windows", 1, _wc.getOpenWindows().length );
        assertEquals( "New banner", "You made it!", _wc.getFrameContents( "banner" ).getText() );
        assertNotNull( "Second frame no longer there",  _wc.getFrameContents( "main" ).getElementWithID( "button2" ) );
    }
    private WebConversation _wc;
}
