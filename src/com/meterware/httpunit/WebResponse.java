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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A response to a web request from a web server.
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 * @author <a href="mailto:DREW.VARNER@oracle.com">Drew Varner</a>
 **/
abstract
public class WebResponse implements HTMLSegment {

	/**
	 * A version flag indicating a cookie is based on the
	 * Internet Engineering Task Force's (IETF)
	 * <a href="http://www.ietf.org/rfc/rfc2109.txt">RFC 2109</a>
	 *
	 * <br />
	 * These cookies come from the <code>Set-Cookie:</code> header
	 **/
	public static final int IETF_RFC2109 = 0;

	/**
	 * A version flag indicating a cookie is based on the
	 * Internet Engineering Task Force's (IETF)
	 * <a href="http://www.ietf.org/rfc/rfc2965.txt">RFC 2965</a>
	 *
	 * <br />
	 * These cookies come from the <code>Set-Cookie2:</code> header
	 **/
	public static final int IETF_RFC2965 = 1;

    /**
     * Returns a web response built from a URL connection. Provided to allow
     * access to WebResponse parsing without using a WebClient.
     **/
    public static WebResponse newResponse( URLConnection connection ) throws IOException {
        return new HttpWebResponse( "_top", connection.getURL(), connection );
    }


    /**
     * Returns true if the response is HTML.
     **/
    public boolean isHTML() {
        return getContentType().equals( HTML_CONTENT );
    }


    /**
     * Returns the URL which invoked this response.
     **/
    public URL getURL() {
        return _url;
    }


    /**
     * Returns the title of the page.
     * @exception SAXException thrown if there is an error parsing this response
     **/
    public String getTitle() throws SAXException {
        return getReceivedPage().getTitle();
    }


    /**
     * Returns the target of the page.
     **/
    public String getTarget() {
        return _target;
    }


    /**
     * Returns a request to refresh this page, if any. This request will be defined
     * by a <meta> tag in the header.  If no tag exists, will return null.
     **/
    public WebRequest getRefreshRequest() {
        return _refreshRequest;
    }


    /**
     * Returns the delay before normally following the request to refresh this page, if any.
     * This request will be defined by a <meta> tag in the header.  If no tag exists,
     * will return zero.
     **/
    public int getRefreshDelay() {
        return _refreshDelay;
    }


    /**
     * Returns the response code associated with this response.
     **/
    abstract
    public int getResponseCode();


    /**
     * Returns the content type of this response.
     **/
    public String getContentType() {
        if (_contentType == null) {
            readContentTypeHeader();
            if (_contentType == null) _contentType = DEFAULT_CONTENT_TYPE;
        }
        return _contentType;
    }


    /**
     * Returns the character set used in this response.
     **/
    public String getCharacterSet() {
        if (_characterSet == null) {
            readContentTypeHeader();
            if (_characterSet == null) _characterSet = HttpUnitOptions.getDefaultCharacterSet();
        }
        return _characterSet;
    }


    /**
     * Returns a list of new cookie names defined as part of this response.
     **/
    public String[] getNewCookieNames() {
        String[] names = new String[ getNewCookies().size() ];
        int i = 0;
        for (Enumeration e = getNewCookies().keys(); e.hasMoreElements(); i++ ) {
            names[i] = (String) e.nextElement();
        }
        return names;
    }


    /**
     * Returns the new cookie value defined as part of this response.
     **/
    public String getNewCookieValue( String name ) {
        return (String) getNewCookies().get( name );
    }


    /**
     * Returns the names of the header fields found in the response.
     **/
    abstract
    public String[] getHeaderFieldNames();


    /**
     * Returns the value for the specified header field. If no such field is defined, will return null.
     * No more than one header may be defined for each key.
     **/
    abstract
    public String getHeaderField( String fieldName );


    /**
     * Returns the text of the response (excluding headers) as a string. Use this method in preference to 'toString'
     * which may be used to represent internal state of this object.
     **/
    public String getText() throws IOException {
        if (_responseText == null) loadResponseText();
        return _responseText;
    }


    /**
     * Returns a buffered input stream for reading the contents of this reply.
     **/
    public InputStream getInputStream() throws IOException {
        if (_inputStream == null) _inputStream = new ByteArrayInputStream( new byte[0] );
        return _inputStream;
    }


