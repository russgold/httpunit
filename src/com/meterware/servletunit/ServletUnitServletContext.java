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

import java.net.URL;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.*;



/**
 * This class acts as a test environment for servlets.
 **/
public class ServletUnitServletContext implements ServletContext {

    /**
     * Returns a ServletContext object that corresponds to a specified URL on the server. 
     * <p>
     * This method allows servlets to gain access to the context for various parts of the server, and as needed obtain RequestDispatcher
     * objects from the context. The given path must be absolute (beginning with "/") and is interpreted based on the server's document root. 
     * <p>
     * In a security conscious environment, the servlet container may return null for a given URL.
     **/
    public javax.servlet.ServletContext getContext(java.lang.String A) {
        throw new RuntimeException( "getContext not implemented" );
    }


    /**
     * Returns the major version of the Java Servlet API that this servlet container supports. 
     * All implementations that comply with Version 2.2 must have this method return the integer 2.
     **/
    public int getMajorVersion() {
        return 2;
    }


    /**
     * Returns the minor version of the Servlet API that this servlet container supports. 
     * All implementations that comply with Version 2.2 must have this method return the integer 2.
     **/
    public int getMinorVersion() {
        return 2;
    }


    /**
     * Returns the MIME type of the specified file, or null if the MIME type is not known. 
     * The MIME type is determined by the configuration of the servlet container, and 
     * may be specified in a web application deployment descriptor. Common MIME types are
     * "text/html" and "image/gif".
     **/
    public java.lang.String getMimeType( String filePath ) {
        return null;  // XXX not implemented
    }


    /**
     * Returns a URL to the resource that is mapped to a specified path. The path must begin 
     * with a "/" and is interpreted as relative to the current context root. 
     * <p>
     * This method allows the servlet container to make a resource available to servlets from any source. 
     * Resources can be located on a local or remote file system, in a database, or in a .war file. 
     * <p>
     * The servlet container must implement the URL handlers and URLConnection objects that are necessary to access the resource. 
     * <p>
     * This method returns null if no resource is mapped to the pathname. 
     *
     * Some containers may allow writing to the URL returned by this method using the methods of the URL class. 
     *
     * The resource content is returned directly, so be aware that requesting a .jsp page returns the JSP source code. Use a
     * RequestDispatcher instead to include results of an execution. 
     *
     * This method has a different purpose than java.lang.Class.getResource, which looks up resources based on a class loader. This
     * method does not use class loaders.
     **/
    public java.net.URL getResource( String path ) {
        return null;  // XXX not implemented
    }


    /**
     * Returns the resource located at the named path as an InputStream object. 
     *
     * The data in the InputStream can be of any type or length. The path must be specified according to the rules given in getResource.
     * This method returns null if no resource exists at the specified path. 

     * Meta-information such as content length and content type that is available via getResource method is lost when using this method. 

     * The servlet container must implement the URL handlers and URLConnection objects necessary to access the resource. 

     * This method is different from java.lang.Class.getResourceAsStream, which uses a class loader. This method allows servlet
     * containers to make a resource available to a servlet from any location, without using a class loader.
     **/
    public java.io.InputStream getResourceAsStream( String path ) {
        return null;   // XXX not implemented
    }


    /**
     * Returns a RequestDispatcher object that acts as a wrapper for the resource located at the given path. A RequestDispatcher
     * object can be used to forward a request to the resource or to include the resource in a response. The resource can be dynamic or static.

     * The pathname must begin with a "/" and is interpreted as relative to the current context root. Use getContext to obtain a
     * RequestDispatcher for resources in foreign contexts. This method returns null if the ServletContext cannot return a
     * RequestDispatcher.
     **/
    public javax.servlet.RequestDispatcher getRequestDispatcher( String path ) {
        return null;   // XXX not implemented
    }


