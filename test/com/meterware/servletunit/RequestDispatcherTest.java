package com.meterware.servletunit;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;


public class RequestDispatcherTest extends TestCase {

    final String outerServletName = "something/interesting";
    final String innerServletName = "something/more";

    final static String REQUEST_URI  = "javax.servlet.include.request_uri";
    final static String CONTEXT_PATH = "javax.servlet.include.context_path";
    final static String SERVLET_PATH = "javax.servlet.include.servlet_path";
    final static String PATH_INFO    = "javax.servlet.include.path_info";
    final static String QUERY_STRING = "javax.servlet.include.query_string";

    private ServletRunner _runner;


    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }


    public static Test suite() {
        return new TestSuite( RequestDispatcherTest.class );
    }


    public RequestDispatcherTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
        WebXMLString wxs = new WebXMLString();
        wxs.addServlet( outerServletName, RequestDispatcherServlet.class );
        wxs.addServlet( innerServletName, IncludedServlet.class );
        _runner = new ServletRunner( wxs.asInputStream(), "/sample");
    }


    public void testRequestDispatcherParameters() throws Exception {
        InvocationContext ic = _runner.newClient().newInvocation( "http://localhost/sample/" + outerServletName + "?param=original&param1=first" );

        final HttpServletRequest request = ic.getRequest();
        RequestDispatcherServlet servlet = (RequestDispatcherServlet) ic.getServlet();
        RequestDispatcher rd = servlet.getServletContext().getRequestDispatcher( "/" + innerServletName + "?param=revised&param2=new" );

        assertEquals( "param", "original", request.getParameter( "param" ) );
        assertEquals( "param1", "first", request.getParameter( "param1" ) );
        assertNull( "param2 should not be defined", request.getParameter( "param2" ) );

        ic.pushIncludedContext( rd );

        assertEquals( "param in included servlet", "revised", request.getParameter( "param" ) );
        assertEquals( "param1 in included servlet", "first", request.getParameter( "param1" ) );
        assertEquals( "param2 in included servlet", "new", request.getParameter( "param2" ) );

        ic.popContext();

        assertEquals( "reverted param", "original", request.getParameter( "param" ) );
        assertEquals( "reverted param1", "first", request.getParameter( "param1" ) );
        assertNull( "reverted param2 should not be defined", request.getParameter( "param2" ) );
    }


    public void notestRequestDispatcherIncludePaths() throws Exception {
        InvocationContext ic = _runner.newClient().newInvocation( "http://localhost/sample/" + outerServletName + "?param=original&param1=first" );

        final HttpServletRequest request = ic.getRequest();
        RequestDispatcherServlet servlet = (RequestDispatcherServlet) ic.getServlet();
        RequestDispatcher rd = servlet.getServletContext().getRequestDispatcher( "/" + innerServletName + "?param=revised&param2=new" );

        assertEquals( "request URI", "/sample/" + outerServletName, request.getRequestURI() );
        assertEquals( "context path attribute", "/sample", request.getContextPath() );
        assertEquals( "servlet path attribute", "/" + outerServletName, request.getServletPath() );
        assertNull( "path info not null attribute", request.getPathInfo() );
//        assertEquals( "query string attribute", "param=original&param1=first", request.getQueryString() ); TODO make this work

        ic.pushIncludedContext( rd );

        assertEquals( "request URI", "/sample/" + outerServletName, request.getRequestURI() );
        assertEquals( "context path attribute", "/sample", request.getContextPath() );
        assertEquals( "servlet path attribute", "/" + outerServletName, request.getServletPath() );
        assertNull( "path info not null attribute", request.getPathInfo() );
//        assertEquals( "query string attribute", "param=original&param1=first", request.getQueryString() );

        assertEquals( "request URI attribute", "/sample/" + innerServletName, request.getAttribute( REQUEST_URI ) );
        assertEquals( "context path attribute", "/sample", request.getAttribute( CONTEXT_PATH ) );
        assertEquals( "servlet path attribute", "/" + innerServletName, request.getAttribute( SERVLET_PATH ) );
        assertNull( "path info attribute not null", request.getAttribute( PATH_INFO ) );
//        assertEquals( "query string attribute", "param=revised&param2=new", request.getAttribute( QUERY_STRING ) );

        ic.popContext();

        assertNull( "reverted URI attribute not null", request.getAttribute( REQUEST_URI ) );
        assertNull( "context path attribute not null", request.getAttribute( CONTEXT_PATH ) );
        assertNull( "servlet path attribute not null", request.getAttribute( SERVLET_PATH ) );
        assertNull( "path info attribute not null", request.getAttribute( PATH_INFO ) );
//        assertNull( "query string attribute not null", "param=revised&param2=new", request.getAttribute( QUERY_STRING ) );
    }


    public void notestInitialAttributes() throws Exception {

        WebRequest request = new GetMethodWebRequest( "http://localhost/" + outerServletName );
        InvocationContext ic = _runner.newClient().newInvocation( request );
        HttpServletRequest servletRequest = ic.getRequest();
        servletRequest.setAttribute( REQUEST_URI, "r" );
        servletRequest.setAttribute( QUERY_STRING, "q" );
        servletRequest.setAttribute( SERVLET_PATH, "s" );
        Servlet servlet = ic.getServlet();
        servlet.service( ic.getRequest(), ic.getResponse() );
        WebResponse response = ic.getServletResponse();

        assertEquals( "Request URI", "r", servletRequest.getAttribute( REQUEST_URI ) );
        assertEquals( "Query String", "q", servletRequest.getAttribute( QUERY_STRING ) );
        assertEquals( "ServletPath", "s", servletRequest.getAttribute( SERVLET_PATH ) );
        assertNotNull( "No response received", response );
        assertEquals( "Response Code", 200, response.getResponseCode() );
        assertEquals( "content type", "text/plain", response.getContentType() );
        assertEquals( "requested resource", IncludedServlet.DESIRED_OUTPUT, response.getText() );
    }


    static class RequestDispatcherServlet extends HttpServlet {

        public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher( "/subdir/pagename.jsp?param=value&param2=value" );
            dispatcher.forward( request, response );
        }
    }


    static class IncludedServlet extends HttpServlet {

        static String DESIRED_REQUEST_URI = "localhost/subdir/pagename.jsp";
        static String DESIRED_SERVLET_PATH = "/subdir/pagename.jsp";
        static String DESIRED_QUERY_STRING = "param=value&param2=value";
        static String DESIRED_OUTPUT = DESIRED_REQUEST_URI + DESIRED_QUERY_STRING + DESIRED_SERVLET_PATH;


        public void service( HttpServletRequest request, HttpServletResponse response ) throws IOException {
            response.setContentType( "text/plain" );
            String requestUri = (String) request.getAttribute( REQUEST_URI );
            String queryString = (String) request.getAttribute( QUERY_STRING );
            String servletPath = (String) request.getAttribute( SERVLET_PATH );
            PrintWriter pw = response.getWriter();
            pw.write( blankIfNull( requestUri ) );
            pw.write( blankIfNull( queryString ) );
            pw.write( blankIfNull( servletPath ) );
            pw.close();
        }

        private String blankIfNull( String s ) {
            return s == null ? "" : s;
        }
    }
}
