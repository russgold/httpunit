package com.meterware.httpunit;
/********************************************************************************************************************
* $Id$
*
* Copyright (c) 2001-2002, Russell Gold
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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Hashtable;
import java.util.Vector;
import java.util.ArrayList;


/**
 * Represents a control in an HTML form.
 **/
abstract class FormControl {

    final static String[] NO_VALUE = new String[0];

    private String  _name;
    private boolean _readOnly;
    private boolean _disabled;


    FormControl( Node node ) {
        _name         = NodeUtils.getNodeAttribute( node, "name" );
        _readOnly     = NodeUtils.isNodeAttributePresent( node, "readonly" );
        _disabled     = NodeUtils.isNodeAttributePresent( node, "disabled" );
    }


    /**
     * Returns the name of this control. If no name is specified, defaults to the empty string.
     **/
    public String getName() {
        return _name;
    }


    /**
     * Returns the current value(s) associated with this control. These values will be transmitted to the server
     * if the control is 'successful'.
     **/
    abstract public String[] getValues();


    /**
     * Returns the values permitted in this control. Does not apply to text or file controls.
     **/
    public String[] getOptionValues() {
        return NO_VALUE;
    }


    /**
     * Returns the list of values displayed by this control, if any.
     **/
    public String[] getDisplayedOptions() {
        return NO_VALUE;
    }


    /**
     * Returns true if this control is read-only.
     **/
    public boolean isReadOnly() {
        return _readOnly;
    }


    /**
     * Returns true if this control is disabled, meaning that it will not send a value to the server as part of a request.
     **/
    public boolean isDisabled() {
        return _disabled;
    }


    /**
     * Returns true if this control accepts free-form text.
     **/
    public boolean isTextControl() {
        return false;
    }


    /**
     * Returns true if only one control of this kind can have a value. This is true for radio buttons.
     **/
    public boolean isExclusive() {
        return false;
    }


    /**
     * Returns true if a single control can have multiple values.
     **/
    public boolean isMultiValued() {
        return false;
    }


    /**
     * Returns true if this control accepts a file for upload.
     **/
    public boolean isFileParameter() {
        return false;
    }


    void updateRequiredValues( Hashtable required ) {
    }


    protected void addValue( Hashtable valueMap, String name, String value ) {
        String[] currentValues = (String[]) valueMap.get( name );
        if (currentValues == null) {
            valueMap.put( name, new String[] { value } );
        } else {
            valueMap.put( name, withNewValue( currentValues, value ) );
        }
    }


    /**
     * Adds a string to an array of strings and returns the result.
     **/
    private String[] withNewValue( String[] group, String value ) {
        String[] result = new String[ group.length+1 ];
        System.arraycopy( group, 0, result, 0, group.length );
        result[ group.length ] = value;
        return result;
    }


    static FormControl newFormParameter( Node node ) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        } else if (node.getNodeName().equals( "textarea" )) {
            return new TextAreaFormControl( node );
        } else if (node.getNodeName().equals( "select" )) {
            return new SelectionFormControl( node );
        } else if (!node.getNodeName().equals( "input" )) {
            return null;
        } else {
            final String type = NodeUtils.getNodeAttribute( node, "type", "text" );
            if (type.equalsIgnoreCase( "text" ) || type.equalsIgnoreCase( "hidden" ) || type.equalsIgnoreCase( "password" )) {
                return new TextFieldFormControl( node );
            } else if (type.equalsIgnoreCase( "radio" )) {
                return new RadioButtonFormControl( node );
            } else if (type.equalsIgnoreCase( "checkbox" )) {
                return new CheckboxFormControl( node );
            } else if (type.equalsIgnoreCase( "file" )) {
                return new FileSubmitFormControl( node );
            } else {
                return null;
            }
        }
    }

}


abstract
class BooleanFormControl extends FormControl {

    private boolean  _isChecked;
    private String[] _value = new String[1];

    private final boolean _isCheckedDefault;
    private final String  _valueAttribute;



    public BooleanFormControl( Node node ) {
        super( node );
        _isChecked      = _isCheckedDefault = NodeUtils.isNodeAttributePresent( node, "checked" );
        _valueAttribute = NodeUtils.getNodeAttribute( node, "value" );
    }


    boolean isChecked() {
        return _isChecked;
    }


    /**
     * Returns the current value(s) associated with this control. These values will be transmitted to the server
     * if the control is 'successful'.
     **/
    public String[] getValues() {
        return isChecked() ? toArray( getQueryValue() ) : NO_VALUE;
    }


    /**
     * Returns the values permitted in this control.
     **/
    public String[] getOptionValues() {
        return (isReadOnly() && !isChecked()) ? NO_VALUE : toArray( getQueryValue() );
    }


    /**
     * Returns the default value of this control in the form. If no value is specified, defaults to the empty string.
     **/
    String getValueAttribute() {
        return _valueAttribute;
    }


    abstract String getQueryValue();


    private String[] toArray( String value ) {
        _value[0] = value;
        return _value;
    }
}



class RadioButtonFormControl extends BooleanFormControl {

    public RadioButtonFormControl( Node node ) {
        super( node );
    }


    /**
     * Returns true if only one control of this kind can have a value.
     **/
    public boolean isExclusive() {
        return true;
    }