    /**
     * Returns the names of the frames found in the page in the order in which they appear.
     * @exception SAXException thrown if there is an error parsing this response
     **/
    public String[] getFrameNames() throws SAXException {
        WebFrame[] frames = getFrames();
        String[] result = new String[ frames.length ];
        for (int i = 0; i < result.length; i++) {
            result[i] = frames[i].getName();
        }

        return result;
    }


//---------------------- HTMLSegment methods -----------------------------

    /**
     * Returns the forms found in the page in the order in which they appear.
     * @exception SAXException thrown if there is an error parsing the response.
     **/
    public WebForm[] getForms() throws SAXException {
        return getReceivedPage().getForms();
    }


    /**
     * Returns the form found in the page with the specified name.
     * @exception SAXException thrown if there is an error parsing the response.
     **/
    public WebForm getFormWithName( String name ) throws SAXException {
        return getReceivedPage().getFormWithName( name );
    }


    /**
     * Returns the form found in the page with the specified ID.
     * @exception SAXException thrown if there is an error parsing the response.
     **/
    public WebForm getFormWithID( String ID ) throws SAXException {
        return getReceivedPage().getFormWithID( ID );
    }


    /**
     * Returns the links found in the page in the order in which they appear.
     * @exception SAXException thrown if there is an error parsing the response.
     **/
    public WebLink[] getLinks() throws SAXException {
        return getReceivedPage().getLinks();
    }


    /**
     * Returns the first link which contains the specified text.
     * @exception SAXException thrown if there is an error parsing the response.
     **/
    public WebLink getLinkWith( String text ) throws SAXException {
        return getReceivedPage().getLinkWith( text );
    }


    /**
     * Returns the first link which contains an image with the specified text as its 'alt' attribute.
     * @exception SAXException thrown if there is an error parsing the response.
     **/
    public WebLink getLinkWithImageText( String text ) throws SAXException {
        return getReceivedPage().getLinkWithImageText( text );
    }


    /**
     * Returns the top-level tables found in this page in the order in which
     * they appear.
     * @exception SAXException thrown if there is an error parsing the response.
     **/
    public WebTable[] getTables() throws SAXException {
        return getReceivedPage().getTables();
    }


    /**
     * Returns a copy of the domain object model tree associated with this response.
     * If the response is HTML, it will use a special parser which can transform HTML into an XML DOM.
     * @exception SAXException thrown if there is an error parsing the response.
     **/
    public Document getDOM() throws SAXException {
        if (isHTML()) {
            return (Document) getReceivedPage().getDOM();
        } else {
            return getXMLDOM();
        }
    }


    /**
     * Returns the first table in the response which has the specified text as the full text of
     * its first non-blank row and non-blank column. Will recurse into any nested tables, as needed.
     * Case is ignored.
     * @exception SAXException thrown if there is an error parsing the response.
     * @return the selected table, or null if none is found
     **/
    public WebTable getTableStartingWith( String text ) throws SAXException {
        return getReceivedPage().getTableStartingWith( text );
    }


    /**
     * Returns the first table in the response which has the specified text as a prefix of the text of
     * its first non-blank row and non-blank column. Will recurse into any nested tables, as needed.
     * Case is ignored.
     * @exception SAXException thrown if there is an error parsing the response.
     * @return the selected table, or null if none is found
     **/
    public WebTable getTableStartingWithPrefix( String text ) throws SAXException {
        return getReceivedPage().getTableStartingWithPrefix( text );
    }


    /**
     * Returns the first table in the response which has the specified text as its summary attribute.
     * Will recurse into any nested tables, as needed.
     * Case is ignored.
     * @exception SAXException thrown if there is an error parsing the response.
     * @return the selected table, or null if none is found
     **/
    public WebTable getTableWithSummary( String text ) throws SAXException {
        return getReceivedPage().getTableWithSummary( text );
    }


