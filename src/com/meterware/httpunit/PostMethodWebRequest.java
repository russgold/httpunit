package com.meterware.httpunit;

import java.io.DataOutputStream;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * An HTTP request using the POST method.
 **/
public class PostMethodWebRequest extends WebRequest {


    /**
     * Constructs a web request using a specific absolute url string.
     **/
    public PostMethodWebRequest( String urlString ) {
        super( urlString );
    }


//---------------------------------- WebRequest methods --------------------------------


    public URL getURL() throws MalformedURLException {
        return new URL( getURLBase(), getURLString() );
    }


    protected void completeRequest( URLConnection connection ) throws IOException {
        connection.setDoInput( true );
        connection.setDoOutput( true );
        DataOutputStream printout = new DataOutputStream( connection.getOutputStream() );
        printout.writeBytes( getParameterString() );
        printout.flush();
        printout.close();
    }

//----------------------------------- package members -----------------------------------


    /**
     * Constructs a web request using a base URL and a relative url string.
     **/
    PostMethodWebRequest( URL urlBase, String urlString ) {
        super( urlBase, urlString );
    }


}
