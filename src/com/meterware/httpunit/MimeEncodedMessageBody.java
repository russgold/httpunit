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
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

import java.util.Dictionary;
import java.util.Enumeration;

/**
 * A POST-method message body which is MIME-encoded. This is used when uploading files, and is selected when the enctype
 * parameter of a form is set to "multi-part/form-data".
 **/
class MimeEncodedMessageBody extends MessageBody {


    public MimeEncodedMessageBody( PostMethodWebRequest request ) {
        super( request );
    }


    /**
     * Returns the content type of this message body.
     **/
    String getContentType() {
        return "multipart/form-data; boundary=" + BOUNDARY;
    }


    /**
     * Returns the request associated with this message body, cast to a POST request.
     **/
    PostMethodWebRequest getPostRequest() {
        return (PostMethodWebRequest) getRequest();
    }


    /**
     * Transmits the body of this request as a sequence of bytes.
     **/
    void writeTo( OutputStream outputStream ) throws IOException {
        for (Enumeration e = getRequest().getParameterNames(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
            String[] values = getRequest().getParameterValues( name );
            for (int i = 0; i < values.length; i++) {
                writeLn( outputStream, "--" + BOUNDARY );
                writeLn( outputStream, "Content-Disposition: form-data; name=\"" + name + '"' );  // XXX need to handle non-ascii names here
                writeLn( outputStream, "Content-Type: text/plain; charset=" + getRequest().getCharacterSet() );
                writeLn( outputStream, "" );
                writeLn( outputStream, values[ i ], getRequest().getCharacterSet() );
            }
        }

        Dictionary files = getPostRequest().getSelectedFiles();
        byte[] buffer = new byte[ 8 * 1024 ];
        for (Enumeration e = files.keys(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
            WebRequest.UploadFileSpec spec = (WebRequest.UploadFileSpec) files.get( name );
            writeLn( outputStream, "--" + BOUNDARY );
            writeLn( outputStream, "Content-Disposition: form-data; name=\"" + encode( name ) + "\"; filename=\"" + encode( spec.getFileName() ) + '"' );   // XXX need to handle non-ascii names here
            writeLn( outputStream, "Content-Type: " + spec.getContentType() );
            writeLn( outputStream, "" );

            InputStream in = spec.getInputStream();
            int count = 0;
            do {
                outputStream.write( buffer, 0, count );
                count = in.read( buffer, 0, buffer.length );
            } while (count != -1);

            in.close();
            writeLn( outputStream, "" );
        }
        writeLn( outputStream, "--" + BOUNDARY + "--" );
    }


    private final static String BOUNDARY = "--HttpUnit-part0-aSgQ2M";
    private final static byte[] CRLF     = { 0x0d, 0x0A };


    private String encode( String string ) {
        char[] chars = string.toCharArray();
        StringBuffer sb = new StringBuffer(chars.length+20);
        for (int i = 0; i < chars.length; i++ ) {
            if (chars[i] == '\\') {
                sb.append( "\\\\" );    // accomodate MS-DOS file paths XXX is this safe??
            } else {
                sb.append( chars[i] );
            }
        }
        return sb.toString();
    }


    private void writeLn( OutputStream os, String value, String encoding ) throws IOException {
        os.write( value.getBytes( encoding ) );
        os.write( CRLF );
    }


    private void writeLn( OutputStream os, String value ) throws IOException {
        writeLn( os, value, getRequest().getCharacterSet() );
    }

}