    String getQueryValue() {
        return getValueAttribute();
    }


    void updateRequiredValues( Hashtable required ) {
        if (isReadOnly() && isChecked()) {
            required.put( getName(), getQueryValue() );
        }
    }
}


class CheckboxFormControl extends BooleanFormControl {
    public CheckboxFormControl( Node node ) {
        super( node );
    }


    void updateRequiredValues( Hashtable required ) {
        if (isReadOnly() && isChecked()) {
            addValue( required, getName(), getQueryValue() );
        }
    }


    String getQueryValue() {
        final String value = getValueAttribute();
        return value.length() == 0 ? "on" : value;
    }
}


class TextFormControl extends FormControl {

    private String[] _value = new String[1];
    private String[] _defaultValue;


    public TextFormControl( Node node, String defaultValue ) {
        super( node );
        _defaultValue = new String[] { defaultValue };
    }


    /**
     * Returns the current value(s) associated with this control. These values will be transmitted to the server
     * if the control is 'successful'.
     **/
    public String[] getValues() {
        return (_value[0] != null) ? _value : _defaultValue;
   }


    /**
     * Returns true to indicate that this control accepts free-form text.
     **/
    public boolean isTextControl() {
        return true;
    }


    void updateRequiredValues( Hashtable required ) {
        if (isReadOnly()) required.put( getName(), _defaultValue );
    }
}


class TextFieldFormControl extends TextFormControl {
    public TextFieldFormControl( Node node ) {
        super( node, NodeUtils.getNodeAttribute( node, "value" ) );
    }

}


class TextAreaFormControl extends TextFormControl {

    public TextAreaFormControl( Node node ) {
        super( node, getDefaultValue( node ) );

        if (!node.getNodeName().equalsIgnoreCase( "textarea" )) {
            throw new RuntimeException( "Not a textarea element" );
        }
    }


    private static String getDefaultValue( Node node ) {
        return NodeUtils.asText( node.getChildNodes() );
    }

}


class FileSubmitFormControl extends FormControl {

    public FileSubmitFormControl( Node node ) {
        super( node );
    }


    /**
     * Returns true if this control accepts a file for upload.
     **/
    public boolean isFileParameter() {
        return true;
    }


    public String[] getValues() {
        return null;   // XXX what should this really do?
    }
}


class SelectionFormControl extends FormControl {
    private final boolean _multiSelect;
    private final String[] _optionValues;
    private final String[] _displayedOptions;

    private String[] _values;


    SelectionFormControl( Node node ) {
        super( node );
        if (!node.getNodeName().equalsIgnoreCase( "select" )) throw new RuntimeException( "Not a select element" );

        _multiSelect      = NodeUtils.isNodeAttributePresent( node, "multiple" );
        _optionValues     = getInitialOptionValues( node );
        _displayedOptions = getInitialDisplayedOptions( node );
        _values           = getInitialValues( node, _optionValues );
    }


    public String[] getValues() {
        return _values;
    }


    public String[] getOptionValues() {
        return _optionValues;
    }


    public String[] getDisplayedOptions() {
        return _displayedOptions;
    }


    /**
     * Returns true if a single control can have multiple values.
     **/
    public boolean isMultiValued() {
        return _multiSelect;
    }


    void updateRequiredParameters( Hashtable required ) {
        if (isReadOnly()) required.put( getName(), getValues() );
    }


    private String[] getInitialValues( Node selectionNode, String[] optionValues ) {
        ArrayList selected = new ArrayList();
        NodeList nl = ((Element) selectionNode).getElementsByTagName( "option" );
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getAttributes().getNamedItem( "selected" ) != null) {
                selected.add( optionValues[i] );
            }
        }

        if (!isMultiValued() && selected.size() == 0 && nl.getLength() > 0) {
            selected.add( optionValues[0] );
        }

        return (String[]) selected.toArray( new String[ selected.size() ] );
    }


    private String[] getInitialDisplayedOptions( Node node ) {
        Vector options = new Vector();
        NodeList nl = ((Element) node).getElementsByTagName( "option" );
        for (int i = 0; i < nl.getLength(); i++) {
            options.addElement( getValue( nl.item(i).getFirstChild() ) );
        }
        String[] result = new String[ options.size() ];
        options.copyInto( result );
        return result;
    }


    private String[] getInitialOptionValues( Node selectNode ) {
        NodeList nl = ((Element) selectNode).getElementsByTagName( "option" );
        ArrayList options = new ArrayList( nl.getLength() );
        for (int i = 0; i < nl.getLength(); i++) {
            options.add( getOptionValue( nl.item(i) ) );
        }
        return (String[]) options.toArray( new String[ options.size() ] );
    }


    private String getOptionValue( Node optionNode ) {
        NamedNodeMap nnm = optionNode.getAttributes();
        if (nnm.getNamedItem( "value" ) != null) {
            return getValue( nnm.getNamedItem( "value" ) );
        } else {
            return getValue( optionNode.getFirstChild() );
        }
    }

    private static String getValue( Node node ) {
        return (node == null) ? "" : emptyIfNull( node.getNodeValue() );
    }


    private static String emptyIfNull( String value ) {
        return (value == null) ? "" : value;
    }

}


