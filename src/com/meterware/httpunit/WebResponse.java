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
import java.io.StringReader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.net.URL;
import java.net.HttpURLConnection;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A response from a web server to a web request.
 **/
abstract
public class WebResponse implements HTMLSegment {


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
     * @exception SAXException thrown if there is an error parsing this response
     **/
    public String getTarget() throws SAXException {
        return _target;
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
     * Returns the value for the specified header field. If no such field is defined, will return null.
     * No more than one header may be defined for each key.  
     **/
    abstract
    public String getHeaderField( String fieldName );
    
    
    /**
     * Returns the text of the response (excluding headers) as a string. Use this method in preference to 'toString'
     * which may be used to represent internal state of this object.
     **/
    abstract
    public String getText();
    
    
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


    public String toString() {
        return getText();
    }


//----------------------------------------- protected members -----------------------------------------------


    /**
     * Constructs a response object.
     * @param url the url from which the response was received
     * @param inputStream the input stream from which the response can be read
     **/
    protected WebResponse( String target, URL url ) {
        _url = url;
        _target = target;
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

    private String _contentType;

    private String _characterSet;

    private Hashtable _newCookies;


    // the following variables are essentially final; however, the JDK 1.1 compiler does not handle blank final variables properly with
    // multiple constructors that call each other, so the final qualifiers have been removed.

    private URL    _url;

    private String _target;


    private Hashtable getNewCookies() {
        if (_newCookies == null) {
            _newCookies = new Hashtable();
            String cookieHeader = getHeaderField( "Set-Cookie" );
            if (cookieHeader != null) {
                StringTokenizer st = new StringTokenizer( cookieHeader, "," );
                while (st.hasMoreTokens()) recognizeOneCookie( st.nextToken() );
            }
        }
        return _newCookies;
    }



    private void recognizeOneCookie( String cookieSpec ) {
        StringTokenizer st = new StringTokenizer( cookieSpec, ";" );
        String token = st.nextToken().trim();
        int i = token.indexOf("=");
        if (i > -1) {
            String name = token.substring(0, i).trim();
            String value = stripQuote( token.substring( i+1, token.length() ).trim() );
	    _newCookies.put( name, value );
	}
    }


    private String stripQuote( String value ) {
        if (((value.startsWith("\"")) && (value.endsWith("\""))) ||
            ((value.startsWith("'") && (value.endsWith("'"))))) {
            return value.substring(1,value.length()-1);
        }
        return value;
   }


    private void readContentTypeHeader() {
        String contentHeader = getHeaderField( "Content-type" );
        if (contentHeader == null) contentHeader = DEFAULT_CONTENT_HEADER;
        StringTokenizer st = new StringTokenizer( contentHeader, ";=" );
        _contentType = st.nextToken();
        while (st.hasMoreTokens()) {
            String parameter = st.nextToken();
            if (st.hasMoreTokens()) {
                String value = st.nextToken();
                if (parameter.trim().equalsIgnoreCase( "charset" )) _characterSet = value;
            }
        }
    }


    private WebFrame[] getFrames() throws SAXException {
        if (_frames == null) {
            NodeList nl = NodeUtils.getElementsByTagName( getReceivedPage().getDOM(), "frame" );
            Vector list = new Vector();
            for (int i = 0; i < nl.getLength(); i++) {
                Node child = nl.item(i);
                list.addElement( new WebFrame( getReceivedPage().getBaseURL(), child ) );
            }
            _frames = new WebFrame[ list.size() ];
            list.copyInto( _frames );
        }

        return _frames;
    }


    private ReceivedPage getReceivedPage() throws SAXException {
        if (_page == null) {
            if (!isHTML()) throw new NotHTMLException( getContentType() );
            _page = new ReceivedPage( _url, _target, getText(), getCharacterSet() );
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

