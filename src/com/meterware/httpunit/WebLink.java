package com.meterware.httpunit;

import java.net.URL;

import org.w3c.dom.Node;

/**
 * This class represents a link in an HTML page. Users of this class may examine the 
 * structure of the link (as a DOM), or create a {@tag WebRequest} to simulate clicking
 * on the link.
 **/
public class WebLink extends WebRequestSource {


    /**
     * Creates and returns a web request which will simulate clicking on this link.
     **/
    public WebRequest getRequest() {
        WebRequest request = new GetMethodWebRequest( getBaseURL(), getURLString(), getTarget() );
	    request.setRequestHeader( "Referer", getBaseURL().toExternalForm() );
	    return request;
    }


    /**
     * Returns the URL referenced by this link. This may be a relative URL.
     **/
    public String getURLString() {
        return NodeUtils.getNodeAttribute( getNode(), "href" );
    }


    /**
     * Returns the text value of this link.
     **/
    public String asText() {
        if (getNode().getNodeName().equals( "area" )) {
            return NodeUtils.getNodeAttribute( getNode(), "alt" );
        } else if (!getNode().hasChildNodes()) {
            return "";
        } else {
            return NodeUtils.asText( getNode().getChildNodes() );
        }
    }


//---------------------------------- package members --------------------------------


    /**
     * Contructs a web link given the URL of its source page and the DOM extracted
     * from that page.
     **/
    WebLink( URL baseURL, String parentTarget, Node node ) {
        super( node, baseURL, parentTarget );
    }

}

