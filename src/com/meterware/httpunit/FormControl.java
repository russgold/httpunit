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
import java.util.List;
import java.util.Arrays;
import java.io.IOException;


/**
 * Represents a control in an HTML form.
 **/
abstract class FormControl {

    final static String[] NO_VALUE = new String[0];

    private       String  _name;
    private final String  _valueAttribute;
    private final boolean _readOnly;
    private final boolean _disabled;


    FormControl() {
        _name           = "";
        _valueAttribute = "";
        _readOnly       = false;
        _disabled       = false;
    }


    FormControl( Node node ) {
        _name           = NodeUtils.getNodeAttribute( node, "name" );
        _valueAttribute = NodeUtils.getNodeAttribute( node, "value" );
        _readOnly       = NodeUtils.isNodeAttributePresent( node, "readonly" );
        _disabled       = NodeUtils.isNodeAttributePresent( node, "disabled" );
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
     * Returns a scriptable object which can act as a proxy for this control.
     */
    public ScriptableObject getScriptableObject() {
        return new Scriptable();
    }


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


    abstract void addValues( ParameterProcessor processor, String characterSet ) throws IOException;


    /**
     * Remove any required values for this control from the list, throwing an exception if they are missing.
     **/
    void claimRequiredValues( List values ) {
    }


    /**
     * Sets this control to the next compatible value from the list, removing it from the list.
     **/
    void claimValue( List values ) {
    }


    /**
     * Sets this control to the next compatible value from the list, removing it from the list.
     **/
    void claimUniqueValue( List values ) {
    }


    /**
     * Specifies a file to be uploaded via this control.
     **/
    void claimUploadSpecification( List files ) {
    }


    /**
     * Resets this control to its initial value.
     **/
    void reset() {
    }


    /**
     * Returns the default value of this control in the form. If no value is specified, defaults to the empty string.
     **/
    protected String getValueAttribute() {
        return _valueAttribute;
    }


    /**
     * Removes the specified required value from the list of values, throwing an exception if it is missing.
     **/
    final protected void claimValueIsRequired( List values, final String value ) {
        if (!values.contains( value )) throw new MissingParameterValueException( getName(), value, (String[]) values.toArray( new String[ values.size() ]) );
        values.remove( value );
    }


    static FormControl newFormParameter( WebForm form, Node node ) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        } else if (node.getNodeName().equals( "textarea" )) {
            return new TextAreaFormControl( node );
        } else if (node.getNodeName().equals( "select" )) {
            return new SelectionFormControl( node );
        } else if (node.getNodeName().equals( "button" )) {
            final String type = NodeUtils.getNodeAttribute( node, "type", "submit" );
            if (type.equalsIgnoreCase( "submit" )) {
                return new SubmitButton( form, node );
            } else {
                return null;
            }
        } else if (!node.getNodeName().equals( "input" )) {
            return null;
        } else {
            final String type = NodeUtils.getNodeAttribute( node, "type", "text" );
            if (type.equalsIgnoreCase( "text" ) || type.equalsIgnoreCase( "password" )) {
                return new TextFieldFormControl( node );
            } else if (type.equalsIgnoreCase( "hidden" )) {
                return new HiddenFieldFormControl( node );
            } else if (type.equalsIgnoreCase( "radio" )) {
                return new RadioButtonFormControl( node );
            } else if (type.equalsIgnoreCase( "checkbox" )) {
                return new CheckboxFormControl( node );
            } else if (type.equalsIgnoreCase( "submit" ) || type.equalsIgnoreCase( "image" )) {
                return new SubmitButton( form, node );
            } else if (type.equalsIgnoreCase( "file" )) {
                return new FileSubmitFormControl( node );
            } else {
                return null;
            }
        }
    }


    class Scriptable implements ScriptableObject {

        public Object get( String propertyName ) {
            throw new RuntimeException( "Unknown property: " + propertyName );
        }


        public void set( String propertyName, Object value ) {
            throw new RuntimeException( "Unknown property: " + propertyName );
        }
    }

}


