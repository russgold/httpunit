package com.meterware.httpunit;
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

import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;


/**
 * Verifies handling of URLs with odd features.
 *
 * @author <a href="mailto:ddkilzer@users.sourceforge.net">David D. Kilzer</a>
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 */
public class NormalizeURLTest extends HttpUnitTest {

    /*
      * Test various combinations of URLs with NO trailing slash (and no directory or file part)
      */

    @Test
    public void testHostnameNoSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name");
        assertEquals("URL", "http://host.name", request.getURL().toExternalForm());
    }


    @Test
    public void testHostnamePortNoSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name:80");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name:80");
    }


    @Test
    public void testUsernameHostnameNoSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username@host.name");
        assertEquals("URL", request.getURL().toExternalForm(), "http://username@host.name");
    }


    @Test
    public void testUsernamePasswordHostnameNoSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username:password@host.name");
        assertEquals("URL", request.getURL().toExternalForm(), "http://username:password@host.name");
    }


    @Test
    public void testUsernameHostnamePortNoSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username@host.name:80");
        assertEquals("URL", request.getURL().toExternalForm(), "http://username@host.name:80");
    }


    @Test
    public void testUsernamePasswordHostnamePortNoSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username:password@host.name:80");
        assertEquals("URL", request.getURL().toExternalForm(), "http://username:password@host.name:80");
    }


    /*
      * Test various combinations of URLs WITH trailing slash (and no directory or file part)
      */

    @Test
    public void testHostnameSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/");
    }


    @Test
    public void testHostnamePortSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name:80/");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name:80/");
    }


    @Test
    public void testUsernameHostnameSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username@host.name/");
        assertEquals("URL", request.getURL().toExternalForm(), "http://username@host.name/");
    }


    @Test
    public void testUsernamePasswordHostnameSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username:password@host.name/");
        assertEquals("URL", request.getURL().toExternalForm(), "http://username:password@host.name/");
    }


    @Test
    public void testUsernameHostnamePortSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username@host.name:80/");
        assertEquals("URL", request.getURL().toExternalForm(), "http://username@host.name:80/");
    }


    @Test
    public void testUsernamePasswordHostnamePortSlash() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://username:password@host.name:80/");
        assertEquals("URL", request.getURL().toExternalForm(), "http://username:password@host.name:80/");
    }


    /*
      * Test various combinations of normal URLs with 0 to 2 directories and a filename
      */

    @Test
    public void testHostnameFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/file.html");
    }


    @Test
    public void testHostnameDirectoryFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory/file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory/file.html");
    }


    @Test
    public void testHostnameDirectory1Directory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory1/directory2/file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory1/directory2/file.html");
    }


    /*
      * Test various combinations of normal URLs with directories requesting a default index page
      */

    @Test
    public void testHostnameDirectory() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory/");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory/");
    }


    @Test
    public void testHostnameDirectory1Directory2() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory1/directory2/");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory1/directory2/");
    }


    /*
      * Torture tests with URLs containing directory navigation ('.' and '..')
      */

    @Test
    public void testTortureHostnameDotFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/./file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/file.html");
    }


    @Test
    public void testTortureHostnameDotDirectoryFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/./directory/file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory/file.html");
    }


    @Test
    public void testTortureHostnameDotDirectoryDotFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/./directory/./file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory/file.html");
    }


    @Test
    public void testTortureHostnameDotDirectoryDotDotFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/./directory/../file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/file.html");
    }


    @Test
    public void testTortureHostnameDotDirectory1Directory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/./directory1/directory2/file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory1/directory2/file.html");
    }


    @Test
    public void testTortureHostnameDotDirectory1DotDirectory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/./directory1/./directory2/file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory1/directory2/file.html");
    }


    @Test
    public void testTortureHostnameDotDirectory1DotDirectory2DotFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/./directory1/./directory2/./file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory1/directory2/file.html");
    }


    @Test
    public void testTortureHostnameDirectory1Directory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory1/directory2/file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory1/directory2/file.html");
    }


    @Test
    public void testTortureHostnameDirectory1DotDotDirectory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory1/../directory2/file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory2/file.html");
    }


    @Test
    public void testTortureHostnameDirectory1DotDotDirectory2DotDotFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory1/../directory2/../file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/file.html");
    }


    @Test
    public void testTortureHostnameDirectory1Directory2DotDotDotDotFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory1/directory2/../../file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/file.html");
    }

    /**
     * patch by Serge Maslyukov
     *
     * @throws Exception
     */
    @Test
    public void testTripleDottedPath() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://en.wikipedia.org/wiki/...And_Found");
        assertEquals("URL", request.getURL().toExternalForm(), "http://en.wikipedia.org/wiki/...And_Found");
    }


    /*
     * Test relative URLs with directory navigation.
     */
    @Test
    public void testRelativePathDotDotFile() throws Exception {
        WebRequest request = new GetMethodWebRequest(new URL("http://host.name/directory1/file.html"), "../directory2/file.html");
        assertEquals("URL", "http://host.name/directory2/file.html", request.getURL().toExternalForm());
    }


    /*
      * Torture tests with URLs containing multiple slashes
      */

    @Test
    public void testHostnameSlash1File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name//file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/file.html");
    }


    @Test
    public void testHostnameSlash2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name///file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/file.html");
    }


    @Test
    public void testHostnameSlash3File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name////file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/file.html");
    }


    @Test
    public void testHostnameSlash1DirectoryFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory//file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory/file.html");
    }


    @Test
    public void testHostnameSlash2DirectoryFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory///file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory/file.html");
    }


    @Test
    public void testHostnameSlash3DirectoryFile() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name/directory////file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory/file.html");
    }


    @Test
    public void testHostnameSlash1Directory1Directory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name//directory1//directory2//file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory1/directory2/file.html");
    }


    @Test
    public void testHostnameSlash2Directory1Directory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name///directory1///directory2///file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory1/directory2/file.html");
    }


    @Test
    public void testHostnameSlash3Directory1Directory2File() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name////directory1////directory2////file.html");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory1/directory2/file.html");
    }


    @Test
    public void testHostnameSlash1Directory() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name//directory//");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory/");
    }


    @Test
    public void testHostnameSlash2Directory() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name///directory///");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory/");
    }


    @Test
    public void testHostnameSlash3Directory() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name////directory////");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory/");
    }


    @Test
    public void testHostnameSlash1Directory1Directory2() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name//directory1//directory2//");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory1/directory2/");
    }


    @Test
    public void testHostnameSlash2Directory1Directory2() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name///directory1///directory2///");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory1/directory2/");
    }


    @Test
    public void testHostnameSlash3Directory1Directory2() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host.name////directory1////directory2////");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host.name/directory1/directory2/");
    }


    @Test
    public void testPathElementLeadingDot() throws Exception {
        WebRequest request = new GetMethodWebRequest("http://host/context/.src/page");
        assertEquals("URL", request.getURL().toExternalForm(), "http://host/context/.src/page");
    }


    @Test
    public void testUrlAsParameter() throws Exception {
        String desiredUrl = "http://localhost:3333/composite/addobserver?url=http://localhost:8081/";
        WebRequest request = new GetMethodWebRequest(desiredUrl);
        assertEquals("URL", desiredUrl, request.getURL().toExternalForm());
    }


    @Test
    public void testSlashesInParameter() throws Exception {
        String desiredUrl = "http://localhost:8888/bug2295681/TestServlet?abc=abc&aaa=%%%&bbb=---%2d%2F%*%aa&ccc=yahoo@yahoo.com&ddd=aaa/../../&eee=/.";
        WebRequest request = new GetMethodWebRequest(desiredUrl);
        assertEquals("URL", desiredUrl, request.getURL().toExternalForm());
    }


}
