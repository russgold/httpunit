package com.meterware.servletunit;
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
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.meterware.httpunit.*;

/**
 * Tests the ServletUnitHttpRequest class.
 **/
public class HttpServletRequestTest extends ServletUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }


    public static Test suite() {
        return new TestSuite( HttpServletRequestTest.class );
    }


    public HttpServletRequestTest( String name ) {
        super( name );
    }


    public void testGetDefaultProperties() throws Exception {
        WebRequest     wr   = new GetMethodWebRequest( "http://localhost/simple" );
        HttpServletRequest request = new ServletUnitHttpRequest( wr, new ServletUnitContext(), new Hashtable(), NO_MESSAGE_BODY );
        assertNull( "Authorization incorrectly specified", request.getAuthType() );
        assertNull( "Character encoding incorrectly specified", request.getCharacterEncoding() );
        assertEquals( "Parameters unexpectedly specified", "", request.getQueryString() );
    }


    public void testSetSingleValuedParameter() throws Exception {
        WebRequest     wr   = new GetMethodWebRequest( "http://localhost/simple" );
        wr.setParameter( "age", "12" );
        wr.setParameter( "color", new String[] { "red", "blue" } );
        HttpServletRequest request = new ServletUnitHttpRequest( wr, new ServletUnitContext(), new Hashtable(), NO_MESSAGE_BODY );

        assertEquals( "age parameter", "12", request.getParameter( "age" ) );
        assertNull( "unset parameter should be null", request.getParameter( "unset" ) );
    }


    public void testSetMultiValuedParameter() throws Exception {
        WebRequest wr  = new GetMethodWebRequest( "http://localhost/simple" );
        wr.setParameter( "age", "12" );
        wr.setParameter( "color", new String[] { "red", "blue" } );
        HttpServletRequest request = new ServletUnitHttpRequest( wr, new ServletUnitContext(), new Hashtable(), NO_MESSAGE_BODY );

        assertMatchingSet( "age parameter", new String[] { "12" }, request.getParameterValues( "age" ) );
        assertMatchingSet( "color parameter", new String[] { "red", "blue" }, request.getParameterValues( "color" ) );
        assertNull( "unset parameter should be null", request.getParameterValues( "unset" ) );
    }


    public void testSetQueryString() throws Exception {
        WebRequest wr  = new GetMethodWebRequest( "http://localhost/simple" );
        wr.setParameter( "age", "12" );
        wr.setParameter( "color", new String[] { "red", "blue" } );
        HttpServletRequest request = new ServletUnitHttpRequest( wr, new ServletUnitContext(), new Hashtable(), NO_MESSAGE_BODY );

        assertEquals( "query string", "color=red&color=blue&age=12", request.getQueryString() );
    }


    public void testInlineSingleValuedParameter() throws Exception {
        WebRequest     wr   = new GetMethodWebRequest( "http://localhost/simple?color=red&color=blue&age=12" );
        HttpServletRequest request = new ServletUnitHttpRequest( wr, new ServletUnitContext(), new Hashtable(), NO_MESSAGE_BODY );

        assertEquals( "age parameter", "12", request.getParameter( "age" ) );
        assertNull( "unset parameter should be null", request.getParameter( "unset" ) );
    }


    public void testInlineMultiValuedParameter() throws Exception {
        WebRequest wr  = new GetMethodWebRequest( "http://localhost/simple?color=red&color=blue&age=12" );
        HttpServletRequest request = new ServletUnitHttpRequest( wr, new ServletUnitContext(), new Hashtable(), NO_MESSAGE_BODY );

        assertMatchingSet( "age parameter", new String[] { "12" }, request.getParameterValues( "age" ) );
        assertMatchingSet( "color parameter", new String[] { "red", "blue" }, request.getParameterValues( "color" ) );
        assertNull( "unset parameter should be null", request.getParameterValues( "unset" ) );
    }


    public void notestInlineQueryString() throws Exception {
        WebRequest wr  = new GetMethodWebRequest( "http://localhost/simple?color=red&color=blue&age=12" );
        HttpServletRequest request = new ServletUnitHttpRequest( wr, new ServletUnitContext(), new Hashtable(), NO_MESSAGE_BODY );

        assertEquals( "query string", "color=red&color=blue&age=12", request.getQueryString() );
    }


    public void testDefaultAttributes() throws Exception {
        WebRequest wr  = new GetMethodWebRequest( "http://localhost/simple" );
        HttpServletRequest request = new ServletUnitHttpRequest( wr, new ServletUnitContext(), new Hashtable(), NO_MESSAGE_BODY );

        assertNull( "attribute should not be defined yet", request.getAttribute( "unset" ) );
        assertTrue( "attribute enumeration should be empty", !request.getAttributeNames().hasMoreElements() );
    }


    public void testNonDefaultAttributes() throws Exception {
        WebRequest wr  = new GetMethodWebRequest( "http://localhost/simple" );
        HttpServletRequest request = new ServletUnitHttpRequest( wr, new ServletUnitContext(), new Hashtable(), NO_MESSAGE_BODY );
        Object value = new Integer(1);

        request.setAttribute( "one", value );

        assertEquals( "attribute one", value, request.getAttribute( "one" ) );

        Enumeration names = request.getAttributeNames();
        assertTrue( "attribute enumeration should not be empty", names.hasMoreElements() );
        assertEquals( "contents in enumeration", "one", names.nextElement() );
        assertTrue( "attribute enumeration should now be empty", !names.hasMoreElements() );
    }


    public void testDuplicateAttributes() throws Exception {
        WebRequest wr  = new GetMethodWebRequest( "http://localhost/simple" );
        HttpServletRequest request = new ServletUnitHttpRequest( wr, new ServletUnitContext(), new Hashtable(), NO_MESSAGE_BODY );
        Object value = new Integer(1);

        request.setAttribute( "one", value );

        try {
            request.setAttribute( "one", value );
            fail( "Did not complain about illegal state" );
        } catch (IllegalStateException e) {
        }
    }


    public void testSessionCreation() throws Exception {
        WebRequest wr  = new GetMethodWebRequest( "http://localhost/simple" );
        HttpServletRequest request = new ServletUnitHttpRequest( wr, new ServletUnitContext(), new Hashtable(), NO_MESSAGE_BODY );

        HttpSession session = request.getSession( /* create */ false );
        assertNull( "Unexpected session found", session );

        session = request.getSession();
        assertNotNull( "No session created", session );
        assertTrue( "Session not marked as new", session.isNew() );
    }


    public void testDefaultCookies() throws Exception {
        WebRequest wr  = new GetMethodWebRequest( "http://localhost/simple" );
        HttpServletRequest request = new ServletUnitHttpRequest( wr, new ServletUnitContext(), new Hashtable(), NO_MESSAGE_BODY );
        Cookie[] cookies = request.getCookies();
        assertNull( "Unexpected cookies found", cookies );
    }


    public void testSetCookies() throws Exception {
        WebRequest wr  = new GetMethodWebRequest( "http://localhost/simple" );
        ServletUnitHttpRequest request = new ServletUnitHttpRequest( wr, new ServletUnitContext(), new Hashtable(), NO_MESSAGE_BODY );
        request.addCookie( new Cookie( "flavor", "vanilla" ) );

        Cookie[] cookies = request.getCookies();
        assertNotNull( "No cookies found", cookies );
        assertEquals( "Num cookies found", 1, cookies.length );
        assertEquals( "Cookie name", "flavor", cookies[0].getName() );
        assertEquals( "Cookie value", "vanilla", cookies[0].getValue() );
    }


    public void testRetrieveSession() throws Exception {
        WebRequest wr  = new GetMethodWebRequest( "http://localhost/simple" );
        ServletUnitContext context = new ServletUnitContext();

        ServletUnitHttpRequest request = new ServletUnitHttpRequest( wr, context, new Hashtable(), NO_MESSAGE_BODY );
        request.addCookie( new Cookie( ServletUnitHttpSession.SESSION_COOKIE_NAME, context.newSession().getId() ) );

        HttpSession session = request.getSession( /* create */ false );
        assertNotNull( "No session created", session );
    }


    public void testGetRequestURI() throws Exception {
        ServletUnitContext context = new ServletUnitContext();
        WebRequest wr = new GetMethodWebRequest( "http://localhost/simple" );

        ServletUnitHttpRequest request = new ServletUnitHttpRequest( wr, context, new Hashtable(), NO_MESSAGE_BODY );
        assertEquals("/simple", request.getRequestURI());

        wr = new GetMethodWebRequest( "http://localhost/simple?foo=bar" );
        request = new ServletUnitHttpRequest( wr, context, new Hashtable(), NO_MESSAGE_BODY);
        assertEquals("/simple", request.getRequestURI());
    }


    private final static byte[] NO_MESSAGE_BODY = new byte[0];
}


