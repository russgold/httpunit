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
     * Constructs a web request with a specific target.
     **/
    public PostMethodWebRequest( URL urlBase, String urlString, String target ) {
        super( urlBase, urlString, target );
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
            return new MimeEncodedMessageBody( this );
        } else {
            return new URLEncodedMessageBody( this );
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

