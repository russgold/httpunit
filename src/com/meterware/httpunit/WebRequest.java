package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2000-2002, Russell Gold
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
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.security.Provider;
import java.security.Security;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * A request sent to a web server.
 **/
abstract
public class WebRequest implements ParameterHolder {


    /**
     * Sets the value of a header to be sent with this request. A header set here will override any matching header set
     * in the WebClient when the request is actually sent.
     */
    public void setHeaderField( String headerName, String headerValue ) {
        getHeaderDictionary().put( headerName, headerValue );
    }

    /**
     * Returns a copy of the headers to be sent with this request.
     **/
    public Dictionary getHeaders() {
        return (Dictionary) getHeaderDictionary().clone();
    }


    /**
     * Returns the final URL associated with this web request.
     **/
    public URL getURL() throws MalformedURLException {
        if (getURLBase() == null || getURLString().indexOf( ':' ) > 0) validateProtocol( getURLString() );
        return new URL( getURLBase(), getURLString() );
    }


    /**
     * Returns the target for this web request.
     **/
    public String getTarget() {
        return _target;
    }


    /**
     * Returns the query string defined for this request.
     **/
    public String getQueryString() {
        return getParameterString();
    }


    /**
     * Returns the HTTP method defined for this request.
     **/
    abstract
    public String getMethod();


//------------------------------------- ParameterCollection methods ------------------------------------


    /**
     * Sets the value of a parameter in a web request.
     **/
    public void setParameter( String name, String value ) {
        if (HttpUnitOptions.getParameterValuesValidated()) validateParameterValue( name, value );
        _parameterCollection.setParameter( name, value );
    }


    /**
     * Sets the multiple values of a parameter in a web request.
     **/
    public void setParameter( String name, String[] values ) {
        if (HttpUnitOptions.getParameterValuesValidated()) validateParameterValues( name, values );
        _parameterCollection.setParameter( name, values );
    }


    /**
     * Returns true if the specified parameter is a file field.
     **/
    public boolean isFileParameter( String name ) {
        return _sourceForm != null && _sourceForm.isFileParameter( name );
    }


    /**
     * Sets the file for a parameter upload in a web request.
     **/
    public void selectFile( String parameterName, File file ) {
        assertFileParameter( parameterName );
        if (!isMimeEncoded()) throw new MultipartFormRequiredException();
    }


    /**
     * Sets the file for a parameter upload in a web request.
     **/
    public void selectFile( String parameterName, File file, String contentType ) {
        assertFileParameter( parameterName );
        if (!isMimeEncoded()) throw new MultipartFormRequiredException();
    }


    /**
     * Sets the file for a parameter upload in a web request.
     **/
    public void selectFile( String parameterName, String fileName, InputStream inputStream, String contentType ) {
        assertFileParameter( parameterName );
    }


    /**
     * Throws an exception if the specified parameter may not be set as a file.
     */
    private void assertFileParameter( String parameterName )
    {
        if (!maySelectFile( parameterName )) throw new IllegalNonFileParameterException( parameterName );
        if (!isMimeEncoded()) throw new MultipartFormRequiredException();
    }


    /**
     * Returns an enumeration of all parameters in this web request.
     **/
    public Enumeration getParameterNames() {
        return _parameterCollection.getParameterNames();
    }


    /**
     * Returns the value of a parameter in this web request.
     * @return the value of the named parameter, or empty string
     *         if it is not set.
     **/
    public String getParameter( String name ) {
        String[] values = getParameterValues( name );
        return values.length == 0 ? "" : values[0];
    }


    /**
     * Returns the multiple default values of the named parameter.
     **/
    public String[] getParameterValues( String name ) {
        return _parameterCollection.getParameterValues( name );
    }


    /**
     * Removes a parameter from this web request.
     **/
    public void removeParameter( String name ) {
        _parameterCollection.removeParameter( name );
    }


//------------------------------------- Object methods ------------------------------------


    public String toString() {
        return getMethod() + " request for " + getURLString();
    }



//------------------------------------- protected members ------------------------------------


    /**
     * Constructs a web request using an absolute URL string.
     **/
    protected WebRequest( String urlString ) {
        this( (URL) null, urlString );
    }


    /**
     * Constructs a web request using a base URL and a relative URL string.
     **/
    protected WebRequest( URL urlBase, String urlString ) {
        this( urlBase, urlString, TOP_FRAME );
    }


