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
import java.io.IOException;

import org.w3c.dom.Node;

/**
 * This class represents a submit button in an HTML form.
 **/
public class SubmitButton extends FormControl {


    public static SubmitButton UNNAMED_BUTTON = new SubmitButton();


    /**
     * Returns the value associated with this submit button.
     **/
    public String getValue() {
        return getValueAttribute();
    }


    /**
     * Returns the ID associated with the button, if any.
     * @return the button ID, or an empty string if no ID is defined.
     **/
    public String getID() {
        return _id;
    }


    /**
     * Returns true if this submit button is an image map.
     **/
    public boolean isImageButton() {
        return _isImageButton;
    }


    /**
     * Returns the current value(s) associated with this control. These values will be transmitted to the server
     * if the control is 'successful'.
     **/
    public String[] getValues() {
        return (isDisabled() || !_pressed) ? NO_VALUE : toArray( getValueAttribute() );
    }


    void addValues( ParameterProcessor processor, String characterSet ) throws IOException {
        if (_pressed && !isDisabled() && getName().length() > 0 && getValueAttribute().length() > 0) {
            processor.addParameter( getName(), getValueAttribute(), characterSet );
            if (_isImageButton) {
                processor.addParameter( getName() + ".x", Integer.toString( _x ), characterSet );
                processor.addParameter( getName() + ".y", Integer.toString( _y ), characterSet );
            }
        }
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


    SubmitButton( WebForm form, Node node ) {
        super( node );
        _isImageButton = NodeUtils.getNodeAttribute( node, "type" ).equalsIgnoreCase( "image" );
        _id = NodeUtils.getNodeAttribute( node, "id" );
    }


    public void setPressed( boolean pressed ) {
        _pressed = pressed;
    }


    public void setLocation( int x, int y ) {
        _x = x;
        _y = y;
    }


//------------------------------------------ private members ----------------------------------


    private final String   _id;
    private final boolean  _isImageButton;
    private       boolean  _pressed;
    private       int      _x;
    private       int      _y;
    private       String[] _value = new String[1];


    private SubmitButton() {
        _id            = "";
        _isImageButton = false;
    }


    private String[] toArray( String value ) {
        _value[0] = value;
        return _value;
    }


    private boolean equals( SubmitButton button ) {
        return getName().equals( button.getName() ) &&
                  (getName().length() == 0 || getValue().equals( button.getValue() ));
    }
}

