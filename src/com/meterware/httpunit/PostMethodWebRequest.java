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
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.net.URL;
import java.net.URLConnection;

import java.util.Dictionary;
import java.util.Hashtable;


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


    /**
     * Returns the HTTP method defined for this request.
     **/
    public String getMethod() {
        return "POST";
    }


    /**
     * Sets the file for a parameter upload in a web request.
     **/
    public void selectFile( String parameterName, File file ) {
        super.selectFile( parameterName, file );

        _files.put( parameterName, new UploadFileSpec( file ) );
    }


//---------------------------------- WebRequest methods --------------------------------


    protected void completeRequest( URLConnection connection ) throws IOException {
        MessageBody mb = newMessageBody();
        mb.updateHeaders( connection );
        connection.setDoInput( true );
        connection.setDoOutput( true );
        OutputStream stream = connection.getOutputStream();
        mb.writeTo( stream );
        stream.flush();
        stream.close();
    }


//----------------------------------- package members -----------------------------------


    /**
     * Constructs a web request for a form.
     **/
    PostMethodWebRequest( URL urlBase, String urlString, String target, WebForm sourceForm, SubmitButton button ) {
        super( urlBase, urlString, target, sourceForm, button );
    }


    /**
     * Returns a mapping of file parameters to upload specs.
     **/
    Dictionary getSelectedFiles() {
        return (Dictionary) _files.clone();
    }



//---------------------------------- private members -------------------------------------

    private Hashtable _files = new Hashtable();


    private MessageBody newMessageBody() {
        if (isMimeEncoded()) {
            return newMimeEncodedMessageBody( this );
        } else {
            return new URLEncodedMessageBody( this );
        }
    }


    private static Constructor _bodyConstructor;


    static private MessageBody newMimeEncodedMessageBody( PostMethodWebRequest request ) {
        try {
            return (MessageBody) getMimeEncodedMessageBodyConstructor().newInstance( new Object[] { request } );
        } catch (IllegalAccessException e) {
            throw new RuntimeException( "Programming error: no access to desired message body constructor. Please report this problem." );
        } catch (InstantiationException e) {
            throw new RuntimeException( "Programming error: message body class is abstract. Please report this problem." );
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) e.getTargetException();
            } else if (e.getTargetException() instanceof Error) {
                throw (Error) e.getTargetException();
            } else {
                throw new RuntimeException( "Error during construction of MimeEncodedMessageBody: " + e.getTargetException() );
            }
            
        }
    }


    static private Constructor getMimeEncodedMessageBodyConstructor() {
        if (_bodyConstructor == null) {
            try {
                confirmJavaMail();
                confirmJavaActivationFramework();
                Class bodyClass = Class.forName( "com.meterware.httpunit.MimeEncodedMessageBody" );
                _bodyConstructor = bodyClass.getConstructor( new Class[] { PostMethodWebRequest.class } );
            } catch (ClassNotFoundException e) {
                throw new RuntimeException( "Multi-part form support was not compiled.\n" +
                                            "Please rebuild HttpUnit or obtain a version which has this capability included." );
            } catch (NoSuchMethodException e) {
                throw new RuntimeException( "Programming error: cannot find desired message body constructor. Please report this problem." );
            }
        }
        return _bodyConstructor;
    }


    static private void confirmJavaMail() {
        try {
            Class.forName( "javax.mail.MessagingException" );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException( "Multi-part form support requires the Java Mail and Java Activation Framework extensions.\n" +
                                        "The Java Mail extension is not in your classpath." );
        }
    }


    static private void confirmJavaActivationFramework() {
        try {
            Class.forName( "javax.activation.DataSource" );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException( "Multi-part form support requires the Java Mail and Java Activation Framework extensions.\n" +
                                        "The Java Activation Framework extension is not in your classpath." );
        }
    }


}

//============================= class URLEncodedMessageBody ======================================

/**
 * A POST method request message body which uses the default URL encoding.
 **/
class URLEncodedMessageBody extends MessageBody {


    URLEncodedMessageBody( PostMethodWebRequest request ) {
        super( request );
    }


    /**
     * Updates the headers for this request as needed.
     **/
    void updateHeaders( URLConnection connection ) throws IOException {
    }


    /**
     * Transmits the body of this request as a sequence of bytes.
     **/
    void writeTo( OutputStream outputStream ) throws IOException {
        outputStream.write( getRequest().getParameterString().getBytes() );
    }
}

