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
     * Specifies the character set encoding for a resource.
     **/
    public void setCharacterSet( String name, String characterSet ) {
        WebResource resource = (WebResource) _resources.get( asResourceName( name ) );
        if (resource == null) {
            resource = new WebResource( "" );
            _resources.put( asResourceName( name ), resource );
        }
        resource.setCharacterSet( characterSet );
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

    /** The encoding used for HTTP headers. **/
    final private static String HEADER_ENCODING = "us-ascii";

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

        BufferedReader br = new BufferedReader( new InputStreamReader( socket.getInputStream(), HEADER_ENCODING ) );
        PrintWriter    pw = new PrintWriter( new OutputStreamWriter( socket.getOutputStream(), HEADER_ENCODING ) );

        socket.setSoTimeout( 1000 );
        socket.setTcpNoDelay( true );

        String commandLine = br.readLine();  // get the first line only
        StringTokenizer st = new StringTokenizer( commandLine );
        String command = st.nextToken();
        String uri     = st.nextToken();
        String protocol = st.nextToken();

        if (!command.equals( "GET" )) {
            sendResponse( pw, HttpURLConnection.HTTP_BAD_METHOD, "unsupported method: " + command );
        } else {
            WebResource resource = (WebResource) _resources.get( uri );
            if (resource == null) {
                sendResponse( pw, HttpURLConnection.HTTP_NOT_FOUND, "unable to find " + uri );
            } else {
                sendResponse( pw, HttpURLConnection.HTTP_OK, "OK" );
                sendLine( pw, "Content-type: " + resource.getContentType() + "; charset=" + resource.getCharacterSet() );
                String[] headers = resource.getHeaders();
                for (int i = 0; i < headers.length; i++) {
                    sendLine( pw, headers[i] );
                }
                sendLine( pw, "" );
                pw.flush();

                pw = new PrintWriter( new OutputStreamWriter( socket.getOutputStream(), resource.getCharacterSet() ) );
                sendText( pw, resource.getContents() );
            }
        }

        pw.close();
        socket.close();
    }


    private void sendResponse( PrintWriter pw, int responseCode, String responseText ) throws IOException {
        sendLine( pw, "HTTP/1.0 " + responseCode + ' ' + responseText );
    }


    private void sendLine( PrintWriter pw, String text ) throws IOException {
        sendText( pw, text );
        sendText( pw, CRLF );
    }


    private void sendText( PrintWriter pw, String text ) throws IOException {
        pw.write( text );
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

    final static String DEFAULT_CHARACTER_SET = "us-ascii";

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


    void setCharacterSet( String characterSet ) {
        _characterSet = characterSet;
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


    String getCharacterSet() {
        return _characterSet;
    }

    private String _contents;
    private String _contentType;
    private String _characterSet = DEFAULT_CHARACTER_SET;
    private Vector _headers = new Vector();
}



