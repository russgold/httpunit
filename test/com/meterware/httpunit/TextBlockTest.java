package com.meterware.httpunit;
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
import junit.framework.TestSuite;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class TextBlockTest extends HttpUnitTest {

    public static void main( String args[] ) {
        junit.textui.TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( TextBlockTest.class );
    }


    public TextBlockTest( String name ) {
        super( name );
    }


    public void testParagraphDetection() throws Exception {
        defineWebPage( "SimplePage",
                        "<p>This has no forms or links since we don't care " +
                        "about them</p>" +
                        "<p class='comment'>But it does have three paragraphs</p>\n" +
                        "<p>Which is what we want to find</p>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/SimplePage.html" );
        assertEquals( "Number of paragraphs", 3, response.getTextBlocks().length );
        assertEquals( "First paragraph", "This has no forms or links since we don't care about them", response.getTextBlocks()[0].getText() );
        BlockElement comment = response.getFirstMatchingTextBlock( BlockElement.MATCH_CLASS, "comment" );
        assertNotNull( "Did not find a comment paragraph", comment );
        assertEquals( "Comment paragraph", "But it does have three paragraphs", comment.getText() );
    }


    public void testHeaderDetection() throws Exception {
        defineWebPage( "SimplePage",
                        "<h1>Here is a section</h1>\n" +
                        "with some text" +
                        "<h2>A subsection</h2>" +
                        "<p>Some more text</p>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/SimplePage.html" );
        BlockElement header1 = response.getFirstMatchingTextBlock( BlockElement.MATCH_TAG, "H1" );
        assertNotNull( "Did not find the H1 header", header1 );
        assertEquals( "H1 header", "Here is a section", header1.getText() );
        BlockElement header2 = response.getFirstMatchingTextBlock( BlockElement.MATCH_TAG, "h2" );
        assertNotNull( "Did not find the h2 header", header2 );
        assertEquals( "H2 header", "A subsection", header2.getText() );
        assertEquals( "Text under header 1", "with some text", response.getTextBlocks()[ getBlockIndex( response, header1 )+1 ].getText() );
    }


    private static int getBlockIndex( WebResponse response, BlockElement header1 ) throws SAXException {
        int index = -1;
        BlockElement[] blocks = response.getTextBlocks();
        for (int i = 0; i < blocks.length; i++) {
            BlockElement block = blocks[i];
            if (block == header1) {
                index = i;
                break;
            }
        }
        return index;
    }

}
