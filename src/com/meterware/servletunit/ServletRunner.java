package com.meterware.servletunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2000, Russell Gold
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

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import com.meterware.httpunit.*;

import org.xml.sax.SAXException;


/**
 * This class acts as a test environment for servlets.
 **/
public class ServletRunner {


    /**
     * Registers a servlet class to be run.
     **/
    public void registerServlet( String resourceName, String servletClassName ) {
        _servlets.put( asResourceName( resourceName ), servletClassName );
    }


    /**
     * Returns the response from the specified servlet.
     **/
    public WebResponse getResponse( WebRequest request ) throws MalformedURLException, IOException, SAXException {
        return _client.getResponse( request );
    }


    /**
     * Creates and returns a new web client that communicates with this servlet runner.
     **/
    public WebClient newClient() {
        return new ServletUnitClient( this );
    }


    private Servlet getServlet( URL url ) {
        String className = (String) _servlets.get( getServletName( url.getFile() ) );
        if (className == null) throw new HttpNotFoundException( url.toExternalForm() );

        try {
            Class servletClass = Class.forName( className );
            if (!Servlet.class.isAssignableFrom( servletClass )) {
                throw new HttpInternalErrorException( url.toExternalForm() );
            }
            return (Servlet) servletClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new HttpNotFoundException( url.toExternalForm() );
        } catch (IllegalAccessException e) {
            throw new HttpInternalErrorException( url.toExternalForm() );
        } catch (InstantiationException e) {
            throw new HttpInternalErrorException( url.toExternalForm() );
        }
    }


    private String getServletName( String urlFile ) {
        if (urlFile.indexOf( '?' ) < 0) {
            return urlFile;
        } else {
            return urlFile.substring( 0, urlFile.indexOf( '?' ) );
        }
    }


    ServletUnitHttpResponse getServletResponse( WebRequest request, Cookie[] cookies ) throws MalformedURLException, IOException {
        ServletUnitHttpRequest  servletRequest  = new ServletUnitHttpRequest( request, _context );
        ServletUnitHttpResponse servletResponse = new ServletUnitHttpResponse();
        for (int i = 0; i < cookies.length; i++) servletRequest.addCookie( cookies[i] );

        try {
            HttpSession session = servletRequest.getSession( /* create */ false );
            if (session != null) ((ServletUnitHttpSession) session).access();

            Servlet servlet = getServlet( request.getURL() );
            servlet.init( new ServletUnitServletConfig( servlet ) );
            servlet.service( servletRequest, servletResponse );
            session = servletRequest.getSession( /* create */ false );
            if (session != null && session.isNew()) {
                servletResponse.addCookie( new Cookie( ServletUnitHttpSession.SESSION_COOKIE_NAME, session.getId() ) );
            }
        } catch (ServletException e) {
            throw new HttpInternalErrorException( request.getURL().toExternalForm() );
        }
        return servletResponse;
    }


//-------------------------------------- private members --------------------------------------------


    /** A mapping of resource names to servlet class names. **/
    Hashtable _servlets = new Hashtable();


    private String asResourceName( String rawName ) {
        if (rawName.startsWith( "/" )) {
            return rawName;
        } else {
            return "/" + rawName;
        }
    }


    ServletUnitClient  _client = new ServletUnitClient( this );
    ServletUnitContext _context = new ServletUnitContext();



}


class ServletUnitClient extends WebClient {


    ServletUnitClient( ServletRunner runner ) {
        _runner = runner;
    }


    /**
     * Creates a web response object which represents the response to the specified web request.
     **/
    protected WebResponse newResponse( WebRequest request ) throws MalformedURLException,IOException {
        return new ServletUnitWebResponse( request.getTarget(), request.getURL(), _runner.getServletResponse( request, getCookies() ) );
    }


    private Cookie[] getCookies() {
        String cookieHeader = (String) getHeaderFields().get( "Cookie" );
        if (cookieHeader == null) return NO_COOKIES;
        Vector cookies = new Vector();

        StringTokenizer st = new StringTokenizer( cookieHeader, "=;" );
        while (st.hasMoreTokens()) {
            String name = st.nextToken();
            if (st.hasMoreTokens()) {
                String value = st.nextToken();
                cookies.addElement( new Cookie( name, value ) );
            }
        }
        Cookie[] results = new Cookie[ cookies.size() ];
        cookies.copyInto( results );
        return results;
    }


    private ServletRunner _runner;

    final private static Cookie[] NO_COOKIES = new Cookie[0]; 
}