    /**
     * Constructs a web request using a base URL, a relative URL string, and a target.
     **/
    protected WebRequest( URL urlBase, String urlString, String target ) {
        _urlBase   = urlBase;
        _urlString = urlString;
        _target    = target;
    }


    /**
     * Constructs a web request from a form.
     **/
    protected WebRequest( URL urlBase, String urlString, String target, WebForm sourceForm, SubmitButton button ) {
        this( urlBase, urlString, target );
        _sourceForm   = sourceForm;

        if (button != null && button.getName().length() > 0) {
            _parameterCollection._parameters.put( button.getName(), button.getValue() );
            if (button.isImageButton()) {
                _imageButtonName = button.getName();
                setSubmitPosition( 0, 0 );
            }
        }

    }


    /**
     * Constructs a web request using a base request and a relative URL string.
     **/
    protected WebRequest( WebRequest baseRequest, String urlString, String target ) throws MalformedURLException {
        this( baseRequest.getURL(), urlString, target );
    }


    /**
     * Returns true if this request is based on a web form.
     **/
    final
    protected boolean isFormBased() {
        return _sourceForm != null;
    }


    /**
     * Returns true if selectFile may be called with this parameter.
     */
    protected boolean maySelectFile( String parameterName )
    {
        return isFileParameter( parameterName );
    }


    /**
     * Returns true if this request is to be MIME-encoded.
     **/
    protected boolean isMimeEncoded() {
        return _sourceForm != null && _sourceForm.isSubmitAsMime();
    }


    /**
     * Returns the content type of this request. If null, no content is specified.
     */
    protected String getContentType() {
        return null;
    }


    /**
     * Returns the character set required for this request.
     **/
    final
    protected String getCharacterSet() {
        if (_sourceForm == null) {
            return "iso-8859-1";
        } else {
            return _sourceForm.getCharacterSet();
        }
    }


    /**
     * Performs any additional processing necessary to complete the request.
     **/
    protected void completeRequest( URLConnection connection ) throws IOException {
    }


    final
    protected URL getURLBase() {
        return _urlBase;
    }


    protected String getURLString() {
        return _urlString;
    }


    final
    protected boolean hasNoParameters() {
        return _parameterCollection._parameters.size() == 0;
    }

    final
    protected String getParameterString() {
        StringBuffer sb = new StringBuffer(HttpUnitUtils.DEFAULT_BUFFER_SIZE);
        Enumeration e = _parameterCollection._parameters.keys();

        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            Object value = _parameterCollection._parameters.get( name );
            if (value instanceof String) {
                appendParameter( sb, name, (String) value, e.hasMoreElements() );
            } else {
                appendParameters( sb, name, (String[]) value, e.hasMoreElements() );
            }
        }
        return sb.toString();
    }


//---------------------------------- package members --------------------------------

    /** The name of the topmost frame. **/
    final static String TOP_FRAME = "_top";


    void setSubmitPosition( int x, int y ) {
        if (_imageButtonName == null) return;
        _parameterCollection._parameters.put( _imageButtonName + ".x", Integer.toString( x ) );
        _parameterCollection._parameters.put( _imageButtonName + ".y", Integer.toString( y ) );
    }


    Hashtable getHeaderDictionary() {
        if (_headers == null) {
            _headers = new Hashtable();
            if (getContentType() != null) _headers.put( "Content-Type", getContentType() );
        }
        return _headers;
    }


    static class UploadFileSpec {
        UploadFileSpec( File file ) {
            _file = file;
            guessContentType();
        }


        UploadFileSpec( File file, String contentType ) {
            _file = file;
            _contentType = contentType;
        }


        UploadFileSpec( String fileName, InputStream inputStream, String contentType ) {
            _fileName = fileName;
            _inputStream = inputStream;
            _contentType = contentType;
        }


        InputStream getInputStream() throws IOException {
            if (_inputStream == null) {
                _inputStream = new FileInputStream( _file );
            }
            return _inputStream;
        }


        String getFileName() {
            if (_fileName == null) {
                _fileName = _file.getAbsolutePath();
            }
            return _fileName;
        }


        String getContentType() {
            return _contentType;
        }

        private File _file;

        private InputStream _inputStream;

        private String _fileName;

        private String _contentType = "text/plain";

        private static String[][] CONTENT_EXTENSIONS = {
            { "text/plain",               "txt", "text" },
            { "text/html",                "htm", "html" },
            { "image/gif",                "gif" },
            { "image/jpeg",               "jpg", "jpeg" },
            { "image/png",                "png" },
            { "application/octet-stream", "zip" }
        };


        private void guessContentType() {
            String extension = getExtension( _file.getName() );
            for (int i = 0; i < CONTENT_EXTENSIONS.length; i++) {
                for (int j=1; j < CONTENT_EXTENSIONS[i].length; j++) {
                    if (extension.equalsIgnoreCase( CONTENT_EXTENSIONS[i][j] )) {
                        _contentType = CONTENT_EXTENSIONS[i][0];
                        return;
                    }
                }
            }
        }

        private String getExtension( String fileName ) {
            return fileName.substring( fileName.lastIndexOf( '.' ) + 1 );
        }
    }