    /**
     * Returns the first table in the response which has the specified text as its ID attribute.
     * Will recurse into any nested tables, as needed.
     * Case is ignored.
     * @exception SAXException thrown if there is an error parsing the response.
     * @return the selected table, or null if none is found
     **/
    public WebTable getTableWithID( String text ) throws SAXException {
        return getReceivedPage().getTableWithID( text );
    }


//---------------------------------------- Object methods --------------------------------------------

    abstract
    public String toString();


//----------------------------------------- protected members -----------------------------------------------


    /**
     * Constructs a response object.
     * @param target the name of the frame to hold the response
     * @param url the url from which the response was received
     **/
    protected WebResponse( String target, URL url ) {
        _url = url;
        _target = target;
    }


    final
    protected void defineRawInputStream( InputStream inputStream ) {
        if (_inputStream != null || _responseText != null) {
            throw new IllegalStateException( "Must be called before response text is defined." );
        }
        _inputStream = inputStream;
    }


    final
    protected void readRefreshRequest( String contentTypeHeader ) {
        int splitIndex = contentTypeHeader.indexOf( ';' );
        if (splitIndex < 0) splitIndex = 0;
        try {
            _refreshDelay = Integer.parseInt( contentTypeHeader.substring( 0, splitIndex ) );
            _refreshRequest = new GetMethodWebRequest( contentTypeHeader.substring( splitIndex+1 ) );
        } catch (NumberFormatException e) {
            System.out.println( "Unable to interpret refresh tag: \"" + contentTypeHeader + '"' );
        }
    }


    /**
     * Overwrites the current value (if any) of the content type header.
     **/
    protected void setContentTypeHeader( String value ) {
        _contentHeader = value;
    }


//------------------------------------------ package members ------------------------------------------------


    final static WebResponse BLANK_RESPONSE = new DefaultWebResponse( "<html><head></head><body></body></html>" );


    /**
     * Returns the frames found in the page in the order in which they appear.
     **/
    WebRequest[] getFrameRequests() throws SAXException {
        WebFrame[] frames = getFrames();
        Vector requests = new Vector();
        for (int i = 0; i < frames.length; i++) {
            if (frames[i].hasInitialRequest()) {
                requests.addElement( frames[i].getInitialRequest() );
            }
        }

        WebRequest[] result = new WebRequest[ requests.size() ];
        requests.copyInto( result );
        return result;
    }


//--------------------------------- private members --------------------------------------


    final private static String DEFAULT_CONTENT_TYPE   = "text/plain";
    final private static String DEFAULT_CONTENT_HEADER = DEFAULT_CONTENT_TYPE;

    final private static String HTML_CONTENT = "text/html";

    private WebFrame[] _frames;

    private ReceivedPage _page;

    private String _contentHeader;

    private String _contentType;

    private String _characterSet;

    private Hashtable _newCookies;

    private WebRequest _refreshRequest;

    private int _refreshDelay;

    private String _responseText;

    private InputStream _inputStream;


    // the following variables are essentially final; however, the JDK 1.1 compiler does not handle blank final variables properly with
    // multiple constructors that call each other, so the final qualifiers have been removed.

    private URL    _url;

    private String _target;


