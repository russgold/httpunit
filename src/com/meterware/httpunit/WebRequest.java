package com.meterware.httpunit;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.reflect.*;
import java.security.Provider;
import java.security.Security;

/**
 * A request sent to a web server.
 **/
abstract
public class WebRequest {


    /**
     * Sets the value of a parameter in a web request.
     **/
    public void setParameter( String name, String value ) {
        if (HttpUnitOptions.getParameterValuesValidated()) validateParameterValue( name, value );
        _parameters.put( name, value );
    }


    /**
     * Sets the multiple values of a parameter in a web request.
     **/
    public void setParameter( String name, String[] values ) {
        if (HttpUnitOptions.getParameterValuesValidated()) validateParameterValues( name, values );
        _parameters.put( name, values );
    }


    /**
     * Sets the file for a parameter upload in a web request. 
     **/
    public void selectFile( String parameterName, File file ) {
        if (_sourceForm == null || !_sourceForm.isFileParameter( parameterName )) {
            throw new IllegalNonFileParameterException( parameterName );
        }
        if (!isMimeEncoded()) throw new MultipartFormRequiredException();
    }


    /**
     * Returns an enumeration of all parameters in this web request.
     **/
    public Enumeration getParameterNames() {
        return _parameters.keys();
    }


    /**
     * Returns the value of a parameter in this web request.
     * @return the value of the named parameter, or null if it is not set.
     **/
    public String getParameter( String name ) {
        Object value = _parameters.get( name );
        if (value instanceof String[]) {
            return ((String[]) value)[0];
        } else if (value == null) {
            return "";
        } else {
            return value.toString();
        }
    }


    /**
     * Returns the multiple default values of the named parameter.
     **/
    public String[] getParameterValues( String name ) {
        Object result = _parameters.get( name );
        if (result instanceof String) return new String[] { (String) result };
        if (result instanceof String[]) return (String[]) result;
        if (result instanceof UploadFileSpec) return new String[] { result.toString() };
        return new String[0];
    }


    /**
     * Removes a parameter from this web request.
     **/
    public void removeParameter( String name ) {
        _parameters.remove( name );
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
            _parameters.put( button.getName(), button.getValue() );
            if (button.isImageButton()) {
                _imageButtonName = button.getName();
                setSubmitPosition( 0, 0 );
            }
        }

    }
    

    /**
     * Constructs a web request using a base request and a relative URL string.
     **/
    protected WebRequest( WebRequest baseRequest, String urlString ) throws MalformedURLException {
        this( baseRequest.getURL(), urlString );
    }


    /**
     * Returns true if the specified parameter is a file field.
     **/
    protected boolean isFileParameter( String name ) {
        return false;
    }


    /**
     * Returns true if this request is to be MIME-encoded.
     **/
    final
    protected boolean isMimeEncoded() {
        return _sourceForm != null && _sourceForm.isSubmitAsMime();
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
        return _parameters.size() == 0;
    }


    final
    protected String getParameterString() {
        StringBuffer sb = new StringBuffer();
        Enumeration e = _parameters.keys();

        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            Object value = _parameters.get( name );
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
        _parameters.put( _imageButtonName + ".x", Integer.toString( x ) );
        _parameters.put( _imageButtonName + ".y", Integer.toString( y ) );
    }


    void setRequestHeader( String headerName, String headerValue ) {
        _headers.put( headerName.toUpperCase(), headerValue );
    }


    Dictionary getHeaders() {
        return _headers;
    }


    static class UploadFileSpec {
        UploadFileSpec( File file ) {
            _file = file;
        }


        UploadFileSpec( File file, String contentType ) {
            _file = file;
            _contentType = contentType;
        }


        File getFile() {
            return _file;
        }


        String getContentType() {
            return _contentType;
        }

        private File _file;

        private String _contentType;
    }


//--------------------------------------- private members ------------------------------------

    /** The name of the system parameter used by java.net to locate protocol handlers. **/
    private final static String PROTOCOL_HANDLER_PKGS  = "java.protocol.handler.pkgs";

    /** The name of the JSSE class which provides support for SSL. **/
    private final static String SunJSSE_PROVIDER_CLASS = "com.sun.net.ssl.internal.ssl.Provider";

    /** The name of the JSSE class which supports the https protocol. **/
    private final static String SSL_PROTOCOL_HANDLER   = "com.sun.net.ssl.internal.www.protocol";


    private URL          _urlBase;
    private String       _urlString;
    private Hashtable    _parameters = new Hashtable();
    private WebForm      _sourceForm;
    private String       _imageButtonName;
    private String       _target = TOP_FRAME;
    private Hashtable    _headers = new Hashtable();

    private boolean      _httpsProtocolSupportEnabled;


    private void appendParameters( StringBuffer sb, String name, String[] values, boolean moreToCome ) {
        for (int i = 0; i < values.length; i++) {
            appendParameter( sb, name, values[i], (i < values.length-1 || moreToCome ) );
        }
    }


    private void appendParameter( StringBuffer sb, String name, String value, boolean moreToCome ) {
        sb.append( encode( name ) ).append( '=' );
        sb.append( encode( value ) );
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
                StringBuffer result = new StringBuffer();
                for (int i = 0; i < rawBytes.length; i++) {
                    int candidate = rawBytes[i] & 0xff;
                    if (candidate == ' ') {
                        result.append( '+' );
                    } else if ((candidate >= 'A' && candidate <= 'Z') ||
                               (candidate >= 'a' && candidate <= 'z') ||
                               (candidate >= '0' && candidate <= '9')) {
                        result.append( (char) rawBytes[i] );
                    } else if (candidate < 16) {
                        result.append( "%0" ).append( Integer.toHexString( candidate ) );
                    } else {
                        result.append( '%' ).append( Integer.toHexString( candidate ) );
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
        if (_sourceForm.isTextParameter( name )) return;
        if (_sourceForm.isFileParameter( name )) throw new IllegalFileParameterException( name );
        if (!inArray( name, _sourceForm.getParameterNames() )) throw new NoSuchParameterException( name );
        if (!inArray( value, _sourceForm.getOptionValues( name ) )) throw new IllegalParameterValueException( name, value );
    }


    private void validateParameterValues( String name, String[] values ) {
        if (_sourceForm == null) return;
        if (values.length > 1 && !_sourceForm.isMultiValuedParameter( name )) {
            throw new SingleValuedParameterException( name );
        }

        for (int i = 0; i < values.length; i++) validateParameterValue( name, values[i] );
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


    IllegalParameterValueException( String parameterName, String badValue ) {
        _parameterName = parameterName;
        _badValue      = badValue;
    }

    public String getMessage() {
        return "May not set parameter '" + _parameterName + "' to '" + _badValue + "'";
    }

    private String _parameterName;
    private String _badValue;
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