//--------------------------------------- private members ------------------------------------

    /** The name of the system parameter used by java.net to locate protocol handlers. **/
    private final static String PROTOCOL_HANDLER_PKGS  = "java.protocol.handler.pkgs";

    /** The name of the JSSE class which provides support for SSL. **/
    private final static String SunJSSE_PROVIDER_CLASS = "com.sun.net.ssl.internal.ssl.Provider";

    /** The name of the JSSE class which supports the https protocol. **/
    private final static String SSL_PROTOCOL_HANDLER   = "com.sun.net.ssl.internal.www.protocol";

    private UncheckedParameterCollection _parameterCollection = new UncheckedParameterCollection();

    private URL          _urlBase;
    private String       _urlString;
    private WebForm      _sourceForm;
    private String       _imageButtonName;
    private String       _target = TOP_FRAME;
    private Hashtable    _headers;

    private boolean      _httpsProtocolSupportEnabled;


    private void appendParameters( StringBuffer sb, String name, String[] values, boolean moreToCome ) {
        for (int i = 0; i < values.length; i++) {
            appendParameter( sb, name, values[i], (i < values.length-1 || moreToCome ) );
        }
    }


    private void appendParameter( StringBuffer sb, String name, String value, boolean moreToCome ) {
        sb.append( encode( name ) );
        if (value != null) sb.append( '=' ).append( encode( value ) );
        if (moreToCome) sb.append( '&' );
    }


    /**
     * Returns a URL-encoded version of the string, including all eight bits, unlike URLEncoder, which strips the high bit.
     **/
    private String encode( String source ) {
        if (_sourceForm == null || _sourceForm.getCharacterSet().equalsIgnoreCase( "iso-8859-1" )) {
            return URLEncoder.encode( source );
        } else {
            try {
                byte[] rawBytes = source.getBytes( _sourceForm.getCharacterSet() );
                StringBuffer result = new StringBuffer(HttpUnitUtils.DEFAULT_BUFFER_SIZE);
                for (int i = 0; i < rawBytes.length; i++) {
                    int candidate = rawBytes[i] & 0xff;
                    if (candidate == ' ') {
                        result.append( '+' );
                    } else if ((candidate >= 'A' && candidate <= 'Z') ||
                               (candidate >= 'a' && candidate <= 'z') ||
                               (candidate == '.') ||
                               (candidate >= '0' && candidate <= '9')) {
                        result.append( (char) rawBytes[i] );
                    } else if (candidate < 16) {
                        result.append( "%0" ).append( Integer.toHexString( candidate ).toUpperCase() );
                    } else {
                        result.append( '%' ).append( Integer.toHexString( candidate ).toUpperCase() );
                    }
                }
                return result.toString();
            } catch (java.io.UnsupportedEncodingException e) {
                return "????";    // XXX should pass the exception through as IOException ultimately
            }
        }
    }


    private void validateParameterValue( String name, String value ) {
        if (_sourceForm == null) return;
        validateOneParameterValue( name, value );
        validateRequiredValues( name, new String[] { value } );
    }


    private void validateOneParameterValue( String name, String value ) {
        if (_sourceForm.isTextParameter( name )) return;
        if (_sourceForm.isFileParameter( name )) throw new IllegalFileParameterException( name );
        if (!inArray( name, _sourceForm.getParameterNames() )) throw new NoSuchParameterException( name );
        if (!inArray( value, _sourceForm.getOptionValues( name ) )) throw new IllegalParameterValueException( name, value, _sourceForm.getOptionValues( name ) );
    }


    private void validateParameterValues( String name, String[] values ) {
        if (_sourceForm == null) return;
        if (values.length > 1 && !_sourceForm.isMultiValuedParameter( name )) {
            if (!_sourceForm.isTextParameter( name ) || _sourceForm.getNumTextParameters( name ) == 1) {
                throw new SingleValuedParameterException( name );
            } else if (values.length > _sourceForm.getNumTextParameters( name )) {
                throw new TextParameterCountException( name, _sourceForm.getNumTextParameters( name ) );
            }
        }

        for (int i = 0; i < values.length; i++) validateOneParameterValue( name, values[i] );
        validateRequiredValues( name, values );
    }


    private void validateRequiredValues( String name, String[] values ) {
        String[] required = _sourceForm.getRequiredValues( name );
        for (int i = 0; i < required.length; i++) {
            if (!inArray( required[i], values )) throw new MissingParameterValueException( name, required[i], values );
        }
    }


    private boolean inArray( String candidate, String[] values ) {
        for (int i = 0; i < values.length; i++) {
            if (candidate.equals( values[i] )) return true;
        }
        return false;
    }


    private void validateProtocol( String urlString ) {
        if (urlString.indexOf(':') <= 0) {
            throw new RuntimeException( "No protocol specified in URL '" + urlString + "'" );
        }

        String protocol = urlString.substring( 0, urlString.indexOf( ':' ) );
        if (protocol.equalsIgnoreCase( "http" )) {
            return;
        } else if (protocol.equalsIgnoreCase( "https" )) {
            validateHttpsProtocolSupport();
        }
    }


    void validateHttpsProtocolSupport() {
        if (!_httpsProtocolSupportEnabled) {
            verifyHttpsSupport();
            _httpsProtocolSupportEnabled = true;
        }
    }


    private static void listProviders() {
        Provider[] list = Security.getProviders();
        for (int i = 0; i < list.length; i++) {
            System.out.println( "provider" + i + "=" + list[i] );
        }
    }


    private static boolean hasProvider( Class providerClass ) {
        Provider[] list = Security.getProviders();
        for (int i = 0; i < list.length; i++) {
            if (list[i].getClass().equals( providerClass )) return true;
        }
        return false;
    }

    private static void verifyHttpsSupport() {
        if (System.getProperty( "java.version" ).startsWith( "1.1" )) {
            throw new RuntimeException( "https support requires Java 2" );
        } else {
            try {
                Class providerClass = Class.forName( SunJSSE_PROVIDER_CLASS );
                if (!hasProvider( providerClass )) Security.addProvider( (Provider) providerClass.newInstance() );
                registerSSLProtocolHandler();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException( "https support requires the Java Secure Sockets Extension. See http://java.sun.com/products/jsse" );
            } catch (Throwable e) {
                throw new RuntimeException( "Unable to enable https support. Make sure that you have installed JSSE " +
                                            "as described in http://java.sun.com/products/jsse/install.html: " + e );
            }
        }
    }


    private static void registerSSLProtocolHandler() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String list = System.getProperty( PROTOCOL_HANDLER_PKGS );
        Method setMethod = System.class.getMethod( "setProperty", new Class[] { String.class, String.class } );
        if (list == null || list.length() == 0) {
            setMethod.invoke( null, new String[] { PROTOCOL_HANDLER_PKGS, SSL_PROTOCOL_HANDLER } );
        } else if (list.indexOf( SSL_PROTOCOL_HANDLER ) < 0) {
            setMethod.invoke( null, new String[] { PROTOCOL_HANDLER_PKGS, SSL_PROTOCOL_HANDLER + " | " + list } );
        }
    }


}


