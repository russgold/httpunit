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
import org.w3c.dom.Node;

/**
 * This class represents a suubmit button in an HTML form.
 **/
public class SubmitButton {

    public static SubmitButton UNNAMED_BUTTON = new SubmitButton();


    /**
     * Returns the name of this submit button.
     **/
    public String getName() {
        return _name;
    }


    /**
     * Returns the value associated with this submit button.
     **/
    public String getValue() {
        return _value;
    }


    /**
     * Returns the ID associated with the button, if any.
     * @return the button ID, or an empty string if no ID is defined.
     **/
    public String getID() {
        return NodeUtils.getNodeAttribute( _node, "id" );
    }


    /**
     * Returns true if this submit button is an image map.
     **/
    public boolean isImageButton() {
        return _isImageButton;
    }


//------------------------------------ Object methods ----------------------------------------


    public String toString() {
        return "Submit with " + getName() + "=" + getValue();
    }


    public int hashCode() {
        return getName().hashCode() + getValue().hashCode();
    }


    public boolean equals( Object o ) {
        return getClass().equals( o.getClass() ) && equals( (SubmitButton) o );
    }


//------------------------------------------ package members ----------------------------------


    SubmitButton( Node node ) {
        _node  = node;
        _name  = NodeUtils.getNodeAttribute( node, "name" );
        _value = NodeUtils.getNodeAttribute( _node, "value" );
        _isImageButton = NodeUtils.getNodeAttribute( _node, "type" ).equalsIgnoreCase( "image" );
    }


//------------------------------------------ private members ----------------------------------

    private Node    _node;
    private String  _name;
    private String  _value;
    private boolean _isImageButton;


    private SubmitButton() {
        _name  = "";
        _value = "";
    }


    private boolean equals( SubmitButton button ) {
        return getName().equals( button.getName() ) &&
                  (getName().length() == 0 || getValue().equals( button.getValue() ));
    }
}

