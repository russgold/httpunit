package com.meterware.servletunit;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2001, Russell Gold
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
import com.meterware.httpunit.HttpInternalErrorException;
import com.meterware.httpunit.HttpNotFoundException;

import java.io.IOException;
import java.io.PrintWriter;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * This class represents the information recorded about a single web application. It is usually extracted from web.xml.
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
class WebApplication {


    /**
     * Constructs a default application spec with no information.
     */
    WebApplication() {}


    /**
     * Constructs an application spec from an XML document.
     */
    WebApplication( Document document ) throws MalformedURLException,SAXException {
        registerServlets( document );
        NodeList nl = document.getElementsByTagName( "security-constraint" );
        for (int i = 0; i < nl.getLength(); i++) {
            _securityConstraints.add( new SecurityConstraintImpl( (Element) nl.item(i) ) );
        }

        extractLoginConfiguration( document );
    }

    /**
     * Registers a servlet class to be run.
     **/
    void registerServlet( String resourceName, String servletClassName ) {
        registerServlet( resourceName, new ServletConfiguration( servletClassName ) );
    }


    /**
     * Registers a servlet to be run.
     **/
    void registerServlet( String resourceName, ServletConfiguration servletConfiguration ) {
        _servlets.put( asResourceName( resourceName ), servletConfiguration );
    }


    Servlet getServlet( URL url ) throws ServletException {
        final ServletConfiguration configuration = getServletConfiguration( url );
        if (configuration == null) throw new HttpNotFoundException( url );

        try {
            Class servletClass = Class.forName( configuration.getClassName() );
            if (!Servlet.class.isAssignableFrom( servletClass )) throw new HttpInternalErrorException( url );

            Servlet servlet = (Servlet) servletClass.newInstance();    // XXX cache instances - by class?
            servlet.init( new ServletUnitServletConfig( servlet, configuration.getInitParams() ) );
            return servlet;
        } catch (ClassNotFoundException e) {
            throw new HttpNotFoundException( url, e );
        } catch (IllegalAccessException e) {
            throw new HttpInternalErrorException( url, e );
        } catch (InstantiationException e) {
            throw new HttpInternalErrorException( url, e );
        }
    }


    private ServletConfiguration getServletConfiguration(URL url) {
        String servletName = getServletName( getURLPath( url ) );
        if (servletName.endsWith( "j_security_check" )) {
            return SECURITY_CHECK_CONFIGURATION;
        } else {
            return (ServletConfiguration) _servlets.get( servletName );
        }
    }


    private String getURLPath( URL url ) {
        return url.getFile();
    }


    /**
     * Returns true if this application uses Basic Authentication.
     */
    boolean usesBasicAuthentication() {
        return _useBasicAuthentication;
    }


    /**
     * Returns true if this application uses form-based authentication.
     */
    boolean usesFormAuthentication() {
        return _useFormAuthentication;
    }


    String getAuthenticationRealm() {
        return _authenticationRealm;
    }


    URL getLoginURL() {
        return _loginURL;
    }


    URL getErrorURL() {
        return _errorURL;
    }


    /**
     * Returns true if the specified path may only be accesses by an authorized user.
     * @param urlPath the application-relative path of the URL
     */
    boolean requiresAuthorization( URL url ) {
        return getControllingConstraint( getURLPath( url ) ) != NULL_SECURITY_CONSTRAINT;
    }


    /**
     * Returns true of the specified role may access the desired URL path.
     */
    boolean roleMayAccess( String roleName, URL url ) {
        return getControllingConstraint( getURLPath( url ) ).hasRole( roleName );
    }


    private SecurityConstraint getControllingConstraint( String urlPath ) {
        for (Iterator i = _securityConstraints.iterator(); i.hasNext();) {
            SecurityConstraint sc = (SecurityConstraint) i.next();
            if (sc.controlsPath( urlPath )) return sc;
        }
        return NULL_SECURITY_CONSTRAINT;
    }


//------------------------------------------------ private members ---------------------------------------------

    private final static ServletConfiguration SECURITY_CHECK_CONFIGURATION = new ServletConfiguration( SecurityCheckServlet.class.getName() );

    /** A mapping of resource names to servlet class names. **/
    private Hashtable _servlets = new Hashtable();

    private ArrayList _securityConstraints = new ArrayList();

    private boolean _useBasicAuthentication;

    private boolean _useFormAuthentication;

    private String _authenticationRealm = "";

    private URL _loginURL;

    private URL _errorURL;


    final static private SecurityConstraint NULL_SECURITY_CONSTRAINT = new NullSecurityConstraint();