    protected void loadResponseText() throws IOException {
        if (_responseText != null) throw new IllegalStateException( "May only invoke loadResponseText once" );
        _responseText = "";

        InputStream inputStream = getInputStream();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[8 * 1024];
            int count = 0;
            do {
                outputStream.write( buffer, 0, count );
                count = inputStream.read( buffer, 0, buffer.length );
            } while (count != -1);

            byte[] bytes = outputStream.toByteArray();
            readMetaTags( bytes );
            _responseText = new String( bytes, getCharacterSet() );
            _inputStream  = new ByteArrayInputStream( bytes );
        } finally {
            inputStream.close();
        }
    }


    private void readMetaTags( byte[] rawMessage ) throws UnsupportedEncodingException {
        ByteTagParser parser = new ByteTagParser( rawMessage );
        ByteTag tag = parser.getNextTag();
        while (tag != null && !tag.getName().equalsIgnoreCase( "body" )) {
            if (tag.getName().equalsIgnoreCase( "meta" )) processMetaTag( tag );
            tag = parser.getNextTag();
        }
    }


    private void processMetaTag( ByteTag tag ) {
        if ("content-type".equalsIgnoreCase( tag.getAttribute( "http_equiv" ) )) {
            inferContentType( tag.getAttribute( "content" ) );
        } else if ("refresh".equalsIgnoreCase( tag.getAttribute( "http_equiv" ) )) {
            readRefreshRequest( tag.getAttribute( "content" ) );
        }
    }


    private void inferContentType( String contentTypeHeader ) {
        String originalHeader = getHeaderField( "Content-type" );
        if (originalHeader == null || originalHeader.indexOf( "charset" ) < 0) {
            setContentTypeHeader( contentTypeHeader );
        }
    }


	/**
	 * Parses cookies from the <code>Set-Cookie</code> and the
	 * <code>Set-Cookie2</code> header fields.
	 * <p>
	 * This class does not strictly follow the specifications, but
	 * attempts to imitate the behavior of popular browsers. Specifically,
	 * this method allows cookie values to contain commas, which the
	 * Netscape standard does not allow for.
	 * </p><p>
	 * This method does not parse path,domain,expires or secure information
	 * about the cookie.</p>
	 *
	 * @returns Hashtable a <code>Hashtable</code> of where the name of the
	 *                    cookie is the key and the value of the cookie is
	 *                    the value
	 */
	private Hashtable getNewCookies() {
		if (_newCookies == null) {
			_newCookies = new Hashtable();
		}
		String cookieHeader = getHeaderField( "Set-Cookie" );
		if (cookieHeader != null) {
			processCookieTokens( getCookieTokens(cookieHeader),IETF_RFC2109 );
		}
		String cookieHeader2 = getHeaderField( "Set-Cookie2" );
		if (cookieHeader2 != null) {
			processCookieTokens( getCookieTokens(cookieHeader2),IETF_RFC2965 );
		}
		return _newCookies;
	}


	private void processCookieTokens(Vector tokens,
	                                    int version) {
		// holds tokens that should be part of the value of
		// the first token before it that contains an
		// equals sign (=)
		String tokensToAdd = "";
		int numTokens = tokens.size();
		for (int i=numTokens - 1; i >= 0; i--) {
			String token = (String) tokens.get(i);
			int equalsIndex = token.indexOf('=');

			// if this token has an equals sign (=) in it
			if (equalsIndex != -1) {
				String name = token.substring(0,equalsIndex).trim();
				// make sure we aren't using a cookie's attribute other
				// than the name/value pair
				if ( !isStringCookieAttribute(name,version) ) {
					String value = token.substring(equalsIndex+1).trim();
					_newCookies.put(name,value+tokensToAdd);
				}
				tokensToAdd = "";
			}

			else {
				// make sure we aren't counting a one word reserved
				// cookie attribute value
				if ( !isTokenReservedWord(token,version) ) {
					tokensToAdd =  token + tokensToAdd;
					String preceedingToken = (String) tokens.get(i - 1);
					char lastChar = preceedingToken.charAt(preceedingToken.length()-1);
					if (lastChar != '=') {
						tokensToAdd = ","+ tokensToAdd;
					}
				}
				// the token is a secure or discard flag for the cookie
				else {
					// just to be safe we should clear the tokens
					// to append to the value of the cookie
					tokensToAdd = "";
				}
			}
		}
	}


	/**
	 * Tokenizes a cookie header and returns the tokens in a
	 * <code>Vector</code>.
	 **/
	private Vector getCookieTokens(String cookieHeader) {
		StringReader sr = new StringReader(cookieHeader);
		StreamTokenizer st = new StreamTokenizer(sr);
		Vector tokens = new Vector();

		// clear syntax tables of the StreamTokenizer
		st.resetSyntax();

		// set all characters as word characters
		st.wordChars(0,Character.MAX_VALUE);

		// set up characters for quoting
		st.quoteChar(34); //double quotes
		st.quoteChar(39); //single quotes

		// set up characters to separate tokens
		st.whitespaceChars(59,59); //semicolon
		st.whitespaceChars(44,44); //comma

		try {
			while (st.nextToken() != StreamTokenizer.TT_EOF) {
				tokens.add( st.sval.trim() );
			}
		}
		catch (IOException ioe) {
			// this will never happen with a StringReader
		}
		sr.close();
		return tokens;
	}


	private boolean isStringCookieAttribute(String string,
	                                           int version) {
		String stringLowercase = string.toLowerCase();
		if (version == IETF_RFC2109) {
			if ( stringLowercase.equals("path") ||
				 stringLowercase.equals("domain") ||
				 stringLowercase.equals("expires") ||
				 stringLowercase.equals("comment") ||
				 stringLowercase.equals("max-age") ||
				 stringLowercase.equals("version") ) {
				return true;
			}
		}
		else if (version == IETF_RFC2965) {
			if ( stringLowercase.equals("path") ||
				 stringLowercase.equals("domain") ||
				 stringLowercase.equals("comment") ||
				 stringLowercase.equals("commenturl") ||
				 stringLowercase.equals("max-age") ||
				 stringLowercase.equals("version") ||
				 stringLowercase.equals("$version") ||
				 stringLowercase.equals("port") ) {
				return true;
			}
		}
		return false;
	}


	private boolean isTokenReservedWord(String token,
	                                       int version) {
		String tokenLowercase = token.toLowerCase();
		if (version == IETF_RFC2109) {
			if ( tokenLowercase.equals("secure") ) {
				return true;
			}
		}
		else if (version == IETF_RFC2965) {
			if ( tokenLowercase.equals("discard") ||
				 tokenLowercase.equals("secure") ) {
				return true;
			}
		}
		return false;
	}


    private void readContentTypeHeader() {
        String contentHeader = (_contentHeader != null) ? _contentHeader
                                                        : getHeaderField( "Content-type" );
        if (contentHeader == null) contentHeader = DEFAULT_CONTENT_HEADER;
        String[] parts = HttpUnitUtils.parseContentTypeHeader( contentHeader );
        _contentType = parts[0];
        if (parts[1] != null) _characterSet = parts[1];
    }


    private WebFrame[] getFrames() throws SAXException {
        if (_frames == null) {
            Vector list = new Vector();
            addFrameTags( list, "frame" );
            _frames = new WebFrame[ list.size() ];
            list.copyInto( _frames );
        }

        return _frames;
    }


    private void addFrameTags( Vector list, String frameTagName ) throws SAXException {
        NodeList nl = NodeUtils.getElementsByTagName( getReceivedPage().getDOM(), frameTagName );
        for (int i = 0; i < nl.getLength(); i++) {
            Node child = nl.item(i);
            list.addElement( new WebFrame( getReceivedPage().getBaseURL(), child ) );
        }
    }


    private ReceivedPage getReceivedPage() throws SAXException {
        if (_page == null) {
            try {
                if (!isHTML()) throw new NotHTMLException( getContentType() );
                _page = new ReceivedPage( _url, _target, getText(), getCharacterSet() );
            } catch (IOException e) {
                throw new SAXException( e );
            }
        }
        return _page;
    }


    private Document getXMLDOM() throws SAXException {
        Document doc = null;

        try {
            Class parserClass = Class.forName("org.apache.xerces.parsers.DOMParser");
            Constructor constructor = parserClass.getConstructor( null );
            Object parser = constructor.newInstance( null );

            Class[] parseMethodArgTypes = { InputSource.class };
            Object[] parseMethodArgs = { new InputSource( new StringReader( getText() ) ) };
            Method parseMethod = parserClass.getMethod( "parse", parseMethodArgTypes );
            parseMethod.invoke( parser, parseMethodArgs );

            Method getDocumentMethod = parserClass.getMethod( "getDocument", null );
            doc = (Document)getDocumentMethod.invoke( parser, null );
        } catch (IOException e) {
            throw new SAXException( e );
        } catch (InvocationTargetException ex) {
            Throwable tex = ex.getTargetException();
            if (tex  instanceof SAXException) {
                throw (SAXException)tex;
            } else if (tex instanceof IOException) {
                throw new RuntimeException( tex.toString() );
            } else {
                throw new IllegalStateException( "unexpected exception" );
            }
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException( "parse method not found" );
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException( "parse method not public" );
        } catch (InstantiationException ex) {
            throw new IllegalStateException( "error instantiating parser" );
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException( "parser class not found" );
        }

        return doc;
   }


}


