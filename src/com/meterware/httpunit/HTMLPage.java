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
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Vector;

import org.w3c.dom.*;

import org.w3c.tidy.Tidy;

import org.xml.sax.SAXException;


/**
 * This class represents an HTML page returned from a request.
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 * @author <a href="mailto:bx@bigfoot.com">Benoit Xhenseval</a>
 **/
public class HTMLPage extends ParsedHTML {


    public HTMLPage( WebResponse response, URL url, String parentTarget, String pageText, String characterSet ) throws SAXException {
        super( response, url, parentTarget, getDOM( url, pageText ), characterSet );
        setBaseAttributes();
    }


    /**
     * Returns the title of the page.
     **/
    public String getTitle() throws SAXException {
        NodeList nl = ((Document) getOriginalDOM()).getElementsByTagName( "title" );
        if (nl.getLength() == 0) return "";
        if (!nl.item(0).hasChildNodes()) return "";
        return nl.item(0).getFirstChild().getNodeValue();
    }


    /**
     * Returns the onLoad event script.
     */
    public String getOnLoadEvent() throws SAXException {
        NodeList nl = ((Document) getOriginalDOM()).getElementsByTagName( "body" );
        if (nl.getLength() == 0) return "";
        return ((Element) nl.item(0)).getAttribute( "onload" );
    }


    /**
     * Returns the contents of any script tags on the page, concatenated.
     */
    public String getScripts() throws SAXException {
        NodeList nl = ((Document) getOriginalDOM()).getElementsByTagName( "script" );
        return NodeUtils.asText( nl );
    }


    /**
     * Returns the location of the linked stylesheet in the head
     * <code>
     * <link type="text/css" rel="stylesheet" href="/mystyle.css" />
     * </code>
     * @author <a href="mailto:bx@bigfoot.com">Benoit Xhenseval</a>
     **/
    public String getExternalStyleSheet() throws SAXException {
        NodeList nl = ((Document) getOriginalDOM()).getElementsByTagName( "link" );
        int length = nl.getLength();
        if (length == 0) return "";

        for (int i = 0; i < length; i++) {
            if ("stylesheet".equalsIgnoreCase(NodeUtils.getNodeAttribute( nl.item(i), "rel" )))
                return NodeUtils.getNodeAttribute( nl.item(i), "href" );
        }
        return "";
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
     **/
    public String[] getMetaTagContent(String attribute, String attributeValue) {
        Vector matches = new Vector();
        NodeList nl = ((Document) getOriginalDOM()).getElementsByTagName("meta");
        int length = nl.getLength();

        for (int i = 0; i < length; i++) {
            if (attributeValue.equalsIgnoreCase(NodeUtils.getNodeAttribute(nl.item(i), attribute))) {
                matches.addElement( NodeUtils.getNodeAttribute( nl.item(i), "content" ) );
            }
        }
        String[] result = new String[ matches.size() ];
        matches.copyInto( result );
        return result;
    }


    public class Scriptable extends ScriptableObject {

        public Object get( String propertyName ) {
            WebForm wf = getFormWithName( propertyName );
            if (wf != null) return wf.getScriptableObject();

            WebLink wl = getLinkWithName( propertyName );
            if (wl != null) return wl.getScriptableObject();

            WebImage wi = getImageWithName( propertyName );
            if (wi != null) return wi.getScriptableObject();

            return super.get( propertyName );
        }


        public String getTitle() throws SAXException {
            return HTMLPage.this.getTitle();
        }


        public WebLink.Scriptable[] getLinks() {
            WebLink[] links = HTMLPage.this.getLinks();
            WebLink.Scriptable[] result = new WebLink.Scriptable[ links.length ];
            for (int i = 0; i < links.length; i++) {
                result[i] = links[i].getScriptableObject();
            }
            return result;
        }


        public WebForm.Scriptable[] getForms() {
            WebForm[] forms = HTMLPage.this.getForms();
            WebForm.Scriptable[] result = new WebForm.Scriptable[ forms.length ];
            for (int i = 0; i < forms.length; i++) {
                result[i] = forms[i].getScriptableObject();
            }
            return result;
        }


        public WebImage.Scriptable[] getImages() {
            WebImage[] images = HTMLPage.this.getImages();
            WebImage.Scriptable[] result = new WebImage.Scriptable[ images.length ];
            for (int i = 0; i < images.length; i++) {
                result[i] = images[i].getScriptableObject();
            }
            return result;
        }


        Scriptable() {}
    }


    Scriptable getScriptableObject() {
        return new Scriptable();
    }

//---------------------------------- private members --------------------------------


    private static Node getDOM( URL url, String pageText ) throws SAXException {
        try {
            return getParser( url ).parseDOM( new ByteArrayInputStream( pageText.getBytes( getUTFEncodingName() ) ), null );
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException( "UTF-8 encoding failed" );
        }
    }


    private static String _utfEncodingName;

    private static String getUTFEncodingName() {
        if (_utfEncodingName == null) {
            String versionNum = System.getProperty( "java.version" );
            if (versionNum.startsWith( "1.1" )) _utfEncodingName = "UTF8";
            else _utfEncodingName = "UTF-8";
        }
        return _utfEncodingName;
    }


    private void setBaseAttributes() throws SAXException {
        NodeList nl = ((Document) getOriginalDOM()).getElementsByTagName( "base" );
        if (nl.getLength() == 0) return;
        try {
            applyBaseAttributes( NodeUtils.getNodeAttribute( nl.item(0), "href" ),
                                 NodeUtils.getNodeAttribute( nl.item(0), "target" ) );
        } catch (MalformedURLException e) {
            throw new RuntimeException( "Unable to set document base: " + e );
        }
    }


    private void applyBaseAttributes( String baseURLString, String baseTarget ) throws MalformedURLException {
        if (baseURLString.length() > 0) {
            this.setBaseURL( new URL( baseURLString ) );
        }
        if (baseTarget.length() > 0) {
            this.setBaseTarget( baseTarget );
        }
    }


    private static Tidy getParser( URL url ) {
        Tidy tidy = new Tidy();
        tidy.setCharEncoding( org.w3c.tidy.Configuration.UTF8 );
        tidy.setQuiet( true );
        tidy.setShowWarnings( HttpUnitOptions.getParserWarningsEnabled() );
        if (!HttpUnitOptions.getHtmlErrorListeners().isEmpty()) {
            tidy.setErrout(new JTidyPrintWriter( url ));
        }
        return tidy;
    }

}