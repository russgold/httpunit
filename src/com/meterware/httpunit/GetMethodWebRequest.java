package com.meterware.httpunit;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * An HTTP request using the GET method.
 **/
public class GetMethodWebRequest extends WebRequest {

    
    /**
     * Constructs a web request using a specific absolute url string.
     **/
    public GetMethodWebRequest( String urlString ) {
        super( urlString );
    }


    /**
     * Constructs a web request using a base URL and a relative url string.
     **/
    public GetMethodWebRequest( URL urlBase, String urlString ) {
        super( urlBase, urlString );
    }


    public URL getURL() throws MalformedURLException {
        if (hasNoParameters()) {
            return new URL( getURLBase(), getURLString() );
        } else {
            return new URL( getURLBase(), getURLString() + "?" + getParameterString() );
        }
    }


}




