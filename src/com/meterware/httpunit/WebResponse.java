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

import com.meterware.httpunit.scripting.ScriptableDelegate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.LinkedList;
import java.util.zip.GZIPInputStream;

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
 * @author <a href="mailto:dglo@ssec.wisc.edu">Dave Glowacki</a>
 * @author <a href="mailto:bx@bigfoot.com">Benoit Xhenseval</a>
 **/
abstract
public class WebResponse implements HTMLSegment {

    /**
     * Returns a web response built from a URL connection. Provided to allow
     * access to WebResponse parsing without using a WebClient.
     **/
    public static WebResponse newResponse( URLConnection connection ) throws IOException {
        return new HttpWebResponse( null, "_top", connection.getURL(), connection, HttpUnitOptions.getExceptionsThrownOnErrorStatus() );
    }


    /**
     * Returns true if the response is HTML.
     **/
    public boolean isHTML() {
        return getContentType().equalsIgnoreCase( HTML_CONTENT );
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
     * Returns the stylesheet linked in the head of the page.
     * <code>
     * <link type="text/css" rel="stylesheet" href="/mystyle.css" />
     * </code>
     * will return "/mystyle.css".
     * @exception SAXException thrown if there is an error parsing this response
     **/
    public String getExternalStyleSheet() throws SAXException {
        return getReceivedPage().getExternalStyleSheet();
    }

    /**
     * Retrieves the "content" of the meta tags for a key pair attribute-attributeValue.
     * <code>
     *  <meta name="robots" content="index" />
     *  <meta name="robots" content="follow" />
     *  <meta http-equiv="Expires" content="now" />
     * </code>
     * this can be used like this
     * <code>
     *      getMetaTagContent("name","robots") will return { "index","follow" }
     *      getMetaTagContent("http-equiv","Expires") will return { "now" }
     * </code>
     * @exception SAXException thrown if there is an error parsing this response
     **/
    public String[] getMetaTagContent(String attribute, String attributeValue) throws SAXException {
        return getReceivedPage().getMetaTagContent(attribute, attributeValue);
    }

    /**
     * Returns the target of the page.
     **/
    public String getTarget() {
        return _frameName;
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
     * Returns the response message associated with this response.
     **/
    abstract
    public String getResponseMessage();


    /**
     * Returns the content length of this response.
     * @return the content length, if known, or -1.
     */
    public int getContentLength() {
        if (_contentLength == UNINITIALIZED_INT) {
            String length = getHeaderField( "Content-Length" );
            _contentLength = (length == null) ? -1 : Integer.parseInt( length );
        }
        return _contentLength;
       }


    /**
     * Returns the content type of this response.
     **/
    public String getContentType() {
        if (_contentType == null) readContentTypeHeader();
        return _contentType;
    }


    /**
     * Returns the character set used in this response.
     **/
    public String getCharacterSet() {
        if (_characterSet == null) {
            readContentTypeHeader();
            if (_characterSet == null) _characterSet = getHeaderField( "Charset" );
            if (_characterSet == null) _characterSet = HttpUnitOptions.getDefaultCharacterSet();
            try {
                "abcd".getBytes( _characterSet );
            } catch (UnsupportedEncodingException e) {
                _characterSet = getDefaultEncoding();
            }
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
     * If more than one header is defined for the specified name, returns only the first found.
     **/
    abstract
    public String getHeaderField( String fieldName );


    /**
     * Returns the values for the specified header field. If no such field is defined, will return an empty array.
     **/
    abstract
    public String[] getHeaderFields( String fieldName );


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


    /**
     * Returns the contents of the specified subframe of this frameset response.
     *
     * @param subFrameName the name of the desired frame as defined in the frameset.
     **/
    public WebResponse getSubframeContents( String subFrameName ) {
        if (_client == null) throw new NoSuchFrameException( subFrameName );
        return _client.getFrameContents( WebFrame.getNestedFrameName( _frameName, subFrameName ) );
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
     * Returns the link found in the page with the specified name.
     * @exception SAXException thrown if there is an error parsing the response.
     **/
    public WebLink getLinkWithName( String name ) throws SAXException {
        return getReceivedPage().getLinkWithName( name );
    }


    /**
     * Returns the link found in the page with the specified ID.
     * @exception SAXException thrown if there is an error parsing the response.
     **/
    public WebLink getLinkWithID( String ID ) throws SAXException {
        return getReceivedPage().getLinkWithID( ID );
    }


    /**
     * Returns the images found in the page in the order in which they appear.
     * @exception SAXException thrown if there is an error parsing the response.
     **/
    public WebImage[] getImages() throws SAXException {
        return getReceivedPage().getImages();
    }


    /**
     * Returns the image found in the page with the specified name attribute.
     * @exception SAXException thrown if there is an error parsing the response.
     **/
    public WebImage getImageWithName( String source ) throws SAXException {
        return getReceivedPage().getImageWithName( source );
    }


    /**
     * Returns the image found in the page with the specified src attribute.
     * @exception SAXException thrown if there is an error parsing the response.
     **/
    public WebImage getImageWithSource( String source ) throws SAXException {
        return getReceivedPage().getImageWithSource( source );
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
            try {
                return HttpUnitUtils.newParser().parse( new InputSource( new StringReader( getText() ) ) );
            } catch (IOException e) {
                throw new SAXException( e );
            }
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


//---------------------------------------- JavaScript methods ----------------------------------------

    private LinkedList _alerts = new LinkedList();

    /**
     * Returns the next javascript alert without removing it from the queue.
     */
    public String getNextAlert() {
        return _alerts.isEmpty() ? null : (String) _alerts.getFirst();
    }


    /**
     * Returns the next javascript alert and removes it from the queue.
     */
    public String popNextAlert() {
        if (_alerts.isEmpty()) throw new IllegalStateException( "Tried to pop a non-existent alert" );
        return (String) _alerts.removeFirst();
    }


    public Scriptable getScriptableObject() {
        return new Scriptable();
    }


    public static ScriptableDelegate newDelegate( String delegateClassName ) {
        if (delegateClassName.equalsIgnoreCase( "Option" )) {
            return new SelectionFormControl.Option();
        } else {
            throw new IllegalArgumentException( "No such scripting class supported: " + delegateClassName );
        }
    }


    public class Scriptable extends ScriptableDelegate {

        public void alert( String message ) {
            _alerts.addLast( message );
        }


        public HTMLPage.Scriptable getDocument() throws SAXException {
            return getReceivedPage().getScriptableObject();
        }


        public void load() throws SAXException {
            runScript( getReceivedPage().getScripts() );
            doEvent( getReceivedPage().getOnLoadEvent() );
        }
    }


//---------------------------------------- Object methods --------------------------------------------

    abstract
    public String toString();


//----------------------------------------- protected members -----------------------------------------------


    /**
     * Constructs a response object.
     * @param frameName the name of the frame to hold the response
     * @param url the url from which the response was received
     **/
    protected WebResponse( WebClient client, String frameName, URL url ) {
        _client = client;
        _url = url;
        _frameName = frameName;
    }


    final
    protected void defineRawInputStream( InputStream inputStream ) throws IOException {
        if (_inputStream != null || _responseText != null) {
            throw new IllegalStateException( "Must be called before response text is defined." );
        }

        if (encodedUsingGZIP()) {
            _inputStream = new GZIPInputStream( inputStream );
        } else {
            _inputStream = inputStream;
        }
    }


    private boolean encodedUsingGZIP() {
        String encoding = getHeaderField( "Content-Encoding" );
        return encoding != null && encoding.indexOf( "gzip" ) >= 0;
    }


    final
    protected void readRefreshRequest( String contentTypeHeader ) {
        int splitIndex = contentTypeHeader.indexOf( ';' );
        if (splitIndex < 0) splitIndex = 0;
        try {
            _refreshDelay = Integer.parseInt( contentTypeHeader.substring( 0, splitIndex ) );
            _refreshRequest = new GetMethodWebRequest( _url, getRefreshURL( contentTypeHeader.substring( splitIndex+1 ) ), _frameName );
        } catch (NumberFormatException e) {
            System.out.println( "Unable to interpret refresh tag: \"" + contentTypeHeader + '"' );
        }
    }


    private String getRefreshURL( String text ) {
        text = text.trim();
        if (!text.toUpperCase().startsWith( "URL" )) {
            return text;
        } else {
            int splitIndex = text.indexOf( '=' );
            return text.substring( splitIndex+1 ).trim();
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


    WebClient getClient() {
        return _client;
    }


//--------------------------------- private members --------------------------------------


    /**
     * A version flag indicating a cookie is based on the
     * Internet Engineering Task Force's (IETF)
     * <a href="http://www.ietf.org/rfc/rfc2109.txt">RFC 2109</a>
     *
     * <br />
     * These cookies come from the <code>Set-Cookie:</code> header
     **/
    final private static int IETF_RFC2109 = 0;

    /**
     * A version flag indicating a cookie is based on the
     * Internet Engineering Task Force's (IETF)
     * <a href="http://www.ietf.org/rfc/rfc2965.txt">RFC 2965</a>
     *
     * <br />
     * These cookies come from the <code>Set-Cookie2:</code> header
     **/
    final private static int IETF_RFC2965 = 1;

    final private static String HTML_CONTENT = "text/html";

    final private static int UNINITIALIZED_INT = -2;

    private WebFrame[] _frames;

    private HTMLPage _page;

    private String _contentHeader;

    private int _contentLength = UNINITIALIZED_INT;

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

    private String _frameName;

    private WebClient _client;


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

            if (HttpUnitOptions.isCheckContentLength() && getContentLength() >= 0 && bytes.length != getContentLength()) {
                throw new IOException("Truncated message. Expected length: " + getContentLength() +
                                                       ", Actual length: " + bytes.length);
            }
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
        if (isHttpEquivMetaTag( tag, "content-type" )) {
            inferContentType( tag.getAttribute( "content" ) );
        } else if (isHttpEquivMetaTag( tag, "refresh" )) {
            readRefreshRequest( tag.getAttribute( "content" ) );
        }
    }


    private boolean isHttpEquivMetaTag( ByteTag tag, String headerName )
    {
        return headerName.equalsIgnoreCase( tag.getAttribute( "http_equiv" ) ) ||
               headerName.equalsIgnoreCase( tag.getAttribute( "http-equiv" ) );
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
     * @return Hashtable a <code>Hashtable</code> of where the name of the
     *                    cookie is the key and the value of the cookie is
     *                    the value
     */
    private Hashtable getNewCookies() {
        if (_newCookies == null) _newCookies = new Hashtable();
        processCookieHeaders( getHeaderFields( "Set-Cookie" ), IETF_RFC2109 );
        processCookieHeaders( getHeaderFields( "Set-Cookie2" ), IETF_RFC2965 );
        return _newCookies;
    }


    private void processCookieHeaders( String cookieHeader[], int version ) {
        for (int i = 0; i < cookieHeader.length; i++) {
            processCookieHeader( cookieHeader[i], version );
        }
    }


    private void processCookieHeader( String cookieHeader, int version ) {
        Vector tokens = getCookieTokens( cookieHeader );
        String tokensToAdd = "";
        for (int i = tokens.size() - 1; i >= 0; i--) {
            String token = (String) tokens.elementAt( i );

            int equalsIndex = getEqualsIndex( token );
            if (equalsIndex != -1) {
                String name = token.substring( 0, equalsIndex ).trim();
                if (!isCookieAttribute( name, version )) {
                    String value = token.substring( equalsIndex + 1 ).trim();
                    _newCookies.put( name, value + tokensToAdd );
                }
                tokensToAdd = "";
            } else if (isCookieReservedWord( token, version )) {
                tokensToAdd = "";
            } else {
                tokensToAdd = token + tokensToAdd;
                if (i > 0) {
                    String preceedingToken = (String) tokens.elementAt( i - 1 );
                    char lastChar = preceedingToken.charAt( preceedingToken.length() - 1 );
                    if (lastChar != '=') {
                        tokensToAdd = "," + tokensToAdd;
                    }
                }
            }
        }
    }


    /**
     * Returns the index (if any) of the equals sign separating a cookie name from the its value.
     * Equals signs at the end of the token are ignored in this calculation, since they may be
     * part of a Base64-encoded value.
     */
    private int getEqualsIndex( String token ) {
        if (!token.endsWith( "==" )) {
            return token.indexOf( '=' );
        } else {
            return getEqualsIndex( token.substring( 0, token.length()-2 ) );
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
        st.quoteChar( '"' ); //double quotes
        st.quoteChar( '\'' ); //single quotes

        // set up characters to separate tokens
        st.whitespaceChars(59,59); //semicolon
        st.whitespaceChars(44,44); //comma

        try {
            while (st.nextToken() != StreamTokenizer.TT_EOF) {
                tokens.addElement( st.sval.trim() );
            }
        }
        catch (IOException ioe) {
            // this will never happen with a StringReader
        }
        sr.close();
        return tokens;
    }


    private boolean isCookieAttribute( String string, int version ) {
        String stringLowercase = string.toLowerCase();
        if (version == IETF_RFC2109) {
            return stringLowercase.equals("path") ||
                   stringLowercase.equals("domain") ||
                   stringLowercase.equals("expires") ||
                   stringLowercase.equals("comment") ||
                   stringLowercase.equals("max-age") ||
                   stringLowercase.equals("version");
        } else if (version == IETF_RFC2965) {
            return stringLowercase.equals("path") ||
                   stringLowercase.equals("domain") ||
                   stringLowercase.equals("comment") ||
                   stringLowercase.equals("commenturl") ||
                   stringLowercase.equals("max-age") ||
                   stringLowercase.equals("version") ||
                   stringLowercase.equals("$version") ||
                   stringLowercase.equals("port");
        } else {
            return false;
        }
    }


    private boolean isCookieReservedWord( String token,
                                         int version ) {
        if (version == IETF_RFC2109) {
            return token.equalsIgnoreCase( "secure" );
        } else if (version == IETF_RFC2965) {
            return token.equalsIgnoreCase( "discard" ) || token.equalsIgnoreCase( "secure" );
        } else {
            return false;
        }
    }


    private void readContentTypeHeader() {
        String contentHeader = (_contentHeader != null) ? _contentHeader
                                                        : getHeaderField( "Content-type" );
        if (contentHeader == null) {
            _contentType = HttpUnitOptions.getDefaultContentType();
            _characterSet = HttpUnitOptions.getDefaultCharacterSet();
            _contentHeader = _contentType + ";charset=" + _characterSet;
        } else {
            String[] parts = HttpUnitUtils.parseContentTypeHeader( contentHeader );
            _contentType = parts[0];
            if (parts[1] != null) _characterSet = parts[1];
        }
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
        NodeList nl = NodeUtils.getElementsByTagName( getReceivedPage().getOriginalDOM(), frameTagName );
        for (int i = 0; i < nl.getLength(); i++) {
            Node child = nl.item(i);
            list.addElement( new WebFrame( getReceivedPage().getBaseURL(), child, _frameName ) );
        }
    }


    private HTMLPage getReceivedPage() throws SAXException {
        if (_page == null) {
            try {
                if (!isHTML()) throw new NotHTMLException( getContentType() );
                _page = new HTMLPage( this, _url, _frameName, getText(), getCharacterSet() );
            } catch (IOException e) {
                throw new SAXException( e );
            }
        }
        return _page;
    }


    private static String _defaultEncoding;

    private final static String[] DEFAULT_ENCODING_CANDIDATES = { HttpUnitUtils.DEFAULT_CHARACTER_SET, "us-ascii", "utf-8", "utf8" };

    static String getDefaultEncoding() {
        if (_defaultEncoding == null) {
            for (int i = 0; i < DEFAULT_ENCODING_CANDIDATES.length; i++) {
                try {
                    _defaultEncoding = DEFAULT_ENCODING_CANDIDATES[i];
                    "abcd".getBytes( _defaultEncoding );   // throws an exception if the encoding is not supported
                    return _defaultEncoding;
                } catch (UnsupportedEncodingException e) {
                }
            }
        }
        return (_defaultEncoding = System.getProperty( "file.encoding" ));
    }



}


//=======================================================================================

class ByteTag {

    ByteTag( byte[] buffer, int start, int length ) throws UnsupportedEncodingException {
        _buffer = new String( buffer, start, length, WebResponse.getDefaultEncoding() ).toCharArray();
        _name = nextToken();

        String attribute = "";
        String token = nextToken();
        while (token.length() != 0) {
            if (token.equals( "=" ) && attribute.length() != 0) {
                getAttributes().put( attribute.toLowerCase(), nextToken() );
                attribute = "";
            } else {
                if (attribute.length() > 0) getAttributes().put( attribute.toLowerCase(), "" );
                attribute = token;
            }
            token = nextToken();
        }
    }


    public String getName() {
        return _name;
    }

    public String getAttribute( String attributeName ) {
        return (String) getAttributes().get( attributeName );
    }

    public String toString() {
        return "ByteTag[ name=" + _name + ";attributes = " + _attributes + ']';
    }


    private Hashtable getAttributes() {
        if (_attributes == null) _attributes = new Hashtable();
        return _attributes;
    }


    private String _name = "";
    private Hashtable _attributes;


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
        super( null, "", null );
        _responseText = text;
    }


    /**
     * Returns the response code associated with this response.
     **/
    public int getResponseCode() {
        return HttpURLConnection.HTTP_OK;
    }


    /**
     * Returns the response message associated with this response.
     **/
    public String getResponseMessage() {
        return "OK";
    }


    public String[] getHeaderFieldNames() {
        return new String[] { "Content-type" };
    }


    /**
     * Returns the value for the specified header field. If no such field is defined, will return null.
     **/
    public String getHeaderField( String fieldName ) {
        if (fieldName.equalsIgnoreCase( "Content-type" )) {
            return "text/html; charset=us-ascii";
        } else {
            return null;
        }
    }

    public String[] getHeaderFields( String fieldName ) {
        String value = getHeaderField( fieldName );
        return value == null ? new String[0] : new String[]{ value };
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