abstract
class BooleanFormControl extends FormControl {

    private boolean  _isChecked;
    private String[] _value = new String[1];

    private final boolean _isCheckedDefault;



    public BooleanFormControl( Node node ) {
        super( node );
        _isChecked      = _isCheckedDefault = NodeUtils.isNodeAttributePresent( node, "checked" );
    }


    boolean isChecked() {
        return _isChecked;
    }


    public void setChecked( boolean checked ) {
        _isChecked = checked;
    }


    void reset() {
        _isChecked = _isCheckedDefault;
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


    void addValues( ParameterProcessor processor, String characterSet ) throws IOException {
        if (isChecked()) processor.addParameter( getName(), getQueryValue(), characterSet );
    }


    /**
     * Remove any required values for this control from the list, throwing an exception if they are missing.
     **/
    void claimRequiredValues( List values ) {
        if (isValueRequired()) claimValueIsRequired( values, getQueryValue() );
    }


    protected boolean isValueRequired() {
        return isReadOnly() && isChecked();
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
}


class RadioGroupFormControl extends FormControl {

    private List _buttonList = new ArrayList();
    private RadioButtonFormControl[] _buttons;
    private String[] _allowedValues;


    public RadioGroupFormControl() {
    }

    void addRadioButton( RadioButtonFormControl control ) {
        _buttonList.add( control );
        _buttons = null;
        _allowedValues = null;
    }


    public String[] getValues() {
        for (int i = 0; i < getButtons().length; i++) {
            if (getButtons()[i].isChecked()) return getButtons()[i].getValues();
        }
        return NO_VALUE;
    }


    /**
     * Returns the option values defined for this radio button group.
     **/
    public String[] getOptionValues() {
        ArrayList valueList = new ArrayList();
        FormControl[] buttons = getButtons();
        for (int i = 0; i < buttons.length; i++) {
            valueList.addAll( Arrays.asList( buttons[i].getOptionValues() ) );
        }
        return (String[]) valueList.toArray( new String[ valueList.size() ] );
    }


    void addValues( ParameterProcessor processor, String characterSet ) throws IOException {
        for (int i = 0; i < getButtons().length; i++) getButtons()[i].addValues( processor, characterSet );
    }


    /**
     * Remove any required values for this control from the list, throwing an exception if they are missing.
     **/
    void claimRequiredValues( List values ) {
        for (int i = 0; i < getButtons().length; i++) {
            getButtons()[i].claimRequiredValues( values );
        }
    }


    void claimUniqueValue( List values ) {
        int matchingButtonIndex = -1;
        for (int i = 0; i < getButtons().length && matchingButtonIndex < 0; i++) {
            if (!getButtons()[i].isReadOnly() && values.contains( getButtons()[i].getQueryValue() )) matchingButtonIndex = i;
        }
        if (matchingButtonIndex <0) throw new IllegalParameterValueException( getButtons()[0].getName(), (String) values.get(0), getAllowedValues() );

        for (int i = 0; i < getButtons().length; i++) {
            if (!getButtons()[i].isReadOnly()) getButtons()[i].setChecked( i == matchingButtonIndex );
        }
        values.remove( getButtons()[ matchingButtonIndex ].getQueryValue() );
    }


    void reset() {
        for (int i = 0; i < getButtons().length; i++) getButtons()[i].reset();
    }


    private String[] getAllowedValues() {
        if (_allowedValues == null) {
            _allowedValues = new String[ getButtons().length ];
            for (int i = 0; i < _allowedValues.length; i++) {
                _allowedValues[i] = getButtons()[i].getQueryValue();
            }
        }
        return _allowedValues;
    }


    private RadioButtonFormControl[] getButtons() {
        if (_buttons == null) _buttons = (RadioButtonFormControl[]) _buttonList.toArray( new RadioButtonFormControl[ _buttonList.size() ] );
        return _buttons;
    }
}


class CheckboxFormControl extends BooleanFormControl {


    public CheckboxFormControl( Node node ) {
        super( node );
    }


    void claimUniqueValue( List values ) {
        if (isValueRequired()) return;
        setChecked( values.contains( getQueryValue() ) );
        if (isChecked()) values.remove( getQueryValue() );
    }


    String getQueryValue() {
        final String value = getValueAttribute();
        return value.length() == 0 ? "on" : value;
    }


    private void addValue( Hashtable valueMap, String name, String value ) {
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


    public ScriptableObject getScriptableObject() {
        return new Scriptable();
    }


    void addValues( ParameterProcessor processor, String characterSet ) throws IOException {
        if (getName().length() > 0) processor.addParameter( getName(), getValues()[0], characterSet );
    }


    void claimValue( List values ) {
        if (isReadOnly()) return;
        if (values.isEmpty()) {
            _value[0] = "";
        } else {
            _value[0] = (String) values.get(0);
            values.remove(0);
        }
    }


    void reset() {
        _value[0] = null;
    }


    void claimRequiredValues( List values ) {
        if (isReadOnly()) claimValueIsRequired( values );
    }


    protected void claimValueIsRequired( List values ) {
        claimValueIsRequired( values, _defaultValue[0] );
    }

    class Scriptable extends FormControl.Scriptable {

        public void set( String propertyName, Object value ) {
            if (propertyName.equalsIgnoreCase( "value" )) {
                _value[0] =_defaultValue[0] = value.toString();
            } else {
                super.set( propertyName, value );
            }
        }
    }
}


class TextFieldFormControl extends TextFormControl {
    public TextFieldFormControl( Node node ) {
        super( node, NodeUtils.getNodeAttribute( node, "value" ) );
    }

}


class HiddenFieldFormControl extends TextFieldFormControl {
    public HiddenFieldFormControl( Node node ) {
        super( node );
    }


    void claimRequiredValues( List values ) {
        claimValueIsRequired( values );
    }


    void claimValue( List values ) {
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

    private UploadFileSpec _fileToUpload;


    public FileSubmitFormControl( Node node ) {
        super( node );
    }


    /**
     * Returns true if this control accepts a file for upload.
     **/
    public boolean isFileParameter() {
        return true;
    }


    /**
     * Returns the name of the selected file, if any.
     */
    public String[] getValues() {
        return new String[] { _fileToUpload == null ? "" : _fileToUpload.getFileName() };
    }


    /**
     * Specifies a number of file upload specifications for this control.
     **/
    void claimUploadSpecification( List files ) {
        if (files.isEmpty()) {
            _fileToUpload = null;
        } else {
            _fileToUpload = (UploadFileSpec) files.get(0);
            files.remove(0);
        }
    }


    void addValues( ParameterProcessor processor, String characterSet ) throws IOException {
        if (!isDisabled() && _fileToUpload != null) {
            processor.addFile( getName(), _fileToUpload );
        }
    }
}


class SelectionFormControl extends FormControl {
    private final boolean _multiSelect;
    private final String[] _optionValues;
    private final String[] _displayedOptions;

    private String[]  _values;
    private boolean[] _selectionIndexes;
    private boolean[] _initialIndexes;


    SelectionFormControl( Node node ) {
        super( node );
        if (!node.getNodeName().equalsIgnoreCase( "select" )) throw new RuntimeException( "Not a select element" );

        _multiSelect      = NodeUtils.isNodeAttributePresent( node, "multiple" );
        _optionValues     = getInitialOptionValues( node );
        _displayedOptions = getInitialDisplayedOptions( node );

        _initialIndexes   = getInitialSelectionIndexes( node );
        _selectionIndexes = (boolean[]) _initialIndexes.clone();
        _values           = getSelectedValues( _selectionIndexes, _optionValues );
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


    void addValues( ParameterProcessor processor, String characterSet ) throws IOException {
        for (int i = 0; i < getValues().length; i++) {
            processor.addParameter( getName(), getValues()[i], characterSet );
        }
    }


    void claimUniqueValue( List values ) {
        boolean[] matches = new boolean[ _optionValues.length ];
        int numMatches = 0;
        for (int i = 0; i < matches.length; i++) {
            matches[i] = values.contains( _optionValues[i] );
            if (matches[i]) {
                numMatches++;
                if (!_multiSelect) break;
            }
        }

        if (!_multiSelect) {
            if (numMatches == 0) throw new IllegalParameterValueException( getName(), (String) values.get(0), getOptionValues() );
        }

        for (int i = 0; i < matches.length; i++) {
            if (matches[i]) values.remove( _optionValues[i] );
        }

        _selectionIndexes = matches;
        _values = getSelectedValues( matches, _optionValues );
    }


    void reset() {
        _selectionIndexes = (boolean[]) _initialIndexes.clone();
        _values           = getSelectedValues( _selectionIndexes, _optionValues );
    }


    private String[] getSelectedValues( boolean[] selected, String[] optionValues ) {
        ArrayList values = new ArrayList();
        for (int i = 0; i < selected.length; i++) {
            if (selected[i]) {
                values.add( optionValues[i] );
            }
        }
        return (String[]) values.toArray( new String[ values.size() ] );
    }


    private boolean[] getInitialSelectionIndexes( Node selectionNode ) {
        boolean noneSelected = true;
        NodeList nl = ((Element) selectionNode).getElementsByTagName( "option" );
        boolean isSelected[] = new boolean[ nl.getLength() ];
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getAttributes().getNamedItem( "selected" ) != null) {
                isSelected[i] = true;
                noneSelected = false;
            }
        }

        if (!isMultiValued() && noneSelected && isSelected.length > 0) {
            isSelected[0] = true;
        }
        return isSelected;
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
//============================= exception class IllegalParameterValueException ======================================


/**
 * This exception is thrown on an attempt to set a parameter to a value not permitted to it by the form.
 **/
class IllegalParameterValueException extends IllegalRequestParameterException {


    IllegalParameterValueException( String parameterName, String badValue, String[] allowed ) {
        _parameterName = parameterName;
        _badValue      = badValue;
        _allowedValues = allowed;
    }


    public String getMessage() {
        StringBuffer sb = new StringBuffer(HttpUnitUtils.DEFAULT_TEXT_BUFFER_SIZE);
        sb.append( "May not set parameter '" ).append( _parameterName ).append( "' to '" );
        sb.append( _badValue ).append( "'. Value must be one of: { " );
        for (int i = 0; i < _allowedValues.length; i++) {
            if (i != 0) sb.append( ", " );
            sb.append( _allowedValues[i] );
        }
        sb.append( " }" );
        return sb.toString();
    }


    private String   _parameterName;
    private String   _badValue;
    private String[] _allowedValues;
}

//============================= exception class MissingParameterValueException ======================================


/**
 * This exception is thrown on an attempt to remove a required value from a form parameter.
 **/
class MissingParameterValueException extends IllegalRequestParameterException {


    MissingParameterValueException( String parameterName, String missingValue, String[] proposed ) {
        _parameterName  = parameterName;
        _missingValue   = missingValue;
        _proposedValues = proposed;
    }


    public String getMessage() {
        StringBuffer sb = new StringBuffer(HttpUnitUtils.DEFAULT_TEXT_BUFFER_SIZE);
        sb.append( "Parameter '" ).append( _parameterName ).append( "' must have the value '" );
        sb.append( _missingValue ).append( "'. Attempted to set it to: { " );
        for (int i = 0; i < _proposedValues.length; i++) {
            if (i != 0) sb.append( ", " );
            sb.append( _proposedValues[i] );
        }
        sb.append( " }" );
        return sb.toString();
    }


    private String   _parameterName;
    private String   _missingValue;
    private String[] _proposedValues;
}


