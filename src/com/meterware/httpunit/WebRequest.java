package com.meterware.httpunit;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * A request sent to a web server.
 **/
abstract
public class WebRequest {


    /**
     * Sets the value of a parameter in a web request.
     **/
    public void setParameter( String name, String value ) {
        _parameters.put( name, value );
    }


    /**
     * Returns the value of a parameter in this web request.
     * @return the value of the named parameter, or null if it is not set.
     **/
    public String getParameter( String name ) {
        return (String) _parameters.get( name );
    }


    /**
     * Removes a parameter from this web request.
     **/
    public void removeParameter( String name ) {
        _parameters.remove( name );
    }


    
    /**
     * Returns the final URL associated with this web request.
     **/
    abstract
    public URL getURL() throws MalformedURLException;



//------------------------------------- protected members ------------------------------------


    /**
     * Constructs a web request using an absolute URL string.
     **/
    protected WebRequest( String urlString ) {
        this( (URL) null, urlString );
    }
    

    /**
     * Constructs a web request using a base URL and a relative URL string.
     **/
    protected WebRequest( URL urlBase, String urlString ) {
        _urlBase   = urlBase;
        _urlString = urlString;
    }
    

    /**
     * Constructs a web request using a base request and a relative URL string.
     **/
    protected WebRequest( WebRequest baseRequest, String urlString ) throws MalformedURLException {
        this( baseRequest.getURL(), urlString );
    }
    

    /**
     * Performs any additional processing necessary to complete the request.
     **/
    protected void completeRequest( URLConnection connection ) throws IOException {
    }


    final
    protected URL getURLBase() {
        return _urlBase;
    }


    final
    protected String getURLString() {
        return _urlString;
    }


    final
    protected boolean hasNoParameters() {
        return _parameters.size() == 0;
    }


    final
    protected String getParameterString() {
        StringBuffer sb = new StringBuffer();
        for (Enumeration e = _parameters.keys(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
            String value = (String) _parameters.get( name );
            sb.append( name ).append( '=' );
            sb.append( URLEncoder.encode( value ) );
            if (e.hasMoreElements()) sb.append( '&' );
        }
        return sb.toString();
    }

    
//--------------------------------------- private members ------------------------------------

    private URL       _urlBase;
    private String    _urlString;
    private Hashtable _parameters = new Hashtable();


}