//=======================================================================================

class ByteTag {

    ByteTag( byte[] buffer, int start, int length ) throws UnsupportedEncodingException {
        _buffer = new String( buffer, start, length, "iso-8859-1" ).toCharArray();
        _name = nextToken();

        String attribute = "";
        String token = nextToken();
        while (token.length() != 0) {
            if (token.equals( "=" ) && attribute.length() != 0) {
                _attributes.put( attribute.toLowerCase(), nextToken() );
                attribute = "";
            } else {
                if (attribute.length() > 0) _attributes.put( attribute.toLowerCase(), "" );
                attribute = token;
            }
            token = nextToken();
        }
    }


    public String getName() {
        return _name;
    }

    public String getAttribute( String attributeName ) {
        return (String) _attributes.get( attributeName );
    }

    public String toString() {
        return "ByteTag[ name=" + _name + ";attributes = " + _attributes + ']';
    }

    private String _name = "";
    private Hashtable _attributes = new Hashtable();


    private char[] _buffer;
    private int    _start;
    private int    _end = -1;


    private String nextToken() {
        _start = _end+1;
        while (_start < _buffer.length && Character.isWhitespace( _buffer[ _start ] )) _start++;
        if (_start >= _buffer.length) {
            return "";
        } else if (_buffer[ _start ] == '"') {
            for (_end = _start+1; _end < _buffer.length && _buffer[ _end ] != '"'; _end++);
            return new String( _buffer, _start+1, _end-_start-1 );
        } else if (_buffer[ _start ] == '\'') {
            for (_end = _start+1; _end < _buffer.length && _buffer[ _end ] != '\''; _end++);
            return new String( _buffer, _start+1, _end-_start-1 );
        } else if (_buffer[ _start ] == '=') {
            _end = _start;
            return "=";
        } else {
            for (_end = _start+1; _end < _buffer.length && _buffer[ _end ] != '=' && !Character.isWhitespace( _buffer[ _end ] ); _end++);
            return new String( _buffer, _start, (_end--)-_start );
        }
    }
}