//================================ exception class NoSuchParameterException =========================================


/**
 * This exception is thrown on an attempt to set a parameter to a value not permitted to it by the form.
 **/
class NoSuchParameterException extends IllegalRequestParameterException {


    NoSuchParameterException( String parameterName ) {
        _parameterName = parameterName;
    }


    public String getMessage() {
        return "No parameter named '" + _parameterName + "' is defined in the form";
    }


    private String _parameterName;

}


//============================= exception class IllegalParameterValueException ======================================


/**
 * This exception is thrown on an attempt to set a parameter to a value not permitted to it by the form.
 **/
class IllegalParameterValueException extends IllegalRequestParameterException {


    IllegalParameterValueException( String parameterName, String badValue, String[] allowed ) {
        _parameterName = parameterName;
        _badValue      = badValue;
        _allowedValues = allowed;
    }


    public String getMessage() {
        StringBuffer sb = new StringBuffer(HttpUnitUtils.DEFAULT_TEXT_BUFFER_SIZE);
        sb.append( "May not set parameter '" ).append( _parameterName ).append( "' to '" );
        sb.append( _badValue ).append( "'. Value must be one of: { " );
        for (int i = 0; i < _allowedValues.length; i++) {
            if (i != 0) sb.append( ", " );
            sb.append( _allowedValues[i] );
        }
        sb.append( " }" );
        return sb.toString();
    }


