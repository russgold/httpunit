package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2001, Russell Gold
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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


/**
 * A web request which contains a non-empty message body. Note that such requests
 * <em>must</em> use the <code>http</code> or <code>https</code> protocols.
 **/
abstract
public class MessageBodyWebRequest extends WebRequest {


    /**
     * Subclasses must override this method to provide a message body for the
     * request.
     **/ 
    abstract
    protected MessageBody getMessageBody();


//---------------------------------- WebRequest methods --------------------------------


    /**
     * Performs any additional processing necessary to complete the request.
     **/
    protected void completeRequest( URLConnection connection ) throws IOException {
        ((HttpURLConnection) connection).setRequestMethod( getMethod() );
        connection.setDoInput( true );
        connection.setDoOutput( true );

        OutputStream stream = connection.getOutputStream();
        getMessageBody().writeTo( stream );
        stream.flush();
        stream.close();
    }


    protected String getContentType() {
        return getMessageBody().getContentType();
    }


//----------------------------------- package members -----------------------------------


    /**
     * Constructs a web request using a specific absolute url string.
     **/
    MessageBodyWebRequest( String urlString ) {
        super( urlString );
    }


    /**
     * Constructs a web request with a specific target.
     **/
    MessageBodyWebRequest( URL urlBase, String urlString, String target ) {
        super( urlBase, urlString, target );
    }


    /**
     * Constructs a web request for a form.
     **/
    MessageBodyWebRequest( URL urlBase, String urlString, String target, WebForm sourceForm, SubmitButton button ) {
        super( urlBase, urlString, target, sourceForm, button );
    }




//============================= class InputStreamMessageBody ======================================

    /**
     * A method request message body read directly from an input stream.
     **/
    static class InputStreamMessageBody extends MessageBody {
    
    
        InputStreamMessageBody( MessageBodyWebRequest request, InputStream source, String contentType ) {
            super( request );
            _source = source;
            _contentType = contentType;
        }
    
    
        /**
         * Returns the content type of this message body.
         **/
        String getContentType() {
            return _contentType;
        }


        /**
         * Transmits the body of this request as a sequence of bytes.
         **/
        void writeTo( OutputStream outputStream ) throws IOException {
            byte[] buffer = new byte[8 * 1024];
            int count = 0;
            do {
                outputStream.write( buffer, 0, count );
                count = _source.read( buffer, 0, buffer.length );
            } while (count != -1);
    
            _source.close();
        }
    
    
        private InputStream _source;
        private String      _contentType;
    }
}
