package com.meterware.httpunit;
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
                    } catch (InterruptedIOException e) {
                        _active = false;
                    } catch (IOException e) {
                        System.out.println( "Error in pseudo server: " + e );
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        System.out.println( "Interrupted. Shutting down" );
                        _active = false;
                    }
                }
		try {
                    if (_serverSocket != null) _serverSocket.close();
                    _serverSocket = null;
                } catch (IOException e) {
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
    public void setResource( String name, PseudoServlet servlet ) {
        _resources.put( asResourceName( name ), servlet );
    }


    /**
     * Defines the contents of an expected resource.
     **/
    public void setResource( String name, String value, String contentType ) {
        _resources.put( asResourceName( name ), new WebResource( value, contentType ) );
    }


    /**
     * Defines a resource which will result in an error message.
     **/
    public void setErrorResource( String name, int errorCode, String errorMessage ) {
        _resources.put( asResourceName( name ), new WebResource( errorCode, errorMessage ) );
    }


    /**
     * Enables the sending of the character set in the content-type header.
     **/
    public void setSendCharacterSet( String name, boolean enabled ) {
        WebResource resource = (WebResource) _resources.get( asResourceName( name ) );
        if (resource == null) throw new IllegalArgumentException( "No defined resource " + name );
        resource.setSendCharacterSet( enabled );
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

        if (!command.equals( "GET" ) && !command.equals( "POST" )) {
            sendResponse( pw, HttpURLConnection.HTTP_BAD_METHOD, "unsupported method: " + command );
        } else {
            try {
                WebResource resource = getResource( command, uri, br );
                if (resource == null) {
                    sendResponse( pw, HttpURLConnection.HTTP_NOT_FOUND, "unable to find " + uri );
                } else if (resource.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    sendResponse( pw, resource.getResponseCode(), resource.getContents() );
                    sendLine( pw, "" );
	            pw.flush();
		} else {
                    sendResponse( pw, HttpURLConnection.HTTP_OK, "OK" );
                    sendLine( pw, "Content-type: " + resource.getContentType() + resource.getCharacterSetParameter() );
                    String[] headers = resource.getHeaders();
                    for (int i = 0; i < headers.length; i++) {
                        sendLine( pw, headers[i] );
                    }
                    sendLine( pw, "" );
                    pw.flush();
                    pw = new PrintWriter( new OutputStreamWriter( socket.getOutputStream(), resource.getCharacterSet() ) );
                    sendText( pw, resource.getContents() );
                }
            } catch (IOException e) {
                e.fillInStackTrace();
                pw.close();
                socket.close();
                throw e;
            } catch (Throwable t) {
		System.out.println( "Internal error: " + t );
		t.printStackTrace();
                sendResponse( pw, HttpURLConnection.HTTP_INTERNAL_ERROR, t.toString() );
            }
        }

        pw.close();
        socket.close();
    }


    private WebResource getResource( String command, String uri, BufferedReader br ) throws IOException {
        Object resource = _resources.get( uri );
        if (command.equals( "GET" ) && resource instanceof WebResource) {
            return (WebResource) resource;
        } else if (command.equals( "POST" ) && resource instanceof PseudoServlet) {
            Dictionary requestData = readRequest( br );
            return ((PseudoServlet) resource).getPostResponse( getParameters( (String) requestData.get( PseudoServlet.CONTENTS ) ), requestData );
        } else if (command.equals( "GET" ) && resource instanceof PseudoServlet) {
            Dictionary requestData = readRequest( br );
            return ((PseudoServlet) resource).getGetResponse( getParameters( (String) requestData.get( PseudoServlet.CONTENTS ) ), requestData );
        } else {
            return null;
        }
    }


    private Dictionary readRequest( BufferedReader br ) throws IOException {
        Hashtable headers = new Hashtable();
        String lastHeader = null;

        String header = br.readLine();
        while (header.length() > 0) {
	    if (header.charAt(0) <= ' ') {
	        if (lastHeader == null) continue;
		headers.put( lastHeader, headers.get( lastHeader ) + header.trim() );
	    } else {
	        lastHeader = header.substring( 0, header.indexOf(':') ).toUpperCase();
                headers.put( lastHeader, header.substring( header.indexOf(':')+1 ).trim() );
	    }
            header = br.readLine();
        }
        readContent( headers, br );
        return headers;
    }


    private void readContent( Hashtable headers, BufferedReader br ) throws IOException {
        if (headers.get( "CONTENT-LENGTH" ) == null) return;
        try {
            int contentLength = Integer.parseInt( (String) headers.get( "CONTENT-LENGTH" ) );
            char[] content = new char[ contentLength ];
            br.read( content );
            headers.put( PseudoServlet.CONTENTS, new String( content ) );
        } catch (NumberFormatException e) {
        }
    }


    private void printContent( char[] content ) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < content.length; i++) {
            if (i == 0) {
            } else if ((i % 40) == 0) {
                sb.append( '\n' ); 
            } else if (i % 4 == 0) {
                sb.append( ' ' );
            }
            sb.append( Integer.toHexString( content[i] ) );
        }
        System.out.println( sb.toString() );
    }
      
    
    private Dictionary getParameters( String content ) throws IOException {
        Hashtable parameters = new Hashtable();
	if (content == null || content.trim().length() == 0) return parameters;

        StringTokenizer st = new StringTokenizer( content, "&=" );
        while (st.hasMoreTokens()) {
            String name = st.nextToken();
            if (st.hasMoreTokens()) {
                addParameter( parameters, decode( name ), decode( st.nextToken() ) );
            }
        }
        return parameters;
    }


    private void addParameter( Hashtable parameters, String name, String value ) {
        String[] oldValues = (String[]) parameters.get( name );
        if (oldValues == null) {
            parameters.put( name, new String[] { value } );
        } else {
            String[] values = new String[ oldValues.length+1 ];
            System.arraycopy( oldValues, 0, values, 0, oldValues.length );
            values[ oldValues.length ] = value;
            parameters.put( name, values );
        }
    }


    private String decode( String byteString ) {
        StringBuffer sb = new StringBuffer();
        char[] chars = byteString.toCharArray();
        char[] hexNum = { '0', '0', '0' };

        int i = 0;
        while (i < chars.length) {
            if (chars[i] == '+') {
                i++;
                sb.append( ' ' );
            } else if (chars[i] == '%') {
                i++;
                hexNum[1] = chars[i++];
                hexNum[2] = chars[i++];
                sb.append( (char) Integer.parseInt( new String( hexNum ), 16 ) );
            } else {
                sb.append( chars[i++] );
            }
        }
        return sb.toString();
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



    private String toUnicode( String string ) {
        StringBuffer sb = new StringBuffer( );
        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            sb.append( "\\u" );
            sb.append( Integer.toHexString( chars[i] ) );
        }
        return sb.toString();
    }
    

    private ServerSocket getServerSocket() throws IOException {
        synchronized (this) {
            if (_serverSocket == null) _serverSocket = new ServerSocket(0);
            _serverSocket.setSoTimeout( 1000 );
        }
        return _serverSocket;
    }


    private ServerSocket _serverSocket;

}
