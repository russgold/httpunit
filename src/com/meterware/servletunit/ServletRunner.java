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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
     * @exception SAXException thrown if there is an error parsing the response
     **/
    public WebResponse getResponse( WebRequest request ) throws MalformedURLException, IOException, SAXException {
        return _client.getResponse( request );
    }


    /**
     * Creates and returns a new web client that communicates with this servlet runner.
     **/
    public ServletUnitClient newClient() {
        return new ServletUnitClient( this );
    }


//-------------------------------- package methods -------------------------------------


    Servlet getServlet( URL url ) {
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


    ServletUnitContext getContext() {
        return _context;
    }


//---------------------------- private members ------------------------------------


    /** A mapping of resource names to servlet class names. **/
    Hashtable _servlets = new Hashtable();

    ServletUnitClient  _client = new ServletUnitClient( this );
    ServletUnitContext _context = new ServletUnitContext();


    private String getServletName( String urlFile ) {
        if (urlFile.indexOf( '?' ) < 0) {
            return urlFile;
        } else {
            return urlFile.substring( 0, urlFile.indexOf( '?' ) );
        }
    }


    private String asResourceName( String rawName ) {
        if (rawName.startsWith( "/" )) {
            return rawName;
        } else {
            return "/" + rawName;
        }
    }


}
