package com.meterware.httpunit.dom;
/********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2004, Russell Gold
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
import org.w3c.dom.html.HTMLInputElement;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class HTMLInputElementImpl extends HTMLControl implements HTMLInputElement {

    private String _value;
    private Boolean _checked;
    private TypeSpecificBehavior _behavior = new EditableTextBehavior();

    ElementImpl create( DocumentImpl owner, String tagName ) {
        HTMLInputElementImpl element = new HTMLInputElementImpl();
        element.initialize( owner, tagName );
        return element;
    }


    public void blur() {
    }


    public void focus() {
    }


    public void click() {
        _behavior.click();
    }


    public void select() {
    }


    public String getAccept() {
        return getAttributeWithNoDefault( "accept" );
    }


    public String getAccessKey() {
        return getAttributeWithNoDefault( "accessKey" );
    }


    public String getAlign() {
        return getAttributeWithDefault( "align", "bottom" );
    }


    public String getAlt() {
        return getAttributeWithNoDefault( "alt" );
    }


    public boolean getChecked() {
        return _behavior.getChecked();
    }


    public boolean getDefaultChecked() {
        return getBooleanAttribute( "checked" );
    }


    public String getDefaultValue() {
        return getAttributeWithNoDefault( "value" );
    }


    public int getMaxLength() {
        return getIntegerAttribute( "maxlength" );
    }


    public String getSize() {
        return getAttributeWithNoDefault( "size" );
    }


    public String getSrc() {
        return getAttributeWithNoDefault( "src" );
    }


    public String getUseMap() {
        return getAttributeWithNoDefault( "useMap" );
    }


    public void setAccept( String accept ) {
        setAttribute( "accept", accept );
    }


    public void setAccessKey( String accessKey ) {
        setAttribute( "accessKey", accessKey );
    }


    public void setAlign( String align ) {
        setAttribute( "align", align );
    }


    public void setAlt( String alt ) {
        setAttribute( "alt", alt );
    }


    public void setChecked( boolean checked ) {
        _behavior.setChecked( checked );
    }


    public void setDefaultChecked( boolean defaultChecked ) {
        setAttribute( "checked", defaultChecked );
    }


    public void setDefaultValue( String defaultValue ) {
        setAttribute( "value", defaultValue );
    }


    public void setMaxLength( int maxLength ) {
        setAttribute( "maxlength", maxLength );
    }


    public void setSize( String size ) {
        setAttribute( "size", size );
    }


    public void setSrc( String src ) {
        setAttribute( "src", src );
    }


    public void setUseMap( String useMap ) {
        setAttribute( "useMap", useMap );
    }


    public String getValue() {
        return _behavior.getValue();
    }


    public void setValue( String value ) {
        _behavior.setValue( value );
    }


    void reset() {
        _behavior.reset();
    }


    public void setAttribute( String name, String value ) throws DOMException {
        super.setAttribute( name, value );
        if (name.equalsIgnoreCase( "type" )) selectBehavior( getType().toLowerCase() );
    }


    void setState( boolean checked ) {
        _checked = checked ? Boolean.TRUE : Boolean.FALSE;
    }


    private void selectBehavior( String type ) {
        if (type == null || type.equals( "text") || type.equals( "password" ) || type.equals( "hidden" )) {
            _behavior = new EditableTextBehavior();
        } else if (type.equals( "checkbox" )) {
            _behavior = new CheckboxBehavior();
        } else if (type.equals( "radio" )) {
            _behavior = new RadioButtonBehavior();
        } else if (type.equals( "reset" )) {
            _behavior = new ResetButtonBehavior();
        } else {
            _behavior = new DefaultBehavior();
        }
    }


    interface TypeSpecificBehavior {
        void setValue( String value );
        String getValue();

        void reset();
        void click();

        boolean getChecked();
        void setChecked( boolean checked );
    }


    class DefaultBehavior implements TypeSpecificBehavior {

        public String getValue() {
            return getDefaultValue();
        }

        public void setValue( String value ) {}

        public boolean getChecked() {
            return getDefaultChecked();
        }

        public void setChecked( boolean checked ) {}

        public void reset() {}

        public void click() {}

    }


    class EditableTextBehavior extends DefaultBehavior {

        public String getValue() {
            return _value != null ? _value : getDefaultValue();
        }

        public void setValue( String value ) {
            _value = value;
        }

        public void reset() {
            _value = null;
        }

    }


    class CheckboxBehavior extends DefaultBehavior {

        public boolean getChecked() {
            return _checked != null ? _checked.booleanValue() : getDefaultChecked();
        }

        public void setChecked( boolean checked ) {
            setState( checked );
        }

        public void reset() {
            _checked = null;
        }


        public void click() {
            setChecked( !getChecked() );
        }

    }


    class RadioButtonBehavior extends CheckboxBehavior {

        public void setChecked( boolean checked ) {
            if (checked) {
                HTMLCollection elements = getForm().getElements();
                for (int i = 0; i < elements.getLength(); i++) {
                    Node node = elements.item(i);
                    if (!(node instanceof HTMLInputElementImpl)) continue;
                    HTMLInputElementImpl input = (HTMLInputElementImpl) node;
                    if (input.getName().equals( getName() ) && input.getType().equalsIgnoreCase( "radio" )) input.setState( false );
                }
            }
            setState( checked );
        }


        public void click() {
            setChecked( true );
        }

    }


    class ResetButtonBehavior extends DefaultBehavior {

        public void click() {
            getForm().reset();
        }

    }

 }
