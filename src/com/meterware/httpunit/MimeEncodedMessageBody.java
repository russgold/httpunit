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
import java.io.IOException;
import java.io.OutputStream;

import java.net.URLConnection;

import java.util.Dictionary;
import java.util.Enumeration;

import javax.activation.*;

import javax.mail.MessagingException;

import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;


/**
 * A POST-method message body which is MIME-encoded. This is used when uploading files, and is selected when the enctype
 * parameter of a form is set to "multi-part/form-data".
 *
 * Note that this class is only compiled if the JavaMail and Java Activation Framework are accessible.
 **/
class MimeEncodedMessageBody extends MessageBody {


    public MimeEncodedMessageBody( PostMethodWebRequest request ) {
        super( request );
    }


    /**
     * Updates the headers for this request as needed.
     **/
    void updateHeaders( URLConnection connection ) throws IOException {
        connection.setRequestProperty( "Content-type", getMimeBody().getContentType() );
    }


    /**
     * Transmits the body of this request as a sequence of bytes.
     **/
    void writeTo( OutputStream outputStream ) throws IOException {
        try {
            getMimeBody().writeTo( outputStream );
        } catch (MessagingException e) {
            throw new IOException( e.toString() );
        }
    }


    private MimeMultipart _mimeMultipart;

    private MimeMultipart getMimeBody() throws IOException {
        if (_mimeMultipart == null) {
            try {
                _mimeMultipart = new MimeMultipart( "form-data" );
                for (Enumeration e = getRequest().getParameterNames(); e.hasMoreElements();) {
                    String name = (String) e.nextElement();
                    MimeBodyPart mbp = new MimeBodyPart();
                    mbp.setDisposition( "form-data; name=\"" + name + '"' );   // XXX need to handle non-ascii names here
                    mbp.setText( getRequest().getParameter( name ), getRequest().getCharacterSet() );
                    _mimeMultipart.addBodyPart( mbp );
                }

                Dictionary files = getRequest().getSelectedFiles();
                for (Enumeration e = files.keys(); e.hasMoreElements();) {
                    String name = (String) e.nextElement();
                    WebRequest.UploadFileSpec spec = (WebRequest.UploadFileSpec) files.get( name );
                    FileDataSource fds = new FileDataSource( spec.getFile() );
                    MimeBodyPart mbp = new MimeBodyPart();
                    mbp.setDisposition( "form-data; name=\"" + name + "\"; filename=\"" + spec.getFile().getName() + '"' );   // XXX need to handle non-ascii names here
                    mbp.setDataHandler( new DataHandler( fds ) );
                    _mimeMultipart.addBodyPart( mbp );
                }
            } catch (MessagingException e) {
                throw new IOException( e.toString() );
            }
        }
        return _mimeMultipart;
    }

}

 
