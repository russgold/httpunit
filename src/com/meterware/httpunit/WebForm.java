package com.meterware.httpunit;

import java.net.URL;

import java.util.Vector;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class represents a form in an HTML page. Users of this class may examine the parameters
 * defined for the form, the structure of the form (as a DOM), or the text of the form. They
 * may also create a {@link WebRequest} to simulate the submission of the form.
 **/
public class WebForm {


    /**
     * Returns an array containing the names of the parameters defined for this form,
     * in the order in which they appear.
     **/
    public String[] getParameterNames() {
        NamedNodeMap[] parameters = getParameters();
        String[] result = new String[ parameters.length ];

        for (int i = 0; i < result.length; i++) {
            result[i] = getValue( parameters[i].getNamedItem( "name" ) );
        }
        return result;
    }


    /**
     * Creates and returns a web request which will simulate the submission of this form.
     **/
    public WebRequest getRequest() {
        NamedNodeMap nnm = _node.getAttributes();
        String action = getValue( nnm.getNamedItem( "action" ) );
        WebRequest result;

        if (getValue( nnm.getNamedItem( "method" ) ).equalsIgnoreCase( "post" )) {
            result = new PostMethodWebRequest( _baseURL, action );
        } else {
            result = new GetMethodWebRequest( _baseURL, action );
        }

        NamedNodeMap[] parameters = getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getNamedItem( "value" ) != null) {
                result.setParameter( getValue( parameters[i].getNamedItem( "name" ) ),
                                     getValue( parameters[i].getNamedItem( "value" ) ) );
            }
        }
        return result;

    }


    /**
     * Returns a copy of the domain object model subtree associated with this form.
     **/
    public Node getDOMSubtree() {
        return _node.cloneNode( /* deep */ true );
    }


//---------------------------------- package members --------------------------------

    /**
     * Contructs a web form given the URL of its source page and the DOM extracted
     * from that page.
     **/
    WebForm( URL baseURL, Node node ) {
        _node    = node;
        _baseURL = baseURL;
    }


//---------------------------------- private members --------------------------------


    /** The URL of the page containing this form. **/
    private URL            _baseURL;

    /** The DOM node representing the form. **/
    private Node           _node;

    /** The attributes of the form parameters. **/
    private NamedNodeMap[] _parameters;


    private String getValue( Node node ) {
        return (node == null) ? "" : node.getNodeValue();
    }


    private NamedNodeMap[] getParameters() {
        if (_parameters == null) {
            Vector list = new Vector();
            addFormParametersToList( _node.getChildNodes(), list );
            _parameters = new NamedNodeMap[ list.size() ];
            list.copyInto( _parameters );
        }

        return _parameters;
    }


    private void addFormParametersToList( NodeList children, Vector list ) {
        for (int i = 0; i < children.getLength(); i++) {
            addFormParametersToList( children.item(i), list );
        }
    }


    private void addFormParametersToList( Node child, Vector list ) {
        if (isFormParameter( child )) {
            list.addElement( child.getAttributes() );
        } else if (child.hasChildNodes()) {
            addFormParametersToList( child.getChildNodes(), list );
        }
    }


    private boolean isFormParameter( Node node ) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return false;
        } else if (!node.getNodeName().equals( "input" )) {
            return false;
        } else {
            NamedNodeMap nnm = node.getAttributes();
            Node n = nnm.getNamedItem( "type" );
            if (n == null) {
                return true;
            } else if (n.getNodeValue().equalsIgnoreCase( "submit" )) {
                return false;
            } else if (n.getNodeValue().equalsIgnoreCase( "reset" )) {
                return false;
            } else {
                return true;
            }
        }
    }


}
