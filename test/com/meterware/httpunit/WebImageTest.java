package com.meterware.httpunit;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2002-2007, Russell Gold
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
import junit.textui.TestRunner;


/**
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
public class WebImageTest extends HttpUnitTest {

    public static void main( String args[] ) {
        TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( WebImageTest.class );
    }


    public WebImageTest( String name ) {
        super( name );
    }


    public void testGetImages() throws Exception {
        defineResource( "SimplePage.html",
                        "<html><head><title>A Sample Page</title></head>\n" +
                        "<body><img src='sample.jpg'>\n" +
                        "<IMG SRC='another.png'>" +
                        " and <img src='onemore.gif' alt='one'>\n" +
                        "</body></html>\n" );
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/SimplePage.html" );
        WebResponse simplePage = wc.getResponse( request );
        assertEquals( "Number of images", 3, simplePage.getImages().length );
        assertEquals( "First image source", "sample.jpg", simplePage.getImages()[0].getSource() );

        WebImage image = simplePage.getImageWithAltText( "one" );
        assertNotNull( "No image found", image );
        assertEquals( "Selected image source", "onemore.gif", image.getSource() );
    }

    /**
     * test for bug report [ 1432236 ] Downloading gif images uses up sockets
     * by Sir Runcible Spoon
     * @throws Exception
     */
    public void testGetImageManyTimes() throws Exception {
    	// try this for different numbers of images
    	int testCounts[]={10,100
    			//,1000    // approx 2.5 secs
    			//,2000    // approx 15  secs
    			//,3000    // approx 47 secs
    			//,4000    // approx 90 secs
    			//,5000    // approx 126 secs
    			// ,10000   // approx 426 secs
    			//,100000  // let us know if you get this running ...
    			};
    	// try
    	for (int testIndex=0;testIndex<testCounts.length;testIndex++) {
    		int MANY_IMAGES_COUNT=testCounts[testIndex];    	
    		// System.out.println(""+(testIndex+1)+". test many images with "+MANY_IMAGES_COUNT+" image links");
      	String html="<html><head><title>A page with many images</title></head>\n" +
      	"<body>\n";
	    	for (int i=0;i<MANY_IMAGES_COUNT;i++) {
	    		html+="<img src='image"+i+".gif' alt='image#"+i+"'>\n";
	    	}
	    	html+="</body></html>\n";
	    	defineResource("manyImages"+testIndex+".html",html);
	      WebConversation wc = new WebConversation();
	      WebRequest request = new GetMethodWebRequest( getHostPath() + "/manyImages"+testIndex+".html");
	      WebResponse manyImagesPage = wc.getResponse( request );
	      assertEquals( "Number of images", MANY_IMAGES_COUNT, manyImagesPage.getImages().length );
	    	for (int i=0;i<MANY_IMAGES_COUNT;i++) {
	        assertEquals( "image source #"+i, "image"+i+".gif", manyImagesPage.getImages()[i].getSource() );    		
	    	} // for
    	} // for	
    }

    public void testFindImageAndLink() throws Exception {
        defineResource( "SimplePage.html",
                        "<html><head><title>A Sample Page</title></head>\n" +
                        "<body><img src='sample.jpg'>\n" +
                        "<a href='somewhere.htm'><IMG SRC='another.png'></a>" +
                        " and <img src='onemore.gif'>\n" +
                        "</body></html>\n" );
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/SimplePage.html" );
        WebResponse simplePage = wc.getResponse( request );
        assertNull( "Found bogus image with 'junk.png'", simplePage.getImageWithSource( "junk.png" ) );

        WebImage image = simplePage.getImageWithSource( "onemore.gif" );
        assertNotNull( "Did not find image with source 'onemore.gif'", image );
        WebLink link = image.getLink();
        assertNull( "Found bogus link for image 'onemore.gif'", link );

        image = simplePage.getImageWithSource( "another.png" );
        assertNotNull( "Did not find image with source 'another.png'", image );
        link = image.getLink();
        assertNotNull( "Did not find link for image 'another.png'", link );
        assertEquals( "Link URL", "somewhere.htm", link.getURLString() );
    }


    public void testImageRequest() throws Exception {
        defineResource( "grouped/SimplePage.html",
                        "<html><head><title>A Sample Page</title></head>\n" +
                        "<body><img name='this_one' src='sample.jpg'>\n" +
                        "<IMG SRC='another.png'>" +
                        " and <img src='onemore.gif' alt='one'>\n" +
                        "</body></html>\n" );
        WebConversation wc = new WebConversation();
        WebRequest request = new GetMethodWebRequest( getHostPath() + "/grouped/SimplePage.html" );
        WebResponse simplePage = wc.getResponse( request );
        WebRequest imageRequest = simplePage.getImageWithName( "this_one" ).getRequest();
        assertEquals( "Image URL", getHostPath() + "/grouped/sample.jpg", imageRequest.getURL().toExternalForm() );
    }


}