    private String   _parameterName;
    private String   _badValue;
    private String[] _allowedValues;
}


//============================= exception class MissingParameterValueException ======================================


/**
 * This exception is thrown on an attempt to remove a required value from a form parameter.
 **/
class MissingParameterValueException extends IllegalRequestParameterException {


    MissingParameterValueException( String parameterName, String missingValue, String[] proposed ) {
        _parameterName  = parameterName;
        _missingValue   = missingValue;
        _proposedValues = proposed;
    }


    public String getMessage() {
        StringBuffer sb = new StringBuffer(HttpUnitUtils.DEFAULT_TEXT_BUFFER_SIZE);
        sb.append( "Parameter '" ).append( _parameterName ).append( "' must have the value '" );
        sb.append( _missingValue ).append( "'. Attempted to set it to: { " );
        for (int i = 0; i < _proposedValues.length; i++) {
            if (i != 0) sb.append( ", " );
            sb.append( _proposedValues[i] );
        }
        sb.append( " }" );
        return sb.toString();
    }


    private String   _parameterName;
    private String   _missingValue;
    private String[] _proposedValues;
}


//============================= exception class SingleValuedParameterException ======================================


/**
 * This exception is thrown on an attempt to set a single-valued parameter to multiple values.
 **/
class SingleValuedParameterException extends IllegalRequestParameterException {


    SingleValuedParameterException( String parameterName ) {
        _parameterName = parameterName;
    }


    public String getMessage() {
        return "Parameter '" + _parameterName + "' may only have one value.";
    }


    private String _parameterName;

}


/**
 * This exception is thrown on an attempt to set a text parameter to more values than are allowed.
 **/
class TextParameterCountException extends IllegalRequestParameterException {


    TextParameterCountException( String parameterName, int numAllowed ) {
        _parameterName = parameterName;
        _numAllowed    = numAllowed;
    }


    public String getMessage() {
        return "Parameter '" + _parameterName + "' may have no more than " + _numAllowed + " values.";
    }


    private String _parameterName;
    private int    _numAllowed;

}


//============================= exception class IllegalFileParameterException ======================================


/**
 * This exception is thrown on an attempt to set a file parameter to a text value.
 **/
class IllegalFileParameterException extends IllegalRequestParameterException {


    IllegalFileParameterException( String parameterName ) {
        _parameterName = parameterName;
    }


    public String getMessage() {
        return "Parameter '" + _parameterName + "' is a file parameter and may not be set to a text value.";
    }


    private String _parameterName;

}


//============================= exception class IllegalNonFileParameterException ======================================


/**
 * This exception is thrown on an attempt to set a non-file parameter to a file value.
 **/
class IllegalNonFileParameterException extends IllegalRequestParameterException {


    IllegalNonFileParameterException( String parameterName ) {
        _parameterName = parameterName;
    }


    public String getMessage() {
        return "Parameter '" + _parameterName + "' is not a file parameter and may not be set to a file value.";
    }


    private String _parameterName;

}


//============================= exception class MultipartFormRequiredException ======================================


/**
 * This exception is thrown on an attempt to set a file parameter in a form that does not specify MIME encoding.
 **/
class MultipartFormRequiredException extends IllegalRequestParameterException {


    MultipartFormRequiredException() {
    }


    public String getMessage() {
        return "The request does not use multipart/form-data encoding, and cannot be used to upload files ";
    }


}
