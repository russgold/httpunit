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
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.HttpNotFoundException;
import com.meterware.httpunit.HttpInternalErrorException;

import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import org.apache.xerces.parsers.DOMParser;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;


/**
 * This class acts as a test environment for servlets.
 **/
public class ServletRunner {

    /**
     * Default constructor, which defines no servlets.
     */
    public ServletRunner() {
        _application = new WebApplication();
    }


    /**
     * Constructor which expects the full path to the web.xml for the application.
     **/
    public ServletRunner( String webXMLFileSpec ) throws IOException, SAXException {
        DOMParser parser = new DOMParser();
        parser.parse( webXMLFileSpec );
        _application = new WebApplication( parser.getDocument() );
    }


    /**
     * Constructor which expects an input stream containing the web.xml for the application.
     **/
    public ServletRunner( InputStream webXML ) throws IOException, SAXException {
        DOMParser parser = new DOMParser();
        parser.parse( new InputSource( webXML ) );
        _application = new WebApplication( parser.getDocument() );
    }


    /**
     * Registers a servlet class to be run.
     **/
    public void registerServlet( String resourceName, String servletClassName ) {
        _application.registerServlet( resourceName, servletClassName );
    }


    /**
     * Returns the response from the specified servlet.
     * @exception SAXException thrown if there is an error parsing the response
     **/
    public WebResponse getResponse( WebRequest request ) throws MalformedURLException, IOException, SAXException {
        return getClient().getResponse( request );
    }


    /**
     * Returns the response from the specified servlet using GET.
     * @exception SAXException thrown if there is an error parsing the response
     **/
    public WebResponse getResponse( String url ) throws MalformedURLException, IOException, SAXException {
        return getClient().getResponse( url );
    }


    /**
     * Creates and returns a new web client that communicates with this servlet runner.
     **/
    public ServletUnitClient newClient() {
        return ServletUnitClient.newClient( _factory );
    }


//-------------------------------------------- package methods ---------------------------------------------------------


    Servlet getServlet( URL url ) throws ServletException {
        return _application.getServlet( url );
    }


    ServletUnitContext getContext() {
        return _context;
    }


//---------------------------- private members ------------------------------------

    WebApplication    _application;

    private ServletUnitClient  _client;

    private ServletUnitContext _context = new ServletUnitContext();

    private InvocationContextFactory _factory = new InvocationContextFactory() {
        public InvocationContext newInvocation( WebRequest request, Cookie[] clientCookies, Dictionary clientHeaders ) throws IOException, MalformedURLException {
            return new InvocationContextImpl( ServletRunner.this, request, clientCookies, clientHeaders );
        }
    };


    private ServletUnitClient getClient() {
        if (_client == null) _client = newClient();
        return _client;
    }

}
