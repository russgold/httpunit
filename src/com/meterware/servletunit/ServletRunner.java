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
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Dictionary;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.meterware.httpunit.*;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * This class acts as a test environment for servlets.
 **/
public class ServletRunner {

    /**
     * Default constructor, which defines no servlets.
     */
    public ServletRunner() {}


    /**
     * Constructor which expects the full path to the web.xml for the application.
     **/
    public ServletRunner( String webXMLFileSpec ) throws IOException, SAXException {
        DOMParser parser = new DOMParser();
        parser.parse( webXMLFileSpec );

        registerServlets( parser.getDocument() );
    }


    /**
     * Constructor which expects an input stream containing the web.xml for the application.
     **/
    public ServletRunner( InputStream webXML ) throws IOException, SAXException {
        DOMParser parser = new DOMParser();
        parser.parse( new InputSource( webXML ) );

        registerServlets( parser.getDocument() );
    }


    private void registerServlets( Document document ) throws SAXException {
        Hashtable nameToClass = new Hashtable();
        NodeList nl = document.getElementsByTagName( "servlet" );
        for (int i = 0; i < nl.getLength(); i++) registerServletClass( nameToClass, (Element) nl.item(i) );
        nl = document.getElementsByTagName( "servlet-mapping" );
        for (int i = 0; i < nl.getLength(); i++) registerServlet( nameToClass, (Element) nl.item(i) );
    }


    private void registerServletClass( Dictionary mapping, Element servletElement ) throws SAXException {
        mapping.put( getChildNodeValue( servletElement, "servlet-name" ),
                     getChildNodeValue( servletElement, "servlet-class" ) );
    }


    private void registerServlet( Dictionary mapping, Element servletElement ) throws SAXException {
        registerServlet( getChildNodeValue( servletElement, "url-pattern" ),
                         (String) mapping.get( getChildNodeValue( servletElement, "servlet-name" ) ) );
    }


    private String getChildNodeValue( Element root, String childNodeName ) throws SAXException {
        NodeList nl = root.getElementsByTagName( childNodeName );
        if (nl.getLength() != 1) throw new SAXException( "Node <" + root.getNodeName() + "> has no child named <" + childNodeName + ">" );
        Node childNode = nl.item(0).getFirstChild();
        if (childNode == null) throw new SAXException( "No value specified for <" + childNodeName + "> node" );
        if (childNode.getNodeType() != Node.TEXT_NODE) throw new SAXException( "No text value found for <" + childNodeName + "> node" );
        return childNode.getNodeValue();
    }

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
        if (className == null) throw new HttpNotFoundException( url );

        try {
            Class servletClass = Class.forName( className );
            if (!Servlet.class.isAssignableFrom( servletClass )) {
                throw new HttpInternalErrorException( url );
            }
            return (Servlet) servletClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new HttpNotFoundException( url, e );
        } catch (IllegalAccessException e) {
            throw new HttpInternalErrorException( url, e );
        } catch (InstantiationException e) {
            throw new HttpInternalErrorException( url, e );
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
