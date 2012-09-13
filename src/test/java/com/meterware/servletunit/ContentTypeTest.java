package com.meterware.servletunit;
/********************************************************************************************************************
 * $Id: ErrorTests.java 1081 2012-09-09 17:16:39Z russgold $
 *
 * Copyright (c) 2012, Russell Gold
 * Copyright (c) 2011, NeoMedia Technologies, Inc.. All Rights Reserved except see below.
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

import static org.junit.Assert.*;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebClient;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * Tests for BR 3301056 ServletUnit handling Content-Type incorrectly
 * by Kevin Hunter
 * @author wf
 *
 */
public class ContentTypeTest
{
    private ServletRunner runner;

    public ContentTypeTest()
    {
    }

    @Before
    public void setUp()
    {
        runner = new ServletRunner();
        runner.registerServlet("/test1", TestServlet1.class.getName());
        runner.registerServlet("/test2", TestServlet2.class.getName());
    }

    @After
    public void tearDown()
    {
        runner.shutDown();
    }

    /*
     * This test case demonstrates that ServletUnit incorrectly replaces the 
     * content type specified by the servlet with one of its own.
     * (Expected behavior would be that ServletUnit would not alter the
     * response coming back from the servlet under test.)
     */
    @Test
    public void testProvidedContentTypeOverwritten() throws Exception
    {
        WebClient client = runner.newClient();
        WebRequest request = new GetMethodWebRequest("http://localhost/test1");
        WebResponse response = client.getResponse(request);
        assertEquals(200, response.getResponseCode());
        assertEquals(7, response.getContentLength());
        assertEquals("<test/>", response.getText());
        assertEquals("text/xml", response.getHeaderField("Content-Type"));
        assertEquals("text/xml", response.getContentType());
    }

    /*
     * This servlet returns a simple XML document, with Content-Type set
     * to "text/xml"
     */
    public static class TestServlet1 extends HttpServlet
    {
        private static final long serialVersionUID = 5434730264615105319L;

        public TestServlet1()
        {
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
        {
            resp.setStatus(200);
            resp.addHeader("Content-Type", "text/xml");
            resp.setContentLength(7);
            resp.getWriter().write("<test/>");
        }
    }

    /*
     * This test case demonstrates that ServletUnit incorrectly provides
     * a content type when the servlet under test doesn't provide one.
     * (Expected behavior would be that ServletUnit would not alter the
     * response coming back from the servlet under test.)
     */
    @Test
    public void testContentProvidedWhenNoneSpecified() throws Exception
    {
        WebClient client = runner.newClient();
        WebRequest request = new GetMethodWebRequest("http://localhost/test2");
        WebResponse response = client.getResponse(request);
        assertEquals(200, response.getResponseCode());
        assertEquals(7, response.getContentLength());
        assertEquals("<test/>", response.getText());
        assertNull(response.getHeaderField("No-Such-Header"));
        // the BR proposes to have a default "null" content-type
        // assertNull(response.getHeaderField("Content-Type"));
        // unfortunately currently the default content type is "text/plain; charset=iso-8859-1"
        // whereas the HttpUnitOptions default Content Type is "text/html" ...
        // we'll check for the current state for the time being 2012-09-13
        assertEquals("text/plain; charset=iso-8859-1",response.getHeaderField("Content-Type"));
    }

    /*
     * This servlet returns a simple XML document, but omits the
     * Content-Type header.
     */
    public static class TestServlet2 extends HttpServlet
    {
        private static final long serialVersionUID = 5434730264615105319L;

        public TestServlet2()
        {
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
        {
            resp.setStatus(200);
            resp.setContentLength(7);
            resp.getWriter().write("<test/>");
        }
    }
}

