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
import java.io.InputStream;
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
public class PostMethodWebRequest extends MessageBodyWebRequest {


    /**
     * Constructs a web request using a specific absolute url string.
     **/
    public PostMethodWebRequest( String urlString ) {
        super( urlString );
    }


    /**
     * Constructs a web request using a specific absolute url string and input stream.
     * @param urlString the URL to which the request should be issued
     * @param source    an input stream which will provide the body of this request
     * @param contentType the MIME content type of the body, including any character set
     **/
    public PostMethodWebRequest( String urlString, InputStream source, String contentType ) {
        super( urlString );
        _body = new InputStreamMessageBody( this, source, contentType );
    }


    /**
     * Constructs a web request with a specific target.
     **/
    public PostMethodWebRequest( URL urlBase, String urlString, String target ) {
        super( urlBase, urlString, target );
    }


    /**
     * Selects whether MIME-encoding will be used for this request. MIME-encoding changes the way the request is sent
     * and is required for requests which include file parameters. This method may only be called for a request which
     * was not created from a form.
     **/
    public void setMimeEncoded( boolean mimeEncoded )
    {
        if (isFormBased()) throw new IllegalStateException( "Encoding is defined by the form from which this request is derived." );
        _mimeEncoded = mimeEncoded;
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


    /**
     * Sets the file for a parameter upload in a web request.
     **/
    public void selectFile( String parameterName, File file, String contentType ) {
        super.selectFile( parameterName, file, contentType );

        _files.put( parameterName, new UploadFileSpec( file, contentType ) );
    }


    /**
     * Sets the file for a parameter upload in a web request.
     **/
    public void selectFile( String parameterName, String fileName, InputStream inputStream, String contentType ) {
        super.selectFile( parameterName, fileName, inputStream, contentType );

        _files.put( parameterName, new UploadFileSpec( fileName, inputStream, contentType ) );
    }


    /**
     * Returns true if selectFile may be called with this parameter.
     */
    protected boolean maySelectFile( String parameterName )
    {
        return !isFormBased() || super.isFileParameter( parameterName );
    }


    /**
     * Returns true if this request is to be MIME-encoded.
     **/
    protected boolean isMimeEncoded() {
        return isFormBased() ? super.isMimeEncoded() : _mimeEncoded;
    }


//----------------------------- MessageBodyWebRequest methods ---------------------------


    protected MessageBody newMessageBody() {
        if (_body != null) {
            return _body;
        } else if (isMimeEncoded()) {
            return new MimeEncodedMessageBody( this );
        } else {
            return new URLEncodedMessageBody( this );
        }
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
    private InputStream _source;
    private MessageBody _body;

    /** If true, non-form-based request will be MIME-encoded. **/
    private boolean _mimeEncoded;

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
     * Returns the content type of this message body.
     **/
    String getContentType() {
        return "application/x-www-form-urlencoded" +
                  (!HttpUnitOptions.isPostIncludesCharset() ? ""
                                                            : "; charset=" + getRequest().getCharacterSet());
    }


    /**
     * Transmits the body of this request as a sequence of bytes.
     **/
    void writeTo( OutputStream outputStream ) throws IOException {
        outputStream.write( getRequest().getParameterString().getBytes() );
    }
}

