package com.meterware.httpunit;
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
import java.net.URL;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * A basic simulated web-server for testing the HttpUnit library.
 **/
public class PseudoServer {


    PseudoServer() {
        Thread t = new Thread() {
            public void run() {
                while (_active) {
                    try {
                        handleConnection();
                        Thread.sleep( 50 );
                    } catch (IOException e) {
                        System.out.println( "Error in pseudo server: " + e );
                    } catch (InterruptedException e) {
                        System.out.println( "Interrupted. Shutting down" );
                        _active = false;
                    }
                }
            }
        };
        t.start();
    }


    public void shutDown() {
        _active = false;
    }


    /**
     * Returns the port on which this server is listening.
     **/
    public int getConnectedPort() throws IOException {
        return getServerSocket().getLocalPort();
    }


    /**
     * Defines the contents of an expected resource.
     **/
    public void setResource( String name, String value ) {
        setResource( name, value, "text/html" );
    }


    /**
     * Defines the contents of an expected resource.
     **/
    public void setResource( String name, String value, String contentType ) {
        _resources.put( asResourceName( name ), new WebResource( value, contentType ) );
    }


    /**
     * Adds a header to a defined resource.
     **/
    public void addResourceHeader( String name, String header ) {
        WebResource resource = (WebResource) _resources.get( asResourceName( name ) );
        if (resource == null) {
            resource = new WebResource( "" );
            _resources.put( asResourceName( name ), resource );
        }
        resource.addHeader( header );
    }


//------------------------------------- private members ---------------------------------------


    private Hashtable _resources = new Hashtable();

    private boolean _active = true;


    final private static String CRLF = "\r\n";


    private String asResourceName( String rawName ) {
        if (rawName.startsWith( "/" )) {
            return rawName;
        } else {
            return "/" + rawName;
        }
    }


    private void handleConnection() throws IOException {
        Socket socket = getServerSocket().accept();

        BufferedReader br = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
        PrintStream ps = new PrintStream( socket.getOutputStream() );
        socket.setSoTimeout( 1000 );
        socket.setTcpNoDelay( true );

        String commandLine = br.readLine();  // get the first line only
        StringTokenizer st = new StringTokenizer( commandLine );
        String command = st.nextToken();
        String uri     = st.nextToken();
        String protocol = st.nextToken();

        if (!command.equals( "GET" )) {
            sendResponse( ps, HttpURLConnection.HTTP_BAD_METHOD, "unsupported method: " + command );
        } else {
            WebResource resource = (WebResource) _resources.get( uri );
            if (resource == null) {
                sendResponse( ps, HttpURLConnection.HTTP_NOT_FOUND, "unable to find " + uri );
            } else {
                sendResponse( ps, HttpURLConnection.HTTP_OK, "OK" );
                sendLine( ps, "Content-type: " + resource.getContentType() );
                String[] headers = resource.getHeaders();
                for (int i = 0; i < headers.length; i++) {
                    sendLine( ps, headers[i] );
                }
                sendLine( ps, "" );
                sendText( ps, resource.getContents() );
                ps.close();
            }
        }

        socket.close();
    }


    private void sendResponse( PrintStream ps, int responseCode, String responseText ) throws IOException {
        sendLine( ps, "HTTP/1.0 " + responseCode + ' ' + responseText );
    }


    private void sendLine( PrintStream ps, String text ) throws IOException {
        sendText( ps, text );
        sendText( ps, CRLF );
    }


    private void sendText( PrintStream ps, String text ) throws IOException {
        ps.write( text.getBytes() );
    }



    private ServerSocket getServerSocket() throws IOException {
        synchronized (this) {
            if (_serverSocket == null) _serverSocket = new ServerSocket(0);
        }
        return _serverSocket;
    }


    private ServerSocket _serverSocket;

}


class WebResource {


    final static String DEFAULT_CONTENT_TYPE = "text/html";

    WebResource( String contents ) {
        this( contents, DEFAULT_CONTENT_TYPE );
    }


    WebResource( String contents, String contentType ) {
        _contents    = contents;
        _contentType = contentType;
    }


    void addHeader( String header ) {
        _headers.addElement( header );
    }


    String[] getHeaders() {
        String[] headers = new String[ _headers.size() ];
        _headers.copyInto( headers );
        return headers;
    }


    String getContents() {
        return _contents;
    }


    String getContentType() {
        return _contentType;
    }

    private String _contents;
    private String _contentType;
    private Vector _headers = new Vector();
}



