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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Hashtable;
import java.util.Vector;


/**
 * Represents a control in an HTML form.
 **/
abstract class FormControl {

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
    String getName() {
        return _name;
    }


    /**
     * Returns the current value(s) associated with this control. These values will be transmitted to the server
     * if the control is 'successful'.
     **/
    abstract String[] getValues();


    /**
     * Returns true if this control is read-only.
     **/
    boolean isReadOnly() {
        return _readOnly;
    }


    /**
     * Returns true if this control is disabled, meaning that it will not send a value to the server as part of a request.
     **/
    boolean isDisabled() {
        return _disabled;
    }


    /**
     * Returns true if this control accepts free-form text.
     **/
    boolean isTextControl() {
        return false;
    }


    /**
     * Returns true if only one control of this kind can have a value. This is true for radio buttons.
     **/
    boolean isExclusive() {
        return false;
    }


    /**
     * Returns true if a single control can have multiple values.
     **/
    boolean isMultiValued() {
        return false;
    }


    /**
     * Returns true if this control accepts a file for upload.
     **/
    boolean isFileParameter() {
        return false;
    }


    void updateRequiredValues( Hashtable required ) {
    }


    void updateParameterOptions( Hashtable options ) {
    }


    void updateParameterOptionValues( Hashtable options ) {
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

    final static String[] NO_VALUE = new String[0];

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
     * Returns the default value of this control in the form. If no value is specified, defaults to the empty string.
     **/
    String getValueAttribute() {
        return _valueAttribute;
    }


    /**
     * Returns the current value(s) associated with this control. These values will be transmitted to the server
     * if the control is 'successful'.
     **/
    String[] getValues() {
        return isChecked() ? toArray( getQueryValue() ) : NO_VALUE;
    }


    abstract String getQueryValue();


    void updateParameterOptionValues( Hashtable options ) {
        if (isChecked() || !isReadOnly()) {
            addValue( options, getName(), getQueryValue() );
        }
    }

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
    boolean isExclusive() {
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
    String[] getValues() {
        return (_value[0] != null) ? _value : _defaultValue;
   }


    /**
     * Returns true to indicate that this control accepts free-form text.
     **/
    boolean isTextControl() {
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
    boolean isFileParameter() {
        return true;
    }


    String[] getValues() {
        return null;   // XXX what should this really do?
    }
}


class SelectionFormControl extends FormControl {
    private boolean _multiSelect;
    private Node    _node;


    SelectionFormControl( Node node ) {
        super( node );
        if (!node.getNodeName().equalsIgnoreCase( "select" )) throw new RuntimeException( "Not a select element" );

        _node = node;
        _multiSelect = NodeUtils.isNodeAttributePresent( node, "multiple" );
    }


    String[] getValues() {
        return getSelected();
    }


    /**
     * Returns true if a single control can have multiple values.
     **/
    boolean isMultiValued() {
        return _multiSelect;
    }


    void updateRequiredParameters( Hashtable required ) {
        if (isReadOnly()) required.put( getName(), getSelected() );
    }


    void updateParameterOptions( Hashtable options ) {
        options.put( getName(), getOptions() );
    }


    void updateParameterOptionValues( Hashtable options ) {
        options.put( getName(), getOptionValues() );
    }


    String[] getSelected() {
        Vector selected = new Vector();
        NodeList nl = ((Element) _node).getElementsByTagName( "option" );
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getAttributes().getNamedItem( "selected" ) != null) {
                selected.addElement( getOptionValue( nl.item(i) ) );
            }
        }

        if (!isMultiValued() && selected.size() == 0 && nl.getLength() > 0) {
            selected.addElement( getOptionValue( nl.item(0) ) );
        }

        String[] result = new String[ selected.size() ];
        selected.copyInto( result );
        return result;
    }


    String[] getOptions() {
        Vector options = new Vector();
        NodeList nl = ((Element) _node).getElementsByTagName( "option" );
        for (int i = 0; i < nl.getLength(); i++) {
            options.addElement( getValue( nl.item(i).getFirstChild() ) );
        }
        String[] result = new String[ options.size() ];
        options.copyInto( result );
        return result;
    }


    String[] getOptionValues() {
        Vector options = new Vector();
        NodeList nl = ((Element) _node).getElementsByTagName( "option" );
        for (int i = 0; i < nl.getLength(); i++) {
            options.addElement( getOptionValue( nl.item(i) ) );
        }
        String[] result = new String[ options.size() ];
        options.copyInto( result );
        return result;
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