    /**
     * Returns a RequestDispatcher object that acts as a wrapper for the named servlet. 
     * 
     * Servlets (and JSP pages also) may be given names via server administration or via a web application deployment descriptor. A servlet
     * instance can determine its name using ServletConfig.getServletName(). 
     *
     * This method returns null if the ServletContext cannot return a RequestDispatcher for any reason.
     **/
    public javax.servlet.RequestDispatcher getNamedDispatcher(java.lang.String A) {
        return null;   // XXX not implemented
    }


    /**
     * @deprecated as of Servlet API 2.1
     **/
    public javax.servlet.Servlet getServlet(java.lang.String A) {
        return null;
    }


    /**
     * @deprecated as of Servlet API 2.0
     **/
    public java.util.Enumeration getServlets() {
        return EMPTY_VECTOR.elements();
    }


    /**
     * @deprecated as of Servlet API 2.1
     **/
    public java.util.Enumeration getServletNames() {
        return EMPTY_VECTOR.elements();
    }


    /**
     * Writes the specified message to a servlet log file, usually an event log. 
     * The name and type of the servlet log file is specific to the servlet container.
     **/
    public void log( String message ) {  // XXX change this to use something testable
    }


    /**
     * @deprecated use log( String, Throwable )
     **/
    public void log( Exception e, String message ) {
        log( message, e );
    }


    /**
     * Writes an explanatory message and a stack trace for a given Throwable exception to the servlet log file. 
     * The name and type of the servlet log file is specific to the servlet container, usually an event log.
     **/
    public void log( String message, Throwable t ) {
        log( message );
        log( "  " + t );
    }


    /**
     * Returns a String containing the real path for a given virtual path. For example, the virtual path "/index.html" has a real path of
     * whatever file on the server's filesystem would be served by a request for "/index.html". 
     *
     * The real path returned will be in a form appropriate to the computer and operating system on which the servlet container is running,
     * including the proper path separators. This method returns null if the servlet container cannot translate the virtual path to a real path for
     * any reason (such as when the content is being made available from a .war archive).
     **/
    public String getRealPath( String path ) {
        return null;  // XXX not implemented
    }


    /**
     * Returns the name and version of the servlet container on which the servlet is running. 

     * The form of the returned string is servername/versionnumber. For example, the JavaServer Web Development Kit may return the
     * string JavaServer Web Dev Kit/1.0. 

     * The servlet container may return other optional information after the primary string in parentheses, for example, JavaServer Web
     * Dev Kit/1.0 (JDK 1.1.6; Windows NT 4.0 x86).
     **/
    public String getServerInfo() {
        return "ServletUnit test framework";
    }


    /**
     * Returns a String containing the value of the named context-wide initialization parameter, or null if the parameter does not exist. 
     *
     * This method can make available configuration information useful to an entire "web application". For example, it can provide a
     * webmaster's email address or the name of a system that holds critical data.
     **/
    public java.lang.String getInitParameter( String name ) {
        return null;  // XXX not implemented
    }                                       


    /**
     * Returns the names of the context's initialization parameters as an Enumeration of String objects, 
     * or an empty Enumeration if the context has no initialization parameters.
     **/
    public java.util.Enumeration getInitParameterNames() {
        return EMPTY_VECTOR.elements();   // XXX not implemented
    }


    /**
     * Returns the servlet container attribute with the given name, or null if there is no attribute by that name. 
     * An attribute allows a servlet container to give the servlet additional information not already 
     * provided by this interface. See your server documentation for information
     * about its attributes. A list of supported attributes can be retrieved using getAttributeNames.
     **/ 
    public Object getAttribute( String name ) {
        return _attributes.get( name );
    }


    public Enumeration getAttributeNames() {
        return _attributes.keys();
    }

    public void setAttribute( String name, Object attribute ) {
        _attributes.put( name, attribute );
    }


    public void removeAttribute( String name ) {
        _attributes.remove( name );
    }


    private final static Vector EMPTY_VECTOR = new Vector();
    private Hashtable _attributes = new Hashtable();
}
