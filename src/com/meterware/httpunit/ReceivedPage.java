package com.meterware.httpunit;

import java.io.ByteArrayInputStream;

import java.net.URL;

import java.util.Vector;

import org.w3c.dom.*;

import org.w3c.tidy.Tidy;

import org.xml.sax.SAXException;


/**
 * This class represents an HTML page returned from a request.
 **/
class ReceivedPage {


    public ReceivedPage( URL url, String pageText ) throws SAXException {
        _url = url;

        Tidy tidy = new Tidy();
        tidy.setQuiet( true );
        _document = tidy.parseDOM( new ByteArrayInputStream( pageText.getBytes() ), null ); 
    }


    /**
     * Returns the forms found in the page in the order in which they appear.
     **/
    public WebForm[] getForms() {
        NodeList forms = _document.getElementsByTagName( "form" );
        WebForm[] result = new WebForm[ forms.getLength() ];
        for (int i = 0; i < result.length; i++) {
            result[i] = new WebForm( getURL(), forms.item( i ) );
        }

        return result;
    }


    /**
     * Returns the links found in the page in the order in which they appear.
     **/
    public WebLink[] getLinks() {
        if (_links == null) {
            NodeList nl = _document.getElementsByTagName( "a" );
            Vector list = new Vector();
            for (int i = 0; i < nl.getLength(); i++) {
                Node child = nl.item(i);
                if (isLinkAnchor( child )) {
                    list.addElement( new WebLink( _url, child ) );
                }
            }
            _links = new WebLink[ list.size() ];
            list.copyInto( _links );
        }

        return _links;
    }


    /**
     * Returns a copy of the domain object model associated with this page.
     **/
    public Document getDOM() {
        return (Document) _document.cloneNode( /* deep */ true );
    }



    /**
     * Returns the associated URL.
     **/
    public URL getURL() {
        return _url;
    }


//---------------------------------- Object methods --------------------------------


    public String toString() {
        return _url.toExternalForm() + System.getProperty( "line.separator" ) +
               _document;
    }

//---------------------------------- private members --------------------------------

    private Document _document;

    private URL _url;

    private WebLink[] _links;


    /**
     * Returns true if the node is a link anchor node.
     **/
    private boolean isLinkAnchor( Node node ) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return false;
        } else if (!node.getNodeName().equals( "a" )) {
            return false;
        } else {
            return (node.getAttributes().getNamedItem( "href" ) != null);
        }
    }


}