//=======================================================================================


class ByteTagParser {

    ByteTagParser( byte[] buffer ) {
        _buffer = buffer;
    }


    ByteTag getNextTag() throws UnsupportedEncodingException {
        _start = _end+1;
        while (_start < _buffer.length && _buffer[ _start ] != '<') _start++;
        for (_end =_start+1; _end < _buffer.length && _buffer[ _end ] != '>'; _end++);
        if (_end >= _buffer.length || _end < _start) return null;
        return new ByteTag( _buffer, _start+1, _end-_start-1 );
    }


    private int _start = 0;
    private int _end   = -1;

    private byte[] _buffer;
}


//=======================================================================================


class DefaultWebResponse extends WebResponse {


    DefaultWebResponse( String text ) {
        super( "", null );
        _responseText = text;
    }


    /**
     * Returns the response code associated with this response.
     **/
    public int getResponseCode() {
        return HttpURLConnection.HTTP_OK;
    }


    public String[] getHeaderFieldNames() {
        return new String[] { "Content-type" };
    }


    /**
     * Returns the value for the specified header field. If no such field is defined, will return null.
     **/
    public String getHeaderField( String fieldName ) {
        if (fieldName.equals( "Content-type" )) {
            return "text/html; charset=us-ascii";
        } else {
            return null;
        }
    }


    /**
     * Returns the text of the response (excluding headers) as a string. Use this method in preference to 'toString'
     * which may be used to represent internal state of this object.
     **/
    public String getText() {
        return _responseText;
    }


    /**
     * Returns an input stream for reading the contents of this reply.
     **/
    public InputStream getInputStream() {
        return new ByteArrayInputStream( _responseText.getBytes() );
    }


    public String toString() {
        return "DefaultWebResponse [" + _responseText + "]";
    }


    private String _responseText;
}


//==================================================================================================


class NotHTMLException extends RuntimeException {

    NotHTMLException( String contentType ) {
        _contentType = contentType;
    }


    public String getMessage() {
        return "The content type of the response is '" + _contentType + "': it must be 'text/html' in order to be recognized as HTML";
    }


    private String _contentType;
}