    private void extractLoginConfiguration( Document document ) throws MalformedURLException,SAXException {
        NodeList nl = document.getElementsByTagName( "login-config" );
        if (nl.getLength() == 1) {
            final Element loginConfigElement = (Element) nl.item(0);
            String authenticationMethod = getChildNodeValue( loginConfigElement, "auth-method", "BASIC" );
            _authenticationRealm = getChildNodeValue( loginConfigElement, "realm-name", "" );
            if (authenticationMethod.equalsIgnoreCase( "BASIC" )) {
                _useBasicAuthentication = true;
                if (_authenticationRealm.length() == 0) throw new SAXException( "No realm specified for BASIC Authorization" );
            } else if (authenticationMethod.equalsIgnoreCase( "FORM" )) {
                _useFormAuthentication = true;
                if (_authenticationRealm.length() == 0) throw new SAXException( "No realm specified for FORM Authorization" );
                _loginURL = new URL( "http", "localhost", getChildNodeValue( loginConfigElement, "form-login-page" ) );
                _errorURL = new URL( "http", "localhost", getChildNodeValue( loginConfigElement, "form-error-page" ) );
            }
        }
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
                     new ServletConfiguration( servletElement ) );
    }


    private void registerServlet( Dictionary mapping, Element servletElement ) throws SAXException {
        registerServlet( getChildNodeValue( servletElement, "url-pattern" ),
                         (ServletConfiguration) mapping.get( getChildNodeValue( servletElement, "servlet-name" ) ) );
    }


    private static String getChildNodeValue( Element root, String childNodeName ) throws SAXException {
        return getChildNodeValue( root, childNodeName, null );
    }


    private static String getChildNodeValue( Element root, String childNodeName, String defaultValue ) throws SAXException {
        NodeList nl = root.getElementsByTagName( childNodeName );
        if (nl.getLength() == 1) {
            return getTextValue( nl.item(0) );
        } else if (defaultValue == null) {
            throw new SAXException( "Node <" + root.getNodeName() + "> has no child named <" + childNodeName + ">" );
        } else {
            return defaultValue;
        }
    }


    private static String getTextValue( Node node ) throws SAXException {
        Node textNode = node.getFirstChild();
        if (textNode == null) throw new SAXException( "No value specified for <" + node.getNodeName() + "> node" );
        if (textNode.getNodeType() != Node.TEXT_NODE) throw new SAXException( "No text value found for <" + node.getNodeName() + "> node" );
        return textNode.getNodeValue();
    }


    private static boolean patternMatches( String urlPattern, String urlPath ) {
        return urlPattern.equals( urlPath );
    }


    private String asResourceName( String rawName ) {    // XXX remove me
        if (rawName.startsWith( "/" )) {
            return rawName;
        } else {
            return "/" + rawName;
        }
    }


    private String getServletName( String urlFile ) {
        if (urlFile.indexOf( '?' ) < 0) {
            return urlFile;
        } else {
            return urlFile.substring( 0, urlFile.indexOf( '?' ) );
        }
    }


//============================================= SecurityCheckServlet class =============================================

    static class SecurityCheckServlet extends HttpServlet {
        protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
            handleLogin( (ServletUnitHttpRequest) req, resp );
        }


        protected void doPost( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
            handleLogin( (ServletUnitHttpRequest) req, resp );
        }


        private void handleLogin( ServletUnitHttpRequest req, HttpServletResponse resp ) throws IOException {
            final String username = req.getParameter( "j_username" );
            final String password = req.getParameter( "j_password" );
            req.writeFormAuthentication( username, password );
            resp.sendRedirect( req.getOriginalURL().toExternalForm() );
        }

    }
//============================================= ServletConfiguration class =============================================


    static class ServletConfiguration {

        public ServletConfiguration( String className ) {
            _className = className;
        }


        ServletConfiguration( Element servletElement ) throws SAXException {
            this( getChildNodeValue( servletElement, "servlet-class" ) );
            final NodeList initParams = servletElement.getElementsByTagName( "init-param" );
            for (int i = initParams.getLength()-1; i >= 0; i--) {
                _initParams.put( getChildNodeValue( (Element) initParams.item(i), "param-name" ),
                                 getChildNodeValue( (Element) initParams.item(i), "param-value" ) );
            }
        }


        String getClassName() {
            return _className;
        }


        Hashtable getInitParams() {
            return _initParams;
        }


        private String    _className;
        private Hashtable _initParams = new Hashtable();
    }


//=================================== SecurityConstract interface and implementations ==================================


    interface SecurityConstraint {
        boolean controlsPath( String urlPath );
        boolean hasRole( String roleName );
    }


    static class NullSecurityConstraint implements SecurityConstraint {
        public boolean controlsPath( String urlPath ) { return false; }
        public boolean hasRole( String roleName ) { return true; }
    }


    static class SecurityConstraintImpl implements SecurityConstraint {
        SecurityConstraintImpl( Element root ) throws SAXException {
            final NodeList roleNames = root.getElementsByTagName( "role-name" );
            for (int i = 0; i < roleNames.getLength(); i++) _roles.add( getTextValue( (Element) roleNames.item(i) ) );

            final NodeList resources = root.getElementsByTagName( "web-resource-collection" );
            for (int i = 0; i < resources.getLength(); i++) _resources.add( new WebResourceCollection( (Element) resources.item(i) ) );
        }

        public boolean controlsPath( String urlPath ) {
            return getMatchingCollection( urlPath ) != null;
        }

        public boolean hasRole( String roleName ) {
            return _roles.contains( roleName );
        }

        private ArrayList _roles     = new ArrayList();
        private ArrayList _resources = new ArrayList();

        public WebResourceCollection getMatchingCollection( String urlPath ) {
            for (Iterator i = _resources.iterator(); i.hasNext();) {
                WebResourceCollection wrc = (WebResourceCollection) i.next();
                if (wrc.controlsPath( urlPath )) return wrc;
            }
            return null;
        }

        class WebResourceCollection {
            WebResourceCollection( Element root ) throws SAXException {
                final NodeList urlPatterns = root.getElementsByTagName( "url-pattern" );
                for (int i = 0; i < urlPatterns.getLength(); i++) _urlPatterns.add( getTextValue( (Element) urlPatterns.item(i) ) );
            }

            boolean controlsPath( String urlPath ) {
                for (Iterator i = _urlPatterns.iterator(); i.hasNext();) {
                    String pattern = (String) i.next();
                    if (patternMatches( pattern, urlPath )) return true;
                }
                return false;
            }

            private ArrayList _urlPatterns  = new ArrayList();
        }
    }

}
